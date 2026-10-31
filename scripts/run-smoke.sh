#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/classes build/test-classes
find src/main/java -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d build/classes
cp src/main/resources/feature_model.xml build/classes/feature_model.xml
javac -encoding UTF-8 -cp build/classes -d build/test-classes src/test/java/be/uclouvain/sme33/SmokeCheck.java
java -Djava.awt.headless=true -cp build/classes:build/test-classes SmokeCheck
