package io.github.pyvesb.notepad4e.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPartSite;

import io.github.pyvesb.notepad4e.Notepad4e;
import io.github.pyvesb.notepad4e.preferences.PreferenceConstants;
import io.github.pyvesb.notepad4e.strings.LocalStrings;
import io.github.pyvesb.notepad4e.utils.AbstractMenuItemSelectionListener;
import io.github.pyvesb.notepad4e.utils.UndoRedoManager;

/**
 * Class representing an individual note in the plugin's view.
 * 
 * @author Pyves
 *
 */
public class Note extends StyledText {

	// Used to parse strings.
	private static final String STRING_SEPARATOR = ",";

	// Used to enable undo and redo actions.
	private final UndoRedoManager undoRedoManager;
	// User defined preferences.
	private final IEclipsePreferences preferences;
	// Used to set the style of bullet lists.
	private final StyleRange bulletStyle;

	// Appearance parameters of the note.
	private Color fontColor;
	private Color backgroundColor;
	private Font font;
	// Menu items (mouse right-click).
	private MenuItem menuItemUndo;
	private MenuItem menuItemRedo;
	private MenuItem menuItemCut;
	private MenuItem menuItemCopy;
	private MenuItem menuItemPaste;
	private MenuItem menuItemSelectAll;
	private MenuItem menuItemSeparator1;
	private MenuItem menuItemSeparator2;

	private enum TextStyle {
		BOLD, ITALIC, UNDERLINE, STRIKEOUT
	}

	/**
	 * Constructor. Sets properties of the editor window.
	 * 
	 * @param parent
	 * @param text
	 * @param editable
	 * @param shortcutHandler
	 */
	public Note(Composite parent, String text, boolean editable) {
		// Enable multiple lines and scroll bars.
		super(parent, SWT.V_SCROLL | SWT.H_SCROLL);

		preferences = InstanceScope.INSTANCE.getNode(Notepad4e.PLUGIN_ID);

		undoRedoManager = new UndoRedoManager(this);
		bulletStyle = new StyleRange();
		bulletStyle.metrics = new GlyphMetrics(0, 0, 0);

		// Scroll bars only appear when the text extends beyond the note window.
		setAlwaysShowScrollBars(false);
		setParametersFromPreferences();
		setText(text);
		initialiseMenu();

		if (!editable) {
			toggleEditable();
		}
	}

	/**
	 * Disposes the resources owned by the note.
	 */
	@Override
	public void dispose() {
		fontColor.dispose();
		backgroundColor.dispose();
		if (font != null) {
			font.dispose();
		}
		menuItemUndo.dispose();
		menuItemRedo.dispose();
		menuItemCut.dispose();
		menuItemCopy.dispose();
		menuItemPaste.dispose();
		menuItemSelectAll.dispose();
		menuItemSeparator1.dispose();
		menuItemSeparator2.dispose();
		super.dispose();
	}

	/**
	 * Sets properties that can be defined by the user in the plugin's preferences page.
	 */
	public void setParametersFromPreferences() {
		// Line spacing parameter.
		setLineSpacing(preferences.getInt(PreferenceConstants.PREF_LINE_SPACING,
				PreferenceConstants.PREF_LINE_SPACING_DEFAULT));

		// Set bullet indentation spacing (width of GlyphMetrics) parameter.
		bulletStyle.metrics.width = preferences.getInt(PreferenceConstants.PREF_BULLET_SPACING,
				PreferenceConstants.PREF_BULLET_SPACING_DEFAULT);

		// Line wrap parameter.
		setWordWrap(preferences.getBoolean(PreferenceConstants.PREF_WRAP, PreferenceConstants.PREF_WRAP_DEFAULT));

		// Text justify parameter.
		setJustify(preferences.getBoolean(PreferenceConstants.PREF_JUSTIFY, PreferenceConstants.PREF_JUSTIFY_DEFAULT));

		// Alignment parameter (left or right).
		if ("right".equals(
				preferences.get(PreferenceConstants.PREF_ALIGNMENT, PreferenceConstants.PREF_ALIGNMENT_DEFAULT))) {
			setAlignment(SWT.RIGHT);
			// Word wrapping must be enabled for right alignment to be effective.
			setWordWrap(true);
		} else {
			setAlignment(SWT.LEFT);
		}

		// Font color parameter.
		String[] fontColorRGBStrings = preferences
				.get(PreferenceConstants.PREF_FONT_COLOR, PreferenceConstants.PREF_FONT_COLOR_DEFAULT)
				.split(STRING_SEPARATOR);
		// The strings in the above array correspond to the red, green and blue colors.
		fontColor = new Color(Display.getCurrent(), Integer.parseInt(fontColorRGBStrings[0]),
				Integer.parseInt(fontColorRGBStrings[1]), Integer.parseInt(fontColorRGBStrings[2]));
		setForeground(fontColor);

		// Background color parameter.
		String[] backgroundColorRGBStrings = preferences
				.get(PreferenceConstants.PREF_BACKGROUND_COLOR, PreferenceConstants.PREF_BACKGROUND_COLOR_DEFAULT)
				.split(STRING_SEPARATOR);
		// The strings in the above array correspond to the red, green and blue colors.
		backgroundColor = new Color(Display.getCurrent(), Integer.parseInt(backgroundColorRGBStrings[0]),
				Integer.parseInt(backgroundColorRGBStrings[1]), Integer.parseInt(backgroundColorRGBStrings[2]));
		setBackground(backgroundColor);

		// Font parameter; a semicolon is appended by the Eclipse API when retrieving it from the plugin's preference
		// page, it must be deleted.
		String fontString = preferences.get(PreferenceConstants.PREF_FONT, PreferenceConstants.PREF_FONT_DEFAULT)
				.replace(";", "");
		// An empty string is returned when the user has not set the font in the preferences; do not set the font so the
		// plugin will display the default font of the StyledText component instead.
		if (!fontString.isEmpty()) {
			font = new Font(Display.getCurrent(), new FontData(fontString));
			setFont(font);
		}
	}

