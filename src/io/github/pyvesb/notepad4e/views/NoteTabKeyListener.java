package io.github.pyvesb.notepad4e.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Class used to listen to keyboard events.
 * 
 * @author Pyves
 *
 */
public class NoteTabKeyListener implements Listener {

	private NotepadView notepadView;

	/**
	 * Constructor.
	 * 
	 * @param notepadView
	 */
	public NoteTabKeyListener(NotepadView notepadView) {
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

		// Shortcuts start with CTRL or CMD keys.
		if ((event.stateMask & SWT.CTRL) == 0 && (event.stateMask & SWT.COMMAND) == 0)
			return;

		// Check for a given key and perform shortcut action accordingly.
		if (event.keyCode == 'w')
			notepadView.closeCurrentNoteTab();
		else if (event.keyCode == 't')
			notepadView.doNewNote();
		else if (event.keyCode == 'b')
			notepadView.doBoldText();
		else if (event.keyCode == 'i')
			notepadView.doItalicText();
		else if (event.keyCode == 'u')
			notepadView.doUnderlineText();
		else if (event.keyCode == 'd')
			notepadView.doClearTextStyle();
		else if (event.keyCode == 'k')
			notepadView.doClearNote();
		else if (event.keyCode == 'z')
			notepadView.doUndo();
		else if (event.keyCode == 'y')
			notepadView.doRedo();
		else
			return;

		// Disallow the shortcut previously defined shortcuts to trigger any other actions within Eclipse or within the
		// plugin.
		event.doit = false;
	}
}
