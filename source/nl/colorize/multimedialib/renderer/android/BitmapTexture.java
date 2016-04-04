//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.android;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.ImageData;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.renderer.RendererException;

/**
 * OpenGL ES texture that is created from a bitmap image. The texture's width
 * and height are required to be a power of two, to ensure they are supported
 * by all GPUs. Note that texture <i>regions</i> do not have this restriction.
 */
public class BitmapTexture implements ImageData {
	
	private Bitmap image;
	private int textureId;
	
	private static final int MAX_TEXTURE_SIZE = 2048;
	
	public BitmapTexture(Bitmap image) {
		if (!MathUtils.isPowerOfTwo(image.getWidth()) || !MathUtils.isPowerOfTwo(image.getHeight())) {
			throw new RendererException("Texture must have power-of-two dimensions: " + 
					image.getWidth() + "x" + image.getHeight());
		}
		
		if (image.getWidth() > MAX_TEXTURE_SIZE || image.getHeight() > MAX_TEXTURE_SIZE) {
			throw new RendererException("Texture dimensions exceed maximum: " + 
					image.getWidth() + "x" + image.getHeight());
		}
		
		this.image = image;
	}
	
	public Bitmap getImage() {
		return image;
	}
	
	public int getWidth() {
		return image.getWidth();
	}
	
	public int getHeight() {
		return image.getHeight();
	}
	
	public ImageData flip(boolean horizontal, boolean vertical) {
		Matrix m = new Matrix();
		m.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
		Bitmap flipped = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, false);
		return new BitmapTexture(flipped);
	}
	
	public ImageData applyTint(ColorRGB tint) {
		//TODO
		throw new UnsupportedOperationException();
	}
	
	public void bind(int textureId) {
		if (this.textureId > 0) {
			throw new IllegalStateException("Texture is already bound");
		}
		this.textureId = textureId;
	}
	
	public int getTextureId() {
		return textureId;
	}
	
	/**
	 * Returns the OpenGL ES texture coordinate S for an x-coordinate somewhere
	 * within this image.
	 */
	public float getS(int x) {
		return (float) x / (float) getWidth();
	}

	/**
	 * Returns the OpenGL ES texture coordinate T for an y-coordinate somewhere
	 * within this image.
	 */
	public float getT(int y) {
		return 1f - ((float) (getHeight() - y) / (float) getHeight());
	}
}
