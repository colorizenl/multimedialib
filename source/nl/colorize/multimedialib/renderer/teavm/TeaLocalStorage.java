//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.ApplicationData;

public class TeaLocalStorage implements ApplicationData {

    @Override
    public String get(String key, String defaultValue) {
        String value = Browser.getLocalStorage(key);
        if (value == null || value.isEmpty()) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public void set(String key, String value) {
        Browser.setLocalStorage(key, value);
    }

    @Override
    public void clear() {
        Browser.clearLocalStorage();
    }
}
