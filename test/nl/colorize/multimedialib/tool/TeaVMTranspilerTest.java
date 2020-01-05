//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;
import nl.colorize.util.swing.Utils2D;
import org.junit.Test;
import org.teavm.apachecommons.io.Charsets;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TeaVMTranspilerTest {

    @Test
    public void testTranspile() throws IOException {
        File sourceDir = Files.createTempDir();
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

        TeaVMTranspiler tool = new TeaVMTranspiler();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        String generatedCode = Files.toString(new File(outputDir, "classes.js"), Charsets.UTF_8);

        String expected = "";
        expected += "function ncmt_TeaVMTranspilerTest$MockApp_main($args) {\n";
        expected += "    var $result;\n";
        expected += "    $result = ju_ArrayList__init_();\n";
        expected += "    $result.$add($rt_s(1));\n";
        expected += "    $result.$add($rt_s(2));\n";
        expected += "    $result.$clear();\n";
        expected += "}\n";

        assertTrue("Generated code:\n" + generatedCode, generatedCode.contains(expected));
    }

    @Test
    public void testCopyStandardOutputFiles() {
        File sourceDir = Files.createTempDir();
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

        TeaVMTranspiler tool = new TeaVMTranspiler();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        assertTrue(new File(outputDir, "multimedialib.js").exists());
        assertTrue(new File(outputDir, "index.html").exists());
    }

    @Test
    public void testCopyBinaryFiles() throws IOException {
        File sourceDir = Files.createTempDir();
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(image, false, false);
        g2.setColor(Color.RED);
        g2.fillOval(0, 0, 100, 100);
        g2.dispose();

        Utils2D.savePNG(image, new File(resourcesDir, "test.png"));

        TeaVMTranspiler tool = new TeaVMTranspiler();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        assertTrue(new File(outputDir, "test.png").exists());
    }

    @Test
    public void testRewriteHTML() throws IOException {
        File sourceDir = Files.createTempDir();
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

        Files.write("This is a test file\ncontaining multiple lines",
            new File(resourcesDir, "test.txt"), Charsets.UTF_8);

        TeaVMTranspiler tool = new TeaVMTranspiler();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), Charsets.UTF_8);

        String expected = "";
        expected += "<div id=\"test_txt\">This is a test file\n";
        expected += "containing multiple lines</div>\n";

        assertTrue("Generated HTML:\n" + generatedHTML, generatedHTML.contains(expected));
    }

    @Test
    public void testGenerateResourceFileManifest() throws IOException {
        File sourceDir = Files.createTempDir();
        File resourcesDir = Files.createTempDir();
        File outputDir = Files.createTempDir();

        Files.write("This is a test file", new File(resourcesDir, "test1.txt"), Charsets.UTF_8);
        Files.write("This is a another test file", new File(resourcesDir, "test2.txt"), Charsets.UTF_8);

        TeaVMTranspiler tool = new TeaVMTranspiler();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), Charsets.UTF_8);

        String expected = "";
        expected += "<div id=\"resource-file-manifest\">test1.txt\n";
        expected += "test2.txt</div>\n";

        assertTrue("Generated HTML:\n" + generatedHTML, generatedHTML.contains(expected));
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
