package io.github.pyvesb.notepad4e.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;

import io.github.pyvesb.notepad4e.Notepad4e;
import io.github.pyvesb.notepad4e.strings.LocalStrings;

import org.eclipse.ui.IWorkbench;

/**
 * Class representing the preference page of the plugin.
 * 
 * @author Pyves
 *
 */
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Creates the field editors corresponding to the different preferences.
	 */
	@Override
	public void createFieldEditors() {
		addField(new RadioGroupFieldEditor(Preferences.ALIGNMENT, LocalStrings.prefAlignment, 2,
				new String[][] { { LocalStrings.prefLeft, "left" }, { LocalStrings.prefRight, "right" } },
				getFieldEditorParent(), true));
		addField(new BooleanFieldEditor(Preferences.CLOSE_CONFIRMATION, LocalStrings.prefCloseConfirmation,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.PASTE_CLIPBOARD_IN_NEW_NOTES, LocalStrings.prefPasteClipboardInNewNotes,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.WRAP, LocalStrings.prefWrap, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Preferences.JUSTIFY, LocalStrings.prefJustify, getFieldEditorParent()));
		addField(new IntegerFieldEditor(Preferences.LINE_SPACING, LocalStrings.prefLineSpacing, getFieldEditorParent()));
		addField(new IntegerFieldEditor(Preferences.BULLET_SPACING, LocalStrings.prefBulletSpacing, getFieldEditorParent()));
		addField(new StringFieldEditor(Preferences.NAME_PREFIX, LocalStrings.prefNamePrefix, getFieldEditorParent()));
		addField(new ColorFieldEditor(Preferences.FONT_COLOR, LocalStrings.prefFontColor, getFieldEditorParent()));
		addField(new ColorFieldEditor(Preferences.BACKGROUND_COLOR, LocalStrings.prefBackgroundColor,
				getFieldEditorParent()));
		addField(new FontFieldEditor(Preferences.FONT, LocalStrings.prefFont, getFieldEditorParent()));
		addField(new IntegerFieldEditor(Preferences.SAVE_INTERVAL, LocalStrings.prefSaveInterval, getFieldEditorParent()));
		addField(new DirectoryFieldEditor(Preferences.SAVE_LOCATION, LocalStrings.prefSaveLocation, getFieldEditorParent()));
	}

	/**
	 * Initialises the preference page.
	 */
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Notepad4e.getDefault().getPreferenceStore());
		setDescription(LocalStrings.prefDesc);
	}
}