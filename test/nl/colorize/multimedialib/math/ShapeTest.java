//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ShapeTest {

    private static final List<Class<?>> SHAPE_CLASSES = List.of(
        Point2D.class,
        Point3D.class,
        Line.class,
        SegmentedLine.class,
        Rect.class,
        Circle.class,
        Polygon.class
    );

    @Test
    void allShapesAreValueClass() {
        for (Class<?> shapeClass : SHAPE_CLASSES) {
            try {
                shapeClass.getDeclaredMethod("equals", Object.class);
            } catch (NoSuchMethodException e) {
                fail("Shape should declare explicit equals: " + shapeClass.getName());
            }

            assertTrue(Modifier.isFinal(shapeClass.getModifiers()),
                "Shape should be final: " + shapeClass.getName());
        }
    }
}
