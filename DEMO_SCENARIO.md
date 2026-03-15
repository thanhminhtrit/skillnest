# KỊCH BẢN DEMO API - SKILLNEST PLATFORM

**Base URL Production:** `http://4.193.192.105:8080`  
**Base URL Local:** `http://localhost:8080`

---

## 📋 MỤC LỤC
1. [Chuẩn bị Demo](#chuẩn-bị-demo)
2. [Kịch bản Demo Chính](#kịch-bản-demo-chính)
3. [API Reference Chi Tiết](#api-reference-chi-tiết)
4. [Test Data](#test-data)

---

## 🎯 CHUẨN BỊ DEMO

### Tools cần có:
- **Postman** hoặc **Swagger UI** (`http://4.193.192.105:8080/swagger-ui/index.html`)
- **2 accounts**: 1 CLIENT và 1 STUDENT

### Kiểm tra trước khi demo:
```bash
# Check server status
curl http://4.193.192.105:8080/actuator/health

# Check Swagger UI
# Mở browser: http://4.193.192.105:8080/swagger-ui/index.html
```

---

## 🎬 KỊCH BẢN DEMO CHÍNH

### **Câu chuyện:** 
> **Alice** (CEO của công ty ABC Tech) cần tuyển 1 developer làm dự án mobile app.  
> **Bob** (sinh viên năm cuối CNTT) muốn tìm job part-time để kiếm tiền và trải nghiệm.

### **Workflow:** 
```
Register → Login → Create Project → Apply Proposal → Accept → Create Contract 
→ Send Messages → Complete → (Optional) Dispute
```

---

## 📝 CHI TIẾT TỪNG BƯỚC

### **BƯỚC 1: ĐĂNG KÝ TÀI KHOẢN**

#### 1.1. Register Alice (CLIENT)
```bash
POST http://4.193.192.105:8080/api/auth/register
Content-Type: application/json

{
  "email": "alice@company.com",
  "password": "Alice@123",
  "fullName": "Alice Nguyen",
  "phone": "0901234567",
  "role": "CLIENT"
}
```

**Response mẫu:**
```json
{
  "statusCode": 201,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBjb21wYW55LmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTUwMDAwLCJleHAiOjE3MTAyMzY0MDB9.xxx",
    "user": {
      "userId": 1,
      "email": "alice@company.com",
      "fullName": "Alice Nguyen",
      "roles": ["CLIENT"],
      "status": "ACTIVE",
      "phone": "0901234567",
      "avatarUrl": null,
      "createdAt": "2026-03-11T10:00:00"
    }
  }
}
```

**💡 Lưu token của Alice:** `TOKEN_ALICE`

---

#### 1.2. Register Bob (STUDENT)
```bash
POST http://4.193.192.105:8080/api/auth/register
Content-Type: application/json

{
  "email": "bob@student.com",
  "password": "Bob@123",
  "fullName": "Bob Tran",
  "phone": "0907654321",
  "role": "STUDENT"
}
```

**💡 Lưu token của Bob:** `TOKEN_BOB`

---

### **BƯỚC 2: LOGIN (nếu cần)**

```bash
POST http://4.193.192.105:8080/api/auth/login
Content-Type: application/json

{
  "email": "alice@company.com",
  "password": "Alice@123"
}
```

---

### **BƯỚC 3: TẠO COMPANY INFO (Alice - CLIENT)**

```bash
POST http://4.193.192.105:8080/api/company-info
Authorization: Bearer {TOKEN_ALICE}
Content-Type: application/json

{
  "name": "ABC Tech Solutions",
  "location": "Ho Chi Minh City, Vietnam",
  "size": "50-100 employees",
  "industry": "Software Development"
}
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Company info created successfully",
  "data": {
    "companyInfoId": 1,
    "userId": 1,
    "name": "ABC Tech Solutions",
    "location": "Ho Chi Minh City, Vietnam",
    "size": "50-100 employees",
    "industry": "Software Development",
    "createdAt": "2026-03-11T10:05:00"
  }
}
```

**💡 Lưu:** `COMPANY_INFO_ID = 1`

---

### **BƯỚC 4: TẠO DỰ ÁN (Alice - CLIENT)**

```bash
POST http://4.193.192.105:8080/api/projects
Authorization: Bearer {TOKEN_ALICE}
Content-Type: application/json

{
  "title": "Mobile App Development - E-commerce Platform",
  "description": "We need an experienced Flutter developer to build a cross-platform mobile app for our e-commerce business. The app should include user authentication, product catalog, shopping cart, and payment integration.",
  "projectType": "FULL_TIME",
  "budgetMin": 15000000,
  "budgetMax": 25000000,
  "currency": "VND",
  "location": "Remote",
  "employmentType": "CONTRACT",
  "salaryUnit": "MONTH",
  "skills": ["Flutter", "Dart", "Firebase", "REST API"],
  "requirements": [
    "2+ years experience with Flutter",
    "Strong understanding of mobile UI/UX",
    "Experience with Firebase and payment gateways",
    "Good communication skills"
  ],
  "benefits": [
    "Flexible working hours",
    "Work from home",
    "Performance bonus"
  ],
  "headcountMin": 1,
  "headcountMax": 2,
  "deadline": "2026-04-15"
}
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Project created successfully",
  "data": {
    "projectId": 1,
    "clientId": 1,
    "clientName": "Alice Nguyen",
    "title": "Mobile App Development - E-commerce Platform",
    "description": "We need an experienced Flutter developer...",
    "projectType": "FULL_TIME",
    "budgetMin": 15000000,
    "budgetMax": 25000000,
    "currency": "VND",
    "status": "OPEN",
    "location": "Remote",
    "employmentType": "CONTRACT",
    "salaryUnit": "MONTH",
    "skills": ["Flutter", "Dart", "Firebase", "REST API"],
    "requirements": [...],
    "benefits": [...],
    "headcountMin": 1,
    "headcountMax": 2,
    "deadline": "2026-04-15",
    "company": {
      "name": "ABC Tech Solutions",
      "location": "Ho Chi Minh City, Vietnam",
      "size": "50-100 employees",
      "industry": "Software Development"
    },
    "createdAt": "2026-03-11T10:10:00",
    "updatedAt": "2026-03-11T10:10:00"
  }
}
```

**💡 Lưu:** `PROJECT_ID = 1`

---

### **BƯỚC 5: XEM DỰ ÁN CÔNG KHAI (Bob hoặc không cần auth)**

```bash
GET http://4.193.192.105:8080/api/projects/public?page=0&size=10&sortBy=createdAt&direction=DESC
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Projects retrieved successfully",
  "data": {
    "content": [
      {
        "projectId": 1,
        "clientId": 1,
        "clientName": "Alice Nguyen",
        "title": "Mobile App Development - E-commerce Platform",
        "description": "We need an experienced Flutter developer...",
        "projectType": "FULL_TIME",
        "budgetMin": 15000000,
        "budgetMax": 25000000,
        "currency": "VND",
        "status": "OPEN",
        "location": "Remote",
        "employmentType": "CONTRACT",
        "salaryUnit": "MONTH",
        "skills": ["Flutter", "Dart", "Firebase", "REST API"],
        "requirements": [...],
        "benefits": [...],
        "company": {
          "name": "ABC Tech Solutions",
          "location": "Ho Chi Minh City, Vietnam",
          "size": "50-100 employees",
          "industry": "Software Development"
        },
        "createdAt": "2026-03-11T10:10:00",
        "postedAgo": "2 hours ago"
      }
    ],
    "pageable": {...},
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

---

### **BƯỚC 6: XEM CHI TIẾT DỰ ÁN (Bob)**

```bash
GET http://4.193.192.105:8080/api/projects/public/1
```

---

### **BƯỚC 7: TẠO PROPOSAL (Bob - STUDENT)**

```bash
POST http://4.193.192.105:8080/api/proposals
Authorization: Bearer {TOKEN_BOB}
Content-Type: application/json

{
  "projectId": 1,
  "coverLetter": "Dear Alice,\n\nI am very interested in your Mobile App Development project. I have 3 years of experience with Flutter and have successfully delivered 5+ e-commerce mobile apps.\n\nI am confident I can deliver a high-quality product within your timeline and budget.\n\nLooking forward to working with you!\n\nBest regards,\nBob Tran",
  "proposedBudget": 18000000,
  "estimatedDuration": 60,
  "portfolio": "https://github.com/bobtran"
}
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Proposal created successfully",
  "data": {
    "proposalId": 1,
    "projectId": 1,
    "projectTitle": "Mobile App Development - E-commerce Platform",
    "studentId": 2,
    "studentName": "Bob Tran",
    "coverLetter": "Dear Alice...",
    "proposedBudget": 18000000,
    "estimatedDuration": 60,
    "portfolio": "https://github.com/bobtran",
    "status": "SUBMITTED",
    "createdAt": "2026-03-11T10:30:00"
  }
}
```

**💡 Lưu:** `PROPOSAL_ID = 1`

---

### **BƯỚC 8: XEM PROPOSALS CHO PROJECT (Alice - CLIENT)**

```bash
GET http://4.193.192.105:8080/api/proposals/project/1?page=0&size=10
Authorization: Bearer {TOKEN_ALICE}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Proposals retrieved successfully",
  "data": {
    "content": [
      {
        "proposalId": 1,
        "projectId": 1,
        "projectTitle": "Mobile App Development - E-commerce Platform",
        "studentId": 2,
        "studentName": "Bob Tran",
        "coverLetter": "Dear Alice...",
        "proposedBudget": 18000000,
        "estimatedDuration": 60,
        "portfolio": "https://github.com/bobtran",
        "status": "SUBMITTED",
        "createdAt": "2026-03-11T10:30:00"
      }
    ],
    "totalElements": 1
  }
}
```

---

### **BƯỚC 9: ACCEPT PROPOSAL VỚI THANH TOÁN (Alice - CLIENT)**

⚠️ **LƯU Ý:** Từ bước này, hệ thống chuyển sang **Payment Flow** với Escrow System (ký quỹ).

```bash
POST http://4.193.192.105:8080/api/payments/proposals/1/accept
Authorization: Bearer {TOKEN_ALICE}
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Payment request created. Please complete bank transfer.",
  "data": {
    "paymentRequestId": 1,
    "proposalId": 1,
    "projectId": 1,
    "clientId": 1,
    "studentId": 2,
    "agreedBudget": 18000000,
    "platformFee": 1440000,
    "totalAmount": 19440000,
    "status": "PENDING_PAYMENT",
    "bankTransferInfo": {
      "bankName": "Vietcombank",
      "accountNumber": "1234567890",
      "accountName": "SKILLNEST PLATFORM",
      "transferContent": "SKILLNEST PAY1 ALICE",
      "amount": 19440000
    },
    "createdAt": "2026-03-11T11:00:00",
    "expiresAt": "2026-03-13T11:00:00"
  }
}
```

**💡 Lưu:** 
- `PAYMENT_REQUEST_ID = 1`
- `TOTAL_AMOUNT = 19,440,000 VND` (18M + 1.44M phí 8%)
- `BANK_TRANSFER_INFO` - Thông tin chuyển khoản

**📌 Note:** 
- Proposal status → `ACCEPTED`
- Project status → `IN_PROGRESS`
- Client phải chuyển khoản 19,440,000 VND vào tài khoản ký quỹ

---

### **BƯỚC 9A: XEM CHI TIẾT PAYMENT REQUEST**

```bash
GET http://4.193.192.105:8080/api/payments/1
Authorization: Bearer {TOKEN_ALICE}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Payment request retrieved successfully",
  "data": {
    "paymentRequestId": 1,
    "proposalId": 1,
    "projectId": 1,
    "projectTitle": "Mobile App Development - E-commerce Platform",
    "clientId": 1,
    "clientName": "Alice Nguyen",
    "studentId": 2,
    "studentName": "Bob Tran",
    "agreedBudget": 18000000,
    "platformFee": 1440000,
    "totalAmount": 19440000,
    "status": "PENDING_PAYMENT",
    "bankTransferInfo": {...},
    "createdAt": "2026-03-11T11:00:00",
    "expiresAt": "2026-03-13T11:00:00"
  }
}
```

---

### **BƯỚC 9B: ALICE CHUYỂN KHOẢN (Ngoài hệ thống)**

Alice thực hiện chuyển khoản ngân hàng:
```
Ngân hàng: Vietcombank
Số tài khoản: 1234567890
Tên tài khoản: SKILLNEST PLATFORM
Số tiền: 19,440,000 VND
Nội dung: SKILLNEST PAY1 ALICE
```

⏳ **Đợi Admin/Manager xác nhận thanh toán...**

---

### **BƯỚC 10: ADMIN/MANAGER XÁC NHẬN THANH TOÁN**

#### 10.1. Admin xem danh sách payment đang chờ
```bash
GET http://4.193.192.105:8080/api/payments/pending?page=0&size=10
Authorization: Bearer {TOKEN_ADMIN}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Pending payments retrieved successfully",
  "data": {
    "content": [
      {
        "paymentRequestId": 1,
        "proposalId": 1,
        "projectTitle": "Mobile App Development - E-commerce Platform",
        "clientName": "Alice Nguyen",
        "studentName": "Bob Tran",
        "totalAmount": 19440000,
        "status": "PENDING_PAYMENT",
        "createdAt": "2026-03-11T11:00:00"
      }
    ],
    "totalElements": 1
  }
}
```

---

#### 10.2. Admin verify payment (sau khi check bank statement)
```bash
POST http://4.193.192.105:8080/api/payments/1/verify
Authorization: Bearer {TOKEN_ADMIN}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Payment verified and contract created successfully",
  "data": {
    "paymentRequestId": 1,
    "status": "VERIFIED",
    "verifiedAt": "2026-03-11T14:00:00",
    "verifiedBy": 3,
    "verifierName": "Admin User",
    "contractId": 1,
    "escrowTransaction": {
      "transactionId": 1,
      "type": "ESCROW_DEPOSIT",
      "amount": 19440000,
      "status": "COMPLETED",
      "fromUserId": 1,
      "fromUserName": "Alice Nguyen",
      "description": "Escrow deposit for contract #1"
    }
  }
}
```

**💡 Lưu:** `CONTRACT_ID = 1`

**📌 Note:** 
- Payment status → `VERIFIED`
- Contract được tạo tự động với status `PENDING`
- Số tiền 19,440,000 VND được giữ trong tài khoản ký quỹ
- Conversation được tạo tự động

---

### **BƯỚC 11: ACTIVATE CONTRACT (Alice hoặc Bob)**
```bash
POST http://4.193.192.105:8080/api/contracts/1/activate
Authorization: Bearer {TOKEN_ALICE}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Contract activated successfully",
  "data": {
    "contractId": 1,
    "status": "ACTIVE",
    "startDate": "2026-03-11T11:10:00"
  }
}
```

---

### **BƯỚC 12: GỬI TIN NHẮN (Alice hoặc Bob)**

#### 12.1. Lấy conversation theo contract ID
```bash
GET http://4.193.192.105:8080/api/conversations/contract/1
Authorization: Bearer {TOKEN_ALICE}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Conversation retrieved successfully",
  "data": {
    "conversationId": 1,
    "contractId": 1,
    "createdAt": "2026-03-11T11:05:00"
  }
}
```

**💡 Lưu:** `CONVERSATION_ID = 1`

---

#### 12.2. Alice gửi tin nhắn
```bash
POST http://4.193.192.105:8080/api/conversations/1/messages
Authorization: Bearer {TOKEN_ALICE}
Content-Type: application/json

