//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.base.Preconditions;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.MediaLoader;

/**
 * Sprite sheet that loads images from individual files the first time they are
 * requested. This class exists so that during development the original images
 * can be used without having to pack them into a sprite sheet first, while
 * still allowing the application to use the {@link SpriteSheet} API.
 */
public class VirtualSpriteSheet extends SpriteSheet {

    private MediaLoader mediaLoader;
    private String prefix;

    public VirtualSpriteSheet(MediaLoader mediaLoader, String prefix) {
        super(null);

        Preconditions.checkArgument(!prefix.startsWith("/"), "Invalid prefix: " + prefix);
        Preconditions.checkArgument(!prefix.contains(".."), "Invalid prefix: " + prefix);

        this.mediaLoader = mediaLoader;
        this.prefix = prefix;
    }

    public VirtualSpriteSheet(MediaLoader mediaLoader) {
        this(mediaLoader, "");
    }

    @Override
    public Image get(String name) {
        FilePointer imagePath = new FilePointer(prefix + name);
        return mediaLoader.loadImage(imagePath);
    }

    @Override
    protected boolean isValidRegion(Rect region) {
        return true;
    }

    @Override
    public Rect getRegion(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public Image getImage() {
        throw new UnsupportedOperationException();
    }
}
