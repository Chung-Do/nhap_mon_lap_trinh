#!/bin/bash
# Build and Package script for Remote Control Application (Unix/Mac)

set -e  # Exit on error

echo "======================================"
echo "Remote Control App - Build Script"
echo "======================================"
echo ""

# Step 1: Build with Maven
echo "Step 1: Building with Maven..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ ERROR: Maven build failed!"
    echo "Please check your Java installation and pom.xml configuration"
    exit 1
fi

echo ""
echo "✅ Maven build completed successfully"
echo ""

# Step 2: Package into ZIP files
echo "Step 2: Packaging into ZIP files..."
bash ./scripts/package.sh
mvn clean package -Pclient -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Client build failed!"
    exit 1
fi

echo "✅ Client JAR built: target/remote-client.jar"

# Step 3: Create output directory
mkdir -p build

# Step 4: Convert to EXE (if Launch4j is installed)
if command -v launch4jc &> /dev/null; then
    echo ""
    echo "Step 3: Converting JARs to EXE..."
    launch4jc launch4j-server.xml
    launch4jc launch4j-client.xml
    echo "✅ EXE files created in build/"
else
    echo ""
    echo "⚠️  Launch4j not found. Skipping EXE conversion."
    echo "    Install Launch4j from: https://launch4j.sourceforge.net/"
    echo "    Or run JARs directly with: java -jar target/remote-server.jar"
fi

echo ""
echo "=== Build Complete! ==="
echo ""
echo "📦 Artifacts:"
echo "  - target/remote-server.jar"
echo "  - target/remote-client.jar"
if [ -f "build/RemoteServer.exe" ]; then
    echo "  - build/RemoteServer.exe"
    echo "  - build/RemoteClient.exe"
fi

echo ""
echo "🚀 To run:"
echo "  Server: java -jar target/remote-server.jar"
echo "  Client: java -jar target/remote-client.jar"