	/**
	 * Undos latest Note modification.
	 */
	public void undo() {
		undoRedoManager.undo();
	}

	/**
	 * Redos latest Note modification.
	 */
	public void redo() {
		undoRedoManager.redo();
	}

	/**
	 * Removes all the text from the note.
	 */
	public void clearText() {
		if (getEditable()) {
			setText("");
		}
	}

	/**
	 * Applies a bold style to the currently selected text.
	 */
	public void boldSelection() {
		addStyleToSelection(TextStyle.BOLD);
	}

	/**
	 * Applies an italic style to the currently selected text.
	 */
	public void italicSelection() {
		addStyleToSelection(TextStyle.ITALIC);
	}

	/**
	 * Applies an underlined style to the currently selected text.
	 */
	public void underlineSelection() {
		addStyleToSelection(TextStyle.UNDERLINE);
	}

	/**
	 * Applies a strikeout style to the currently selected text.
	 */
	public void strikeoutSelection() {
		addStyleToSelection(TextStyle.STRIKEOUT);
	}

	/**
	 * Applies a bullet list style to the currently selected lines.
	 */
	public void bulletListSelection() {
		if (!getEditable()) {
			return;
		}

		String textBeforeSelection = getText().substring(0, getSelection().x);
		int selectionStartLine = getTextLineCount(textBeforeSelection) - 1;
		int selectionLineCount = getTextLineCount(getSelectionText());
		int selectionCurrentBullets = 0;
		// Count number of lines that currently have a bullet.
		for (int line = selectionStartLine; line < selectionStartLine + selectionLineCount; ++line) {
			if (getLineBullet(line) != null) {
				++selectionCurrentBullets;
			}
		}

		if (selectionCurrentBullets == selectionLineCount) {
			// All lines have bullets, remove them all.
			setLineBullet(selectionStartLine, selectionLineCount, null);
			return;
		}

		Bullet bullet = new Bullet(ST.BULLET_DOT, bulletStyle);
		setLineBullet(selectionStartLine, selectionLineCount, bullet);
	}

	/**
	 * Removes all styles from the current selection.
	 */
	public void clearSelectionStyles() {
		if (!getEditable()) {
			return;
		}
		// Record style modification for undo actions.
		undoRedoManager.recordNoteModification(null, getStyleRanges());

		Point selectionRange = getSelectionRange();
		// No colors are specified as they are defined by the plugin's preferences.
		StyleRange styleRange = new StyleRange(selectionRange.x, selectionRange.y, null, null, SWT.NORMAL);
		setStyleRange(styleRange);
	}

	/**
	 * Makes the note read-only or editable again.
	 */
	public void toggleEditable() {
		boolean newState = !getEditable();
		setEditable(newState);
		menuItemUndo.setEnabled(newState);
		menuItemRedo.setEnabled(newState);
		menuItemCut.setEnabled(newState);
		menuItemPaste.setEnabled(newState);
	}

