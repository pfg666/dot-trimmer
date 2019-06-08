package com.pfg666.dottrimmer;

import java.util.logging.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
	private static Logger LOGGER = Logger.getGlobal();
	
	
	public static void main(String args[]) {
		DotTrimmerConfig config = new DotTrimmerConfig();
        JCommander commander = new JCommander(config);
        commander.setAllowParameterOverwriting(true);
        try {
        	if (args.length == 0) {
                commander.usage();
                return;
            }
            commander.parse(args);
            
            try {
            	DotTrimmer trimmer = new DotTrimmer(config);
        		trimmer.trimModel();
            } catch (Exception E) {
                LOGGER.severe("Encountered an exception. See debug for more info.");
                E.printStackTrace();
                //TODO ^^ what says here :)
                LOGGER.severe(E.getMessage());
            }
        } catch (ParameterException E) {
            LOGGER.severe("Could not parse provided parameters. " + E.getLocalizedMessage());
            LOGGER.severe(E.getMessage());
            commander.usage();
        }
	}
	
}
