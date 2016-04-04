//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.util.animation.Animatable;

/**
 * Graphic type that displays a text. Line breaks in the text are preserved, 
 * allowing for multi-line texts. In addition to this the text can also be 
 * word-wrapped automatically around a set line width.
 */
public class Text extends StandardGraphic implements Animatable {

	private CharSequence text;
	private BitmapFont font;
	private Align alignment;
	private int wordWrapWidth;
	
	// Animation
	private float animationTimer;
	private float animationSpeed;

	// Cached fields
	private List<String> lines;
	private int[] lineWidths;
	private int widestLine;
	
	private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n').trimResults();
	
	public enum Align {
		LEFT,
		RIGHT,
		CENTER
	}
	
	/**
	 * Creates a {@code BitmapText} from the specified text and font.
	 * @param x Left anchor point.
	 * @param y Coordinate of the first line's baseline.
	 */
	public Text(CharSequence text, BitmapFont font, int x, int y) {
		this.text = text;
		this.font = font;
		this.position.set(x, y);
		this.alignment = Align.LEFT;
		this.wordWrapWidth = 0;
		
		animationTimer = 0f;
		animationSpeed = 0f;
		
		setTextInitial(text);
	}
	
	private void setTextInitial(CharSequence text) {
		this.text = text;
		calculateDefaultTextLayout();
		calculateLineWidths();
	}
	
	/**
	 * Changes the text that is displayed to the specified value. Note that this
	 * will recalculate the text layout.
	 */
	public void setText(CharSequence text) {
		this.text = text;
		
		if (isWordWrapEnabled()) {
			calculateWordWrappedTextLayout();			
		} else {
			calculateDefaultTextLayout();
		}
		calculateLineWidths();
	}
	
	private void calculateDefaultTextLayout() {
		lines = NEWLINE_SPLITTER.splitToList(text);
	}

	private void calculateWordWrappedTextLayout() {
		lines = new ArrayList<String>();
		StringBuilder lineBuffer = new StringBuilder(wordWrapWidth);
			
		for (String line : NEWLINE_SPLITTER.split(text)) {
			if (line.length() > 0) {
				for (int i = 0; i < line.length(); i++) {
					char c = line.charAt(i);
					if (c == ' ') {
						if (estimateWidth(lineBuffer) >= wordWrapWidth) {
							lines.add(lineBuffer.toString());
							lineBuffer.delete(0, lineBuffer.length());
						} else {
							lineBuffer.append(c);
						}
					} else {
						lineBuffer.append(c);
					}
				}
	
				lines.add(lineBuffer.toString());
				lineBuffer.delete(0, lineBuffer.length());
			} else {
				lines.add("");
			}
		}
	}

	private void calculateLineWidths() {
		lineWidths = new int[lines.size()];
		widestLine = 0;
		
		for (int i = 0; i < lines.size(); i++) {
			lineWidths[i] = estimateWidth(lines.get(i));
			widestLine = Math.max(lineWidths[i], widestLine);
		}
	}
	
	/**
	 * Returns the width (in pixels) this would take if it were to be drawn. The 
	 * returned value is an estimate, as the renderer might anti-alias, which will 
	 * have a slight influence on the actual width.
	 */
	private int estimateWidth(CharSequence text) {
		int width = 0;
		for (int i = 0; i < text.length(); i++) {
			width += font.getGlyphBounds(text.charAt(i)).getWidth() + font.getLetterSpacing();
		}
		return width;
	}

	public CharSequence getText() {
		return text;
	}
	
	public List<String> getLines() {
		if (isAnimated()) {
			return getAnimatedTextState();
		} else {
			return lines;
		}
	}
	
	private List<String> getAnimatedTextState() {
		int remainingCharacters = Math.round(animationTimer * animationSpeed);
		List<String> result = new ArrayList<String>(lines.size());
		for (String line : lines) {
			if (line.length() <= remainingCharacters) {
				result.add(line);
			} else if (remainingCharacters > 0) {
				result.add(line.substring(0, remainingCharacters));
			}
			remainingCharacters -= line.length();
		}
		return result;
	}

	public BitmapFont getFont() {
		return font;
	}
	
	public void setAlignment(Align alignment) {
		this.alignment = alignment;
	}

	public Align getAlignment() {
		return alignment;
	}

	public void setWordWrapWidth(int wordWrapWidth) {
		this.wordWrapWidth = wordWrapWidth;
		// Recalculate the text layout
		setText(text);
	}
	
	public int getWordWrapWidth() {
		return wordWrapWidth;
	}
	
	public boolean isWordWrapEnabled() {
		return wordWrapWidth > 0;
	}
	
	/**
	 * Returns the anchor point of the line with the specified index. The location
	 * of this anchor point depends on the base x-coordinate, the width of the
	 * line, and the alignment of the text. 
	 */
	public int getLineAnchorX(int lineIndex) {
		switch (alignment) {
			case LEFT : return getX();
			case RIGHT : return getX() - lineWidths[lineIndex];
			case CENTER : return getX() - lineWidths[lineIndex] / 2;
			default : throw new AssertionError();
		}
	}
	
	public int getWidth() {
		return isWordWrapEnabled() ? wordWrapWidth : widestLine;
	}
	
	public int getHeight() {
		if (lines.size() == 1) {
			return font.getGlyphBounds(' ').getHeight();
		}
		return font.getLineHeight() * lines.size();
	}
	
	public Shape getBounds() {
		//TODO
		return new Rect(-5000, -5000, 10000, 10000);
	}
	
	public void enableAnimation(float charactersPerSecond) {
		this.animationTimer = 0f;
		this.animationSpeed = charactersPerSecond;
	}
	
	public boolean isAnimated() {
		return animationSpeed > 0f;
	}

	public void onFrame(float deltaTime) {
		if (isAnimated()) {
			animationTimer += deltaTime;
		}
	}
}
