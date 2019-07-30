package com.pfg666.dottrimmer.replacements;

import java.util.Arrays;

public class Replacer {
	private  ReplacementRule [] rules;
	
	public Replacer() {
		rules = new ReplacementRule [] {};
	}
	
	public Replacer(ReplacementRule [] rules) {
		this.rules = rules;
	}
	
	public String replace(String string) {
		String result = string;
		for (ReplacementRule rule : rules) {
			if (rule.isApplicable(result)) {
				result =  rule.apply(result);
				if (rule.isFinal()) {
					return result;
				}
			}
		}
		
		return result;
	}

	@Override
	public String toString() {
		return String.format("Replacer [rules=%s]", Arrays.toString(rules));
	}

}
