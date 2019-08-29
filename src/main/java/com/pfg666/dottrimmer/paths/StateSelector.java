package com.pfg666.dottrimmer.paths;

import java.util.LinkedHashSet;
import java.util.Set;

import net.automatalib.automata.concepts.DetSuffixOutputAutomaton;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;

public class StateSelector<S, I, T, O> {
	
	public Set<S> selectStates(MealyMachine<S, I, T, O> automaton, ParsedColoredPath<I> path) {
		Set<S> states = new LinkedHashSet<S>();
		if (!path.getColoredPath().isFree())
			states.add(automaton.getState(path.getPrefixWord()));
		else
			states.addAll(automaton.getStates());

		if (path.getColoredPath().getOutputs() != null)
			states = filterStatesBasedOnMatchingOutputSequence(states, automaton, path);
		
		if (path.getColoredPath().getAnyOutput() != null) 
			states = filterStatesBasedOnMatchingAnyOutput(states, automaton, path);
		
		return states;
	}
	
	

	private Set<S> filterStatesBasedOnMatchingOutputSequence(Set<S> states, MealyMachine<S,I,T,O> automaton,
			ParsedColoredPath<I> path) {
		Set<S> outputFilteredStates = new LinkedHashSet<>();
		if (path.getColoredPath().getOutputs().size() != path.getPathWord().size()) {
			throw new RuntimeException("The number of inputs in the paths is "
					+ "expected to be the same as the number of output matches. \n Parsed color path: " + path);
		} else {
			for (S state : states) {
				Word<O> pathOutput = automaton.computeStateOutput(state, path.getPathWord());
				boolean matches = true;
				for (int i=0; i<path.getColoredPath().getOutputs().size(); i++) {
					O autOutput = pathOutput.getSymbol(i);
					String outputRegex = path.getColoredPath().getOutputs().get(i);
					if (!autOutput.toString().matches(outputRegex)) {
						matches = false;
						break;
					}
				}
				if (matches) {
					outputFilteredStates.add(state);
				}
			}
		}
		
		return outputFilteredStates;
	}
	
	public Set<S> filterStatesBasedOnMatchingAnyOutput(Set<S> states, DetSuffixOutputAutomaton<S, I, T, Word<O>> automaton,
			ParsedColoredPath<I> path) {
		Set<S> outputFilteredStates = new LinkedHashSet<>();
		for (S state : states) {
			Word<O> pathOutput = automaton.computeStateOutput(state, path.getPathWord());
			boolean matches = pathOutput.stream().anyMatch(po -> po.toString().matches(path.getColoredPath().getAnyOutput()));
			if (matches) {
				outputFilteredStates.add(state);
			}
		}
		
		return outputFilteredStates;
	}
}
