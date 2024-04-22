//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.util.ReflectionUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static nl.colorize.multimedialib.math.Shape.EPSILON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void flatGlobalTransform() {
        Sprite child = new Sprite(new MockImage());

        child.getTransform().setPosition(10, 20);
        child.getTransform().setPosition(30, 40);

        assertEquals("(30, 40)", child.getGlobalTransform().getPosition().toString());
    }

    @Test
    void pushGlobalTransformToChildren() {
        Container parent = new Container();
        Sprite child = new Sprite(new MockImage());
        parent.addChild(child);

        parent.getTransform().setPosition(10, 20);
        child.getTransform().setPosition(30, 40);
        parent.getTransform().setPosition(50, 60);

        assertEquals("(50, 60)", parent.getGlobalTransform().getPosition().toString());
        assertEquals("(80, 100)", child.getGlobalTransform().getPosition().toString());
    }

    @Test
    void recalculateGlobalTransformFromParent() {
        Container parent = new Container();
        Sprite child = new Sprite(new MockImage());
        parent.addChild(child);

        parent.getTransform().setPosition(10, 20);
        child.getTransform().setPosition(30, 40);
        child.getTransform().setPosition(50, 60);

        assertEquals("(10, 20)", parent.getGlobalTransform().getPosition().toString());
        assertEquals("(60, 80)", child.getGlobalTransform().getPosition().toString());
    }

    @Test
    void localTransformImplementsAllSetters() throws Exception {
        String localTransformClassName = DisplayListLocation.class.getName() + "$LocalTransform";
        Class<?> localTransformClass = Class.forName(localTransformClassName);
        Map<String, Class<?>> transformProperties = ReflectionUtils.getPropertyTypes(Transform.class);

        for (String property : transformProperties.keySet()) {
            String setterName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
            Class<?> setterArg = transformProperties.get(property);
            Method setter = localTransformClass.getDeclaredMethod(setterName, setterArg);

            assertTrue(Modifier.isPublic(setter.getModifiers()));
        }
    }
}
