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
 * Scene that is split into a number of subsystems. This is typically needed
 * for scenes that contain a significant amount of functionality, in which
 * case the scene can be split into subsystems, with each subsystem
 * corresponding to a functional area, to manage the scene's complexity.
 */
public class ComplexScene implements Scene {

    private List<Subsystem> subsystems;

    public ComplexScene() {
        this.subsystems = new ArrayList<>();
    }

    public void register(Subsystem subsystem) {
        subsystems.add(subsystem);
    }

    public void unregister(Subsystem subsystem) {
        subsystems.remove(subsystem);
    }

    @Override
    public void start() {
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
}
