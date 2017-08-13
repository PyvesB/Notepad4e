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

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Notepad4e.getDefault().getPreferenceStore());
		setDescription(LocalStrings.prefDesc);
	}

	/**
	 * Creates the field editors corresponding to the different preferences.
	 */
	@Override
	public void createFieldEditors() {
		addField(new RadioGroupFieldEditor(PreferenceConstants.PREF_ALIGNMENT, LocalStrings.prefAlignment, 2,
				new String[][] { { LocalStrings.prefLeft, "left" }, { LocalStrings.prefRight, "right" } },
				getFieldEditorParent(), true));
		addField(new BooleanFieldEditor(PreferenceConstants.PREF_CLOSE_CONFIRMATION, LocalStrings.prefCloseConfirmation,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.PREF_PASTE_CLIPBOARD_IN_NEW_NOTES,
				LocalStrings.prefPasteClipboardInNewNotes, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.PREF_WRAP, LocalStrings.prefWrap, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.PREF_JUSTIFY, LocalStrings.prefJustify,
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.PREF_LINE_SPACING, LocalStrings.prefLineSpacing,
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.PREF_BULLET_SPACING, LocalStrings.prefBulletSpacing,
				getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PREF_NAME_PREFIX, LocalStrings.prefNamePrefix,
				getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.PREF_FONT_COLOR, LocalStrings.prefFontColor,
				getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.PREF_BACKGROUND_COLOR, LocalStrings.prefBackgroundColor,
				getFieldEditorParent()));
		addField(new FontFieldEditor(PreferenceConstants.PREF_FONT, LocalStrings.prefFont, getFieldEditorParent()));
	}

	/**
	 * Initialise the preference page.
	 */
	@Override
	public void init(IWorkbench workbench) {}

}