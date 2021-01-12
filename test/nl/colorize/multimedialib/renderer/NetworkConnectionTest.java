//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkConnectionTest {

    @Test
    void sendingMessagesAfterConnect() {
        List<String> sent = new ArrayList<>();
        NetworkConnection connection = new NetworkConnection(sent::add);
        connection.send("123");
        connection.send("456");

        assertEquals(Collections.emptyList(), sent);

        connection.connect();
        assertEquals(ImmutableList.of("123", "456"), sent);

        connection.send("789");
        assertEquals(ImmutableList.of("123", "456", "789"), sent);
    }
}
