#!/usr/bin/env bash
set -euo pipefail

echo "[Pyrium] Building Java modules..."
mvn -q -f pyrium-core/pom.xml clean package
mvn -q -f pyrium-rpb/pom.xml clean package
mvn -q -f pyrium-bootstrap/pom.xml clean package

echo "[Pyrium] Setting up Python AOT environment..."
python3 -m venv .venv
. .venv/bin/activate
pip -q install --upgrade pip
pip -q install -e pyrium-aot

echo "[Pyrium] Build complete."
