//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CopyrightUpdateToolTest {

    @Test
    public void testUpdateCopyrightYear() {
        CopyrightUpdateTool tool = new CopyrightUpdateTool();
        tool.startCopyrightYear = "2020";
        tool.newCopyrightYear = "2020";

        assertEquals("Copyright 2020 Colorize", tool.processLine("Copyright 2020 Colorize"));
        assertEquals("Copyright 2020 Colorize", tool.processLine("Copyright 2019 Colorize"));
    }

    @Test
    public void testUpdateCopyrightMultiYearLeaveStart() {
        CopyrightUpdateTool tool = new CopyrightUpdateTool();
        tool.startCopyrightYear = "leave";
        tool.newCopyrightYear = "2020";

        assertEquals("Copyright 2019-2020 Colorize", tool.processLine("Copyright 2019-2020 Colorize"));
        assertEquals("Copyright 2008-2020 Colorize", tool.processLine("Copyright 2008-2020 Colorize"));
        assertEquals("Copyright 2008-2020 Colorize", tool.processLine("Copyright 2008-2019 Colorize"));
        assertEquals("Copyright 2007, 2020 Colorize", tool.processLine("Copyright 2007, 2020 Colorize"));
        assertEquals("Copyright 2007, 2020 Colorize", tool.processLine("Copyright 2007, 2019 Colorize"));
    }

    @Test
    public void testUpdateCopyrightMultiYearReplaceStart() {
        CopyrightUpdateTool tool = new CopyrightUpdateTool();
        tool.startCopyrightYear = "2007";
        tool.newCopyrightYear = "2020";

        assertEquals("Copyright 2007-2020 Colorize", tool.processLine("Copyright 2010-2020 Colorize"));
        assertEquals("Copyright 2007, 2020 Colorize", tool.processLine("Copyright 2011, 2019 Colorize"));
    }

    @Test
    public void testRewriteLicenseURL() {
        CopyrightUpdateTool tool = new CopyrightUpdateTool();
        tool.startCopyrightYear = "2020";
        tool.newCopyrightYear = "2020";
        tool.license = "test";

        assertEquals("// test", tool.processLine("// Apache license"));
    }
}
