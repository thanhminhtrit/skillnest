# 🔧 HƯỚNG DẪN KHẮC PHỤC LỖI 403 - THIẾU JWT TOKEN

## 🔴 VẤN ĐỀ BẠN ĐANG GẶP

Bạn đang gọi API:
```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -d ''
```

**Lỗi nhận được:** `403 Forbidden`

**Nguyên nhân:** ❌ **THIẾU JWT TOKEN trong header Authorization**

API này yêu cầu:
1. ✅ Authentication (phải có JWT token)
2. ✅ Authorization (phải có role CLIENT)

Khi không có token, Spring Security sẽ reject request ngay lập tức với lỗi 403.

---

## ✅ GIẢI PHÁP: GỌI API ĐÚNG CÁCH

### **Bước 1: Login để lấy JWT Token**

#### **Option A: Dùng curl**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "alice@example.com",
  "password": "password123"
}'
```

#### **Option B: Dùng Swagger UI**
1. Mở trình duyệt: `http://localhost:8080/swagger-ui.html`
2. Tìm endpoint: `POST /api/auth/login`
3. Click **"Try it out"**
4. Nhập body:
```json
{
  "email": "alice@example.com",
  "password": "password123"
}
```
5. Click **"Execute"**

---

#### **Response mẫu:**
```json
{
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTQ1MzA1LCJleHAiOjE3MTAyMzE3MDV9.ABC123XYZ789_VERY_LONG_TOKEN_HERE",
    "userId": 1,
    "email": "alice@example.com",
    "fullName": "Alice Client",
    "roles": ["CLIENT"]
  }
}
```

**📝 QUAN TRỌNG:** Copy toàn bộ giá trị của `token` (phần rất dài sau "token": "...")

---

### **Bước 2: Gọi API accept proposal VỚI TOKEN**

#### **Option A: Dùng curl (ĐÚNG)**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTQ1MzA1LCJleHAiOjE3MTAyMzE3MDV9.ABC123XYZ789_VERY_LONG_TOKEN_HERE'
```

**⚠️ Chú ý:**
- Thay `eyJhbGciOiJIUzUxMiJ9...` bằng token THẬT từ bước 1
- Phải có chữ `Bearer ` (có khoảng trắng) trước token
- Token rất dài (khoảng 200-300 ký tự)

---

#### **Option B: Dùng Swagger UI (KHUYẾN NGHỊ)**

1. Mở Swagger: `http://localhost:8080/swagger-ui.html`

2. Click nút **"Authorize"** ở góc trên bên phải

3. Nhập vào ô **"Value":**
```
Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTQ1MzA1LCJleHAiOjE3MTAyMzE3MDV9.ABC123XYZ789_VERY_LONG_TOKEN_HERE
```
   (Thay token thật vào)

4. Click **"Authorize"**

5. Bây giờ tìm endpoint: `POST /api/payments/proposals/{proposalId}/accept`

6. Click **"Try it out"**

7. Nhập `proposalId = 3`

8. Click **"Execute"**

---

### **Response thành công:**
```json
{
  "statusCode": 201,
  "message": "Payment request created. Please complete bank transfer.",
  "data": {
    "paymentRequestId": 1,
    "qrCodeUrl": "https://skillneststorage.blob.core.windows.net/qrcodes/SKILLNEST-ABC123.png",
    "paymentReference": "SKILLNEST-ABC123",
    "totalAmount": 1200000,
    "platformFee": 1200000,
    "studentAmount": 18000000,
    "currency": "VND",
    "bankDetails": {
      "bankName": "Vietcombank",
      "accountNumber": "1234567890",
      "accountName": "SKILLNEST PLATFORM",
      "transferNote": "SKILLNEST SKILLNEST-ABC123"
    },
    "message": "Please transfer 1200000 VND (8% platform maintenance fee) to proceed. Contract will be created after payment verification."
  }
}
```

---

## 🔍 SO SÁNH CURL SAI VÀ ĐÚNG

### ❌ **CURL SAI (Thiếu token - Lỗi 403)**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -d ''
```

**Kết quả:** 403 Forbidden ❌

---

### ✅ **CURL ĐÚNG (Có token - Thành công)**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTQ1MzA1LCJleHAiOjE3MTAyMzE3MDV9.YOUR_ACTUAL_TOKEN_HERE'
```

**Kết quả:** 201 Created ✅

---

## 📊 CHECKLIST DEBUG LỖI 403

Nếu vẫn gặp lỗi 403 sau khi thêm token, kiểm tra:

### ✅ **1. Token có đúng format không?**

