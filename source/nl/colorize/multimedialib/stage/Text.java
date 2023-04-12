//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.util.TextUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Draws text to the screen using the specified TrueType font.
 */
public class Text implements Graphic2D {

    private String text;
    private List<String> lines;
    private OutlineFont font;
    private Align align;
    private float lineHeight;

    private boolean visible;
    private Point2D position;
    private float alpha;

    public Text(String text, OutlineFont font, Align align) {
        this.text = text;
        this.lines = TextUtils.LINE_SPLITTER.splitToList(text);
        this.font = font;
        this.align = align;
        this.lineHeight = 0f;

        this.visible = true;
        this.position = new Point2D(0f, 0f);
        this.alpha = 100f;
    }

    public Text(String text, OutlineFont font) {
        this(text, font, Align.LEFT);
    }

    public void setText(String... lines) {
        if (lines.length == 1) {
            setText(TextUtils.LINE_SPLITTER.splitToList(text));
        } else {
            setText(ImmutableList.copyOf(lines));
        }
    }

    public void setText(List<String> lines) {
        if (lines.isEmpty()) {
            this.text = "";
            this.lines = Collections.emptyList();
        } else if (lines.size() == 1) {
            this.text = lines.get(0);
            this.lines = TextUtils.LINE_SPLITTER.splitToList(text);
        } else {
            this.text = TextUtils.LINE_JOINER.join(lines);
            this.lines = lines;
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

    public void setFont(OutlineFont font) {
        this.font = font;
    }

    public OutlineFont getFont() {
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
        if (lineHeight > 0f) {
            return lineHeight;
        } else {
            return Math.round(font.getStyle().size() * 1.8f);
        }
    }

    @Override
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
        float approximateWidth = font.getStyle().size() * text.length();
        float approximateHeight = lineHeight * lines.size();
        return new Rect(position.getX(), position.getY(), approximateWidth, approximateHeight);
    }

    @Override
    public boolean hitTest(Point2D point) {
        return false;
    }

    @Override
    public String toString() {
        return "Text [" + text.replace("\n", " ") + "]";
    }
}
