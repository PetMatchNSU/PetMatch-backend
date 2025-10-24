#!/bin/bash

# Server setup script for PetMatch production deployment
# Run this script on your production server (158.160.173.155)

set -e

echo "🚀 Setting up PetMatch production server..."

# Update system
echo "📦 Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install Docker
echo "🐳 Installing Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
    echo "✅ Docker installed successfully"
else
    echo "✅ Docker already installed"
fi

# Install Docker Compose
echo "🐳 Installing Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "✅ Docker Compose installed successfully"
else
    echo "✅ Docker Compose already installed"
fi

# Install Nginx
echo "🌐 Installing Nginx..."
if ! command -v nginx &> /dev/null; then
    sudo apt install nginx -y
    sudo systemctl enable nginx
    sudo systemctl start nginx
    echo "✅ Nginx installed and started"
else
    echo "✅ Nginx already installed"
fi

# Create application directory
echo "📁 Creating application directory..."
sudo mkdir -p /opt/petmatch
sudo chown $USER:$USER /opt/petmatch

# Create nginx configuration
echo "⚙️ Configuring Nginx..."
sudo tee /etc/nginx/sites-available/petmatch > /dev/null << 'EOF'
server {
    listen 80;
    server_name 158.160.173.155;

    # Backend API
    location /api/ {
        proxy_pass http://localhost:8091/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers
        add_header 'Access-Control-Allow-Origin' '*' always;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
        add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization' always;
        
        if ($request_method = 'OPTIONS') {
            add_header 'Access-Control-Allow-Origin' '*';
            add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS';
            add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization';
            add_header 'Access-Control-Max-Age' 1728000;
            add_header 'Content-Type' 'text/plain; charset=utf-8';
            add_header 'Content-Length' 0;
            return 204;
        }
    }

    # Frontend (будущий)
    location / {
        # Пока редиректируем на backend API docs
        return 301 /api/swagger-ui.html;
    }
}
EOF

# Enable the site
sudo ln -sf /etc/nginx/sites-available/petmatch /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Test and reload nginx
sudo nginx -t
sudo systemctl reload nginx

echo "✅ Nginx configured successfully"

# Create environment file template
echo "📝 Creating environment file template..."
cat > /opt/petmatch/.env.production << 'EOF'
# Database Configuration
POSTGRES_DB=petmatch
POSTGRES_USER=petmatch
POSTGRES_PASSWORD=your_secure_password_here
POSTGRES_EXPORTER_DATA_SOURCE=postgresql://petmatch:your_secure_password_here@petmatch-postgres:5432/petmatch?sslmode=disable

# Redis Configuration
REDIS_PASSWORD=your_redis_password_here

# MinIO Configuration
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=your_minio_password_here
MINIO_DEFAULT_BUCKETS=petmatch-files

# Grafana Configuration
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=your_grafana_password_here

# Application Configuration
SPRING_PROFILES_ACTIVE=production
EOF

echo "✅ Environment file created at /opt/petmatch/.env.production"
echo "⚠️  Please edit /opt/petmatch/.env.production and set secure passwords!"

