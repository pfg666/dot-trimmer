package com.pfg666.dottrimmer;

import net.automatalib.automata.graphs.TransitionEdge;

public class EdgeInfo<S,I,T>  {
	
	public S getSource() {
		return source;
	}

	public I getInput() {
		return input;
	}

	public T getTransition() {
		return transition;
	}

	private S source;
	private I input;
	private T transition;

	public EdgeInfo(S source, I input, T transition) {
		this.source = source;
		this.transition = transition;
		this.input = input;
	}
	
	public TransitionEdge<I, T> asTransitionEdge() {
		return new TransitionEdge<>(input, transition);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((transition == null) ? 0 : transition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EdgeInfo other = (EdgeInfo) obj;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (transition == null) {
			if (other.transition != null)
				return false;
		} else if (!transition.equals(other.transition))
			return false;
		return true;
	}

}
