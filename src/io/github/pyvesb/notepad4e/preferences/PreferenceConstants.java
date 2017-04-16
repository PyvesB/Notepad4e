package io.github.pyvesb.notepad4e.preferences;

/**
 * Class used to store the names of the preferences used by the plugin as well as their default values.
 * 
 * @author Pyves
 *
 */
public class PreferenceConstants {

	// Names of preferences.
	public static final String PREF_WRAP = "Wrap";
	public static final String PREF_JUSTIFY = "Justify";
	public static final String PREF_ALIGNMENT = "Alignment";
	public static final String PREF_FONT = "Font";
	public static final String PREF_FONT_COLOR = "FontColor";
	public static final String PREF_BACKGROUND_COLOR = "BackgroundColor";
	public static final String PREF_LINE_SPACING = "LineSpacing";
	public static final String PREF_NAME_PREFIX = "NamePrefix";
	public static final String PREF_CLOSE_CONFIRMATION = "CloseConfirmation";
	public static final String PREF_PASTE_CLIPBOARD_IN_NEW_NOTES = "PasteClipboardInNewNotes";
	public static final String PREF_BULLET_SPACING = "BulletSpacing";

	// Default values of preferences.
	public static final boolean PREF_WRAP_DEFAULT = true;
	public static final boolean PREF_JUSTIFY_DEFAULT = false;
	public static final String PREF_ALIGNMENT_DEFAULT = "left";
	public static final String PREF_FONT_DEFAULT = "";
	public static final String PREF_FONT_COLOR_DEFAULT = "0,0,0";
	public static final String PREF_BACKGROUND_COLOR_DEFAULT = "255,255,255";
	public static final int PREF_LINE_SPACING_DEFAULT = 0;
	public static final String PREF_NAME_PREFIX_DEFAULT = "Note";
	public static final boolean PREF_CLOSE_CONFIRMATION_DEFAULT = true;
	public static final boolean PREF_PASTE_CLIPBOARD_IN_NEW_NOTES_DEFAULT = false;
	public static final int PREF_BULLET_SPACING_DEFAULT = 15;

	private PreferenceConstants() {
		// Not called.
	}
}
