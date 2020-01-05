//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.multimedialib.tool.DemoApplication;
import nl.colorize.util.Platform;

/**
 * Launcher for the TeaVM version of the demo application. This class will be
 * transpiled to JavaScript and then called from the browser.
 */
public class TeaDemo {

    private static final FilePointer VERIFICATION_FILE = new FilePointer("verification-instructions.txt");

    public static void main(String[] args) {
        Platform.enableTeaVM();

        Browser.log("MultimediaLib - TeaVM Demo");
        Browser.log("Screen size: " + Browser.getScreenWidth() + "x" + Browser.getScreenHeight());
        Browser.log("Page size: " + Browser.getPageWidth() + "x" + Browser.getPageHeight());

        Canvas canvas = Canvas.flexible(DemoApplication.DEFAULT_CANVAS_WIDTH,
            DemoApplication.DEFAULT_CANVAS_HEIGHT);
        TeaRenderer renderer = new TeaRenderer(canvas);

        Application app = new Application(renderer);
        DemoApplication demo = new DemoApplication(app);
        app.changeScene(demo);

        String verificationInstructions = renderer.getMediaLoader().loadText(VERIFICATION_FILE);
        Browser.log(verificationInstructions);
    }
}
