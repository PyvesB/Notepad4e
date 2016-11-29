package io.github.pyvesb.notepad4e.utils;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

import io.github.pyvesb.notepad4e.views.NoteTab;

/**
 * Class in charge of handling the undo and redo actions of a NoteTab.
 * 
 * @author Pyves
 *
 */
public class UndoRedoManager {

	// When performing an undo or redo action action, the ExtendedModifyListener is fired; boolean used to prevent from
	// populating deques in this case.
	protected boolean lastActionUndoOrRedo = true;

	// Reference to the NoteTab this manager is handling.
	protected NoteTab noteTab;

	// Used to limit the size of undo and redo actions from growing indefinitely.
	private static final int MAX_DEQUE_SIZES = 250;

	// Deques used to store previous text actions and styles.
	private Deque<ModificationRecord> undoDeque;
	private Deque<ModificationRecord> redoDeque;

	// Styles before starting any undo actions.
	private StyleRange[] stylesBeforeUndo;

	/**
	 * Constructor.
	 * 
	 * @param noteTab
	 */
	public UndoRedoManager(NoteTab note) {
		undoDeque = new ArrayDeque<>();
		redoDeque = new ArrayDeque<>();
		this.noteTab = note;

		// Listen to text modifications.
		noteTab.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent event) {
				// Last modification was due to an undo/redo action: do not record it.
				if (lastActionUndoOrRedo) {
					lastActionUndoOrRedo = false;
					return;
				}
				recordTabModification(event, noteTab.getStyleRanges());
			}
		});
	}

	/**
	 * Records history for undo/redo functions.
	 * 
	 * @param event
	 * @param styles
	 */
	public void recordTabModification(VerifyEvent event, StyleRange[] styles) {
		// Previous action cannot be an undo: empty redo deque and remove stylesBeforeUndo.
		redoDeque.clear();
		stylesBeforeUndo = null;

		// Construct modification record depending on whether the function was called by a style or a text
		// modification and push it on the deque.
		if (event != null) {
			undoDeque.push(new ModificationRecord(styles, event.start, event.text.length(),
					noteTab.getText().substring(event.start, event.end), event.text));
		} else {
			undoDeque.push(new ModificationRecord(styles, 0, 0, null, null));
		}

		// Limit maximum size of deque by clearing oldest records.
		if (undoDeque.size() > MAX_DEQUE_SIZES) {
			undoDeque.pollLast();
		}
	}

	/**
	 * Performs an undo action.
	 */
	public void undo() {
		// Nothing to undo.
		if (undoDeque.isEmpty()) {
			return;
		}

		// Set styles at the point where undo action start being performed; this information is not stored by any
		// ModificationRecord as they contain styles as they were before the event.
		if (stylesBeforeUndo == null) {
			stylesBeforeUndo = noteTab.getStyleRanges();
		}

		ModificationRecord undoFragment = undoDeque.pop();

		if (undoFragment.getReplacedText() != null) {
			// Ignore next ExtendedModifyEvent.
			lastActionUndoOrRedo = true;

			String noteTabText = noteTab.getText();

			StringBuilder previousString = new StringBuilder(noteTabText.substring(0, undoFragment.getStart()));
			previousString.append(undoFragment.getReplacedText());
			previousString.append(noteTabText.substring(undoFragment.getStart() + undoFragment.getLength()));

			noteTab.setText(previousString.toString());
		}

		noteTab.setStyleRanges(undoFragment.getStyles());

		redoDeque.push(undoFragment);
	}

	/**
	 * Performs a redo action.
	 */
	public void redo() {
		// Nothing to redo.
		if (redoDeque.isEmpty()) {
			return;
		}

		ModificationRecord redoFragment = redoDeque.pop();

		if (redoFragment.getNewText() != null) {
			// Ignore next ExtendedModifyEvent.
			lastActionUndoOrRedo = true;

			String noteTabText = noteTab.getText();

			StringBuilder previousString = new StringBuilder(noteTabText.substring(0, redoFragment.getStart()));
			previousString.append(redoFragment.getNewText());
			previousString
					.append(noteTabText.substring(redoFragment.getStart() + redoFragment.getReplacedText().length()));

			noteTab.setText(previousString.toString());
		}

		if (!redoDeque.isEmpty()) {
			// Styles in ModificationRecord correspond to how they were before the change; redo actions go through the
			// timeline in the other way, the styles must be taken from the next element in the deque without removing
			// it.
			noteTab.setStyleRanges(redoDeque.peek().getStyles());
		} else {
			// deque empty: set styles to how they were before any undo actions.
			noteTab.setStyleRanges(stylesBeforeUndo);
		}

		undoDeque.push(redoFragment);
	}

	/**
	 * Class used to keep records of the state of the note tab to be able to perform undo and redo actions.
	 * 
	 * @author Pyves
	 *
	 */
	private class ModificationRecord {

		// Styles at the beginning of the modification.
		private StyleRange[] styles;
		// Starting point of what was modified in the new text.
		private int start;
		// Length of what was modified in the new text.
		private int length;
		// Replaced text.
		private String replacedText;
		// New text.
		private String newText;

		public ModificationRecord(StyleRange[] styles, int start, int length, String replacedText, String newText) {
			this.styles = styles;
			this.start = start;
			this.length = length;
			this.replacedText = replacedText;
			this.newText = newText;
		}

		public StyleRange[] getStyles() {
			return styles;
		}

		public int getStart() {
			return start;
		}

		public int getLength() {
			return length;
		}

		public String getReplacedText() {
			return replacedText;
		}

		public String getNewText() {
			return newText;
		}
	}
}
