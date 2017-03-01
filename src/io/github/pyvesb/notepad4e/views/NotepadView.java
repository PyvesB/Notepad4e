package io.github.pyvesb.notepad4e.views;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.part.ViewPart;

import io.github.pyvesb.notepad4e.Notepad4e;
import io.github.pyvesb.notepad4e.preferences.PreferenceConstants;
import io.github.pyvesb.notepad4e.utils.NotepadAction;
import io.github.pyvesb.notepad4e.utils.ShortcutHandler;

/**
 * Class handling the plugin's view with the different note tabs.
 * 
 * @author Pyves
 *
 */
public class NotepadView extends ViewPart implements IPreferenceChangeListener {

	// Various constants.
	private static final String LOCK_CHARACTER = "\uD83D\uDD12";
	private static final int SAVE_INTERVAL_MILLIS = 120000;
	// The ID of the view as specified by the extension.
	public static final String ID = "notepad4e.views.NotepadView";
	// Keys used to store and retrieve the plugin's view between Eclipse sessions.
	private static final String STORE_COUNT_KEY = "NumOfTabs";
	private static final String STORE_TEXT_PREFIX_KEY = "TabText";
	private static final String STORE_STYLE_PREFIX_KEY = "TabStyle";
	private static final String STORE_TITLE_PREFIX_KEY = "TabTitle";
	private static final String STORE_EDITABLE_PREFIX_KEY = "TabEditable";

	// Keyboard events listener.
	private final ShortcutHandler shortcutHandler;

	// Actions corresponding to the different buttons in the view.
	private Action addNewNoteAction;
	private Action clearNoteAction;
	private Action boldTextAction;
	private Action italicTextAction;
	private Action underlineTextAction;
	private Action strikeoutTextAction;
	private Action clearTextStyleAction;
	private Action toggleEditableAction;
	private Action saveNoteAction;
	private Action preferencesAction;
	private Action websiteAction;
	private Action changelogAction;
	// User defined preferences.
	private IEclipsePreferences preferences;
	// Object handling the different tabs.
	private CTabFolder tabFolder;
	// Current clipboard, used for the paste contents of clipboard in new notes feature.
	private Clipboard clipboard;

	/**
	 * Constructor.
	 */
	public NotepadView() {
		shortcutHandler = new ShortcutHandler(this);
	}

	/**
	 * Allows to create the viewer and initialise it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		preferences = InstanceScope.INSTANCE.getNode(Notepad4e.PLUGIN_ID);
		// Listen to any change to the preferences of the plugin.
		preferences.addPreferenceChangeListener(this);

		clipboard = new Clipboard(Display.getCurrent());
		
		tabFolder = new CTabFolder(parent, SWT.MULTI | SWT.WRAP);

		addPluginDisposeListener();
		addCloseTabListener();
		addSwapTabListener();
		addRenameTabListener();
		addTabSelectionListener();

		restoreViewFromPreviousSession();

		Job autosaveJob = new Job("ScheduledAutosave") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						savePluginState();
					}
				});
				schedule(SAVE_INTERVAL_MILLIS);
				return Status.OK_STATUS;
			}
		};
		autosaveJob.schedule(SAVE_INTERVAL_MILLIS);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(tabFolder, "Notepad4e.viewer");

		IContextService contextService = getSite().getService(IContextService.class);
		contextService.activateContext("notepad4e.context");

		IHandlerService handlerService = getSite().getService(IHandlerService.class);
		// Associate each shortcut command with the shortcut handler.
		for (NotepadAction notepadAction : NotepadAction.values()) {
			if (notepadAction.getCommandID() != null) {
				handlerService.activateHandler(notepadAction.getCommandID(), shortcutHandler);
			}
		}

		makeActions();
		contributeToActionBars();
	}

	/**
	 * Unregisters listeners and cleans up.
	 */
	@Override
	public void dispose() {
		shortcutHandler.dispose();
		preferences.removePreferenceChangeListener(this);
		super.dispose();
	}

