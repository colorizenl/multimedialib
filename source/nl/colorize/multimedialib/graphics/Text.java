//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.util.TextUtils;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Draws text to the screen using the specified TrueType font.
 */
public class Text implements Graphic2D {

    private String text;
    private List<String> lines;
    private TTFont font;
    private Align align;
    private float lineHeight;

    private boolean visible;
    private Point2D position;
    private float alpha;

    public Text(String text, TTFont font, Align align) {
        this.text = text;
        this.lines = TextUtils.LINE_SPLITTER.splitToList(text);
        this.font = font;
        this.align = align;
        this.lineHeight = font.getLineHeight();

        this.visible = true;
        this.position = new Point2D(0f, 0f);
        this.alpha = 100f;
    }

    public Text(String text, TTFont font) {
        this(text, font, Align.LEFT);
    }

    public void setText(String... lines) {
        if (lines.length == 1) {
            this.text = lines[0];
            this.lines = TextUtils.LINE_SPLITTER.splitToList(text);
        } else {
            this.text = TextUtils.LINE_JOINER.join(lines);
            this.lines = ImmutableList.copyOf(lines);
        }
    }

    public String getText() {
        return text;
    }

    public List<String> getLines() {
        return lines;
    }

    public void forLines(BiConsumer<Integer, String> callback) {
        for (int i = 0; i < lines.size(); i++) {
            callback.accept(i, lines.get(i));
        }
    }

    public void setFont(TTFont font) {
        this.font = font;
    }

    public TTFont getFont() {
        return font;
    }

    public void setAlign(Align align) {
        this.align = align;
    }

    public Align getAlign() {
        return align;
    }

    public void setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public Point2D getPosition() {
        return position;
    }

    public void setAlpha(float alpha) {
        this.alpha = MathUtils.clamp(alpha, 0f, 100f);
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public Rect getBounds() {
        float approximateWidth = font.size() * text.length();
        float approximateHeight = lineHeight * lines.size();
        return new Rect(position.getX(), position.getY(), approximateWidth, approximateHeight);
    }

    @Override
    public boolean hitTest(Point2D point) {
        return false;
    }
}
