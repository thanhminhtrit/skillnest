# SkillNest API Demo Script

## Prerequisites

- Backend running at `http://localhost:8080`
- PostgreSQL with seed data (subscription plans inserted)
- Environment variable `OPENAI_API_KEY` set

---

## SCENARIO 1: Registration & Profile Setup

### 1.1 Register Client (John)

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "john.client@test.com",
  "password": "Password@123",
  "fullName": "John Client",
  "phone": "0901234567",
  "role": "CLIENT"
}
```

**Expected:** `201 Created`
**Action:** Save `data.token` → `JOHN_TOKEN`

---

### 1.2 Register Student (Alice)

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "alice.student@test.com",
  "password": "Password@123",
  "fullName": "Alice Student",
  "phone": "0907654321",
  "role": "STUDENT"
}
```

**Expected:** `201 Created`
**Action:** Save `data.token` → `ALICE_TOKEN`

---

### 1.3 Login Client

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john.client@test.com",
  "password": "Password@123"
}
```

**Expected:** `200 OK`
**Action:** Refresh `JOHN_TOKEN`

---

### 1.4 Create Student Profile (Alice)

```http
PUT http://localhost:8080/api/profile
Authorization: Bearer {{ALICE_TOKEN}}
Content-Type: application/json

{
  "university": "FPT University",
  "major": "Software Engineering",
  "year": "Year 3",
  "gpa": 3.8,
  "bio": "Passionate full-stack developer with experience in React, Node.js, and Python.",
  "skills": ["React", "Node.js", "Python", "PostgreSQL", "Docker"],
  "interests": ["Web Development", "AI/ML", "Cloud Computing"],
  "preferredLocations": ["Ha Noi", "Remote"],
  "preferredJobTypes": ["Full-time", "Internship"]
}
```

**Expected:** `200 OK`

---

### 1.5 Create Company Info (John)

```http
POST http://localhost:8080/api/company-info
Authorization: Bearer {{JOHN_TOKEN}}
Content-Type: application/json

{
  "name": "TechViet Solutions",
  "location": "Ha Noi",
  "size": "100-500 employees",
  "industry": "Information Technology"
}
```

**Expected:** `201 Created`

---

## SCENARIO 2: Subscription Management

### 2.1 View Available Plans (Public)

```http
GET http://localhost:8080/api/subscriptions/plans
```

**Expected:** `200 OK`
```json
[
  { "name": "FREE",  "price": 0,      "postLimit": 1,    "aiMatchingLimit": 3   },
  { "name": "BASIC", "price": 199000, "postLimit": 15,   "aiMatchingLimit": 30  },
  { "name": "PRO",   "price": 399000, "postLimit": null, "aiMatchingLimit": 100 }
]
```

---

### 2.2 Check Current Subscription (John)

> **Note:** FREE subscription must be assigned to John via `/api/subscriptions/subscribe?planId=1`
> or by a startup data initializer before this step.

```http
GET http://localhost:8080/api/subscriptions/my
Authorization: Bearer {{JOHN_TOKEN}}
```

**Expected:**
```json
{
  "planName": "Gói Miễn Phí",
  "postsUsed": 0,
  "postsRemaining": 1,
  "aiMatchingUsed": 0,
  "aiMatchingRemaining": 3
}
```

---

### 2.3 Upgrade to BASIC Plan

```http
POST http://localhost:8080/api/subscriptions/subscribe?planId=2
Authorization: Bearer {{JOHN_TOKEN}}
```

**Expected:** `200 OK`
```json
{
  "planName": "Gói Cơ Bản",
  "postsRemaining": 15,
  "aiMatchingRemaining": 30
}
```

---

### 2.4 Alice Subscribes to FREE Plan

```http
POST http://localhost:8080/api/subscriptions/subscribe?planId=1
Authorization: Bearer {{ALICE_TOKEN}}
```

**Expected:** `200 OK`

---

## SCENARIO 3: Project Creation with Quota Check

### 3.1 Create Project 1 (Senior Full-Stack Dev)

```http
POST http://localhost:8080/api/projects
Authorization: Bearer {{JOHN_TOKEN}}
Content-Type: application/json

