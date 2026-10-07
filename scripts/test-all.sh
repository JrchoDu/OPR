#!/usr/bin/env sh
set -eu
./scripts/test-cobol.sh
mvn -f desktop/pom.xml test
