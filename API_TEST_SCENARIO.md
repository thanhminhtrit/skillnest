# KỊCH BẢN TEST API - SKILLNEST BACKEND

## BASE URL
```
http://localhost:8080
```

## RESPONSE FORMAT
Tất cả API trả về định dạng:
```json
{
  "statusCode": 200,
  "message": "Success message",
  "data": { ... }
}
```

---

## KỊCH BẢN DEMO HOÀN CHỈNH (8-10 PHÚT)

### 🎭 NHÂN VẬT
- **Client**: Alice (Công ty ABC) - người đăng job
- **Student**: Bob (Sinh viên IT) - người ứng tuyển
- **Student**: Carol (Sinh viên IT) - người ứng tuyển khác

### 🎯 MỤC TIÊU
Minh họa workflow đầy đủ: Đăng job → Ứng tuyển → Chấp nhận → Hợp đồng → Nhắn tin → Tranh chấp

---

## PHẦN 1: SETUP & AUTHENTICATION (2 phút)

### 1.1. Đăng ký Client (Alice)
```bash
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "Alice Johnson",
  "email": "alice@company.com",
  "password": "Alice@123",
  "role": "CLIENT"
}
```

**Response Expected:**
```json
{
  "statusCode": 201,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGc...",
    "userId": 1,
    "email": "alice@company.com",
    "role": "CLIENT"
  }
}
```

**📝 LƯU Ý:** Lưu lại `token` vào biến `CLIENT_TOKEN`

---

### 1.2. Đăng ký Student (Bob)
```bash
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "Bob Smith",
  "email": "bob@student.com",
  "password": "Bob@123",
  "role": "STUDENT"
}
```

**Response Expected:**
```json
{
  "statusCode": 201,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGc...",
    "userId": 2,
    "email": "bob@student.com",
    "role": "STUDENT"
  }
}
```

**📝 LƯU Ý:** Lưu lại `token` vào biến `STUDENT_TOKEN`

---

### 1.3. Đăng ký Student (Carol)
```bash
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "Carol Lee",
  "email": "carol@student.com",
  "password": "Carol@123",
  "role": "STUDENT"
}
```

**📝 LƯU Ý:** Lưu lại `token` vào biến `STUDENT2_TOKEN`

---

### 1.4. Login lại (Optional - để demo login flow)
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "alice@company.com",
  "password": "Alice@123"
}
```

---

### 1.5. Get Current User Info
```bash
GET /api/auth/me
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "User retrieved successfully",
  "data": {
    "userId": 1,
    "fullName": "Alice Johnson",
    "email": "alice@company.com",
    "role": "CLIENT",
    "createdAt": "2026-03-03T10:00:00"
  }
}
```

---

## PHẦN 2: COMPANY INFO SETUP (1 phút)

### 2.1. Client tạo Company Info
```bash
POST /api/company-info
Authorization: Bearer {CLIENT_TOKEN}
Content-Type: application/json

{
  "name": "ABC Tech Solutions",
  "location": "Ho Chi Minh City, Vietnam",
  "size": "50-100 employees",
  "industry": "Software Development"
}
```

**Response Expected:**
```json
{
  "statusCode": 201,
  "message": "Company info created successfully",
  "data": {
    "companyInfoId": 1,
    "name": "ABC Tech Solutions",
    "location": "Ho Chi Minh City, Vietnam",
    "size": "50-100 employees",
    "industry": "Software Development"
  }
}
```

---

### 2.2. Get Company Info
```bash
GET /api/company-info
Authorization: Bearer {CLIENT_TOKEN}
```

---

## PHẦN 3: PROJECT/JOB MANAGEMENT (2 phút)

### 3.1. Client tạo Project (Job Posting)
```bash
POST /api/projects
Authorization: Bearer {CLIENT_TOKEN}
Content-Type: application/json

