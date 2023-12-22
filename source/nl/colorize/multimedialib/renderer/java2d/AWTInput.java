//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.util.swing.Popups;
import nl.colorize.util.swing.SwingUtils;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Input device that uses AWT to capture mouse and keyboard events.
 */
public class AWTInput implements InputDevice, KeyListener, MouseListener, MouseMotionListener {

    private Canvas canvas;
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

    private static final Map<KeyCode, Integer> KEY_CODE_MAPPING =
            new ImmutableMap.Builder<KeyCode, Integer>()
        .put(KeyCode.LEFT, KeyEvent.VK_LEFT)
        .put(KeyCode.RIGHT, KeyEvent.VK_RIGHT)
        .put(KeyCode.UP, KeyEvent.VK_UP)
        .put(KeyCode.DOWN, KeyEvent.VK_DOWN)
        .put(KeyCode.ENTER, KeyEvent.VK_ENTER)
        .put(KeyCode.SPACEBAR, KeyEvent.VK_SPACE)
        .put(KeyCode.ESCAPE, KeyEvent.VK_ESCAPE)
        .put(KeyCode.SHIFT, KeyEvent.VK_SHIFT)
        .put(KeyCode.BACKSPACE, KeyEvent.VK_BACK_SPACE)
        .put(KeyCode.TAB, KeyEvent.VK_TAB)
        .put(KeyCode.A, KeyEvent.VK_A)
        .put(KeyCode.B, KeyEvent.VK_B)
        .put(KeyCode.C, KeyEvent.VK_C)
        .put(KeyCode.D, KeyEvent.VK_D)
        .put(KeyCode.E, KeyEvent.VK_E)
        .put(KeyCode.F, KeyEvent.VK_F)
        .put(KeyCode.G, KeyEvent.VK_G)
        .put(KeyCode.H, KeyEvent.VK_H)
        .put(KeyCode.I, KeyEvent.VK_I)
        .put(KeyCode.J, KeyEvent.VK_J)
        .put(KeyCode.K, KeyEvent.VK_K)
        .put(KeyCode.L, KeyEvent.VK_L)
        .put(KeyCode.M, KeyEvent.VK_M)
        .put(KeyCode.N, KeyEvent.VK_N)
        .put(KeyCode.O, KeyEvent.VK_O)
        .put(KeyCode.P, KeyEvent.VK_P)
        .put(KeyCode.Q, KeyEvent.VK_Q)
        .put(KeyCode.R, KeyEvent.VK_R)
        .put(KeyCode.S, KeyEvent.VK_S)
        .put(KeyCode.T, KeyEvent.VK_T)
        .put(KeyCode.U, KeyEvent.VK_U)
        .put(KeyCode.V, KeyEvent.VK_V)
        .put(KeyCode.W, KeyEvent.VK_W)
        .put(KeyCode.X, KeyEvent.VK_X)
        .put(KeyCode.Y, KeyEvent.VK_Y)
        .put(KeyCode.Z, KeyEvent.VK_Z)
        .put(KeyCode.N1, KeyEvent.VK_1)
        .put(KeyCode.N2, KeyEvent.VK_2)
        .put(KeyCode.N3, KeyEvent.VK_3)
        .put(KeyCode.N4, KeyEvent.VK_4)
        .put(KeyCode.N5, KeyEvent.VK_5)
        .put(KeyCode.N6, KeyEvent.VK_6)
        .put(KeyCode.N7, KeyEvent.VK_7)
        .put(KeyCode.N8, KeyEvent.VK_8)
        .put(KeyCode.N9, KeyEvent.VK_9)
        .put(KeyCode.N0, KeyEvent.VK_0)
        .put(KeyCode.F1, KeyEvent.VK_F1)
        .put(KeyCode.F2, KeyEvent.VK_F2)
        .put(KeyCode.F3, KeyEvent.VK_F3)
        .put(KeyCode.F4, KeyEvent.VK_F4)
        .put(KeyCode.F5, KeyEvent.VK_F5)
        .put(KeyCode.F6, KeyEvent.VK_F6)
        .put(KeyCode.F7, KeyEvent.VK_F7)
        .put(KeyCode.F8, KeyEvent.VK_F8)
        .put(KeyCode.F9, KeyEvent.VK_F9)
        .put(KeyCode.F10, KeyEvent.VK_F10)
        .put(KeyCode.F11, KeyEvent.VK_F11)
        .put(KeyCode.F12, KeyEvent.VK_F12)
        .put(KeyCode.PLUS, KeyEvent.VK_PLUS)
        .put(KeyCode.MINUS, KeyEvent.VK_MINUS)
        .put(KeyCode.EQUALS, KeyEvent.VK_EQUALS)
        .build();

    public AWTInput(Canvas canvas) {
        this.canvas = canvas;
        this.eventsBuffer = new CopyOnWriteArrayList<>();

        keyStates = new int[MAX_KEY_CODES];
        mouseX = 0;
        mouseY = 0;
        mouseState = MOUSE_STATE_DEFAULT;
    }
    
    /**
     * Copies all events that have been received during the last frame to this
     * class' internal state. This method must be called every frame.
     */
    @Override
    public void update(float deltaTime) {
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
    public Iterable<Pointer> getPointers() {
        float canvasX = canvas.toCanvasX(mouseX);
        float canvasY = canvas.toCanvasY(mouseY);
        Point2D mouseCanvasPosition = new Point2D(canvasX, canvasY);

        Pointer pointer = new Pointer("mouse");
        pointer.setPosition(mouseCanvasPosition);
        pointer.setPressed(mouseState == MOUSE_STATE_PRESSED);
        pointer.setReleased(mouseState == MOUSE_STATE_RELEASED);
        return List.of(pointer);
    }

    @Override
    public void clearPointerState() {
        mouseState = MOUSE_STATE_DEFAULT;
    }

    @Override
    public boolean isTouchAvailable() {
        return false;
    }

    @Override
    public boolean isKeyboardAvailable() {
        return true;
    }

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
        return isKeyPressed(KEY_CODE_MAPPING.get(keyCode));
    }

    public boolean isKeyPressed(int keycode) {
        return keyStates[keycode] == KEY_STATE_PRESSED;
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        return isKeyReleased(KEY_CODE_MAPPING.get(keyCode));
    }
    
    public boolean isKeyReleased(int keycode) {
        return keyStates[keycode] == KEY_STATE_RELEASED;
    }

    @Override
    public String requestTextInput(String labelText, String initialValue) {
        JLabel label = new JLabel(labelText);
        JTextField field = new JTextField(initialValue);

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        SwingUtils.setPreferredWidth(panel, 300);

        Popups.message(null, "", panel);

        return field.getText();
    }

    @Override
    public void fillClipboard(String text) {
        SwingUtils.copyToClipboard(text);
    }
}
