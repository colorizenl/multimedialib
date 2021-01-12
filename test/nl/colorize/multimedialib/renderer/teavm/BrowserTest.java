//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrowserTest {

    private static final List<Class<?>> ALLOWED_TYPES = ImmutableList.of(
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
        AnimationFrameCallback.class,
        AjaxCallback.class,
        ConnectionCallback.class,
        ModelLoadCallback.class
    );

    @Test
    public void testAllParametersHaveCompatibleTypes() {
        for (Method method : Browser.class.getDeclaredMethods()) {
            for (Class<?> type : method.getParameterTypes()) {
                assertTrue(ALLOWED_TYPES.contains(type) || type.getName().startsWith("java.lang"),
                    "Parameter types is not allowed by TeaVM: " + type);
            }
        }
    }

    @Test
    public void testAllReturnTypesAreCompatible() {
        for (Method method : Browser.class.getDeclaredMethods()) {
            Class<?> returnType = method.getReturnType();
            assertTrue(ALLOWED_TYPES.contains(returnType),
                "Return type is not allowed by TeaVM: " + returnType);
        }
    }
}
