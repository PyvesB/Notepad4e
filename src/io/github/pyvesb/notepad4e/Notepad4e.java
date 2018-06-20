package io.github.pyvesb.notepad4e;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import io.github.pyvesb.notepad4e.preferences.Preferences;
import io.github.pyvesb.notepad4e.strings.LocalStrings;

/**
 * Class used to control the plugin's life cycle.
 * 
 * @author Pyves
 *
 */
public class Notepad4e extends AbstractUIPlugin {

	// Plugin's ID.
	public static final String PLUGIN_ID = "Notepad4e";

	private static final String FN_DIALOG_SETTINGS_CUSTOM = "notepad4e.xml";
	private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml";

	// Shared instance. Not ideal to use a static field here, but common practice to do this with AbstractUIPlugin.
	private static Notepad4e plugin;

	private IDialogSettings dialogSettings;

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

	@Override
	public IDialogSettings getDialogSettings() {
		if (dialogSettings == null) {
			restoreDialogSettings();
		}
		return dialogSettings;
	}

	@Override
	public void saveDialogSettings() {
		String directory = getDialogSettingsDirectory();
		saveDialogSettings(directory);
	}

	public void saveDialogSettings(String directory) {
		String settingsPath;
		if (directory == null || directory.isEmpty()) {
			settingsPath = getStateLocation().append(FN_DIALOG_SETTINGS).toOSString();
		} else {
			settingsPath = directory + File.separator + FN_DIALOG_SETTINGS_CUSTOM;
		}
		try {
			dialogSettings.save(settingsPath);
		} catch (IOException | IllegalStateException e) {
			// Ignore problems as in super.saveDialogSettings().
		}
	}

	public void restoreDialogSettings() {
		String directory = getDialogSettingsDirectory();
		if (directory == null || directory.isEmpty()) {
			dialogSettings = super.getDialogSettings();
		} else {
			dialogSettings = new DialogSettings("Workbench");
			String settingsPath = directory + File.separator + FN_DIALOG_SETTINGS_CUSTOM;
			File settingsFile = new File(settingsPath);
			if (settingsFile.exists()) {
				try {
					dialogSettings.load(settingsPath);
				} catch (IOException e) {
					getLog().log(new Status(IStatus.ERROR, LocalStrings.getDialogSettingsErrorMsg, e.toString()));
					dialogSettings = super.getDialogSettings();
				}
			}
		}
	}

	private String getDialogSettingsDirectory() {
		return InstanceScope.INSTANCE.getNode(PLUGIN_ID).get(Preferences.SAVE_LOCATION, Preferences.SAVE_LOCATION_DEFAULT);
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
