package io.github.pyvesb.notepad4e.views;

import java.net.URL;
import java.util.concurrent.TimeUnit;

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
import io.github.pyvesb.notepad4e.preferences.Preferences;
import io.github.pyvesb.notepad4e.strings.LocalStrings;
import io.github.pyvesb.notepad4e.utils.AbstractSelectedNoteAction;
import io.github.pyvesb.notepad4e.utils.NotepadAction;
import io.github.pyvesb.notepad4e.utils.ShortcutHandler;

/**
 * Class handling the plugin's view with the different note tabs.
 * 
 * @author Pyves
 *
 */
public class NotepadView extends ViewPart implements IPreferenceChangeListener {

	private static final String LOCK_PREFIX = "\uD83D\uDD12 ";
	// The ID of the view as specified by the extension.
	public static final String ID = "notepad4e.views.NotepadView";
	// Keys used to store and retrieve the plugin's view between Eclipse sessions.
	private static final String STORE_COUNT_KEY = "NumOfTabs";
	private static final String STORE_TEXT_PREFIX_KEY = "TabText";
	private static final String STORE_STYLE_PREFIX_KEY = "TabStyle";
	private static final String STORE_TITLE_PREFIX_KEY = "TabTitle";
	private static final String STORE_EDITABLE_PREFIX_KEY = "TabEditable";
	private static final String STORE_BULLETS_PREFIX_KEY = "TabBullets";

	// Keyboard events listener.
	private final ShortcutHandler shortcutHandler = new ShortcutHandler(this);

	// Actions corresponding to the different buttons in the view.
	private Action addNewNoteAction;
	private Action clearNoteAction;
	private Action boldTextAction;
	private Action italicTextAction;
	private Action underlineTextAction;
	private Action strikeoutTextAction;
	private Action bulletListAction;
	private Action clearTextStyleAction;
	private Action toggleEditableAction;
	private Action exportNoteAction;
	private Action preferencesAction;
	private Action websiteAction;
	private Action changelogAction;
	// User defined preferences.
	private IEclipsePreferences preferences;
	// Object handling the different tabs.
	private CTabFolder tabFolder;
	// Current clipboard, used for the paste contents of clipboard in new notes feature.
	private Clipboard clipboard;
	// Note autosave interval.
	private long saveIntervalMillis;

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

