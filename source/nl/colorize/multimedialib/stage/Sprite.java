//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.StateMachine;
import nl.colorize.multimedialib.scene.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

/**
 * Static or animated image that can be displayed on the stage. Multiple
 * sprites can use the same image data. As sprites represent an instance
 * of the original image, sprites can be transformed without affecting
 * or modifying the underlying image data.
 * <p>
 * Sprites support multiple graphical states, where each state can be
 * identified by its name. The currently active graphics are updated
 * automatically for as long as the sprite is on the stage.
 */
public class Sprite implements Spatial2D {

    @Getter @Setter(PROTECTED) private Container parent;
    @Getter private ImageTransform transform;
    @Getter private ImageTransform globalTransform;

    private Map<String, StateGraphics> availableStates;
    private StateMachine<StateGraphics> stateMachine;
    private double lastTick;

    private static final String NULL_STATE = "$$null";
    private static final String DEFAULT_STATE = "$$default";

    /**
     * Creates a sprite without default graphics. Trying to use the sprite
     * before graphics have been added will result in an exception.
     */
    public Sprite() {
        this.transform = new ImageTransform();
        this.globalTransform = new ImageTransform();

        availableStates = new HashMap<>();
        stateMachine = StateMachine.withInitialState(new StateGraphics(NULL_STATE, null));
        lastTick = -1f;
    }

    /**
     * Creates a sprite that uses the specified animation as its default
     * graphics.
     */
    public Sprite(Animation anim) {
        this();
        addGraphics(DEFAULT_STATE, anim);
    }

    /**
     * Creates a sprite that uses the specified image as its default graphics.
     */
    public Sprite(Image image) {
        this();
        addGraphics(DEFAULT_STATE, image);
    }

    /**
     * Adds graphics to this sprite. If the sprite does not contain graphics
     * yet, this will automatically change the sprite's current graphics.
     * Otherwise, changing the sprite's graphics can be done later using the
     * specified name.
     *
     * @throws IllegalArgumentException if a state with the same name has
     *         already been registered with this sprite.
     */
    public void addGraphics(String stateName, Animation graphics) {
        Preconditions.checkNotNull(stateName, "Missing state name");
        Preconditions.checkNotNull(graphics, "Missing state graphics");
        Preconditions.checkArgument(!hasState(stateName), "State already exists: " + stateName);

        StateGraphics state = new StateGraphics(stateName, graphics);
        availableStates.put(stateName, state);

        if (availableStates.size() == 1) {
            changeGraphics(stateName);
        }

        updateCurrentGraphics();
    }

    /**
     * Adds graphics to this sprite. If the sprite does not contain graphics
     * yet, this will automatically change the sprite's current graphics.
     * Otherwise, changing the sprite's graphics can be done later using the
     * specified name.
     *
     * @throws IllegalArgumentException if a state with the same name has
     *         already been registered with this sprite.
     */
    public void addGraphics(String stateName, Image stateGraphics) {
        addGraphics(stateName, new Animation(stateGraphics));
    }

    /**
     * Changes this sprite's graphics to the state identified by the specified
     * name. If the sprite is already in that state, this method does nothing.
     *
     * @throws IllegalArgumentException if the sprite does not define any
     *         graphics for the requested state.
     */
    public void changeGraphics(String stateName) {
        StateGraphics state = availableStates.get(stateName);

        Preconditions.checkNotNull(state, "No graphics defined for state: " + stateName);

        if (!stateMachine.getCurrentState().equals(state)) {
            stateMachine.changeState(state);
            updateCurrentGraphics();
        }
    }

    /**
     * Leaves the sprite in its current state, but resets the graphics for that
     * state to play from the beginning.
     */
    public void resetCurrentGraphics() {
        stateMachine.getCurrentStateTimer().reset();
        updateCurrentGraphics();
    }

    public String getActiveState() {
        return stateMachine.getCurrentState().name;
    }

    public Set<String> getAvailableStates() {
        return availableStates.keySet();
    }

    public boolean hasState(String stateName) {
        return availableStates.containsKey(stateName);
    }

    public Animation getGraphics(String stateName) {
        return availableStates.get(stateName).graphics;
    }

    @Deprecated
    public Animation getCurrentStateGraphics() {
        return stateMachine.getCurrentState().graphics;
    }

    @Deprecated
    public Timer getCurrentStateTimer() {
        StateGraphics currentState = stateMachine.getCurrentState();
        double time = stateMachine.getCurrentStateTimer().getTime();

        if (currentState.graphics.isLoop() || currentState.graphics.getFrameCount() == 1) {
            return Timer.at(time);
        } else {
            return Timer.at(time, currentState.graphics.getDuration());
        }
    }

    public Image getCurrentGraphics() {
        return stateMachine.getCurrentState().current;
    }

    public int getCurrentWidth() {
        return stateMachine.getCurrentState().current.getWidth();
    }

    public int getCurrentHeight() {
        return stateMachine.getCurrentState().current.getHeight();
    }

    @Override
    public void animate(Timer sceneTime) {
        Preconditions.checkState(getCurrentGraphics() != null, "Sprite does not contain graphics");

        double tick = sceneTime.getTime();

        if (lastTick >= 0f) {
            double deltaTime = tick - lastTick;
            Timer stateTimer = stateMachine.getCurrentStateTimer();
            stateTimer.setTime(stateTimer.getTime() + deltaTime);
        }

        updateCurrentGraphics();
        lastTick = tick;
    }

    private void updateCurrentGraphics() {
        StateGraphics state = stateMachine.getCurrentState();
        double time = stateMachine.getCurrentStateTimer().getTime();
        state.current = state.graphics.getFrameAtTime(time);
    }

    @Override
    public Rect getStageBounds() {
        Point2D position = globalTransform.getPosition();
        double width = Math.max(getCurrentWidth() * (globalTransform.getScaleX() / 100f), 1f);
        double height = Math.max(getCurrentHeight() * (globalTransform.getScaleY() / 100f), 1f);
        return new Rect(position.x() - width / 2f, position.y() - height / 2f, width, height);
    }

    /**
     * Creates a new sprite with states and graphics based on this one, but it
     * starts back in its initial state.
     */
    public Sprite copy() {
        Sprite copy = new Sprite();
        for (StateGraphics state : availableStates.values()) {
            copy.addGraphics(state.name, state.graphics);
        }
        copy.changeGraphics(stateMachine.getCurrentState().name);
        copy.getTransform().set(getTransform());
        return copy;
    }

    @Override
    public String toString() {
        return "Sprite [" + stateMachine.getCurrentState().name + "]";
    }

    /**
     * Provides quick access to the name and graphics associated with one
     * of the possible states for this sprite. This avoids having to call
     * {@code Map.get} to obtain the sprite's graphics. It also caches the
     * currently active frame in the animation for similar reasons.
     */
    @RequiredArgsConstructor
    private static class StateGraphics {

        private final String name;
        private final Animation graphics;
        private Image current;
    }
}
