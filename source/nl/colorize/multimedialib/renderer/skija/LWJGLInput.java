//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.skija;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.renderer.InputDevice;
import nl.colorize.multimedialib.renderer.KeyCode;
import nl.colorize.multimedialib.renderer.Pointer;
import nl.colorize.multimedialib.renderer.RenderConfig;
import nl.colorize.util.EventQueue;
import nl.colorize.util.LogHelper;
import nl.colorize.util.Platform;
import nl.colorize.util.Subject;
import nl.colorize.util.swing.Popups;
import nl.colorize.util.swing.SwingUtils;
import org.lwjgl.glfw.GLFW;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implements mouse and keyboard input using LWJGL callback methods. Skija is
 * purely a graphics library and provides no input of its own, so the Skija
 * renderer relies on LWJGL for platform integration.
 */
public class LWJGLInput implements InputDevice {

    private RenderConfig config;
    private Pointer mouse;
    private Set<KeyCode> keysPressed;
    private Set<KeyCode> keysReleased;

    private static final Map<Integer, KeyCode> KEY_MAPPING = ImmutableMap.<Integer, KeyCode>builder()
        .put(GLFW.GLFW_KEY_LEFT, KeyCode.LEFT)
        .put(GLFW.GLFW_KEY_RIGHT, KeyCode.RIGHT)
        .put(GLFW.GLFW_KEY_UP, KeyCode.UP)
        .put(GLFW.GLFW_KEY_DOWN, KeyCode.DOWN)
        .put(GLFW.GLFW_KEY_ENTER, KeyCode.ENTER)
        .put(GLFW.GLFW_KEY_SPACE, KeyCode.SPACEBAR)
        .put(GLFW.GLFW_KEY_ESCAPE, KeyCode.ESCAPE)
        .put(GLFW.GLFW_KEY_LEFT_SHIFT, KeyCode.SHIFT)
        .put(GLFW.GLFW_KEY_BACKSPACE, KeyCode.BACKSPACE)
        .put(GLFW.GLFW_KEY_TAB, KeyCode.TAB)
        .put(GLFW.GLFW_KEY_A, KeyCode.A)
        .put(GLFW.GLFW_KEY_B, KeyCode.B)
        .put(GLFW.GLFW_KEY_C, KeyCode.C)
        .put(GLFW.GLFW_KEY_D, KeyCode.D)
        .put(GLFW.GLFW_KEY_E, KeyCode.E)
        .put(GLFW.GLFW_KEY_F, KeyCode.F)
        .put(GLFW.GLFW_KEY_G, KeyCode.G)
        .put(GLFW.GLFW_KEY_H, KeyCode.H)
        .put(GLFW.GLFW_KEY_I, KeyCode.I)
        .put(GLFW.GLFW_KEY_J, KeyCode.J)
        .put(GLFW.GLFW_KEY_K, KeyCode.K)
        .put(GLFW.GLFW_KEY_L, KeyCode.L)
        .put(GLFW.GLFW_KEY_M, KeyCode.M)
        .put(GLFW.GLFW_KEY_N, KeyCode.N)
        .put(GLFW.GLFW_KEY_O, KeyCode.O)
        .put(GLFW.GLFW_KEY_P, KeyCode.P)
        .put(GLFW.GLFW_KEY_Q, KeyCode.Q)
        .put(GLFW.GLFW_KEY_R, KeyCode.R)
        .put(GLFW.GLFW_KEY_S, KeyCode.S)
        .put(GLFW.GLFW_KEY_T, KeyCode.T)
        .put(GLFW.GLFW_KEY_U, KeyCode.U)
        .put(GLFW.GLFW_KEY_V, KeyCode.V)
        .put(GLFW.GLFW_KEY_W, KeyCode.W)
        .put(GLFW.GLFW_KEY_X, KeyCode.X)
        .put(GLFW.GLFW_KEY_Y, KeyCode.Y)
        .put(GLFW.GLFW_KEY_Z, KeyCode.Z)
        .put(GLFW.GLFW_KEY_1, KeyCode.N1)
        .put(GLFW.GLFW_KEY_2, KeyCode.N2)
        .put(GLFW.GLFW_KEY_3, KeyCode.N3)
        .put(GLFW.GLFW_KEY_4, KeyCode.N4)
        .put(GLFW.GLFW_KEY_5, KeyCode.N5)
        .put(GLFW.GLFW_KEY_6, KeyCode.N6)
        .put(GLFW.GLFW_KEY_7, KeyCode.N7)
        .put(GLFW.GLFW_KEY_8, KeyCode.N8)
        .put(GLFW.GLFW_KEY_9, KeyCode.N9)
        .put(GLFW.GLFW_KEY_0, KeyCode.N0)
        .put(GLFW.GLFW_KEY_F1, KeyCode.F1)
        .put(GLFW.GLFW_KEY_F2, KeyCode.F2)
        .put(GLFW.GLFW_KEY_F3, KeyCode.F3)
        .put(GLFW.GLFW_KEY_F4, KeyCode.F4)
        .put(GLFW.GLFW_KEY_F5, KeyCode.F5)
        .put(GLFW.GLFW_KEY_F6, KeyCode.F6)
        .put(GLFW.GLFW_KEY_F7, KeyCode.F7)
        .put(GLFW.GLFW_KEY_F8, KeyCode.F8)
        .put(GLFW.GLFW_KEY_F9, KeyCode.F9)
        .put(GLFW.GLFW_KEY_F10, KeyCode.F10)
        .put(GLFW.GLFW_KEY_F11, KeyCode.F11)
        .put(GLFW.GLFW_KEY_F12, KeyCode.F12)
        .put(GLFW.GLFW_KEY_COMMA, KeyCode.COMMA)
        .put(GLFW.GLFW_KEY_PERIOD, KeyCode.PERIOD)
        .put(GLFW.GLFW_KEY_EQUAL, KeyCode.EQUALS)
        .put(GLFW.GLFW_KEY_MINUS, KeyCode.MINUS)
        .build();

