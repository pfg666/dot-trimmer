package com.pfg666.dottrimmer.paths;

import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class ColoredPath {
	
	public static ColoredPath [] loadColoredPaths(String file) {
		Gson gson = new Gson();
		try (FileReader fr = new FileReader(file)) {
			ColoredPath [] paths= gson.fromJson(fr, ColoredPath [].class);
			return paths;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}
	
	// some safeguards against null prefixes/paths
	private static List<String> DEFAULT_PREFIX = Collections.emptyList();
	private static List<String> DEFAULT_PATH = Collections.emptyList();
	

	private String color;
	private String style;
	private List<String> path;
	private List<String> prefix;
	private List<String> outputs;
	private String anyOutput;
	private boolean free = false;
	// does it refer to the original inputs or to the replaced
	private boolean replace = true;
	
	
	public ColoredPath() {
	}

	public ColoredPath(String color, List<String> inputs) {
		this.color = color;
		this.path = inputs;
	}
	
	public ColoredPath(String color, List<String> inputs, List<String> prefix) {
		this.color = color;
		this.path = inputs;
		this.prefix = prefix;
	}
	
	public EdgeProperties getEdgeProperties() {
		return new EdgeProperties(color, style);
	}

	public List<String> getPath() {
		if (path == null)
			return DEFAULT_PATH;
		else 
			return Collections.unmodifiableList(path);
	}
	
	public List<String> getPrefix() {
		if (prefix == null) 
			return DEFAULT_PREFIX;
		else 
			return Collections.unmodifiableList(prefix);
	}
	
	public List<String> getOutputs() {
		return outputs;
	}
	
	public String getAnyOutput() {
		return anyOutput;
	}
	
	public boolean isFree() {
		return free;
	}
	
	public boolean isReplaced() {
		return replace;
	}
	
	public String toString() {
		return "[" + getPrefix().toString() + "] " + color + ":" + getPath().toString();
	}
	
	public ColoredPath replace(Function<String,String> repl) {
		ColoredPath copy = new ColoredPath(color, path.stream().map(repl).collect(Collectors.toList()));
		if (prefix != null) {
			copy.prefix = prefix.stream().map(repl).collect(Collectors.toList());
		}
		copy.replace = false;
		copy.free = free;
		if (outputs != null) {
			copy.outputs = outputs.stream().map(repl).collect(Collectors.toList());
		}
		if (anyOutput != null) {
			copy.anyOutput = repl.apply(anyOutput);
		}
		return copy;
	}
}
