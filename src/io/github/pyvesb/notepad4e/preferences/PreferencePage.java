package io.github.pyvesb.notepad4e.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;

import io.github.pyvesb.notepad4e.Notepad4e;

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
		setDescription(
				"Modify the appearance and several editor properties of Notepad4e.\nEdit shortcuts in the General -> Keys section of Eclipse preferences.");
	}

	/**
	 * Creates the field editors corresponding to the different preferences.
	 */
	@Override
	public void createFieldEditors() {
		addField(new RadioGroupFieldEditor(PreferenceConstants.PREF_ALIGNMENT, "Text alignment:", 2,
				new String[][] { { "Left", "left" }, { "Right", "right" } }, getFieldEditorParent(), true));
		addField(new BooleanFieldEditor(PreferenceConstants.PREF_CLOSE_CONFIRMATION, "Confirmation when closing a note",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.PREF_PASTE_CLIPBOARD_IN_NEW_NOTES,
				"Paste current contents of clipboard when creating a new note", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.PREF_WRAP,
				"Wrap lines in notes (automatically enforced if right alignment)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.PREF_JUSTIFY, "Justify lines", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.PREF_LINE_SPACING, "Line spacing:", 
				getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PREF_NAME_PREFIX, "Name prefix of new notes:",
				getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.PREF_FONT_COLOR, "Font color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.PREF_BACKGROUND_COLOR, "Background color:",
				getFieldEditorParent()));
		addField(new FontFieldEditor(PreferenceConstants.PREF_FONT, "Text font:", getFieldEditorParent()));
	}

	/**
	 * Initialise the preference page.
	 */
	@Override
	public void init(IWorkbench workbench) {}

}