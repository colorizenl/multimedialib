//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.FiniteStateMachine;
import nl.colorize.multimedialib.scene.State;

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
 * identified by name, and are based around an internal {@link FiniteStateMachine}.
 */
public class Sprite implements Graphic2D {

    @Getter private StageLocation location;
    private FiniteStateMachine<Animation> stateMachine;

    private static final String DEFAULT_STATE = "$$default";

    public Sprite() {
        this.location = new StageLocation();
        this.stateMachine = new FiniteStateMachine<>();
    }

    public Sprite(Animation anim) {
        this();
        addState(DEFAULT_STATE, anim);
    }

    public Sprite(Image image) {
        this();
        addState(DEFAULT_STATE, image);
    }

    /**
     * Adds a new animation state to this sprite.
     *
     * @throws IllegalArgumentException if a state with the same name has
     *         already been registered with this sprite.
     */
    public void addState(String name, Animation graphics) {
        stateMachine.register(State.of(name, graphics));
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
    }

    /**
     * Changes the sprite's graphics to the state with the specified name. If
     * the sprite was already in that stae this method does nothing.
     */
    public void changeState(String state) {
        stateMachine.changeState(state);
    }

    /**
     * Leaves the sprite in its current state, but resets the graphics for that
     * state to play from the beginning.
     */
    public void resetState() {
        stateMachine.resetActiveState();
    }

    public String getActiveState() {
        return stateMachine.getActiveState().name();
    }

    public Set<String> getPossibleStates() {
        return stateMachine.getPossibleStates().stream()
            .map(State::name)
            .collect(Collectors.toSet());
    }

    public boolean hasState(String stateName) {
        return stateMachine.hasPossibleState(stateName);
    }

    public Animation getStateGraphics(String stateName) {
        return stateMachine.getPossibleState(stateName).properties();
    }
    
    public Animation getActiveStateGraphics() {
        return getStateGraphics(getActiveState());
    }

    public Image getCurrentGraphics() {
        Animation graphics = stateMachine.getActiveStateProperties();
        return graphics.getFrameAtTime(stateMachine.getActiveStateTime());
    }

    public int getCurrentWidth() {
        return getCurrentGraphics().getWidth();
    }

    public int getCurrentHeight() {
        return getCurrentGraphics().getHeight();
    }

    @Override
    public Rect getStageBounds() {
        Transform globalTransform = getGlobalTransform();
        Point2D position = globalTransform.getPosition();
        float width = Math.max(getCurrentWidth() * (globalTransform.getScaleX() / 100f), 1f);
        float height = Math.max(getCurrentHeight() * (globalTransform.getScaleY() / 100f), 1f);
        return new Rect(position.getX() - width / 2f, position.getY() - height / 2f, width, height);
    }

    /**
     * Creates a new sprite with states and graphics based on this one, but it
     * starts back in its initial state.
     */
    public Sprite copy() {
        Sprite copy = new Sprite();
        for (State<Animation> state : stateMachine.getPossibleStates()) {
            copy.addState(state.name(), state.properties());
        }
        copy.changeState(stateMachine.getActiveState().name());
        copy.getTransform().set(getTransform());
        return copy;
    }

    @Override
    public String toString() {
        return "Sprite [" + getCurrentGraphics() + "]";
    }
}
