#!/bin/bash
# SkillNest API Test Script
# Usage: bash test_scenarios.sh
# Requires: curl, jq

BASE_URL="http://localhost:8080"
JOHN_TOKEN=""
ALICE_TOKEN=""
ADMIN_TOKEN=""
PROJECT_ID_1=""
PROJECT_ID_2=""
PROPOSAL_ID_1=""
PAYMENT_REQUEST_ID_1=""

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

pass() { echo -e "${GREEN}[PASS]${NC} $1"; }
fail() { echo -e "${RED}[FAIL]${NC} $1"; }
info() { echo -e "${BLUE}[INFO]${NC} $1"; }
section() { echo -e "\n${YELLOW}======== $1 ========${NC}"; }

check_status() {
  local actual=$1
  local expected=$2
  local label=$3
  if [ "$actual" == "$expected" ]; then
    pass "$label (HTTP $actual)"
  else
    fail "$label - expected HTTP $expected, got $actual"
  fi
}

# ─────────────────────────────────────────
section "SCENARIO 1: Registration & Profile"
# ─────────────────────────────────────────

info "1.1 Register Client (John)"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.client@test.com",
    "password": "Password@123",
    "fullName": "John Client",
    "phone": "0901234567",
    "role": "CLIENT"
  }')
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "201" "Register Client"
JOHN_TOKEN=$(echo "$BODY" | jq -r '.data.token // empty')
[ -n "$JOHN_TOKEN" ] && pass "JOHN_TOKEN saved" || fail "Could not extract JOHN_TOKEN"

info "1.2 Register Student (Alice)"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.student@test.com",
    "password": "Password@123",
    "fullName": "Alice Student",
    "phone": "0907654321",
    "role": "STUDENT"
  }')
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "201" "Register Student"
ALICE_TOKEN=$(echo "$BODY" | jq -r '.data.token // empty')
[ -n "$ALICE_TOKEN" ] && pass "ALICE_TOKEN saved" || fail "Could not extract ALICE_TOKEN"

info "1.3 Login Client"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "john.client@test.com", "password": "Password@123"}')
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "200" "Login Client"
JOHN_TOKEN=$(echo "$BODY" | jq -r '.data.token // empty')

info "1.4 Create Alice's Profile"
RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/profile" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "university": "FPT University",
    "major": "Software Engineering",
    "year": "Year 3",
    "gpa": 3.8,
    "bio": "Passionate full-stack developer with experience in React, Node.js, and Python.",
    "skills": ["React", "Node.js", "Python", "PostgreSQL", "Docker"],
    "interests": ["Web Development", "AI/ML", "Cloud Computing"],
    "preferredLocations": ["Ha Noi", "Remote"],
    "preferredJobTypes": ["Full-time", "Internship"]
  }')
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "200" "Create Student Profile"

info "1.5 Create John's Company Info"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/company-info" \
  -H "Authorization: Bearer $JOHN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TechViet Solutions",
    "location": "Ha Noi",
    "size": "100-500 employees",
    "industry": "Information Technology"
  }')
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "201" "Create Company Info"

# ─────────────────────────────────────────
section "SCENARIO 2: Subscription Management"
# ─────────────────────────────────────────

info "2.1 View Available Plans"
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/subscriptions/plans")
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "200" "Get All Plans"

info "2.2 John subscribes to FREE plan"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/subscriptions/subscribe?planId=1" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "200" "John subscribes FREE"

info "2.3 Alice subscribes to FREE plan"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/subscriptions/subscribe?planId=1" \
  -H "Authorization: Bearer $ALICE_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "200" "Alice subscribes FREE"

info "2.4 John upgrades to BASIC"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/subscriptions/subscribe?planId=2" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "200" "Upgrade to BASIC"
POSTS_REMAINING=$(echo "$BODY" | jq -r '.data.postsRemaining // "?"')
info "Posts remaining after BASIC: $POSTS_REMAINING"

info "2.5 Check John's subscription"
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/subscriptions/my" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "200" "Get My Subscription (John)"

# ─────────────────────────────────────────
section "SCENARIO 3: Project Creation"
# ─────────────────────────────────────────

info "3.1 Create Project 1 (Senior Full-Stack Dev)"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/projects" \
  -H "Authorization: Bearer $JOHN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
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
    "requirements": ["3+ years experience", "Strong knowledge of React and Node.js"],
    "headcountMin": 1,
    "headcountMax": 2,
    "deadline": "2025-12-31",
    "benefits": ["Competitive salary", "Flexible working hours"]
  }')
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "201" "Create Project 1"
PROJECT_ID_1=$(echo "$BODY" | jq -r '.data.projectId // empty')
[ -n "$PROJECT_ID_1" ] && pass "PROJECT_ID_1=$PROJECT_ID_1" || fail "Could not extract PROJECT_ID_1"

