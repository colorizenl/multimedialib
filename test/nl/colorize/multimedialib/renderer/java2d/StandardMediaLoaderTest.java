//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.util.ResourceFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardMediaLoaderTest {

    private static final ResourceFile PLACEHOLDER = new ResourceFile("placeholder.txt");

    @Test
    void stripTrailingNewlineFromTextFile() {
        List<String> a = mockMediaLoader("first\nsecond").loadTextLines(PLACEHOLDER);
        List<String> b = mockMediaLoader("first\nsecond\n").loadTextLines(PLACEHOLDER);
        List<String> c = mockMediaLoader("").loadTextLines(PLACEHOLDER);
        List<String> d = mockMediaLoader("first\n\nsecond").loadTextLines(PLACEHOLDER);

        assertEquals(List.of("first", "second"), a);
        assertEquals(List.of("first", "second"), b);
        assertEquals(List.of(), c);
        assertEquals(List.of("first", "", "second"), d);
    }

    private StandardMediaLoader mockMediaLoader(String contents) {
        return new StandardMediaLoader() {
            @Override
            public String loadText(ResourceFile file) {
                return contents;
            }
        };
    }
}
