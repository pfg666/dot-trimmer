package com.pfg666.dottrimmer;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;
import com.pfg666.dottrimmer.paths.ColoredPath;
import com.pfg666.dottrimmer.paths.ColoringDOTHelper;
import com.pfg666.dottrimmer.paths.EdgeProperties;
import com.pfg666.dottrimmer.paths.ParsedColoredPath;
import com.pfg666.dottrimmer.paths.StateSelector;
import com.pfg666.dottrimmer.replacements.ReplacementGenerator;
import com.pfg666.dottrimmer.replacements.Replacer;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.automata.transducers.impl.FastMealyState;
import net.automatalib.automata.transducers.impl.MealyTransition;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.helper.AggregateVisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ListAlphabet;

public class DotTrimmer {
	private static Logger LOGGER = Logger.getGlobal();

	private DotTrimmerConfig config;

	public DotTrimmer(DotTrimmerConfig config) {
		this.config = config;
	}

	public FastMealy<String, String> trimUsingOtherConstruct(FastMealy<String, String> mealy) {
		HashMap<FastMealyState<String>, FastMealyState<String>> stateMap = new HashMap<>();
		Alphabet<String> alphabet = mealy.getInputAlphabet();
		List<String> list = new ArrayList<>(alphabet);
		list.add(config.getMergeLabel());
		FastMealy<String, String> trimmed = buildEmptyCopy(mealy, new ListAlphabet<String>(list), stateMap);
		constructSimplifiedMachine(mealy, trimmed, stateMap);
		return trimmed;
	}
	
	private FastMealy<String, String> buildEmptyCopy(FastMealy<String, String> mealy, Collection<String> inputs, Map<FastMealyState<String>, FastMealyState<String>> stateMap) {
		List<String> list = new ArrayList<>(inputs);
		FastMealy<String, String> emptyCopy = new FastMealy<String, String>(new ListAlphabet<>(list));
		for (FastMealyState<String> state : mealy.getStates()) {
			FastMealyState<String> newState = emptyCopy.addState(null);
			stateMap.put(state, newState);
		}
		return emptyCopy;
	}
	
	private FastMealy<String, String> buildFullCopy(FastMealy<String, String> mealy) {
		HashMap<FastMealyState<String>, FastMealyState<String>> stateMap = new HashMap<>();
		FastMealy<String, String> copy = buildEmptyCopy(mealy, mealy.getInputAlphabet(), stateMap);
		copy.setInitialState(stateMap.get(mealy.getInitialState()));
		for (FastMealyState<String> state : mealy.getStates()) {
			FastMealyState<String> otherState = stateMap.get(state);
			for (int i = 0; i < mealy.getInputAlphabet().size(); i++) {
				MealyTransition<FastMealyState<String>, String> trans = state.getTransitionObject(i);
				MealyTransition<FastMealyState<String>, String> otherTrans = new MealyTransition<FastMealyState<String>, String>(
							stateMap.get(trans.getSuccessor()), trans.getOutput());
				otherState.setTransitionObject(i, otherTrans);
			}
		}
		return copy;
	}

	private void constructSimplifiedMachine(FastMealy<String, String> mealy, FastMealy<String, String> trimmed,
			Map<FastMealyState<String>, FastMealyState<String>> stateMap) {
		int inputSize = mealy.getInputAlphabet().size();
		trimmed.setInitialState(stateMap.get(mealy.getInitialState()));
		for (FastMealyState<String> state : mealy.getStates()) {
			FastMealyState<String> otherState = stateMap.get(state);
			Set<MealyTransition<FastMealyState<String>, String>> excludeTrans = new HashSet<>();
			List<Set<String>> symbolGrouping = generateSymbolGrouping(mealy, state);
			int maxSize = symbolGrouping.stream().mapToInt(g -> g.size()).max().getAsInt();
			if (maxSize > config.getMergeThreshold()) {
				Set<String> maxSet = symbolGrouping.stream().filter(g -> g.size() == maxSize).findFirst().get();
				maxSet.stream().map(inp -> mealy.getTransition(state, inp)).forEach(tr -> excludeTrans.add(tr));
				String input = maxSet.iterator().next();
				MealyTransition<FastMealyState<String>, String> trans = mealy.getTransition(state, input);
				MealyTransition<FastMealyState<String>, String> otherTrans = new MealyTransition<FastMealyState<String>, String>(
						stateMap.get(trans.getSuccessor()), trans.getOutput());
				otherState.ensureInputCapacity(inputSize+1);
				otherState.setTransitionObject(inputSize, otherTrans);
			}
			for (int i = 0; i < inputSize; i++) {
				MealyTransition<FastMealyState<String>, String> trans = state.getTransitionObject(i);
				if (trans != null && !excludeTrans.contains(state.getTransitionObject(i))) {
					MealyTransition<FastMealyState<String>, String> otherTrans = new MealyTransition<FastMealyState<String>, String>(
							stateMap.get(trans.getSuccessor()), trans.getOutput());
					otherState.setTransitionObject(i, otherTrans);
				}
			}
		}
	}

