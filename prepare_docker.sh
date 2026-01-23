#!/bin/bash
set -e

# Configuration
SDK_DIR="../pinapp-notify-sdk"
SDK_JAR_NAME="pinapp-notify-sdk-1.0.0-SNAPSHOT.jar"
DEST_LIBS_DIR="libs"
IMAGE_NAME="pinapp-gateway"

echo "Starting Docker preparation process..."

# Step 1: Build the SDK
echo "--> Building pinapp-notify-sdk in $SDK_DIR..."
if [ -d "$SDK_DIR" ]; then
    pushd "$SDK_DIR" > /dev/null
    mvn clean install
    popd > /dev/null
else
    echo "Error: SDK directory '$SDK_DIR' not found."
    exit 1
fi

# Step 2: Create local libs directory
echo "--> Creating '$DEST_LIBS_DIR' directory..."
mkdir -p "$DEST_LIBS_DIR"

# Step 3: Copy the compiled SDK JAR
SOURCE_JAR="$SDK_DIR/target/$SDK_JAR_NAME"
if [ -f "$SOURCE_JAR" ]; then
    echo "--> Copying $SOURCE_JAR to $DEST_LIBS_DIR/..."
    cp "$SOURCE_JAR" "$DEST_LIBS_DIR/"
else
    echo "Error: Source JAR '$SOURCE_JAR' not found. Build may have failed."
    exit 1
fi

# Step 4: Build the Docker image
echo "--> Building Docker image '$IMAGE_NAME'..."
docker build -t "$IMAGE_NAME" .

echo "Done! Docker image '$IMAGE_NAME' created successfully."