{
  "content": "Hi Bob! Welcome to the project. Let's schedule a kickoff meeting this week.",
  "type": "TEXT"
}
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Message sent successfully",
  "data": {
    "messageId": 1,
    "conversationId": 1,
    "senderId": 1,
    "senderName": "Alice Nguyen",
    "type": "TEXT",
    "content": "Hi Bob! Welcome to the project...",
    "sentAt": "2026-03-11T11:15:00"
  }
}
```

---

#### 12.3. Bob reply
```bash
POST http://4.193.192.105:8080/api/conversations/1/messages
Authorization: Bearer {TOKEN_BOB}
Content-Type: application/json

{
  "content": "Thanks Alice! I'm available this Thursday afternoon. Looking forward to it!",
  "type": "TEXT"
}
```

---

#### 12.4. Xem lịch sử tin nhắn
```bash
GET http://4.193.192.105:8080/api/conversations/1/messages?page=0&size=20
Authorization: Bearer {TOKEN_ALICE}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Messages retrieved successfully",
  "data": {
    "content": [
      {
        "messageId": 2,
        "conversationId": 1,
        "senderId": 2,
        "senderName": "Bob Tran",
        "type": "TEXT",
        "content": "Thanks Alice! I'm available...",
        "sentAt": "2026-03-11T11:20:00"
      },
      {
        "messageId": 1,
        "conversationId": 1,
        "senderId": 1,
        "senderName": "Alice Nguyen",
        "type": "TEXT",
        "content": "Hi Bob! Welcome to the project...",
        "sentAt": "2026-03-11T11:15:00"
      }
    ],
    "totalElements": 2
  }
}
```

---

### **BƯỚC 13: HOÀN THÀNH CONTRACT (Alice - CLIENT)**

```bash
POST http://4.193.192.105:8080/api/contracts/1/complete
Authorization: Bearer {TOKEN_ALICE}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Contract completed successfully",
  "data": {
    "contractId": 1,
    "status": "COMPLETED",
    "completedAt": "2026-05-11T11:30:00"
  }
}
```

**📌 Note:** Sau khi complete, Project status → `COMPLETED`

---

### **BƯỚC 13A: ADMIN GIẢI NGÂN CHO STUDENT (sau khi contract hoàn thành)**

⚠️ **LƯU Ý:** Bước quan trọng - Admin giải ngân từ tài khoản ký quỹ cho Student.

```bash
POST http://4.193.192.105:8080/api/payments/contracts/1/release
Authorization: Bearer {TOKEN_ADMIN}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Payment released to student successfully",
  "data": {
    "transactionId": 2,
    "type": "RELEASE_TO_STUDENT",
    "contractId": 1,
    "amount": 18000000,
    "platformFee": 1440000,
    "netAmount": 18000000,
    "status": "COMPLETED",
    "fromUserId": null,
    "fromUserName": "Escrow Account",
    "toUserId": 2,
    "toUserName": "Bob Tran",
    "description": "Payment released for completed contract #1",
    "processedBy": 3,
    "processedByName": "Admin User",
    "processedAt": "2026-05-11T12:00:00"
  }
}
```

**💡 Thông tin thanh toán:**
- **Student nhận:** 18,000,000 VND
- **Platform giữ:** 1,440,000 VND (phí 8%)
- **Tổng ký quỹ ban đầu:** 19,440,000 VND

**📌 Note:** 
- Tiền được chuyển từ tài khoản ký quỹ cho Bob
- Contract status vẫn là `COMPLETED`
- Transaction được ghi nhận đầy đủ

---

### **BƯỚC 13B: HOÀN TRẢ TIỀN CHO CLIENT (nếu hủy contract - Alternative flow)**

⚠️ **Scenario khác:** Nếu contract bị hủy, Admin có thể hoàn tiền cho Client.

```bash
POST http://4.193.192.105:8080/api/payments/contracts/1/refund?reason=Project%20cancelled%20by%20mutual%20agreement
Authorization: Bearer {TOKEN_ADMIN}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Payment refunded to client successfully",
  "data": {
    "transactionId": 3,
    "type": "REFUND_TO_CLIENT",
    "contractId": 1,
    "amount": 19440000,
    "platformFee": 0,
    "netAmount": 19440000,
    "status": "COMPLETED",
    "fromUserId": null,
    "fromUserName": "Escrow Account",
    "toUserId": 1,
    "toUserName": "Alice Nguyen",
    "description": "Refund for cancelled contract #1. Reason: Project cancelled by mutual agreement",
    "processedBy": 3,
    "processedByName": "Admin User",
    "processedAt": "2026-05-11T13:00:00"
  }
}
```

**💡 Thông tin hoàn trả:**
- **Client nhận lại:** 19,440,000 VND (FULL AMOUNT)
- **Platform không giữ phí** khi refund
- **Lý do:** Được ghi nhận trong description

---

### **BƯỚC 13C: HỦY PAYMENT REQUEST (nếu Client thay đổi ý định)**

⚠️ **Trước khi Admin verify:** Client có thể hủy payment request nếu chưa được xác nhận.

```bash
DELETE http://4.193.192.105:8080/api/payments/1
Authorization: Bearer {TOKEN_ALICE}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Payment request cancelled successfully",
  "data": null
}
```

**📌 Note:** 
- Chỉ hủy được khi status = `PENDING_PAYMENT`
- Sau khi Admin verify thì không thể hủy
- Proposal quay lại status `SUBMITTED`

---

### **BƯỚC 14: MỞ DISPUTE (Optional - nếu có vấn đề)**
```bash
POST http://4.193.192.105:8080/api/disputes
Authorization: Bearer {TOKEN_BOB}
Content-Type: application/json

