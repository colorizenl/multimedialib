//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Updatable;
import nl.colorize.multimedialib.scene.SimpleState;
import nl.colorize.multimedialib.scene.StateMachine;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Static or animated two-dimensional image that can be composited into a larger
 * scene. Multiple sprites may use the same image data, the sprite is merely an
 * instance of the image that can be drawn by the renderer and does not modify
 * the image itself. Sprites can be transformed (positioned, rotated, scaled)
 * before they are displayed.
 * <p>
 * Sprites can have multiple possible graphical states, with each state being
 * represented by either a static image or by an animation. States are
 * identified by name, and are based around an internal {@link StateMachine}.
 */
public class Sprite implements Updatable {

    private StateMachine<SpriteState> stateMachine;
    private Point2D position;
    private Transform transform;

    public Sprite() {
        this.stateMachine = new StateMachine<>();
        this.position = new Point2D(0, 0);
        this.transform = new Transform();
    }

    /**
     * Adds a new animation state to this sprite.
     *
     * @throws IllegalArgumentException if a state with the same name has
     *         already been registered with this sprite.
     */
    public void addState(String name, Animation graphics) {
        SpriteState state = new SpriteState(name, graphics);
        stateMachine.register(state);
    }

    /**
     * Shorthand that adds a new state to this sprite that consists of a single
     * static image.
     *
     * @throws IllegalArgumentException if a state with the same name has
     *         already been registered with this sprite.
     */
    public void addState(String name, Image stateGraphics) {
        addState(name, new Animation(stateGraphics));
    }

    @Override
    public void update(float deltaTime) {
        stateMachine.update(deltaTime);

        SpriteState activeState = stateMachine.getActiveState();

        Preconditions.checkState(activeState != null, "Sprite is not active");
        Preconditions.checkState(activeState.graphics != null, "Sprite does not have graphics");
    }

    /**
     * Changes the sprite's graphics to the state with the specified name. If
     * the sprite was already in that stae this method does nothing.
     */
    public void changeState(String stateName) {
        SpriteState newState = stateMachine.getPossibleState(stateName);
        stateMachine.changeState(newState);
    }

    /**
     * Leaves the sprite in its current state, but resets the graphics for that
     * state to play from the beginning.
     */
    public void resetState() {
        stateMachine.resetActiveState();
    }

    public String getActiveState() {
        return stateMachine.getActiveState().getName();
    }

    public float getTimeInActiveState() {
        return stateMachine.getActiveStateTime();
    }

    public Set<String> getPossibleStates() {
        return stateMachine.getPossibleStates().stream()
            .map(state -> state.getName())
            .collect(Collectors.toSet());
    }

    public boolean hasState(String stateName) {
        return stateMachine.hasState(stateName);
    }

    public Animation getStateGraphics(String stateName) {
        return stateMachine.getPossibleState(stateName).graphics;
    }
    
    public Animation getActiveStateGraphics() {
        return getStateGraphics(getActiveState());
    }

    public Image getCurrentGraphics() {
        SpriteState activeState = stateMachine.getActiveState();
        return activeState.graphics.getFrameAtTime(stateMachine.getActiveStateTime());
    }

    public int getCurrentWidth() {
        return getCurrentGraphics().getWidth();
    }

    public int getCurrentHeight() {
        return getCurrentGraphics().getHeight();
    }

    public void setPosition(Point2D p) {
        position.set(p);
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public Point2D getPosition() {
        return position;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public Transform getTransform() {
        return transform;
    }

    /**
     * Creates a new sprite with states and graphics based on this one, but it
     * starts back in its initial state.
     */
    public Sprite copy() {
        Sprite copy = new Sprite();
        for (SpriteState state : stateMachine.getPossibleStates()) {
            copy.addState(state.getName(), state.graphics);
        }
        copy.changeState(stateMachine.getActiveState().getName());
        copy.setPosition(position.copy());
        copy.setTransform(transform.copy());
        return copy;
    }

    /**
     * Represents one of the sprite's possible graphical states.
     */
    private static class SpriteState extends SimpleState {

        private Animation graphics;

        public SpriteState(String name, Animation graphics) {
            super(name, 0f, null, true);
            this.graphics = graphics;
        }
    }
}
