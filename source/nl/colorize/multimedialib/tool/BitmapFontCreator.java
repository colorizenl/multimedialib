//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;

import nl.colorize.multimedialib.graphics.ImageAtlas;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.util.swing.Utils2D;

/**
 * Creates bitmap fonts by rendering all characters from a TrueType font to an
 * image. The created bitmap font consists of an image atlas and a XML file that
 * contains the font's metadata. Font rendering is done using Java2D.
 */
public class BitmapFontCreator extends CommandLineTool {
    
    @Option(name="-font", required=true, usage="Name of TrueType font to convert to bitmap font")
    private String fontFace;
    
    @Option(name="-style", required=false, usage="One of 'plain', 'bold', 'italic'")
    private String fontStyle = "plain";
    
    @Option(name="-size", required=true, usage="Font size in points")
    private int fontSize;
    
    @Option(name="-imageWidth", required=true, usage="Width of the font's image atlas")
    private int imageWidth;
    
    @Option(name="-out", required=true, usage="Output directory")
    private String outputDirPath;
    
    private static final int ANTI_ALIAS_PADDING = 0;

    public static void main(String[] args) {
        BitmapFontCreator tool = new BitmapFontCreator();
        tool.start(args);
    }

    public void run() {
        Font font = getTrueTypeFont();
        List<String> alphabet = getGlyphsAlphabet();
        Map<String, BufferedImage> glyphImages = renderGlyphImages(font, alphabet);
        
        ImageAtlasPacker imageAtlasPacker = new ImageAtlasPacker();
        ImageAtlas imageAtlas = imageAtlasPacker.createImageAtlas(glyphImages, imageWidth);
        File outputDir = parseOutputDirectory(outputDirPath, true);
        imageAtlasPacker.saveImageAtlas(imageAtlas, outputDir);
    }
    
    private Font getTrueTypeFont() {
        return new Font(fontFace, getFontStyleFlags(), fontSize);
    }

    private int getFontStyleFlags() {
        if (fontStyle.equals("bold")) {
            return Font.BOLD;
        } else if (fontStyle.equals("italic")) {
            return Font.ITALIC;
        } else {
            return Font.PLAIN;
        }
    }
    
    private List<String> getGlyphsAlphabet() {
        String alphabet = " ";
        alphabet += "abcdefghijklmnopqrstuvwxyz";
        alphabet += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        alphabet += "1234567890";
        alphabet += "!@#%&*()-_+=[]:;'\"\\|,./?";
        
        List<String> alphabetList = new ArrayList<String>();
        for (int i = 0; i < alphabet.length(); i++) {
            alphabetList.add(Character.toString(alphabet.charAt(i)));
        }
        return alphabetList;
    }
    
    private Map<String, BufferedImage> renderGlyphImages(Font font, List<String> alphabet) {
        Map<String, BufferedImage> glyphImages = new LinkedHashMap<String, BufferedImage>();
        for (String letter : alphabet) {
            glyphImages.put(letter, renderGlyphImage(font, letter));
        }
        return glyphImages;
    }

    private BufferedImage renderGlyphImage(Font font, String letter) {
        int glyphWidth = determineGlyphWidth(font, letter);
        int glyphHeight = determineGlyphHeight(font);
        int baseline = Math.round(0.75f * glyphHeight);
        
        BufferedImage glyphImage = new BufferedImage(glyphWidth, glyphHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(glyphImage, true, true);
        Utils2D.drawStringCentered(g2, letter, glyphWidth / 2, baseline);
        g2.dispose();
        return glyphImage;
    }

    private int determineGlyphWidth(Font font, String letter) {
        BufferedImage scratchImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(scratchImage, true, true);
        int glyphWidth = g2.getFontMetrics(font).stringWidth(letter) + 2 * ANTI_ALIAS_PADDING;
        g2.dispose();
        return glyphWidth;
    }

    private int determineGlyphHeight(Font font) {
        return MathUtils.nextPowerOfTwo(font.getSize() + 2 * ANTI_ALIAS_PADDING);
    }
}
