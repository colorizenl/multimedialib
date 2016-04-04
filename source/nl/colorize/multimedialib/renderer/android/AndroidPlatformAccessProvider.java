//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;

import com.google.common.collect.ImmutableMap;

import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.Version;

/**
 * Extends {@code Platform} with support for Android features. This includes
 * access to asset files, application data, redirecting logging to LogCat, and
 * using the version from the Android manifest.
 */
public class AndroidPlatformAccessProvider implements Platform.PlatformAccessProvider {
	
	private Context context;
	
	private static final Map<Level, Integer> LOG_LEVELS = ImmutableMap.of(
			Level.CONFIG, Log.DEBUG,
			Level.INFO, Log.INFO,
			Level.WARNING, Log.WARN,
			Level.SEVERE, Log.ERROR);
	
	public AndroidPlatformAccessProvider(Context context) {
		this.context = context;
		redirectLoggingToLogCat();
	}

	private void redirectLoggingToLogCat() {
		Logger rootColorizeLogger = LogHelper.getRootColorizeLogger();
		LogHelper.removeHandlers(rootColorizeLogger);
		rootColorizeLogger.addHandler(new Handler() {
			public void publish(LogRecord record) {
				String tag = record.getLoggerName().substring(record.getLoggerName().lastIndexOf('.') + 1);
				int level = LOG_LEVELS.get(record.getLevel());
				String message = record.getMessage();
				if (record.getThrown() != null) {
					message += " - " + Log.getStackTraceString(record.getThrown());
				}
				Log.println(level, tag, message);
			}

			public void close() {
			}

			public void flush() {
			}
		});
	}

	public boolean supports() {
		return Platform.isAndroid();
	}
	
	public InputStream openResourceFile(String path) throws IOException {
		InputStream inClassPath = getClass().getClassLoader().getResourceAsStream(path);
		if (inClassPath != null) {
			return inClassPath;
		}
		
		AssetManager assetManager = context.getAssets();
		try {
			return assetManager.open(path);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot open asset file: " + path, e);
		}
	}
		
	public File getApplicationDataDirectory(String app) {
		return context.getFilesDir();
	}
	
	public File getUserDataDirectory() {
		return context.getFilesDir();
	}
	
	public Version getImplementationVersion(Class<?> classInJarFile) {
		String packageName = context.getPackageName();
		try {
			return Version.parse(context.getPackageManager().getPackageInfo(packageName, 0).versionName);
		} catch (NameNotFoundException e) {
			throw new IllegalArgumentException("Unknown package: " + packageName);
		}
	}
	
	/**
	 * Returns true if the currently running app was downloaded from Google Play.
	 */
	public static boolean isGooglePlay(Context context) {
		String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
		return installer != null && installer.equals("com.android.vending");
	}
	
	public static Point getDisplaySize(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point holder = new Point();
		display.getSize(holder);
		return holder;
	}
	
	/**
	 * Returns a textual description of the device's screen size.
	 */
	public static String getDisplaySizeDescription(Activity activity) {
		Point displaySize = getDisplaySize(activity);
		return displaySize.x + "x" + displaySize.y;
	}
}
