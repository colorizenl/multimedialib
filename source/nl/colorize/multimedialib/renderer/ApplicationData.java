//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

/**
 * Interface for using the renderer to save or load persistent application data.
 * Application data is saved to the platform's standard location. Storage is
 * limited to simple key/value pairs, as some platforms do not allow saving
 * arbitrary files.
 */
public interface ApplicationData {

    public String get(String key, String defaultValue);

    public void set(String key, String value);

    public void clear();
}
