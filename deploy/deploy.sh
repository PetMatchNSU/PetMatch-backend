#!/bin/bash

# Deployment script for PetMatch backend and frontend
# Usage: ./deploy.sh [image:tag] [environment]
# Examples:
#   ./deploy.sh andrvat/petmatch-backend:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad production
#   ./deploy.sh andrvat/petmatch-frontend:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad production

set -e

# Configuration
DOCKER_COMPOSE_FILE="docker-compose.prod.yml"

# Parse input arguments
FULL_IMAGE_NAME=${1:-""}
ENVIRONMENT=${2:-"production"}

# Extract image name and tag from first argument
IMAGE_NAME="${FULL_IMAGE_NAME%:*}"
TAG="${FULL_IMAGE_NAME##*:}"

# Extract service name from image name (last part after /)
SERVICE_NAME="${IMAGE_NAME##*/}"

echo "🚀 Starting deployment..."
echo "Full image: $FULL_IMAGE_NAME"
echo "Service: $SERVICE_NAME"
echo "Tag: $TAG"
echo "Environment: $ENVIRONMENT"

# Validate service name and set compose service name
if [[ "$SERVICE_NAME" == "petmatch-backend" ]]; then
    COMPOSE_SERVICE_NAME="petmatch-service"
    export BACKEND_IMAGE_TAG="$TAG"
elif [[ "$SERVICE_NAME" == "petmatch-frontend" ]]; then
    COMPOSE_SERVICE_NAME="petmatch-frontend"
    export FRONTEND_IMAGE_TAG="$TAG"
else
    echo "❌ Invalid service name: $SERVICE_NAME"
    echo "   Must be either 'petmatch-backend' or 'petmatch-frontend'"
    exit 1
fi

echo "Using compose service: $COMPOSE_SERVICE_NAME"

echo "Using compose file: $DOCKER_COMPOSE_FILE"

# Pull latest image
echo "📥 Pulling latest image..."
docker pull "$FULL_IMAGE_NAME" || echo "Tagged image not found, using local image"

# Stop only the target service (keep infrastructure running)
echo "⏹️ Stopping $COMPOSE_SERVICE_NAME service..."
docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file .env.$ENVIRONMENT stop "$COMPOSE_SERVICE_NAME" || true

# Remove old service container
echo "🗑️ Removing old $COMPOSE_SERVICE_NAME container..."
docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file .env.$ENVIRONMENT rm -f "$COMPOSE_SERVICE_NAME" || true

# Start the service
echo "▶️ Starting $COMPOSE_SERVICE_NAME service..."
docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file .env.$ENVIRONMENT up -d "$COMPOSE_SERVICE_NAME"

# Wait for health check
echo "⏳ Waiting for $COMPOSE_SERVICE_NAME to be healthy..."
timeout=60
counter=0
while [ $counter -lt $timeout ]; do
    if [[ "$SERVICE_NAME" == "petmatch-backend" ]]; then
      if docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file .env.$ENVIRONMENT ps "$COMPOSE_SERVICE_NAME" | grep -q "healthy"; then
          echo "✅ $COMPOSE_SERVICE_NAME is healthy!"
          break
      fi
      echo "Waiting for petmatch-backend health check... ($counter/$timeout)"
      sleep 2
      counter=$((counter + 2))
    elif [[ "$SERVICE_NAME" == "petmatch-frontend" ]]; then
      if docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file .env.$ENVIRONMENT logs "$COMPOSE_SERVICE_NAME" | grep -q "vite preview"; then
          echo "✅ $COMPOSE_SERVICE_NAME is healthy!"
          break
      fi
      echo "Waiting for petmatch-frontend health check... ($counter/$timeout)"
      sleep 2
      counter=$((counter + 2))
    else
        echo "❌ Invalid service name: $SERVICE_NAME"
        echo "   Must be either 'petmatch-backend' or 'petmatch-frontend' during health check"
        exit 1
    fi
done

if [ $counter -ge $timeout ]; then
    echo "❌ $COMPOSE_SERVICE_NAME failed to become healthy within $timeout seconds"
    echo "📋 Service logs:"
    docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file .env.$ENVIRONMENT logs "$COMPOSE_SERVICE_NAME"
    exit 1
fi

# Clean up old images
echo "🧹 Cleaning up old images..."
docker image prune -f

echo "🎉 Deployment completed successfully!"
echo "📊 Service status:"
docker-compose -f "$DOCKER_COMPOSE_FILE" --env-file .env.$ENVIRONMENT ps "$COMPOSE_SERVICE_NAME"