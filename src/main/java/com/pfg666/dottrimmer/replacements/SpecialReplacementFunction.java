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
	UPPER_SNAKE_TO_UPPER_CAMEL(
			SpecialReplacementFunction::upperSnakeToUpperCamel
			),
	
	UPPER_CAMEL_TO_UPPER_SNAKE(
			s -> {
				return CaseFormat.UPPER_CAMEL
						.to(CaseFormat.UPPER_UNDERSCORE, s);
			}
			),
	UPPER_CAMEL_TO_LOWER_SNAKE(
			SpecialReplacementFunction::upperCamelToLowerSnake
			);
	
	private static Pattern UPPER_SNAKE_PATTERN;
	
	private Function<String, String> replFunction;
	
	SpecialReplacementFunction(Function<String, String> replFunction) {
		this.replFunction = replFunction;
	}
	
	public String applyReplacementFunction(String string) {
		return replFunction.apply(string);
	}
	
	
	private static Pattern getUpperSnakePattern() {
		if (UPPER_SNAKE_PATTERN == null)
			UPPER_SNAKE_PATTERN = Pattern.compile("([A-Z]+)_?");
		return UPPER_SNAKE_PATTERN;
	}
	
	/*
	 * The Java equivalent of: sed -r 's/([A-Z])([A-Z]*)_*\/\\1\\L\\2\/g'
	 */
	private static String upperSnakeToUpperCamel(String s) {
		Pattern pattern = getUpperSnakePattern();
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
	
	private static String upperCamelToLowerSnake(String s) {
		String result = CaseFormat.UPPER_CAMEL
				.to(CaseFormat.LOWER_UNDERSCORE, s);
		if (result.startsWith("_")) {
			result = result.substring(1);
		}
		return result;
	}
 }
