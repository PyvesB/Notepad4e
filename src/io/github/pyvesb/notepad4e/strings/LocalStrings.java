package io.github.pyvesb.notepad4e.strings;

import org.eclipse.osgi.util.NLS;

/**
 * Class used to translate the plugin's messages into different languages. Each translation must be contained in a
 * localstring_XX.properties file, where XX represents the locale.
 * 
 * @author Pyves
 *
 */
public class LocalStrings extends NLS {

	private static final String BUNDLE_NAME = "io.github.pyvesb.notepad4e.strings.localstrings";

	public static String dialogCloseLockedMsg;
	public static String dialogCloseLockedTitle;
	public static String dialogCloseMsg;
	public static String dialogCloseTitle;
	public static String dialogErrorMsg;
	public static String dialogErrorTitle;
	public static String dialogExportTitle;
	public static String dialogExportedMsg;
	public static String dialogExportedTitle;
	public static String dialogOverwriteMsg;
	public static String dialogOverwriteTitle;
	public static String dialogRenameMsg;
	public static String dialogRenameTitle;
	public static String iconBold;
	public static String iconBulletList;
	public static String iconChangelog;
	public static String iconClearNote;
	public static String iconClearStyle;
	public static String iconExport;
	public static String iconItalic;
	public static String iconLock;
	public static String iconNewNote;
	public static String iconPreferences;
	public static String iconStrikeout;
	public static String iconUnderline;
	public static String iconWebpage;
	public static String menuCopy;
	public static String menuCut;
	public static String menuPaste;
	public static String menuRedo;
	public static String menuSelectAll;
	public static String menuUndo;
	public static String prefAlignment;
	public static String prefBackgroundColor;
	public static String prefBulletSpacing;
	public static String prefCloseConfirmation;
	public static String prefDesc;
	public static String prefFont;
	public static String prefFontColor;
	public static String prefJustify;
	public static String prefLeft;
	public static String prefLineSpacing;
	public static String prefNamePrefix;
	public static String prefPasteClipboardInNewNotes;
	public static String prefRight;
	public static String prefWrap;
	public static String prefSaveInterval;

	static {
		// Initialise resource bundle.
		NLS.initializeMessages(BUNDLE_NAME, LocalStrings.class);
	}

	private LocalStrings() {
		// Not called.
	}

}
