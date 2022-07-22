//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.Primitive;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.mock.MockSceneContext;
import nl.colorize.multimedialib.scene.DisplayObject;
import nl.colorize.multimedialib.scene.Layer;
import nl.colorize.multimedialib.scene.SceneContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplayObjectTest {

    private SceneContext context;

    @BeforeEach
    public void before() {
        context = new MockSceneContext();
        context.changeScene(new MockScene());
    }

    @Test
    void cannotAttachTwice() {
        Primitive rect = Primitive.of(new Rect(0, 0, 100, 100), ColorRGB.RED);
        DisplayObject object = new DisplayObject().withGraphics(rect);
        object.attachTo(context);
        context.update(1f);

        assertThrows(IllegalStateException.class, () -> object.attachTo(context));
    }

    @Test
    void terminateRemovesFrameHandlerAndGraphics() {
        Primitive rect = Primitive.of(new Rect(0, 0, 100, 100), ColorRGB.RED);
        DisplayObject object = new DisplayObject().withGraphics(rect);
        List<String> frames = new ArrayList<>();
        object.withFrameHandler(deltaTime -> frames.add("1"));
        object.attachTo(context);

        context.update(1f);
        context.update(1f);

        assertEquals(ImmutableList.of("1", "1"), frames);
        assertTrue(context.getStage().contains(Layer.DEFAULT, rect));

        object.terminate();
        context.update(1f);

        assertEquals(ImmutableList.of("1", "1"), frames);
        assertFalse(context.getStage().contains(Layer.DEFAULT, rect));
    }

    @Test
    void terminateHandler() {
        Primitive rect = Primitive.of(new Rect(0, 0, 100, 100), ColorRGB.RED);
        DisplayObject object = new DisplayObject().withGraphics(rect);
        List<String> frames = new ArrayList<>();
        object.withFrameHandler(deltaTime -> frames.add("1"));
        object.withTerminationHandler(() -> frames.add("2"));
        object.attachTo(context);

        context.update(1f);
        context.update(1f);
        object.terminate();
        context.update(1f);

        assertEquals(ImmutableList.of("1", "1", "2"), frames);
    }

    @Test
    void stopAfterDuration() {
        Primitive rect = Primitive.of(new Rect(0, 0, 100, 100), ColorRGB.RED);
        List<String> frames = new ArrayList<>();

        new DisplayObject()
            .withGraphics(rect)
            .withFrameHandler(deltaTime -> frames.add("1"))
            .stopAfter(2f)
            .attachTo(context);

        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(ImmutableList.of("1", "1"), frames);
    }

    @Test
    void stopAfterCondition() {
        Primitive rect = Primitive.of(new Rect(0, 0, 100, 100), ColorRGB.RED);
        List<String> frames = new ArrayList<>();

        new DisplayObject()
            .withGraphics(rect)
            .withFrameHandler(deltaTime -> frames.add("1"))
            .stopIf(() -> frames.size() >= 2)
            .attachTo(context);

        context.update(1f);
        context.update(1f);
        context.update(1f);

        assertEquals(ImmutableList.of("1", "1"), frames);
    }
}