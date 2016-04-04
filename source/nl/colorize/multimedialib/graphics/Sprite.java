//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import java.util.HashMap;
import java.util.Map;

import nl.colorize.multimedialib.math.Polygon;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.util.animation.Animatable;

/**
 * Static or animated two-dimensional image that can be integrated into a larger
 * scene. Multiple sprites may use the same image data, the sprite is merely an
 * instance of the image that can be drawn by the renderer and does not modify
 * the image itself. Sprites can be transformed (rotated, scaled) before they are
 * displayed.
 * <p>
 * Frames can have multiple graphical states. These states consist of either a
 * single, static image or an animation containing multiple images. States are
 * identified by name. Sprites are required to have at least one state and 
 * graphical representation.
 */
public class Sprite extends StandardGraphic implements Animatable {

	private Map<String, Animation> graphicsPerState;
	private String activeState;
	private float timeInState;
	private Shape boundingShape;
	
	public Sprite() {
		graphicsPerState = new HashMap<String, Animation>();
		activeState = null;
		timeInState = 0f;
		boundingShape = new Rect(0, 0, 0, 0);
	}
	
	public void addState(String name, Animation graphics) {
		if (graphicsPerState.containsKey(name)) {
			throw new IllegalArgumentException("State already exists: " + name);
		}
		
		graphicsPerState.put(name, graphics);
		
		if (activeState == null) {
			changeState(name);
		}
	}
	
	public void addState(String name, ImageRegion graphics) {
		addState(name, new Animation(graphics));
	}
	
	public void addState(String name, ImageData graphics) {
		addState(name, ImageRegion.from(graphics));
	}
	
	/**
	 * Changes this sprite's currently active state.
	 * @throws IllegalArgumentException if state with that name exists.
	 */
	public void changeState(String name) {
		if (!graphicsPerState.containsKey(name)) {
			throw new IllegalArgumentException("No such state: " + name);
		}
		
		if (!name.equals(activeState)) {
			activeState = name;
			timeInState = 0f;
		}
	}
	
	public String getCurrentState() {
		return activeState;
	}

	/**
	 * Returns the image that corresponds to this sprite's current state's graphical
	 * representation. This image might be part of an animation.
	 */
	public ImageRegion getCurrentGraphics() {
		Animation graphicsForActiveState = graphicsPerState.get(activeState);
		return graphicsForActiveState.getFrameAtTime(timeInState);
	}

	public Shape getBounds() {
		updateBoundingShape();
		return boundingShape;
	}

	private void updateBoundingShape() {
		ImageRegion currentGraphics = getCurrentGraphics();
		int x = getX();
		int y = getY();
		int width = currentGraphics.getRegion().getWidth();
		int height = currentGraphics.getRegion().getHeight();
		
		if (transform.isRotated() || transform.isScaled()) {
			Polygon boundingPolygon = prepareBoundingShape(Polygon.class);
			
			width = Math.round(width * 0.01f * transform.getHorizontalScale());
			height = Math.round(height * 0.01f * transform.getVerticalScale());
			
			int[] points = {
				x - width / 2, y - height / 2,
				x + width / 2, y - height / 2,
				x + width / 2, y + height / 2,
				x - width / 2, y + height / 2
			};
			
			boundingPolygon.setPoints(points);
			
			if (transform.getRotation() != 0) {
				boundingPolygon.rotateDegrees(transform.getRotation(), x, y);
			}
			
			boundingShape = boundingPolygon;
		} else {
			Rect boundingRect = prepareBoundingShape(Rect.class);
			boundingRect.set(x - width / 2, y - height / 2, width, height);
			boundingShape = boundingRect;
		}	
	}

	@SuppressWarnings("unchecked")
	private <T extends Shape> T prepareBoundingShape(Class<T> shapeClass) {
		if (boundingShape != null && boundingShape.getClass() == shapeClass) {
			return (T) boundingShape;
		}
		
		if (shapeClass == Rect.class) {
			return (T) new Rect(0, 0, 0, 0);
		} else if (shapeClass == Polygon.class) {
			int[] initialPoints = {0, 0, 0, 0, 0, 0, 0, 0};
			return (T) new Polygon(initialPoints);
		} else {
			throw new IllegalArgumentException("Unknown bounding shape: " + shapeClass);
		}
	}

	public void onFrame(float deltaTime) {
		timeInState += deltaTime;
	}
}
