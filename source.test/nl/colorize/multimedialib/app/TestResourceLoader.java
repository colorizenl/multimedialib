//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;

import nl.colorize.multimedialib.graphics.AudioData;
import nl.colorize.multimedialib.graphics.BitmapFont;
import nl.colorize.multimedialib.graphics.ImageAtlas;
import nl.colorize.multimedialib.graphics.ImageData;
import nl.colorize.multimedialib.graphics.ImageRegion;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.mock.MockImageData;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.util.LoadUtils;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.swing.Utils2D;
import nl.colorize.util.xml.XMLHelper;

import org.jdom2.Document;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@code ResourceLoader} class. All tests that depend on a
 * renderer are performed using a {@code Java2DRenderer}.
 */
public class TestResourceLoader {

	private Renderer renderer;
	private ResourceLoader resourceLoader;
	
	@Before
	public void before() {
		renderer = new Java2DRenderer(ScaleStrategy.flexible(640, 480), 25);
		resourceLoader = new ResourceLoader(renderer);
	}
	
	@Test
	public void testLoadImage() {
		ImageData image = renderer.loadImage(new ResourceFile("mario.png"));
		assertEquals(256, image.getWidth());
		assertEquals(256, image.getHeight());
	}
	
	@Test
	public void testLoadAudio() throws IOException {
		AudioData audio = renderer.loadAudio(new ResourceFile("test.mp3"));
		assertNotNull(audio.openStream());
	}
	
	@Test
	public void testLoadImageAtlasXML() {
		ImageAtlas imageAtlas = resourceLoader.loadImageAtlas(new ResourceFile("mario.png"), 
				new ResourceFile("mario.atlas.xml"));
		
		assertEquals(256, imageAtlas.getAtlas().getWidth());
		assertEquals(256, imageAtlas.getAtlas().getHeight());
		
		assertEquals(20, imageAtlas.getSubImageNames().size());
		assertEquals(new Rect(0, 0, 48, 64), imageAtlas.getSubImage("north_0").getRegion());
		assertEquals(new Rect(96, 0, 48, 64), imageAtlas.getSubImage("north_2").getRegion());
		assertEquals(new Rect(0, 192, 48, 64), imageAtlas.getSubImage("west_0").getRegion());
		assertEquals(new Rect(192, 192, 48, 64), imageAtlas.getSubImage("west_4").getRegion());
	}
	
	@Test
	public void testBuildImageAtlasXML() {
		ImageAtlas atlas = new ImageAtlas(MockImageData.create(256, 256));
		atlas.markSubImage("a", new Rect(0, 0, 128, 128));
		atlas.markSubImage("b", new Rect(142, 14, 100, 100));
		
		Document xmlDocument = resourceLoader.buildImageAtlasXML(atlas);
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
		
		BitmapFont bitmapFont = resourceLoader.loadBitmapFont(new ResourceFile(imageFile), 
				new ResourceFile(xmlFile));
		ImageRegion firstAttempt = bitmapFont.getGlyph('a');
		ImageRegion secondAttempt = bitmapFont.getGlyph('a');
		
		assertNotNull(firstAttempt);
		assertTrue(firstAttempt == secondAttempt);
	}
}
