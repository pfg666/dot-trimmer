package com.pfg666.dottrimmer;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.automatalib.automata.transducers.MealyMachine;

public class EndGoalProcessor {
	// the endgoal is a transition which generates an output matching this regex.
	public String outputRegex;
	public EndGoalProcessor(String output) {
		this.outputRegex =  output;
	}
	public String getOutputRegex() {
		return outputRegex;
	}
	
	/**
	 * returns all states satisfying the end goal.
	 */
	private <S,I,T,O> List<S> getStatesToEndGoal(MealyMachine<S, I, T, O> automaton, Collection<I> inputs) {
		List<S> endGoalStates = new LinkedList<>();
		for (S state : automaton.getStates()) {
			for (I input : inputs) {
				if (automaton.getOutput(state, input).toString().matches(outputRegex)) {
					endGoalStates.add(state);
				}
			}
		}
		return endGoalStates;
	}
	
	public <S,I,T,O> Set<S> getNecessaryStatesForEndgoal(MealyMachine<S, I, T, O> automaton, Collection<I> inputs) {
		List<S> endGoalStates = getStatesToEndGoal(automaton, inputs);
		Set<S> necessaryStates = new LinkedHashSet<>();
		Map<S, Set<S>> predMap = new HashMap<>();
		for (S s : automaton.getStates()) {
			for (I input : inputs) {
				S succ= automaton.getSuccessor(s, input);
				if (succ != null) {
					predMap.putIfAbsent(succ, new LinkedHashSet<>());
					predMap.get(succ).add(s);
				}
			}
		}
		
		for (S endGoalState : endGoalStates) {
			Set<S> predStates = getOnRouteStates(automaton, inputs, endGoalState, predMap);
			necessaryStates.addAll(predStates);
			for (I input : inputs) {
				S succ= automaton.getSuccessor(endGoalState, input);
				necessaryStates.add(succ);
			}
		}
		return necessaryStates;
	}
	
	private <S,I,T,O> Set<S> getOnRouteStates(MealyMachine<S, I, T, O> automaton, Collection<I> inputs, S targetState, Map<S, Set<S>> map) {
		Set<S> visited = new LinkedHashSet<S>();
		Queue<S> toVisit = new ArrayDeque<S>();
		toVisit.add(targetState);
		while (!toVisit.isEmpty()) {
			S visitedState = toVisit.poll();
			visited.add(visitedState);
			Set<S> predStates = map.get(visitedState);
			for (S predState : predStates) {
				if (!visited.contains(predState)) {
					toVisit.add(predState);
				}
			}
		}
		
		return visited;
	}
}
