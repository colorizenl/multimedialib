//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.mock.MockImage;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        container.getChildren().getAddedElements().subscribe(e -> events.add("add-" + e));
        container.getChildren().getRemovedElements().subscribe(e -> events.add("remove-" + e));

        assertEquals("[Text [a], Text [b]]", container.getChildren().toString());
        assertEquals("[add-Text [a], add-Text [b]]", events.toString());

        container.addChild(x);
        container.removeChild(b);
        container.removeChild(c);

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

        Stage stage = new Stage(GraphicsMode.MODE_2D, new Canvas(800, 600, ScaleStrategy.flexible()));
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
}
