//-----------------------------------------------------------------------------
// Ape Attack
// Copyright 2005, 2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.mock.MockImage;
import org.junit.jupiter.api.Test;

import static nl.colorize.multimedialib.math.Point2D.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupTest {

    @Test
    void bulkMoveGraphics() {
        Sprite first = new Sprite(new MockImage());
        first.setPosition(100f, 200f);

        Sprite second = new Sprite(new MockImage());
        second.setPosition(200f, 300f);

        Group group = new Group();
        group.add(first);
        group.addOffset(second, 50f, 100f);

        assertEquals(150f, second.getPosition().getX(), EPSILON);
        assertEquals(300f, second.getPosition().getY(), EPSILON);
    }
}