# Create production docker-compose file
echo "📜 Creating production docker-compose file..."
cat > /opt/petmatch/docker-compose.prod.yml << 'EOF'
services:
  # PetMatch Postgres database
  petmatch-postgres:
    image: postgres:17-alpine
    container_name: petmatch-postgres
    restart: always
    ports:
      # Порт снаружи 5433, внутри контейнера 5432
      - "5433:5432"
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      # Прокидывание скрипта инициализации базы данных
      # ro суффикс = read-only файл
      - /opt/petmatch/infrastructure/databases/petmatch:/docker-entrypoint-initdb.d:ro
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U petmatch -p 5432"]
      # Скрипт pg_isready будет запускаться с интервалом 10с, таймаут на выполнение скрипта 5с, макс. количество попыток 20
      interval: 10s
      timeout: 5s
      retries: 20
  # PetMatch Postgres database metrics exporter
  petmatch-postgres-metrics-exporter:
    image: prometheuscommunity/postgres-exporter:v0.17.0
    container_name: petmatch-postgres-metrics-exporter
    restart: always
    ports:
      - "9188:9187"
    # Экспортер метрик должен запускаться только после успешного запуска самой базы данных
    depends_on:
      petmatch-postgres:
        condition: service_healthy
    environment:
      # Шаблон: postgresql://username:password@hostname:port/database_name?sslmode=disable
      DATA_SOURCE_NAME: ${POSTGRES_EXPORTER_DATA_SOURCE}
  # Prometheus metrics
  prometheus:
    image: prom/prometheus:v3.5.0
    container_name: prometheus
    restart: always
    ports:
      - "9090:9090"
    volumes:
      - /opt/petmatch/infrastructure/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:9090/-/healthy"]
      interval: 10s
      timeout: 5s
      retries: 10
  # Loki logs
  loki:
    image: grafana/loki:2.9.2
    container_name: loki
    restart: always
    ports:
      - "3100:3100"
    volumes:
      - /opt/petmatch/infrastructure/loki/loki-config.yaml:/etc/loki/loki-config.yaml:ro
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost:3100/ready"]
      interval: 10s
      timeout: 5s
      retries: 10
  # Tempo tracings
  tempo:
    image: grafana/tempo:2.4.1
    container_name: tempo
    restart: always
    command: ["-config.file=/etc/tempo/tempo.yaml"]
    ports:
      - "3200:3200"
    volumes:
      - /opt/petmatch/infrastructure/tempo/tempo.yaml:/etc/tempo/tempo.yaml
      - tempo_data:/var/tempo
  # Alloy agent
  alloy:
    image: grafana/alloy:v1.10.2
    container_name: alloy
    restart: always
    ports:
      - "9080:9080" # HTTP Interface
      - "4317:4317" # OTLP gRPC
      - "4318:4318" # OTLP HTTP
    volumes:
      - /opt/petmatch/infrastructure/alloy/config.alloy:/etc/alloy/config.alloy:ro
      # Помимо конфигурации, агенту Alloy необходим доступ до информации о контейнерах Docker, чтобы агент смог собирать данные с них
      - /opt/petmatch/var/lib/docker/containers:/var/lib/docker/containers:ro
      - /opt/petmatch/var/run/docker.sock:/var/run/docker.sock
      - /opt/petmatch/var/log:/var/log:ro
      - alloy_data:/var/lib/alloy/data
    environment:
      GRAFANA_LOKI_URL: http://loki:3100/loki/api/v1/push
    command:
      - run
      # Открываем доступ до Alloy UI по HTTP
      - --server.http.listen-addr=0.0.0.0:9080
      - --storage.path=/var/lib/alloy/data
      - /etc/alloy/config.alloy
  # Grafana UI
  grafana:
    image: grafana/grafana:10.3.1
    container_name: grafana
    restart: always
    ports:
      - "3000:3000"
    depends_on:
      prometheus:
        condition: service_healthy
      loki:
        condition: service_healthy
      tempo:
        condition: service_started
      alloy:
        condition: service_started
    environment:
      # GF_AUTH_ANONYMOUS_ENABLED = true позволяет получить доступ до части функциональности без авторизации в Grafana
      GF_AUTH_ANONYMOUS_ENABLED: true
      GF_SECURITY_ADMIN_USER: ${GRAFANA_ADMIN_USER}
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_ADMIN_PASSWORD}
      GF_METRICS_ENABLED: "true"
    volumes:
      # Указание источников данных для Grafana при старте контейнера
      - /opt/petmatch/infrastructure/grafana/provisioning:/etc/grafana/provisioning
      # Подключение описаний dashboards в виде JSON напрямую в Grafana при старте контейнера
      - /opt/petmatch/infrastructure/grafana/dashboards:/var/lib/grafana/dashboards
      - grafana_data:/var/lib/grafana
      - nexus-data:/nexus-data
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:3000/api/health"]
      interval: 10s
      timeout: 5s
      retries: 10
  # PetMatch backend - Production version with image tag
  petmatch-service:
    image: petmatch-backend:${BACKEND_IMAGE_TAG:-latest}
    container_name: petmatch-service
    restart: always
    ports:
      - "8091:8091"
    environment:
      POSTGRES_HOST: petmatch-postgres
      POSTGRES_PORT: 5432
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      OTLP_EXPORTER_ENDPOINT: http://tempo:4318
      JAVA_OPTS: "-XX:+UseG1GC -XX:+UseContainerSupport"
    depends_on:
      petmatch-postgres:
        condition: service_healthy
      petmatch-minio:
        condition: service_healthy
      petmatch-redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-i", "http://localhost:8081/actuator/health"]
      interval: 5s
      timeout: 5s
      retries: 3
  # Redis cache
  petmatch-redis:
    image: redis:7-alpine
    container_name: petmatch-redis
    restart: always
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD} --maxmemory 128mb --maxmemory-policy allkeys-lru
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
  # Redis metrics exporter
  redis-exporter:
    image: oliver006/redis_exporter:v1.70.0
    container_name: redis-exporter
    restart: always
    ports:
      - "9121:9121"
    depends_on:
      petmatch-redis:
        condition: service_healthy
    environment:
      REDIS_ADDR: "redis://petmatch-redis:6379"
      REDIS_PASSWORD: ${REDIS_PASSWORD}
    command:
      - '--redis.addr=redis://petmatch-redis:6379'
      - '--redis.password=${REDIS_PASSWORD}'
  # MinIO object storage (S3)
  petmatch-minio:
    image: minio/minio:RELEASE.2025-04-22T22-12-26Z
    container_name: petmatch-minio
    restart: always
    ports:
      - "9001:9001"
      - "9000:9000"
    command: server /data --console-address ":9001" --address ":9000"
    environment:
      # То же самое, что access key
      MINIO_ROOT_USER: ${MINIO_ROOT_USER}
      # То же самое, что secret key
      MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD}
      MINIO_DEFAULT_BUCKETS: ${MINIO_DEFAULT_BUCKETS}
      MINIO_PROMETHEUS_AUTH_TYPE: "public"  # Make metrics publicly accessible
    volumes:
      - minio_data:/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3

