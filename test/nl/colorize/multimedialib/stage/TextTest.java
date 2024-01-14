//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextTest {

    private static final FontFace FONT = HeadlessRenderer.DEFAULT_FONT;

    @Test
    void multilineText() {
        Text text = new Text("", FONT);
        text.setText("Single line");

        assertEquals(List.of("Single line"), text.getLines());

        text.setText(List.of("Line", "other line"));

        assertEquals(List.of("Line", "other line"), text.getLines());

        text.setText("Line\nother line");

        assertEquals(List.of("Line", "other line"), text.getLines());

        text.setText(List.of("Line", "other\nline"));

        assertEquals(List.of("Line", "other", "line"), text.getLines());
    }

    @Test
    void wordWrapTextBasedOnHeuristics() {
        Text text = new Text("", FONT);
        text.setLineWidth(400);
        text.setText("This text is very long and should be word-wrapped over multiple lines");

        List<String> expected = List.of(
            "This text is very long and should be word-wrapped over multiple",
            "lines"
        );

        assertEquals(expected, text.getLines());
    }

    @Test
    void preserveOriginalLineBreaksWhileWordWrapping() {
        Text text = new Text("", FONT);
        text.setLineWidth(400);
        text.setText(List.of(
            "This text is very long and should be word-wrapped over multiple lines",
            "while preserving original line breaks"
        ));

        List<String> expected = List.of(
            "This text is very long and should be word-wrapped over multiple",
            "lines",
            "while preserving original line breaks"
        );

        assertEquals(expected, text.getLines());
    }

    @Test
    void ridiculouslyLongWordCannotBeWordWrapped() {
        Text text = new Text("", FONT);
        text.setLineWidth(150);
        text.setText("ThisWordIsTooLongAndCannotBeWordWrapped but these ones can");

        List<String> expected = List.of(
            "ThisWordIsTooLongAndCannotBeWordWrapped",
            "but these ones can"
        );

        assertEquals(expected, text.getLines());
    }

    @Test
    void preserveDoubleNewLine() {
        Text text = new Text("", FONT);
        text.setText("first\n\nsecond\nthird");

        assertEquals(List.of("first", "", "second", "third"), text.getLines());
    }

    @Test
    void preserveDoubleNewLineDuringWordWrap() {
        Text text = new Text("", FONT);
        text.setLineWidth(200);
        text.setText("first\n\nsecond\nthird");

        assertEquals(List.of("first", "", "second", "third"), text.getLines());
    }
}
