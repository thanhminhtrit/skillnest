# ============================================
# DEPLOY SCRIPT - CẬP NHẬT ĐÚNG ĐƯỜNG DẪN
# ============================================

# Cấu hình
$PROJECT_DIR = "D:\FPT_U\HK8\EXE2\skillnest"
$SSH_KEY = "D:\FPT_U\HK8\skillnest-vn_key.pem"
$SERVER_IP = "4.193.192.105"
$SERVER_USER = "azureuser"
$JAR_NAME = "skillnest-0.0.1-SNAPSHOT.jar"

# ⚠️ ĐƯỜNG DẪN ĐÚNG TRÊN SERVER (dựa vào service file)
$SERVER_JAR_PATH = "/home/azureuser/skillnest-0.0.1-SNAPSHOT.jar"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SKILLNEST DEPLOY (CORRECT PATH)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Bước 1: Build
Write-Host "[1/5] Building application..." -ForegroundColor Yellow
Set-Location $PROJECT_DIR
& .\mvnw.cmd clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Build successful!" -ForegroundColor Green
Write-Host ""

# Bước 2: Verify JAR
Write-Host "[2/5] Verifying JAR file..." -ForegroundColor Yellow
$JAR_PATH = "$PROJECT_DIR\target\$JAR_NAME"

if (!(Test-Path $JAR_PATH)) {
    Write-Host "❌ JAR file not found!" -ForegroundColor Red
    exit 1
}

$fileSize = (Get-Item $JAR_PATH).Length / 1MB
Write-Host "✅ JAR verified: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Green
Write-Host ""

# Bước 3: Upload
Write-Host "[3/5] Uploading to server..." -ForegroundColor Yellow
$uploadTarget = "${SERVER_USER}@${SERVER_IP}:${SERVER_JAR_PATH}.new"
Write-Host "Uploading to: $uploadTarget" -ForegroundColor Gray

& scp -i $SSH_KEY $JAR_PATH $uploadTarget

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Upload failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Upload successful!" -ForegroundColor Green
Write-Host ""

# Bước 4: Deploy
Write-Host "[4/5] Deploying on server..." -ForegroundColor Yellow

$deployScript = @"
# Backup file cũ nếu tồn tại
if [ -f $SERVER_JAR_PATH ]; then
    echo "💾 Creating backup..."
    cp -a $SERVER_JAR_PATH ${SERVER_JAR_PATH}.bak.`$(date +%F-%H%M%S)
    echo "✅ Backup created"
else
    echo "⚠️  No existing JAR file to backup"
fi

echo "🛑 Stopping service..."
sudo systemctl stop skillnest
echo "✅ Service stopped"

echo "🔄 Replacing JAR file..."
mv -f ${SERVER_JAR_PATH}.new $SERVER_JAR_PATH
echo "✅ JAR replaced"

echo "🚀 Starting service..."
sudo systemctl start skillnest
sleep 3
echo "✅ Service started"
"@

$deployScript | & ssh -i $SSH_KEY "$SERVER_USER@$SERVER_IP" "bash -s"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Deploy failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Deploy completed!" -ForegroundColor Green
Write-Host ""

# Bước 5: Verify
Write-Host "[5/5] Verifying deployment..." -ForegroundColor Yellow

$verifyScript = @"
echo "📊 Service Status:"
sudo systemctl status skillnest --no-pager | head -15

echo ""
echo "🔌 Port Check:"
sudo lsof -i :8080 | grep LISTEN

echo ""
echo "📝 Recent Logs (checking for errors):"
journalctl -u skillnest -n 50 --no-pager | grep -E "Started|ERROR|Exception|Tomcat started" | tail -10
"@

$verifyScript | & ssh -i $SSH_KEY "$SERVER_USER@$SERVER_IP" "bash -s"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ✅ DEPLOYMENT COMPLETED!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🌐 Application URL: http://$SERVER_IP:8080" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Next steps:" -ForegroundColor Yellow
Write-Host "   1. Test in browser: http://$SERVER_IP:8080" -ForegroundColor White
Write-Host "   2. Monitor logs: ssh -i `"$SSH_KEY`" $SERVER_USER@$SERVER_IP 'journalctl -u skillnest -f'" -ForegroundColor White
Write-Host ""