{
  "contractId": 1,
  "reason": "The client has not released payment as agreed in the contract. I have completed all deliverables and the client has approved them, but payment is still pending."
}
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Dispute created successfully",
  "data": {
    "disputeId": 1,
    "contractId": 1,
    "raisedBy": 2,
    "raisedByName": "Bob Tran",
    "reason": "The client has not released payment...",
    "status": "OPEN",
    "createdAt": "2026-05-11T12:00:00"
  }
}
```

**💡 Lưu:** `DISPUTE_ID = 1`

---

### **BƯỚC 15: XEM DISPUTES (Bob hoặc Alice)**

```bash
GET http://4.193.192.105:8080/api/disputes/contract/1?page=0&size=10
Authorization: Bearer {TOKEN_BOB}
```

---

## 📚 API REFERENCE CHI TIẾT

### 🔐 **Authentication APIs**

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | ❌ | Đăng ký tài khoản mới |
| POST | `/api/auth/login` | ❌ | Đăng nhập |
| GET | `/api/auth/me` | ✅ | Lấy thông tin user hiện tại |

---

### 🏢 **Company Info APIs**

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/company-info` | ✅ | CLIENT | Tạo thông tin công ty |
| GET | `/api/company-info` | ✅ | CLIENT | Lấy thông tin công ty của mình |
| PUT | `/api/company-info` | ✅ | CLIENT | Cập nhật thông tin công ty |
| DELETE | `/api/company-info` | ✅ | CLIENT | Xóa thông tin công ty |

