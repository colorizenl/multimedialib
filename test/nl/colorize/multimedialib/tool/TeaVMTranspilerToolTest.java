//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.colorize.util.swing.Utils2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeaVMTranspilerToolTest {

    @Test
    void transpile(@TempDir File resourcesDir, @TempDir File outputDir) throws IOException {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        String generatedCode = Files.toString(tool.getScriptFile(), Charsets.UTF_8);

        String expected = """
            ncmt_TeaVMTranspilerToolTest$MockApp_main = $args => {
                let $result;
                ncmt_TeaVMTranspilerToolTest$MockApp_$callClinit();
                $result = ju_ArrayList__init_1();
                $result.$add($rt_s(2));
                $result.$add($rt_s(3));
                $result.$clear();
            },
            """;

        assertTrue(generatedCode.contains(expected), "Generated code:\n" + generatedCode);
    }

    @Test
    void copyStandardOutputFiles(@TempDir File resourcesDir, @TempDir File outputDir) {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        assertTrue(new File(outputDir, "multimedialib.js").exists());
        assertTrue(new File(outputDir, "index.html").exists());
    }

    @Test
    void copyBinaryFiles(@TempDir File resourcesDir, @TempDir File outputDir) throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(image, false, false);
        g2.setColor(Color.RED);
        g2.fillOval(0, 0, 100, 100);
        g2.dispose();

        Utils2D.savePNG(image, new File(resourcesDir, "test.x"));

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        assertTrue(new File(outputDir, "test.x").exists());
    }

    @Test
    void rewriteHTML(@TempDir File resourcesDir, @TempDir File outputDir) throws IOException {
        Files.write("This is a test file\ncontaining multiple lines",
            new File(resourcesDir, "test.txt"), Charsets.UTF_8);

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), Charsets.UTF_8);

        String expected = "";
        expected += "<div id=\"test_txt\">This is a test file\n";
        expected += "containing multiple lines</div>\n";

        assertTrue(generatedHTML.contains(expected), "Generated HTML:\n" + generatedHTML);
    }

    @Test
    void generateResourceManifest(@TempDir File resourcesDir, @TempDir File outputDir) throws IOException {
        Files.write("This is a test file", new File(resourcesDir, "test1.txt"), Charsets.UTF_8);
        Files.write("This is a another test file", new File(resourcesDir, "test2.txt"), Charsets.UTF_8);

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), Charsets.UTF_8);

        String expected = """
            <div id="resource-file-manifest">OpenSans-Regular.ttf
            apple-favicon.png
            colorize-icon-256.png
            colorize-icon-32.png
            colorize-logo.gltf
            colorize-logo.png
            demo-sound.mp3
            demo.png
            favicon.png
            fragment-shader.glsl
            loading.gif
            multimedialib.css
            particle-circle.png
            particle-diamond.png
            test1.txt
            test2.txt
            vertex-shader.glsl
            </div>
            """;

        assertTrue(generatedHTML.contains(expected), "Generated HTML:\n" + generatedHTML);
    }

    @Test
    void reportErrorIfClassIsUnavailableInTeaVM(@TempDir File resourcesDir, @TempDir File outputDir) {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = BrokenMockApp.class.getName();

        assertThrows(UnsupportedOperationException.class, () -> tool.run());
    }

    @Test
    void insertMetaIntoHTML(@TempDir File resourcesDir, @TempDir File outputDir) throws IOException {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.meta = "first=value,second=other value";
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), Charsets.UTF_8);

        assertTrue(generatedHTML.contains("<meta name=\"first\" content=\"value\" />"));
        assertTrue(generatedHTML.contains("<meta name=\"second\" content=\"other value\" />"));
    }

    @Test
    void insertBuildId(@TempDir File resourcesDir, @TempDir File outputDir) throws IOException {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.buildId = "1234";
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), Charsets.UTF_8);

        assertTrue(generatedHTML.contains("<meta name=\"build-id\" content=\"1234\" />"));
    }

    /**
     * This only exists so that the generated TeaVM code can have an entry
     * point during the tests.
     */
    private static class MockApp {

        @VisibleForTesting
        public static void main(String[] args) {
            List<String> result = new ArrayList<>();
            result.add("a");
            result.add("2");
            result.clear();;
        }
    }

    /**
     * Entry point for an application that intentionally uses a class that
     * is not supported by TeaVM.
     */
    private static class BrokenMockApp {

        @VisibleForTesting
        public static void main(String[] args) {
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        }
    }
}
