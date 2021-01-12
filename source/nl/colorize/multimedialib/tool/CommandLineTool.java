//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import nl.colorize.util.LogHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Skeleton implementation for tools that provide a command line interface,
 * where the command line arguments are parsed using Args4j. Subclasses should
 * provide a {@code main} method that calls {@link #start(String[])}. This
 * method will parse the command line arguments, and when valid start the tool
 * by calling {@link #run()}.
 */
public abstract class CommandLineTool {
        
    private PrintStream usageStream;
    
    private static final Logger LOGGER = LogHelper.getLogger(CommandLineTool.class);

    public CommandLineTool() {
        this.usageStream = System.err;
    }
    
    public final void start(String[] args) {
        CmdLineParser cmdParser = new CmdLineParser(this);
        cmdParser.getProperties().withUsageWidth(80);
        
        try {
            cmdParser.parseArgument(args);
            run();
        } catch (CmdLineException e) {
            printUsage(cmdParser);
            LOGGER.info(e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while running tool", e);
        }
    }
    
    private void printUsage(CmdLineParser cmdParser) {
        usageStream.println(getClass().getSimpleName());
        usageStream.println();
        usageStream.println("Arguments:");
        cmdParser.printUsage(usageStream);
        usageStream.println();
    }
    
    /**
     * Runs this tool. This method is called by {@link #start(String[])}. When 
     * this method is called it can be assumed that arguments have already been 
     * mapped to fields.
     */
    public abstract void run();
}