---

### 📋 **Project APIs**

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/projects` | ✅ | CLIENT | Tạo dự án mới |
| PUT | `/api/projects/{id}` | ✅ | CLIENT (owner) | Cập nhật dự án |
| DELETE | `/api/projects/{id}` | ✅ | CLIENT (owner) | Xóa dự án |
| POST | `/api/projects/{id}/close` | ✅ | CLIENT (owner) | Đóng dự án |
| GET | `/api/projects/public` | ❌ | - | Xem tất cả dự án công khai |
| GET | `/api/projects/public/{id}` | ❌ | - | Xem chi tiết dự án |
| GET | `/api/projects/my` | ✅ | CLIENT | Xem dự án của tôi |

---

### 📝 **Proposal APIs**

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/proposals` | ✅ | STUDENT | Tạo proposal mới |
| GET | `/api/proposals/project/{projectId}` | ✅ | CLIENT (owner) | Xem proposals của dự án |
| GET | `/api/proposals/my` | ✅ | STUDENT | Xem proposals của tôi |
| GET | `/api/proposals/{id}` | ✅ | - | Xem chi tiết proposal |
| POST | `/api/proposals/{id}/accept` | ✅ | CLIENT (owner) | Chấp nhận proposal |
| POST | `/api/proposals/{id}/reject` | ✅ | CLIENT (owner) | Từ chối proposal |

