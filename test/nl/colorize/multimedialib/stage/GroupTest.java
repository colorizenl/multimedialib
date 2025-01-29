//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.mock.MockMesh;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupTest {

    @Test
    void removeChildWhileIterating() {
        Mesh a = new MockMesh();
        Mesh b = new MockMesh();

        Group group = new Group();
        group.addChild(a);
        group.addChild(b);

        for (StageNode3D child : group) {
            if (child.equals(a)) {
                group.removeChild(b);
            }
        }

        assertEquals(1, group.getChildren().size());
        assertEquals(a, group.getChildren().iterator().next());
    }
}
