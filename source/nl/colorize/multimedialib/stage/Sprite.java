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
import nl.colorize.multimedialib.renderer.GraphicsMode;
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

    @Getter protected Graphic2D parent;
    @Getter private Transform transform;

    private Map<String, SpriteState> stateGraphics;
    private SpriteState currentState;
    private float currentStateTime;
    private Image currentGraphics;
    private float lastTick;

    private static final String NULL_STATE = "$$null";
    private static final String DEFAULT_STATE = "$$default";

    /**
     * Creates a sprite without default graphics. Trying to use the sprite
     * before graphics have been added will result in an exception.
     */
    public Sprite() {
        this.transform = new Transform();

        stateGraphics = new HashMap<>();
        currentState = new SpriteState(NULL_STATE, null);
        currentStateTime = 0f;
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

        if (!currentState.equals(state)) {
            currentState = state;
            currentStateTime = 0f;
            updateCurrentGraphics();
        }
    }

    /**
     * Leaves the sprite in its current state, but resets the graphics for that
     * state to play from the beginning.
     */
    public void resetCurrentGraphics() {
        currentStateTime = 0f;
        updateCurrentGraphics();
    }

    public String getActiveState() {
        return currentState.name;
    }

    public Set<String> getPossibleStates() {
        return stateGraphics.keySet();
    }

    public boolean hasGraphics(String stateName) {
        return stateGraphics.containsKey(stateName);
    }

    public Animation getGraphics(String stateName) {
        SpriteState state = stateGraphics.get(stateName);
        Preconditions.checkArgument(state != null, "No graphics defined: " + stateName);
        return state.graphics;
    }

    @Deprecated
    public Animation getCurrentStateGraphics() {
        Preconditions.checkState(currentGraphics != null, "Sprite is without graphics");
        return currentState.graphics;
    }

    @Deprecated
    public Timer getCurrentStateTimer() {
        if (currentState.graphics.isLoop() || currentState.graphics.getFrameCount() == 1) {
            return Timer.at(currentStateTime);
        } else {
            return Timer.at(currentStateTime, currentState.graphics.getDuration());
        }
    }

    public Image getCurrentGraphics() {
        Preconditions.checkState(currentGraphics != null, "Sprite is without graphics");
        return currentGraphics;
    }

    public int getCurrentWidth() {
        Preconditions.checkState(currentGraphics != null, "Sprite is without graphics");
        return currentGraphics.getWidth();
    }

    public int getCurrentHeight() {
        Preconditions.checkState(currentGraphics != null, "Sprite is without graphics");
        return currentGraphics.getHeight();
    }

    @Override
    public void updateGraphics(Timer sceneTime) {
        Preconditions.checkState(currentGraphics != null, "Sprite is without graphics");

        float tick = sceneTime.getTime();

        if (lastTick >= 0f) {
            float deltaTime = tick - lastTick;
            currentStateTime += deltaTime;
        }

        updateCurrentGraphics();
        lastTick = tick;
    }

    private void updateCurrentGraphics() {
        currentGraphics = currentState.graphics.getFrameAtTime(currentStateTime);
    }

    @Override
    public Rect getStageBounds() {
        Transform globalTransform = calculateGlobalTransform();
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
        copy.changeGraphics(currentState.name);
        copy.getTransform().set(getTransform());
        return copy;
    }

    @Override
    public String toString() {
        return "Sprite [" + currentState.name + "]";
    }

    /**
     * Connects one of the sprite's graphical states with the name that can
     * be used to activate that state.
     */
    private record SpriteState(String name, Animation graphics) {
    }
}
