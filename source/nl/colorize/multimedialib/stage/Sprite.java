//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.StateMachine;
import nl.colorize.multimedialib.scene.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
public class Sprite implements Graphic2D {

    @Getter private final DisplayListLocation location;
    private Map<String, SpriteState> stateGraphics;
    private StateMachine<SpriteState> stateMachine;
    // Cached for performance reasons.
    private Image currentGraphics;

    private static final String NULL_STATE = "$$null";
    private static final String DEFAULT_STATE = "$$default";

    public Sprite() {
        this.location = new DisplayListLocation(this);
        this.stateGraphics = new HashMap<>();
        this.stateMachine = new StateMachine<>(new SpriteState(NULL_STATE, null));
    }

    public Sprite(Animation anim) {
        this();
        addGraphics(DEFAULT_STATE, anim);
    }

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
        Preconditions.checkArgument(!hasGraphics(stateName), "State already exists: " + stateName);

        SpriteState state = new SpriteState(stateName, graphics);
        stateGraphics.put(stateName, state);

        if (stateGraphics.size() == 1) {
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
        SpriteState state = stateGraphics.get(stateName);

        Preconditions.checkNotNull(state, "No graphics defined for " + stateName);

        if (!stateMachine.getActiveState().equals(state)) {
            stateMachine.forceState(state);
            updateCurrentGraphics();
        }
    }

    /**
     * Leaves the sprite in its current state, but resets the graphics for that
     * state to play from the beginning.
     */
    public void resetCurrentGraphics() {
        stateMachine.getActiveStateTimer().reset();
        updateCurrentGraphics();
    }

    public String getActiveState() {
        return stateMachine.getActiveState().name;
    }

    public Set<String> getPossibleStates() {
        return stateGraphics.keySet();
    }

    public boolean hasGraphics(String stateName) {
        return stateGraphics.containsKey(stateName);
    }

    public Animation getGraphics(String stateName) {
        Preconditions.checkArgument(hasGraphics(stateName), "No graphics defined for " + stateName);
        return stateGraphics.get(stateName).graphics;
    }

    @Deprecated
    public Animation getCurrentStateGraphics() {
        return stateMachine.getActiveState().graphics;
    }

    @Deprecated
    public Timer getCurrentStateTimer() {
        return stateMachine.getActiveStateTimer();
    }

    public Image getCurrentGraphics() {
        return currentGraphics;
    }

    public int getCurrentWidth() {
        return currentGraphics.getWidth();
    }

    public int getCurrentHeight() {
        return currentGraphics.getHeight();
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkState(!stateGraphics.isEmpty(),
            "Cannot animate sprite that does not yet have any graphics");

        stateMachine.update(deltaTime);
        updateCurrentGraphics();
    }

    private void updateCurrentGraphics() {
        SpriteState activeState = stateMachine.getActiveState();
        float time = stateMachine.getActiveStateTimer().getTime();
        currentGraphics = activeState.graphics.getFrameAtTime(time);
    }

    @Override
    public Rect getStageBounds() {
        Transformable globalTransform = getGlobalTransform();
        Point2D position = globalTransform.getPosition();
        float width = Math.max(getCurrentWidth() * (globalTransform.getScaleX() / 100f), 1f);
        float height = Math.max(getCurrentHeight() * (globalTransform.getScaleY() / 100f), 1f);
        return new Rect(position.x() - width / 2f, position.y() - height / 2f, width, height);
    }

    /**
     * Creates a new sprite with states and graphics based on this one, but it
     * starts back in its initial state.
     */
    public Sprite copy() {
        Sprite copy = new Sprite();
        for (SpriteState state : stateGraphics.values()) {
            copy.addGraphics(state.name, state.graphics);
        }
        copy.changeGraphics(stateMachine.getActiveState().name);
        copy.getTransform().set(getTransform());
        return copy;
    }

    @Override
    public String toString() {
        SpriteState activeState = stateMachine.getActiveState();
        return "Sprite [" + activeState.name + "]";
    }

    /**
     * Connects one of the sprite's graphical states with the name that can
     * be used to activate that state.
     */
    private record SpriteState(String name, Animation graphics) {
    }
}
