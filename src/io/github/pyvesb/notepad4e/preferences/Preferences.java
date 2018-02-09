package io.github.pyvesb.notepad4e.preferences;

/**
 * Class used to list the names of the preferences used by the plugin as well as their default values.
 * 
 * @author Pyves
 *
 */
public class Preferences {

	// Names of preferences.
	public static final String WRAP = "Wrap";
	public static final String JUSTIFY = "Justify";
	public static final String ALIGNMENT = "Alignment";
	public static final String FONT = "Font";
	public static final String FONT_COLOR = "FontColor";
	public static final String BACKGROUND_COLOR = "BackgroundColor";
	public static final String LINE_SPACING = "LineSpacing";
	public static final String NAME_PREFIX = "NamePrefix";
	public static final String CLOSE_CONFIRMATION = "CloseConfirmation";
	public static final String PASTE_CLIPBOARD_IN_NEW_NOTES = "PasteClipboardInNewNotes";
	public static final String BULLET_SPACING = "BulletSpacing";
	public static final String SAVE_INTERVAL = "SaveInterval";

	// Default values of preferences.
	public static final boolean WRAP_DEFAULT = true;
	public static final boolean JUSTIFY_DEFAULT = false;
	public static final String ALIGNMENT_DEFAULT = "left";
	public static final String FONT_DEFAULT = "";
	public static final String FONT_COLOR_DEFAULT = "0,0,0";
	public static final String BACKGROUND_COLOR_DEFAULT = "255,255,255";
	public static final int LINE_SPACING_DEFAULT = 0;
	public static final String NAME_PREFIX_DEFAULT = "Note";
	public static final boolean CLOSE_CONFIRMATION_DEFAULT = true;
	public static final boolean PASTE_CLIPBOARD_IN_NEW_NOTES_DEFAULT = false;
	public static final int BULLET_SPACING_DEFAULT = 15;
	public static final int SAVE_INTERVAL_DEFAULT = 120;

	private Preferences() {
		// Not called.
	}
}
