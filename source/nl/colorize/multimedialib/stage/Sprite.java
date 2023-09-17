//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.scene.StateMachine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Static or animated image that can be displayed on the stage. Multiple
 * sprites can use the same image data, the sprite is merely an instance of
 * the image that can be drawn by the renderer and does not modify the image
 * itself. As they represent an instance of the original image, sprites can
 * be transformed without affecting the underlying image data.
 * <p>
 * Sprites support multiple graphical states, where each state can be
 * identified by its name.
 */
public class Sprite implements Graphic2D {

    @Getter private StageLocation location;
    private Map<String, Animation> graphics;
    private StateMachine<String> state;

    private static final String DEFAULT_STATE = "$$default";

    public Sprite() {
        this.location = new StageLocation();
        this.graphics = new HashMap<>();
        this.state = new StateMachine<>(DEFAULT_STATE);
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
    public void addGraphics(String stateName, Animation stateGraphics) {
        Preconditions.checkArgument(!graphics.containsKey(stateName),
            "Sprite already contains graphics for " + stateName);

        graphics.put(stateName, stateGraphics);
        if (graphics.size() == 1) {
            changeGraphics(stateName);
        }
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
     */
    public void changeGraphics(String stateName) {
        Preconditions.checkArgument(graphics.containsKey(stateName),
            "Sprite does not contain graphics for " + stateName);

        state.changeState(stateName);
    }

    /**
     * Leaves the sprite in its current state, but resets the graphics for that
     * state to play from the beginning.
     */
    public void resetCurrentGraphics() {
        state.getActiveStateTimer().reset();
    }

    @Deprecated
    public String getActiveState() {
        return state.getActiveState();
    }

    public Set<String> getPossibleStates() {
        return graphics.keySet();
    }

    public boolean hasGraphics(String stateName) {
        return graphics.containsKey(stateName);
    }

    public Animation getGraphics(String stateName) {
        Animation stateGraphics = graphics.get(stateName);
        Preconditions.checkArgument(stateGraphics != null,
            "Sprite does not contain graphics for " + stateName);
        return stateGraphics;
    }

    @Deprecated
    public Animation getCurrentStateGraphics() {
        return graphics.get(state.getActiveState());
    }

    public Image getCurrentGraphics() {
        Animation currentGraphics = graphics.get(state.getActiveState());
        float time = state.getActiveStateTimer().getTime();
        return currentGraphics.getFrameAtTime(time);
    }

    public int getCurrentWidth() {
        return getCurrentGraphics().getWidth();
    }

    public int getCurrentHeight() {
        return getCurrentGraphics().getHeight();
    }

    @Override
    public void update(float deltaTime) {
        Preconditions.checkState(!graphics.isEmpty(), "Sprite does not contain any graphics");

        state.update(deltaTime);
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
        for (Map.Entry<String, Animation> entry : graphics.entrySet()) {
            copy.addGraphics(entry.getKey(), entry.getValue());
        }
        copy.changeGraphics(state.getActiveState());
        copy.getTransform().set(getTransform());
        return copy;
    }

    @Override
    public String toString() {
        return "Sprite [" + getCurrentGraphics() + "]";
    }
}
