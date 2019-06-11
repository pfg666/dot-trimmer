package com.pfg666.dottrimmer;

import java.util.List;

import com.beust.jcommander.Parameter;

public class DotTrimmerConfig {
	private static String OTHER="Other";
	
	@Parameter(names = {"-t","--thresh"}, required = false, 
			description = "The minimum number of grouped inputs that can be merged. ")
	private int mergeThreshold = Integer.MAX_VALUE;
	
	@Parameter(names = {"-l","--label"}, required = false, 
			description = "The label replacing merged inputs. ")
	private String mergeLabel = OTHER;
	
	@Parameter(names = {"-r","--replacements"}, required = false, 
			description = "Path to the file with replacements to be applied. ")
	private String replacements;  
	
	@Parameter(names = {"-i","--input"}, required = true, 
			description = "Path to the input .dot file containing the model to be simplified. ")
	private String model;
	
	@Parameter(names = {"-sc","--stateColor"}, required = false, 
			description = "A list of <stateid>:<color> elements. Edges of all paths leading to the state with the corresponding id are colored using the given color")
	private List<String> coloredStates;
	
	@Parameter(names = {"-wl","--withoutLoops"}, required = false, description = "If set to true, state-coloring will ignore paths with loops")
	private boolean withoutLoops;
	
	@Parameter(names = {"-pc","--pathColor"}, required = false, description = "Colors the paths as described in a .json")
	private String coloredPathsFile;
	
	@Parameter(names = {"-o","--output"}, required = false, 
			description = "Path to the generated .dot file containing the trimmed model. ")
	private String output = null;
	
	
	public int getMergeThreshold() {
		return mergeThreshold;
	}

	public String getMergeLabel() {
		return mergeLabel;
	}

	public String getReplacements() {
		return replacements;
	}
	
	public boolean isWithoutLoops() {
		return withoutLoops;
	}

	public String getModel() {
		return model;
	}
	
	public String getOutput() {
		return output;
	}
	
	public List<String> getColoredStates() {
		return coloredStates;
	}
	
	public String getColoredPathsFile() {
		return coloredPathsFile;
	}
}
