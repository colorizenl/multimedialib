//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Stopwatch;
import nl.colorize.util.Subject;
import nl.colorize.util.TextUtils;

/**
 * Tracks progress for media files that are loaded by the renderer. Can be
 * used by applications to display a loading screen while the renderer is
 * loading media files asynchronously.
 * <p>
 * A secondary use of this information is to provide performance statistics,
 * since the information can be used to determine the loading time for every
 * individual media asset, as well as the total loading time.
 */
@Getter
@Setter
public class LoadStatus {

    private final ResourceFile file;
    private boolean loaded;
    private float time;

    private LoadStatus(ResourceFile file) {
        this.file = file;
        this.loaded = false;
        this.time = 0f;
    }

    @Override
    public String toString() {
        if (loaded) {
            return file + "[" + TextUtils.numberFormat(time, 1) + "s]";
        } else {
            return file + "[loading]";
        }
    }

    /**
     * Creates a {@link LoadStatus} that tracks the status of the asynchronous
     * operation represented by the {@link Subject}.
     */
    public static LoadStatus track(ResourceFile file, Subject<?> operation) {
        LoadStatus loadStatus = new LoadStatus(file);
        Stopwatch timer = new Stopwatch();
        operation.subscribe(event -> {
            loadStatus.setLoaded(true);
            loadStatus.setTime(timer.tock() / 1000f);
        });
        return loadStatus;
    }
}
