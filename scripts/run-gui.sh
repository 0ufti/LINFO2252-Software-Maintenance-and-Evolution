#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/classes
find src/main/java -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d build/classes
cp src/main/resources/feature_model.xml build/classes/feature_model.xml
java -cp build/classes Main --ui --console
