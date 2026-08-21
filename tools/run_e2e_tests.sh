#!/bin/sh
# ==============================================================================
# HyperCeiler E2E Test Suite Automated Runner Harness
# Executes Tiers 1-4 End-to-End Tests for iOS Control Center Modifications
# ==============================================================================

set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
TEST_SRC_DIR="${PROJECT_ROOT}/tests/src/com/sevtinge/hyperceiler/test"
BUILD_CLASSES_DIR="${PROJECT_ROOT}/build/test_classes"

# Ensure test classes directory exists
mkdir -p "${BUILD_CLASSES_DIR}"

# Locate JDK / Java Runtime and Compile
if [ -d "/data/data/com.termux/files/home/jdk-17.0.12+7" ] && [ -x "/data/data/com.termux/files/usr/bin/proot" ]; then
    /data/data/com.termux/files/usr/bin/proot \
        -b /data/data/com.termux/files/usr/glibc/lib:/lib \
        -b /data/data/com.termux/files/usr/tmp:/tmp \
        /data/data/com.termux/files/home/jdk-17.0.12+7/bin/javac \
        -d "${BUILD_CLASSES_DIR}" "${TEST_SRC_DIR}"/*.java

    /data/data/com.termux/files/usr/bin/proot \
        -b /data/data/com.termux/files/usr/glibc/lib:/lib \
        -b /data/data/com.termux/files/usr/tmp:/tmp \
        /data/data/com.termux/files/home/jdk-17.0.12+7/bin/java \
        -cp "${BUILD_CLASSES_DIR}" \
        com.sevtinge.hyperceiler.test.E2ETestSuiteRunner
elif command -v java >/dev/null 2>&1 && command -v javac >/dev/null 2>&1; then
    javac -d "${BUILD_CLASSES_DIR}" "${TEST_SRC_DIR}"/*.java
    java -cp "${BUILD_CLASSES_DIR}" com.sevtinge.hyperceiler.test.E2ETestSuiteRunner
elif [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ] && [ -x "${JAVA_HOME}/bin/javac" ]; then
    "${JAVA_HOME}/bin/javac" -d "${BUILD_CLASSES_DIR}" "${TEST_SRC_DIR}"/*.java
    "${JAVA_HOME}/bin/java" -cp "${BUILD_CLASSES_DIR}" com.sevtinge.hyperceiler.test.E2ETestSuiteRunner
else
    echo "ERROR: Java 17+ JDK runtime could not be located." >&2
    exit 1
fi
