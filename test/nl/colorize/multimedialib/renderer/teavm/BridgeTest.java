//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import nl.colorize.multimedialib.renderer.pixi.PixiBridge;
import nl.colorize.multimedialib.renderer.three.ThreeBridge;
import org.junit.jupiter.api.Test;
import org.teavm.jso.JSObject;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Combined tests for all TeaVM "bridge" classes, to make sure they comply to
 * the TeaVM rules for Java/JavaScript compatibility.
 */
public class BridgeTest {

    private static final List<Class<?>> BRIDGE_CLASSES = List.of(
        Browser.class,
        BrowserBridge.class,
        PeerjsBridge.class,
        PixiBridge.class,
        ThreeBridge.class
    );

    private static final List<Class<?>> ALLOWED_TYPES = List.of(
        void.class,
        int.class,
        int[].class,
        float.class,
        float[].class,
        boolean.class,
        boolean[].class,
        double.class,
        double[].class,
        String.class,
        String[].class,
        JSObject.class
    );

    @Test
    public void testAllParametersHaveCompatibleTypes() {
        for (Class<?> bridgeClass : BRIDGE_CLASSES) {
            for (Method method : bridgeClass.getDeclaredMethods()) {
                for (Class<?> parameterType : method.getParameterTypes()) {
                    assertTrue(isAllowedType(parameterType),
                        "Parameter types is not allowed by TeaVM: " + parameterType);
                }
            }
        }
    }

    @Test
    public void testAllReturnTypesAreCompatible() {
        for (Class<?> bridgeClass : BRIDGE_CLASSES) {
            for (Method method : bridgeClass.getDeclaredMethods()) {
                Class<?> returnType = method.getReturnType();
                assertTrue(isAllowedType(returnType),
                    "Return type is not allowed by TeaVM: " + returnType);
            }
        }
    }

    private boolean isAllowedType(Class<?> type) {
        if (type.getName().startsWith("java.lang.")) {
            return true;
        }

        return ALLOWED_TYPES.stream()
            .anyMatch(allowed -> allowed.isAssignableFrom(type));
    }
}
