//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import nl.colorize.multimedialib.graphics.BitmapFont;
import nl.colorize.multimedialib.graphics.Image;
import nl.colorize.multimedialib.graphics.ImageAtlas;
import nl.colorize.multimedialib.graphics.ImageAtlasLoader;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.java2d.StandardMediaLoader;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;
import nl.colorize.util.xml.XMLHelper;
import org.jdom2.Document;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ImageAtlasLoaderTest {

    private ImageAtlasLoader imageAtlasLoader;

    @Before
    public void before() {
        imageAtlasLoader = new ImageAtlasLoader(new StandardMediaLoader());
    }

    @Test
    public void testLoadImageAtlas() {
        ImageAtlas imageAtlas = imageAtlasLoader.load(new ResourceFile("mario.png"),
                new ResourceFile("mario.atlas.xml"));

        assertEquals(256, imageAtlas.getSourceImage().getWidth());
        assertEquals(256, imageAtlas.getSourceImage().getHeight());

        assertEquals(20, imageAtlas.getSubImages().size());
        assertEquals(new Rect(0, 0, 48, 64), imageAtlas.getSubImageBounds("north_0"));
        assertEquals(new Rect(96, 0, 48, 64), imageAtlas.getSubImageBounds("north_2"));
        assertEquals(new Rect(0, 192, 48, 64), imageAtlas.getSubImageBounds("west_0"));
        assertEquals(new Rect(192, 192, 48, 64), imageAtlas.getSubImageBounds("west_4"));
    }

    @Test
    public void testExportImageAtlas() {
        ImageAtlas atlas = new ImageAtlas(new MockImage(256, 256));
        atlas.markSubImage("a", new Rect(0, 0, 128, 128));
        atlas.markSubImage("b", new Rect(142, 14, 100, 100));

        Document xmlDocument = imageAtlasLoader.export(atlas);
        String xml = XMLHelper.toString(xmlDocument);

        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        expected += "<imageAtlas>\n";
        expected += "    <subImage name=\"a\">\n";
        expected += "        <region x=\"0\" y=\"0\" width=\"128\" height=\"128\" />\n";
        expected += "    </subImage>\n";
        expected += "    <subImage name=\"b\">\n";
        expected += "        <region x=\"142\" y=\"14\" width=\"100\" height=\"100\" />\n";
        expected += "    </subImage>\n";
        expected += "</imageAtlas>\n";

        assertEquals(expected, xml);
    }

    @Test
    public void testLoadedBitmapFontsArePreloaded() throws Exception {
        BufferedImage testImage = Utils2D.createTestImage(128, 128);
        File imageFile = LoadUtils.getTempFile(".png");
        Utils2D.savePNG(testImage, imageFile);

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        xml += "<imageAtlas>\n";
        xml += "    <subImage name=\"?\">\n";
        xml += "        <region x=\"0\" y=\"0\" width=\"128\" height=\"128\" />\n";
        xml += "    </subImage>\n";
        xml += "    <subImage name=\"a\">\n";
        xml += "        <region x=\"0\" y=\"0\" width=\"128\" height=\"128\" />\n";
        xml += "    </subImage>\n";
        xml += "</imageAtlas>\n";
        File xmlFile = LoadUtils.createTempFile(xml, Charsets.UTF_8);

        BitmapFont bitmapFont = imageAtlasLoader.loadBitmapFont(new ResourceFile(imageFile),
                new ResourceFile(xmlFile));
        Image firstAttempt = bitmapFont.getGlyph('a');
        Image secondAttempt = bitmapFont.getGlyph('a');

        assertNotNull(firstAttempt);
        assertTrue(firstAttempt == secondAttempt);
    }
}
