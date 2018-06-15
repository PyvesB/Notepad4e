package io.github.pyvesb.notepad4e.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import io.github.pyvesb.notepad4e.Notepad4e;

/**
 * Class used to initialise the default preference values.
 * 
 * @author Pyves
 *
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initialises the default values of the parameters in the plugin's preference page.
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Notepad4e.getDefault().getPreferenceStore();
		store.setDefault(Preferences.WRAP, Preferences.WRAP_DEFAULT);
		store.setDefault(Preferences.JUSTIFY, Preferences.JUSTIFY_DEFAULT);
		store.setDefault(Preferences.ALIGNMENT, Preferences.ALIGNMENT_DEFAULT);
		store.setDefault(Preferences.FONT, Preferences.FONT_DEFAULT);
		store.setDefault(Preferences.FONT_COLOR, Preferences.FONT_COLOR_DEFAULT);
		store.setDefault(Preferences.BACKGROUND_COLOR, Preferences.BACKGROUND_COLOR_DEFAULT);
		store.setDefault(Preferences.LINE_SPACING, Preferences.LINE_SPACING_DEFAULT);
		store.setDefault(Preferences.NAME_PREFIX, Preferences.NAME_PREFIX_DEFAULT);
		store.setDefault(Preferences.CLOSE_CONFIRMATION, Preferences.CLOSE_CONFIRMATION_DEFAULT);
		store.setDefault(Preferences.PASTE_CLIPBOARD_IN_NEW_NOTES, Preferences.PASTE_CLIPBOARD_IN_NEW_NOTES_DEFAULT);
		store.setDefault(Preferences.BULLET_SPACING, Preferences.BULLET_SPACING_DEFAULT);
		store.setDefault(Preferences.SAVE_INTERVAL, Preferences.SAVE_INTERVAL_DEFAULT);
		store.setDefault(Preferences.SAVE_LOCATION, Preferences.SAVE_LOCATION_DEFAULT);
	}
}
