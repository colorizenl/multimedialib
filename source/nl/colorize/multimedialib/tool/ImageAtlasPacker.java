//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.ImageAtlas;
import nl.colorize.multimedialib.graphics.ImageAtlasLoader;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.java2d.Java2DImage;
import nl.colorize.util.FileUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.swing.Utils2D;
import nl.colorize.util.xml.XMLHelper;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Tool that packs a numer of images into an image atlas. The created image
 * atlas consists of the atlas image and a XML file that contains the metadata
 * for the sub-images. Images are loaded and rendered to the image atlas using
 * Java2D. The dimensions of both the image atlas itself and its cells containing
 * sub-images will be power-of-two, even though the dimensions of the sub-images
 * themselves aren't.
 */
public class ImageAtlasPacker extends CommandLineTool {

    @Argument(index=0, metaVar="inputDir", required=true, usage="Location of images to pack")
    private String inputDirPath; 
    
    @Argument(index=1, metaVar="outputDir", required=true, usage="Where to create files")
    private String outputDirPath;
    
    @Option(name="-atlasWidth", required=true, usage="Width of created image atlas")
    private int atlasWidth = 0;
    
    @Option(name="-outlines", required=false, usage="Draws sub-image outlines for testing purposes")
    private boolean outlines = false;
    
    private static final List<String> SUPPORTED_FORMATS = ImmutableList.of("jpg", "png");
    private static final int MAX_ATLAS_SIZE = 2048;
    private static final Color OUTLINE_COLOR = Color.PINK;
    private static final Logger LOGGER = LogHelper.getLogger(ImageAtlasPacker.class);

    public static void main(String[] args) {
        ImageAtlasPacker tool = new ImageAtlasPacker();
        tool.start(args);
    }
    
    public void run() {
        File inputDir = parseInputDirectory(inputDirPath);
        List<File> imageFiles = findImageFilesToPack(inputDir);
        LOGGER.info("Found " + imageFiles.size() + " images to pack");
        for (File imageFile : imageFiles) {
            LOGGER.info("    - " + FileUtils.getRelativePath(imageFile, inputDir));
        }
        
        Map<String, BufferedImage> subImages = new LinkedHashMap<String, BufferedImage>();
        for (File imageFile : imageFiles) {
            subImages.put(imageFile.getName(), loadImage(imageFile));
        }
        
        ImageAtlas imageAtlas = createImageAtlas(subImages, atlasWidth);
        int atlasHeight = imageAtlas.getSourceImage().getHeight();
        LOGGER.info("Image atlas dimensions: " + atlasWidth + "x" + atlasHeight);
        
        if (atlasWidth > MAX_ATLAS_SIZE || atlasHeight > MAX_ATLAS_SIZE) {
            LOGGER.warning("Image atlas exceeds maximum size: " + atlasWidth + "x" + atlasHeight);
        }
        
        File outputDir = parseOutputDirectory(outputDirPath, true);
        saveImageAtlas(imageAtlas, outputDir);
    }

    private List<File> findImageFilesToPack(File inputDir) {
        try {
            return Files.walk(inputDir.toPath())
                .map(path -> path.toFile())
                .filter(file -> !file.isDirectory())
                .filter(file -> SUPPORTED_FORMATS.contains(FileUtils.getFileExtension(file).toLowerCase()))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read from " + inputDir.getAbsolutePath());
        }
    }

    private BufferedImage loadImage(File imageFile) {
        try {
            return Utils2D.loadImage(imageFile);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load image: " + imageFile.getAbsolutePath());
        }
    }
    
    /**
     * Creates an image atlas from the specified collection of images. The images
     * will be positioned to allow for the smallest image atlas possible.
     * @param subImages Maps from a sub-image's name to its contents. 
     * @param atlasWidth Width of the image atlas (the height will be auto-detected).
     */
    protected ImageAtlas createImageAtlas(Map<String, BufferedImage> subImages, int atlasWidth) {
        int cellSize = autoDetectCellSize(subImages.values());
        int atlasHeight = autoDetectAtlasHeight(subImages.values(), atlasWidth, cellSize);
        Map<BufferedImage, Rect> subImageBounds = calculateSubImageBounds(subImages.values(), 
                atlasWidth, cellSize);
        BufferedImage atlas = renderImageAtlas(subImageBounds, atlasWidth, atlasHeight);
        
        ImageAtlas imageAtlas = new ImageAtlas(new Java2DImage(atlas));
        for (BufferedImage subImage : arrangeSubImages(subImages.values())) {
            Rect cell = subImageBounds.get(subImage);
            Rect region = new Rect(cell.getCenterX() - subImage.getWidth() / 2,
                    cell.getCenterY() - subImage.getHeight() / 2, 
                    subImage.getWidth(), subImage.getHeight());
            imageAtlas.markSubImage(findSubImageName(subImages, subImage), region);
        }
        return imageAtlas;
    }
    
