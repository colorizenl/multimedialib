//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.LogHelper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used when the renderer encounters an error. While the error handler has
 * access to the {@link SceneContext}, it itself is connected to the life cycle
 * of the application, and not bound to the current scene.
 */
@FunctionalInterface
public interface ErrorHandler {

    /**
     * Default implementation that does nothing except logging the error.
     */
    public static final ErrorHandler DEFAULT = (context, cause) -> {
        Logger logger = LogHelper.getLogger(ErrorHandler.class);
        logger.log(Level.SEVERE, "MultimediaLib application error", cause);
    };

    public void onError(SceneContext context, Exception cause);
}