{
  "title": "Senior Java Spring Boot Developer",
  "description": "We are looking for an experienced Java developer to build our backend system...",
  "projectType": "FIXED_PRICE",
  "budgetMin": 15000000,
  "budgetMax": 25000000,
  "currency": "VND",
  "skillIds": ["Java", "Spring Boot", "PostgreSQL", "Docker"],
  "location": "Ho Chi Minh City",
  "employmentType": "Full-time",
  "salaryUnit": "MONTH",
  "requirements": [
        "3+ years experience with Java/Spring Boot",
        "Strong knowledge of RESTful API design",
        "Experience with PostgreSQL and JPA/Hibernate",
        "Understanding of microservices architecture"
  ],
  "headcountMin": 2,
  "headcountMax": 3,
  "deadline": "2026-04-30",
  "benefits": [
    "Competitive salary",
    "Health insurance",
    "Annual bonus",
    "Flexible working hours",
    "Remote work option"
  ]
}
```

**Response Expected:**
```json
{
  "statusCode": 201,
  "message": "Project created successfully",
  "data": {
    "projectId": 1,
    "clientId": 1,
    "clientName": "Alice Johnson",
    "title": "Senior Java Spring Boot Developer",
    "description": "...",
    "projectType": "FIXED_PRICE",
    "budgetMin": 15000000,
    "budgetMax": 25000000,
    "currency": "VND",
    "status": "OPEN",
    "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker"],
    "location": "Ho Chi Minh City",
    "employmentType": "Full-time",
    "salaryUnit": "MONTH",
    "requirements": [...],
    "company": {
      "name": "ABC Tech Solutions",
      "location": "Ho Chi Minh City, Vietnam",
      "size": "50-100 employees",
      "industry": "Software Development"
    },
    "isSaved": false,
    "hasApplied": null,
    "postedAgo": "Đăng 0 phút trước",
    "headcountMin": 2,
    "headcountMax": 3,
    "deadline": "2026-04-30",
    "benefits": [...],
    "createdAt": "2026-03-03T10:05:00",
    "updatedAt": "2026-03-03T10:05:00"
  }
}
```

**📝 LƯU Ý:** Lưu lại `projectId = 1`

---

### 3.2. Public Browse Projects (Không cần token)
```bash
GET /api/projects/public?page=0&size=10&sortBy=createdAt&direction=DESC
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "Projects retrieved successfully",
  "data": {
    "content": [
      {
        "projectId": 1,
        "title": "Senior Java Spring Boot Developer",
        ...
      }
    ],
    "pageable": {...},
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### 3.3. Get Project Detail (Public)
```bash
GET /api/projects/public/1
```

---

### 3.4. Client xem My Projects
```bash
GET /api/projects/my?page=0&size=10
Authorization: Bearer {CLIENT_TOKEN}
```

---

## PHẦN 4: PROPOSAL WORKFLOW (2 phút)

### 4.1. Student Bob tạo Proposal
```bash
POST /api/proposals
Authorization: Bearer {STUDENT_TOKEN}
Content-Type: application/json

{
  "projectId": 1,
  "coverLetter": "Dear Alice, I am very interested in this position. I have 4 years of experience with Java Spring Boot and have successfully delivered multiple enterprise projects. I am confident I can bring value to your team.",
  "proposedBudget": 20000000,
  "estimatedDuration": 30
}
```

**Response Expected:**
```json
{
  "statusCode": 201,
  "message": "Proposal created successfully",
  "data": {
    "proposalId": 1,
    "projectId": 1,
    "projectTitle": "Senior Java Spring Boot Developer",
    "studentId": 2,
    "studentName": "Bob Smith",
    "coverLetter": "Dear Alice, I am very interested...",
    "proposedBudget": 20000000,
    "estimatedDuration": 30,
    "status": "SUBMITTED",
    "createdAt": "2026-03-03T10:10:00"
  }
}
```

**📝 LƯU Ý:** Lưu lại `proposalId = 1`

---

### 4.2. Student Carol cũng tạo Proposal
```bash
POST /api/proposals
Authorization: Bearer {STUDENT2_TOKEN}
Content-Type: application/json

{
  "projectId": 1,
  "coverLetter": "Hello, I am Carol. I have 2 years experience with Spring Boot...",
  "proposedBudget": 18000000,
  "estimatedDuration": 45
}
```

**📝 LƯU Ý:** Lưu lại `proposalId = 2`

---

### 4.3. Student Bob xem My Proposals
```bash
GET /api/proposals/my?page=0&size=10
Authorization: Bearer {STUDENT_TOKEN}
```

---

### 4.4. Client xem all Proposals cho Project
```bash
GET /api/proposals/project/1?page=0&size=10
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "Proposals retrieved successfully",
  "data": {
    "content": [
      {
        "proposalId": 2,
        "studentName": "Carol Lee",
        "proposedBudget": 18000000,
        "status": "SUBMITTED"
      },
      {
        "proposalId": 1,
        "studentName": "Bob Smith",
        "proposedBudget": 20000000,
        "status": "SUBMITTED"
      }
    ],
    "totalElements": 2
  }
}
```

---

### 4.5. Client Reject Proposal của Carol
```bash
POST /api/proposals/2/reject
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "Proposal rejected successfully",
  "data": {
    "proposalId": 2,
    "status": "REJECTED"
  }
}
```

---

### 4.6. Client Accept Proposal của Bob
```bash
POST /api/proposals/1/accept
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "Proposal accepted successfully",
  "data": {
    "proposalId": 1,
    "status": "ACCEPTED"
  }
}
```

---

## PHẦN 5: CONTRACT MANAGEMENT (1.5 phút)

### 5.1. Client tạo Contract từ Accepted Proposal
```bash
POST /api/contracts/proposal/1
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 201,
  "message": "Contract created successfully",
  "data": {
    "contractId": 1,
    "projectId": 1,
    "projectTitle": "Senior Java Spring Boot Developer",
    "clientId": 1,
    "clientName": "Alice Johnson",
    "studentId": 2,
    "studentName": "Bob Smith",
    "status": "PENDING",
    "amount": 20000000,
    "startDate": "2026-03-03",
    "endDate": "2026-04-02",
    "createdAt": "2026-03-03T10:15:00"
  }
}
```

**📝 LƯU Ý:** Lưu lại `contractId = 1`

---

### 5.2. Student xem Contract Detail
```bash
GET /api/contracts/1
Authorization: Bearer {STUDENT_TOKEN}
```

---

### 5.3. Client Activate Contract
```bash
POST /api/contracts/1/activate
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "Contract activated successfully",
  "data": {
    "contractId": 1,
    "status": "ACTIVE"
  }
}
```

---

### 5.4. Student xem My Contracts
```bash
GET /api/contracts/my?page=0&size=10
Authorization: Bearer {STUDENT_TOKEN}
```

---

### 5.5. Client xem My Contracts
```bash
GET /api/contracts/my?page=0&size=10
Authorization: Bearer {CLIENT_TOKEN}
```

---

## PHẦN 6: CONVERSATION & MESSAGING (1.5 phút)

### 6.1. Tạo Conversation cho Contract (Auto-created hoặc manual)
```bash
POST /api/conversations/contract/1
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 201,
  "message": "Conversation created successfully",
  "data": {
    "conversationId": 1,
    "contractId": 1,
    "participants": [
      {"userId": 1, "name": "Alice Johnson"},
      {"userId": 2, "name": "Bob Smith"}
    ],
    "createdAt": "2026-03-03T10:20:00"
  }
}
```

**📝 LƯU Ý:** Lưu lại `conversationId = 1`

---

### 6.2. Client gửi message
```bash
POST /api/conversations/1/messages
Authorization: Bearer {CLIENT_TOKEN}
Content-Type: application/json

{
  "content": "Hi Bob, congratulations! When can you start?"
}
```

**Response Expected:**
```json
{
  "statusCode": 201,
  "message": "Message sent successfully",
  "data": {
    "messageId": 1,
    "conversationId": 1,
    "senderId": 1,
    "senderName": "Alice Johnson",
    "content": "Hi Bob, congratulations! When can you start?",
    "type": "TEXT",
    "sentAt": "2026-03-03T10:21:00"
  }
}
```

---

### 6.3. Student reply message
```bash
POST /api/conversations/1/messages
Authorization: Bearer {STUDENT_TOKEN}
Content-Type: application/json

{
  "content": "Thank you Alice! I can start next Monday. Looking forward to working with you!"
}
```

---

### 6.4. Get all Messages trong Conversation
```bash
GET /api/conversations/1/messages?page=0&size=20
Authorization: Bearer {STUDENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "Messages retrieved successfully",
  "data": {
    "content": [
      {
        "messageId": 2,
        "senderName": "Bob Smith",
        "content": "Thank you Alice! I can start next Monday...",
        "sentAt": "2026-03-03T10:22:00"
      },
      {
        "messageId": 1,
        "senderName": "Alice Johnson",
        "content": "Hi Bob, congratulations!...",
        "sentAt": "2026-03-03T10:21:00"
      }
    ],
    "totalElements": 2
  }
}
```

---

### 6.5. Get Conversation by Contract
```bash
GET /api/conversations/contract/1
Authorization: Bearer {CLIENT_TOKEN}
```

---

## PHẦN 7: DISPUTE MANAGEMENT (1 phút)

### 7.1. Student mở Dispute
```bash
POST /api/disputes
Authorization: Bearer {STUDENT_TOKEN}
Content-Type: application/json

{
  "contractId": 1,
  "reason": "PAYMENT_ISSUE",
  "description": "The client has not released the agreed milestone payment despite project completion."
}
```

**Response Expected:**
```json
{
  "statusCode": 201,
  "message": "Dispute created successfully",
  "data": {
    "disputeId": 1,
    "contractId": 1,
    "raisedById": 2,
    "raisedByName": "Bob Smith",
    "reason": "PAYMENT_ISSUE",
    "description": "The client has not released the agreed milestone payment...",
    "status": "OPEN",
    "createdAt": "2026-03-03T10:25:00"
  }
}
```

**📝 LƯU Ý:** Lưu lại `disputeId = 1`

---

### 7.2. Client xem Dispute
```bash
GET /api/disputes/1
Authorization: Bearer {CLIENT_TOKEN}
```

---

### 7.3. Get Disputes by Contract
```bash
GET /api/disputes/contract/1?page=0&size=10
Authorization: Bearer {CLIENT_TOKEN}
```

---

### 7.4. Update Dispute Status (Admin/Participant)
```bash
PUT /api/disputes/1/status?status=IN_PROGRESS
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "Dispute status updated successfully",
  "data": {
    "disputeId": 1,
    "status": "IN_PROGRESS"
  }
}
```

---

### 7.5. Get My Disputes
```bash
GET /api/disputes/my?page=0&size=10
Authorization: Bearer {STUDENT_TOKEN}
```

---

## PHẦN 8: PROFILE MANAGEMENT (Optional - 1 phút)

### 8.1. Student update Profile
```bash
PUT /api/profile
Authorization: Bearer {STUDENT_TOKEN}
Content-Type: application/json

{
  "fullName": "Bob Smith",
  "bio": "Experienced Java developer with passion for clean code",
  "location": "Ho Chi Minh City",
  "phone": "+84901234567",
  "skills": ["Java", "Spring Boot", "React", "Docker"],
  "experience": [
    {
      "title": "Backend Developer",
      "company": "Tech Corp",
      "duration": "2022-2024"
    }
  ]
}
```

---

### 8.2. Get Profile
```bash
GET /api/profile
Authorization: Bearer {STUDENT_TOKEN}
```

---

### 8.3. Get Application History
```bash
GET /api/profile/applications
Authorization: Bearer {STUDENT_TOKEN}
```

---

### 8.4. Get Profile Stats
```bash
GET /api/profile/stats
Authorization: Bearer {STUDENT_TOKEN}
```

---

### 8.5. Get Metadata (locations, job types)
```bash
GET /api/profile/metadata
Authorization: Bearer {STUDENT_TOKEN}
```

---

## PHẦN 9: FINALIZE CONTRACT (30 giây)

### 9.1. Client Complete Contract
```bash
POST /api/contracts/1/complete
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "Contract completed successfully",
  "data": {
    "contractId": 1,
    "status": "COMPLETED"
  }
}
```

---

### 9.2. Client Update Project (Optional)
```bash
PUT /api/projects/1
Authorization: Bearer {CLIENT_TOKEN}
Content-Type: application/json

{
  "title": "Senior Java Spring Boot Developer (UPDATED)",
  "budgetMax": 30000000
}
```

---

### 9.3. Client Close Project
```bash
POST /api/projects/1/close
Authorization: Bearer {CLIENT_TOKEN}
```

**Response Expected:**
```json
{
  "statusCode": 200,
  "message": "Project closed successfully",
  "data": {
    "projectId": 1,
    "status": "CLOSED"
  }
}
```

---

## PHẦN 10: NEGATIVE TEST CASES (Optional)

### 10.1. Unauthorized Access
```bash
GET /api/projects/my
# No Authorization header

# Expected: 403 Forbidden
```

---

### 10.2. Student cố gắng tạo Project (sai role)
```bash
POST /api/projects
Authorization: Bearer {STUDENT_TOKEN}
Content-Type: application/json

{
  "title": "Test Project",
  "description": "Should fail"
}

# Expected: 403 Forbidden or 400 Bad Request
```

---

### 10.3. Access Contract không phải của mình
```bash
# Giả sử có student khác (Carol) cố xem contract của Bob
GET /api/contracts/1
Authorization: Bearer {STUDENT2_TOKEN}

# Expected: 403 Forbidden
```

---

### 10.4. Validation Error
```bash
POST /api/projects
Authorization: Bearer {CLIENT_TOKEN}
Content-Type: application/json

{
  "title": "",
  "description": ""
}

# Expected: 400 Bad Request with validation errors
```

---

## 📋 CHECKLIST TRƯỚC KHI DEMO

- [ ] Database đã sạch (hoặc có seed data phù hợp)
- [ ] Application đang chạy trên port 8080
- [ ] Swagger UI accessible tại: http://localhost:8080/swagger-ui.html
- [ ] Đã chuẩn bị Postman Collection hoặc script
- [ ] Đã có danh sách Skills trong database (Java, Spring Boot, PostgreSQL, Docker, React...)
- [ ] JWT token expiry đủ dài cho session demo
- [ ] Logger level phù hợp (INFO hoặc DEBUG)

---

## 🔑 BIẾN MÔI TRƯỜNG CẦN LƯU

```
CLIENT_TOKEN=eyJhbGc...
STUDENT_TOKEN=eyJhbGc...
STUDENT2_TOKEN=eyJhbGc...
PROJECT_ID=1
PROPOSAL_ID_BOB=1
PROPOSAL_ID_CAROL=2
CONTRACT_ID=1
CONVERSATION_ID=1
DISPUTE_ID=1
```

---

## 🎯 KẾT QUẢ MONG ĐỢI SAU DEMO

1. ✅ Client đã tạo company info và 1 project
2. ✅ 2 students đã ứng tuyển (1 accepted, 1 rejected)
3. ✅ 1 contract được tạo và activated
4. ✅ Conversation với 2+ messages trao đổi
5. ✅ 1 dispute được mở và tracking
6. ✅ Contract được complete và project closed
7. ✅ Tất cả permissions được enforce đúng

---

## 📊 API ENDPOINTS SUMMARY

| Module | Endpoint | Method | Auth | Role |
|--------|----------|--------|------|------|
| **Auth** | `/api/auth/register` | POST | ❌ | - |
| | `/api/auth/login` | POST | ❌ | - |
| | `/api/auth/me` | GET | ✅ | ALL |
| **Company Info** | `/api/company-info` | POST | ✅ | CLIENT |
| | `/api/company-info` | GET | ✅ | CLIENT |
| | `/api/company-info` | PUT | ✅ | CLIENT |
| | `/api/company-info` | DELETE | ✅ | CLIENT |
| **Project** | `/api/projects` | POST | ✅ | CLIENT |
| | `/api/projects/{id}` | PUT | ✅ | CLIENT (owner) |
| | `/api/projects/{id}` | DELETE | ✅ | CLIENT (owner) |
| | `/api/projects/{id}/close` | POST | ✅ | CLIENT (owner) |
| | `/api/projects/public` | GET | ❌ | - |
| | `/api/projects/public/{id}` | GET | ❌ | - |
| | `/api/projects/my` | GET | ✅ | CLIENT |
| **Proposal** | `/api/proposals` | POST | ✅ | STUDENT |
| | `/api/proposals/project/{id}` | GET | ✅ | CLIENT (owner) |
| | `/api/proposals/my` | GET | ✅ | STUDENT |
| | `/api/proposals/{id}` | GET | ✅ | Participants |
| | `/api/proposals/{id}/accept` | POST | ✅ | CLIENT (owner) |
| | `/api/proposals/{id}/reject` | POST | ✅ | CLIENT (owner) |
| **Contract** | `/api/contracts/proposal/{id}` | POST | ✅ | CLIENT (owner) |
| | `/api/contracts/{id}/activate` | POST | ✅ | Participants |
| | `/api/contracts/{id}/complete` | POST | ✅ | CLIENT |
| | `/api/contracts/{id}/cancel` | POST | ✅ | Participants |
| | `/api/contracts/my` | GET | ✅ | ALL |
| | `/api/contracts/{id}` | GET | ✅ | Participants |
| **Conversation** | `/api/conversations/contract/{id}` | POST | ✅ | Participants |
| | `/api/conversations/contract/{id}` | GET | ✅ | Participants |
| | `/api/conversations/{id}/messages` | POST | ✅ | Participants |
| | `/api/conversations/{id}/messages` | GET | ✅ | Participants |
| **Dispute** | `/api/disputes` | POST | ✅ | Participants |
| | `/api/disputes/my` | GET | ✅ | ALL |
| | `/api/disputes/{id}` | GET | ✅ | Participants |
| | `/api/disputes/{id}/status` | PUT | ✅ | Participants/Admin |
| | `/api/disputes/contract/{id}` | GET | ✅ | Participants |
| **Profile** | `/api/profile` | GET | ✅ | ALL |
| | `/api/profile` | PUT | ✅ | ALL |
| | `/api/profile/applications` | GET | ✅ | STUDENT |
| | `/api/profile/stats` | GET | ✅ | ALL |
| | `/api/profile/metadata` | GET | ✅ | ALL |

**Tổng cộng: 40+ endpoints**

---

## 🚀 CÁCH CHẠY DEMO NHANH

### Option 1: Sử dụng Postman
1. Import file collection này vào Postman
2. Set environment variables (base_url, tokens)
3. Chạy từng request theo thứ tự trong folder

### Option 2: Sử dụng cURL Script
```bash
# Tạo file demo.sh và chạy các lệnh curl theo thứ tự
chmod +x demo.sh
./demo.sh
```

### Option 3: Sử dụng Swagger UI
1. Truy cập: http://localhost:8080/swagger-ui.html
2. Authorize với token sau khi register/login
3. Test từng endpoint theo kịch bản

---

## 💡 TIPS DEMO

1. **Prepare data trước**: Có thể seed skills vào DB trước
2. **Token management**: Dùng Postman environment variables
3. **Visual flow**: Vẽ diagram workflow để người xem dễ hiểu
4. **Error handling**: Demo cả success và error cases
5. **Performance**: Show pagination với số lượng data lớn
6. **Security**: Highlight permission checks
7. **Real-world scenarios**: Dùng tên, số liệu thực tế

---

## 📞 SUPPORT

Nếu gặp vấn đề:
- Check logs: Console output hoặc log file
- Verify JWT token chưa expire
- Confirm database connection
- Check role-based access control
- Verify request body format

---

**Happy Testing! 🎉**

