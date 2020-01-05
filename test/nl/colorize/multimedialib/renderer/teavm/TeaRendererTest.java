//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.multimedialib.tool.DemoApplication;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests the parts of the TeaVM renderer that are implemented in Java. Note that
 * the JavaScript code is *not* tested by JUnit, and mocked during these tests.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Browser.class)
public class TeaRendererTest {

    @Test
    public void testDemoApplication() {
        PowerMock.mockStatic(Browser.class);
        Browser.renderFrame(EasyMock.anyObject());
        EasyMock.expect(Browser.getCanvasWidth()).andReturn(800f);
        EasyMock.expect(Browser.getCanvasHeight()).andReturn(600f);
        EasyMock.expect(Browser.getPointerState()).andReturn(1f);
        EasyMock.expect(Browser.getPointerX()).andReturn(10f);
        EasyMock.expect(Browser.getPointerY()).andReturn(10f);
        EasyMock.expect(Browser.getKeyState(EasyMock.anyInt())).andReturn(0f).anyTimes();
        Browser.loadImage(EasyMock.anyString(), EasyMock.anyString());
        Browser.loadFont(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Browser.loadAudio(EasyMock.anyString(), EasyMock.anyString());
        Browser.drawRect(EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat(),
            EasyMock.anyFloat(), EasyMock.anyString(), EasyMock.anyFloat());
        EasyMock.expectLastCall().anyTimes();
        Browser.drawImageRegion(EasyMock.anyString(), EasyMock.anyFloat(), EasyMock.anyFloat(),
            EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat(),
            EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyFloat(),
            EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyString());
        EasyMock.expectLastCall().anyTimes();
        Browser.drawPolygon(EasyMock.anyObject(), EasyMock.anyString(), EasyMock.anyFloat());
        Browser.drawText(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(),
            EasyMock.anyString(), EasyMock.anyFloat(), EasyMock.anyFloat(), EasyMock.anyString(),
            EasyMock.anyFloat());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(Browser.getImageWidth(EasyMock.anyString())).andReturn(1000f).anyTimes();
        EasyMock.expect(Browser.getImageHeight(EasyMock.anyString())).andReturn(1000f).anyTimes();
        PowerMock.replay(Browser.class);

        TeaRenderer renderer = new TeaRenderer(Canvas.create(800, 600));
        Application app = new Application(renderer);
        app.changeScene(new DemoApplication(app));
        renderer.onRenderFrame();

        PowerMock.verify(Browser.class);
    }
}