info "3.2 Create Project 2 (Frontend Intern)"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/projects" \
  -H "Authorization: Bearer $JOHN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Frontend Developer Intern",
    "description": "Looking for a talented frontend developer intern to join our team.",
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
  }')
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "201" "Create Project 2"
PROJECT_ID_2=$(echo "$BODY" | jq -r '.data.projectId // empty')
[ -n "$PROJECT_ID_2" ] && pass "PROJECT_ID_2=$PROJECT_ID_2" || fail "Could not extract PROJECT_ID_2"

info "3.3 Verify post quota deducted"
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/subscriptions/my" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "200" "Check quota after 2 posts"
POSTS_USED=$(echo "$BODY" | jq -r '.data.postsUsed // "?"')
[ "$POSTS_USED" == "2" ] && pass "postsUsed=2 as expected" || fail "Expected postsUsed=2, got $POSTS_USED"

# ─────────────────────────────────────────
section "SCENARIO 4: AI Matching — Find Students"
# ─────────────────────────────────────────

info "4.1 John finds best students for Project 1"
RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/ai-matching/find-students?projectId=$PROJECT_ID_1&limit=5" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "200" "Find Best Students"
MATCH_COUNT=$(echo "$BODY" | jq -r '.data | length')
info "Matched $MATCH_COUNT students"

info "4.2 Verify AI quota deducted (John)"
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/subscriptions/my" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "200" "Check AI quota"
AI_USED=$(echo "$BODY" | jq -r '.data.aiMatchingUsed // "?"')
[ "$AI_USED" == "1" ] && pass "aiMatchingUsed=1 as expected" || fail "Expected aiMatchingUsed=1, got $AI_USED"

# ─────────────────────────────────────────
section "SCENARIO 5: AI Matching — Find Projects"
# ─────────────────────────────────────────

info "5.1 Alice finds matching projects"
RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/ai-matching/find-projects?limit=10" \
  -H "Authorization: Bearer $ALICE_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "200" "Find Best Projects (Alice)"
MATCH_COUNT=$(echo "$BODY" | jq -r '.data | length')
info "Matched $MATCH_COUNT projects"

info "5.2 Verify Alice's FREE quota (3 total, 1 used)"
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/subscriptions/my" \
  -H "Authorization: Bearer $ALICE_TOKEN")
BODY=$(echo "$RESP" | head -1)
AI_REMAINING=$(echo "$BODY" | jq -r '.data.aiMatchingRemaining // "?"')
[ "$AI_REMAINING" == "2" ] && pass "aiMatchingRemaining=2 as expected" || fail "Expected 2, got $AI_REMAINING"

# ─────────────────────────────────────────
section "SCENARIO 6: Proposal → Payment → Contract"
# ─────────────────────────────────────────

info "6.1 Alice submits proposal"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/proposals" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"projectId\": $PROJECT_ID_1,
    \"coverLetter\": \"I am very interested in this full-stack position. With my strong background in React, Node.js, and PostgreSQL, I can deliver excellent results.\",
    \"proposedPrice\": 20000000,
    \"currency\": \"VND\",
    \"durationDays\": 30
  }")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "201" "Submit Proposal"
PROPOSAL_ID_1=$(echo "$BODY" | jq -r '.data.proposalId // empty')
[ -n "$PROPOSAL_ID_1" ] && pass "PROPOSAL_ID_1=$PROPOSAL_ID_1" || fail "Could not extract PROPOSAL_ID_1"

