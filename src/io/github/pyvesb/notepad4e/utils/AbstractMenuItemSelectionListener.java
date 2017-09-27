package io.github.pyvesb.notepad4e.utils;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Class used to implement part of the functionality of the SelectionListener, for menu items. This simply provides an
 * empty widgetDefaultSelected method, as well as a more specifically named/parameterless onNoteMenuItemSelected method.
 * Nothing fancy.
 * 
 * @author Pyves
 *
 */
public abstract class AbstractMenuItemSelectionListener implements SelectionListener {

	@Override
	public void widgetSelected(SelectionEvent event) {
		onNoteMenuItemSelected();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		// Not called when the SelectionListener is attached to a MenuItem.
	}

	/**
	 * Runs an action when the menu item is selected.
	 */
	protected abstract void onNoteMenuItemSelected();

}
