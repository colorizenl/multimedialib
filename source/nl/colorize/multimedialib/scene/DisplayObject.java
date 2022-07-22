//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.Graphic2D;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.util.animation.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Combines one or multiple graphics on stage with a system that controls those
 * graphics. This class implements the {@link ActorSystem} interface. Adding the
 * system to the current scene will automatically add all its graphics to the
 * stage. Vice versa, terminating the display object will also remove all of its
 * graphics from the stage.
 * <p>
 * Both the (admittedly not-so-great) name and the purpose of this class are
 * based on the Flash ActionScript class of the same name.
 */
public final class DisplayObject implements ActorSystem {

    private List<Graphic2D> graphics;
    private String layer;
    private List<Updatable> frameHandlers;
    private List<Runnable> clickHandlers;
    private List<Runnable> terminationHandlers;

    private Stage stage;
    private boolean terminated;

    public DisplayObject() {
        this.graphics = new ArrayList<>();
        this.layer = Layer.DEFAULT.getName();
        this.frameHandlers = new ArrayList<>();
        this.clickHandlers = new ArrayList<>();
        this.terminationHandlers = new ArrayList<>();

        this.stage = null;
        this.terminated = false;
    }

    public DisplayObject withGraphics(Graphic2D... graphics) {
        return withGraphics(ImmutableList.copyOf(graphics));
    }

    public DisplayObject withGraphics(List<Graphic2D> graphics) {
        Preconditions.checkState(!isInitialized(), "DisplayObject already initialized");
        Preconditions.checkState(!terminated, "DisplayObject already terminated");
        this.graphics.addAll(graphics);
        return this;
    }

    public DisplayObject withLayer(Layer layer) {
        return withLayer(layer.getName());
    }

    public DisplayObject withLayer(String layerName) {
        Preconditions.checkState(!isInitialized(), "DisplayObject already initialized");
        Preconditions.checkState(!terminated, "DisplayObject already terminated");
        this.layer = layerName;
        return this;
    }

    public DisplayObject withFrameHandler(Updatable handler) {
        Preconditions.checkState(!terminated, "DisplayObject already terminated");
        frameHandlers.add(handler);
        return this;
    }

    public DisplayObject withClickHandler(Runnable handler) {
        Preconditions.checkState(!terminated, "DisplayObject already terminated");
        clickHandlers.add(handler);
        return this;
    }

    public DisplayObject withTerminationHandler(Runnable handler) {
        Preconditions.checkState(!terminated, "DisplayObject already terminated");
        terminationHandlers.add(handler);
        return this;
    }

    /**
     * Adds a frame handler that will update the specified timeline every frame,
     * and then invoke the callback function based on the timeline's new value.
     * If the timeline does not loop, ending the timeline will also terminate
     * the {@code DisplayObject}.
     */
    public DisplayObject withTimelineHandler(Timeline timeline, Consumer<Float> callback) {
        return withFrameHandler(deltaTime -> {
            timeline.movePlayhead(deltaTime);
            callback.accept(timeline.getValue());

            if (timeline.isCompleted() && !timeline.isLoop()) {
                terminate();
            }
        });
    }

    public DisplayObject stopAfter(float duration) {
        Timer timer = new Timer(duration);
        return withFrameHandler(deltaTime -> {
            timer.update(deltaTime);
            if (timer.isCompleted()) {
                terminate();
            }
        });
    }

    public DisplayObject stopIf(Supplier<Boolean> condition) {
        return withFrameHandler(deltaTime -> {
            if (condition.get()) {
                terminate();
            }
        });
    }

    @Override
    public void init(SceneContext context) {
        Preconditions.checkState(!isInitialized(), "DisplayObject already initialized");
        Preconditions.checkState(!terminated, "DisplayObject already terminated");

        stage = context.getStage();

        for (Graphic2D graphic : graphics) {
            Preconditions.checkState(!stage.contains(graphic),
                "Graphic has already been added to stage: " + stage);

            stage.add(layer, graphic);
        }
    }

    private boolean isInitialized() {
        return stage != null;
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        if (!terminated) {
            for (Updatable frameHandler : frameHandlers) {
                frameHandler.update(deltaTime);
            }

            if (!clickHandlers.isEmpty()) {
                updateInput(context.getInputDevice());
            }
        }
    }

    private void updateInput(InputDevice inputDevice) {
        if (inputDevice.isPointerReleased() && hitTest(inputDevice.getPointers())) {
            for (Runnable clickHandler : clickHandlers) {
                clickHandler.run();
            }
        }
    }

    private boolean hitTest(List<Point2D> points) {
        return points.stream()
            .anyMatch(this::hitTest);
    }

    public boolean hitTest(Point2D point) {
        return graphics.stream()
            .anyMatch(graphic -> graphic.hitTest(point));
    }

    /**
     * Adds additional graphics to this {@code DisplayObject}. This method is
     * different from the "normal" way of adding graphics as it can be used
     * when the {@code DisplayObject} is already live.
     */
    public void extendGraphics(Graphic2D graphic) {
        if (!terminated) {
            if (isInitialized()) {
                graphics.add(graphic);
                stage.add(layer, graphic);
            } else {
                withGraphics(graphic);
            }
        }
    }

    public void terminate() {
        if (terminated) {
            return;
        }

        terminated = true;
        terminationHandlers.forEach(Runnable::run);

        if (stage != null) {
            graphics.forEach(graphic -> stage.remove(layer, graphic));
            stage = null;
        }

        // No need to remove/end the system, this will happen
        // automatically at the end of the frame because
        // isCompleted() will return true.
    }

    @Override
    public boolean isCompleted() {
        return terminated;
    }

    /**
     * Attaches this {@code }DisplayObject} to the current scene and adds all of
     * its graphics to the stage. Using this method is equivalent to
     * {@code context.attach(displayObject)}.
     */
    public void attachTo(SceneContext context) {
        Preconditions.checkState(!isInitialized(), "DisplayObject already initialized");
        context.attach(this);
    }
}
