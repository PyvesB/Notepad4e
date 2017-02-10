package io.github.pyvesb.notepad4e;

import org.eclipse.jface.resource.ImageDescriptor;
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

	// Shared instance.
	private static Notepad4e plugin;

	/**
	 * Constructor.
	 */
	public Notepad4e() {}

	/**
	 * Starts the plugin.
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/**
	 * Stops the plugin.
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static Notepad4e getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 *
	 * @param path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
