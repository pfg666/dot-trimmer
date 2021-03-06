#!/bin/bash
# this script installs the .jars to the local maven repo
LIB_DIR=./src/main/resources/lib
mvn install:install-file -Dfile=$LIB_DIR/com.alexmerz.graphviz.jar -DgroupId=com.alexmerz.graphviz -DartifactId=graphviz -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=$LIB_DIR/dot-parser-0.1.jar -DgroupId=com.pfg666.dotparser -DartifactId=dot-parser -Dversion=0.1 -Dpackaging=jar -DgeneratePom=true
