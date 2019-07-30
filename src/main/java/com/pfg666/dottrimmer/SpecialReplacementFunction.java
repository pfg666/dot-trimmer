package com.pfg666.dottrimmer;

import java.util.function.Function;

import com.google.common.base.CaseFormat;

/**
 * Some hard coded replacement functions for convenience, some of which
 * cannot be expressed by replacement rules.
 */
public enum SpecialReplacementFunction {
	SNAKE_TO_CAMEL(
			s -> {
				return CaseFormat.UPPER_UNDERSCORE
						.to(CaseFormat.UPPER_CAMEL, s);
			}
			),
	
	CAMEL_TO_SNAKE(
			s -> {
				return CaseFormat.UPPER_CAMEL
						.to(CaseFormat.UPPER_UNDERSCORE, s);
			}
			);
	
	private Function<String, String> replFunction;
	
	SpecialReplacementFunction(Function<String, String> replFunction) {
		this.replFunction = replFunction;
	}
	
	public String applyReplacementFunction(String string) {
		return replFunction.apply(string);
	}
 }
