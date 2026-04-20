#!/bin/bash

echo "🖥️  Starting Remote Control Server..."
echo ""

# Check if compiled
if [ ! -d "bin" ]; then
    echo "❌ Project not compiled yet!"
    echo "   Run: ./compile.sh"
    exit 1
fi

# Run server
java -cp "bin:lib/*" server.ServerMain
