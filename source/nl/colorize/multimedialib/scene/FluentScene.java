//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import java.util.function.BooleanSupplier;

/**
 * Implementation of the {@link Scene} interface that delegates to callback
 * functions. This class is intended for dynamically creating sub-scenes.
 */
public class FluentScene implements Scene {

    private Updatable onFrame;
    private BooleanSupplier completed;
    private Runnable onComplete;

    public FluentScene(Updatable onFrame) {
        this.onFrame = onFrame;
    }

    public FluentScene withCompletion(BooleanSupplier completed, Runnable onComplete) {
        this.completed = completed;
        this.onComplete = onComplete;
        return this;
    }

    public FluentScene withCompletion(BooleanSupplier completed) {
        return withCompletion(completed, null);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        onFrame.update(deltaTime);
    }

    @Override
    public void end(SceneContext context) {
        if (onComplete != null) {
            onComplete.run();
        }
    }

    @Override
    public boolean isCompleted() {
        return completed != null && completed.getAsBoolean();
    }
}
