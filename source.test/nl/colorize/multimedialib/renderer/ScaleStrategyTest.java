//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.Rect;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for the {@code ScaleStrategy} class.
 */
public class ScaleStrategyTest {
        
    private static ScaleStrategy flexible = ScaleStrategy.flexible(800, 600);
    private static ScaleStrategy fixed = ScaleStrategy.fixed(800, 600);
    private static ScaleStrategy stretch = ScaleStrategy.stretch(800, 600);
    private static ScaleStrategy proportional = ScaleStrategy.proportional(800, 600);
    private static ScaleStrategy smart = ScaleStrategy.smart(800, 600);

    @Test
    public void testGetCanvasBounds() {
        Rect same = new Rect(0, 0, 800, 600);
        Rect other = new Rect(0, 0, 1024, 768);
        Rect otherAspectRatio = new Rect(0, 0, 480, 800);
        
        assertEquals(new Rect(0, 0, 800, 600), flexible.getCanvasBounds(same));
        assertEquals(new Rect(0, 0, 1024, 768), flexible.getCanvasBounds(other));
        assertEquals(new Rect(0, 0, 480, 800), flexible.getCanvasBounds(otherAspectRatio));
        
        assertEquals(new Rect(0, 0, 800, 600), fixed.getCanvasBounds(same));
        assertEquals(new Rect(112, 84, 800, 600), fixed.getCanvasBounds(other));
        assertEquals(new Rect(-160, 100, 800, 600), fixed.getCanvasBounds(otherAspectRatio));
        
        assertEquals(new Rect(0, 0, 800, 600), stretch.getCanvasBounds(same));
        assertEquals(new Rect(0, 0, 1024, 768), stretch.getCanvasBounds(other));
        assertEquals(new Rect(0, 0, 480, 800), stretch.getCanvasBounds(otherAspectRatio));
        
        assertEquals(new Rect(0, 0, 800, 600), proportional.getCanvasBounds(same));
        assertEquals(new Rect(0, 0, 1024, 768), proportional.getCanvasBounds(other));
        assertEquals(new Rect(0, 220, 480, 360), proportional.getCanvasBounds(otherAspectRatio));
        
        assertEquals(new Rect(0, 0, 800, 600), smart.getCanvasBounds(same));
        assertEquals(new Rect(0, 0, 1024, 768), smart.getCanvasBounds(other));
        assertEquals(new Rect(0, 0, 480, 800), smart.getCanvasBounds(otherAspectRatio));
    }
    
    @Test
    public void testConvertToCanvas() {
        Rect screen = new Rect(0, 0, 1280, 800);
        assertEquals(320, flexible.convertToCanvasX(screen, 320));
        assertEquals(80, fixed.convertToCanvasX(screen, 320));
        assertEquals(200, stretch.convertToCanvasX(screen, 320));
        assertEquals(160, proportional.convertToCanvasX(screen, 320));
        
        assertEquals(0, smart.convertToCanvasX(screen, 0));
        assertEquals(480, smart.convertToCanvasX(screen, 640));
        assertEquals(960, smart.convertToCanvasX(screen, 1280));
        assertEquals(0, smart.convertToCanvasY(screen, 0));
        assertEquals(599, smart.convertToCanvasY(screen, 799));
    }
    
    @Test
    public void testConvertToScreen() {
        Rect screen = new Rect(0, 0, 1280, 800);
        assertEquals(600, flexible.convertToScreenX(screen, 600));
        assertEquals(840, fixed.convertToScreenX(screen, 600));
        assertEquals(960, stretch.convertToScreenX(screen, 600));
        
        assertEquals(106, proportional.convertToScreenX(screen, 0));
        assertEquals(239, proportional.convertToScreenX(screen, 100));
        assertEquals(366, proportional.convertToScreenX(new Rect(0, 0, 1000, 200), 0));
        assertEquals(633, proportional.convertToScreenX(new Rect(0, 0, 1000, 200), 800));
        
        assertEquals(0, smart.convertToScreenX(screen, 0));
        assertEquals(534, smart.convertToScreenX(screen, 400));
        assertEquals(1067, smart.convertToScreenX(screen, 800));
        assertEquals(1280, smart.convertToScreenX(screen, 960));
        assertEquals(0, smart.convertToScreenY(screen, 0));
        assertEquals(1065, smart.convertToScreenY(screen, 799));
    }
    
    @Test
    public void testDynamicCanvasSize() {
        assertEquals(800, proportional.getCanvasWidth(new Rect(0, 0, 400, 300)));
        assertEquals(800, proportional.getCanvasWidth(new Rect(0, 0, 1280, 800)));
        
        assertEquals(400, flexible.getCanvasWidth(new Rect(0, 0, 400, 300)));
        assertEquals(300, flexible.getCanvasHeight(new Rect(0, 0, 400, 300)));
        assertEquals(1280, flexible.getCanvasWidth(new Rect(0, 0, 1280, 800)));
        assertEquals(800, flexible.getCanvasHeight(new Rect(0, 0, 1280, 800)));
        
        assertEquals(800, smart.getCanvasWidth(new Rect(0, 0, 400, 300)));
        assertEquals(600, smart.getCanvasHeight(new Rect(0, 0, 400, 300)));
        assertEquals(960, smart.getCanvasWidth(new Rect(0, 0, 1280, 800)));
        assertEquals(600, smart.getCanvasHeight(new Rect(0, 0, 1280, 800)));
    }
    
    @Test
    public void testStringForm() {
        assertEquals("flexible", flexible.toString());
        assertEquals("fixed", fixed.toString());
        assertEquals("stretch", stretch.toString());
        assertEquals("proportional", proportional.toString());
        assertEquals("smart", smart.toString());
    }
}
