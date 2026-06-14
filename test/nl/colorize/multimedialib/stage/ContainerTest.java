//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Circle;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContainerTest {

    @Test
    void getStageBounds() {
        Sprite a = new Sprite(new MockImage(100, 100));
        a.setPosition(10, 20);

        Sprite b = new Sprite(new MockImage(50, 50));
        b.setPosition(70, 20);

        Container container = new Container();
        container.addChild(a);
        container.addChild(b);

        Stage stage = new Stage(new Canvas(800, 600, ScaleStrategy.flexible()));
        stage.getRoot().addChild(container);
        stage.recalculateGlobalTransform(a);
        stage.recalculateGlobalTransform(b);

        assertEquals(Rect.fromPoints(-40, -30, 95, 70), container.getStageBounds());
    }

    @Test
    void attachToParent() {
        Container a = new Container("a");
        Container b = new Container("b");
        a.addChild(b);

        assertEquals("[b [0]]", a.getChildren().toString());
        assertEquals("[]", b.getChildren().toString());
    }

    @Test
    void detachChild() {
        Container a = new Container("a");
        Container b = new Container("b");
        a.addChild(b);
        a.removeChild(b);

        assertEquals("[]", a.getChildren().toString());
        assertEquals("[]", b.getChildren().toString());
    }

    @Test
    void removeParentRelationOnDetach() {
        Container parent = new Container("parent");
        Sprite sprite = new Sprite(new MockImage(100, 100));
        Primitive primitive = new Primitive(new Circle(100), ColorRGB.RED);
        Text text = new Text("test", null);

        parent.addChild(sprite);
        parent.addChild(primitive);
        parent.addChild(text);

        parent.removeChild(sprite);
        parent.removeChild(primitive);
        parent.removeChild(text);

        assertNull(sprite.getParent());
        assertNull(primitive.getParent());
        assertNull(text.getParent());
    }

    @Test
    void reattachChild() {
        Container parent = new Container();
        Container child = parent.addChildContainer();
        Container grandchild = child.addChildContainer();

        child.detach();
        parent.addChild(grandchild);

        assertEquals(1, parent.getChildren().size());
        assertEquals(grandchild, parent.getChildren().getFirst());
        assertEquals(0, child.getChildren().size());
        assertNull(child.getParent());
        assertEquals(parent, grandchild.getParent());
    }
}
