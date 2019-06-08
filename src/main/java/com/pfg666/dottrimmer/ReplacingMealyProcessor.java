package com.pfg666.dottrimmer;

import com.pfg666.dotparser.fsm.mealy.MealyProcessor;

public class ReplacingMealyProcessor implements MealyProcessor<String, String>{
	
	private Replacer replacer;

	public ReplacingMealyProcessor(Replacer replacer) {
		this.replacer = replacer;
	}

	@Override
	public String processInput(String input) {
		return replacer.replace(input);
	}

	@Override
	public String processOutput(String output) {
		return replacer.replace(output);
	}
	

}
