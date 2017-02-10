package io.github.pyvesb.notepad4e.utils;

/**
 * Enum used to represent all the possible actions in the plugin that are either handled by shortcuts or by buttons in
 * NotepadView or by both.
 * 
 * @author Pyves
 *
 */
public enum NotepadAction {

	NEW_NOTE("New Note", "notepad4e.command.note.new", "/icons/file-new-16x16.png"),
	BOLD_TEXT("Bold", "notepad4e.command.text.bold", "/icons/style_bold.gif"),
	ITALIC_TEXT("Italic", "notepad4e.command.text.italic", "/icons/style_italic.gif"),
	UNDERLINE_TEXT("Underline", "notepad4e.command.text.underline", "/icons/style_underline.gif"),
	CLEAR_TEXT_STYLE("Clear Style", "notepad4e.command.text.clear", "/icons/clear_co.gif"),
	CLEAR_NOTE("Clear Note", "notepad4e.command.note.clear", "/icons/clear.png"),
	UNDO(null, "notepad4e.command.text.undo", null),
	REDO(null, "notepad4e.command.text.redo", null),
	CLOSE(null, "notepad4e.command.note.close", null),
	SAVE_NOTE("Export File", null, "/icons/saveas_edit_floppy.png"),
	PREFERENCES("Preferences", null, "/icons/settings_obj.png"),
	WEBSITE("Webpage", null, "/icons/web.png"),
	CHANGELOG("Changelog", null, "/icons/changelog_obj.gif");
	
	private final String text;
	private final String commandID;
	private final String imagePath;

	private NotepadAction(String text, String commandID, String image) {
		this.text = text;
		this.commandID = commandID;
		this.imagePath = image;
	}

	public static NotepadAction of(String commandID) {
		for (NotepadAction notepadAction : values()) {
			if (commandID.equals(notepadAction.commandID)) {
				return notepadAction;
			}
		}
		return null;
	}
	
	public final String getText() {
		return text;
	}

	public final String getCommandID() {
		return commandID;
	}
	
	public final String getImagePath() {
		return imagePath;
	}
}
