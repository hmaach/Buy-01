#!/bin/bash

# Configuration
RES_DIR="backend/api-gateway/src/main/resources"
KEY_FILE="$RES_DIR/key.pem"
CERT_FILE="$RES_DIR/cert.pem"
P12_FILE="$RES_DIR/edge-service.p12"
PASSWORD=${SSL_PASSWORD} # Match this in your application.yml

# Ensure directory exists
mkdir -p $RES_DIR

if [ -f "$P12_FILE" ]; then
    echo "SSL Certificate already exists in $P12_FILE"
else
    echo "Generating SSL Certificate..."
    
    # Step A: Generate PEM files
    openssl req -x509 -newkey rsa:4096 -keyout "$KEY_FILE" -out "$CERT_FILE" \
    -sha256 -days 365 -nodes -subj "/C=MA/ST=Oujda/L=Oujda/O=Zone01/OU=IT/CN=localhost"

    # Step B: Bundle into .p12
    openssl pkcs12 -export -in "$CERT_FILE" -inkey "$KEY_FILE" \
    -out "$P12_FILE" -name springgateway -passout pass:$PASSWORD

    # Cleanup raw PEM files (Keep them if you need them for Nginx later, otherwise delete)
    rm "$KEY_FILE" "$CERT_FILE"
    
    echo "SSL Certificate generated successfully at $P12_FILE"
fi