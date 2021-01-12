//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.util.FileUtils;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.swing.Utils2D;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppleIconTool extends CommandLineTool {

    @Argument(index = 0, metaVar = "image", required = true)
    public File inputImageFile;

    @Argument(index = 1, metaVar = "location", required = true)
    public File location;

    @Option(name = "-platform", required = true, usage = "Either 'mac' or 'ios'")
    public String platform = "mac";

    private static final List<Integer> MAC_VARIANTS = ImmutableList.of(16, 32, 128, 256, 512);
    private static final List<Integer> IOS_VARIANTS = ImmutableList.of(60, 76, 1024);
    private static final Logger LOGGER = LogHelper.getLogger(AppleIconTool.class);

    public static void main(String[] args) {
        AppleIconTool tool = new AppleIconTool();
        tool.start(args);
    }

    @Override
    public void run() {
        try {
            if (!Platform.isMac()) {
                LOGGER.warning("Creating ICNS icons is only possible on Mac OS");
                return;
            }

            BufferedImage sourceImage = loadImage(inputImageFile);
            List<Icon> iconSet = createIconSet(sourceImage, getIconVariants());

            switch (platform) {
                case "mac" : createICNS(iconSet); break;
                case "ios" : createIOS(iconSet); break;
                default : throw new IllegalArgumentException("Unsupported platform: " + platform);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create ICNS icon", e);
        }
    }

    private List<Integer> getIconVariants() {
        switch (platform) {
            case "mac" : return MAC_VARIANTS;
            case "ios" : return IOS_VARIANTS;
            default : throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
    }

    private BufferedImage loadImage(File sourceImageFile) {
        try {
            BufferedImage image = Utils2D.loadImage(sourceImageFile);
            if (image.getWidth() != image.getHeight()) {
                throw new RuntimeException("Image must be square to be used as icon");
            }
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load image: " + sourceImageFile.getAbsolutePath());
        }
    }

    private List<Icon> createIconSet(BufferedImage source, List<Integer> variants) {
        List<Icon> iconSet = new ArrayList<>();

        for (int variant : variants) {
            iconSet.add(new Icon(getIconName(variant, 1), 1, scaleIconImage(source, variant)));
            if (variant < 1024) {
                iconSet.add(new Icon(getIconName(variant, 2), 2, scaleIconImage(source, 2 * variant)));
            }
            if (variant == 60) {
                iconSet.add(new Icon(getIconName(variant, 3), 3, scaleIconImage(source, 3 * variant)));
            }
        }

        return iconSet;
    }

    private String getIconName(int size, int factor) {
        String separator = platform.equals("mac") ? "_" : "-";
        if (factor == 1) {
            return "icon" + separator + size + "x" + size + ".png";
        }
        return "icon" + separator + size + "x" + size + "@" + factor + "x.png";
    }

    private BufferedImage scaleIconImage(BufferedImage sourceImage, int size) {
        if (sourceImage.getWidth() == size && sourceImage.getHeight() == size) {
            return sourceImage;
        } else {
            return Utils2D.scaleImage(sourceImage, size, size, true);
        }
    }

    private void createICNS(List<Icon> iconSet) {
        Preconditions.checkArgument(location.getName().endsWith(".icns"),
            "Output file must be an ICNS icon");

        try {
            File tempDir = FileUtils.createTempDir();
            File iconSetDir = createMacIconSet(iconSet, tempDir);
            String inputPath = iconSetDir.getAbsolutePath();
            String outputPath = location.getAbsolutePath();

            new ProcessBuilder("iconutil", "-c", "icns", inputPath, "-o", outputPath)
                .inheritIO()
                .start()
                .waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to create icon", e);
        }
    }

    private File createMacIconSet(List<Icon> iconSet, File tempDir) throws IOException {
        File iconSetDir = new File(tempDir, "icon.iconset");
        if (iconSetDir.exists()) {
            throw new IOException("Icon set already exists: " + iconSetDir.getAbsolutePath());
        }
        iconSetDir.mkdir();
        saveIconSet(iconSet, iconSetDir);
        return iconSetDir;
    }

    private void saveIconSet(List<Icon> iconSet, File dir) throws IOException {
        for (Icon icon : iconSet) {
            File imageFile = new File(dir, icon.name);
            Utils2D.savePNG(icon.image, imageFile);
        }
    }

    private void createIOS(List<Icon> iconSet) throws IOException {
        location.mkdir();
        saveIconSet(iconSet, location);
        generateContentsJSON(iconSet, new File(location, "Contents.json"));
    }

    private void generateContentsJSON(List<Icon> iconSet, File outputFile) {
        try (PrintWriter writer = new PrintWriter(outputFile, Charsets.UTF_8)) {
            writer.println("{");
            writer.println("  \"images\" : [");
            for (Icon icon : iconSet) {
                writer.println("    {");
                writer.println("      \"size\" : \"" + icon.getSize() + "\",");
                writer.println("      \"idiom\" : \"" + icon.getIdiom() + "\",");
                writer.println("      \"filename\" : \"" + icon.name + "\",");
                writer.println("      \"scale\" : \"" + icon.variant + "x\"");
                writer.println(iconSet.get(iconSet.size() - 1).equals(icon) ? "    }" : "    },");
            }
            writer.println("  ],");
            writer.println("  \"info\" : {");
            writer.println("    \"version\" : 1,");
            writer.println("    \"author\" : \"xcode\"");
            writer.println("  }");
            writer.println("}");
        } catch (IOException e) {
            throw new RuntimeException("Unable to generate " + outputFile.getAbsolutePath());
        }
    }

    /**
     * Represents all properties corresponding to one of the icon images within
     * an Apple icon.
     */
    private static class Icon {

        private String name;
        private int variant;
        private BufferedImage image;

        public Icon(String name, int variant, BufferedImage image) {
            this.name = name;
            this.variant = variant;
            this.image = image;
        }

        public String getSize() {
            int size = image.getWidth() / variant;
            return size + "x" + size;
        }

        public String getIdiom() {
            if (image.getWidth() == 1024) {
                return "ios-marketing";
            } else if (image.getWidth() % 76 == 0) {
                return "ipad";
            } else {
                return "iphone";
            }
        }
    }
}