	/**
	 * Refreshes all notes when a change in the plugin's preferences is detected.
	 * 
	 * @param event
	 */
	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		for (int tabIndex = 0; tabIndex < tabFolder.getItemCount(); ++tabIndex) {
			getNote(tabIndex).setParametersFromPreferences();
		}
	}

	/**
	 * Passes the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		if (tabFolder.getItemCount() == 0) {
			// Give focus to the plugin; hack-ish trick to "steal" focus from other elements in some scenarios (example:
			// no tabs and try to open view again via quick access).
			tabFolder.getAccessible().getControl().setFocus();
		} else {
			// Set focus on the last item in the tabs folder component.
			tabFolder.getItem(tabFolder.getItemCount() - 1).getControl().setFocus();
		}
	}

	/**
	 * Performs the new note action.
	 */
	public void doNewNoteTab() {
		String namePrefix = preferences.get(PreferenceConstants.PREF_NAME_PREFIX,
				PreferenceConstants.PREF_NAME_PREFIX_DEFAULT);
		String noteText = "";
		if (preferences.getBoolean(PreferenceConstants.PREF_PASTE_CLIPBOARD_IN_NEW_NOTES,
				PreferenceConstants.PREF_PASTE_CLIPBOARD_IN_NEW_NOTES_DEFAULT)) {
			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			noteText = (String) clipboard.getContents(plainTextTransfer, DND.CLIPBOARD);
		}
		// Add a new note tab with a number appended to its name (Note 1, Note 2, Note 3, etc.).
		addNewNoteTab(namePrefix + " " + (tabFolder.getItemCount() + 1), noteText, "", true);
		tabFolder.setSelection(tabFolder.getItemCount() - 1);
	}

	/**
	 * Performs the clear note action.
	 */
	public void doClearNote() {
		if (tabFolder.getItemCount() > 0) {
			getNote(tabFolder.getSelectionIndex()).clearText();
		}
	}

	/**
	 * Performs the bold text action.
	 */
	public void doBoldText() {
		if (tabFolder.getItemCount() > 0) {
			getNote(tabFolder.getSelectionIndex()).boldSelection();
		}
	}

	/**
	 * Performs the italic text action.
	 */
	public void doItalicText() {
		if (tabFolder.getItemCount() > 0) {
			getNote(tabFolder.getSelectionIndex()).italicSelection();
		}
	}

	/**
	 * Performs the underline text action.
	 */
	public void doUnderlineText() {
		if (tabFolder.getItemCount() > 0) {
			getNote(tabFolder.getSelectionIndex()).underlineSelection();
		}
	}
	
	/**
	 * Performs the strikeout text action.
	 */
	public void doStrikeoutText() {
		if (tabFolder.getItemCount() > 0) {
			getNote(tabFolder.getSelectionIndex()).strikeoutSelection();
		}
	}

	/**
	 * Performs the clear text action.
	 */
	public void doClearTextStyle() {
		if (tabFolder.getItemCount() > 0) {
			getNote(tabFolder.getSelectionIndex()).clearSelectionStyles();
		}
	}

	/**
	 * Performs the undo text action.
	 */
	public void doUndoText() {
		getNote(tabFolder.getSelectionIndex()).undo();
	}

	/**
	 * Performs the redo text action.
	 */
	public void doRedoText() {
		getNote(tabFolder.getSelectionIndex()).redo();
	}

	/**
	 * Performs the close action.
	 */
	public void doCloseNoteTab() {
		if (tabFolder.getItemCount() > 0) {
			if (!getNote(tabFolder.getSelectionIndex()).getEditable()) {
				if (MessageDialog.openQuestion(getSite().getShell(), "Close Locked Note",
						"This note is locked. Are you really sure you want to close it?")) {
					tabFolder.getItem(tabFolder.getSelectionIndex()).dispose();
				}
			} else if (!preferences.getBoolean(PreferenceConstants.PREF_CLOSE_CONFIRMATION,
					PreferenceConstants.PREF_CLOSE_CONFIRMATION_DEFAULT)
					|| MessageDialog.openQuestion(getSite().getShell(), "Close Note",
							"Are you sure you want to close this note?")) {
				tabFolder.getItem(tabFolder.getSelectionIndex()).dispose();
			}
		}
	}

	/**
	 * Listens to disposal of the tab folder and saves state for next Eclipse session or when reopening the view.
	 */
	private void addPluginDisposeListener() {
		tabFolder.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				savePluginState();
			}
		});
	}

	/**
	 * Saves plugin state for next Eclipse session or when reopening the view.
	 */
	private void savePluginState() {
		if (!tabFolder.isDisposed()) {
			IDialogSettings section = Notepad4e.getDefault().getDialogSettings().getSection(ID);
			section.put(STORE_COUNT_KEY, tabFolder.getItemCount());
			for (int tabIndex = 0; tabIndex < tabFolder.getItemCount(); ++tabIndex) {
				if (tabFolder.getItem(tabIndex).isDisposed()) {
					Note note = getNote(tabIndex);
					section.put(STORE_TEXT_PREFIX_KEY + tabIndex, note.getText());
					section.put(STORE_STYLE_PREFIX_KEY + tabIndex, note.serialiseStyle());
					section.put(STORE_TITLE_PREFIX_KEY + tabIndex, tabFolder.getItem(tabIndex).getText());
					section.put(STORE_EDITABLE_PREFIX_KEY + tabIndex, note.getEditable());
				}
			}
			Notepad4e.save();
		}
	}

	/**
	 * Displays a confirmation dialog when closing a note tab, if enabled in preferences.
	 */
	private void addCloseTabListener() {
		tabFolder.addCTabFolder2Listener(new CTabFolder2Listener() {
			@Override
			public void close(CTabFolderEvent event) {
				// Selected tab may not be the one being closed, the one provided by the event must be used.
				if (!getNote(tabFolder.indexOf((CTabItem) event.item)).getEditable()) {
					event.doit = MessageDialog.openQuestion(getSite().getShell(), "Close Locked Note",
							"This note is locked. Are you really sure you want to close it?");
				} else if (preferences.getBoolean(PreferenceConstants.PREF_CLOSE_CONFIRMATION,
						PreferenceConstants.PREF_CLOSE_CONFIRMATION_DEFAULT)) {
					event.doit = MessageDialog.openQuestion(getSite().getShell(), "Close Note",
							"Are you sure you want to close this note?");
				}
			}

			@Override
			public void minimize(CTabFolderEvent event) {}

			@Override
			public void maximize(CTabFolderEvent event) {}

			@Override
			public void restore(CTabFolderEvent event) {}

			@Override
			public void showList(CTabFolderEvent event) {}
		});
	}

	/**
	 * Allows to rename a tab when user double clicks on its title.
	 */
	private void addRenameTabListener() {
		tabFolder.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent event) {}

			@Override
			public void mouseDown(MouseEvent event) {}

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				CTabItem clickedTab = tabFolder.getItem(new Point(event.x, event.y));
				if (clickedTab == null) {
					return;
				}
				boolean isLocked = false;
				String dialogText = clickedTab.getText();
				if (dialogText.startsWith(LOCK_CHARACTER)) {
					isLocked = true;
					dialogText = dialogText.substring(3);
				}
				// Open a dialog window so user can enter the new name of his note.
				InputDialog inputDialog = new InputDialog(null, "Rename Note",
						"Please select the new name of the note:", dialogText, null);
				inputDialog.open();
				// If user selected Cancel, text will be null.
				if (inputDialog.getValue() != null && !inputDialog.getValue().isEmpty()) {
					if (isLocked) {
						clickedTab.setText(LOCK_CHARACTER + " " + inputDialog.getValue());
					} else {
						clickedTab.setText(inputDialog.getValue());
					}
				}
			}
		});
	}

	/**
	 * Swaps two tabs and corresponding notes when a user drags one to another.
	 */
	private void addSwapTabListener() {
		tabFolder.addDragDetectListener(new DragDetectListener() {
			@Override
			public void dragDetected(DragDetectEvent dragDetectedEvent) {
				final Rectangle viewRectangle = Geometry.toDisplay(tabFolder.getParent(),
						tabFolder.getBounds());
				final Tracker tracker = new Tracker(tabFolder, SWT.NONE);
				tracker.setStippled(true);
				tracker.addListener(SWT.Move, new Listener() {
					@Override
					public void handleEvent(Event event) {
						Point location = new Point(event.x - viewRectangle.x, event.y - viewRectangle.y);
						CTabItem tabAtLocation = tabFolder.getItem(location);
						if (tabAtLocation != null) {
							// Move tracker to follow mouse cursor.
							tracker.setRectangles(new Rectangle[] { tabAtLocation.getBounds() });
						} else {
							// Mouse cursor no longer above any tab in the action bar, hide tacker.
							tracker.setRectangles(new Rectangle[0]);
						}
					}
				});
				if (tracker.open()) {
					Rectangle[] rectangles = tracker.getRectangles();
					if (rectangles.length > 0) {
						CTabItem tabToSwap = tabFolder.getItem(new Point(rectangles[0].x, rectangles[0].y));
						// Swap selected tab with the one situated at the mouse cursor's position.
						if (tabToSwap != null) {
							swapNoteTabs(tabFolder.getSelectionIndex(), tabFolder.indexOf(tabToSwap));
						}
					}
				}
				tracker.close();
				tracker.dispose();
			}
		});
	}

	/**
	 * Listens for tab selections and displays or removes lock symbol when a locked tab is selected.
	 */
	private void addTabSelectionListener() {
		tabFolder.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// Remove lock symbols from all tabs.
				for (int tabIndex = 0; tabIndex < tabFolder.getItemCount(); ++tabIndex) {
					CTabItem tab = tabFolder.getItem(tabIndex);
					if (tab.getText().startsWith(LOCK_CHARACTER)) {
						tab.setText(tab.getText().substring(3));
					}
				}
				// Put lock symbol on selected tab, if non editable.
				if (!getNote(tabFolder.getSelectionIndex()).getEditable()) {
					CTabItem selectedTab = (CTabItem) event.item;
					selectedTab.setText(LOCK_CHARACTER + " " + selectedTab.getText());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {}
		});
	}

	/**
	 * Returns a Note object given an index in the tab folder.
	 * 
	 * @param index
	 * @return
	 */
	private Note getNote(int index) {
		return (Note) (tabFolder.getItem(index).getControl());
	}

	/**
	 * Allows to restore the plugin's view as it was in a previous session of Eclipse.
	 */
	private void restoreViewFromPreviousSession() {
		IDialogSettings settings = Notepad4e.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(ID);
		if (section == null) {
			section = settings.addNewSection(ID);
		}

		int numOfTabs = 0;
		String numOfTabsString = section.get(STORE_COUNT_KEY);
		// numOfTabsString can be null if plugin was not previously launched in this working environment.
		if (numOfTabsString != null) {
			numOfTabs = Integer.parseInt(numOfTabsString);
		}

		if (numOfTabs == 0) {
			// No notes were previously opened: create new one.
			String prefixName = preferences.get(PreferenceConstants.PREF_NAME_PREFIX,
					PreferenceConstants.PREF_NAME_PREFIX_DEFAULT);
			addNewNoteTab(prefixName + " 1", "", "", true);
			// Set selection on this tab.
			tabFolder.setSelection(0);
		} else {
			// Populate with tabs opened in previous session.
			for (int tabIndex = 0; tabIndex < numOfTabs; ++tabIndex) {
				String tabTitle = section.get(STORE_TITLE_PREFIX_KEY + tabIndex);
				String noteText = section.get(STORE_TEXT_PREFIX_KEY + tabIndex);
				String noteStyle = section.get(STORE_STYLE_PREFIX_KEY + tabIndex);
				boolean editable = section.get(STORE_EDITABLE_PREFIX_KEY + tabIndex) == null ? true
						: section.getBoolean(STORE_EDITABLE_PREFIX_KEY + tabIndex);
				addNewNoteTab(tabTitle, noteText, noteStyle, editable);
				// Set selection on the last tab.
				tabFolder.setSelection(numOfTabs - 1);
			}
		}
	}

	/**
	 * Adds a new note to the view.
	 * 
	 * @param title
	 * @param text
	 * @param style
	 * @param editable
	 */
	private void addNewNoteTab(String title, String text, String style, boolean editable) {
		CTabItem tab = new CTabItem(tabFolder, SWT.NONE);
		tab.setText(title);
		// Add listener to clean up corresponding note when disposing the tab.
		tab.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				CTabItem itemToDispose = (CTabItem) event.getSource();
				((Note) itemToDispose.getControl()).dispose();
			}
		});
		Note note = new Note(tabFolder, text, editable);
		if (style.length() > 0) {
			note.deserialiseStyle(style);
		}
		tab.setControl(note);
	}

	/**
	 * Populates the different action bars of the view.
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Populates the drop down menu action bar.
	 * 
	 * @param manager
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(toggleEditableAction);
		manager.add(saveNoteAction);
		manager.add(new Separator());
		manager.add(preferencesAction);
		manager.add(websiteAction);
		manager.add(changelogAction);
	}

	/**
	 * Populates the tool bar.
	 * 
	 * @param manager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(boldTextAction);
		manager.add(italicTextAction);
		manager.add(underlineTextAction);
		manager.add(strikeoutTextAction);
		manager.add(clearTextStyleAction);
		manager.add(new Separator());
		manager.add(addNewNoteAction);
		manager.add(clearNoteAction);
	}

	/**
	 * Defines all the plugin's actions corresponding to the different buttons in the view.
	 */
	private void makeActions() {
		addNewNoteAction = new Action() {
			@Override
			public void run() {
				doNewNoteTab();
			}
		};
		setTextAndImageToAction(addNewNoteAction, NotepadAction.NEW_NOTE);

		clearNoteAction = new Action() {
			@Override
			public void run() {
				doClearNote();
			}
		};
		setTextAndImageToAction(clearNoteAction, NotepadAction.CLEAR_NOTE);

		boldTextAction = new Action() {
			@Override
			public void run() {
				doBoldText();
			}
		};
		setTextAndImageToAction(boldTextAction, NotepadAction.BOLD_TEXT);

		italicTextAction = new Action() {
			@Override
			public void run() {
				doItalicText();
			}
		};
		setTextAndImageToAction(italicTextAction, NotepadAction.ITALIC_TEXT);

		underlineTextAction = new Action() {
			@Override
			public void run() {
				doUnderlineText();
			}
		};
		setTextAndImageToAction(underlineTextAction, NotepadAction.UNDERLINE_TEXT);
		
		strikeoutTextAction = new Action() {
			@Override
			public void run() {
				doStrikeoutText();
			}
		};
		setTextAndImageToAction(strikeoutTextAction, NotepadAction.STRIKEOUT_TEXT);

		clearTextStyleAction = new Action() {
			@Override
			public void run() {
				doClearTextStyle();
			}
		};
		setTextAndImageToAction(clearTextStyleAction, NotepadAction.CLEAR_STYLE_TEXT);

		toggleEditableAction = new Action() {
			@Override
			public void run() {
				if (tabFolder.getItemCount() > 0) {
					CTabItem tab = tabFolder.getItem(tabFolder.getSelectionIndex());
					Note selectedNote = getNote(tabFolder.getSelectionIndex());
					if (!selectedNote.getEditable()) {
						tab.setText(tab.getText().substring(3));
					} else {
						tab.setText(LOCK_CHARACTER + " " + tab.getText());
					}
					selectedNote.toggleEditable();
				}
			}
		};
		setTextAndImageToAction(toggleEditableAction, NotepadAction.TOGGLE_EDITABLE_NOTE);

		saveNoteAction = new Action() {
			@Override
			public void run() {
				if (tabFolder.getItemCount() > 0) {
					getNote(tabFolder.getSelectionIndex()).saveToFile(getSite());
				}
			}
		};
		setTextAndImageToAction(saveNoteAction, NotepadAction.EXPORT_NOTE);

		preferencesAction = new Action() {
			@Override
			public void run() {
				// Create preference dialog page that will appear in current workbench window.
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null,
						"notepad4e.preferences.PreferencePage", new String[] { "notepad4e.preferences.PreferencePage" },
						null);
				dialog.open();
			}
		};
		setTextAndImageToAction(preferencesAction, NotepadAction.PREFERENCES);

		websiteAction = new Action() {
			@Override
			public void run() {
				// Open website in the user's external browser.
				Program.launch("https://github.com/PyvesB/Notepad4e");
			}
		};
		setTextAndImageToAction(websiteAction, NotepadAction.WEBSITE);

		changelogAction = new Action() {
			@Override
			public void run() {
				// Open changelog page in the user's external browser.
				Program.launch("https://github.com/PyvesB/Notepad4e/releases");
			}
		};
		setTextAndImageToAction(changelogAction, NotepadAction.CHANGELOG);
	}

	/**
	 * Sets the image and the tool tip to an action button.
	 * 
	 * @param action
	 * @param text
	 * @param image
	 * @param shortcut
	 */
	private void setTextAndImageToAction(Action action, NotepadAction notepadAction) {
		if (notepadAction.getCommandID() != null) {
			// Action appears in action bar with an associated shortcut.
			action.setToolTipText(notepadAction.getText() + getShortcutDescription(notepadAction.getCommandID()));
		} else {
			// Action appears in drop down menu.
			action.setText(notepadAction.getText());
		}

		// The URL matches an image in the plugin's icons folder.
		URL url = FileLocator.find(Platform.getBundle(Notepad4e.PLUGIN_ID), new Path(notepadAction.getImagePath()),
				null);
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
		action.setImageDescriptor(imageDescriptor);
	}

	/**
	 * Returns key binding as a String for a given ShortcutCommand.
	 * 
	 * @param shortcut
	 * @return
	 */
	private String getShortcutDescription(String commandID) {
		Binding bestBinding = null;
		for (Binding binding : getViewSite().getService(IBindingService.class).getBindings()) {
			if (binding.getParameterizedCommand() != null
					&& commandID.equals(binding.getParameterizedCommand().getId())) {
				if (bestBinding == null) {
					bestBinding = binding;
				} else if (binding.getType() == Binding.USER) {
					// Give higher priority to a user type binding (user has overriden default).
					bestBinding = binding;
					break;
				}
			}
		}
		return bestBinding == null ? "" : " " + bestBinding.getTriggerSequence().format();
	}

	/**
	 * Swaps two tabs and corresponding notes in the view.
	 * 
	 * @param selectedIndex
	 * @param swappedIndex
	 */
	private void swapNoteTabs(int selectedIndex, int swappedIndex) {
		Note selectedNote = getNote(selectedIndex);
		Note swappedNote = getNote(swappedIndex);
		tabFolder.getItem(swappedIndex).setControl(selectedNote);
		tabFolder.getItem(selectedIndex).setControl(swappedNote);

		String selectedTitle = tabFolder.getItem(selectedIndex).getText();
		String swappedTitle = tabFolder.getItem(swappedIndex).getText();
		tabFolder.getItem(swappedIndex).setText(selectedTitle);
		tabFolder.getItem(selectedIndex).setText(swappedTitle);

		tabFolder.setSelection(swappedIndex);
	}
}