	/**
	 * Creates a string giving a description of the styles in the current note.
	 * 
	 * @return CSV string containing a serialised representation of the styles
	 */
	public String serialiseStyle() {
		StringBuilder styleSerialisation = new StringBuilder();
		StyleRange[] currentStyles = getStyleRanges();
		// Append integers corresponding to various information of each style range object, separated by
		// STRING_SEPARATOR.
		for (int styleIndex = 0; styleIndex < currentStyles.length; ++styleIndex) {
			styleSerialisation.append(currentStyles[styleIndex].start);
			styleSerialisation.append(STRING_SEPARATOR);
			styleSerialisation.append(currentStyles[styleIndex].length);
			styleSerialisation.append(STRING_SEPARATOR);
			styleSerialisation.append(currentStyles[styleIndex].fontStyle);
			styleSerialisation.append(STRING_SEPARATOR);
			// If underlined, 1, else 0.
			styleSerialisation.append(currentStyles[styleIndex].underline ? 1 : 0);
			styleSerialisation.append(STRING_SEPARATOR);
			// If strikeout, 1, else 0.
			styleSerialisation.append(currentStyles[styleIndex].strikeout ? 1 : 0);
			styleSerialisation.append(STRING_SEPARATOR);
		}
		return styleSerialisation.toString();
	}

	/**
	 * Creates a string giving a description of the bullets in the current note.
	 * 
	 * @return CSV string containing a serialised representation of the bullets
	 */
	public String serialiseBullets() {
		int totalLines = getTextLineCount(getText());
		StringBuilder bulletLines = new StringBuilder();
		for (int line = 0; line < totalLines; ++line) {
			if (getLineBullet(line) != null) {
				// Bullet found: add line number with separator.
				bulletLines.append(line);
				bulletLines.append(STRING_SEPARATOR);
			}
		}
		// Remove trailing separator.
		return bulletLines.length() > 1 ? bulletLines.substring(0, bulletLines.length() - 1) : "";
	}

	/**
	 * Applies styles to the current note based on a styles' serialisation string.
	 * 
	 * @param serialisation
	 */
	public void deserialiseStyle(String serialisation) {
		String[] integers = serialisation.split(STRING_SEPARATOR);
		StyleRange[] styles = new StyleRange[integers.length / 5];
		// Do the parsing.
		for (int styleIndex = 0; styleIndex < styles.length; ++styleIndex) {
			// Each StyleRange object has 5 corresponding integers in the CSV string.
			int integerIndex = 5 * styleIndex;
			styles[styleIndex] = new StyleRange();
			styles[styleIndex].start = Integer.parseInt(integers[integerIndex]);
			styles[styleIndex].length = Integer.parseInt(integers[integerIndex + 1]);
			styles[styleIndex].fontStyle = Integer.parseInt(integers[integerIndex + 2]);
			styles[styleIndex].underline = (Integer.parseInt(integers[integerIndex + 3]) == 1) ? true : false;
			styles[styleIndex].strikeout = (Integer.parseInt(integers[integerIndex + 4]) == 1) ? true : false;
		}
		// Apply the parsed styles.
		setStyleRanges(styles);
	}

	/**
	 * Adds bullets to the current note based on a bullets' serialisation string (for instance "0,1,4").
	 * 
	 * @param serialisation
	 */
	public void deserialiseBullets(String serialisation) {
		Bullet bullet = new Bullet(ST.BULLET_DOT, bulletStyle);
		for (String lineNumber : serialisation.split(STRING_SEPARATOR)) {
			setLineBullet(Integer.parseInt(lineNumber), 1, bullet);
		}
	}

	/**
	 * Exports the brute text in the current note as a text file.
	 * 
	 * @param iWorkbenchPartSite
	 */
	public void exportToFile(IWorkbenchPartSite iWorkbenchPartSite) {
		// Retrieve the file to save to with an explorer window.
		FileDialog fileDialog = new FileDialog(iWorkbenchPartSite.getShell(), SWT.SAVE);
		fileDialog.setText(LocalStrings.dialogExportTitle);
		String fileName = fileDialog.open();
		// Invalid name specified.
		if (fileName == null || fileName.isEmpty()) {
			return;
		}

		File file = new File(fileName);
		if (file.exists() && !MessageDialog.openQuestion(iWorkbenchPartSite.getShell(),
				LocalStrings.dialogOverwriteTitle, LocalStrings.dialogOverwriteMsg)) {
			return;
		}

		// Write the current note's text to the file, with handling of IO exceptions.
		try (FileOutputStream outStream = new FileOutputStream(file);
				PrintWriter printStream = new PrintWriter(outStream)) {
			printStream.print(getText());
			printStream.flush();
			MessageDialog.openInformation(iWorkbenchPartSite.getShell(), LocalStrings.dialogExportedTitle,
					LocalStrings.dialogExportedMsg);
		} catch (IOException e) {
			MessageDialog.openInformation(iWorkbenchPartSite.getShell(), LocalStrings.dialogErrorTitle,
					LocalStrings.dialogErrorMsg);
			Notepad4e.getDefault().getLog().log(new Status(IStatus.ERROR, LocalStrings.dialogErrorMsg, e.toString()));
		}
	}

