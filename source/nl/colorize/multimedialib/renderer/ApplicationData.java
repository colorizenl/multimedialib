//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.graphics.ColorRGB;

import java.util.Date;

/**
 * Interface for using the renderer to save or load persistent application data.
 * Application data is saved to the platform's standard location. Storage is
 * limited to simple key/value pairs, as some platforms do not allow saving
 * arbitrary files. Although all data is stored as strings internally,
 * convenience methods are available to parse the values as common data types
 * such as integers, floats, or booleans.
 */
public interface ApplicationData {

    public void set(String key, String value);

    public String get(String key, String defaultValue);

    default void remove(String key) {
        set(key, (String) null);
    }

    public void clear();

    // Convenience methods

    default void set(String key, int value) {
        set(key, String.valueOf(value));
    }

    default int get(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }

    default void set(String key, float value) {
        set(key, String.valueOf(value));
    }

    default float get(String key, float defaultValue) {
        return Float.parseFloat(get(key, String.valueOf(defaultValue)));
    }

    default void set(String key, boolean value) {
        set(key, String.valueOf(value));
    }

    default boolean get(String key, boolean defaultValue) {
        return get(key, String.valueOf(defaultValue)).equals("true");
    }

    default void set(String key, Date date) {
        set(key, String.valueOf(date.getTime()));
    }

    default Date get(String key, Date defaultValue) {
        long timestamp = Long.parseLong(get(key, String.valueOf(defaultValue.getTime())));
        return new Date(timestamp);
    }

    default void set(String key, ColorRGB color) {
        set(key, color.toHex());
    }

    default ColorRGB get(String key, ColorRGB defaultValue) {
        String hex = get(key, defaultValue.toHex());
        return ColorRGB.parseHex(hex);
    }
}
