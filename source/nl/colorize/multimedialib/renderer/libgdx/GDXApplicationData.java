//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.ApplicationData;

/**
 * Uses libGDX's mechanism for user preferences to store application data.
 */
public class GDXApplicationData implements ApplicationData {

    private Preferences preferences;

    public GDXApplicationData(String applicationName) {
        Preconditions.checkArgument(applicationName.trim().length() >= 2,
            "Invalid application name");

        preferences = Gdx.app.getPreferences(applicationName);
    }

    @Override
    public String get(String key, String defaultValue) {
        String value = preferences.getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public void set(String key, String value) {
        preferences.putString(key, value);
        preferences.flush();
    }

    @Override
    public void clear() {
        preferences.clear();
        preferences.flush();
    }
}
