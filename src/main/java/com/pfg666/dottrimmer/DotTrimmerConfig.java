package com.pfg666.dottrimmer;

import java.util.List;

import com.beust.jcommander.Parameter;

public class DotTrimmerConfig {
	private static String OTHER="Other";
	
	/*
	 * Merge transition operation
	 */
	
	@Parameter(names = {"-t","--thresh"}, required = false, 
			description = "The minimum number of grouped inputs that can be merged. ")
	private Integer mergeThreshold = null;
	
	@Parameter(names = {"-l","--label"}, required = false, 
			description = "The label replacing merged inputs. ")
	private String mergeLabel = OTHER;
	
	/*
	 * Remove input operation
	 */
	
	@Parameter(names = {"-ri","--removeInputs"}, required = false, 
			description = "Removes inputs and minimizes the resulting automaton.")
	private List<String> removeInputs;
	
	
	/*
	 *  Apply replacements operation
	 */
	
	@Parameter(names = {"-r","--replacements"}, required = false, 
			description = "Path to the file containing the replacements to be applied. ")
	private String replacements;  
	
	/*
	 * State path coloring operation
	 */
	
	@Parameter(names = {"-csp","--colorStatePaths"}, required = false, 
			description = "A list of <stateid>:<color> elements. Edges of all paths leading to the state with the corresponding id are colored using the given color")
	private List<String> coloredStates;
	
	@Parameter(names = {"-ml","--maxLength"}, required = false, description = "The maximum path length to search for")
	private Integer maxLength = 11;
	
	@Parameter(names = {"-wl","--withoutLoops"}, required = false, description = "If set to true, state-coloring will ignore paths with self-loops")
	private boolean withoutLoops;
	
	/*
	 * End goal removal operation
	 */
	
	@Parameter(names = {"-ego","--endGoalOutput"}, required = false, description = "Only keep states for which there exists a path to a transition generating an output which matches the supplied regex (a so-called endGoalOutput).")
	private String endGoalOutput = null;
	
	
	/*
	 * Regular path coloring operation 
	 */

	@Parameter(names = {"-cp","--colorPaths"}, required = false, description = "Colors the paths as described in a .json")
	private String coloredPathsFile;
	
	/*
	 * I/O 
	 */

	@Parameter(names = {"-i","--input"}, required = true, 
			description = "Path to the input .dot file containing the model to be simplified. ")
	private String model;
	
	@Parameter(names = {"-o","--output"}, required = false, 
			description = "Path to the generated .dot file containing the trimmed model. ")
	private String output = null;
	
	
	public Integer getMergeThreshold() {
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
	
	public String getEndGoalOutput() {
		if (endGoalOutput != null && endGoalOutput.startsWith("'") && endGoalOutput.endsWith("'"))
			return endGoalOutput.substring(1, endGoalOutput.length()-1);
		return endGoalOutput;
	}
	
	public List<String> getColoredStates() {
		return coloredStates;
	}
	
	public String getColoredPathsFile() {
		return coloredPathsFile;
	}

	public Integer getMaxLength() {
		return maxLength;
	}
	
	public List<String> getRemoveInputs() {
		return removeInputs;
	}

}
