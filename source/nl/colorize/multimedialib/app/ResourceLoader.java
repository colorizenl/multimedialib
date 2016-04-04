//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.app;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import nl.colorize.multimedialib.graphics.AudioData;
import nl.colorize.multimedialib.graphics.BitmapFont;
import nl.colorize.multimedialib.graphics.ImageAtlas;
import nl.colorize.multimedialib.graphics.ImageData;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.xml.XMLHelper;

/**
 * Loads resources such as images, sounds, image atlases, and bitmap fonts from
 * a variery of formats. Unless stated otherwise, all methods in this class will
 * throw a {@code RendererException} when a resource cannot be loaded. 
 */
public class ResourceLoader {

	private Renderer renderer;
	
	public ResourceLoader(Renderer renderer) {
		this.renderer = renderer;
	}
	
	public ImageData loadImage(ResourceFile source) {
		return renderer.loadImage(source);
	}
	
	public AudioData loadAudio(ResourceFile source) {
		return renderer.loadAudio(source);
	}
	
	public ImageAtlas loadImageAtlas(ResourceFile imageSource, ResourceFile xmlSource) {
		ImageAtlas imageAtlas = new ImageAtlas(loadImage(imageSource));
		
		try {
			Document xml = XMLHelper.parse(xmlSource);
			parseImageAtlasXML(xml, imageAtlas);
		} catch (JDOMException e) {
			throw new RendererException("Cannot parse image atlas XML file: " + xmlSource, e);
		}
		
		return imageAtlas;
	}
	
	private void parseImageAtlasXML(Document xml, ImageAtlas imageAtlas) {
		Element imageAtlasElement = xml.getRootElement();
		if (!imageAtlasElement.getName().equals("imageAtlas")) {
			throw new RendererException("XML file does not appear to contain image atlas");
		}
		
		for (Element subImageElement : imageAtlasElement.getChildren("subImage")) {
			String name = subImageElement.getAttributeValue("name");
			Rect region = parseRegionElement(subImageElement.getChild("region"));
			imageAtlas.markSubImage(name, region);
		}
	}

	private Rect parseRegionElement(Element child) {
		int x = Integer.parseInt(child.getAttributeValue("x"));
		int y = Integer.parseInt(child.getAttributeValue("y"));
		int width = Integer.parseInt(child.getAttributeValue("width"));
		int height = Integer.parseInt(child.getAttributeValue("height"));
		return new Rect(x, y, width, height);
	}

	public Document buildImageAtlasXML(ImageAtlas imageAtlas) {
		Element imageAtlasElement = new Element("imageAtlas");
		for (String name : imageAtlas.getSubImageNames()) {
			imageAtlasElement.addContent(buildSubImageElement(imageAtlas, name));
		}
		return new Document(imageAtlasElement);
	}
	
	private Element buildSubImageElement(ImageAtlas imageAtlas, String name) {
		Rect region = imageAtlas.getSubImage(name).getRegion();
		
		Element subImageElement = new Element("subImage");
		subImageElement.setAttribute("name", name);
		subImageElement.addContent(buildBoundsElement("region", region));
		return subImageElement;
	}

	private Element buildBoundsElement(String name, Rect bounds) {
		Element boundsElement = new Element(name);
		boundsElement.setAttribute("x", String.valueOf(bounds.getX()));
		boundsElement.setAttribute("y", String.valueOf(bounds.getY()));
		boundsElement.setAttribute("width", String.valueOf(bounds.getWidth()));
		boundsElement.setAttribute("height", String.valueOf(bounds.getHeight()));
		return boundsElement;
	}
	
	public BitmapFont loadBitmapFont(ResourceFile imageSource, ResourceFile xmlSource) {
		ImageAtlas imageAtlas = loadImageAtlas(imageSource, xmlSource);
		return new BitmapFont(imageAtlas);
	}
	
	public Document buildBitmapFontXML(BitmapFont font) {
		return buildImageAtlasXML(font.getImageAtlas());
	}
}
