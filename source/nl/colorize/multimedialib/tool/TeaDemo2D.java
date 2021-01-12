//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.teavm.Browser;
import nl.colorize.multimedialib.renderer.teavm.TeaRenderer;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.util.Platform;

/**
 * Launcher for the TeaVM version of the demo application. This class will be
 * transpiled to JavaScript and then called from the browser.
 */
public class TeaDemo2D {

    private static final FilePointer VERIFICATION_FILE = new FilePointer("verification-instructions.txt");

    public static void main(String[] args) {
        Platform.enableTeaVM();

        Browser.log("MultimediaLib - TeaVM Demo");
        Browser.log("Screen size: " + Browser.getScreenWidth() + "x" + Browser.getScreenHeight());
        Browser.log("Page size: " + Math.round(Browser.getPageWidth()) + "x" +
            Math.round(Browser.getPageHeight()));

        Canvas canvas = Canvas.flexible(Demo2D.DEFAULT_CANVAS_WIDTH,
            Demo2D.DEFAULT_CANVAS_HEIGHT);
        TeaRenderer renderer = new TeaRenderer(canvas);
        Application.start(renderer, new Demo2D());

        String verificationInstructions = renderer.getMediaLoader().loadText(VERIFICATION_FILE);
        Browser.log(verificationInstructions);
    }
}
