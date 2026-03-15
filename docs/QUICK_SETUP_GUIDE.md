# Admin/Manager Payment System - Quick Setup Guide

## Prerequisites
- PostgreSQL database running
- Java 17+
- Maven
- Existing SkillNest application

## Setup Steps

### 1. Run Database Migration

```bash
# Connect to your PostgreSQL database
psql -h 4.193.192.105 -U skillnest -d skillnest_db

# Run the migration script
\i D:/FPT_U/HK8/EXE2/skillnest/docs/database/migration_admin_payment_system.sql

# Or using psql command:
psql -h 4.193.192.105 -U skillnest -d skillnest_db -f D:/FPT_U/HK8/EXE2/skillnest/docs/database/migration_admin_payment_system.sql
```

### 2. Verify Tables Created

```sql
-- Check new tables exist
SELECT * FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('payment_requests', 'transactions');

-- Check roles added
SELECT * FROM roles WHERE name IN ('ADMIN', 'MANAGER');
```

### 3. Create Test Admin Account

```bash
# Generate bcrypt hash for password "Admin@123"
# Use online tool: https://bcrypt-generator.com/
# Or use Spring Security's BCryptPasswordEncoder

# Insert admin user
INSERT INTO users (email, password_hash, full_name, status, token_version, created_at, updated_at)
VALUES ('admin@skillnest.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye/IFw6sxYJC4Q3R6Fn8yeF8pXxMqY7Xy', 'System Admin', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

# Assign ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.email = 'admin@skillnest.com' AND r.name = 'ADMIN';
```

### 4. Create Test Manager Account

```sql
INSERT INTO users (email, password_hash, full_name, status, token_version, created_at, updated_at)
VALUES ('manager@skillnest.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye/IFw6sxYJC4Q3R6Fn8yeF8pXxMqY7Xy', 'System Manager', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.email = 'manager@skillnest.com' AND r.name = 'MANAGER';
```

### 5. Build and Run Application

```bash
cd D:\FPT_U\HK8\EXE2\skillnest

# Clean and build
mvn clean package -DskipTests

# Run application
java -jar target/skillnest-0.0.1-SNAPSHOT.jar

# Or use Maven
mvn spring-boot:run
```

### 6. Verify Application Started

Check logs for:
```
Started SkillnestApplication in X seconds
Tomcat started on port 8080
```

Access Swagger UI:
```
http://localhost:8080/swagger-ui/index.html
```

## Testing the Payment System

### Test Flow 1: Complete Payment Process

#### Step 1: Login as Client
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@example.com",
    "password": "password123"
  }'
```

Save the token from response: `CLIENT_TOKEN=eyJhbGci...`

#### Step 2: Create Project (if not exists)
```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Build Mobile App",
    "description": "Need Android developer",
    "projectType": "MOBILE_APP",
    "budgetMin": 5000000,
    "budgetMax": 10000000,
    "currency": "VND"
  }'
```

#### Step 3: Login as Student
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "password123"
  }'
```

Save token: `STUDENT_TOKEN=eyJhbGci...`

#### Step 4: Student Creates Proposal
```bash
curl -X POST http://localhost:8080/api/proposals \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 1,
    "coverLetter": "I can build your app",
    "proposedPrice": 8000000,
    "currency": "VND",
    "durationDays": 30
  }'
```

#### Step 5: Client Accepts Proposal (Initiates Payment)
```bash
curl -X POST http://localhost:8080/api/payments/proposals/1/accept \
  -H "Authorization: Bearer $CLIENT_TOKEN"
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Payment request created. Please complete bank transfer.",
  "data": {
    "paymentRequestId": 1,
    "qrCodeUrl": "https://...qrcodes/SKILLNEST-ABC123.png",
    "paymentReference": "SKILLNEST-ABC123",
    "totalAmount": 8000000,
    "platformFee": 640000,
    "studentAmount": 7360000,
    "currency": "VND",
    "bankDetails": {
      "bankName": "Vietcombank",
      "accountNumber": "1234567890",
      "accountName": "SKILLNEST PLATFORM",
      "transferNote": "SKILLNEST SKILLNEST-ABC123"
    }
  }
}
```

#### Step 6: Login as Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@skillnest.com",
    "password": "Admin@123"
  }'
```

Save token: `ADMIN_TOKEN=eyJhbGci...`

#### Step 7: Admin Views Pending Payments
```bash
curl -X GET http://localhost:8080/api/payments/pending \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Step 8: Admin Verifies Payment (Creates Contract)
```bash
curl -X POST http://localhost:8080/api/payments/1/verify \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Payment verified and contract created successfully",
  "data": {
    "paymentRequestId": 1,
    "status": "PAID",
    "verifiedBy": 3,
    "verifiedByName": "System Admin",
    "verifiedAt": "2026-03-09T10:30:00"
  }
}
```

