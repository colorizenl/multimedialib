//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package org.teavm.classlib.java.awt;

public class TColor {

    private int r;
    private int g;
    private int b;
    private int a;

    public static final TColor BLACK = new TColor(0, 0, 0);
    public static final TColor WHITE = new TColor(255, 255, 255);

    public TColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public TColor(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public TColor(float r, float g, float b) {
        this(r, g, b, 1f);
    }

    public TColor(float r, float g, float b, float a) {
        this(Math.round(r * 255f), Math.round(g * 255f), Math.round(b * 255f), Math.round(a * 255f));
    }

    public TColor(int value) {
        this.r = (value >> 16) & 0xFF;
        this.g = (value >> 8) & 0xFF;
        this.b = (value) & 0xFF;
        this.a = (value >> 24) & 0xFF;
    }

    public int getRed() {
        return r;
    }

    public int getGreen() {
        return g;
    }

    public int getBlue() {
        return b;
    }

    public int getAlpha() {
        return a;
    }
}
