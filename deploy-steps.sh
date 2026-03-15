#!/bin/bash
# SkillNest Deployment Script for Azure VM
# Run this on your Azure VM (4.193.192.105)

echo "=========================================="
echo "SkillNest Backend Deployment"
echo "=========================================="

# Step 1: Stop the service
echo "Step 1: Stopping skillnest service..."
sudo systemctl stop skillnest

# Step 2: Backup current jar (if exists)
if [ -f /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar ]; then
    echo "Step 2: Backing up current jar..."
    sudo cp /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar.backup_$(date +%Y%m%d_%H%M%S)
fi

# Step 3: Move new jar to correct location
echo "Step 3: Deploying new jar..."
sudo mv /home/azureuser/skillnest/skillnest-ver1.3.jar /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar

# Step 4: Set correct permissions
echo "Step 4: Setting permissions..."
sudo chown azureuser:azureuser /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar
sudo chmod 755 /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar

# Step 5: Start the service
echo "Step 5: Starting skillnest service..."
sudo systemctl start skillnest

# Step 6: Wait a few seconds
echo "Step 6: Waiting for service to start..."
sleep 5

# Step 7: Check status
echo "Step 7: Checking service status..."
sudo systemctl status skillnest --no-pager

echo ""
echo "=========================================="
echo "Deployment completed!"
echo "=========================================="
echo ""
echo "To view logs in real-time, run:"
echo "  sudo journalctl -u skillnest -f"
echo ""
echo "To check health:"
echo "  curl http://localhost:8080/actuator/health"
echo "  curl http://4.193.192.105:8080/api/auth/health"
echo ""

