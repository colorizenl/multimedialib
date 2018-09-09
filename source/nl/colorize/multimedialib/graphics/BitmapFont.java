//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.math.Rect;

/**
 * Font that uses images for glyphs. Unlike TrueType fonts, bitmap fonts always 
 * have the same size, style, and color. This makes them less versatile, but
 * (generally) faster. 
 */
public class BitmapFont {

    private ImageAtlas imageAtlas;
    private int baseline;
    private int lineHeight;
    private int letterSpacing;
    
    private static final float DEFAULT_BASELINE_FACTOR = 0.75f;
    private static final float DEFAULT_LINE_HEIGHT_FACTOR = 1.5f;
    private static final int DEFAULT_LETTER_SPACING = 0;
    private static final String GLYPH_NOT_FOUND = "?";

    public BitmapFont(ImageAtlas imageAtlas) {
        this.imageAtlas = imageAtlas;
        
        Rect typicalGlyph = imageAtlas.getSubImageBounds("a");
        baseline = Math.round(DEFAULT_BASELINE_FACTOR * typicalGlyph.getHeight());
        lineHeight = Math.round(DEFAULT_LINE_HEIGHT_FACTOR * typicalGlyph.getHeight());
        letterSpacing = DEFAULT_LETTER_SPACING;
        
        // Ensure font has minimum required set of glyphs.
        getGlyph(GLYPH_NOT_FOUND.charAt(0));
    }

    public int getBaseline() {
        return baseline;
    }

    public int getLineHeight() {
        return lineHeight;
    }
    
    public int getLetterSpacing() {
        return letterSpacing;
    }
    
    public ImageAtlas getImageAtlas() {
        return imageAtlas;
    }
    
    private String toGlyphName(char c) {
        String glyph = Character.toString(c);
        if (imageAtlas.containsSubImage(glyph)) {
            return glyph;
        } else {
            return GLYPH_NOT_FOUND;
        }
    }
    
    public Image getGlyph(char c) {
        return imageAtlas.getSubImage(toGlyphName(c));
    }
    
    public Rect getGlyphBounds(char c) {
        return imageAtlas.getSubImageBounds(toGlyphName(c));
    }
}
