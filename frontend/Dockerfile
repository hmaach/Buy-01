# Stage 1: Build the Angular app
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production --base-href=/app/

# Stage 2: Serve with Nginx + SSL
FROM nginx:alpine

# Install openssl
RUN apk add --no-cache openssl

# Generate self-signed certificate
ARG SSL_PASSWORD=123456
RUN mkdir -p /etc/nginx/ssl && \
    openssl req -x509 -newkey rsa:4096 \
    -keyout /etc/nginx/ssl/key.pem \
    -out /etc/nginx/ssl/cert.pem \
    -sha256 -days 365 -nodes \
    -subj "/C=MA/ST=Oujda/L=Oujda/O=Zone01/OU=IT/CN=localhost"

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built Angular app
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 443
CMD ["nginx", "-g", "daemon off;"]