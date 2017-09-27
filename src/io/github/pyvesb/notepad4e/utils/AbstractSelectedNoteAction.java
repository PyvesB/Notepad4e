package io.github.pyvesb.notepad4e.utils;

import org.eclipse.jface.action.Action;

import io.github.pyvesb.notepad4e.views.Note;
import io.github.pyvesb.notepad4e.views.NotepadView;

/**
 * Class used to run an action on the currently selected note.
 * 
 * @author Pyves
 *
 */
public abstract class AbstractSelectedNoteAction extends Action {

	private final NotepadView notepadView;

	/**
	 * Constructor. Sets a reference to the main plugin's view.
	 * 
	 * @param notepadView
	 */
	public AbstractSelectedNoteAction(NotepadView notepadView) {
		this.notepadView = notepadView;
	}

	@Override
	public void run() {
		Note selectedNote = notepadView.getSelectedNote();
		if (selectedNote != null) {
			runSelectedNoteAction(selectedNote);
		}
	}

	/**
	 * Runs an action on the currently selected note.
	 * 
	 * @param selectedNote
	 */
	protected abstract void runSelectedNoteAction(Note selectedNote);

}
