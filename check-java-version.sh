#!/bin/bash

# Script to check Java version compatibility for Android builds
# This helps detect Java version mismatches before attempting to build

set -e

# Function to extract Java major version from build.gradle
# First checks root build.gradle for subprojects configuration,
# then falls back to app/build.gradle
extract_gradle_java_version() {
    local app_gradle_file="$1"
    local root_gradle_file="${app_gradle_file%/app/build.gradle}/build.gradle"
    
    # First try to find version in root build.gradle subprojects block
    local version=""
    if [ -f "$root_gradle_file" ]; then
        version=$(grep -A 15 "subprojects" "$root_gradle_file" | \
            grep "sourceCompatibility" | \
            sed 's/.*VERSION_//' | \
            sed 's/[^0-9]*\([0-9]\+\).*/\1/' | \
            head -1)
    fi
    
    # If not found in root, try app/build.gradle with direct android.compileOptions pattern
    if [ -z "$version" ]; then
        version=$(grep -A 5 "android.compileOptions" "$app_gradle_file" | \
            grep "sourceCompatibility" | \
            sed 's/.*VERSION_//' | \
            sed 's/[^0-9]*\([0-9]\+\).*/\1/' | \
            head -1)
    fi
    
    # If still not found, try looking in afterEvaluate block in app/build.gradle
    if [ -z "$version" ]; then
        version=$(grep -A 10 "afterEvaluate" "$app_gradle_file" | \
            grep "sourceCompatibility" | \
            sed 's/.*VERSION_//' | \
            sed 's/[^0-9]*\([0-9]\+\).*/\1/' | \
            head -1)
    fi
    
    echo "$version"
}

# Function to extract Java major version from capacitor.build.gradle
extract_capacitor_java_version() {
    local capacitor_file="$1"
    grep "sourceCompatibility" "$capacitor_file" | \
        sed 's/.*VERSION_//' | \
        sed 's/[^0-9]*\([0-9]\+\).*/\1/' | \
        head -1
}

# Function to extract current Java major version
get_current_java_version() {
    # Extract major version from various Java version formats
    # Examples: "17.0.18", "1.8.0_292", "21", "11.0.12"
    java -version 2>&1 | grep -i version | \
        sed 's/.*"\([0-9.]*\)".*/\1/' | \
        sed 's/^1\.//' | \
        sed 's/\..*//' | \
        head -1
}

echo "🔍 Checking Java version compatibility..."
echo ""

# Check if Android project exists
if [ ! -f "frontend/android/app/build.gradle" ]; then
    echo "⚠️  Android project not found, skipping Java version validation"
    exit 0
fi

# Get current Java version
JAVA_VERSION=$(get_current_java_version)
if [ -z "$JAVA_VERSION" ]; then
    echo "❌ Error: Could not detect current Java version"
    echo "Please ensure Java is installed and available in PATH"
    exit 1
fi
echo "Current Java version: $JAVA_VERSION"

# Extract Java version from build.gradle
GRADLE_JAVA_VERSION=$(extract_gradle_java_version "frontend/android/app/build.gradle")
if [ -z "$GRADLE_JAVA_VERSION" ]; then
    echo "❌ Error: Could not extract Java version from build.gradle"
    echo "Please ensure frontend/android/app/build.gradle contains android.compileOptions with sourceCompatibility"
    exit 1
fi
echo "Required Java version (build.gradle): $GRADLE_JAVA_VERSION"

# Extract Java version from capacitor.build.gradle if it exists
if [ -f "frontend/android/app/capacitor.build.gradle" ]; then
    CAPACITOR_JAVA_VERSION=$(extract_capacitor_java_version "frontend/android/app/capacitor.build.gradle")
    if [ -n "$CAPACITOR_JAVA_VERSION" ]; then
        echo "Capacitor-generated Java version: $CAPACITOR_JAVA_VERSION"
        
        if [ "$CAPACITOR_JAVA_VERSION" != "$GRADLE_JAVA_VERSION" ]; then
            echo ""
            echo "⚠️  Warning: Capacitor generated Java $CAPACITOR_JAVA_VERSION, but build.gradle overrides to Java $GRADLE_JAVA_VERSION"
            echo "   This is expected - build.gradle override takes precedence"
        fi
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
