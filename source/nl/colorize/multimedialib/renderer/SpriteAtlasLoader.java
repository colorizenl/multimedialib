//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.base.Splitter;
import nl.colorize.multimedialib.math.Coordinate;
import nl.colorize.multimedialib.math.Region;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.SpriteAtlas;
import nl.colorize.util.ResourceFile;

import java.util.List;

/**
 * Loads a sprite atlas based on the libGDX {@code .atlas} file format. Can be
 * used even when the libGDX renderer is not used.
 * <p>
 * This class is not intended to be used directly by applications, it is
 * provided by the media loader implementation for the current renderer.
 */
public class SpriteAtlasLoader {

    private MediaLoader mediaLoader;

    private static final Splitter COORDINATE_SPLITTER = Splitter.on(",").trimResults();

    protected SpriteAtlasLoader(MediaLoader mediaLoader) {
        this.mediaLoader = mediaLoader;
    }

    protected SpriteAtlas load(ResourceFile file) {
        if (!file.getName().endsWith(".atlas")) {
            throw new MediaException("Provided file is not a sprite atlas: " + file);
        }

        ParserState state = new ParserState();
        state.file = file;
        state.atlas = new SpriteAtlas();
        state.reset();

        for (String line : mediaLoader.loadTextLines(file)) {
            if (!line.isEmpty()) {
                parseLine(line, state);
            }
        }

        return state.atlas;
    }

    private void parseLine(String line, ParserState state) {
        if (line.endsWith(".png")) {
            state.currentImage = loadImage(state.file, line);
        } else if (countIndent(line) == 0) {
            state.name = line;
        } else if (line.trim().startsWith("xy:")) {
            state.xy = parsePropertyValue(line);
        } else if (line.trim().startsWith("size:")) {
            state.size = parsePropertyValue(line);

            if (state.isReady()) {
                flushEntry(state);
            }
        }
    }

    private int countIndent(String line) {
        int indent = 0;

        for (int i = 0; i < Math.min(line.length(), 80); i++) {
            if (line.charAt(i) == ' ') {
                indent++;
            } else if (line.charAt(i) == '\t') {
                indent += 4;
            }
        }

        return indent;
    }

    private void flushEntry(ParserState state) {
        Region region = new Region(state.xy.x(), state.xy.y(), state.size.x(), state.size.y());
        state.atlas.add(state.name, state.currentImage, region);

        state.reset();
    }

    private Image loadImage(ResourceFile origin, String name) {
        ResourceFile imageFile = origin.sibling(name);
        return mediaLoader.loadImage(imageFile);
    }

    private Coordinate parsePropertyValue(String line) {
        String value = line.substring(line.indexOf(":") + 1);
        List<String> parts = COORDINATE_SPLITTER.splitToList(value);
        return new Coordinate(Integer.parseInt(parts.get(0)), Integer.parseInt(parts.get(1)));
    }

    /**
     * The parser state is stored in a separate class to prevent the parser
     * from relying on global state, which could become an issue when a
     * renderer implementation tries to parse multiple files in parallel.
     */
    private static class ParserState {

        private ResourceFile file;
        private SpriteAtlas atlas;
        private Image currentImage;
        private String name;
        private Coordinate xy;
        private Coordinate size;

        public boolean isReady() {
            return name != null && currentImage != null && xy != null && size != null;
        }

        public void reset() {
            name = null;
            xy = null;
            size = null;
        }
    }
}
