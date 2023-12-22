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
import nl.colorize.util.TextUtils;

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
    private StateMachine<String> stateMachine;

    private static final String DEFAULT_STATE = "$$default";

    public Sprite() {
        this.location = new StageLocation();
        this.graphics = new HashMap<>();
        this.stateMachine = new StateMachine<>(DEFAULT_STATE);
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
     * @throws NullPointerException when trying to add {@code null} graphics.
     */
    public void addGraphics(String stateName, Animation stateGraphics) {
        Preconditions.checkNotNull(stateName, "Missing state name");
        Preconditions.checkNotNull(stateGraphics, "Missing state graphics");
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

        if (!stateMachine.getActiveState().equals(stateName)) {
            stateMachine.forceState(stateName);
        }
    }

    /**
     * Leaves the sprite in its current state, but resets the graphics for that
     * state to play from the beginning.
     */
    public void resetCurrentGraphics() {
        stateMachine.getActiveStateTimer().reset();
    }

    @Deprecated
    public String getActiveState() {
        return stateMachine.getActiveState();
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
        return graphics.get(stateMachine.getActiveState());
    }

    @Deprecated
    public Timer getCurrentStateTimer() {
        return stateMachine.getActiveStateTimer();
    }

    public Image getCurrentGraphics() {
        Animation currentGraphics = graphics.get(stateMachine.getActiveState());
        float time = stateMachine.getActiveStateTimer().getTime();
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

        stateMachine.update(deltaTime);
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
        copy.changeGraphics(stateMachine.getActiveState());
        copy.getTransform().set(getTransform());
        return copy;
    }

    @Override
    public String toString() {
        String state = stateMachine.getActiveState();
        String time = TextUtils.numberFormat(stateMachine.getActiveStateTimer().getTime(), 1);
        return "Sprite [" + state + "@" + time + "]";
    }
}
