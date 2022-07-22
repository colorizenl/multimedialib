//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2022 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import com.google.common.base.Charsets;
import nl.colorize.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CopyrightUpdateToolTest {

    @Test
    public void testUpdateCopyrightYear() {
        CopyrightUpdateTool tool = new CopyrightUpdateTool();
        tool.startCopyrightYear = "2020";
        tool.newCopyrightYear = "2022";

        assertEquals("Copyright 2022 Colorize", tool.processLine("Copyright 2020 Colorize"));
        assertEquals("Copyright 2022 Colorize", tool.processLine("Copyright 2019 Colorize"));
    }

    @Test
    public void testUpdateCopyrightMultiYearLeaveStart() {
        CopyrightUpdateTool tool = new CopyrightUpdateTool();
        tool.startCopyrightYear = "leave";
        tool.newCopyrightYear = "2022";

        assertEquals("Copyright 2019-2022 Colorize", tool.processLine("Copyright 2019-2020 Colorize"));
        assertEquals("Copyright 2008-2022 Colorize", tool.processLine("Copyright 2008-2020 Colorize"));
        assertEquals("Copyright 2008-2022 Colorize", tool.processLine("Copyright 2008-2019 Colorize"));
        assertEquals("Copyright 2007, 2022 Colorize", tool.processLine("Copyright 2007, 2020 Colorize"));
        assertEquals("Copyright 2007, 2022 Colorize", tool.processLine("Copyright 2007, 2019 Colorize"));
    }

    @Test
    public void testUpdateCopyrightMultiYearReplaceStart() {
        CopyrightUpdateTool tool = new CopyrightUpdateTool();
        tool.startCopyrightYear = "2007";
        tool.newCopyrightYear = "2022";

        assertEquals("Copyright 2007-2022 Colorize", tool.processLine("Copyright 2010-2020 Colorize"));
        assertEquals("Copyright 2007, 2022 Colorize", tool.processLine("Copyright 2011, 2019 Colorize"));
    }

    @Test
    public void testRewriteLicenseURL() {
        CopyrightUpdateTool tool = new CopyrightUpdateTool();
        tool.startCopyrightYear = "2020";
        tool.newCopyrightYear = "2022";
        tool.license = "test";

        assertEquals("// test", tool.processLine("// Apache license"));
    }

    @Test
    void processAllFilesInDirectory(@TempDir File tempDir) throws IOException {
        String example = "";
        example += "# Copyright 2019-2020 Colorize\n";
        example += "\n";
        example += "print('Hello world')\n";

        FileUtils.write(example, Charsets.UTF_8, new File(tempDir, "a.py"));

        CopyrightUpdateTool tool = new CopyrightUpdateTool();
        tool.dir = tempDir;
        tool.startCopyrightYear = "leave";
        tool.newCopyrightYear = "2022";
        tool.run();

        String expected = "";
        expected += "# Copyright 2019-2022 Colorize\n";
        expected += "\n";
        expected += "print('Hello world')\n";

        assertEquals(expected, FileUtils.read(new File(tempDir, "a.py"), Charsets.UTF_8));
    }
}
