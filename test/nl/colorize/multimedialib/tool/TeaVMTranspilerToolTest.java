//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import nl.colorize.util.FileUtils;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeaVMTranspilerToolTest {

    @Test
    public void testTranspile() throws IOException {
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.renderer = "canvas";
        tool.run();

        String generatedCode = Files.toString(new File(outputDir, "classes.js"), Charsets.UTF_8);

        String expected = "";
        expected += "function ncmt_TeaVMTranspilerToolTest$MockApp_main($args) {\n";
        expected += "    var $result;\n";
        expected += "    $result = ju_ArrayList__init_();\n";
        expected += "    $result.$add($rt_s(3));\n";
        expected += "    $result.$add($rt_s(4));\n";
        expected += "    $result.$clear();\n";
        expected += "}\n";

        assertTrue(generatedCode.contains(expected), "Generated code:\n" + generatedCode);
    }

    @Test
    public void testCopyStandardOutputFiles() {
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.renderer = "canvas";
        tool.run();

        assertTrue(new File(outputDir, "multimedialib.js").exists());
        assertTrue(new File(outputDir, "index.html").exists());
    }

    @Test
    public void testCopyBinaryFiles() throws IOException {
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

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
        tool.renderer = "canvas";
        tool.run();

        assertTrue(new File(outputDir, "test.x").exists());
    }

    @Test
    public void testRewriteHTML() throws IOException {
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

        Files.write("This is a test file\ncontaining multiple lines",
            new File(resourcesDir, "test.txt"), Charsets.UTF_8);

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.renderer = "canvas";
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), Charsets.UTF_8);

        String expected = "";
        expected += "<div id=\"test_txt\">This is a test file\n";
        expected += "containing multiple lines</div>\n";

        assertTrue(generatedHTML.contains(expected), "Generated HTML:\n" + generatedHTML);
    }

    @Test
    public void testGenerateResourceFileManifest() throws IOException {
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

        Files.write("This is a test file", new File(resourcesDir, "test1.txt"), Charsets.UTF_8);
        Files.write("This is a another test file", new File(resourcesDir, "test2.txt"), Charsets.UTF_8);

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.renderer = "canvas";
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), Charsets.UTF_8);

        String expected = "";
        expected += "<div id=\"resource-file-manifest\">test1.txt\n";
        expected += "test2.txt</div>\n";

        assertTrue(generatedHTML.contains(expected), "Generated HTML:\n" + generatedHTML);
    }

    @Test
    void generateServiceWorker(@TempDir File inputDir, @TempDir File outputDir) throws IOException {
        FileUtils.write("{}", Charsets.UTF_8, new File(inputDir, "manifest.json"));

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = inputDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.renderer = "canvas";
        tool.manifestFile = new File(inputDir, "manifest.json");
        tool.run();

        assertTrue(new File(outputDir, "manifest.json").exists());
        assertTrue(new File(outputDir, "service-worker.js").exists());
    }
    
    /**
     * This only exists so that the generated TeaVM code can have
     * an entry point during the tests.
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
}
