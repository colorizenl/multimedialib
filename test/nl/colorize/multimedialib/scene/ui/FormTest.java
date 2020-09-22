//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import nl.colorize.multimedialib.graphics.TTFont;
import nl.colorize.multimedialib.mock.MockImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormTest {

    private static final WidgetStyle STYLE = new WidgetStyle(null, (TTFont) null);

    @Test
    public void testAddLabel() {
        Form form = new Form(Location.fixed(100, 100), STYLE, null);
        form.add("Test", new Button(STYLE), null);

        assertEquals(2, form.getWidgets().size());
        assertEquals(TextLabel.class, form.getWidgets().get(0).getClass());
        assertEquals(Button.class, form.getWidgets().get(1).getClass());
    }

    @Test
    public void testLabelsAreOptional() {
        Form form = new Form(Location.fixed(100, 100), STYLE, null);
        form.add(new Button(STYLE), null);

        assertEquals(1, form.getWidgets().size());
        assertEquals(Button.class, form.getWidgets().get(0).getClass());
    }

    @Test
    public void testPositionWidgetsRelative() {
        Form form = new Form(Location.fixed(100, 100), STYLE, null);
        form.add("First", new Button(STYLE), null);
        form.add("Second", new Button(STYLE), null);

        assertEquals(4, form.getWidgets().size());
        assertEquals("LEFT 95, TOP 100", form.getWidgets().get(0).getLocation().toString());
        assertEquals("LEFT 105, TOP 100", form.getWidgets().get(1).getLocation().toString());
        assertEquals("LEFT 95, TOP 120", form.getWidgets().get(2).getLocation().toString());
        assertEquals("LEFT 105, TOP 120", form.getWidgets().get(3).getLocation().toString());
    }

    @Test
    public void testCustomMargins() {
        Form form = new Form(Location.fixed(100, 100), STYLE, null);
        form.setMargin(10, 30);
        form.add("First", new Button(STYLE), null);
        form.add("Second", new Button(STYLE), null);

        assertEquals(4, form.getWidgets().size());
        assertEquals("LEFT 95, TOP 100", form.getWidgets().get(0).getLocation().toString());
        assertEquals("LEFT 105, TOP 100", form.getWidgets().get(1).getLocation().toString());
        assertEquals("LEFT 95, TOP 130", form.getWidgets().get(2).getLocation().toString());
        assertEquals("LEFT 105, TOP 130", form.getWidgets().get(3).getLocation().toString());
    }

    @Test
    public void testDynamicLayout() {
        Form form = new Form(Location.fixed(100, 100), STYLE, null);
        Button first = form.add("First", new Button(STYLE), null);
        Button second = form.add("Second", new Button(STYLE), null);
        Button third = form.add("Third", new Button(STYLE), null);
        form.setVisible(second, false);

        assertEquals(4, form.getWidgets().size());
        assertTrue(form.getWidgets().contains(first));
        assertFalse(form.getWidgets().contains(second));
        assertTrue(form.getWidgets().contains(third));

        assertEquals("LEFT 105, TOP 100", first.getLocation().toString());
        assertEquals("LEFT 105, TOP 120", third.getLocation().toString());
    }

    @Test
    public void testOffsetForWidth() {
        Form form = new Form(Location.fixed(100, 100), STYLE, null);
        form.add("First", new Button(new WidgetStyle(new MockImage(100, 100), (TTFont) null)), null);

        assertEquals(2, form.getWidgets().size());
        assertEquals("LEFT 95, TOP 100", form.getWidgets().get(0).getLocation().toString());
        assertEquals("LEFT 155, TOP 100", form.getWidgets().get(1).getLocation().toString());
    }
}
