package io.github.pyvesb.notepad4e.utils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

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
	 * Constructor.
	 * 
	 * @param notepadView
	 */
	public ShortcutHandler(NotepadView notepadView) {
		this.notepadView = notepadView;
	}

	/**
	 * Enables to deal with keyboard events, in other words Notepad4e shortcuts. All keyboard events are listened to,
	 * filtering must therefore be done accordingly.
	 * 
	 * @param event
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (event.getCommand() == null) {
			return null;
		}

		switch (NotepadAction.of(event.getCommand().getId())) {
		case NEW_NOTE:
			notepadView.doNewNote();
			break;
		case BOLD_TEXT:
			notepadView.doBoldText();
			break;
		case ITALIC_TEXT:
			notepadView.doItalicText();
			break;
		case UNDERLINE_TEXT:
			notepadView.doUnderlineText();
			break;
		case CLEAR_TEXT_STYLE:
			notepadView.doClearTextStyle();
			break;
		case CLEAR_NOTE:
			notepadView.doClearNote();
			break;
		case UNDO:
			notepadView.doUndo();
			break;
		case REDO:
			notepadView.doRedo();
			break;
		case CLOSE:
			notepadView.doClose();
			break;
		default:
			break;
		}
		return null;
	}
}
