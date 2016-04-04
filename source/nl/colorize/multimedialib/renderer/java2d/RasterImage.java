//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.ImageData;
import nl.colorize.util.swing.Utils2D;

/**
 * Represents image data using Java 2D's {@link java.awt.image.BufferedImage}.
 * Images can be loaded from files using ImageIO, and can also be created
 * programmatically.
 */
public class RasterImage implements ImageData {

	private BufferedImage image;
	
	public RasterImage(BufferedImage image) {
		this.image = image;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public int getWidth() {
		return image.getWidth();
	}
	
	public int getHeight() {
		return image.getHeight();
	}
	
	public RasterImage flip(boolean horizontal, boolean vertical) {
		BufferedImage flippedImage = new BufferedImage(image.getWidth(), image.getHeight(), 
				image.getType());
		Graphics2D g2 = Utils2D.createGraphics(flippedImage, true, true);
		g2.drawImage(image, 
				horizontal ? image.getWidth() : 0, 
				vertical ? image.getHeight() : 0, 
				horizontal ? -image.getWidth() : image.getWidth(), 
				vertical ? -image.getHeight() : image.getHeight(), null);
		g2.dispose();
		return new RasterImage(flippedImage);
	}
	
	public RasterImage applyTint(ColorRGB tint) {
		Color tintColor = new Color(tint.getR(), tint.getG(), tint.getB());
		return new RasterImage(Utils2D.applyTint(image, tintColor));
	}
}
