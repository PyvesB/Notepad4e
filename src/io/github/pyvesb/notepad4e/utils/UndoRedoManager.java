package io.github.pyvesb.notepad4e.utils;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.swt.custom.StyleRange;

import io.github.pyvesb.notepad4e.views.Note;

/**
 * Class in charge of handling the undo and redo actions of a note.
 * 
 * @author Pyves
 *
 */
public class UndoRedoManager {

	// Used to prevent the size of undo and deques queues from growing indefinitely.
	private static final int MAX_DEQUE_SIZES = 250;

	// Reference to the note this manager is handling.
	private final Note note;
	// Deques used to store note states.
	private final Deque<NoteState> undoDeque = new ArrayDeque<>(MAX_DEQUE_SIZES);
	private final Deque<NoteState> redoDeque = new ArrayDeque<>(MAX_DEQUE_SIZES);

	/**
	 * Constructor, connects the note and the new manager instance.
	 * 
	 * @param note
	 */
	public UndoRedoManager(Note note) {
		this.note = note;
	}

	/**
	 * Saves a note's state to allow undo and redo operations to be performed.
	 */
	public void saveNoteState() {
		// Empty redo deque.
		redoDeque.clear();

		undoDeque.push(new NoteState(note.getText(), note.getCaretOffset(), note.getStyleRanges(), getBulletLineMapping()));

		// Limit maximum size of deque by clearing oldest states.
		if (undoDeque.size() > MAX_DEQUE_SIZES) {
			undoDeque.pollLast();
		}
	}

	/**
	 * Performs an undo action.
	 */
	public void undo() {
		NoteState noteState = undoDeque.pollFirst();
		if (noteState != null) { // Something to undo.
			if (redoDeque.isEmpty()) {
				// Pushes the current state of the note before a sequence of undo operations is performed.
				redoDeque.push(new NoteState(note.getText(), note.getCaretOffset(), note.getStyleRanges(),
						getBulletLineMapping()));
			}
			restoreState(noteState);
			redoDeque.push(noteState);
		}
	}

	/**
	 * Performs a redo action.
	 */
	public void redo() {
		if (redoDeque.size() > 1) { // Something to redo.
			NoteState noteState = redoDeque.removeFirst();
			restoreState(redoDeque.peekFirst());
			undoDeque.push(noteState);
		}
	}

	/**
	 * Restores the note to a previous state.
	 * 
	 * @param noteState
	 */
	private void restoreState(NoteState noteState) {
		// Set the text via the content to avoid firing events which would be picked up by the manager whilst
		// performing undo redo operations.
		note.getContent().setText(noteState.getText());

		note.setCaretOffset(noteState.getCaretOffset());

		note.setStyleRanges(noteState.getStyles());

		for (int line = 0; line < noteState.getBulletLineMapping().length; ++line) {
			note.setLineBullet(line, noteState.getBulletLineMapping()[line]);
		}
	}
	
	/**
	 * Constructs an array containing the bullets for the note.
	 * 
	 * @return array of bullets indexed by line number; null value if no bullet on the line
	 */
	private boolean[] getBulletLineMapping() {
		boolean[] bullets = new boolean[note.getLineCount()];
		for (int line = 0; line < bullets.length; ++line) {
			bullets[line] = (note.getLineBullet(line) != null);
		}
		return bullets;
	}

	/**
	 * Class used to keep track of the state of the note in order to perform undo and redo actions.
	 * 
	 * @author Pyves
	 *
	 */
	private static final class NoteState {

		// Text content of the note.
		final String text;
		// Offset of the caret relative to the start of the text.
		final int caretOffset;
		// Styles of the text, e.g. bold, italic, etc.
		final StyleRange[] styles;
		// Indicates whether a bullet is present at the beginning of each text line.
		final boolean[] bulletLineMapping;

		NoteState(String text, int caretOffset, StyleRange[] styles, boolean[] bulletLineMapping) {
			this.text = text;
			this.caretOffset = caretOffset;
			this.styles = styles;
			this.bulletLineMapping = bulletLineMapping;
		}

		String getText() {
			return text;
		}

		int getCaretOffset() {
			return caretOffset;
		}

		StyleRange[] getStyles() {
			return styles;
		}

		boolean[] getBulletLineMapping() {
			return bulletLineMapping;
		}
	}
}
