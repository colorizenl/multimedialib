//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.google.common.base.Preconditions;
import nl.colorize.util.AppProperties;
import nl.colorize.util.LogHelper;
import nl.colorize.util.cli.CommandLineArgumentParser;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Command line tool that generates a sprite atlas from a directory of images.
 */
public class SpriteAtlasPacker {

    protected File inputDir;
    protected File outputDir;
    protected String atlasName;
    protected boolean nested;
    protected boolean flatten;

    private static final Logger LOGGER = LogHelper.getLogger(SpriteAtlasPacker.class);

    public static void main(String[] argv) {
        AppProperties args = new CommandLineArgumentParser(SpriteAtlasPacker.class)
            .addRequired("--input", "Input directory containing images to process.")
            .addRequired("--output", "Output directory for saving the generated texture atlas.")
            .addOptional("--name", "File name for generated sprite atlas, defaults to directory name")
            .addFlag("--nested", "Creates a separate sprite atlas for each subdirectory.")
            .addFlag("--flatten", "Base region name on file name only, instead of relative path.")
            .parseArgs(argv);

        SpriteAtlasPacker tool = new SpriteAtlasPacker();
        tool.inputDir = args.getDir("input");
        tool.outputDir = args.getDir("output");
        tool.atlasName = args.get("name", "");
        tool.nested = args.getBool("nested");
        tool.flatten = args.getBool("flatten");
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
        if (nested) {
            return Arrays.stream(inputDir.listFiles())
                .filter(File::isDirectory)
                .toList();
        } else {
            return List.of(inputDir);
        }
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
