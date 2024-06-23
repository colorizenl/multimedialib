//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.util.Subscribable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * Tracks progress for media files that are loaded by the renderer. If the
 * renderer supports loading media files asynchronously, this can be used to
 * display a loading screen. If the renderer only supports loading media
 * files synchronously, media files will immediately be marked as loaded upon
 * creation.
 */
public record LoadStatus(FilePointer file, BooleanSupplier tracker) {

    public boolean isLoaded() {
        return tracker.getAsBoolean();
    }

    @Override
    public String toString() {
        return file.toString();
    }

    /**
     * Creates a {@link LoadStatus} that tracks the status of the asynchronous
     * operation represented by the {@link Subscribable}.
     */
    public static LoadStatus track(FilePointer file, Subscribable<?> operation) {
        AtomicBoolean status = new AtomicBoolean(false);
        operation.subscribe(event -> status.set(true));
        return new LoadStatus(file, status::get);
    }
}
