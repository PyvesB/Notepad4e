package io.github.pyvesb.notepad4e.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Class used to listen to keyboard events.
 * 
 * @author Pyves
 *
 */
public class NoteTabKeyListener implements VerifyKeyListener, Listener {

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
	 * Enables to deal with keyboard events, in other words Notepad4e shortcuts. These shortcuts are relative to a given
	 * note tab.
	 * 
	 * @param event
	 */
	@Override
	public void verifyKey(VerifyEvent event) {
		// Shortcuts start with CTRL or CMD keys.
		if ((event.stateMask & SWT.CTRL) == 0 && (event.stateMask & SWT.COMMAND) == 0)
			return;

		if (event.keyCode == 'b')
			notepadView.doBoldText();
		else if (event.keyCode == 'i')
			notepadView.doItalicText();
		else if (event.keyCode == 'u')
			notepadView.doUnderlineText();
		else if (event.keyCode == 'd')
			notepadView.doClearTextStyle();
		else if (event.keyCode == 'k')
			notepadView.doClearNote();
		else
			return;

		// Disallow the shortcut previously defined shortcuts to trigger any other actions within the note tab.
		event.doit = false;
	}

	/**
	 * Enables to deal with keyboard events, in other words Notepad4e shortcuts. These shortcuts are relative to a
	 * higher level of abstraction and behaviour of the different note tabs.
	 * 
	 * @param event
	 */
	@Override
	public void handleEvent(Event event) {
		// Shortcuts start with CTRL or CMD keys.
		if ((event.stateMask & SWT.CTRL) == 0 && (event.stateMask & SWT.COMMAND) == 0)
			return;

		// Check for a given key and whether focus is on the plugin's view.
		if (event.keyCode == 'w' && notepadView.isFocused()) {
			notepadView.closeCurrentNoteTab();
		} else if (event.keyCode == 't' && notepadView.isFocused()) {
			notepadView.doNewNote();
		} else {
			return;
		}

		// Disallow the shortcut previously defined shortcuts to trigger any other actions within Eclipse.
		event.doit = false;
	}
}
