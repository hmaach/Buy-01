# ==============================
# Configuration
# ==============================

BACKEND_DIR := backend

SERVICES := \
	api-gateway \
	discovery-server \
	media-service \
	product-service \
	user-service

MVN_CMD := ./mvnw

# ==============================
# Phony Targets
# ==============================

.PHONY: help build clean install rebuild docker-up docker-down docker-build

# Default target
help:
	@echo "Available targets:"
	@echo "  make keys           -> generate RSA keys for JWT signing in .env"
	@echo "  make install        -> clean install all backend services"
	@echo "  make clean          -> clean all backend services"
	@echo "  make rebuild        -> clean install all services"
	@echo "  make docker-build   -> build docker images"
	@echo "  make docker-up      -> start docker-compose"
	@echo "  make docker-down    -> stop docker-compose"

# ==============================
# Maven Targets
# ==============================

install:
	@for service in $(SERVICES); do \
		echo "Building $$service..."; \
		cd $(BACKEND_DIR)/$$service && $(MVN_CMD) clean install && cd - > /dev/null; \
	done

clean:
	@for service in $(SERVICES); do \
		echo "Cleaning $$service..."; \
		cd $(BACKEND_DIR)/$$service && $(MVN_CMD) clean && cd - > /dev/null; \
	done

rebuild: clean install

# ==============================
# Docker Targets
# ==============================

docker-build:
	docker compose build

docker-up:
	docker compose up -d

docker-down:
	docker compose down

# ==============================
# Key Generation
# ==============================
.PHONY: keys

keys:
	@echo "Generating RSA keys..."
	@./scripts/generate-keys.sh
