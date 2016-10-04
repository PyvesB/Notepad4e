package io.github.pyvesb.notepad4e.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import io.github.pyvesb.notepad4e.views.NotepadView;

/**
 * Class used to listen to keyboard events and launch actions accordingly.
 * 
 * @author Pyves
 *
 */
public class ShortcutManager implements Listener {

	private NotepadView notepadView;

	/**
	 * Constructor.
	 * 
	 * @param notepadView
	 */
	public ShortcutManager(NotepadView notepadView) {
		this.notepadView = notepadView;
	}

	/**
	 * Enables to deal with keyboard events, in other words Notepad4e shortcuts. All keyboard events are listened to,
	 * filtering must therefore be done accordingly.
	 * 
	 * @param event
	 */
	@Override
	public void handleEvent(Event event) {
		// Check whether focus is on the plugin's view, ignore if not.
		if (!notepadView.isFocused())
			return;

		if ((event.stateMask & (SWT.CTRL | SWT.SHIFT)) == (SWT.CTRL | SWT.SHIFT)
				|| (event.stateMask & (SWT.COMMAND | SWT.SHIFT)) == (SWT.COMMAND | SWT.SHIFT)) {
			// ctrl+shift+z redo shortcut; also available via ctrl+y.
			if (event.keyCode == 'z') {
				notepadView.doRedo();
			} else
				return;
		} else if ((event.stateMask & SWT.CTRL) == SWT.CTRL || (event.stateMask & SWT.COMMAND) == SWT.COMMAND) {
			// ctrl+key shortcut.
			switch (event.keyCode) {
			case 't':
				notepadView.doNewNote();
				break;
			case 'b':
				notepadView.doBoldText();
				break;
			case 'i':
				notepadView.doItalicText();
				break;
			case 'u':
				notepadView.doUnderlineText();
				break;
			case 'd':
				notepadView.doClearTextStyle();
				break;
			case 'k':
				notepadView.doClearNote();
				break;
			case 'z':
				notepadView.doUndo();
				break;
			case 'y':
				notepadView.doRedo();
				break;
			default:
				return;
			}
		} else {
			// No shortcut relevant to the plugin: return without cancelling event.
			return;
		}

		// Disallow the shortcut previously defined shortcuts to trigger any other actions within Eclipse or within the
		// plugin.
		event.doit = false;
	}
}