    /**
     * Sorts sub-images by their vertical size, smallest first.
     */
    private Iterable<BufferedImage> arrangeSubImages(Collection<BufferedImage> subImages) {
        List<BufferedImage> arrangedSubImages = new ArrayList<BufferedImage>(subImages);
        Collections.sort(arrangedSubImages, new Comparator<BufferedImage>() {
            public int compare(BufferedImage a, BufferedImage b) {
                return a.getHeight() - b.getHeight();
            }
        });
        return arrangedSubImages;
    }

    private int autoDetectCellSize(Collection<BufferedImage> images) {
        int largestWidth = 0;
        int largestHeight = 0;
        for (BufferedImage image : images) {
            largestWidth = Math.max(largestWidth, MathUtils.nextPowerOfTwo(image.getWidth()));
            largestHeight = Math.max(largestHeight, MathUtils.nextPowerOfTwo(image.getHeight()));
        }
        return Math.max(largestWidth, largestHeight);
    }
    
    private int autoDetectAtlasHeight(Collection<BufferedImage> images, int atlasWidth, int cellSize) {
        int imagesPerRow = atlasWidth / cellSize;
        int rows = MathUtils.ceiling((float) images.size() / (float) imagesPerRow);
        return MathUtils.nextPowerOfTwo(rows * cellSize);
    }
    
    private Map<BufferedImage, Rect> calculateSubImageBounds(Collection<BufferedImage> images, 
            int atlasWidth, int cellSize) {
        Map<BufferedImage, Rect> subImageBounds = new LinkedHashMap<>();
        int x = 0;
        int y = 0;
        
        for (BufferedImage image : images) {
            Preconditions.checkArgument(image.getWidth() <= cellSize && image.getHeight() <= cellSize,
                    "Sub-image does not fit in cell: " + image.getWidth() + "x" + image.getHeight());

            subImageBounds.put(image, new Rect(x, y, cellSize, cellSize));
            
            x += cellSize;
            if (x >= atlasWidth) {
                x = 0;
                y += cellSize;
            }
        }
        
        return subImageBounds;
    }
    
    private String findSubImageName(Map<String, BufferedImage> subImages, BufferedImage subImage) {
        for (Map.Entry<String, BufferedImage> entry : subImages.entrySet()) {
            if (entry.getValue() == subImage) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Unknown sub-image");
    }
    
    private BufferedImage renderImageAtlas(Map<BufferedImage, Rect> subImageBounds, int atlasWidth,
            int atlasHeight) {
        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(atlas, true, true);
        
        for (BufferedImage image : subImageBounds.keySet()) {
            Rect cell = subImageBounds.get(image);
            int cellCenterX = (cell.getX() + cell.getWidth() / 2) - (image.getWidth() / 2);
            int cellCenterY = (cell.getY() + cell.getHeight() / 2) - (image.getHeight() / 2);
            g2.drawImage(image, cellCenterX, cellCenterY, null);
            
            if (outlines) {
                g2.setColor(OUTLINE_COLOR);
                g2.drawRect(cellCenterX - cell.getWidth() / 2, cellCenterY - cell.getHeight() / 2,
                        cell.getWidth(), cell.getHeight());
            }
        }
        
        g2.dispose();
        return atlas;
    }
    
    protected void saveImageAtlas(ImageAtlas imageAtlas, File outputDir) {
        long timestamp = System.currentTimeMillis();
        File imageAtlasFile = new File(outputDir, "image_atlas_" + timestamp + ".png");
        File xmlFile = new File(outputDir, "image_atlas_" + timestamp + ".xml");
        try {
            Utils2D.savePNG(((Java2DImage) imageAtlas.getSourceImage()).getImage(), imageAtlasFile);
            LOGGER.info("Created image atlas at " + imageAtlasFile.getAbsolutePath());

            ImageAtlasLoader imageAtlasLoader = new ImageAtlasLoader(null);
            XMLHelper.write(imageAtlasLoader.export(imageAtlas), xmlFile);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not create image atlas", e);
        }
    }
}
