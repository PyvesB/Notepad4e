package io.github.pyvesb.notepad4e.utils;

import java.util.Stack;

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
	// populating stacks in this case.
	protected boolean lastActionUndoOrRedo = true;

	// Reference to the NoteTab this manager is handling.
	protected NoteTab noteTab;

	// Used to limit the size of undo and redo actions from growing indefinitely.
	private static final int MAX_STACK_SIZES = 100;

	// Stacks used to store previous text actions and styles.
	private Stack<ModificationRecord> undoStack;
	private Stack<ModificationRecord> redoStack;

	// Styles before starting any undo actions.
	private StyleRange[] stylesBeforeUndo;

	/**
	 * Constructor.
	 * 
	 * @param noteTab
	 */
	public UndoRedoManager(NoteTab note) {
		undoStack = new Stack<>();
		redoStack = new Stack<>();
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
		// Previous action cannot be an undo: empty redo stack and remove stylesBeforeUndo.
		redoStack.clear();
		stylesBeforeUndo = null;

		// Construct modification record depending on whether the function was called by a style or a text
		// modification and push it on the stack.
		if (event != null)
			undoStack.push(new ModificationRecord(styles, event.start, event.text.length(),
					noteTab.getText().substring(event.start, event.end), event.text));
		else
			undoStack.push(new ModificationRecord(styles, 0, 0, null, null));

		// Limit maximum size of stack by clearing oldest records.
		if (undoStack.size() > MAX_STACK_SIZES)
			undoStack.remove(0);
	}

	/**
	 * Performs an undo action.
	 */
	public void undo() {
		// Nothing to undo.
		if (undoStack.isEmpty())
			return;

		// Set styles at the point where undo action start being performed; this information is not stored by any
		// ModificationRecord as they contain styles as they were before the event.
		if (stylesBeforeUndo == null) {
			stylesBeforeUndo = noteTab.getStyleRanges();
		}

		ModificationRecord undoFragment = undoStack.pop();

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

		redoStack.push(undoFragment);
	}

	/**
	 * Performs a redo action.
	 */
	public void redo() {
		// Nothing to redo.
		if (redoStack.isEmpty())
			return;

		ModificationRecord redoFragment = redoStack.pop();

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

		if (!redoStack.isEmpty()) {
			// Styles in ModificationRecord correspond to how they were before the change; redo actions go through the
			// timeline in the other way, the styles must be taken from the next element in the stack without removing
			// it.
			noteTab.setStyleRanges(redoStack.peek().getStyles());
		} else {
			// stack empty: set styles to how they were before any undo actions.
			noteTab.setStyleRanges(stylesBeforeUndo);
		}

		undoStack.push(redoFragment);
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