---

### 📄 **Contract APIs**

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/contracts/proposal/{proposalId}` | ✅ | CLIENT | Tạo contract từ proposal |
| POST | `/api/contracts/{id}/activate` | ✅ | Participants | Kích hoạt contract |
| POST | `/api/contracts/{id}/complete` | ✅ | CLIENT | Hoàn thành contract |
| POST | `/api/contracts/{id}/cancel` | ✅ | Participants | Hủy contract |
| GET | `/api/contracts/my` | ✅ | - | Xem contracts của tôi |
| GET | `/api/contracts/{id}` | ✅ | Participants | Xem chi tiết contract |

---

### 💬 **Conversation & Message APIs**

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/conversations/contract/{contractId}` | ✅ | Participants | Tạo conversation (auto khi tạo contract) |
| GET | `/api/conversations/contract/{contractId}` | ✅ | Participants | Lấy conversation theo contract |
| POST | `/api/conversations/{id}/messages` | ✅ | Participants | Gửi tin nhắn |
| GET | `/api/conversations/{id}/messages` | ✅ | Participants | Xem tin nhắn |

---

### ⚠️ **Dispute APIs**

| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/disputes` | ✅ | Participants | Mở dispute |
| GET | `/api/disputes/contract/{contractId}` | ✅ | Participants | Xem disputes của contract |
| GET | `/api/disputes/{id}` | ✅ | Participants | Xem chi tiết dispute |

---

## 🧪 TEST DATA

### Test Accounts (đã tạo sẵn nếu có)
```javascript
// CLIENT Account
{
  "email": "alice@company.com",
  "password": "Alice@123",
  "role": "CLIENT"
}

