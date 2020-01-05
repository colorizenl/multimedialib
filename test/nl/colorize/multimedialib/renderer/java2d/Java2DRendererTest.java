//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.scene.Application;
import nl.colorize.multimedialib.tool.DemoApplication;
import nl.colorize.util.swing.Utils2D;
import org.easymock.EasyMock;
import org.junit.Test;

import javax.swing.JFrame;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class Java2DRendererTest {

    @Test
    public void testDemoApplication() throws InterruptedException {
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Utils2D.createGraphics(image, false, false);

        BufferStrategy bufferStrategy = EasyMock.createMock(BufferStrategy.class);
        EasyMock.expect(bufferStrategy.getDrawGraphics()).andReturn(g2).anyTimes();
        EasyMock.expect(bufferStrategy.contentsLost()).andReturn(true).anyTimes();
        EasyMock.replay(bufferStrategy);

        JFrame mockWindow = EasyMock.createMock(JFrame.class);
        mockWindow.addMouseListener(EasyMock.anyObject());
        mockWindow.addMouseMotionListener(EasyMock.anyObject());
        mockWindow.addKeyListener(EasyMock.anyObject());
        mockWindow.createBufferStrategy(2);
        EasyMock.expect(mockWindow.getBufferStrategy()).andReturn(bufferStrategy);
        EasyMock.expect(mockWindow.getInsets()).andReturn(new Insets(0, 0, 0, 0));
        EasyMock.expect(mockWindow.getWidth()).andReturn(800).anyTimes();
        EasyMock.expect(mockWindow.getHeight()).andReturn(600).anyTimes();
        mockWindow.dispose();
        EasyMock.replay(mockWindow);

        Java2DRenderer renderer = new Java2DRenderer(Canvas.create(800, 600), 60,
            new WindowOptions("test")) {
            @Override
            protected JFrame initializeWindow(WindowOptions windowOptions) {
                // Do not create an actual window so that the tests can also
                // run in headless environments like BitBucket pipelines.
                return mockWindow;
            }
        };
        Application app = new Application(renderer);
        DemoApplication demo = new DemoApplication(app);
        app.changeScene(demo);

        Thread.sleep(1000);

        renderer.terminate();
        g2.dispose();
        EasyMock.verify(mockWindow);
    }
}
