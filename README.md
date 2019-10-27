# dot-trimmer
A java tool for trimming/improving the looks of Mealy machine .dot models using Graphviz and automatalib libraries.
It assumes the model is a Mealy machine, wherein each .dot edge corresponding to a transition contains a label describing it in the form "input / output".

# Installing
To install, simply run:
``` 
bash prepare.sh
mvn install 
```
# Use Cases
The main use cases of dot-trimmer are:
* removing inputs
* merging transitions with the same outputs and lead to the same state under a custom input label
* removing states not on path to some end-goal output which is identified by a regex expression
* coloring edges in the resulting .dot file
* replacing inputs/outputs by applying string replacement transformations

## Remove Inputs
dot-trimmer allows you to remove inputs from a given .dot file. After removal, dot-trimmer will minimize the model before exporting it to .dot. 
``` 
java -jar dot-trimmer.jar --input aut.dot --removeInputs "INP1,INP2,INP3"
```
## Merge Transitions
dot-trimmer can merge transitions which have the same output and lead to the same state, replacing them by a single transition with a custom input and the common output. By default, dot-trimmer uses Other as the custom input, but the user can choose a different string. To enable merging transitions, the user needs the supply *thresh*, which is the minium number of transitions with same output for which merging can be performed. Example:
``` 
java -jar dot-trimmer.jar --input aut.dot --thresh 3 --label "DifferentOther"
```

## State Removal based on Output
dot-trimmer can remove states for which there does not exist a path to a transition carrying an output corresponding to a regex. This functionality can be useful in analyzing models of protocol implementations, which may contain many states, but few states from which a handshake can be completed. The latter likely are more interesting. If a state of handshake completion can be identified by some output (we term, an end-goal output) which does not occur in other states, the user can use this output to retain only interesting states (states en-route to a handshake). For TLS, an end-goal output may be "Application", the command becoming:

```
java -jar dot-trimmer.jar --input tls-model.dot -ego "Application"
```

## Coloring Edges
dot-trimmer has basic support for coloring transitions of a .dot model:
* color all transitions on way from the initial state to a given state (there is primitive support for this currently)
* color all transitions traversed by executing supplied sequences of inputs

This function requires a .json describing the coloring, see the example directory.

## Replacing Input/Output Labels
dot-trimmer allows a user to apply *replacement rules* over the inputs/outputs. These rules comprise 4 components:
* a matching string, an input/output should match this string in order for the rule to be triggered
* replacee string, a regex to be replaced
* replacement string, a regex describing the replacement
* final, determines whether this is the last rule to be processed.

Rules are processed in the order they appear in the replacements .json file, and are applied sequentially on the input/output, in a pipeline fashion. In case a rule is final or the end of the .json file is reached, the resulting input/output is returned, and is written in the place of the old input/output. The rules are described in .json, see the example directory.

# TODO
Things I hope to add in the future are coloring, which could be done in two ways:
1. better algorithm for state-based coloring, ability to choose two states between which all transitions are colored
2. being able to configure which trimming/coloring options to apply
3. a more generic framework for applying transformations. These might be performing string replacements, merging transitions, coloring states ...

But we shall see if I find the time to do this.

