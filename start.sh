#!/bin/bash

# 智慧旅游SaaS平台启动脚本
echo "======================================"
echo "智慧旅游SaaS平台启动脚本"
echo "======================================"

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: 请先安装Docker"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误: 请先安装Docker Compose"
    exit 1
fi

# 创建必要的目录
echo "创建必要的目录结构..."
mkdir -p nginx/conf.d
mkdir -p nginx/ssl
mkdir -p monitoring
mkdir -p logs

# 停止可能正在运行的容器
echo "停止现有容器..."
docker-compose down

# 清理旧的镜像（可选）
read -p "是否清理旧的Docker镜像? (y/n): " clean_images
if [ "$clean_images" = "y" ]; then
    echo "清理旧镜像..."
    docker system prune -f
fi

# 创建Nginx配置文件
echo "创建Nginx配置文件..."
cat > nginx/nginx.conf << 'EOF'
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # 启用Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 10240;
    gzip_proxied expired no-cache no-store private must-revalidate auth;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/x-javascript
        application/xml+rss
        application/json;

    include /etc/nginx/conf.d/*.conf;
}
EOF

cat > nginx/conf.d/tourism.conf << 'EOF'
upstream user-service {
    server user-service:8801;
}

upstream product-service {
    server product-service:8802;
}

server {
    listen 80;
    server_name localhost;

    # 用户服务代理
    location /user-service/ {
        proxy_pass http://user-service/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 产品服务代理
    location /product-service/ {
        proxy_pass http://product-service/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 健康检查
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    # 首页
    location / {
        return 200 '
        <!DOCTYPE html>
        <html>
        <head>
            <title>智慧旅游SaaS平台</title>
            <meta charset="utf-8">
            <style>
                body { font-family: Arial, sans-serif; margin: 50px; }
                .service { margin: 20px 0; padding: 20px; border: 1px solid #ddd; }
            </style>
        </head>
        <body>
            <h1>智慧旅游SaaS平台</h1>
            <div class="service">
                <h3>用户服务</h3>
                <p><a href="/user-service/swagger-ui.html">API文档</a></p>
                <p>端口: 8801</p>
            </div>
            <div class="service">
                <h3>产品服务</h3>
                <p><a href="/product-service/swagger-ui.html">API文档</a></p>
                <p>端口: 8802</p>
            </div>
            <div class="service">
                <h3>监控服务</h3>
                <p><a href=":3000">Grafana监控面板</a> (admin/admin123)</p>
                <p><a href=":9090">Prometheus</a></p>
                <p><a href=":15672">RabbitMQ管理界面</a> (guest/guest)</p>
            </div>
        </body>
        </html>
        ';
        add_header Content-Type text/html;
    }
}
EOF

# 创建Prometheus配置
echo "创建Prometheus配置文件..."
cat > monitoring/prometheus.yml << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'tourism-user-service'
    static_configs:
      - targets: ['user-service:8801']
    metrics_path: '/actuator/prometheus'
    
  - job_name: 'tourism-product-service'
    static_configs:
      - targets: ['product-service:8802']
    metrics_path: '/actuator/prometheus'
EOF

# 构建并启动服务
echo "构建并启动服务..."
docker-compose up -d --build

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 检查服务状态
echo "检查服务状态..."
docker-compose ps

# 显示访问信息
echo ""
echo "======================================"
echo "服务启动完成！"
echo "======================================"
echo "主页: http://localhost"
echo "用户服务API: http://localhost/user-service/swagger-ui.html"
echo "产品服务API: http://localhost/product-service/swagger-ui.html"
echo "Grafana监控: http://localhost:3000 (admin/admin123)"
echo "Prometheus: http://localhost:9090"
echo "RabbitMQ管理: http://localhost:15672 (guest/guest)"
echo "MySQL数据库: localhost:3306 (root/root123456)"
echo "Redis缓存: localhost:6379"
echo "======================================"
echo ""
echo "查看日志命令:"
echo "docker-compose logs -f [service-name]"
echo ""
echo "停止服务命令:"
echo "docker-compose down"
echo "======================================"