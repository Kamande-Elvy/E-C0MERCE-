#!/usr/bin/env bash
set -e
LIBS=/usr/share/java/sqlite-jdbc.jar:/usr/share/java/slf4j-api.jar:/usr/share/java/slf4j-nop.jar
mkdir -p out
javac -d out *.java
java -cp "out:$LIBS" Main