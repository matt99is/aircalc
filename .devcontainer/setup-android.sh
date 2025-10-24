#!/bin/bash
set -e

echo "Setting up Android SDK..."

# Download Android command-line tools
cd ~
wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip -q commandlinetools-linux-9477386_latest.zip -d temp-android

# Set up directory structure
mkdir -p android-sdk/cmdline-tools
mv temp-android/cmdline-tools android-sdk/cmdline-tools/latest
rm -rf temp-android commandlinetools-linux-9477386_latest.zip

# Install Android SDK components
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

echo "Accepting licenses..."
yes | sdkmanager --licenses > /dev/null 2>&1

echo "Installing Android SDK components..."
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Install Claude Code
echo "Installing Claude Code..."
npm install -g @anthropic-ai/claude-code

echo "Android development environment setup complete!"
