package com.pfg666.dottrimmer;

public class ReplacementRule {
	private String replaceIf;
	private String replacee;
	private String replacement;
	private boolean isFinal;
	private SpecialReplacementFunction function;
	
	public ReplacementRule() {
		isFinal = false;
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
			return function.applyReplacementFunction(replacee);
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
