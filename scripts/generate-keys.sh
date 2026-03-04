#!/bin/bash

set -e

echo "Generating RSA key pair for JWT authentication..."

TEMP_DIR=$(mktemp -d)
PRIVATE_KEY_FILE="$TEMP_DIR/private_key.pem"
PUBLIC_KEY_FILE="$TEMP_DIR/public_key.pem"

openssl genpkey -algorithm RSA -out "$PRIVATE_KEY_FILE" -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in "$PRIVATE_KEY_FILE" -out "$PUBLIC_KEY_FILE"

PRIVATE_KEY=$(cat "$PRIVATE_KEY_FILE" | base64 -w 0)
PUBLIC_KEY=$(cat "$PUBLIC_KEY_FILE" | base64 -w 0)

rm -rf "$TEMP_DIR"

if [ ! -f .env ]; then
    echo ".env file not found. Copying from .env.example..."
    cp .env.example .env
fi

if grep -q "^JWT_PRIVATE_KEY=" .env; then
    echo "Updating existing JWT_PRIVATE_KEY..."
    sed -i "s|^JWT_PRIVATE_KEY=.*|JWT_PRIVATE_KEY=$PRIVATE_KEY|" .env
else
    echo "Adding JWT_PRIVATE_KEY..."
    sed -i "s|^JWT_PRIVATE_KEY=|JWT_PRIVATE_KEY=$PRIVATE_KEY|" .env
fi

if grep -q "^JWT_PUBLIC_KEY=" .env; then
    echo "Updating existing JWT_PUBLIC_KEY..."
    sed -i "s|^JWT_PUBLIC_KEY=.*|JWT_PUBLIC_KEY=$PUBLIC_KEY|" .env
else
    echo "Adding JWT_PUBLIC_KEY..."
    sed -i "s|^JWT_PUBLIC_KEY=|JWT_PUBLIC_KEY=$PUBLIC_KEY|" .env
fi

echo "✓ RSA key pair generated and added to .env file successfully!"
echo "  - Private key (base64): ${#PRIVATE_KEY} characters"
echo "  - Public key (base64): ${#PUBLIC_KEY} characters"
