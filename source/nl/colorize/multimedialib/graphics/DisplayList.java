//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.RendererException;

/**
 * Contains all graphics that should be drawn by the renderer at the end of the
 * frame update. Graphics are stored ordered by z-index, the order in which they
 * will be drawn.
 */
public class DisplayList implements Iterable<Graphic> {
	
	private SortedMap<Integer, List<Graphic>> graphicsPerZIndex;
	private Rect canvas;
	
	public DisplayList() {
		graphicsPerZIndex = new TreeMap<Integer, List<Graphic>>();
		canvas = new Rect(0, 0, 0, 0);
	}
	
	public void add(Graphic element) {
		List<Graphic> atZIndex = graphicsPerZIndex.get(element.getZIndex());
		if (atZIndex == null) {
			atZIndex = new ArrayList<Graphic>();
			graphicsPerZIndex.put(element.getZIndex(), atZIndex);
		}
		atZIndex.add(element);
	}
	
	public void addAll(Collection<Graphic> elements) {
		for (Graphic element : elements) {
			add(element);
		}
	}
	
	public void addAll(Graphic... elements) {
		for (Graphic element : elements) {
			add(element);
		}
	}
	
	public void remove(Graphic element) {
		for (List<Graphic> graphics : graphicsPerZIndex.values()) {
			graphics.remove(element);
		}
	}
	
	public void clear() {
		graphicsPerZIndex.clear();
	}
	
	public Iterator<Graphic> iterator() {
		return new DisplayListIterator(graphicsPerZIndex);
	}
	
	public int size() {
		int size = 0;
		for (List<Graphic> graphics : graphicsPerZIndex.values()) {
			size += graphics.size();
		}
		return size;
	}
	
	public List<Graphic> toList() {
		return ImmutableList.copyOf(iterator());
	}
	
	/**
	 * Draws all graphics in this display list using the specified renderer.
	 * Graphics will be drawn in order of z-index. Graphics might not be drawn
	 * if they exist entirely outside of the canvas, if they are fully transparent,
	 * or if they are overlapped by other graphics. In these cases no part of the 
	 * graphic will be visible and there is therefore no point in drawing it.
	 * @throws RendererException if the renderer is unable to draw one of the
	 *         graphics included in the display list.
	 */
	public void draw(Renderer renderer) {
		canvas.set(0, 0, renderer.getCanvasWidth(), renderer.getCanvasHeight());
		
		for (Graphic graphic : this) {
			if (graphic.getBounds().intersects(canvas)) {
				drawGraphic(graphic, renderer);
			}
		}
	}

	private void drawGraphic(Graphic graphic, Renderer renderer) {
		if (graphic instanceof Sprite) {
			renderer.drawSprite((Sprite) graphic);
		} else if (graphic instanceof Shape2D) {
			renderer.drawShape((Shape2D) graphic);
		} else if (graphic instanceof Text) {
			renderer.drawText((Text) graphic);
		} else {
			// This intentionally includes ImageData. The renderer does have
			// drawImage(...), but the display list doesn't know the coordinates
			// where to draw it.
			throw new RendererException("Graphic not supported by renderer: " + 
					graphic.getClass().getName());
		}
	}
	
	/**
	 * Iterates over graphics based on their z-index, or based on the insertion
	 * order if the z-index is the same. 
	 */
	private static class DisplayListIterator extends AbstractIterator<Graphic> {
		
		private Iterator<List<Graphic>> remainingGraphics;
		private Iterator<Graphic> currentGraphics;
		
		public DisplayListIterator(SortedMap<Integer, List<Graphic>> graphicsPerZIndex) {
			remainingGraphics = graphicsPerZIndex.values().iterator();
		}

		@Override
		protected Graphic computeNext() {
			if (currentGraphics == null || !currentGraphics.hasNext()) {
				if (remainingGraphics.hasNext()) {
					currentGraphics = remainingGraphics.next().iterator();
				} else {
					return endOfData();
				}
			}
			return currentGraphics.next();
		}
	}
}
