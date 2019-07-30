package com.pfg666.dottrimmer;

import java.io.FileReader;
import java.util.Collections;
import java.util.List;

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
	private List<String> path;
	private List<String> prefix;
	
	
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


	public String getColor() {
		return color;
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
	
	public String toString() {
		return "[" + getPrefix().toString() + "] " + color + ":" + getPath().toString();
	}
}
