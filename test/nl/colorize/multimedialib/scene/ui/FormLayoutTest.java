//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.stage.FontFace.DEFAULT_FONT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormLayoutTest {

    private static final DeclarativeStyle STYLE = new DeclarativeStyle(DEFAULT_FONT);

    @Test
    void columnLayout() {
        Form form = new Form(null, FormLayout.column(100, 20, 10, 10), null);
        form.addLabel("a", STYLE);
        form.addButton("b", STYLE);
        form.addTitle("c", STYLE);
        form.addButton("d", STYLE);

        assertEquals(100, form.getFormWidth());
        assertEquals(80, form.getFormHeight());
        assertEquals("[[0, 0, 45, 20], [55, 0, 45, 20], [0, 30, 100, 20], [0, 60, 100, 20]]",
            form.getWidgetBounds().toString());
    }

    @Test
    void distributeHorizontalLayout() {
        Form form = new Form(null, FormLayout.distributeHorizontal(100, 50, 10, 2), null);
        form.addButton("a", STYLE);
        form.addButton("b", STYLE);

        assertThrows(IllegalStateException.class, () -> form.addButton("c", STYLE));

        assertEquals(100, form.getFormWidth());
        assertEquals(50, form.getFormHeight());
        assertEquals("[[0, 0, 45, 50], [55, 0, 45, 50]]",
            form.getWidgetBounds().toString());
    }

    @Test
    void horizontalLayout() {
        Form form = new Form(null, FormLayout.horizontal(100, 50, 10), null);
        form.addButton("a", STYLE);
        form.addButton("b", STYLE);
        form.addButton("c", STYLE);

        assertEquals(320, form.getFormWidth());
        assertEquals(50, form.getFormHeight());
        assertEquals("[[0, 0, 100, 50], [110, 0, 100, 50], [220, 0, 100, 50]]",
            form.getWidgetBounds().toString());
    }
}