	private List<Set<String>> generateSymbolGrouping(FastMealy<String, String> a, FastMealyState<String> state) {
		List<Set<String>> inputsWithSameBehavior = new ArrayList<>();
		for (String sym : a.getInputAlphabet()) {
			boolean found = false;
			for (Set<String> inputs : inputsWithSameBehavior) {
				String sample = inputs.iterator().next();
				if (promptsSameBehavior(a, state, sym, sample)) {
					inputs.add(sym);
					found = true;
					break;
				}
			}

			if (!found) {
				LinkedHashSet<String> newSet = new LinkedHashSet<>();
				newSet.add(sym);
				inputsWithSameBehavior.add(newSet);
			}
		}

		return inputsWithSameBehavior;
	}

	private boolean promptsSameBehavior(FastMealy<String, String> a, FastMealyState<String> state, String symbol1,
			String symbol2) {
		// the the automaton is not specified for the symbols, return false
		if (a.getTransition(state, symbol1) == null || a.getTransition(state, symbol2) == null)
			return false;
		return isCompatible(a.getTransition(state, symbol1), a.getTransition(state, symbol2));
	}

	private boolean isCompatible(MealyTransition<FastMealyState<String>, String> tr1,
			MealyTransition<FastMealyState<String>, String> tr2) {
		return tr1.getOutput().equals(tr2.getOutput()) && tr1.getSuccessor().equals(tr2.getSuccessor());
	}

	private String getOutputFile() {
		String trimmedModelFile;
		if (config.getOutput() == null) {
			trimmedModelFile = config.getModel().contains(".dot") ? config.getModel().replace(".dot", ".trimmed.dot")
					: config.getModel().concat(".trimmed.dot");
		} else {
			trimmedModelFile = config.getOutput();
		}
		return trimmedModelFile;
	}

	public DotTrimmerResult trimModel() throws FileNotFoundException, IOException, ParseException {
		LOGGER.info("Starting transformation procedure");
		ReplacementGenerator gen = new ReplacementGenerator();
		if (config.getReplacements() != null) {
			LOGGER.info("Loading replacements for .json");
			gen.loadReplacements(config.getReplacements());
		}

		LOGGER.info("Parsing automaton from file (and applying replacements)");
		MealyDotParser<String, String> parser = new MealyDotParser<String, String>(
				new ReplacingMealyProcessor(gen.getReplacer()));
		FastMealy<String, String> mealy = parser.parseAutomaton(config.getModel()).get(0);
		
		
		FastMealy<String, String> trimmedMealy = mealy;
		
		if (config.getRemoveInputs() != null) {
			LOGGER.info("Simplifying the model by removing inputs");
			trimmedMealy = trimByRemovingInputs(trimmedMealy);
		}
		
		if (config.getEndGoalOutput() != null) {
			LOGGER.info("Simplifying the model using the EndGoal construct");
			trimmedMealy = trimUsingEndGoalConstruct(trimmedMealy);
		}
		
		if (config.getMergeThreshold() != null) {
			LOGGER.info("Simplifying the model using the Other input construct");
			trimmedMealy = trimUsingOtherConstruct(trimmedMealy);
		}
		
		String outputFile = getOutputFile();

		LOGGER.info("Applying coloring");
		List<VisualizationHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>> helpers = generateHelpers(
				trimmedMealy, gen.getReplacer());

		AggregateVisualizationHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>> helper = new AggregateVisualizationHelper<>(helpers);

		LOGGER.info("Exporting the model to .dot");
		GraphDOT.write(trimmedMealy, trimmedMealy.getInputAlphabet(), new FileWriter(outputFile), helper);
		return new DotTrimmerResult(outputFile, trimmedMealy);
	}

