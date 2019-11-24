//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.scene.SceneManager;
import nl.colorize.multimedialib.tool.DemoApplication;

public class TeaDemo {

    private static final FilePointer VERIFICATION_INSTRUCTIONS = new FilePointer("verification-instructions.txt");

    public static void main(String[] args) {
        Browser.log("MultimediaLib - TeaVM Demo");

        Canvas canvas = new Canvas(800, 600, 1f);
        TeaRenderer renderer = new TeaRenderer(canvas);
        SceneManager sceneManager = SceneManager.attach(renderer);
        DemoApplication demo = new DemoApplication(renderer);
        sceneManager.changeScene(demo);

        String verificationInstructions = renderer.getMediaLoader().loadText(VERIFICATION_INSTRUCTIONS);
        Browser.log(verificationInstructions);
    }
}
