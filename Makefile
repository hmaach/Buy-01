# ==============================
# Configuration
# ==============================

BACKEND_DIR := backend

MVN_CMD := ./mvnw

# ==============================
# Phony Targets
# ==============================

.PHONY: help build clean install rebuild docker-up docker-up-dbs docker-down docker-build fresh env

# Default target
help:
	@echo "Available targets:"
	@echo "  make env            -> generate RSA keys for JWT signing in .env"
	@echo "  make ssl           -> generate SSL certificates for secure communication"
	@echo "  make install        -> clean install all backend services"
	@echo "  make clean          -> clean all backend services"
	@echo "  make rebuild        -> clean install all services"
	@echo "  make docker-build   -> build docker images"
	@echo "  make docker-up      -> start docker-compose"
	@echo "  make docker-up-dev  -> start docker-compose with dbs and discovery-server for development"
	@echo "  make docker-up-dbs  -> start docker-compose databases only"
	@echo "  make docker-down    -> stop docker-compose"
	@echo "  make fresh          -> wipe all app data and images, rebuild, start (asks first)"

# ==============================
# Maven Targets
# ==============================

install: ssl
	cd $(BACKEND_DIR) && $(MVN_CMD) -B clean install -DskipTests

clean:
	cd $(BACKEND_DIR) && $(MVN_CMD) -B clean

rebuild: clean install

# ==============================
# Docker Targets
# ==============================

docker-build: ssl
	docker compose build

docker-up:
	docker compose up -d

docker-up-dbs:
	docker compose up -d kafka mongo-user mongo-product mongo-media

docker-up-dev:
	docker compose up -d kafka mongo-user mongo-product mongo-media discovery-server

docker-down:
	docker compose down

fresh:
	sudo ./scripts/fresh-start.sh

# ==============================
# Key Generation
# ==============================
ssl:
	@echo "Checking SSL certificates..."
	@bash ./scripts/setup-ssl.sh

env:
	@echo "Generating RSA keys..."
	@./scripts/generate-keys.sh
