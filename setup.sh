#!/bin/bash
# KeysCafe Keyboard - Quick Setup Script

echo "🔥 KeysCafe Keyboard Setup"
echo "=========================="

# Check Java version
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d '"' -f 2)
    echo "✅ Java found: $JAVA_VERSION"
else
    echo "❌ Java not found. Please install JDK 17."
    exit 1
fi

# Check Android SDK
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_SDK_ROOT not set. Please set it to your Android SDK path."
else
    echo "✅ Android SDK found"
fi

# Make gradlew executable
chmod +x gradlew 2>/dev/null || true

echo ""
echo "📦 Building project..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📱 APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "To install on device:"
    echo "  ./gradlew installDebug"
    echo ""
    echo "To run tests:"
    echo "  ./gradlew test"
else
    echo ""
    echo "❌ Build failed. Check errors above."
    exit 1
fi
