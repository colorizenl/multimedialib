//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import nl.colorize.multimedialib.scene.Updatable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Static or animated two-dimensional image that can be integrated into a larger
 * scene. Multiple sprites may use the same image data, the sprite is merely an
 * instance of the image that can be drawn by the renderer and does not modify
 * the image itself. Sprites can be transformed (rotated, scaled) before they
 * are displayed.
 * <p>
 * Sprites can have multiple possible graphical states, with each state being
 * represented by either a static image or by an animation.
 */
public class Sprite implements Updatable {

    private Map<String, Animation> availableStates;
    private String currentState;
    private float timeInCurrentState;

    public Sprite() {
        availableStates = new HashMap<>();
        currentState = null;
        timeInCurrentState = 0f;
    }

    /**
     * Adds a new animation state to this sprite.
     * @throws IllegalArgumentException if a state with the same name has
     *         already been registered with this sprite.
     */
    public void addState(String name, Animation graphics) {
        Preconditions.checkArgument(!availableStates.containsKey(name), "Duplicate state: " + name);
        Preconditions.checkArgument(graphics != null, "No graphics for state: " + name);

        availableStates.put(name, graphics);

        if (currentState == null) {
            changeState(name);
        }
    }

    /**
     * Shorthand that adds a new state to this sprite that consists of a single
     * static image.
     * @throws IllegalArgumentException if a state with the same name has
     *         already been registered with this sprite.
     */
    public void addState(String name, Image stateGraphics) {
        addState(name, new Animation(stateGraphics));
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkState(currentState != null,
            "Sprite does not have graphics yet");

        timeInCurrentState += deltaTime;
    }

    /**
     * Changes the sprite's graphics to the state with the specified name. If
     * the sprite was already in that stae this method does nothing.
     */
    public void changeState(String stateName) {
        Preconditions.checkArgument(availableStates.containsKey(stateName),
            "Unknown state: " + stateName);

        if (currentState == null || !currentState.equals(stateName)) {
            currentState = stateName;
            timeInCurrentState = 0f;
        }
    }

    /**
     * Leaves the sprite in its current state, but resets the graphics for that
     * state to play from the beginning.
     */
    public void resetState() {
        timeInCurrentState = 0f;
    }

    public String getCurrentState() {
        return currentState;
    }

    public float getTimeInCurrentState() {
        return timeInCurrentState;
    }

    public Set<String> getAvailableStates() {
        return ImmutableSet.copyOf(availableStates.keySet());
    }

    public boolean hasState(String stateName) {
        return availableStates.containsKey(stateName);
    }

    public Animation getStateGraphics(String stateName) {
        Animation graphics = availableStates.get(stateName);
        Preconditions.checkArgument(graphics != null, "Unknown state: " + stateName);
        return graphics;
    }

    public Image getCurrentGraphics() {
        Preconditions.checkState(currentState != null,
            "Sprite does not have graphics yet");

        Animation graphics = availableStates.get(currentState);
        return graphics.getFrameAtTime(timeInCurrentState);
    }

    public int getCurrentWidth() {
        return getCurrentGraphics().getWidth();
    }

    public int getCurrentHeight() {
        return getCurrentGraphics().getHeight();
    }

    /**
     * Creates a new sprite with states and graphics based on this one, but it
     * starts back in its initial state.
     */
    public Sprite copy() {
        Sprite copy = new Sprite();
        for (Map.Entry<String, Animation> entry : availableStates.entrySet()) {
            copy.addState(entry.getKey(), entry.getValue());
        }
        copy.changeState(currentState);
        return copy;
    }
}
