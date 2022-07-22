//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.teavm.jso.JSObject;

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
        JSObject.class
    );

    @Test
    public void testAllParametersHaveCompatibleTypes() {
        for (Method method : Browser.class.getDeclaredMethods()) {
            for (Class<?> parameterType : method.getParameterTypes()) {
                assertTrue(isAllowedType(parameterType),
                    "Parameter types is not allowed by TeaVM: " + parameterType);
            }
        }
    }

    @Test
    public void testAllReturnTypesAreCompatible() {
        for (Method method : Browser.class.getDeclaredMethods()) {
            Class<?> returnType = method.getReturnType();
            assertTrue(isAllowedType(returnType),
                "Return type is not allowed by TeaVM: " + returnType);
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
