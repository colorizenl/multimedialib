//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package org.teavm.classlib.java.util;

public class TUUID {

    private String value;

    private TUUID(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TUUID) {
            TUUID other = (TUUID) o;
            return value.equals(other.value);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    public static TUUID randomUUID() {
        String value = s4() + s4() + "-" + s4() + "-" + s4() + "-" + s4() + "-" + s4() + s4() + s4();
        return new TUUID(value);
    }

    private static String s4() {
        return Integer.toString((int) Math.floor((1 + Math.random()) * 65536), 16).substring(1);
    }

    public static TUUID fromString(String value) {
        return new TUUID(value);
    }
}
