package com.pfg666.dottrimmer;

import java.util.Map;

import net.automatalib.graphs.dot.EmptyDOTHelper;

public class ColoringDOTHelper<N, E> extends EmptyDOTHelper<N, E> {
	public ColoringDOTHelper() {
	}
	
	@Override
	public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
		properties.put(EdgeAttrs.COLOR, "green");
		return true;
	}

}
