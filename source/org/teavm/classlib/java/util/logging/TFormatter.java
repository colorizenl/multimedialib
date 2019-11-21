//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package org.teavm.classlib.java.util.logging;

import java.util.logging.LogRecord;

public abstract class TFormatter {

    public abstract String format(LogRecord record);
}
