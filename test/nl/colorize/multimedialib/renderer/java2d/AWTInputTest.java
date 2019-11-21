//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2019 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.java2d;

import nl.colorize.multimedialib.renderer.KeyCode;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class AWTInputTest {

    @Test
    public void testAllKeyCodesAreMapped() throws Exception {
        Field constant = AWTInput.class.getDeclaredField("KEY_CODE_MAPPING");
        constant.setAccessible(true);
        Map<KeyCode, Integer> mapping = (Map<KeyCode, Integer>) constant.get(null);

        List<KeyCode> culprits = Arrays.stream(KeyCode.values())
            .filter(keyCode -> !mapping.containsKey(keyCode))
            .collect(Collectors.toList());

        assertTrue("Not all key codes have been mapped: " + culprits, culprits.isEmpty());
    }
}