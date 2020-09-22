//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@code RenderCallback} that does nothing except delegate
 * calls to a number of nested objects that also implement {@code RenderCallback}.
 */
public class NestedRenderCallback implements RenderCallback {

    private List<RenderCallback> delegates;

    public NestedRenderCallback() {
        this.delegates = new ArrayList<>();
    }

    public void add(RenderCallback delegate) {
        delegates.add(delegate);
    }

    public void remove(RenderCallback delegate) {
        delegates.remove(delegate);
    }

    @Override
    public void update(Renderer renderer, float deltaTime) {
        for (RenderCallback delegate : delegates) {
            delegate.update(renderer, deltaTime);
        }
    }

    @Override
    public void render(Renderer renderer, GraphicsContext2D graphics) {
        for (RenderCallback delegate : delegates) {
            delegate.render(renderer, graphics);
        }
    }
}
