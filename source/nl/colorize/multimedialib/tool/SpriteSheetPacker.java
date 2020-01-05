//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.SpriteSheet;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.MediaException;
import nl.colorize.multimedialib.renderer.java2d.AWTImage;
import nl.colorize.util.LogHelper;
import nl.colorize.util.swing.Utils2D;
import org.kohsuke.args4j.Option;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Creates a new sprite sheet by taking all images located within a direcory,
 * and packing them into a single sprite sheet.
 */
public class SpriteSheetPacker extends CommandLineTool {

    @Option(name = "-input", required = true, usage = "Directory containing source images")
    public File inputDir;

    @Option(name = "-outimage", required = true, usage = "Generated image file location")
    public File outputImageFile;

    @Option(name = "-outdata", required = true, usage = "Generated YAML data file location")
    public File outputDataFile;

    @Option(name = "-size", required = true, usage = "Width/height of the sprite sheet")
    public int size;

    private static final List<Integer> VALID_SIZES = ImmutableList.of(32, 64, 128, 256, 512, 1024);
    private static final Logger LOGGER = LogHelper.getLogger(SpriteSheetPacker.class);

    public static void main(String[] args) {
        SpriteSheetPacker tool = new SpriteSheetPacker();
        tool.start(args);
    }

    @Override
    public void run() {
        Preconditions.checkArgument(VALID_SIZES.contains(size), "Invalid argument: " + size);

        LOGGER.info("Gathering images from directory " + inputDir.getAbsolutePath());

        Map<File, BufferedImage> images = gatherSourceImages();
        LOGGER.info("Creating sprite sheet from " + images.size() + " images");
        images.keySet().stream()
            .sorted(Comparator.comparing(File::getName))
            .forEach(file -> LOGGER.info("- " + file.getName()));

        LOGGER.info("Creating sprite sheet from " + images.size() + " images");
        SpriteSheet spritesheet = packImages(images);

        try {
            Utils2D.savePNG(((AWTImage) spritesheet.getImage()).getImage(), outputImageFile);
            saveDataFile(spritesheet);
            LOGGER.info("Saved sprite sheet to " + outputImageFile.getAbsolutePath());
        } catch (IOException e) {
            throw new MediaException("Could not save sprite sheet", e);
        }
    }

    private Map<File, BufferedImage> gatherSourceImages() {
        try {
            Map<File, BufferedImage> images = new HashMap<>();

            Files.walk(inputDir.toPath())
                .map(path -> path.toFile())
                .filter(file -> !file.isDirectory() && isImageFile(file))
                .peek(file -> LOGGER.info("- " + file.getName()))
                .forEach(file -> images.put(file, loadImage(file)));

            return images;
        } catch (IOException e) {
            throw new MediaException("Cannot load images from source directory");
        }
    }

    private boolean isImageFile(File file) {
        return file.getName().endsWith(".jpg") || file.getName().endsWith(".png");
    }

    private BufferedImage loadImage(File file) {
        try {
            return Utils2D.loadImage(file);
        } catch (IOException e) {
            throw new MediaException("Cannot load image " + file.getAbsolutePath(), e);
        }
    }

    private SpriteSheet packImages(Map<File, BufferedImage> images) {
        BufferedImage buffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(buffer, true, true);
        SpriteSheet spritesheet = new SpriteSheet(new AWTImage(buffer));

        int x = 0;
        int y = 0;

        for (File imageFile : sortByImageHeight(images)) {
            BufferedImage image = images.get(imageFile);

            if (x + image.getWidth() > size) {
                x = 0;
                y += image.getHeight();
                Preconditions.checkState(y < size, "Images do not fit in sprite sheet");
            }

            Rect region = new Rect(x, y, image.getWidth(), image.getHeight());
            spritesheet.markRegion(imageFile.getName(), region);
            g2.drawImage(image, x, y, null);
            x += region.getWidth();
        }

        g2.dispose();

        return spritesheet;
    }

    private Iterable<File> sortByImageHeight(Map<File, BufferedImage> images) {
        return images.keySet().stream()
            .sorted((a, b) -> images.get(b).getHeight() - images.get(a).getHeight())
            .collect(Collectors.toList());
    }

    private void saveDataFile(SpriteSheet spritesheet) throws IOException {
        try (PrintWriter writer = new PrintWriter(outputDataFile, Charsets.UTF_8.displayName())) {
            for (String name : spritesheet.getRegionNames()) {
                Rect region = spritesheet.getRegion(name);

                writer.println("- name: " + name);
                writer.println("  x: " + Math.round(region.getX()));
                writer.println("  y: " + Math.round(region.getY()));
                writer.println("  width: " + Math.round(region.getWidth()));
                writer.println("  height: " + Math.round(region.getHeight()));
            }
        }
    }
}
