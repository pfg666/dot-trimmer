# dot-trimmer
A java tool for trimming/improving the looks of Mealy machine .dot models using Graphviz and automatalib libraries.

The most important simplification currently supported is, for each state, replacing the largest set of inputs prompting the same behavior by the input "Other". 

Also supported are *replacement rules* over the inputs/outputs. These rules comprise 4 components:
* a matching string, an input/output should match this string in order for the rule to be triggered
* replacee string, a regex to be replaced
* replacement string, a regex describing the replacement
* final, determines whether this is the last rule to be processed.

Rules are processed in the order they appear in the replacements .json file, and are applied sequentially on the input/output, in a pipeline fashion. In case a rule is final or the end of the .json file is reached, the resulting input/output is returned, and is written in the place of the old input/output. 

Things I hope to add in the future are coloring, which could be done in two ways:
1. given some sequences of inputs, color their corresponding transitions
2. given a state, color the transitions in all possible paths to the state

But we shall see if I get the time.

