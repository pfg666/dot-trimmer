package com.pfg666.dottrimmer;

import java.io.FileReader;
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

	private String color;
	private List<String> path;
	
	public ColoredPath() {
	}

	public ColoredPath(String color, List<String> inputs) {
		this.color = color;
		this.path = inputs;
	}


	public String getColor() {
		return color;
	}

	public List<String> getPath() {
		return path;
	}
	
	
	public String toString() {
		return color + ":" + path.toString();
	}
}
