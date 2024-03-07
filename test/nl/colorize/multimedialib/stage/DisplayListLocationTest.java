//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DisplayListLocationTest {

    @Test
    void attachToParent() {
        Container a = new Container("a");
        Container b = new Container("b");
        a.addChild(b);

        assertNull(a.getLocation().getParent());
        assertEquals(List.of(b), a.getLocation().getChildren());
        assertEquals(List.of(b), a.getLocation().getAddedChildren().flush());
        assertEquals(List.of(), a.getLocation().getRemovedChildren().flush());

        assertEquals(a, b.getLocation().getParent());
        assertEquals(List.of(), b.getLocation().getChildren());
        assertEquals(List.of(), b.getLocation().getAddedChildren().flush());
        assertEquals(List.of(), b.getLocation().getRemovedChildren().flush());
    }

    @Test
    void detachChild() {
        Container a = new Container("a");
        Container b = new Container("b");
        a.addChild(b);
        a.removeChild(b);

        assertNull(a.getLocation().getParent());
        assertEquals(List.of(), a.getLocation().getChildren());
        assertEquals(List.of(), a.getLocation().getAddedChildren().flush());
        assertEquals(List.of(b), a.getLocation().getRemovedChildren().flush());

        assertNull(b.getLocation().getParent());
        assertEquals(List.of(), b.getLocation().getChildren());
        assertEquals(List.of(), b.getLocation().getAddedChildren().flush());
        assertEquals(List.of(), b.getLocation().getRemovedChildren().flush());
    }

    @Test
    void detachFromParentContainer() {
        Container a = new Container("a");
        Container b = new Container("b");
        a.addChild(b);
        b.getLocation().detach();

        assertNull(a.getLocation().getParent());
        assertEquals(List.of(), a.getLocation().getChildren());
        assertEquals(List.of(), a.getLocation().getAddedChildren().flush());
        assertEquals(List.of(b), a.getLocation().getRemovedChildren().flush());

        assertNull(b.getLocation().getParent());
        assertEquals(List.of(), b.getLocation().getChildren());
        assertEquals(List.of(), b.getLocation().getAddedChildren().flush());
        assertEquals(List.of(), b.getLocation().getRemovedChildren().flush());
    }

    @Test
    void calculateGlobalTransform() {
        Container a = new Container("a");
        a.getTransform().setPosition(20f, 0f);

        Container b = new Container("b");
        b.getTransform().setPosition(30f, 40f);
        a.addChild(b);

        assertEquals(20f, a.getGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(0f, a.getGlobalTransform().getPosition().y(), EPSILON);
        assertEquals(50f, b.getGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(40f, b.getGlobalTransform().getPosition().y(), EPSILON);

        a.getTransform().setPosition(50f, 60f);

        assertEquals(50f, a.getGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(60f, a.getGlobalTransform().getPosition().y(), EPSILON);
        assertEquals(80f, b.getGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(100f, b.getGlobalTransform().getPosition().y(), EPSILON);

        b.getTransform().setPosition(70f, 80f);

        assertEquals(50f, a.getGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(60f, a.getGlobalTransform().getPosition().y(), EPSILON);
        assertEquals(120f, b.getGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(140f, b.getGlobalTransform().getPosition().y(), EPSILON);
    }
}
