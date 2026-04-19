#!/bin/bash
# Bash script to package Client and Server into ZIP files
# Usage: bash ./scripts/package.sh

set -e  # Exit on error

echo ""
echo "======================================"
echo "Remote Control App - Packaging Script"
echo "======================================"
echo ""

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
TARGET_PATH="$PROJECT_ROOT/target"
DIST_PATH="$PROJECT_ROOT/dist"
OUT_PATH="$PROJECT_ROOT"

# Check if JAR files exist
echo "Checking for JAR files..."
if [ ! -f "$TARGET_PATH/remote-client.jar" ]; then
    echo "❌ ERROR: remote-client.jar not found!"
    echo "Please run: mvn clean package -DskipTests"
    exit 1
fi

if [ ! -f "$TARGET_PATH/remote-server.jar" ]; then
    echo "❌ ERROR: remote-server.jar not found!"
    echo "Please run: mvn clean package -DskipTests"
    exit 1
fi

echo "✓ JAR files found"
echo ""

# Clean up old dist folder
echo "Cleaning up old dist folder..."
rm -rf "$DIST_PATH" 2>/dev/null || true
echo "✓ Cleaned"
echo ""

# Create Client package structure
echo "Creating Client package structure..."
mkdir -p "$DIST_PATH/remote-client"
cp "$TARGET_PATH/remote-client.jar" "$DIST_PATH/remote-client/"

# Create Client README
cat > "$DIST_PATH/remote-client/README.txt" << 'EOF'
===============================================================
REMOTE CONTROL CLIENT APPLICATION
===============================================================

Version: 1.0.0
Language: Java 11+
Author: Your Name
Date: Run this from the extracted ZIP

PREREQUISITES:
- Java 11 or higher installed
- Network connectivity to Server (LAN)

INSTALLATION:
1. Extract remote-client.zip to a folder
2. Ensure Java is in your system PATH
3. Run: java -jar remote-client.jar

FEATURES:
- List/Start/Stop Applications
- List/Start/Stop/Kill Processes
- Screenshot Capture
- Keylogger
- File Transfer (Download/Upload)
- System Control (Shutdown/Restart)
- Webcam Stream
- Network Monitoring
- Remote Desktop
- System Lock

TROUBLESHOOTING:
Q: "Java not found"
A: Install Java 11+ from https://adoptium.net/

Q: Cannot connect to server
A: Ensure server is running and both machines are on same LAN
   Check firewall settings (port 8888)

For more info, see: log.md in project root

===============================================================
EOF

# Create Client launcher script
cat > "$DIST_PATH/remote-client/run.sh" << 'EOF'
#!/bin/bash
# Launch Remote Control Client
# Prerequisites: Java 11+

JAR_FILE="remote-client.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: $JAR_FILE not found in current directory"
    echo "Please ensure you extracted all files from the ZIP"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found!"
    echo "Please install Java 11+ from https://adoptium.net/"
    exit 1
fi

echo "Starting Remote Control Client..."
java -jar "$JAR_FILE"
EOF

chmod +x "$DIST_PATH/remote-client/run.sh"

# Create Client config template
cat > "$DIST_PATH/remote-client/config.properties" << 'EOF'
# Remote Control Client Configuration
# Edit this file to customize behavior

# Server connection settings
server.host=192.168.1.100
server.port=8888
connection.timeout=10000

# GUI settings
gui.window.width=1200
gui.window.height=800
gui.log.buffer.lines=1000

# Screenshot settings
screenshot.quality=90
screenshot.format=PNG

# Webcam settings
webcam.fps=15
webcam.quality=80

# File transfer settings
file.chunk.size=1048576

# Network monitoring settings
monitor.refresh.interval=1000
EOF

echo "✓ Client package created"
echo ""

# Create Server package structure
echo "Creating Server package structure..."
mkdir -p "$DIST_PATH/remote-server"
cp "$TARGET_PATH/remote-server.jar" "$DIST_PATH/remote-server/"

# Create Server README
cat > "$DIST_PATH/remote-server/README.txt" << 'EOF'
===============================================================
REMOTE CONTROL SERVER APPLICATION
===============================================================

Version: 1.0.0
Language: Java 11+
Author: Your Name
Date: Run this from the extracted ZIP

PREREQUISITES:
- Java 11 or higher installed
- Network connectivity to Client (LAN)

INSTALLATION:
1. Extract remote-server.zip to a folder
2. Ensure Java is in your system PATH
3. Run: java -jar remote-server.jar

FEATURES:
- Application Management
- Process Control
- Screen Capture
- Keylogger
- File Transfer (Upload/Download)
- System Control (Shutdown/Restart)
- Webcam Capture
- Network Monitoring
- Remote Desktop
- System Lock

TROUBLESHOOTING:
Q: "Java not found"
A: Install Java 11+ from https://adoptium.net/

Q: "Port 8888 already in use"
A: Edit config.properties to use different port

Q: Cannot receive commands from client
A: Check firewall - ensure TCP port 8888 is open
   Run: netstat -an | grep LISTENING

For more info, see: log.md in project root

===============================================================
EOF

# Create Server launcher script
cat > "$DIST_PATH/remote-server/run.sh" << 'EOF'
#!/bin/bash
# Launch Remote Control Server
# Prerequisites: Java 11+

JAR_FILE="remote-server.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: $JAR_FILE not found in current directory"
    echo "Please ensure you extracted all files from the ZIP"
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found!"
    echo "Please install Java 11+ from https://adoptium.net/"
    exit 1
fi

echo "Starting Remote Control Server..."
java -jar "$JAR_FILE"
EOF

chmod +x "$DIST_PATH/remote-server/run.sh"

# Create Server config template
cat > "$DIST_PATH/remote-server/config.properties" << 'EOF'
# Remote Control Server Configuration
# Edit this file to customize behavior

# Server listening settings
server.port=8888
server.listen.timeout=30000

# GUI settings
gui.window.width=800
gui.window.height=600
gui.show.on.startup=true
gui.log.buffer.lines=1000

# System control settings
allow.system.shutdown=true
allow.system.restart=true
allow.lock.system=true

# File transfer settings
file.chunk.size=1048576
file.transfer.timeout=300000

# Keylogger settings
keylogger.enabled=true
keylogger.log.file=keylog.txt

# Webcam settings
webcam.enabled=true
webcam.fps=15
webcam.quality=80

# Network monitoring settings
monitor.enabled=true
monitor.refresh.interval=1000
EOF

echo "✓ Server package created"
echo ""

# Create ZIP files
echo "Creating ZIP packages..."

# Client ZIP
if [ -f "$OUT_PATH/remote-client.zip" ]; then
    rm "$OUT_PATH/remote-client.zip"
fi
cd "$DIST_PATH/remote-client"
zip -r "$OUT_PATH/remote-client.zip" . > /dev/null
cd - > /dev/null
echo "✓ Created: remote-client.zip"

# Server ZIP
if [ -f "$OUT_PATH/remote-server.zip" ]; then
    rm "$OUT_PATH/remote-server.zip"
fi
cd "$DIST_PATH/remote-server"
zip -r "$OUT_PATH/remote-server.zip" . > /dev/null
cd - > /dev/null
echo "✓ Created: remote-server.zip"

echo ""
echo "======================================"
echo "✓ PACKAGING COMPLETED SUCCESSFULLY"
echo "======================================"
echo ""
echo "Output files:"
echo "  • $OUT_PATH/remote-client.zip"
echo "  • $OUT_PATH/remote-server.zip"
echo ""
echo "Next step: Extract ZIPs and run run.sh"
echo ""
