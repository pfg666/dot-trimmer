package com.pfg666.dottrimmer;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
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
	
	//TODO The algorithm has to be improved, as it currently relies solely on maxPath to determine which paths to choose.
	
	public Set<EdgeInfo<S,I,T>> getEdgesLeadingToState(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton, Collection<? extends I> inputs, S toState, 
			int maxPath,
			boolean excludeLoops) {
		Queue<Word<EdgeInfo<S,I,T>>> bfsQueue = new ArrayDeque<>();
		Set<EdgeInfo<S,I,T>> leadingEdges = new LinkedHashSet<>();
		Set<Object> visited = new HashSet<>();
		Map<S, Set<EdgeInfo<S,I,T>>> predMap = getPrecedingEdgesMap(automaton, inputs);
		bfsQueue.addAll(predMap.get(toState).stream().map(e -> Word.fromSymbols(e)).collect(Collectors.toSet()));
		Word<EdgeInfo<S,I,T>> curr;

		while ((curr = bfsQueue.poll()) != null) {
			if (excludeLoops && hasSelfLoop(curr, toState)) {
				continue;
			}
			EdgeInfo<S, I, T> currEdge = curr.getSymbol(0);
			S source = currEdge.getSource();
			if (Objects.equals(automaton.getInitialState(), source))  {
				leadingEdges.addAll(curr.asList());
			}
			
			Set<EdgeInfo<S, I, T>> predEdges = predMap.get(source);
			if (predEdges != null) {
				for (EdgeInfo<S, I, T> edge : predEdges) {
					Word<EdgeInfo<S, I, T>> path = curr.prepend(edge);
					Object configuration = extractConfiguration(path);
					if (!visited.contains(configuration) && path.size() < maxPath) {
						bfsQueue.add(path);
					}
				}
			}
		}
		
		return leadingEdges;
	}
	
	/*
	 * Extracts a more compact configuration which uniquely characterizes a path. 
	 */
	private Object extractConfiguration(Word<EdgeInfo<S,I,T>> path) {
		return path.stream().collect(Collectors.toList());
	}
	
	private boolean hasLoop(Word<EdgeInfo<S,I,T>> path, S toState) {
		long distCount = Stream.concat(path.stream().map(e -> e.getSource()), Stream.of(toState)).distinct().count();
		return !(distCount == (long) path.length() + 1);
	}
	
	private boolean hasSelfLoop(Word<EdgeInfo<S,I,T>> path, S toState) {
		if (path.isEmpty()) {
			return false;
		} else {
			EdgeInfo<S, I, T> prev = path.firstSymbol();
			for (int i=1; i<path.size(); i++) {
				EdgeInfo<S, I, T> curr = path.getSymbol(i);
				if (prev.getSource().equals(curr.getSource())) 
					return true;
				prev = curr;
			}
		}
		return false;
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
	
	public Set<EdgeInfo<S,I,T>> getEdgesForPath(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton, Collection<? extends I> inputs, Word<I> prefix, Word<I> path) {
		if (!inputs.containsAll(path.asList())) {
			return Collections.emptySet();
		} 
		LinkedHashSet<EdgeInfo<S,I,T>> edges = new LinkedHashSet<>();
		S cur = automaton.getSuccessor(automaton.getInitialState(), prefix);
		for (I input : path) {
			S succ = automaton.getSuccessor(cur, input);
			T trans = automaton.getTransition(cur, input);
			if (succ == null || trans == null) {
				return Collections.emptySet();
			}
			edges.add(new EdgeInfo<>(succ, input, trans));
			cur = succ;
		}
		return edges;
	}
	

}
