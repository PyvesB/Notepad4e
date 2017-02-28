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
		store.setDefault(PreferenceConstants.PREF_WRAP, PreferenceConstants.PREF_WRAP_DEFAULT);
		store.setDefault(PreferenceConstants.PREF_JUSTIFY, PreferenceConstants.PREF_JUSTIFY_DEFAULT);
		store.setDefault(PreferenceConstants.PREF_ALIGNMENT, PreferenceConstants.PREF_ALIGNMENT_DEFAULT);
		store.setDefault(PreferenceConstants.PREF_FONT, PreferenceConstants.PREF_FONT_DEFAULT);
		store.setDefault(PreferenceConstants.PREF_FONT_COLOR, PreferenceConstants.PREF_FONT_COLOR_DEFAULT);
		store.setDefault(PreferenceConstants.PREF_BACKGROUND_COLOR, PreferenceConstants.PREF_BACKGROUND_COLOR_DEFAULT);
		store.setDefault(PreferenceConstants.PREF_LINE_SPACING, PreferenceConstants.PREF_LINE_SPACING_DEFAULT);
		store.setDefault(PreferenceConstants.PREF_NAME_PREFIX, PreferenceConstants.PREF_NAME_PREFIX_DEFAULT);
		store.setDefault(PreferenceConstants.PREF_CLOSE_CONFIRMATION, PreferenceConstants.PREF_CLOSE_CONFIRMATION_DEFAULT);
		store.setDefault(PreferenceConstants.PREF_PASTE_CLIPBOARD_IN_NEW_NOTES, PreferenceConstants.PREF_PASTE_CLIPBOARD_IN_NEW_NOTES_DEFAULT);
	}
}
