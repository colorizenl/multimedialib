//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.tool;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import nl.colorize.util.LogHelper;

/**
 * Standard implementation for tools that provide a command line interface.
 */
public abstract class CommandLineTool {
        
    private PrintStream usageStream;
    
    private static final Logger LOGGER = LogHelper.getLogger(CommandLineTool.class);

    public CommandLineTool() {
        usageStream = System.err;
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
        usageStream.println(getDescription());
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
    
    protected File parseInputFile(String path) {
        File inputFile = new File(path);
        if (!inputFile.exists()) {
            throw new IllegalArgumentException("File '" + path + "' not found");
        }
        return inputFile;
    }
    
    protected File parseInputDirectory(String path) {
        File inputDirectory = new File(path);
        if (!inputDirectory.exists()) {
            throw new IllegalArgumentException("Directory '" + path + "' not found");
        }
        if (!inputDirectory.isDirectory()) {
            throw new IllegalArgumentException("'" + path + "' is not a directory");
        }
        return inputDirectory;
    }
    
    protected File parseOutputFile(String path) {
        File outputFile = new File(path);
        if (outputFile.exists()) {
            throw new IllegalArgumentException("File '" + path + "' already exists");
        }
        return outputFile;
    }
    
    /**
     * Parses an output directory path provided as a string argument.
     * @param allowExisting When true, allow already existing directories to
     *        be used.
     */
    protected File parseOutputDirectory(String path, boolean allowExisting) {
        File outputDirectory = new File(path);
        if (!allowExisting && outputDirectory.exists()) {
            throw new IllegalArgumentException("Directory '" + path + "' already exists");
        }
        if (outputDirectory.exists() && !outputDirectory.isDirectory()) {
            throw new IllegalArgumentException("'" + path + "' is not a directory");
        }
        return outputDirectory;
    }
    
    /**
     * Gives a human-readable description of this tool. This description will
     * be printed with the usage instructions. The default description is the
     * name of the class.
     */
    protected String getDescription() {
        return getClass().getSimpleName();
    }
}
