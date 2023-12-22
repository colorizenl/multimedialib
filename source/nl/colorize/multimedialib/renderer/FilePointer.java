//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Reference to a resource file that is part of the application using a
 * relative path. This class only represents the location of the file, loading
 * the file contents is platform-specific and performed by {@link MediaLoader}.
 */
public record FilePointer(String path) {

    private static final Splitter PATH_SPLITTER = Splitter.on("/").omitEmptyStrings();

    public FilePointer {
        Preconditions.checkArgument(!path.isEmpty() && !path.contains("\\"),
            "Invalid path: " + path);
        Preconditions.checkArgument(!path.startsWith("/"),
            "Absolute files are not allowed: " + path);
    }

    public FilePointer(File localFile) {
        this(localFile.getAbsolutePath());
    }

    public String getFileName() {
        List<String> pathComponents = PATH_SPLITTER.splitToList(path);
        return pathComponents.get(pathComponents.size() - 1);
    }

    /**
     * Creates a new file pointer with the specified name, that is located in
     * the same parent directory as this file.
     */
    public FilePointer sibling(String fileName) {
        List<String> pathComponents = new ArrayList<>();
        pathComponents.addAll(PATH_SPLITTER.splitToList(path));
        pathComponents.remove(pathComponents.size() - 1);
        pathComponents.add(fileName);

        return new FilePointer(String.join("/", pathComponents));
    }

    @Override
    public String toString() {
        return path;
    }
}
