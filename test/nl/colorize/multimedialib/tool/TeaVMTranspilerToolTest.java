//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.annotations.VisibleForTesting;
import nl.colorize.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeaVMTranspilerToolTest {

    private static final File RESOURCES_DIR = new File("resources");

    @Test
    void transpile(@TempDir File outputDir) throws IOException {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = RESOURCES_DIR;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        List<File> scriptFiles = FileUtils.walkFiles(outputDir,
            f -> f.getName().startsWith("script-") && f.getName().endsWith(".js"));
        String generatedCode = Files.readString(scriptFiles.getFirst().toPath(), UTF_8);

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

        assertEquals(1, scriptFiles.size());
        assertTrue(generatedCode.contains(expected), "Generated code:\n" + generatedCode);
    }

    @Test
    void copyFiles(@TempDir File outputDir) {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = RESOURCES_DIR;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.run();

        assertTrue(new File(outputDir, "multimedialib.js").exists());
        assertTrue(new File(outputDir, "assets").exists());
        assertTrue(new File(outputDir, "assets/colorize-icon-256.png").exists());
        assertTrue(new File(outputDir, "assets/browser").exists());
        assertTrue(new File(outputDir, "assets/browser/assets").exists());
        assertTrue(new File(outputDir, "assets/browser/assets/multimedialib.css").exists());
    }

    @Test
    void generateHTML(@TempDir File outputDir) throws IOException {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = RESOURCES_DIR;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.buildId = "1234";
        tool.run();

        File htmlFile = new File(outputDir, "index.html");
        String html = Files.readString(htmlFile.toPath(), UTF_8);
        String head = html.split("<body>")[0].trim();

        String expected = """
            <!DOCTYPE html>
            <html lang="en-US">
                <head>
                    <meta charset="UTF-8" />
                    <title>test</title>
                    <meta name="viewport" content="initial-scale=1.0, width=device-width, user-scalable=no" />
                    <link rel="shortcut icon" type="image/x-icon" href="assets/browser/assets/favicon.png" />
                    <link rel="apple-touch-icon" href="assets/browser/assetsapple-favicon.png" />
                    <link rel="stylesheet" type="text/css" href="assets/browser/assets/multimedialib.css" />
            
                    <meta name="build-id" content="1234" />
            
            
                    <script src="scripts/peerjs.min.js"></script>
            
            
                    <script src="multimedialib.js"></script>
                    <script src="browser-bridge.js"></script>
                    <script src="peerjs-bridge.js"></script>
                    <script src="script-1234.js"></script>
                </head>""";

        assertEquals(expected.replaceAll("  +", ""), head.replaceAll("  +", ""));
    }

    @Test
    void generateManifest(@TempDir File outputDir) throws IOException {
        TeaVMTranspilerTool tool = new TeaVMTranspilerTool();
        tool.projectName = "test";
        tool.resourceDir = RESOURCES_DIR;
        tool.outputDir = outputDir;
        tool.mainClassName = MockApp.class.getName();
        tool.buildId = "1234";
        tool.run();

        File htmlFile = new File(outputDir, "index.html");
        String html = Files.readString(htmlFile.toPath(), UTF_8);
        String manifest = html.split("<div id=\"resource-file-manifest\">")[1].split("</div>")[0];

        String expected = """
            OpenSans-Regular.ttf
            browser/assets/apple-favicon.png
            browser/assets/favicon.png
            browser/assets/loading.gif
            browser/assets/multimedialib.css
            browser/fragment-shader.glsl
            browser/index.html
            browser/javascript-libraries.txt
            browser/vertex-shader.glsl
            colorize-emblem-180.png
            colorize-emblem-64.png
            colorize-icon-256.png
            colorize-icon-32.png
            colorize-icon.icns
            colorize-logo-180.png
            com/badlogic/gdx/graphics/g3d/particles/particles.fragment.glsl
            com/badlogic/gdx/graphics/g3d/particles/particles.vertex.glsl
            com/badlogic/gdx/graphics/g3d/shaders/default.fragment.glsl
            com/badlogic/gdx/graphics/g3d/shaders/default.vertex.glsl
            com/badlogic/gdx/graphics/g3d/shaders/depth.fragment.glsl
            com/badlogic/gdx/graphics/g3d/shaders/depth.vertex.glsl
            com/badlogic/gdx/utils/lsans-15.fnt
            com/badlogic/gdx/utils/lsans-15.png
            demo/colorize-logo.gltf
            demo/crate.vox.mtl
            demo/crate.vox.obj
            demo/crate.vox.png
            demo/demo-sound.ogg
            demo/demo.png
            effects/particle-circle.png
            effects/particle-diamond.png
            net/mgsx/gltf/shaders/brdfLUT.png
            net/mgsx/gltf/shaders/default.fs.glsl
            net/mgsx/gltf/shaders/default.vs.glsl
            net/mgsx/gltf/shaders/depth.fs.glsl
            net/mgsx/gltf/shaders/depth.vs.glsl
            net/mgsx/gltf/shaders/emissive-only.fs.glsl
            net/mgsx/gltf/shaders/ibl-sun.fs.glsl
            net/mgsx/gltf/shaders/ibl-sun.vs.glsl
            net/mgsx/gltf/shaders/pbr/compat.fs.glsl
            net/mgsx/gltf/shaders/pbr/compat.vs.glsl
            net/mgsx/gltf/shaders/pbr/env.glsl
            net/mgsx/gltf/shaders/pbr/functions.glsl
            net/mgsx/gltf/shaders/pbr/ibl.glsl
            net/mgsx/gltf/shaders/pbr/iridescence.glsl
            net/mgsx/gltf/shaders/pbr/lights.glsl
            net/mgsx/gltf/shaders/pbr/material.glsl
            net/mgsx/gltf/shaders/pbr/pbr.fs.glsl
            net/mgsx/gltf/shaders/pbr/pbr.vs.glsl
            net/mgsx/gltf/shaders/pbr/shadows.glsl
            net/mgsx/gltf/shaders/skybox.fs.glsl
            net/mgsx/gltf/shaders/skybox.vs.glsl
            preload.txt
            startup-logo.png""";

        assertEquals(expected, manifest);
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

        String generatedHTML = Files.readString(new File(outputDir, "index.html").toPath(), UTF_8);

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

        String generatedHTML = Files.readString(new File(outputDir, "index.html").toPath(), UTF_8);

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
