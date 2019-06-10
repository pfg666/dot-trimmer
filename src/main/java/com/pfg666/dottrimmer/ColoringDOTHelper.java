package com.pfg666.dottrimmer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.automatalib.graphs.dot.EmptyDOTHelper;

public class ColoringDOTHelper<N, E> extends EmptyDOTHelper<N, E> {
	private Set<E> edges;
	private String color;
	
	public ColoringDOTHelper() {
		
	}

	public ColoringDOTHelper(Set<E> edges, String color) {
		this.edges = new HashSet<>(edges);
		this.color = color;
	}
	
	@Override
	public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
		if (edges.contains(edge))
			properties.put(EdgeAttrs.COLOR, color);
		return true;
	}

}
