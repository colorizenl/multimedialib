//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;

/**
 * Interface for objects responsible for loading multiple media assets in
 * bulk. Loading media is typically done during application startup, though
 * larger applications can also choose to load media in "blocks" based on
 * where in the application they're needed. Using a {@link MediaAssetStore}
 * is preferred to loading media assets directly in the scene, since this
 * avoids media files from being reloaded every time the scene is started.
 */
public interface MediaAssetStore {

    /**
     * Loads all media assets managed by this {@link MediaAssetStore}. Also
     * provides network access in case some of the media needs to be loaded
     * from remote locations.
     * <p>
     * To avoid loading the same media files multiple times, this method will
     * only be called when {@link #isLoaded()} returns false.
     *
     * @throws MediaException If one of more of the media assets fails to load.
     */
    public void loadMedia(MediaLoader mediaLoader, Network network);

    public boolean isLoaded();
}
