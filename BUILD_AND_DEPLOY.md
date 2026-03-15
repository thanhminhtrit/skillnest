# 🚀 Hướng Dẫn Build và Deploy lên Azure VM

## ✅ Đã kiểm tra và sửa xong:

### 1. **application.yml** (Development)
- Database: localhost (dùng khi dev local)
- Logging: DEBUG level
- SQL: show-sql = true

### 2. **application-prod.yml** (Production)
- Database: 4.193.192.105 (Azure VM)
- Logging: INFO level, ghi vào file `/var/log/skillnest/application.log`
- SQL: show-sql = false
- Compression: enabled
- Bank info: MB Bank - 01280120048888

## 📋 Các bước Build và Deploy:

### Bước 1: Clean và Build Project
```cmd
cd D:\FPT_U\HK8\EXE2\skillnest
mvnw clean package -DskipTests
```

✅ Kết quả: File `skillnest-0.0.1-SNAPSHOT.jar` trong thư mục `target/`

### Bước 2: Test trên Local (Optional)
```cmd
java -jar -Dspring.profiles.active=prod target\skillnest-0.0.1-SNAPSHOT.jar
```

### Bước 3: Upload lên Azure VM
**Sử dụng SCP/WinSCP/FileZilla:**
```bash
# Từ Windows (PowerShell/CMD)
scp target\skillnest-0.0.1-SNAPSHOT.jar azureuser@4.193.192.105:/home/azureuser/skillnest/

# Hoặc dùng WinSCP GUI
Host: 4.193.192.105
Username: azureuser
Port: 22
```

### Bước 4: SSH vào Azure VM
```bash
ssh azureuser@4.193.192.105
```

### Bước 5: Tạo thư mục log (lần đầu tiên)
```bash
sudo mkdir -p /var/log/skillnest
sudo chown azureuser:azureuser /var/log/skillnest
```

### Bước 6: Stop service cũ (nếu đang chạy)
```bash
# Tìm process đang chạy
ps aux | grep skillnest

# Kill process (thay PID bằng số thực tế)
kill -9 <PID>

# Hoặc dùng pkill
pkill -f skillnest
```

### Bước 7: Start application
```bash
cd /home/azureuser/skillnest

# Chạy với production profile
nohup java -jar -Dspring.profiles.active=prod skillnest-0.0.1-SNAPSHOT.jar > output.log 2>&1 &

# Check logs
tail -f output.log
tail -f /var/log/skillnest/application.log
```

### Bước 8: Kiểm tra service
```bash
# Check process
ps aux | grep skillnest

# Check port
netstat -tulpn | grep 8080

# Test API
curl http://localhost:8080/actuator/health
curl http://localhost:8080/swagger-ui.html
```

## 🔧 Tạo Service Systemd (Recommended)

### Tạo file service:
```bash
sudo nano /etc/systemd/system/skillnest.service
```

### Nội dung file:
```ini
[Unit]
Description=Skillnest Spring Boot Application
After=network.target

[Service]
Type=simple
User=azureuser
WorkingDirectory=/home/azureuser/skillnest
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /home/azureuser/skillnest/skillnest-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

StandardOutput=append:/var/log/skillnest/application.log
StandardError=append:/var/log/skillnest/error.log

[Install]
WantedBy=multi-user.target
```

### Quản lý service:
```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service (tự động start khi boot)
sudo systemctl enable skillnest

# Start service
sudo systemctl start skillnest

# Check status
sudo systemctl status skillnest

# Stop service
sudo systemctl stop skillnest

# Restart service
sudo systemctl restart skillnest

# View logs
sudo journalctl -u skillnest -f
```

## 🔍 Troubleshooting

### 1. Port 8080 đã bị chiếm:
```bash
# Tìm process đang dùng port 8080
sudo lsof -i :8080

# Kill process
sudo kill -9 <PID>
```

### 2. Database connection failed:
```bash
# Test kết nối PostgreSQL
psql -h 4.193.192.105 -U skillnest -d skillnest_db

# Check PostgreSQL service
sudo systemctl status postgresql
```

### 3. Permission denied cho log file:
```bash
sudo chown -R azureuser:azureuser /var/log/skillnest
sudo chmod -R 755 /var/log/skillnest
```

### 4. Out of Memory:
```bash
# Chạy với giới hạn memory
java -jar -Xmx512m -Xms256m -Dspring.profiles.active=prod skillnest-0.0.1-SNAPSHOT.jar
```

## 📊 Monitoring

### Check application health:
```bash
curl http://4.193.192.105:8080/actuator/health
```

### View logs:
```bash
# Application log
tail -f /var/log/skillnest/application.log

# Error log
tail -f /var/log/skillnest/error.log

# System log
sudo journalctl -u skillnest -n 100 --no-pager
```

## 🎯 Quick Deploy Script

Tạo file `deploy.sh` để tự động hóa:
```bash
#!/bin/bash

echo "🚀 Deploying Skillnest to Azure VM..."

# 1. Build project
echo "📦 Building project..."
./mvnw clean package -DskipTests

# 2. Upload to server
echo "📤 Uploading to Azure VM..."
scp target/skillnest-0.0.1-SNAPSHOT.jar azureuser@4.193.192.105:/home/azureuser/skillnest/

# 3. Restart service on server
echo "🔄 Restarting service..."
ssh azureuser@4.193.192.105 << 'EOF'
sudo systemctl restart skillnest
sleep 5
sudo systemctl status skillnest
EOF

echo "✅ Deployment completed!"
echo "🔗 Access: http://4.193.192.105:8080"
```

Chạy script:
```bash
chmod +x deploy.sh
./deploy.sh
```

## 🔐 Security Notes

1. **Đổi mật khẩu JWT secret** trước khi deploy production
2. **Backup database** trước khi update
3. **Test API** sau khi deploy
4. **Monitor logs** trong 10-15 phút đầu

## 📞 Support URLs

- **API Base**: http://4.193.192.105:8080
- **Swagger UI**: http://4.193.192.105:8080/swagger-ui.html
- **Health Check**: http://4.193.192.105:8080/actuator/health
- **Database**: 4.193.192.105:5432

---
**Last Updated**: March 11, 2026

