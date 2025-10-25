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
# Copy the relevant content from file deploy/.env.production in the repository right here
# After editing, run the commands
# sudo nginx -t
# sudo systemctl reload nginx
EOF
echo "⚠️ Check the content of /etc/nginx/sites-available/petmatch manually on the subject of relevance compared to nginx.conf file in the project repository!"

# Enable the site
sudo ln -sf /etc/nginx/sites-available/petmatch /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

echo "⚠️ Do not forget to reload nginx service!"

# Create environment file template
echo "📝 Creating environment file template..."
cat > /opt/petmatch/.env.production << 'EOF'
# Copy the relevant content from file deploy/.env.production in the repository right here
EOF

echo "✅ Environment file created at /opt/petmatch/.env.production"
echo "⚠️ Edit the content of /opt/petmatch/.env.production manually!.."
echo "⚠️ Edit /opt/petmatch/.env.production and set secure passwords!"

# Create production docker-compose file
echo "📜 Creating production docker-compose file..."
cat > /opt/petmatch/docker-compose.prod.yml << 'EOF'
# Copy the relevant content from file docker-compose.prod.yml in the repository right here
EOF

echo "⚠️ Edit the content of /opt/petmatch/docker-compose.prod.yml manually!.."

# Create deployment script
echo "📜 Creating deployment script..."
cat > /opt/petmatch/deploy.sh << 'EOF'
# Copy the relevant content from file deploy/deploy.sh in the repository right here
EOF

echo "⚠️ Edit the content of /opt/petmatch/deploy.sh manually!.."

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
echo "1. Edit /opt/petmatch/.env.production"
echo "2. Edit /opt/petmatch/deploy.sh"
echo "3. Edit /opt/petmatch/docker-compose.prod.yml"
echo "3. Edit /etc/nginx/sites-available/petmatch and reload nginx service"
echo "4. Run ./start-infrastructure.sh to start infrastructure services"
echo "5. Configure GitHub Actions secrets for deployment"
echo ""