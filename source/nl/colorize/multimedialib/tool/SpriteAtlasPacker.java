//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.google.common.base.Preconditions;
import nl.colorize.util.FileUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.cli.CommandLineArgumentParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Command line tool that generates a sprite atlas from a directory of images.
 * The created sprite atlas consists of a PNG file containing all sprites, plus
 * a metadata file in libGDX format.
 */
public class SpriteAtlasPacker {

    protected File inputDir;
    protected File outputDir;
    protected String atlasName;
    protected boolean nested;
    protected boolean flatten;

    private static final List<String> IMAGE_FILE_EXTENSIONS = List.of("png", "jpg");
    private static final Logger LOGGER = LogHelper.getLogger(SpriteAtlasPacker.class);

    public static void main(String[] argv) {
        CommandLineArgumentParser args = new CommandLineArgumentParser(SpriteAtlasPacker.class)
            .addRequired("--input", "Input directory containing images to process.")
            .addRequired("--output", "Output directory for saving the generated texture atlas.")
            .addOptional("--name", "File name for generated sprite atlas, defaults to directory name")
            .addFlag("--nested", "Creates a separate sprite atlas for each subdirectory.")
            .addFlag("--flatten", "Base region name on file name only, instead of relative path.")
            .parseArgs(argv);

        SpriteAtlasPacker tool = new SpriteAtlasPacker();
        tool.inputDir = args.get("input").getDir();
        tool.outputDir = args.get("output").getDir();
        tool.atlasName = args.get("name").getStringOr("");
        tool.nested = args.get("nested").getBool();
        tool.flatten = args.get("flatten").getBool();
        tool.run();
    }

    protected void run() {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.flattenPaths = flatten;

        for (File imageDir : locateImageDirs()) {
            LOGGER.info("Gathering images from " + imageDir.getAbsolutePath());
            TexturePacker.process(settings, imageDir.getAbsolutePath(), outputDir.getAbsolutePath(),
                getAtlasName(imageDir));
            LOGGER.info("Generated sprite atlas in " + outputDir.getAbsolutePath());
        }
    }

    private List<File> locateImageDirs() {
        if (!nested) {
            return List.of(inputDir);
        }

        try {
            return Files.walk(inputDir.toPath())
                .map(path -> path.toFile())
                .filter(File::isDirectory)
                .filter(this::containsImages)
                .toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read directory", e);
        }
    }

    private boolean containsImages(File dir) {
        return Arrays.stream(dir.listFiles())
            .filter(file -> !file.isDirectory())
            .map(FileUtils::getFileExtension)
            .anyMatch(IMAGE_FILE_EXTENSIONS::contains);
    }

    private String getAtlasName(File imageDir) {
        if (atlasName == null || atlasName.isEmpty()) {
            return imageDir.getName();
        } else {
            Preconditions.checkArgument(!nested, "Cannot combine --nested with --name");
            return atlasName;
        }
    }
}
