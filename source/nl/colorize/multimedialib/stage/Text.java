//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.util.TextUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Draws text to the screen using the specified TrueType font.
 */
@Data
public class Text implements Graphic2D {

    private final StageLocation location;
    private String text;
    private List<String> lines;
    private OutlineFont font;
    private Align align;
    private float lineHeight;

    public Text(String text, OutlineFont font, Align align) {
        this.location = new StageLocation();
        this.text = text;
        this.lines = TextUtils.LINE_SPLITTER.splitToList(text);
        this.font = font;
        this.align = align;
        //TODO the font shouldn't be null, but we used this
        //     in some of the tests.
        this.lineHeight = font == null ? 10f : Math.round(font.getStyle().size() * 1.8f);
    }

    public Text(String text, OutlineFont font) {
        this(text, font, Align.LEFT);
    }

    public void setText(String... lines) {
        setText(ImmutableList.copyOf(lines));
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

    public void forLines(BiConsumer<Integer, String> callback) {
        for (int i = 0; i < lines.size(); i++) {
            callback.accept(i, lines.get(i));
        }
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public Rect getStageBounds() {
        Transform globalTransform = getGlobalTransform();
        Point2D position = globalTransform.getPosition();
        float approximateWidth = font.getStyle().size() * text.length();
        float approximateHeight = lineHeight * lines.size();
        return new Rect(position.getX(), position.getY(), approximateWidth, approximateHeight);
    }

    @Override
    public String toString() {
        String displayText = text.replace("\n", " ");
        if (displayText.length() > 20) {
            displayText = displayText.substring(0, 20) + "...";
        }
        return "Text [" + displayText + "]";
    }
}
