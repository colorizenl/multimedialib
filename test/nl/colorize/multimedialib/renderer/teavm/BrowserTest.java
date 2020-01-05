//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.teavm;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class BrowserTest {

    private static final List<Class<?>> ALLOWED_TYPES = ImmutableList.of(
        void.class,
        int.class,
        int[].class,
        float.class,
        float[].class,
        boolean.class,
        boolean[].class,
        String.class,
        String[].class,
        AnimationFrameCallback.class
    );

    @Test
    public void testAllParametersHaveCompatibleTypes() {
        for (Method method : Browser.class.getDeclaredMethods()) {
            for (Class<?> type : method.getParameterTypes()) {
                assertTrue("Parameter types is not allowed by TeaVM: " + type,
                    ALLOWED_TYPES.contains(type) || type.getName().startsWith("java.lang"));
            }
        }
    }

    @Test
    public void testAllReturnTypesAreCompatible() {
        for (Method method : Browser.class.getDeclaredMethods()) {
            Class<?> returnType = method.getReturnType();
            assertTrue("Return type is not allowed by TeaVM: " + returnType,
                ALLOWED_TYPES.contains(returnType));
        }
    }
}
