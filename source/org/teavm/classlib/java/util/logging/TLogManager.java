//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package org.teavm.classlib.java.util.logging;

import java.io.InputStream;

public class TLogManager {

    public void readConfiguration(InputStream stream) {
        // Ignore the configuration, keep using the default logging
        // configuration instead.
    }

    public static TLogManager getLogManager() {
        return new TLogManager();
    }
}
