//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilePointerTest {

    @Test
    void disallowAbsolutePath() {
        assertThrows(IllegalArgumentException.class, () -> new FilePointer("/tmp/test.txt"));
    }

    @Test
    void getFileName() {
        assertEquals("test.txt", new FilePointer("test.txt").getFileName());
        assertEquals("test.txt", new FilePointer("a/test.txt").getFileName());
        assertEquals("test.txt", new FilePointer("a/b/test.txt").getFileName());
    }

    @Test
    void getSiblingFile() {
        assertEquals("other.txt", new FilePointer("test.txt").sibling("other.txt").path());
        assertEquals("a/other.txt", new FilePointer("a/test.txt").sibling("other.txt").path());
        assertEquals("a/b/other.txt", new FilePointer("a/b/test.txt").sibling("other.txt").path());
    }

    @Test
    void defaultConstructorDoesNotAllowAbsolutePath() {
        assertThrows(IllegalArgumentException.class, () -> new FilePointer("/tmp/test.txt"));
    }
}
