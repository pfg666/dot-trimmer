package com.pfg666.dottrimmer;

import net.automatalib.automata.transducers.impl.FastMealy;

public class DotTrimmerResult {
	private String modelFile;
	FastMealy<String, String> trimmedModel;
	
	
	public DotTrimmerResult(String modelFile, FastMealy<String, String> trimmedModel) {
		super();
		this.modelFile = modelFile;
		this.trimmedModel = trimmedModel;
	}
	
	public String getModelFile() {
		return modelFile;
	}
	public FastMealy<String, String> getTrimmedModel() {
		return trimmedModel;
	}

}
