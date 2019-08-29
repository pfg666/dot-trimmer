package com.pfg666.dottrimmer.paths;

import net.automatalib.words.Word;

public class ParsedColoredPath<I>{
	
	public ParsedColoredPath(ColoredPath coloredPath, Word<I> prefixWord, Word<I> pathWord) {
		this.coloredPath = coloredPath;
		this.pathWord = pathWord;
		this.prefixWord = prefixWord;
	}
	
	private ColoredPath coloredPath;
	private Word<I> pathWord;
	private Word<I> prefixWord;
	
	public ColoredPath getColoredPath() {
		return coloredPath;
	}
	
	public Word<I> getPathWord() {
		return pathWord;
	}	
	public Word<I> getPrefixWord() {
		return prefixWord;
	}
}
