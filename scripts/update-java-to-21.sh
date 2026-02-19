#!/usr/bin/env zsh
# =============================================================================
# install-oracle-jdk21-zsh.sh
# Install Oracle JDK 21 (x64) on Ubuntu + zsh – NO SUDO
# Uses direct .tar.gz from Oracle
# =============================================================================

set -euo pipefail

# --- Config ---
JDK_DIR="$HOME/jdk"
SYMLINK="$JDK_DIR/jdk21"
ZSHRC="$HOME/.zshrc"

# --- Oracle JDK 21 x64 .tar.gz ---
echo "Downloading Oracle JDK 21 (x64)..."
DOWNLOAD_URL="https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz"
FILENAME="jdk-21_linux-x64_bin.tar.gz"

# --- Download ---
mkdir -p "$JDK_DIR"
cd "$JDK_DIR"

echo "Downloading: $FILENAME (~200 MB)"
curl -L -# --fail \
     -H "User-Agent: Mozilla/5.0 (X11; Linux x86_64)" \
     -o "$FILENAME" \
     "$DOWNLOAD_URL"

# --- Verify it's a real tar.gz ---
echo "Verifying file type..."
file "$FILENAME" | grep -q "gzip compressed data"
if [[ $? -ne 0 ]]; then
  echo "Error: Downloaded file is not a valid .tar.gz (got HTML?)"
  echo "First 5 lines:"
  head -n 5 "$FILENAME"
  rm -f "$FILENAME"
  exit 1
fi

# --- Extract ---
echo "Extracting..."
tar -xzf "$FILENAME"

# --- Cleanup ---
rm -f "$FILENAME"

# --- Find extracted folder ---
EXTRACTED_DIR=$(find . -maxdepth 1 -type d -name 'jdk-21*' -print -quit)
if [[ -z "$EXTRACTED_DIR" ]]; then
  echo "Error: No jdk-21* folder found after extraction."
  exit 1
fi

echo "Extracted: $EXTRACTED_DIR"

# --- Create symlink ---
rm -f "$SYMLINK"
ln -s "$(pwd)/$EXTRACTED_DIR" "$SYMLINK"
echo "Symlink: $SYMLINK → $EXTRACTED_DIR"

# --- Update .zshrc ---
echo "Updating $ZSHRC..."
sed -i '/# === ORACLE JDK 21 ===/,/# === END JDK 21 ===/d' "$ZSHRC" 2>/dev/null || true

cat >> "$ZSHRC" <<'EOF'

# === ORACLE JDK 21 ===
export JAVA_HOME="$HOME/jdk/jdk21"
export PATH="$JAVA_HOME/bin:$PATH"
# === END JDK 21 ===
EOF

echo "JAVA_HOME and PATH added to $ZSHRC"

# --- Reload current shell ---
export JAVA_HOME="$SYMLINK"
export PATH="$JAVA_HOME/bin:$PATH"
echo "Environment updated in current shell."

# --- Verify ---
echo "Verifying installation..."
"$SYMLINK/bin/java" -version

echo ""
echo "Oracle JDK 21 installed successfully!"
echo "   JAVA_HOME = $JAVA_HOME"
echo "   Run: java -version"
echo ""
echo "Tip: Open a new terminal or run 'source ~/.zshrc' to refresh."