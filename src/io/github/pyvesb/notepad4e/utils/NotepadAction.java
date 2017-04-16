package io.github.pyvesb.notepad4e.utils;

/**
 * Enum used to represent all the possible user actions in the plugin. These are either handled by shortcuts or by
 * NotepadView buttons or by both.
 * 
 * @author Pyves
 *
 */
public enum NotepadAction {

	NEW_NOTE("New Note", "notepad4e.command.note.new", "/icons/file-new-16x16.png"),
	BOLD_TEXT("Bold", "notepad4e.command.text.bold", "/icons/style_bold.gif"),
	ITALIC_TEXT("Italic", "notepad4e.command.text.italic", "/icons/style_italic.gif"),
	UNDERLINE_TEXT("Underline", "notepad4e.command.text.underline", "/icons/style_underline.gif"),
	STRIKEOUT_TEXT("Strikeout", "notepad4e.command.text.strikeout", "/icons/style_strikeout.png"),
	CLEAR_STYLE_TEXT("Clear Style", "notepad4e.command.text.clear", "/icons/clear_co.png"),
	BULLET_LIST("Bullet List", "notepad4e.command.bullet.list", "/icons/bullets.png"),
	CLEAR_NOTE("Clear Note", "notepad4e.command.note.clear", "/icons/clear.png"),
	UNDO_TEXT(null, "notepad4e.command.text.undo", null),
	REDO_TEXT(null, "notepad4e.command.text.redo", null),
	CLOSE_NOTE(null, "notepad4e.command.note.close", null),
	TOGGLE_EDITABLE_NOTE("Lock/Unlock Note", null, "/icons/deadlock_view.png"),
	EXPORT_NOTE("Export Note", null, "/icons/save_edit.png"),
	PREFERENCES("Preferences", null, "/icons/settings_obj.png"),
	WEBSITE("Project Webpage", null, "/icons/web.png"),
	CHANGELOG("Changelog", null, "/icons/change.gif");
	
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