{
  "title": "Senior Full-Stack Developer",
  "description": "We are looking for an experienced full-stack developer to build a modern web application using React and Node.js. Must have strong knowledge of PostgreSQL and Docker.",
  "projectType": "FIXED_PRICE",
  "budgetMin": 15000000,
  "budgetMax": 25000000,
  "currency": "VND",
  "location": "Ha Noi",
  "employmentType": "Full-time",
  "salaryUnit": "MONTH",
  "skillIds": ["React", "Node.js", "PostgreSQL", "Docker"],
  "requirements": [
    "3+ years experience in full-stack development",
    "Strong knowledge of React and Node.js",
    "Experience with PostgreSQL and Docker"
  ],
  "headcountMin": 1,
  "headcountMax": 2,
  "deadline": "2025-12-31",
  "benefits": ["Competitive salary", "Flexible working hours", "Remote work option"]
}
```

**Expected:** `201 Created`
**Action:** Save `data.projectId` → `PROJECT_ID_1`

---

### 3.2 Create Project 2 (Frontend Intern)

```http
POST http://localhost:8080/api/projects
Authorization: Bearer {{JOHN_TOKEN}}
Content-Type: application/json

{
  "title": "Frontend Developer Intern",
  "description": "Looking for a talented frontend developer intern to join our team. Must be proficient in React.",
  "projectType": "FIXED_PRICE",
  "budgetMin": 5000000,
  "budgetMax": 8000000,
  "currency": "VND",
  "location": "Ha Noi",
  "employmentType": "Internship",
  "salaryUnit": "MONTH",
  "skillIds": ["React", "JavaScript", "HTML", "CSS"],
  "requirements": ["Student or fresh graduate", "Strong knowledge of React"],
  "headcountMin": 2,
  "headcountMax": 3,
  "deadline": "2025-11-30",
  "benefits": ["Real project experience", "Potential full-time offer"]
}
```

**Expected:** `201 Created`
**Action:** Save `data.projectId` → `PROJECT_ID_2`

---

### 3.3 Verify Quota Deduction

```http
GET http://localhost:8080/api/subscriptions/my
Authorization: Bearer {{JOHN_TOKEN}}
```

**Expected:** `postsUsed: 2, postsRemaining: 13`

---

## SCENARIO 4: AI Matching — Client Finds Best Students

### 4.1 Find Best Students for Project 1

```http
POST http://localhost:8080/api/ai-matching/find-students?projectId={{PROJECT_ID_1}}&limit=5
Authorization: Bearer {{JOHN_TOKEN}}
```

**Expected:** `200 OK`
```json
{
  "statusCode": 200,
  "message": "Found N matching students",
  "data": [
    {
      "entityId": 2,
      "entityType": "STUDENT",
      "name": "Alice Student",
      "matchScore": 0.87,
      "matchPercentage": 87,
      "matchReason": "Alice has strong expertise in React and Node.js..."
    }
  ]
}
```

---

### 4.2 Verify AI Quota Deducted

```http
GET http://localhost:8080/api/subscriptions/my
Authorization: Bearer {{JOHN_TOKEN}}
```

**Expected:** `aiMatchingUsed: 1, aiMatchingRemaining: 29`

---

## SCENARIO 5: AI Matching — Student Finds Best Projects

### 5.1 Alice Finds Matching Projects

```http
POST http://localhost:8080/api/ai-matching/find-projects?limit=10
Authorization: Bearer {{ALICE_TOKEN}}
```

**Expected:** `200 OK` — project list ranked by match score

---

### 5.2 Verify Alice's Quota

```http
GET http://localhost:8080/api/subscriptions/my
Authorization: Bearer {{ALICE_TOKEN}}
```

**Expected:** `aiMatchingUsed: 1, aiMatchingRemaining: 2` _(FREE plan: 3 limit)_

---

## SCENARIO 6: Full Workflow — Proposal → Payment → Contract

### 6.1 Alice Creates Proposal

```http
POST http://localhost:8080/api/proposals
Authorization: Bearer {{ALICE_TOKEN}}
Content-Type: application/json

