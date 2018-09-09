//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.xml.XMLHelper;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * Reads and writes image atlases to an XML file format. The XML file only
 * contains details related to the sub-images within the image atlas, it
 * does not contain the actual image data from the source image.
 * <p>
 * In addition to "regular" image atlases, this class can also be used to
 * load and save bitmap fonts.
 */
public class ImageAtlasLoader {

    private MediaLoader mediaLoader;

    public ImageAtlasLoader(MediaLoader mediaLoader) {
        this.mediaLoader = mediaLoader;
    }

    public ImageAtlas load(ResourceFile imageSource, ResourceFile xmlSource) {
        ImageAtlas imageAtlas = new ImageAtlas(mediaLoader.loadImage(imageSource));

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

    public Document export(ImageAtlas imageAtlas) {
        Element imageAtlasElement = new Element("imageAtlas");
        for (String name : imageAtlas.getSubImages().keySet()) {
            imageAtlasElement.addContent(buildSubImageElement(imageAtlas, name));
        }
        return new Document(imageAtlasElement);
    }

    private Element buildSubImageElement(ImageAtlas imageAtlas, String name) {
        Rect region = imageAtlas.getSubImageBounds(name);

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
        ImageAtlas imageAtlas = load(imageSource, xmlSource);
        return new BitmapFont(imageAtlas);
    }

    public Document exportBitmapFont(BitmapFont font) {
        return export(font.getImageAtlas());
    }
}
