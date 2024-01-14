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
}
