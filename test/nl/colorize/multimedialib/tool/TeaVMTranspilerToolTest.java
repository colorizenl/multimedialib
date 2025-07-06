//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.annotations.VisibleForTesting;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        String generatedCode = Files.toString(tool.getScriptFile(), UTF_8);

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
            new File(resourcesDir, "test.txt"), UTF_8);

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), UTF_8);

        String expected = "";
        expected += "<div id=\"test_txt\">This is a test file\n";
        expected += "containing multiple lines</div>\n";

        assertTrue(generatedHTML.contains(expected), "Generated HTML:\n" + generatedHTML);
    }

    @Test
    void generateResourceManifest(@TempDir File resourcesDir, @TempDir File outputDir) throws IOException {
        Files.write("This is a test file", new File(resourcesDir, "test1.txt"), UTF_8);
        Files.write("This is a another test file", new File(resourcesDir, "test2.txt"), UTF_8);

        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), UTF_8);

        String expected = """
            <div id="resource-file-manifest">OpenSans-Regular.ttf
            apple-favicon.png
            colorize-emblem.png
            colorize-icon-256.png
            colorize-icon-32.png
            colorize-logo.gltf
            colorize-logo.png
            crate.vox.mtl
            crate.vox.obj
            crate.vox.png
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

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), UTF_8);

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

        String generatedHTML = Files.toString(new File(outputDir, "index.html"), UTF_8);

        assertTrue(generatedHTML.contains("<meta name=\"build-id\" content=\"1234\" />"));
    }

    @Test
    void generateImportMap(@TempDir File resourcesDir, @TempDir File outputDir) throws IOException {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = resourcesDir;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.buildId = "1234";
        tool.run();

        String scriptTags = Files.toString(new File(outputDir, "index.html"), UTF_8)
            .split("<meta name=\"build-id\" content=\"1234\" />")[1]
            .split("<script src=\"script-")[0]
            .trim();

        String expected = """
            <script src="libraries/pixi.min.js"></script>
            <script src="libraries/peerjs.min.js"></script>
            <script type="importmap">
                {
                    "imports": {
                        "three": "./libraries/three.module.js",
                        "three/loaders/GLTFLoader": "./libraries/three/loaders/GLTFLoader.js",
                        "three/loaders/OBJLoader": "./libraries/three/loaders/OBJLoader.js",
                        "three/loaders/MTLLoader": "./libraries/three/loaders/MTLLoader.js",
                        "three/utils/BufferGeometryUtils": "./libraries/three/utils/BufferGeometryUtils.js"
                    }
                }
            </script>
            
                    <script src="multimedialib.js" type="module"></script>
                    <script src="browser-bridge.js" type="module"></script>
                    <script src="pixi-bridge.js" type="module"></script>
                    <script src="three-bridge.js" type="module"></script>
                    <script src="peerjs-bridge.js" type="module"></script>
            """;

        assertEquals(expected.trim(), scriptTags.trim());
        assertTrue(new File(outputDir, "libraries/pixi.min.js").exists());
        assertTrue(new File(outputDir, "libraries/three.module.js").exists());
        assertTrue(new File(outputDir, "libraries/three.core.js").exists());
        assertTrue(new File(outputDir, "libraries/three/loaders/GLTFLoader.js").exists());
        assertTrue(new File(outputDir, "libraries/three/utils/BufferGeometryUtils.js").exists());
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
