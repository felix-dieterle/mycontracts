#!/bin/bash

# Script to check Java version compatibility for Android builds
# This helps detect Java version mismatches before attempting to build

set -e

echo "🔍 Checking Java version compatibility..."
echo ""

# Check if Android project exists
if [ ! -f "frontend/android/app/build.gradle" ]; then
    echo "⚠️  Android project not found, skipping Java version validation"
    exit 0
fi

# Get current Java version
JAVA_VERSION=$(java -version 2>&1 | grep -i version | sed 's/.*version "\([0-9]*\).*/\1/')
echo "Current Java version: $JAVA_VERSION"

# Extract Java version from build.gradle
GRADLE_JAVA_VERSION=$(grep -A 2 "android.compileOptions" frontend/android/app/build.gradle | grep "sourceCompatibility" | sed 's/.*VERSION_//' | sed 's/[^0-9]//g' | head -1)
echo "Required Java version (build.gradle): $GRADLE_JAVA_VERSION"

# Extract Java version from capacitor.build.gradle if it exists
if [ -f "frontend/android/app/capacitor.build.gradle" ]; then
    CAPACITOR_JAVA_VERSION=$(grep "sourceCompatibility" frontend/android/app/capacitor.build.gradle | sed 's/.*VERSION_//' | sed 's/[^0-9]//g' | head -1)
    echo "Capacitor-generated Java version: $CAPACITOR_JAVA_VERSION"
    
    if [ "$CAPACITOR_JAVA_VERSION" != "$GRADLE_JAVA_VERSION" ]; then
        echo ""
        echo "⚠️  Warning: Capacitor generated Java $CAPACITOR_JAVA_VERSION, but build.gradle overrides to Java $GRADLE_JAVA_VERSION"
        echo "   This is expected - build.gradle override takes precedence"
    fi
fi

echo ""

# Check if current Java version matches required version
if [ "$JAVA_VERSION" != "$GRADLE_JAVA_VERSION" ]; then
    echo "❌ Error: Java version mismatch!"
    echo ""
    echo "Your current Java version is $JAVA_VERSION, but the Android build requires Java $GRADLE_JAVA_VERSION"
    echo ""
    echo "Please install Java $GRADLE_JAVA_VERSION or update frontend/android/app/build.gradle to match your Java version"
    echo ""
    echo "To check your Java version: java -version"
    exit 1
fi

echo "✅ Java version check passed: Java $JAVA_VERSION"
echo ""
echo "You can proceed with the Android build:"
echo "  cd frontend/android && ./gradlew assembleDebug"
