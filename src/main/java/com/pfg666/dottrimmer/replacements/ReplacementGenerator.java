package com.pfg666.dottrimmer.replacements;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class ReplacementGenerator {
	
	private Gson gson;
	private Replacer replacer;

	public ReplacementGenerator() {
		this.gson = new Gson();
		this.replacer = new Replacer();
	}
	
	public void loadReplacements(String replacementFileName) throws FileNotFoundException, IOException{
		try (FileReader fr = new FileReader(replacementFileName)) {
			this.replacer = gson.fromJson(fr, Replacer.class);
		}
	}
	
	public Replacer getReplacer() {
		return replacer;
	}
	
	public Map<String, String> generateReplacements(List<String> strings) {
		LinkedHashMap<String, String> replacements = new LinkedHashMap<>();
		strings.forEach(s -> replacements.put(s, replacer.replace(s)));
		return replacements;
	}
}
