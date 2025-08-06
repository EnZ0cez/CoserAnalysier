#!/bin/bash

# AI Social Media Agent Startup Script

echo "🤖 AI Social Media Agent - Startup Script"
echo "======================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version check passed"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6+."
    exit 1
fi

echo "✅ Maven check passed"

# Check if Ollama is running
echo "🔍 Checking Ollama service..."
if ! curl -s http://localhost:11434/api/tags &> /dev/null; then
    echo "❌ Ollama is not running. Please start Ollama first:"
    echo "   1. Install Ollama: https://ollama.ai/"
    echo "   2. Start Ollama: ollama serve"
    echo "   3. Pull a model: ollama pull llama3.1"
    exit 1
fi

echo "✅ Ollama is running"

# Check if a model is available
MODELS=$(curl -s http://localhost:11434/api/tags | grep -o '"name":"[^"]*"' | cut -d'"' -f4)
if [ -z "$MODELS" ]; then
    echo "❌ No Ollama models found. Please pull a model first:"
    echo "   ollama pull llama3.1"
    exit 1
fi

echo "✅ Available Ollama models: $MODELS"

# Build the application
echo "🔨 Building the application..."
if ! mvn clean install -q; then
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

echo "✅ Build successful"

# Start the application
echo "🚀 Starting AI Social Media Agent..."
echo "📍 Application will be available at: http://localhost:8080"
echo "📚 API documentation: http://localhost:8080/api/v1/agent/platforms"
echo "🌐 Web interface: http://localhost:8080"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

mvn spring-boot:run