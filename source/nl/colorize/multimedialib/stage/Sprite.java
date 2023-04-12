//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

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

    private FiniteStateMachine<Animation> stateMachine;
    private boolean visible;
    private Point2D position;
    private Transform transform;

    private static final String DEFAULT_STATE = "$$default";

    public Sprite() {
        this.stateMachine = new FiniteStateMachine<>();
        this.visible = true;
        this.position = new Point2D(0, 0);
        this.transform = new Transform();
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
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setPosition(Point2D p) {
        position.set(p);
    }

    @Override
    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    @Override
    public Point2D getPosition() {
        return position;
    }

    public void setTransform(Transform transform) {
        if (transform == null) {
            transform = new Transform();
        }
        this.transform = transform;
    }

    public Transform getTransform() {
        return transform;
    }

    @Override
    public Rect getBounds() {
        float width = Math.max(getCurrentWidth() * (transform.getScaleX() / 100f), 1f);
        float height = Math.max(getCurrentHeight() * (transform.getScaleY() / 100f), 1f);
        return new Rect(position.getX() - width / 2f, position.getY() - height / 2f, width, height);
    }

    /**
     * Scales this sprite to the specified dimensions, based on the sprite's
     * current graphics.
     */
    public void scaleTo(float width, float height) {
        float scaleX = width / (float) getCurrentWidth() * 100f;
        float scaleY = height / (float) getCurrentHeight() * 100f;
        transform.setScale(scaleX, scaleY);
    }

    @Override
    public boolean hitTest(Point2D point) {
        return getBounds().contains(point);
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
        copy.setPosition(position.copy());
        copy.setTransform(transform.copy());
        return copy;
    }

    @Override
    public String toString() {
        return "Sprite [" + getCurrentGraphics() + " @ " + position + "]";
    }
}
