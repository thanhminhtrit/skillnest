# SkillNest Backend Deployment Guide - Ver 1.3

## Current Situation
- New jar uploaded to: `/home/azureuser/skillnest/skillnest-ver1.3.jar`
- Service currently running old version at: `/home/azureuser/skillnest-0.0.1-SNAPSHOT.jar`
- Service status: Active (running)

## Deployment Steps

### Option A: Quick Manual Deployment (Recommended)

SSH into your VM and run these commands one by one:

```bash
# 1. Stop the service
sudo systemctl stop skillnest

# 2. Backup current jar (optional but recommended)
sudo cp /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar.backup_$(date +%Y%m%d_%H%M%S) 2>/dev/null || echo "No existing jar to backup"

# 3. Replace with new version
sudo mv /home/azureuser/skillnest/skillnest-ver1.3.jar /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar

# 4. Set correct permissions
sudo chown azureuser:azureuser /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar
sudo chmod 755 /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar

# 5. Start the service
sudo systemctl start skillnest

# 6. Check status
sudo systemctl status skillnest

# 7. Monitor logs (Ctrl+C to exit)
sudo journalctl -u skillnest -f
```

### Option B: Using the Deployment Script

```bash
# 1. Upload the script
scp -i "D:\FPT_U\HK8\skillnest-vn_key.pem" "D:\FPT_U\HK8\EXE2\skillnest\deploy-steps.sh" azureuser@4.193.192.105:/home/azureuser/

# 2. SSH into VM
ssh -i "D:\FPT_U\HK8\skillnest-vn_key.pem" azureuser@4.193.192.105

# 3. Run the script
chmod +x /home/azureuser/deploy-steps.sh
bash /home/azureuser/deploy-steps.sh
```

## Verification Steps

### 1. Check Service Status
```bash
sudo systemctl status skillnest
```
Expected: `Active: active (running)`

### 2. Check Application Logs
```bash
sudo journalctl -u skillnest -n 100 --no-pager
```
Look for:
- `Started SkillnestApplication`
- No error messages
- `Tomcat started on port 8080`

### 3. Test Health Endpoint (from VM)
```bash
curl -v http://localhost:8080/actuator/health
```
Expected response: `{"status":"UP"}`

### 4. Test API Endpoint (from VM)
```bash
curl -v http://localhost:8080/api/projects
```

### 5. Test from External (from your local machine)
```bash
curl -v http://4.193.192.105:8080/api/projects
```

## Common Issues & Solutions

### Issue 1: Port 8080 not accessible from outside
**Solution:**
```bash
# On VM, check if port is open
sudo netstat -tlnp | grep 8080

# Check Azure Network Security Group rules
# Ensure inbound rule allows port 8080 from your IP or 0.0.0.0/0
```

### Issue 2: Database connection errors
**Symptoms:** `Unable to obtain JDBC connection` or `Connection refused`

**Solution:**
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Test connection from VM
psql -h 4.193.192.105 -U skillnest -d skillnest_db -c "SELECT 1;"

# If connection fails, check pg_hba.conf
sudo nano /etc/postgresql/*/main/pg_hba.conf
# Should have: host all all 0.0.0.0/0 md5
```

### Issue 3: Service fails to start
**Solution:**
```bash
# Check detailed logs
sudo journalctl -u skillnest -n 500 --no-pager

# Check jar file permissions
ls -lah /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar

# Manually test jar
cd /home/azureuser
java -jar skillnest-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Issue 4: "relation does not exist" errors
**Solution:** Database schema needs to be initialized
```bash
# Connect to database
psql -h 4.193.192.105 -U skillnest -d skillnest_db

# Check if schema exists
\dn

# Check if tables exist
\dt skillnest.*

# If tables missing, run your schema SQL file
# (Upload skillnest_schema.sql to VM first)
psql -h 4.193.192.105 -U skillnest -d skillnest_db -f /home/azureuser/skillnest_schema.sql
```

## Rollback Procedure

If the new version has issues, rollback to previous version:

```bash
# 1. Stop service
sudo systemctl stop skillnest

# 2. Find backup
ls -lah /home/azureuser/*.backup*

# 3. Restore backup (replace timestamp with actual)
sudo cp /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar.backup_20260304_XXXXXX /home/azureuser/skillnest-0.0.1-SNAPSHOT.jar

# 4. Start service
sudo systemctl start skillnest

# 5. Verify
sudo systemctl status skillnest
```

## Post-Deployment Checklist

- [ ] Service is running: `sudo systemctl status skillnest`
- [ ] No errors in logs: `sudo journalctl -u skillnest -n 100`
- [ ] Health endpoint responds: `curl http://localhost:8080/actuator/health`
- [ ] Can access Swagger UI: `http://4.193.192.105:8080/swagger-ui/index.html`
- [ ] Auth endpoints work: Test login/register
- [ ] Database queries work: Test project list
- [ ] File uploads work (if applicable)

## Monitoring Commands

```bash
# Real-time logs
sudo journalctl -u skillnest -f

# Recent logs (last 200 lines)
sudo journalctl -u skillnest -n 200 --no-pager

# Service status
sudo systemctl status skillnest

# Resource usage
top -p $(pgrep -f skillnest)

# Memory usage
ps aux | grep skillnest

# Disk space
df -h
```

## Configuration Files Location

- Service file: `/etc/systemd/system/skillnest.service`
- Application jar: `/home/azureuser/skillnest-0.0.1-SNAPSHOT.jar`
- Logs: `sudo journalctl -u skillnest`
- Database: `4.193.192.105:5432/skillnest_db`

## Emergency Contacts / Resources

- Azure Portal: https://portal.azure.com
- VM IP: 4.193.192.105
- Database: skillnest_db @ 4.193.192.105:5432
- Swagger: http://4.193.192.105:8080/swagger-ui/index.html

---

**Deployment Date:** March 4, 2026  
**Version:** 1.3  
**Database Schema:** public (skillnest)

