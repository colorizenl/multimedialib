//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainerTest {

    @Test
    void flushAddedAndRemoved() {
        List<String> events = new ArrayList<>();

        Container container = new Container();
        container.getAddedChildren().subscribe(e -> events.add("add-" + e));
        container.getRemovedChildren().subscribe(e -> events.add("remove-" + e));

        Text a = new Text("a", null);
        Text b = new Text("b", null);
        Text c = new Text("c", null);
        Text x = new Text("x", null);

        container.addChild(a);
        container.addChild(b);

        assertEquals("[Text [a], Text [b]]", container.getChildren().toString());
        assertEquals("[add-Text [a], add-Text [b]]", events.toString());

        container.addChild(x);
        container.removeChild(b);
        container.removeChild(c);

        assertEquals("[Text [a], Text [x]]", container.getChildren().toString());
        assertEquals("[add-Text [a], add-Text [b], add-Text [x], remove-Text [b]]", events.toString());
    }
}
