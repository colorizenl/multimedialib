//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.three;

import org.teavm.jso.JSObject;

/**
 * TeaVM interface for the {@code three-bridge.js} JavaScript implementation.
 */
public interface ThreeBridge extends JSObject {

    public void init();

    public void render();

    public void loadTexture();

    /**
     *
     * let clock = new THREE.Clock();
     * let delta = 0;
     * // 30 fps
     * let interval = 1 / 30;
     *
     * function update() {
     *   requestAnimationFrame(update);
     *   delta += clock.getDelta();
     *
     *    if (delta  > interval) {
     *        // The draw or time dependent code are here
     *        render();
     *
     *        delta = delta % interval;
     *    }
     * }
     *
     */
}