		saveIntervalMillis = TimeUnit.SECONDS
				.toMillis(preferences.getInt(Preferences.SAVE_INTERVAL, Preferences.SAVE_INTERVAL_DEFAULT));
		if (saveIntervalMillis >= 0) {
			new Job("ScheduledAutosave") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							savePluginState(preferences.get(Preferences.SAVE_LOCATION, Preferences.SAVE_LOCATION_DEFAULT));
						}
					});
					schedule(saveIntervalMillis);
					return Status.OK_STATUS;
				}
			}.schedule(saveIntervalMillis);
		}

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
		tabFolder.dispose();
		clipboard.dispose();
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
		if (Preferences.SAVE_LOCATION.equals(event.getKey())) {
			savePluginState((String) event.getOldValue());
			// Load dialog settings using new location.
			Notepad4e.getDefault().restoreDialogSettings();
			// This will merge newly restored dialog settings with current state of notes.
			restoreViewFromPreviousSession();
		}
		for (int tabIndex = 0; tabIndex < tabFolder.getItemCount(); ++tabIndex) {
			getNote(tabIndex).setParametersFromPreferences();
		}
		saveIntervalMillis = TimeUnit.SECONDS
				.toMillis(preferences.getInt(Preferences.SAVE_INTERVAL, Preferences.SAVE_INTERVAL_DEFAULT));
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
	 * Adds a new note to the notepad.
	 */
	public void addNewNote() {
		String noteTitle = getNewNoteTitle();
		String noteText = "";
		if (preferences.getBoolean(Preferences.PASTE_CLIPBOARD_IN_NEW_NOTES,
				Preferences.PASTE_CLIPBOARD_IN_NEW_NOTES_DEFAULT)) {
			noteText = (String) clipboard.getContents(TextTransfer.getInstance(), DND.CLIPBOARD);
		}
		// Add a new note tab with a number appended to its name (Note 1, Note 2, Note 3, etc.).
		addNewNoteTab(noteTitle, noteText, null, true, null);
		CTabItem previousSelectedTab = tabFolder.getSelection();
		// Remove lock for currently selected tab.
		if (previousSelectedTab != null && previousSelectedTab.getText().startsWith(LOCK_PREFIX)) {
			previousSelectedTab.setText(previousSelectedTab.getText().substring(LOCK_PREFIX.length()));
		}
		tabFolder.setSelection(tabFolder.getItemCount() - 1);
	}

	/**
	 * Closes the currently selected tab in the view and disposes resources appropriately.
	 */
	public void closeCurrentSelection() {
		Note selectedNote = getSelectedNote();
		if (selectedNote != null) {
			if (!selectedNote.getEditable()) {
				if (MessageDialog.openQuestion(getSite().getShell(), LocalStrings.dialogCloseLockedTitle,
						LocalStrings.dialogCloseLockedMsg)) {
					tabFolder.getSelection().dispose();
				}
			} else if (!preferences.getBoolean(Preferences.CLOSE_CONFIRMATION, Preferences.CLOSE_CONFIRMATION_DEFAULT)
					|| MessageDialog.openQuestion(getSite().getShell(), LocalStrings.dialogCloseTitle,
							LocalStrings.dialogCloseMsg)) {
				tabFolder.getSelection().dispose();
			}
		}
	}

	/**
	 * Returns the currently selected Note or null.
	 * 
	 * @return selected Note
	 */
	public Note getSelectedNote() {
		return tabFolder.getSelectionIndex() >= 0 ? getNote(tabFolder.getSelectionIndex()) : null;
	}

	/**
	 * Constructs the title of a new note. The title does not match the ones of extisting notes.
	 * 
	 * @return the note title, for instance "Note 2"
	 */
	private String getNewNoteTitle() {
		int noteNumber = tabFolder.getItemCount() + 1;
		String title = preferences.get(Preferences.NAME_PREFIX, Preferences.NAME_PREFIX_DEFAULT) + " " + noteNumber;
		if (tabFolder.getItemCount() == 0) {
			return title;
		}
		while (true) {
			for (int tabIndex = 0; tabIndex < tabFolder.getItemCount(); ++tabIndex) {
				if (tabFolder.getItem(tabIndex).getText().contains(title)) {
					break;
				} else if (tabIndex == tabFolder.getItemCount() - 1) {
					return title;
				}
			}
			++noteNumber;
			title = preferences.get(Preferences.NAME_PREFIX, Preferences.NAME_PREFIX_DEFAULT) + " " + noteNumber;
		}
	}

	/**
	 * Returns a Note object given an index in the tab folder.
	 * 
	 * @param index
	 * @return Note at the given index
	 */
	private Note getNote(int index) {
		return (Note) (tabFolder.getItem(index).getControl());
	}

	/**
	 * Listens to disposal of the tab folder and saves state for next Eclipse session or when reopening the view.
	 */
	private void addPluginDisposeListener() {
		tabFolder.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				savePluginState(preferences.get(Preferences.SAVE_LOCATION, Preferences.SAVE_LOCATION_DEFAULT));
			}
		});
	}

	/**
	 * Saves plugin state for next Eclipse session or when reopening the view.
	 * 
	 * @param directory
	 */
	private void savePluginState(String directory) {
		if (!tabFolder.isDisposed()) {
			IDialogSettings section = Notepad4e.getDefault().getDialogSettings().getSection(ID);
			section.put(STORE_COUNT_KEY, tabFolder.getItemCount());
			for (int tabIndex = 0; tabIndex < tabFolder.getItemCount(); ++tabIndex) {
				CTabItem tab = tabFolder.getItem(tabIndex);
				if (!tab.isDisposed()) {
					Note note = getNote(tabIndex);
					section.put(STORE_TEXT_PREFIX_KEY + tabIndex, note.getText());
					section.put(STORE_STYLE_PREFIX_KEY + tabIndex, note.serialiseStyle());
					if (tab.getText().startsWith(LOCK_PREFIX)) {
						// Do not save lock symbol.
						section.put(STORE_TITLE_PREFIX_KEY + tabIndex, tab.getText().substring(LOCK_PREFIX.length()));
					} else {
						section.put(STORE_TITLE_PREFIX_KEY + tabIndex, tab.getText());
					}
					section.put(STORE_EDITABLE_PREFIX_KEY + tabIndex, note.getEditable());
					section.put(STORE_BULLETS_PREFIX_KEY + tabIndex, note.serialiseBullets());
				}
			}
			Notepad4e.getDefault().saveDialogSettings(directory);
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
					event.doit = MessageDialog.openQuestion(getSite().getShell(), LocalStrings.dialogCloseLockedTitle,
							LocalStrings.dialogCloseLockedMsg);
				} else if (preferences.getBoolean(Preferences.CLOSE_CONFIRMATION, Preferences.CLOSE_CONFIRMATION_DEFAULT)) {
					event.doit = MessageDialog.openQuestion(getSite().getShell(), LocalStrings.dialogCloseTitle,
							LocalStrings.dialogCloseMsg);
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
			public void mouseDoubleClick(MouseEvent event) {
				CTabItem clickedTab = tabFolder.getItem(new Point(event.x, event.y));
				if (clickedTab == null) {
					return;
				}
				boolean isLocked = false;
				String dialogText = clickedTab.getText();
				if (dialogText.startsWith(LOCK_PREFIX)) {
					isLocked = true;
					dialogText = dialogText.substring(LOCK_PREFIX.length());
				}
				// Open a dialog window so user can enter the new name of his note.
				InputDialog inputDialog = new InputDialog(null, LocalStrings.dialogRenameTitle,
						LocalStrings.dialogRenameMsg, dialogText, null);
				inputDialog.open();
				// If user selected Cancel, text will be null.
				if (inputDialog.getValue() != null && !inputDialog.getValue().isEmpty()) {
					if (isLocked) {
						clickedTab.setText(LOCK_PREFIX + inputDialog.getValue());
					} else {
						clickedTab.setText(inputDialog.getValue());
					}
				}
			}

			@Override
			public void mouseUp(MouseEvent event) {}

			@Override
			public void mouseDown(MouseEvent event) {}
		});
	}

	/**
	 * Swaps two tabs and corresponding notes when a user drags one to another.
	 */
	private void addSwapTabListener() {
		tabFolder.addDragDetectListener(new DragDetectListener() {
			@Override
			public void dragDetected(DragDetectEvent dragDetectedEvent) {
				final Rectangle viewRectangle = Geometry.toDisplay(tabFolder.getParent(), tabFolder.getBounds());
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
							swapNoteTabs(tabFolder.indexOf(tabToSwap));
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
					if (tab.getText().startsWith(LOCK_PREFIX)) {
						tab.setText(tab.getText().substring(LOCK_PREFIX.length()));
					}
				}
				// Put lock symbol on selected tab, if non editable.
				if (!getSelectedNote().getEditable()) {
					CTabItem selectedTab = (CTabItem) event.item;
					selectedTab.setText(LOCK_PREFIX + selectedTab.getText());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {}
		});
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

		if (numOfTabs == 0 && tabFolder.getItemCount() == 0) {
			// No notes were previously opened: create new one.
			String prefixName = preferences.get(Preferences.NAME_PREFIX, Preferences.NAME_PREFIX_DEFAULT);
			addNewNoteTab(prefixName + " 1", "", null, true, null);
			// Set selection on this tab.
			tabFolder.setSelection(0);
		} else {
			// Populate with tabs opened in previous session.
			for (int tabIndex = 0; tabIndex < numOfTabs; ++tabIndex) {
				String tabTitle = section.get(STORE_TITLE_PREFIX_KEY + tabIndex);
				boolean editable = section.get(STORE_EDITABLE_PREFIX_KEY + tabIndex) == null ? true
						: section.getBoolean(STORE_EDITABLE_PREFIX_KEY + tabIndex);
				String noteText = section.get(STORE_TEXT_PREFIX_KEY + tabIndex);
				String noteStyle = section.get(STORE_STYLE_PREFIX_KEY + tabIndex);
				String noteBullets = section.get(STORE_BULLETS_PREFIX_KEY + tabIndex);
				if (tabTitle != null && noteText != null) {
					addNewNoteTab(tabTitle, noteText, noteStyle, editable, noteBullets);
				}
			}
			// Set selection on the last tab.
			tabFolder.setSelection(tabFolder.getItemCount() - 1);
			if (!getSelectedNote().getEditable()) {
				tabFolder.getSelection().setText(LOCK_PREFIX + tabFolder.getSelection().getText());
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
	 * @param bullets
	 */
	private void addNewNoteTab(String title, String text, String style, boolean editable, String bullets) {
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
		Note note = new Note(tabFolder, text, style, bullets, editable);
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
		manager.add(exportNoteAction);
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
		manager.add(bulletListAction);
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
				addNewNote();
			}
		};
		setTextAndImageToAction(addNewNoteAction, NotepadAction.NEW_NOTE);

		clearNoteAction = new AbstractSelectedNoteAction(this) {
			@Override
			protected void runSelectedNoteAction(Note selectedNote) {
				selectedNote.clearText();
			}
		};
		setTextAndImageToAction(clearNoteAction, NotepadAction.CLEAR_NOTE);

		boldTextAction = new AbstractSelectedNoteAction(this) {
			@Override
			protected void runSelectedNoteAction(Note selectedNote) {
				selectedNote.boldSelection();
			}
		};
		setTextAndImageToAction(boldTextAction, NotepadAction.BOLD_TEXT);

		italicTextAction = new AbstractSelectedNoteAction(this) {
			@Override
			protected void runSelectedNoteAction(Note selectedNote) {
				selectedNote.italicSelection();
			}
		};
		setTextAndImageToAction(italicTextAction, NotepadAction.ITALIC_TEXT);

		underlineTextAction = new AbstractSelectedNoteAction(this) {
			@Override
			protected void runSelectedNoteAction(Note selectedNote) {
				selectedNote.underlineSelection();
			}
		};
		setTextAndImageToAction(underlineTextAction, NotepadAction.UNDERLINE_TEXT);

		strikeoutTextAction = new AbstractSelectedNoteAction(this) {
			@Override
			protected void runSelectedNoteAction(Note selectedNote) {
				selectedNote.strikeoutSelection();
			}
		};
		setTextAndImageToAction(strikeoutTextAction, NotepadAction.STRIKEOUT_TEXT);

		bulletListAction = new AbstractSelectedNoteAction(this) {
			@Override
			protected void runSelectedNoteAction(Note selectedNote) {
				selectedNote.bulletListSelection();
			}
		};
		setTextAndImageToAction(bulletListAction, NotepadAction.BULLET_LIST);

		clearTextStyleAction = new AbstractSelectedNoteAction(this) {
			@Override
			protected void runSelectedNoteAction(Note selectedNote) {
				selectedNote.clearSelectionStyles();
			}
		};
		setTextAndImageToAction(clearTextStyleAction, NotepadAction.CLEAR_STYLE_TEXT);

		toggleEditableAction = new AbstractSelectedNoteAction(this) {
			@Override
			protected void runSelectedNoteAction(Note selectedNote) {
				CTabItem tab = tabFolder.getSelection();
				if (!selectedNote.getEditable()) {
					tab.setText(tab.getText().substring(LOCK_PREFIX.length()));
				} else {
					tab.setText(LOCK_PREFIX + tab.getText());
				}
				selectedNote.toggleEditable();
			}
		};
		setTextAndImageToAction(toggleEditableAction, NotepadAction.TOGGLE_EDITABLE_NOTE);

		exportNoteAction = new AbstractSelectedNoteAction(this) {
			@Override
			protected void runSelectedNoteAction(Note selectedNote) {
				selectedNote.exportToFile(getSite());
			}
		};
		setTextAndImageToAction(exportNoteAction, NotepadAction.EXPORT_NOTE);

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
	 * @param notepadAction
	 */
	private void setTextAndImageToAction(Action action, NotepadAction notepadAction) {
		if (notepadAction.getCommandID() != null) {
			// Action appears in action bar with an associated shortcut.
			action.setToolTipText(notepadAction.getText() + getKeyBindingDescription(notepadAction.getCommandID()));
		} else {
			// Action appears in drop down menu.
			action.setText(notepadAction.getText());
		}

		// The URL matches an image in the plugin's icons folder.
		URL url = FileLocator.find(Platform.getBundle(Notepad4e.PLUGIN_ID), new Path(notepadAction.getImagePath()),
				null);
		action.setImageDescriptor(ImageDescriptor.createFromURL(url));
	}

	/**
	 * Returns key binding as a String for a given command ID.
	 * 
	 * @param commandID
	 * @return the key binding, for instance Ctrl + B
	 */
	private String getKeyBindingDescription(String commandID) {
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
	 * @param swappedIndex
	 */
	private void swapNoteTabs(int swappedIndex) {
		Note selectedNote = getSelectedNote();
		Note swappedNote = getNote(swappedIndex);
		tabFolder.getItem(swappedIndex).setControl(selectedNote);
		tabFolder.getSelection().setControl(swappedNote);

		String selectedTitle = tabFolder.getSelection().getText();
		String swappedTitle = tabFolder.getItem(swappedIndex).getText();
		tabFolder.getItem(swappedIndex).setText(selectedTitle);
		tabFolder.getSelection().setText(swappedTitle);

		tabFolder.setSelection(swappedIndex);
	}
}