{
  "projectId": {{PROJECT_ID_1}},
  "coverLetter": "I am very interested in this full-stack position. With my strong background in React, Node.js, and PostgreSQL, I can deliver high-quality results.",
  "proposedPrice": 20000000,
  "currency": "VND",
  "durationDays": 30
}
```

**Expected:** `201 Created`
**Action:** Save `data.proposalId` → `PROPOSAL_ID_1`

---

### 6.2 John Views Proposals

```http
GET http://localhost:8080/api/proposals/project/{{PROJECT_ID_1}}?page=0&size=10
Authorization: Bearer {{JOHN_TOKEN}}
```

**Expected:** `200 OK` — proposal list with Alice's entry

---

### 6.3 John Accepts Proposal & Gets Payment QR

```http
POST http://localhost:8080/api/payments/proposals/{{PROPOSAL_ID_1}}/accept
Authorization: Bearer {{JOHN_TOKEN}}
```

**Expected:** `201 Created` — QR code + bank details
**Action:** Save `data.paymentRequestId` → `PAYMENT_REQUEST_ID_1`

---

### 6.4 Admin Verifies Payment

```http
POST http://localhost:8080/api/payments/{{PAYMENT_REQUEST_ID_1}}/verify
Authorization: Bearer {{ADMIN_TOKEN}}
```

**Expected:** `200 OK` — contract auto-created

---

### 6.5 Alice Views Her Contract

```http
GET http://localhost:8080/api/contracts/my?page=0&size=10
Authorization: Bearer {{ALICE_TOKEN}}
```

**Expected:** `200 OK` — contract with `status: "ACTIVE"`

---

## SCENARIO 7: Quota Exceeded Tests

### 7.1 Alice Exhausts FREE AI Quota (3 uses)

```http
# Use 2nd time
POST http://localhost:8080/api/ai-matching/find-projects?limit=5
Authorization: Bearer {{ALICE_TOKEN}}

# Use 3rd time
POST http://localhost:8080/api/ai-matching/find-projects?limit=5
Authorization: Bearer {{ALICE_TOKEN}}

# 4th call → FAILS
POST http://localhost:8080/api/ai-matching/find-projects?limit=5
Authorization: Bearer {{ALICE_TOKEN}}
```

**Expected on 4th call:** `400 Bad Request`
```json
{ "statusCode": 400, "message": "AI matching limit reached. Upgrade your plan!" }
```

---

### 7.2 Post Quota Exceeded (BASIC = 15 posts, 2 already used)

After creating 13 more projects, the 16th call should fail:

**Expected:** `400 Bad Request`
```json
{ "statusCode": 400, "message": "Post limit reached. Used: 15/15" }
```

---

## SCENARIO 8: Upgrade to PRO

### 8.1 John Upgrades to PRO

```http
POST http://localhost:8080/api/subscriptions/subscribe?planId=3
Authorization: Bearer {{JOHN_TOKEN}}
```

**Expected:** `200 OK`

---

### 8.2 Verify Unlimited Posts

```http
GET http://localhost:8080/api/subscriptions/my
Authorization: Bearer {{JOHN_TOKEN}}
```

**Expected:**
```json
{
  "planName": "Gói Chuyên Nghiệp",
  "postLimit": null,
  "postsRemaining": null,
  "aiMatchingLimit": 100,
  "aiMatchingRemaining": 99
}
```

---

## Error Cases Reference

| Scenario | Request | Expected |
|---|---|---|
| Wrong password | POST /auth/login with bad password | 401 |
| No token | Any secured endpoint without Bearer | 403 |
| Expired token | Any secured endpoint with old JWT | 401 |
| Missing required field | POST /projects without title | 400 |
| Wrong role | Student calls /api/ai-matching/find-students | 403 |
| No subscription | GET /subscriptions/my without subscribing | 404 |
| Duplicate subscription | subscribe to same plan | Previous cancelled, new created |
