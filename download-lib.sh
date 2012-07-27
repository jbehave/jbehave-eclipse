#!/bin/bash

mvn org.apache.maven.plugins:maven-dependency-plugin:2.4:copy-dependencies -forg.jbehave.eclipse/pom.xml -Ddest=org.jbehave.eclipse/lib

