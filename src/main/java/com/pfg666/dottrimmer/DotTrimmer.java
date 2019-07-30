package com.pfg666.dottrimmer;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;
import com.pfg666.dottrimmer.paths.ColoredPath;
import com.pfg666.dottrimmer.paths.ColoringDOTHelper;
import com.pfg666.dottrimmer.paths.ParsedColoredPath;
import com.pfg666.dottrimmer.paths.StateSelector;
import com.pfg666.dottrimmer.replacements.ReplacementGenerator;
import com.pfg666.dottrimmer.replacements.Replacer;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.graphs.dot.AggregateDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ListAlphabet;

public class DotTrimmer {
	private static Logger LOGGER = Logger.getGlobal();

	// the minimum number of grouped inputs that can be merged
	private int mergeThreshold;
	// the label replacing merged inputs
	private String mergeLabel;
	private DotTrimmerConfig config;

	public DotTrimmer(int mergeThreshold, String mergeLabel) {
		this.mergeThreshold = mergeThreshold;
		this.mergeLabel = mergeLabel;
	}

	public DotTrimmer(DotTrimmerConfig config) {
		this.mergeLabel = config.getMergeLabel();
		this.mergeThreshold = config.getMergeThreshold();
		this.config = config;
	}

	public FastMealy<String, String> generateSimplifiedMachine(FastMealy<String, String> mealy) {
		Alphabet<String> alphabet = mealy.getInputAlphabet();
		List<String> list = new ArrayList<>(alphabet);
		list.add(mergeLabel);
		FastMealy<String, String> trimmed = new FastMealy<String, String>(new ListAlphabet<>(list));
		Map<FastMealyState<String>, FastMealyState<String>> stateMap = new HashMap<>();
		for (FastMealyState<String> state : mealy.getStates()) {
			FastMealyState<String> newState = trimmed.addState(null);
			stateMap.put(state, newState);
		}
		constructSimplifiedMachine(mealy, trimmed, stateMap);
		return trimmed;
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
			if (maxSize > mergeThreshold) {
				Set<String> maxSet = symbolGrouping.stream().filter(g -> g.size() == maxSize).findFirst().get();
				maxSet.stream().map(inp -> mealy.getTransition(state, inp)).forEach(tr -> excludeTrans.add(tr));
				String input = maxSet.iterator().next();
				MealyTransition<FastMealyState<String>, String> trans = mealy.getTransition(state, input);
				MealyTransition<FastMealyState<String>, String> otherTrans = new MealyTransition<FastMealyState<String>, String>(
						stateMap.get(trans.getSuccessor()), trans.getOutput());
				otherState.setTransition(inputSize, otherTrans);
			}
			for (int i = 0; i < inputSize; i++) {
				MealyTransition<FastMealyState<String>, String> trans = state.getTransition(i);
				if (!excludeTrans.contains(state.getTransition(i))) {
					MealyTransition<FastMealyState<String>, String> otherTrans = new MealyTransition<FastMealyState<String>, String>(
							stateMap.get(trans.getSuccessor()), trans.getOutput());
					otherState.setTransition(i, otherTrans);
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

		LOGGER.info("Simplifying the model using the Other input construct");
		FastMealy<String, String> trimmedMealy = generateSimplifiedMachine(mealy);
		String trimmedModelFile = getOutputFile();

		LOGGER.info("Applying coloring");
		List<GraphDOTHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>> helpers = generateHelpers(
				trimmedMealy, gen.getReplacer());

		AggregateDOTHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>> helper = new AggregateDOTHelper<>();
		helpers.forEach(h -> helper.add(h));

		LOGGER.info("Exporting the model to .dot");
		GraphDOT.write(trimmedMealy, trimmedMealy.getInputAlphabet(), new FileWriter(trimmedModelFile), helper);
		return new DotTrimmerResult(trimmedModelFile, trimmedMealy);
	}

	// generics bonanza
	private List<GraphDOTHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>> generateHelpers(
			FastMealy<String, String> trimmedMealy, Replacer replacer) {
		Alphabet<String> inputs = trimmedMealy.getInputAlphabet();
		List<GraphDOTHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>> helpers = new LinkedList<>();
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
				System.out.println("Returned edges: " + edges.size());
				System.out.println(edges);
				Set<TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>> transitions = edges
						.stream().map(e -> e.asTransitionEdge()).collect(Collectors.toSet());
				helpers.add(
						new ColoringDOTHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>(
								transitions, color));
			}
		}

		if (config.getColoredPathsFile() != null) {
			StateSelector<FastMealyState<String>, String, MealyTransition<FastMealyState<String>, String>, String> stateSelector = new StateSelector<>();
			ColoredPath[] paths = ColoredPath.loadColoredPaths(config.getColoredPathsFile());
			for (ColoredPath path : paths) {
				ParsedColoredPath<String> parsedColorPath = parseColorPath(path, replacer, trimmedMealy);
				Set<FastMealyState<String>> states = stateSelector.selectStates(trimmedMealy, parsedColorPath);
				
				Set<EdgeInfo<FastMealyState<String>, String, MealyTransition<FastMealyState<String>, String>>> edges = collector
						.getEdgesForPath(trimmedMealy, inputs, parsedColorPath.getPathWord(), states);
				if (!edges.isEmpty()) {
					Set<TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>> transitions = edges
							.stream().map(e -> e.asTransitionEdge()).collect(Collectors.toSet());
					helpers.add(
							new ColoringDOTHelper<FastMealyState<String>, TransitionEdge<String, MealyTransition<FastMealyState<String>, String>>>(
									transitions, path.getColor()));
				}
			}
		}

		return helpers;
	}
	
	private ParsedColoredPath<String> parseColorPath(ColoredPath path, Replacer replacer, FastMealy<String, String> trimmedMealy) {
		List<String> realPath = path.getPath().stream().collect(Collectors.toList());
		List<String> realPrefix = path.getPrefix().stream().collect(Collectors.toList());
		return new ParsedColoredPath<>(path, Word.fromList(realPrefix), Word.fromList(realPath));
	}
}
