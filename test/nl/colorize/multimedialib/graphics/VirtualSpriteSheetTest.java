//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nl.colorize.multimedialib.mock.MockMediaLoader;

public class VirtualSpriteSheetTest {

    @Test
    public void testLoadImages() {
        MockMediaLoader mediaLoader = new MockMediaLoader();
        VirtualSpriteSheet spriteSheet = new VirtualSpriteSheet(mediaLoader);
        spriteSheet.get("a.png");

        assertEquals(1, mediaLoader.getLoaded().size());
        assertEquals("a.png", mediaLoader.getLoaded().get(0).toString());
    }

    @Test
    public void testLoadImagesWithPrefix() {
        MockMediaLoader mediaLoader = new MockMediaLoader();
        VirtualSpriteSheet spriteSheet = new VirtualSpriteSheet(mediaLoader, "sub/");
        spriteSheet.get("a.png");

        assertEquals(1, mediaLoader.getLoaded().size());
        assertEquals("sub/a.png", mediaLoader.getLoaded().get(0).toString());
    }
}