# Docker volumes to save services data
volumes:
  artifacts:
  tempo_data:
  nexus-data:
  loki_data:
  loki_chunks:
  loki_index:
  loki_rules:
  postgres_data:
  redis_data:
  minio_data:
  grafana_data:
  alloy_data:
EOF

# Create deployment script
echo "📜 Creating deployment script..."
cat > /opt/petmatch/deploy.sh << 'EOF'
#!/bin/bash

# Deployment script for PetMatch backend
# Usage: ./deploy.sh [tag] [environment]
# Example: ./deploy.sh v1.0.0 production

set -e

# Configuration
BACKEND_IMAGE_NAME="petmatch-backend"
DOCKER_COMPOSE_FILE="docker-compose.prod.yml"

# Default values
TAG=${1:-"latest"}
ENVIRONMENT=${2:-"production"}

echo "🚀 Starting deployment..."
echo "Tag: $TAG"
echo "Environment: $ENVIRONMENT"

# Load environment variables
if [ -f .env.production ]; then
    export $(cat .env.production | grep -v '^#' | xargs)
fi

# Set the image tag
export BACKEND_IMAGE_TAG="$TAG"

# Pull latest image
echo "📥 Pulling latest image..."
docker pull "$BACKEND_IMAGE_NAME:$TAG"

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
EOF

chmod +x /opt/petmatch/deploy.sh

echo "✅ Deployment script created at /opt/petmatch/deploy.sh"

# Create startup script for infrastructure
echo "📜 Creating infrastructure startup script..."
cat > /opt/petmatch/start-infrastructure.sh << 'EOF'
#!/bin/bash

# Start infrastructure services
echo "🚀 Starting infrastructure services..."

# Load environment variables
if [ -f .env.production ]; then
    export $(cat .env.production | grep -v '^#' | xargs)
fi

# Start infrastructure services (everything except petmatch-service)
docker-compose -f docker-compose.prod.yml up -d \
    petmatch-postgres \
    petmatch-postgres-metrics-exporter \
    prometheus \
    loki \
    tempo \
    alloy \
    grafana \
    petmatch-redis \
    redis-exporter \
    petmatch-minio

echo "✅ Infrastructure services started!"
echo "📊 Services status:"
docker-compose -f docker-compose.prod.yml ps
EOF

chmod +x /opt/petmatch/start-infrastructure.sh

echo "✅ Infrastructure startup script created at /opt/petmatch/start-infrastructure.sh"

echo ""
echo "🎉 Server setup completed successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Edit /opt/petmatch/.env.production and set secure passwords"
echo "2. Run ./start-infrastructure.sh to start infrastructure services"
echo "3. Configure GitHub Actions secrets for deployment"
echo ""