info "6.2 John views proposals"
RESP=$(curl -s -w "\n%{http_code}" -X GET \
  "$BASE_URL/api/proposals/project/$PROJECT_ID_1?page=0&size=10" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "200" "View Proposals"

info "6.3 John accepts proposal (creates payment QR)"
RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/payments/proposals/$PROPOSAL_ID_1/accept" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "201" "Accept Proposal & Get QR"
PAYMENT_REQUEST_ID_1=$(echo "$BODY" | jq -r '.data.paymentRequestId // empty')
[ -n "$PAYMENT_REQUEST_ID_1" ] && pass "PAYMENT_REQUEST_ID_1=$PAYMENT_REQUEST_ID_1" || fail "Could not extract PAYMENT_REQUEST_ID_1"

info "6.4 Admin verifies payment (requires ADMIN_TOKEN)"
if [ -n "$ADMIN_TOKEN" ]; then
  RESP=$(curl -s -w "\n%{http_code}" -X POST \
    "$BASE_URL/api/payments/$PAYMENT_REQUEST_ID_1/verify" \
    -H "Authorization: Bearer $ADMIN_TOKEN")
  HTTP_CODE=$(echo "$RESP" | tail -1)
  check_status "$HTTP_CODE" "200" "Verify Payment"
else
  info "ADMIN_TOKEN not set — skipping payment verification"
fi

info "6.5 Alice views her contracts"
RESP=$(curl -s -w "\n%{http_code}" -X GET \
  "$BASE_URL/api/contracts/my?page=0&size=10" \
  -H "Authorization: Bearer $ALICE_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "200" "View Contracts"

# ─────────────────────────────────────────
section "SCENARIO 7: Quota Exceeded Tests"
# ─────────────────────────────────────────

info "7.1 Exhaust Alice's FREE AI quota (3 uses; 1 already used — 2 more)"
for i in 2 3; do
  RESP=$(curl -s -w "\n%{http_code}" -X POST \
    "$BASE_URL/api/ai-matching/find-projects?limit=5" \
    -H "Authorization: Bearer $ALICE_TOKEN")
  HTTP_CODE=$(echo "$RESP" | tail -1)
  check_status "$HTTP_CODE" "200" "AI use $i/3"
done

info "7.2 4th call should be rejected"
RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/ai-matching/find-projects?limit=5" \
  -H "Authorization: Bearer $ALICE_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
check_status "$HTTP_CODE" "400" "AI quota exceeded (4th call)"
MSG=$(echo "$BODY" | jq -r '.message // empty')
info "Message: $MSG"

# ─────────────────────────────────────────
section "SCENARIO 8: Upgrade to PRO"
# ─────────────────────────────────────────

info "8.1 John upgrades to PRO"
RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/subscriptions/subscribe?planId=3" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "200" "Upgrade to PRO"

info "8.2 Verify unlimited posts (postsRemaining = null)"
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/subscriptions/my" \
  -H "Authorization: Bearer $JOHN_TOKEN")
BODY=$(echo "$RESP" | head -1)
POST_LIMIT=$(echo "$BODY" | jq -r '.data.postLimit')
[ "$POST_LIMIT" == "null" ] && pass "postLimit=null (unlimited)" || fail "Expected null, got $POST_LIMIT"

# ─────────────────────────────────────────
section "ERROR CASES"
# ─────────────────────────────────────────

info "E1 - Wrong password"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "john.client@test.com", "password": "WrongPass!"}')
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "401" "Wrong password returns 401"

info "E2 - No token on secured endpoint"
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/subscriptions/my")
HTTP_CODE=$(echo "$RESP" | tail -1)
[ "$HTTP_CODE" == "401" ] || [ "$HTTP_CODE" == "403" ] \
  && pass "No token returns 401/403" \
  || fail "Expected 401/403, got $HTTP_CODE"

info "E3 - Student calls find-students (wrong role)"
RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/ai-matching/find-students?projectId=$PROJECT_ID_1&limit=5" \
  -H "Authorization: Bearer $ALICE_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "403" "Student calling CLIENT endpoint returns 403"

info "E4 - Client calls find-projects (wrong role)"
RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE_URL/api/ai-matching/find-projects?limit=5" \
  -H "Authorization: Bearer $JOHN_TOKEN")
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "403" "Client calling STUDENT endpoint returns 403"

info "E5 - Missing required fields on project creation"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/projects" \
  -H "Authorization: Bearer $JOHN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description": "Missing title"}')
HTTP_CODE=$(echo "$RESP" | tail -1)
check_status "$HTTP_CODE" "400" "Missing required field returns 400"

echo -e "\n${YELLOW}======== DONE ========${NC}"
echo "Summary variables:"
echo "  JOHN_TOKEN   = ${JOHN_TOKEN:0:20}..."
echo "  ALICE_TOKEN  = ${ALICE_TOKEN:0:20}..."
echo "  PROJECT_ID_1 = $PROJECT_ID_1"
echo "  PROJECT_ID_2 = $PROJECT_ID_2"
echo "  PROPOSAL_ID_1 = $PROPOSAL_ID_1"
echo "  PAYMENT_REQUEST_ID_1 = $PAYMENT_REQUEST_ID_1"