#### Step 9: Check Contract Created
```bash
# Student or Client can check
curl -X GET http://localhost:8080/api/contracts \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

#### Step 10: Mark Project Completed
```bash
# Student marks completed
curl -X PUT http://localhost:8080/api/contracts/1/complete \
  -H "Authorization: Bearer $STUDENT_TOKEN"
```

#### Step 11: Admin Releases Payment to Student
```bash
curl -X POST http://localhost:8080/api/payments/contracts/1/release \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Payment released to student successfully",
  "data": {
    "transactionId": 2,
    "type": "PAYOUT",
    "amount": 7360000,
    "toUserId": 2,
    "toUserName": "John Student",
    "description": "Payment released to student for completed contract #1"
  }
}
```

#### Step 12: View All Transactions
```bash
curl -X GET http://localhost:8080/api/payments/contracts/1/transactions \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Test Flow 2: Admin User Management

#### View All Users
```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Search Users
```bash
curl -X GET "http://localhost:8080/api/admin/users?search=john&page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Filter by Status
```bash
curl -X GET "http://localhost:8080/api/admin/users?status=ACTIVE&page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Suspend User
```bash
curl -X PUT "http://localhost:8080/api/admin/users/2/status?status=SUSPENDED" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### Update User Profile
```bash
curl -X PUT http://localhost:8080/api/admin/users/2 \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe Updated",
    "phone": "0123456789",
    "bio": "Updated bio"
  }'
```

### Test Flow 3: Manager Dispute Handling

#### Login as Manager
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "manager@skillnest.com",
    "password": "Admin@123"
  }'
```

Save token: `MANAGER_TOKEN=eyJhbGci...`

#### View All Disputes
```bash
curl -X GET http://localhost:8080/api/manager/disputes \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

#### Filter Disputes by Status
```bash
curl -X GET "http://localhost:8080/api/manager/disputes?status=OPEN" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

#### Update Dispute Status
```bash
curl -X PUT "http://localhost:8080/api/manager/disputes/1/status?status=IN_REVIEW" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

#### View Messages in Disputed Contract
```bash
curl -X GET http://localhost:8080/api/manager/disputes/1/messages \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

#### Post System Message
```bash
curl -X POST "http://localhost:8080/api/manager/messages/system?conversationId=1&content=Your dispute is under review" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

#### Hide Inappropriate Message
```bash
curl -X PUT http://localhost:8080/api/manager/messages/5/hide \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

## Troubleshooting

### Issue: "Role not found"
**Solution:** Run database migration to add ADMIN and MANAGER roles.

### Issue: "Payment request already exists"
**Solution:** Cancel existing payment request first or use different proposal.

### Issue: "Forbidden - Insufficient permissions"
**Solution:** Verify user has correct role (ADMIN/MANAGER) assigned.

### Issue: "Contract not found"
**Solution:** Ensure payment has been verified and contract created.

### Issue: QR code generation fails
**Solution:** Check ZXing library is in dependencies (should be auto-included).

### Issue: Can't release payment
**Solution:** 
1. Check contract status is COMPLETED
2. Verify you're logged in as ADMIN (not MANAGER)
3. Ensure payment hasn't been released already

## Verification Checklist

- [ ] Database migration executed successfully
- [ ] payment_requests and transactions tables exist
- [ ] ADMIN and MANAGER roles exist
- [ ] Admin user created and can login
- [ ] Manager user created and can login
- [ ] Application starts without errors
- [ ] Swagger UI accessible
- [ ] Client can accept proposal and get QR code
- [ ] Admin can verify payment
- [ ] Contract auto-created after payment verification
- [ ] Admin can release payment to student
- [ ] Transactions recorded correctly
- [ ] Admin can view/manage users
- [ ] Manager can handle disputes
- [ ] Manager can moderate messages

## Security Notes

1. **Production Passwords**: Change default admin/manager passwords immediately
2. **Bank Details**: Update bank account information in application.yml
3. **QR Code Storage**: Implement Azure Blob Storage upload for production
4. **Payment Verification**: Implement actual bank API integration for automatic verification
5. **SSL/TLS**: Always use HTTPS in production
6. **Rate Limiting**: Implement rate limiting for payment endpoints
7. **Audit Logging**: Log all financial transactions

## Support

For issues or questions:
- Check logs: `logs/application.log`
- Review documentation: `docs/ADMIN_PAYMENT_SYSTEM.md`
- Contact development team

---

**Last Updated**: March 9, 2026

