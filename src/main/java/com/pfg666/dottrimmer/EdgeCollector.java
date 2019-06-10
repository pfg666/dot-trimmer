package com.pfg666.dottrimmer;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Word;

public class EdgeCollector<S, I, T>  {
	
	public Set<EdgeInfo<S,I,T>> getEdgesLeadingToState(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton, Collection<? extends I> inputs, S toState, boolean excludeLoops) {
		Queue<Word<EdgeInfo<S,I,T>>> bfsQueue = new ArrayDeque<>();
		Set<EdgeInfo<S,I,T>> leadingEdges = new LinkedHashSet<>();
		Set<Set<EdgeInfo<S,I,T>>> visited = new HashSet<>();
		Map<S, Set<EdgeInfo<S,I,T>>> predMap = getPrecedingEdgesMap(automaton, inputs);
		bfsQueue.addAll(predMap.get(toState).stream().map(e -> Word.fromSymbols(e)).collect(Collectors.toSet()));
		Word<EdgeInfo<S,I,T>> curr;

		while ((curr = bfsQueue.poll()) != null) {
			if (excludeLoops && hasLoop(curr, toState)) {
				continue;
			}
			EdgeInfo<S, I, T> currEdge = curr.getSymbol(0);
			HashSet<EdgeInfo<S, I, T>> configuration = new HashSet<>(curr.asList());
			if (visited.contains(configuration)) {
				continue;
			}
			visited.add(configuration);
			S source = currEdge.getSource();
			if (Objects.equals(automaton.getInitialState(), source)) 
				leadingEdges.addAll(curr.asList());
			
			Set<EdgeInfo<S, I, T>> predEdges = predMap.get(source);
			if (predEdges != null) {
				for (EdgeInfo<S, I, T> edge : predEdges) {
					bfsQueue.add(curr.prepend(edge));
				}
			}
		}
		
		return leadingEdges;
		
	} 
	
	private boolean hasLoop(Word<EdgeInfo<S,I,T>> path, S toState) {
		long distCount = Stream.concat(path.stream().map(e -> e.getSource()), Stream.of(toState)).distinct().count();
		return !(distCount == (long) path.length() + 1);
	}
	
	private Map<S, Set<EdgeInfo<S,I,T>>> getPrecedingEdgesMap(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton, Collection<? extends I> inputs) {
		Map<S, Set<EdgeInfo<S,I,T>>> map = new HashMap<>();
		for (S s : automaton.getStates()) {
			for (I input : inputs) {
				S succ= automaton.getSuccessor(s, input);
				if (succ != null) {
					map.putIfAbsent(succ, new LinkedHashSet<>());
					map.get(succ).add(new EdgeInfo<>(s, input, automaton.getTransition(s, input)));
				}
			}
		}
		return map;
	}
	

}