	private FastMealy<String, String> trimByRemovingInputs(FastMealy<String, String> trimmedMealy) {
		List<String> keptInputs = new LinkedList<>(trimmedMealy.getInputAlphabet());
		keptInputs.removeAll( config.getRemoveInputs());
		FastMealy<String, String> min = trimmedMealy; 
		FastMealy<String, String> copy;
		copy = new FastMealy<String, String>(new ListAlphabet<>(keptInputs));
		net.automatalib.util.automata.copy.AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, trimmedMealy, keptInputs, copy);
		min = new FastMealy<String, String>(new ListAlphabet<>(keptInputs));
		min = Automata.minimize(copy, keptInputs, min);

		return min;
	}

	private FastMealy<String, String> trimUsingEndGoalConstruct(FastMealy<String, String> mealy) {
		EndGoalProcessor endGoalProcessor = new EndGoalProcessor(config.getEndGoalOutput());
		FastMealy<String, String> copy = buildFullCopy(mealy);
		Set<FastMealyState<String>> statesToKeep = endGoalProcessor.getNecessaryStatesForEndgoal(copy, copy.getInputAlphabet());
		for (FastMealyState<String> state : new ArrayList<>(copy.getStates())) {
			if (!statesToKeep.contains(state)) {
				copy.removeAllTransitions(state);
				copy.removeState(state);
			}
		}
		
		return copy;
	}

	// generics bonanza
	private List<VisualizationHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>> generateHelpers(
			FastMealy<String, String> trimmedMealy, Replacer replacer) {
		Alphabet<String> inputs = trimmedMealy.getInputAlphabet();
		List<VisualizationHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>> helpers = new LinkedList<>();
		EdgeCollector<FastMealyState<String>, String, MealyTransition<FastMealyState<String>, String>> collector = new EdgeCollector<>();
		if (config.getColoredStates() != null) {
			for (String coloredState : config.getColoredStates()) {
				String[] split = coloredState.split("\\:");
				Integer stateId = Integer.valueOf(split[0]);
				String color = split[1].trim();
				FastMealyState<String> state = trimmedMealy.getState(stateId);
				Set<EdgeInfo<FastMealyState<String>, String, MealyTransition<FastMealyState<String>, String>>> edges = collector
						.getEdgesLeadingToState(trimmedMealy, inputs, state, config.getMaxLength(),
								config.isWithoutLoops());
				Set<TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>> transitions = edges
						.stream().map(e -> e.asTransitionEdge()).collect(Collectors.toSet());
				EdgeProperties edgeProp = new EdgeProperties(color, null);
				helpers.add(
						new ColoringDOTHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>(
								transitions, edgeProp));
			}
		}

		if (config.getColoredPathsFile() != null) {
			StateSelector<FastMealyState<String>, String, MealyTransition<FastMealyState<String>, String>, String> stateSelector = new StateSelector<>();
			ColoredPath[] paths = ColoredPath.loadColoredPaths(config.getColoredPathsFile());
			for (ColoredPath path : paths) {
				ParsedColoredPath<String> parsedColorPath = parseColorPath(path, replacer, trimmedMealy);
				// are all the inputs contained in the alphabet
				boolean isContained = Stream.concat(parsedColorPath.getPathWord().stream(), parsedColorPath.getPrefixWord().stream())
						.allMatch(i -> inputs.contains(i));
				if (isContained) {
					Set<FastMealyState<String>> states = stateSelector.selectStates(trimmedMealy, parsedColorPath);
					
					Set<EdgeInfo<FastMealyState<String>, String, MealyTransition<FastMealyState<String>, String>>> edges = collector
							.getEdgesForPath(trimmedMealy, inputs, parsedColorPath.getPathWord(), states);
					if (!edges.isEmpty()) {
						Set<TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>> transitions = edges
								.stream().map(e -> e.asTransitionEdge()).collect(Collectors.toSet());
						helpers.add(
								new ColoringDOTHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>(
										transitions, path.getEdgeProperties()));
					}
				}
			}
		}

		return helpers;
	}
	
	private ParsedColoredPath<String> parseColorPath(ColoredPath path, Replacer replacer, FastMealy<String, String> trimmedMealy) {
		if (!path.isReplaced()) {
			List<String> realPath = path.getPath().stream().collect(Collectors.toList());
			List<String> realPrefix = path.getPrefix().stream().collect(Collectors.toList());
			return new ParsedColoredPath<>(path, Word.fromList(realPrefix), Word.fromList(realPath));
		} else {
			List<String> realPath = path.getPath().stream().map(i -> replacer.replace(i)).collect(Collectors.toList());
			List<String> realPrefix = path.getPrefix().stream().map(i -> replacer.replace(i)).collect(Collectors.toList());
			return new ParsedColoredPath<>(path.replace(replacer::replace), Word.fromList(realPrefix), Word.fromList(realPath));
		}
	}
}
