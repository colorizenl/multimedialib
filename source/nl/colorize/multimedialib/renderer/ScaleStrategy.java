//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.math.Rect;

/**
 * Influences how and if the renderer scales graphics when the canvas and the
 * screen are not of the same size. This is especially relevant if the application
 * will be used on devices with different screen sizes and aspect ratios.
 */
public class ScaleStrategy {
    
    private int canvasWidth;
    private int canvasHeight;
    private boolean flexible;
    private boolean proportional;
    
    private ScaleStrategy(int canvasWidth, int canvasHeight, boolean flexible, boolean proportional) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.flexible = flexible;
        this.proportional = proportional;
    }
    
    /**
     * Returns the (non-scaled) width of the canvas used by this scale strategy. 
     */
    public int getCanvasWidth(Rect screen) {
        return flexible ? screen.getWidth() : canvasWidth;
    }
    
    /**
     * Returns the (non-scaled) height of the canvas used by this scale strategy. 
     */
    public int getCanvasHeight(Rect screen) {
        return flexible ? screen.getHeight() : canvasHeight;
    }
    
    public Rect getPreferredCanvasBounds() {
        return new Rect(0, 0, canvasWidth, canvasHeight);
    }
    
    /**
     * Returns the size and position of the canvas when applying this scale
     * strategy to the specified screen.
     */
    public Rect getCanvasBounds(Rect screen) {
        if (flexible) {
            return screen;
        }
        
        int canvasX = getScaledCanvasX(screen);
        int canvasY = getScaledCanvasY(screen);
        int scaledCanvasWidth = getScaledCanvasWidth(screen);
        int scaledCanvasHeight = getScaledCanvasHeight(screen);
        return new Rect(canvasX, canvasY, scaledCanvasWidth, scaledCanvasHeight);
    }
    
    private int getScaledCanvasX(Rect screen) {
        if (flexible) {
            return screen.getX();
        }
        return (screen.getWidth() - getScaledCanvasWidth(screen)) / 2 + screen.getX();
    }
    
    private int getScaledCanvasY(Rect screen) {
        if (flexible) {
            return screen.getY();
        }
        return (screen.getHeight() - getScaledCanvasHeight(screen)) / 2 + screen.getY();
    }
    
    int getScaledCanvasWidth(Rect screen) {
        if (flexible) {
            return screen.getWidth();
        }
        return Math.round(canvasWidth * getScaleFactorX(screen));
    }
    
    int getScaledCanvasHeight(Rect screen) {
        if (flexible) {
            return screen.getHeight();
        }
        return Math.round(canvasHeight * getScaleFactorY(screen));
    }
    
    public float getScaleFactorX(Rect screen) {
        if (flexible) {
            return 1f;
        }
        
        int scaledCanvasWidth = screen.getWidth();
        if (proportional) {
            float canvasAspectRatio = MathUtils.getAspectRatio(canvasWidth, canvasHeight);
            float screenAspectRatio = MathUtils.getAspectRatio(screen);
            if (canvasAspectRatio < screenAspectRatio) {
                scaledCanvasWidth = Math.round(screen.getHeight() * canvasAspectRatio);
            }
        }
        
        return (float) scaledCanvasWidth / (float) canvasWidth;
    }
    
    public float getScaleFactorY(Rect screen) {
        if (flexible) {
            return 1f;
        }
        
        int scaledCanvasHeight = screen.getHeight();
        if (proportional) {
            float canvasAspectRatio = MathUtils.getAspectRatio(canvasWidth, canvasHeight);
            float screenAspectRatio = MathUtils.getAspectRatio(screen);
            if (canvasAspectRatio >= screenAspectRatio) {
                scaledCanvasHeight = Math.round(screen.getWidth() * (1f / canvasAspectRatio));
            }
        }
        
        return (float) scaledCanvasHeight / (float) canvasHeight;
    }
    
    public int convertToScreenX(Rect screen, int canvasX) {
        float scaleFactor = getScaleFactorX(screen);
        int offset = getScaledCanvasX(screen);
        return Math.round(canvasX * scaleFactor + offset);
    }
    
    public int convertToScreenY(Rect screen, int canvasY) {
        float scaleFactor = getScaleFactorY(screen);
        int offset = getScaledCanvasY(screen);
        return Math.round(canvasY * scaleFactor + offset);
    }
    
    public int convertToCanvasX(Rect screen, int screenX) {
        float scaleFactor = getScaleFactorX(screen);
        int offset = getScaledCanvasX(screen);
        return Math.round((screenX - offset) / scaleFactor);
    }
    
    public int convertToCanvasY(Rect screen, int screenY) {
        float scaleFactor = getScaleFactorY(screen);
        int offset = getScaledCanvasY(screen);
        return Math.round((screenY - offset) / scaleFactor);
    }

    /**
     * Performs no scaling so that the canvas size always matches the screen size.
     */
    public static ScaleStrategy flexible(int initialCanvasWidth, int initialCanvasHeight) {
        return new ScaleStrategy(initialCanvasWidth, initialCanvasHeight, true, false);
    }
    
    /**
     * Does not scale the canvas, keeping it at the specified size regardless of
     * the size of the screen. Black borders will appear if the screen is larger
     * than the canvas.
     */
    public static ScaleStrategy fixed(int canvasWidth, int canvasHeight) {
        return new ScaleStrategy(canvasWidth, canvasHeight, false, false) {
            @Override
            public float getScaleFactorX(Rect screen) {
                return 1f;
            }
            
            @Override
            public float getScaleFactorY(Rect screen) {
                return 1f;
            }
        };
    }
    
    /**
     * Stretches the canvas to match the screen size. The canvas may be stretched
     * or squashed if it has a different aspect ratio than the screen.
     */
    public static ScaleStrategy stretch(int canvasWidth, int canvasHeight) {
        return new ScaleStrategy(canvasWidth, canvasHeight, false, false);
    }
    
    /**
     * Scales the canvas to match the screen size, but without changing its aspect
     * ratio. Black borders may appear if the screen has a different aspect ratio.
     */
    public static ScaleStrategy proportional(int canvasWidth, int canvasHeight) {
        return new ScaleStrategy(canvasWidth, canvasHeight, false, true);
    }
    
    /**
     * Scales the canvas to match the screen size. However, when the screen aspect
     * ratio is different, instead of showing black borders like 
     * {@link #proportional(int, int)} it will increase the canvas to fill the 
     * remaining space.
     */
    public static ScaleStrategy smart(int canvasWidth, int canvasHeight) {
        return new ScaleStrategy(canvasWidth, canvasHeight, false, true) {
            @Override
            public int getCanvasWidth(Rect screen) {
                float screenAspectRatio = MathUtils.getAspectRatio(screen);
                return Math.round(getCanvasHeight(screen) * screenAspectRatio);
            }
            
            @Override
            int getScaledCanvasWidth(Rect screen) {
                return screen.getWidth();
            }
            
            @Override
            int getScaledCanvasHeight(Rect screen) {
                return screen.getHeight();
            }
        };
    }
}
