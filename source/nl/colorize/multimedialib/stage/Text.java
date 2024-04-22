//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Draws text to the screen using the specified TrueType font. Multiline text
 * is supported, and any newline characters in the text will be preserved when
 * the text is rendered. By default, the text will occupy whatever horizontal
 * space it needs, but word-wrapping can optionally be enabled by specifying
 * an explicit line width.
 */
@Getter
@Setter
public class Text implements Graphic2D {

    private final DisplayListLocation location;
    private List<String> lines;
    private FontFace font;
    private Align align;
    private int lineWidth;
    private float lineHeight;

    private static final Splitter LINE_BOUNDARY = Splitter.on('\n');
    private static final Splitter WORD_BOUNDARY = Splitter.on(' ');

    //TODO both of these are reasonably accurate for most fonts,
    //     but of course it would be much better if the renderer
    //     can provide the actual width and height of the text
    //     without needing to rendering it.
    private static final float ESTIMATED_CHAR_WIDTH_FACTOR = 0.6f;
    private static final float ESTIMATED_LINE_HEIGHT_FACTOR = 1.8f;

    public Text(String text, FontFace font, Align align, int lineWidth) {
        this.location = new DisplayListLocation(this);
        this.lines = Collections.emptyList();
        this.font = font;
        this.align = align;
        this.lineWidth = lineWidth;
        //TODO the font shouldn't be null, but we used this in some of the tests.
        this.lineHeight = font == null ? 10f : font.style().size() * ESTIMATED_LINE_HEIGHT_FACTOR;

        setText(text);
    }

    public Text(String text, FontFace font, Align align) {
        this(text, font, align, 0);
    }

    public Text(String text, FontFace font) {
        this(text, font, Align.LEFT);
    }

    public void setText(String text) {
        setText(List.of(text));
    }

    public void setText(List<String> text) {
        if (text.isEmpty()) {
            lines = Collections.emptyList();
        } else {
            lines = text.stream()
                .flatMap(line -> LINE_BOUNDARY.splitToStream(line))
                .flatMap(line -> processLine(line))
                .toList();
        }
    }

    /**
     * Breaks a line from the input text into the lines that should be
     * displayed. How this will be done depends on whether an explicit
     * line width has been defined.
     */
    private Stream<String> processLine(String line) {
        if (lineWidth <= 0 || line.isEmpty()) {
            return Stream.of(line);
        }

        List<String> result = new ArrayList<>();
        StringBuilder currentLineBuffer = new StringBuilder();
        int estimatedCharactersPerLine = (int) Math.floor(lineWidth / estimateCharWidth());

        for (String word : WORD_BOUNDARY.split(line)) {
            if (!currentLineBuffer.isEmpty()) {
                if (currentLineBuffer.length() + word.length() + 1 > estimatedCharactersPerLine) {
                    result.add(currentLineBuffer.toString());
                    currentLineBuffer.setLength(0);
                } else {
                    currentLineBuffer.append(" ");
                }
            }

            currentLineBuffer.append(word);
        }

        if (!currentLineBuffer.isEmpty()) {
            result.add(currentLineBuffer.toString());
        }

        return result.stream();
    }

    public void forLines(BiConsumer<Integer, String> callback) {
        for (int i = 0; i < lines.size(); i++) {
            callback.accept(i, lines.get(i));
        }
    }

    @Override
    public Rect getStageBounds() {
        Transform globalTransform = getGlobalTransform();
        Point2D position = globalTransform.getPosition();

        int longestLine = lines.stream()
            .mapToInt(String::length)
            .max()
            .orElse(1);

        float approximateWidth = longestLine * estimateCharWidth();
        float approximateHeight = font.style().size() * lines.size();
        return Rect.around(position, approximateWidth, approximateHeight);
    }

    private float estimateCharWidth() {
        return font.style().size() * ESTIMATED_CHAR_WIDTH_FACTOR;
    }

    @Override
    public void updateGraphics(Timer sceneTime) {
    }

    @Override
    public String toString() {
        String preview = "";
        if (!lines.isEmpty()) {
            preview = lines.getFirst();
        }
        if (preview.length() > 20) {
            preview = preview.substring(0, 20) + "...";
        }
        return "Text [" + preview + "]";
    }
}
