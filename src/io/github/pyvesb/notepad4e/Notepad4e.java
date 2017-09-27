package io.github.pyvesb.notepad4e;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Class used to control the plugin's life cycle.
 * 
 * @author Pyves
 *
 */
public class Notepad4e extends AbstractUIPlugin {

	// Plugin's ID.
	public static final String PLUGIN_ID = "Notepad4e";

	// Shared instance. Not ideal to use a static field here, but common practice to do this with AbstractUIPlugin.
	private static Notepad4e plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Saves the DialogSettings (used to remember the plugin's state between Eclipse sessions).
	 */
	public static void save() {
		plugin.saveDialogSettings();
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static Notepad4e getDefault() {
		return plugin;
	}
}
