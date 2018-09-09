//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.util.animation.Animatable;

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
 * represented by either a static image or by an animation. In addition to a
 * graphical representation, each state also has a <em>bounding shape</em>,
 * which is used to determine if sprites are colliding with each other. If not
 * set, a state's bounding shape is the same size as the
 */
public class Sprite implements Animatable {

    private Map<String, Animation> availableStates;
    private String currentState;
    private float timeInCurrentState;

    public Sprite() {
        availableStates = new HashMap<>();
        currentState = null;
        timeInCurrentState = 0f;
    }

    public void addState(String name, Animation stateGraphics, Shape boundingShape) {
        Preconditions.checkArgument(!availableStates.containsKey(name), "Duplicate state: " + name);
        Preconditions.checkArgument(stateGraphics != null, "No graphics for state: " + name);

        availableStates.put(name, stateGraphics);

        if (currentState == null) {
            changeState(name);
        }
    }

    public void addState(String name, Image stateGraphics, Shape boundingShape) {
        addState(name, new Animation(stateGraphics), boundingShape);
    }

    public void addState(String name, Animation stateGraphics) {
        addState(name, stateGraphics, null);
    }

    public void addState(String name, Image stateGraphics) {
        addState(name, stateGraphics, null);
    }

    public void changeState(String name) {
        currentState = name;
        timeInCurrentState = 0f;
    }

    public String getCurrentState() {
        return currentState;
    }

    public Set<String> getAvailableStates() {
        return ImmutableSet.copyOf(availableStates.keySet());
    }

    @Override
    public void onFrame(float deltaTime) {
        Preconditions.checkState(!availableStates.isEmpty(), "Sprite does not have graphics yet");

        timeInCurrentState += deltaTime;
    }

    public Image getCurrentGraphics() {
        Preconditions.checkState(currentState != null, "Sprite does not have graphics yet");
        return availableStates.get(currentState).getFrameAtTime(timeInCurrentState);
    }
}
