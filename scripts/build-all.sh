#!/usr/bin/env sh
set -eu
./scripts/build-cobol.sh
./scripts/build-java.sh
./scripts/test-all.sh