**Đúng:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Sai:**
```
Authorization: eyJhbGciOiJIUzUxMiJ9...          # Thiếu "Bearer "
Authorization: Bearer  eyJhbGciOiJIUzUxMiJ9...  # 2 khoảng trắng
Authorization: bearer eyJhbGciOiJIUzUxMiJ9...   # "bearer" viết thường
```

---

### ✅ **2. Token có hết hạn chưa?**

Token có thời hạn 24 giờ. Nếu đã quá 24h kể từ khi login, phải login lại để lấy token mới.

**Kiểm tra trong logs:**
```
[ERROR] Expired JWT token
```

**Giải pháp:** Login lại (Bước 1)

---

### ✅ **3. User có role CLIENT không?**

Kiểm tra response của API login:
```json
{
  "roles": ["CLIENT"]  ← Phải có CLIENT ở đây
}
```

Nếu không có CLIENT, kiểm tra trong database:
```sql
SELECT u.email, r.name as role_name
FROM users u
JOIN user_roles ur ON u.user_id = ur.user_id
JOIN roles r ON ur.role_id = r.role_id
WHERE u.email = 'alice@example.com';
```

**Nếu không có role CLIENT, thêm vào:**
```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.email = 'alice@example.com' AND r.name = 'CLIENT';
```

---

### ✅ **4. Proposal có tồn tại không?**

Kiểm tra trong database:
```sql
SELECT * FROM proposals WHERE proposal_id = 3;
```

Nếu không có, thử với proposal_id khác (1, 2, ...)

---

### ✅ **5. Client có phải owner của project không?**

API này chỉ cho phép CLIENT accept proposal của project MÌNH TẠO.

Kiểm tra:
```sql
SELECT 
    p.proposal_id,
    pr.project_id,
    pr.title as project_title,
    c.email as client_email,
    s.email as student_email
FROM proposals p
JOIN projects pr ON p.project_id = pr.project_id
JOIN users c ON pr.client_id = c.user_id
JOIN users s ON p.student_id = s.user_id
WHERE p.proposal_id = 3;
```

**Client email phải khớp với email đã login!**

---

## 🎯 MẪU CURL ĐẦY ĐỦ

### **1. Login (Lấy token)**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "alice@example.com",
  "password": "password123"
}'
```

**Lưu token từ response vào biến (Linux/Mac):**
```bash
TOKEN=$(curl -s -X 'POST' \
  'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "alice@example.com",
  "password": "password123"
}' | jq -r '.data.token')

echo "Token: $TOKEN"
```

---

### **2. Accept proposal (Dùng token)**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -H "Authorization: Bearer $TOKEN"
```

**Hoặc thay token trực tiếp:**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTQ1MzA1LCJleHAiOjE3MTAyMzE3MDV9.YOUR_ACTUAL_TOKEN_HERE'
```

---

## 🎓 HIỂU VỀ JWT TOKEN

### **JWT Token là gì?**
JWT (JSON Web Token) là một chuỗi mã hóa chứa thông tin user:
- Email
- User ID
- Thời gian tạo
- Thời gian hết hạn

### **Format của JWT Token:**
```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTQ1MzA1LCJleHAiOjE3MTAyMzE3MDV9.signature_here
   ^                      ^                                                                                  ^
   Header                 Payload (chứa email, userId, iat, exp)                                             Signature
```

### **Cách hoạt động:**
1. User login → Server tạo JWT token → Trả về cho client
2. Client lưu token
3. Mỗi request sau đó, client gửi token trong header `Authorization: Bearer <token>`
4. Server verify token → Biết user là ai → Check quyền → Cho phép/Từ chối

---

## ⚠️ LƯU Ý BẢO MẬT

1. **Không share token công khai** - Token = quyền truy cập hệ thống
2. **Token có thời hạn** - Mặc định 24h, sau đó phải login lại
3. **Mỗi user có token riêng** - Không dùng token của người khác
4. **HTTPS trong production** - Token phải được mã hóa khi truyền

---

## 🎉 TÓM TẮT

### **Vấn đề của bạn:**
- ❌ Gọi API không có JWT token
- ❌ Spring Security block ngay lập tức → 403 Forbidden

### **Giải pháp:**
1. ✅ Login để lấy JWT token
2. ✅ Thêm header `Authorization: Bearer <token>` vào mọi request
3. ✅ Dùng Swagger UI cho dễ (đã tích hợp sẵn)

### **Câu lệnh curl đúng:**
```bash
# 1. Login
curl -X 'POST' 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email": "alice@example.com", "password": "password123"}'

# 2. Copy token từ response

# 3. Accept proposal VỚI TOKEN
curl -X 'POST' 'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'Authorization: Bearer <TOKEN_TỪ_BƯỚC_1>'
```

---

**Thử lại và cho tôi biết kết quả nhé! 🚀**