	/**
	 * Initialises the menu triggered by a right-click inside the note.
	 */
	private void initialiseMenu() {
		Menu menu = new Menu(getShell(), SWT.POP_UP);
		menuItemUndo = new MenuItem(menu, SWT.NONE);
		menuItemUndo.setText(LocalStrings.menuUndo);
		menuItemUndo.addSelectionListener(new AbstractMenuItemSelectionListener() {
			@Override
			public void onNoteMenuItemSelected() {
				undo();
			}
		});
		menuItemRedo = new MenuItem(menu, SWT.NONE);
		menuItemRedo.setText(LocalStrings.menuRedo);
		menuItemRedo.addSelectionListener(new AbstractMenuItemSelectionListener() {
			@Override
			public void onNoteMenuItemSelected() {
				redo();
			}
		});
		menuItemSeparator1 = new MenuItem(menu, SWT.SEPARATOR);
		menuItemCut = new MenuItem(menu, SWT.NONE);
		menuItemCut.setText(LocalStrings.menuCut);
		menuItemCut.addSelectionListener(new AbstractMenuItemSelectionListener() {
			@Override
			public void onNoteMenuItemSelected() {
				cut();
			}
		});
		menuItemCopy = new MenuItem(menu, SWT.NONE);
		menuItemCopy.setText(LocalStrings.menuCopy);
		menuItemCopy.addSelectionListener(new AbstractMenuItemSelectionListener() {
			@Override
			public void onNoteMenuItemSelected() {
				copy();
			}
		});
		menuItemPaste = new MenuItem(menu, SWT.NONE);
		menuItemPaste.setText(LocalStrings.menuPaste);
		menuItemPaste.addSelectionListener(new AbstractMenuItemSelectionListener() {
			@Override
			public void onNoteMenuItemSelected() {
				paste();
			}
		});
		menuItemSeparator2 = new MenuItem(menu, SWT.SEPARATOR);
		menuItemSelectAll = new MenuItem(menu, SWT.NONE);
		menuItemSelectAll.setText(LocalStrings.menuSelectAll);
		menuItemSelectAll.addSelectionListener(new AbstractMenuItemSelectionListener() {
			@Override
			public void onNoteMenuItemSelected() {
				selectAll();
			}
		});
		setMenu(menu);
	}

	/**
	 * Applies a new style to the currently selected text.
	 * 
	 * @param newStyle
	 */
	private void addStyleToSelection(TextStyle newStyle) {
		if (!getEditable()) {
			return;
		}
		// Record style modification for undo actions.
		undoRedoManager.recordNoteModification(null, getStyleRanges());

		Point selectionRange = getSelectionRange();
		// Retrieve the current styles in the selection. If the selection (or parts of it) does not have any style,
		// there are no corresponding entries in the following array.
		StyleRange[] currentStyles = getStyleRanges(selectionRange.x, selectionRange.y);

		StyleRange selectionStyleRange = new StyleRange(selectionRange.x, selectionRange.y, null, null);
		switch (newStyle) {
			case BOLD:
				selectionStyleRange.fontStyle = SWT.BOLD;
				break;
			case ITALIC:
				selectionStyleRange.fontStyle = SWT.ITALIC;
				break;
			case UNDERLINE:
				selectionStyleRange.underline = true;
				break;
			case STRIKEOUT:
				selectionStyleRange.strikeout = true;
				break;
			default:
				return;
		}
		// Apply the style to the whole selection range; ranges that previously had no style and that are are not
		// accounted for in currentStyles now have the wanted style.
		setStyleRange(selectionStyleRange);

		// The above call overwrote the previous styles; the previous styles are re-applied with the additional
		// new one.
		for (int styleIndex = 0; styleIndex < currentStyles.length; ++styleIndex) {
			switch (newStyle) {
				case BOLD:
					currentStyles[styleIndex].fontStyle |= SWT.BOLD;
					break;
				case ITALIC:
					currentStyles[styleIndex].fontStyle |= SWT.ITALIC;
					break;
				case UNDERLINE:
					currentStyles[styleIndex].underline = true;
					break;
				case STRIKEOUT:
					currentStyles[styleIndex].strikeout = true;
					break;
				default:
					return;
			}
			setStyleRange(currentStyles[styleIndex]);
		}
	}

	/**
	 * Computes the number of lines in the input string.
	 * 
	 * @param text
	 * @return number of lines in the input string
	 */
	private int getTextLineCount(String text) {
		int previousLineCount = 1;
		for (int c = 0; c < text.length(); ++c) {
			if (text.charAt(c) == '\n') {
				++previousLineCount;
			}
		}
		return previousLineCount;
	}
}
