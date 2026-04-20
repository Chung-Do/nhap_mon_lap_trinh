#!/bin/bash

echo "💻 Starting Remote Control Client..."
echo ""

# Check if compiled
if [ ! -d "bin" ]; then
    echo "❌ Project not compiled yet!"
    echo "   Run: ./compile.sh"
    exit 1
fi

# Run client
java -cp "bin:lib/*" client.ClientMain
