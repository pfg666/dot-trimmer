package com.pfg666.dottrimmer.replacements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacementRule {
	private String replaceIf;
	private String replacee;
	private String replacement;
	private boolean isFinal;
	private SpecialReplacementFunction function;
	
	public ReplacementRule() {
		isFinal = false;
		replaceIf = ".*";
	}

	public ReplacementRule(String replaceIf, String replacee, String replacement, boolean isFinal) {
		this.replaceIf = replaceIf;
		this.replacee = replacee;
		this.replacement = replacement;
		this.isFinal  = isFinal;
	}
	
	public boolean isApplicable(String input) {
		return input.matches(replaceIf);
	}
	
	public String apply(String input) {
		if (function == null) {
			return input.replaceAll(replacee, replacement);
		} else {
			if (replacee == null) {
				return function.applyReplacementFunction(input);
			} else {
				Pattern p = Pattern.compile(replacee);
				Matcher match = p.matcher(input);
				StringBuffer buffer = new StringBuffer();
				while(match.find()) {
					String matchReplacement = function.applyReplacementFunction(match.group());
					match.appendReplacement(buffer, matchReplacement);
				}
				match.appendTail(buffer);
				return buffer.toString();
			}
			
		}
	}
	
	public boolean isFinal() {
		return isFinal;
	}

	@Override
	public String toString() {
		return String.format("ReplacementRule [replaceIf=%s, replacee=%s, replacement=%s, isFinal=%s]", replaceIf,
				replacee, replacement, isFinal);
	}
	
}
