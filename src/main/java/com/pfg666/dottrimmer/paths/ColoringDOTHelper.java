package com.pfg666.dottrimmer.paths;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.automatalib.visualization.DefaultVisualizationHelper;


public class ColoringDOTHelper<N, E> extends DefaultVisualizationHelper<N, E> {
	private Set<E> edges;
	private EdgeProperties prop;
	
	public ColoringDOTHelper(Set<E> edges, EdgeProperties prop) {
		this.edges = new HashSet<>(edges);
		this.prop = prop;
	}
	
	@Override
	public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
		if (edges.contains(edge)) {
			if (prop.getColor() != null) 
				properties.put(EdgeAttrs.COLOR, prop.getColor());
			if (prop.getStyle() != null)
				properties.put(EdgeAttrs.STYLE, prop.getStyle());
		}
		return true;
	}

}
