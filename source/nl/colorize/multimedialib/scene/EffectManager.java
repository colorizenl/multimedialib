//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Controls registration and playback for graphical effects that can be played
 * as part of a scene. It can either be used in combination with
 * {@link Subsystem} for out-of-the-box support, but instances can also be
 * created manually assuming the effect manager is updated and rendered during
 * every frame.
 */
public class EffectManager implements Subsystem {

    private List<Effect> effects;

    public EffectManager() {
        this.effects = new ArrayList<>();
    }

    @Override
    public void init() {
        cancelAll();
    }

    public void play(Effect effect) {
        effects.add(effect);
    }

    public void cancel(Effect effect) {
        effects.remove(effect);
    }

    public void cancelAll() {
        effects.clear();
    }

    @Override
    public void update(float deltaTime) {
        Effect[] buffer = effects.toArray(new Effect[0]);

        for (Effect effect : buffer) {
            effect.update(deltaTime);

            if (effect.isCompleted()) {
                effects.remove(effect);
            }
        }
    }

    @Override
    public void render(GraphicsContext graphics) {
        for (Effect effect : effects) {
            effect.render(graphics);
        }
    }
}