// STUDENT Account
{
  "email": "bob@student.com",
  "password": "Bob@123",
  "role": "STUDENT"
}
```

---

## 🔄 WORKFLOW STATUS TRANSITIONS

### Project Status Flow:
```
OPEN → IN_PROGRESS → COMPLETED/CLOSED
```

### Proposal Status Flow:
```
SUBMITTED → ACCEPTED/REJECTED
```

### Contract Status Flow:
```
PENDING → ACTIVE → COMPLETED/CANCELLED
```

### Dispute Status Flow:
```
OPEN → IN_REVIEW → RESOLVED/CLOSED
```

---

## 📌 LƯU Ý QUAN TRỌNG

### Authentication:
- Tất cả API có ✅ đều cần token trong header:
  ```
  Authorization: Bearer {YOUR_TOKEN}
  ```

### Permissions:
- **CLIENT**: Chỉ thao tác với projects/contracts của mình
- **STUDENT**: Chỉ thao tác với proposals/contracts của mình
- **Participants**: Chỉ 2 bên trong contract mới thấy conversation/messages

### Response Format:
Tất cả API đều trả về format:
```json
{
  "statusCode": 200,
  "message": "Success message",
  "data": { ... }
}
```

### Error Response:
```json
{
  "statusCode": 400,
  "message": "Error message",
  "data": null
}
```

---

## 🎯 DEMO TIPS

1. **Chuẩn bị trước:**
   - Tạo 2 accounts (CLIENT + STUDENT)
   - Lưu tokens vào Postman environment variables
   - Test từng endpoint riêng lẻ trước

2. **Demo flow:**
   - Bắt đầu từ Register → Login
   - Tạo Project → Apply Proposal → Accept
   - Create Contract → Send Messages
   - Complete hoặc Dispute (tùy tình huống)

3. **Highlight points:**
   - Auto create conversation khi tạo contract
   - Permission checks (chỉ participants thấy được)
   - Real-time messaging flow
   - Status transitions

4. **Backup plans:**
   - Có sẵn accounts đã tạo
   - Có sẵn data để fallback
   - Prepare screenshots nếu API down

---

## 🚀 QUICK START

### Postman Collection (Import vào Postman):
```bash
# Tạo environment variables:
BASE_URL = http://4.193.192.105:8080
TOKEN_ALICE = {paste_token_here}
TOKEN_BOB = {paste_token_here}
```

### Swagger UI:
```
http://4.193.192.105:8080/swagger-ui/index.html
```

---

**Good luck with your demo! 🎉**
