#!/bin/sh
set -eu

cd -- "$(dirname -- "$0")"
mkdir -p out
javac -encoding UTF-8 -sourcepath src -d out src/Main.java
exec java -cp out Main "$@"
