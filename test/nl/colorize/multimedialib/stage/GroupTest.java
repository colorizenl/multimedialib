//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.mock.MockImage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.math.Point2D.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupTest {

    @Test
    void bulkMoveGraphics() {
        Sprite first = new Sprite(new MockImage());
        first.setPosition(100f, 200f);

        Sprite second = new Sprite(new MockImage());
        second.setPosition(200f, 300f);

        Group group = new Group(first);
        group.addOffset(second, 50f, 100f);

        assertEquals(150f, second.getPosition().getX(), EPSILON);
        assertEquals(300f, second.getPosition().getY(), EPSILON);
    }

    @Test
    void getGroupBounds() {
        Sprite first = new Sprite(new MockImage(100, 100));
        first.setPosition(100f, 200f);

        Sprite second = new Sprite(new MockImage(100, 100));
        second.setPosition(200f, 300f);

        Group group = new Group(first, second);

        assertEquals("50, 150, 200, 200", group.getBounds().toString());
    }

    @Test
    void forEach() {
        Sprite first = new Sprite(new MockImage(100, 100));
        Text second = new Text("1234", null);
        Text third = new Text("5678", null);

        Group group = new Group(first, second, third);
        List<Graphic2D> buffer = new ArrayList<>();
        group.forEach(Text.class, buffer::add);

        assertEquals(List.of(second, third), buffer);
    }
}
