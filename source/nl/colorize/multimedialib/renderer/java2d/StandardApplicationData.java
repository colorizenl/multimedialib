//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.ApplicationData;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Saves application data to a simple {@code .properties} file in the platform's
 * standard location.
 */
public class StandardApplicationData implements ApplicationData {

    private Properties data;
    private File preferencesFile;

    private static final String PREFERENCES_FILE_NAME = "preferences.properties";
    private static final Logger LOGGER = LogHelper.getLogger(StandardApplicationData.class);

    public StandardApplicationData(String applicationName) {
        Preconditions.checkArgument(applicationName.trim().length() >= 2,
            "Invalid application name");

        data = new Properties();
        preferencesFile = Platform.getApplicationData(applicationName, PREFERENCES_FILE_NAME);

        if (preferencesFile.exists()) {
            try {
                data = LoadUtils.loadProperties(preferencesFile, Charsets.UTF_8);
            } catch (IOException e) {
                LOGGER.warning("Cannot load application data: " + e.getMessage());
            }
        }
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = data.getProperty(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public void set(String key, String value) {
        data.setProperty(key, value);
        save();
    }

    @Override
    public void clear() {
        data.clear();
        save();
    }

    private void save() {
        try {
            LoadUtils.saveProperties(data, preferencesFile, Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warning("Cannot save application data: " + e.getMessage());
        }
    }
}
