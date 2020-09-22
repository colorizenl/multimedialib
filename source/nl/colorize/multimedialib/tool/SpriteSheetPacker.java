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
import nl.colorize.util.CSVRecord;
import nl.colorize.util.LogHelper;
import nl.colorize.util.swing.Utils2D;
import org.kohsuke.args4j.Option;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
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

    @Option(name = "-outdata", required = true, usage = "Generated metadata file location")
    public File outputDataFile;

    @Option(name = "-metadata", required = true, usage = "Metadata file format, either 'yaml' or 'csv'")
    public String metadataFormat;

    @Option(name = "-size", required = true, usage = "Width/height of the sprite sheet")
    public int size;

    @Option(name = "-exclude", required = false, usage = "Excludes all images beyond a certain size")
    public int excludeSize;

    private static final List<Integer> VALID_SIZES = ImmutableList.of(32, 64, 128, 256, 512, 1024, 2048);
    private static final int PADDING = 1;
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
            saveMetadata(spritesheet);
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
            throw new MediaException("Cannot load images from source directory", e);
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

        int x = PADDING;
        int y = PADDING;
        int rowHeight = 0;

        for (File imageFile : prepareImages(images)) {
            BufferedImage image = images.get(imageFile);

            if (x + image.getWidth() > size) {
                x = PADDING;
                y += rowHeight + 2 * PADDING;
                rowHeight = 0;
            }

            Rect region = new Rect(x, y, image.getWidth(), image.getHeight());
            spritesheet.markRegion(toRegionName(imageFile), region);
            g2.drawImage(image, x, y, null);
            x += region.getWidth() + 2 * PADDING;
            rowHeight = Math.max(rowHeight, image.getHeight());
        }

        g2.dispose();

        return spritesheet;
    }

    private String toRegionName(File imageFile) {
        String relativePath = inputDir.toPath().relativize(imageFile.toPath()).toString();
        if (relativePath.indexOf('.') != -1) {
            relativePath = relativePath.substring(0, relativePath.lastIndexOf('.'));
        }
        return relativePath;
    }

    private boolean isImageExcluded(BufferedImage image) {
        if (excludeSize > 0) {
            return image.getWidth() >= excludeSize || image.getHeight() >= excludeSize;
        } else {
            return image.getWidth() > size || image.getHeight() > size;
        }
    }

    private Iterable<File> prepareImages(Map<File, BufferedImage> images) {
        List<File> excluded = new ArrayList<>();

        for (Map.Entry<File, BufferedImage> entry : images.entrySet()) {
            if (isImageExcluded(entry.getValue())) {
                LOGGER.info("Excluding image " + entry.getKey().getName());
                excluded.add(entry.getKey());
            }
        }

        return images.keySet().stream()
            .filter(file -> !excluded.contains(file))
            .sorted((a, b) -> images.get(b).getHeight() - images.get(a).getHeight())
            .collect(Collectors.toList());
    }

    private void saveMetadata(SpriteSheet spritesheet) throws IOException {
        if (metadataFormat.equals("yaml")) {
            saveMetadataYAML(spritesheet);
        } else if (metadataFormat.equals("csv")) {
            saveMetadataCSV(spritesheet);
        } else {
            throw new IllegalArgumentException("Unknown metadata format: " + metadataFormat);
        }
    }

    private void saveMetadataYAML(SpriteSheet spritesheet) throws IOException {
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

    private void saveMetadataCSV(SpriteSheet spriteSheet) throws IOException {
        List<String> headers = ImmutableList.of("name", "x", "y", "width", "height");

        List<String> regions = new ArrayList<>();
        regions.addAll(spriteSheet.getRegionNames());
        Collections.sort(regions);

        List<CSVRecord> records = regions.stream()
            .map(region -> getFields(spriteSheet, region))
            .map(fields -> CSVRecord.create(fields, ";"))
            .collect(Collectors.toList());

        try (PrintWriter writer = new PrintWriter(outputDataFile, Charsets.UTF_8.displayName())) {
            writer.write(CSVRecord.toCSV(records, headers));
        }
    }

    private List<String> getFields(SpriteSheet spriteSheet, String region) {
        Rect bounds = spriteSheet.getRegion(region);
        return ImmutableList.of(region, format(bounds.getX()), format(bounds.getY()),
            format(bounds.getWidth()), format(bounds.getHeight()));
    }

    private String format(float coordinate) {
        return String.valueOf(Math.round(coordinate));
    }
}
