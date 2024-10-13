//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.mock.MockImage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContainerTest {

    @Test
    void flushAddedAndRemoved() {
        List<String> events = new ArrayList<>();

        Text a = new Text("a", null);
        Text b = new Text("b", null);
        Text c = new Text("c", null);
        Text x = new Text("x", null);

        Container container = new Container();
        container.addChild(a);
        container.addChild(b);

        container.getAddedChildren().flush().forEach(e -> events.add("add-" + e));
        container.getRemovedChildren().flush().forEach(e -> events.add("remove-" + e));

        assertEquals("[Text [a], Text [b]]", container.getChildren().toString());
        assertEquals("[add-Text [a], add-Text [b]]", events.toString());

        container.addChild(x);
        container.removeChild(b);
        container.removeChild(c);

        container.getAddedChildren().flush().forEach(e -> events.add("add-" + e));
        container.getRemovedChildren().flush().forEach(e -> events.add("remove-" + e));

        assertEquals("[Text [a], Text [x]]", container.getChildren().toString());
        assertEquals("[add-Text [a], add-Text [b], add-Text [x], remove-Text [b]]", events.toString());
    }

    @Test
    void getStageBounds() {
        Sprite a = new Sprite(new MockImage(100, 100));
        a.setPosition(10, 20);

        Sprite b = new Sprite(new MockImage(50, 50));
        b.setPosition(70, 20);

        Container container = new Container();
        container.addChild(a);
        container.addChild(b);

        assertEquals(Rect.fromPoints(-40, -30, 95, 70), container.getStageBounds());
    }

    @Test
    void attachToParent() {
        Container a = new Container("a");
        Container b = new Container("b");
        a.addChild(b);

        assertNull(a.getParent());
        assertEquals(List.of(b), a.getChildren());
        assertEquals(List.of(b), a.getAddedChildren().flush());
        assertEquals(List.of(), a.getRemovedChildren().flush());

        assertEquals(a, b.getParent());
        assertEquals(List.of(), b.getChildren());
        assertEquals(List.of(), b.getAddedChildren().flush());
        assertEquals(List.of(), b.getRemovedChildren().flush());
    }

    @Test
    void detachChild() {
        Container a = new Container("a");
        Container b = new Container("b");
        a.addChild(b);
        a.removeChild(b);

        assertNull(a.getParent());
        assertEquals(List.of(), a.getChildren());
        assertEquals(List.of(), a.getAddedChildren().flush());
        assertEquals(List.of(b), a.getRemovedChildren().flush());

        assertNull(b.getParent());
        assertEquals(List.of(), b.getChildren());
        assertEquals(List.of(), b.getAddedChildren().flush());
        assertEquals(List.of(), b.getRemovedChildren().flush());
    }

    @Test
    void detachFromParentContainer() {
        Container a = new Container("a");
        Container b = new Container("b");
        a.addChild(b);
        b.detach();

        assertNull(a.getParent());
        assertEquals(List.of(), a.getChildren());
        assertEquals(List.of(), a.getAddedChildren().flush());
        assertEquals(List.of(b), a.getRemovedChildren().flush());

        assertNull(b.getParent());
        assertEquals(List.of(), b.getChildren());
        assertEquals(List.of(), b.getAddedChildren().flush());
        assertEquals(List.of(), b.getRemovedChildren().flush());
    }


    @Test
    void calculateGlobalTransform() {
        Container a = new Container("a");
        a.getTransform().setPosition(20f, 0f);

        Container b = new Container("b");
        b.getTransform().setPosition(30f, 40f);
        a.addChild(b);

        assertEquals(20f, a.calculateGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(0f, a.calculateGlobalTransform().getPosition().y(), EPSILON);
        assertEquals(50f, b.calculateGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(40f, b.calculateGlobalTransform().getPosition().y(), EPSILON);

        a.getTransform().setPosition(50f, 60f);

        assertEquals(50f, a.calculateGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(60f, a.calculateGlobalTransform().getPosition().y(), EPSILON);
        assertEquals(80f, b.calculateGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(100f, b.calculateGlobalTransform().getPosition().y(), EPSILON);

        b.getTransform().setPosition(70f, 80f);

        assertEquals(50f, a.calculateGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(60f, a.calculateGlobalTransform().getPosition().y(), EPSILON);
        assertEquals(120f, b.calculateGlobalTransform().getPosition().x(), EPSILON);
        assertEquals(140f, b.calculateGlobalTransform().getPosition().y(), EPSILON);
    }

    @Test
    void flatGlobalTransform() {
        Sprite child = new Sprite(new MockImage());

        child.getTransform().setPosition(10, 20);
        child.getTransform().setPosition(30, 40);

        assertEquals("(30, 40)", child.calculateGlobalTransform().getPosition().toString());
    }

    @Test
    void pushGlobalTransformToChildren() {
        Container parent = new Container();
        Sprite child = new Sprite(new MockImage());
        parent.addChild(child);

        parent.getTransform().setPosition(10, 20);
        child.getTransform().setPosition(30, 40);
        parent.getTransform().setPosition(50, 60);

        assertEquals("(50, 60)", parent.calculateGlobalTransform().getPosition().toString());
        assertEquals("(80, 100)", child.calculateGlobalTransform().getPosition().toString());
    }

    @Test
    void recalculateGlobalTransformFromParent() {
        Container parent = new Container();
        Sprite child = new Sprite(new MockImage());
        parent.addChild(child);

        parent.getTransform().setPosition(10, 20);
        child.getTransform().setPosition(30, 40);
        child.getTransform().setPosition(50, 60);

        assertEquals("(10, 20)", parent.calculateGlobalTransform().getPosition().toString());
        assertEquals("(60, 80)", child.calculateGlobalTransform().getPosition().toString());
    }

}
