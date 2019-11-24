//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.renderer.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Scene that is split into a number of subsystems. This is typically needed
 * for scenes that contain a significant amount of functionality, in which
 * case the scene can be split into subsystems, with each subsystem
 * corresponding to a functional area, to manage the scene's complexity.
 */
public class ComplexScene implements Scene {

    private List<Subsystem> subsystems;
    private SceneContext context;

    public ComplexScene() {
        this.subsystems = new ArrayList<>();
    }

    public void register(Subsystem subsystem) {
        Preconditions.checkState(context == null,
            "Subsystem cannot be registered, scene has already started");

        subsystems.add(subsystem);
    }

    @Override
    public void start(SceneContext context) {
        this.context = context;

        for (Subsystem subsystem : subsystems) {
            subsystem.init();
        }
    }

    @Override
    public void update(float deltaTime) {
        for (Subsystem subsystem : subsystems) {
            subsystem.update(deltaTime);
        }
    }

    @Override
    public void render(GraphicsContext graphics) {
        for (Subsystem subsystem : subsystems) {
            subsystem.render(graphics);
        }
    }

    public SceneContext getContext() {
        Preconditions.checkState(context != null, "Scene has not started yet");
        return context;
    }
}
