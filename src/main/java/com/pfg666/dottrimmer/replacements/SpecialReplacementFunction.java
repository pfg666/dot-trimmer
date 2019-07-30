package com.pfg666.dottrimmer.replacements;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CaseFormat;

/**
 * Some hard coded replacement functions for convenience, some of which
 * cannot be expressed by replacement rules.
 */
public enum SpecialReplacementFunction {
	SNAKE_TO_CAMEL(
			SpecialReplacementFunction::snakeToCamel
			),
	
	CAMEL_TO_SNAKE(
			s -> {
				return CaseFormat.UPPER_CAMEL
						.to(CaseFormat.UPPER_UNDERSCORE, s);
			}
			);
	
	private static Pattern SNAKE_PATTERN;
	
	private Function<String, String> replFunction;
	
	SpecialReplacementFunction(Function<String, String> replFunction) {
		this.replFunction = replFunction;
	}
	
	public String applyReplacementFunction(String string) {
		return replFunction.apply(string);
	}
	
	
	private static Pattern getSnakePattern() {
		if (SNAKE_PATTERN == null)
			SNAKE_PATTERN = Pattern.compile("([A-Z]+)_?");
		return SNAKE_PATTERN;
	}
	
	/*
	 * The Java equivalent of: sed -r 's/([A-Z])([A-Z]*)_*\/\\1\\L\\2\/g'
	 */
	private static String snakeToCamel(String s) {
		Pattern pattern = getSnakePattern();
		Matcher matcher = pattern.matcher(s);
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
			String upperCased = matcher.group();
			if (upperCased.length() == 1)
				continue;
			StringBuilder builder = new StringBuilder();
			builder.append(upperCased.charAt(0));
			if (upperCased.endsWith("_"))
				builder.append(upperCased.toLowerCase().substring(1, upperCased.length()-1));
			else 
				builder.append(upperCased.toLowerCase().substring(1));
			String camelCased = builder.toString();
			matcher.appendReplacement(sb, camelCased);
		}
		matcher.appendTail(sb);
		String result = sb.toString();
		return result;
	}
 }
