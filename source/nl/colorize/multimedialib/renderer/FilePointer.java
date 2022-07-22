//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;

/**
 * Reference to a resource file that is part of the application using a
 * relative path. This class only represents the location of the file, and not
 * its contents.
 */
public class FilePointer {

    private String path;

    public FilePointer(String path) {
        Preconditions.checkArgument(path.length() > 0, "Invalid path: " + path);
        Preconditions.checkArgument(!path.startsWith("/"), "Absolute path not allowed: " + path);

        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FilePointer) {
            FilePointer other = (FilePointer) o;
            return path.equals(other.path);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return path;
    }
}
