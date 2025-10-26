#!/bin/bash

# Deployment script for PetMatch backend
# Usage: ./deploy.sh [tag] [environment]
# Example: ./deploy.sh v1.0.0 production

set -e

# Configuration
BACKEND_IMAGE_NAME="andrvat/petmatch-backend"
FRONTEND_IMAGE_NAME="andrvat/petmatch-frontend"
DOCKER_COMPOSE_FILE="docker-compose.prod.yml"

# Default values
TAG=${1:-"latest"}
ENVIRONMENT=${2:-"production"}

# Update environment variables for the new tag
export BACKEND_IMAGE_TAG="${TAG##*:}"
export FRONTEND_IMAGE_TAG="${TAG##*:}"

echo "Using docker image tag $BACKEND_IMAGE_TAG..."

echo "🚀 Starting deployment..."
echo "Tag: $TAG"
echo "Environment: $ENVIRONMENT"

echo "Export environment variables for the given environment..."
export $(cat .env.$ENVIRONMENT | grep -v '^#' | xargs)

echo "Using compose file: $DOCKER_COMPOSE_FILE"

# Pull latest images
echo "📥 Pulling latest images..."
docker pull "$TAG" || echo "Backend image not found"

# Stop only application services (keep infrastructure running)
echo "⏹️ Stopping application services..."
docker-compose -f "$DOCKER_COMPOSE_FILE" stop petmatch-service || true

# Remove old application containers
echo "🗑️ Removing old application containers..."
docker-compose -f "$DOCKER_COMPOSE_FILE" rm -f petmatch-service || true

# Start application services
echo "▶️ Starting application services..."
docker-compose -f "$DOCKER_COMPOSE_FILE" up -d petmatch-service

# Wait for health check
echo "⏳ Waiting for application to be healthy..."
timeout=60
counter=0
while [ $counter -lt $timeout ]; do
    if docker-compose -f "$DOCKER_COMPOSE_FILE" ps petmatch-service | grep -q "healthy"; then
        echo "✅ Application is healthy!"
        break
    fi
    echo "Waiting for health check... ($counter/$timeout)"
    sleep 2
    counter=$((counter + 2))
done

if [ $counter -ge $timeout ]; then
    echo "❌ Application failed to become healthy within $timeout seconds"
    echo "📋 Application logs:"
    docker-compose -f "$DOCKER_COMPOSE_FILE" logs petmatch-service
    exit 1
fi

# Clean up old images
echo "🧹 Cleaning up old images..."
docker image prune -f

echo "🎉 Deployment completed successfully!"
echo "📊 Application status:"
docker-compose -f "$DOCKER_COMPOSE_FILE" ps petmatch-service
