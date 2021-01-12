//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecorderTest {

    @Test
    void record() {
        Recorder recorder = new Recorder(0.1f, 10);
        recorder.record("test", () -> 123f);

        assertEquals(ImmutableList.of(), recorder.getRecordedValues("test"));

        recorder.update(0.07f);
        recorder.update(0.07f);
        recorder.update(0.07f);

        assertEquals(ImmutableList.of(123f, 123f), recorder.getRecordedValues("test"));
    }

    @Test
    void exceedCapacity() {
        List<String> values = new ArrayList<>();

        Recorder recorder = new Recorder(0.1f, 3);
        recorder.record("test", () -> {
            values.add("123");
            return (float) values.size();
        });

        assertEquals(ImmutableList.of(), recorder.getRecordedValues("test"));

        recorder.update(0.1f);
        recorder.update(0.1f);
        recorder.update(0.1f);
        recorder.update(0.1f);

        assertEquals(4, values.size());
        assertEquals(ImmutableList.of(2f, 3f, 4f), recorder.getRecordedValues("test"));
    }
}
