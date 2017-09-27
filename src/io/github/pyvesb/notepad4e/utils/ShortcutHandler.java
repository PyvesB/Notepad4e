package io.github.pyvesb.notepad4e.utils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import io.github.pyvesb.notepad4e.views.Note;
import io.github.pyvesb.notepad4e.views.NotepadView;

/**
 * Class used to listen to keyboard events and launch actions accordingly.
 * 
 * @author Pyves
 *
 */
public class ShortcutHandler extends AbstractHandler {

	private final NotepadView notepadView;

	/**
	 * Constructor. Sets a reference to the main plugin's view.
	 * 
	 * @param notepadView
	 */
	public ShortcutHandler(NotepadView notepadView) {
		this.notepadView = notepadView;
	}

	/**
	 * Deals with keyboard events, in other words Notepad4e shortcuts. This handler listens to all events, filtering
	 * must therefore be done accordingly.
	 * 
	 * @param event
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (event.getCommand() == null) {
			return null;
		}

		NotepadAction action = NotepadAction.of(event.getCommand().getId());
		if (action == NotepadAction.NEW_NOTE) {
			notepadView.addNewNote();
		} else if (action == NotepadAction.CLOSE_NOTE) {
			notepadView.closeCurrentSelection();
		} else {
			Note selectedNote = notepadView.getSelectedNote();
			if (selectedNote != null) {
				executeNoteAction(action, selectedNote);
			}
		}
		return null;
	}

	/**
	 * Executes an action on the selected note.
	 * 
	 * @param action
	 * @param selectedNote
	 */
	private void executeNoteAction(NotepadAction action, Note selectedNote) {
		switch (action) {
			case BOLD_TEXT:
				selectedNote.boldSelection();
				break;
			case ITALIC_TEXT:
				selectedNote.italicSelection();
				break;
			case UNDERLINE_TEXT:
				selectedNote.underlineSelection();
				break;
			case STRIKEOUT_TEXT:
				selectedNote.strikeoutSelection();
				break;
			case BULLET_LIST:
				selectedNote.bulletListSelection();
				break;
			case CLEAR_STYLE_TEXT:
				selectedNote.clearSelectionStyles();
				break;
			case CLEAR_NOTE:
				selectedNote.clearText();
				break;
			case UNDO_TEXT:
				selectedNote.undo();
				break;
			case REDO_TEXT:
				selectedNote.redo();
				break;
			default:
				break;
		}
	}
}
