//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2018 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;

/**
 * Input device that uses AWT to capture mouse and keyboard events.
 */
public class AWTInput implements InputDevice, KeyListener, MouseListener, MouseMotionListener {
    
    private Java2DRenderer renderer;
    
    private List<InputEvent> eventsBuffer;
    
    private int[] keyStates;
    private int mouseX;
    private int mouseY;
    private int mouseState;
    
    private static final int KEY_STATE_DEFAULT = 0;
    private static final int KEY_STATE_PRESSED = 1;
    private static final int KEY_STATE_RELEASED = 2;
    
    private static final int MOUSE_STATE_DEFAULT = 0;
    private static final int MOUSE_STATE_PRESSED = 1;
    private static final int MOUSE_STATE_RELEASED = 2;
    
    private static final int MAX_KEY_CODES = 600;

    public AWTInput(Java2DRenderer renderer) {
        this.renderer = renderer;

        eventsBuffer = Collections.synchronizedList(new ArrayList<InputEvent>());

        keyStates = new int[MAX_KEY_CODES];
        mouseX = 0;
        mouseY = 0;
        mouseState = MOUSE_STATE_DEFAULT;
    }
    
    /**
     * Copies all events that have been received during the last frame to this
     * class' internal state. This method must be called every frame.
     */
    public void refreshFromEventBuffer() {
        InputEvent[] bufferSnapshot = eventsBuffer.toArray(new InputEvent[0]);
        eventsBuffer.clear();
        
        resetState();
        
        for (InputEvent event : bufferSnapshot) {
            if (event instanceof KeyEvent) {
                handleKeyEvent((KeyEvent) event);
            } else if (event instanceof MouseEvent) {
                handleMouseEvent((MouseEvent) event);
            }
        }
    }
    
    private void resetState() {
        for (int i = 0; i < MAX_KEY_CODES; i++) {
            if (keyStates[i] == KEY_STATE_RELEASED) {
                keyStates[i] = KEY_STATE_DEFAULT;
            }
        }
        
        if (mouseState == MOUSE_STATE_RELEASED) {
            mouseState = MOUSE_STATE_DEFAULT;
        }
    }
    
    private void handleKeyEvent(KeyEvent event) {
        int eventID = event.getID();
        
        if (eventID == KeyEvent.KEY_PRESSED) {
            setKeyState(event.getKeyCode(), KEY_STATE_PRESSED);
        } else if (eventID == KeyEvent.KEY_RELEASED) {
            setKeyState(event.getKeyCode(), KEY_STATE_RELEASED);
        }
    }
    
    private void handleMouseEvent(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
        
        int eventID = event.getID();
        if (eventID == MouseEvent.MOUSE_PRESSED) {
            mouseState = MOUSE_STATE_PRESSED;
        } else if (eventID == MouseEvent.MOUSE_RELEASED) {
            mouseState = MOUSE_STATE_RELEASED;
        }    
    }
    
    private void setKeyState(int keycode, int state) {
        if (keycode >= 0 && keycode < MAX_KEY_CODES) {
            keyStates[keycode] = state;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        eventsBuffer.add(e);
        e.consume();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        eventsBuffer.add(e);
        e.consume();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        eventsBuffer.add(e);
        e.consume();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        eventsBuffer.add(e);
        e.consume();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e.consume();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        eventsBuffer.add(e);
        e.consume();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        e.consume();
    }

    @Override
    public Point2D getPointer() {
        Rect screenBounds = renderer.getScreenBounds();
        int pointerOnCanvasX  = renderer.getScaleStrategy().convertToCanvasX(screenBounds, mouseX);
        int pointerOnCanvasY = renderer.getScaleStrategy().convertToCanvasY(screenBounds, mouseY);
        return new Point2D(pointerOnCanvasX, pointerOnCanvasY);
    }

    @Override
    public boolean isPointerPressed() {
        return mouseState == MOUSE_STATE_PRESSED;
    }

    @Override
    public boolean isPointerReleased() {
        return mouseState == MOUSE_STATE_RELEASED;
    }

    @Override
    public boolean isKeyboardAvailable() {
        return true;
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        //TODO
        return false;
    }

    public boolean isKeyPressed(int keycode) {
        return keyStates[keycode] == KEY_STATE_PRESSED;
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        //TODO
        return false;
    }
    
    public boolean isKeyReleased(int keycode) {
        return keyStates[keycode] == KEY_STATE_RELEASED;
    }
}