    private static final CharMatcher APPLE_SCRIPT_QUOTES = CharMatcher.anyOf("\"'");
    private static final Logger LOGGER = LogHelper.getLogger(LWJGLInput.class);

    public LWJGLInput(RenderConfig config) {
        this.config = config;
        this.mouse = new Pointer("mouse");
        this.keysPressed = new HashSet<>();
        this.keysReleased = new HashSet<>();
    }

    protected void reset() {
        if (mouse.getState() == Pointer.STATE_RELEASED) {
            mouse.setState(Pointer.STATE_IDLE);
        }

        keysReleased.clear();
    }

    protected void onMouseMove(long windowId, double x, double y) {
        float screenPixelRatio = Platform.isMac() ? config.getCanvas().getScreenPixelRatio() : 1f;
        int screenX = (int) Math.round(x * screenPixelRatio);
        int screenY = (int) Math.round(y * screenPixelRatio);
        float canvasX = config.getCanvas().toCanvasX(screenX);
        float canvasY = config.getCanvas().toCanvasY(screenY);
        mouse.setPosition(new Point2D(canvasX, canvasY));
    }

    protected void onMouseButton(long windowId, int button, int action, int mods) {
        if (action == 0) {
            mouse.setState(Pointer.STATE_RELEASED);
        } else {
            mouse.setState(Pointer.STATE_PRESSED);
        }
    }

    protected void onKey(long windowId, int key, int scancode, int action, int mods) {
        KeyCode keyCode = KEY_MAPPING.get(key);

        if (keyCode != null) {
            if (action == GLFW.GLFW_PRESS) {
                keysPressed.add(keyCode);
                keysReleased.remove(keyCode);
            } else if (action == GLFW.GLFW_RELEASE) {
                keysReleased.add(keyCode);
                keysPressed.remove(keyCode);
            }
        }
    }

    @Override
    public Iterable<Pointer> getPointers() {
        return List.of(mouse);
    }

    @Override
    public void clearPointerState() {
        mouse.setState(Pointer.STATE_IDLE);
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
        return keysPressed.contains(keyCode);
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
        return keysReleased.contains(keyCode);
    }

    @Override
    public EventQueue<String> requestTextInput(String label, String initialValue) {
        if (Platform.isMac()) {
            return requestAppleScriptInput(label, initialValue);
        } else {
            return requestSwingInput(label, initialValue);
        }
    }

    private EventQueue<String> requestSwingInput(String labelText, String initialValue) {
        JLabel label = new JLabel(labelText);
        JTextField field = new JTextField(initialValue);

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        SwingUtils.setPreferredWidth(panel, 300);

        Popups.message(null, "", panel);

        Subject<String> subject = new Subject<>();
        if (field.getText() != null && !field.getText().isEmpty()) {
            subject.next(field.getText());
        }

        return EventQueue.subscribe(subject);
    }

    private EventQueue<String> requestAppleScriptInput(String label, String initialValue) {
        Subject<String> subject = new Subject<>();

        String script = String.format("display dialog \"%s\" default answer \"%s\"",
            APPLE_SCRIPT_QUOTES.removeFrom(label), APPLE_SCRIPT_QUOTES.removeFrom(initialValue));

        try {
            Process process = new ProcessBuilder("osascript", "-e", script)
                .start();

            if (process.waitFor() == 0) {
                String output = new String(process.getInputStream().readAllBytes(), UTF_8);
                if (output.contains("text returned:")) {
                    String result = Splitter.on("text returned:").splitToList(output).getLast().trim();
                    subject.next(result);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Requesting text input via Apple Script failed", e);
        }

        return EventQueue.subscribe(subject);
    }

    @Override
    public void fillClipboard(String text) {
        SwingUtils.copyToClipboard(text);
    }

    @Override
    public void update(float deltaTime) {
    }
}
