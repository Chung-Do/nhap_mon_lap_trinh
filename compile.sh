#!/bin/bash

echo "🔨 Compiling Remote Control Application..."
echo ""

# Create directories
mkdir -p bin
mkdir -p logs
mkdir -p lib

# Check if JSON library exists
if [ ! -f "lib/json-20230227.jar" ]; then
    echo "⚠️  JSON library not found. Downloading..."
    cd lib
    curl -O https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar
    cd ..
    echo "✅ JSON library downloaded"
fi

# Compile all Java files
echo "📦 Compiling source files..."
javac -d bin -cp "lib/*" $(find src -name "*.java")

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Compilation successful!"
    echo ""
    echo "📁 Output: bin/"
    echo "📝 Logs will be saved to: logs/"
    echo ""
    echo "▶️  To run:"
    echo "   Server: ./run-server.sh"
    echo "   Client: ./run-client.sh"
else
    echo ""
    echo "❌ Compilation failed!"
    echo "Please check the error messages above."
    exit 1
fi
