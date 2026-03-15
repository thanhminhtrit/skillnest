# 🔧 HƯỚNG DẪN SỬA LỖI 403 - TOKEN ĐÃ CÓ NHƯNG KHÔNG GỬI VỚI CURL

## 🔴 VẤN ĐỀ

Bạn đã:
- ✅ Login thành công
- ✅ Có token: `eyJhbGciOiJIUzUxMiJ9...`
- ✅ Authorize trong Swagger UI

Nhưng khi dùng curl:
```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -d ''
```

→ Vẫn bị 403 Forbidden ❌

**Nguyên nhân:** Swagger UI và curl là 2 công cụ RIÊNG BIỆT. Authorize trong Swagger KHÔNG tự động áp dụng cho curl!

---

## ✅ GIẢI PHÁP 1: THÊM TOKEN VÀO CURL (KHUYẾN NGHỊ)

```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBjb21wYW55LmNvbSIsInVzZXJJZCI6OCwiaWF0IjoxNzczMjE1NTUyLCJleHAiOjE3NzMzMDE5NTJ9.j2TibbMtY9mGeCpHAHwhTTixif4JrPRByz4LHXXiqSB2gCJfhLnl5Eyq3pXEv1spuvF_pa8r0HxqG_okNlrSDA' \
  -d ''
```

**Thay đổi:**
- ✅ Thêm dòng: `-H 'Authorization: Bearer <TOKEN>'`

---

## ✅ GIẢI PHÁP 2: TEST TRONG SWAGGER UI LUÔN (DỄ HƠN)

Vì bạn đã authorize trong Swagger rồi, test luôn trong đó:

1. Mở Swagger: `http://localhost:8080/swagger-ui.html`
2. Tìm: `POST /api/payments/proposals/{proposalId}/accept`
3. Click **"Try it out"**
4. Nhập `proposalId = 3`
5. Click **"Execute"**

→ Sẽ thành công ngay!

---

## 🔍 KIỂM TRA TOKEN CỦA BẠN

Token của bạn:
```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBjb21wYW55LmNvbSIsInVzZXJJZCI6OCwiaWF0IjoxNzczMjE1NTUyLCJleHAiOjE3NzMzMDE5NTJ9.j2TibbMtY9mGeCpHAHwhTTixif4JrPRByz4LHXXiqSB2gCJfhLnl5Eyq3pXEv1spuvF_pa8r0HxqG_okNlrSDA
```

**Decoded payload:**
```json
{
  "sub": "alice@company.com",
  "userId": 8,
  "iat": 1773215552,
  "exp": 1773301952
}
```

**Thông tin:**
- Email: `alice@company.com`
- User ID: `8`
- Issued At: `2026-03-09 14:32:32` (GMT+7)
- Expires At: `2026-03-10 14:32:32` (GMT+7)

⚠️ **LƯU Ý:** Token đã HẾT HẠN! (Expired At: 2026-03-10, hôm nay là 2026-03-11)

---

## ⚠️ VẤN ĐỀ PHỤ: TOKEN ĐÃ HẾT HẠN

Token của bạn expire vào ngày **2026-03-10 14:32:32**, nhưng hôm nay là **2026-03-11**.

**Nguyên nhân lỗi 403 CÓ THỂ LÀ:**
1. ❌ Token đã hết hạn (24h)
2. ❌ Không gửi token trong curl

---

## ✅ GIẢI PHÁP ĐẦY ĐỦ

### **Bước 1: Login lại để lấy token MỚI**

```bash
curl -X 'POST' \
  'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "email": "alice@company.com",
  "password": "YOUR_PASSWORD"
}'
```

**Response sẽ có token mới:**
```json
{
  "statusCode": 200,
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...<NEW_TOKEN>",
    "userId": 8,
    "email": "alice@company.com",
    "roles": ["CLIENT"]  ← Kiểm tra phải có CLIENT
  }
}
```

---

### **Bước 2: Copy token MỚI và gọi API**

```bash
curl -X 'POST' \
  'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer <NEW_TOKEN_FROM_STEP_1>' \
  -d ''
```

---

## 🎯 CÁCH NHANH NHẤT: DÙNG SWAGGER UI

1. **Login lại trong Swagger:**
   - Tìm `POST /api/auth/login`
   - Try it out
   - Nhập email: `alice@company.com` và password
   - Execute
   - Copy token mới

2. **Authorize lại:**
   - Click nút **"Authorize"** 
   - Paste token mới: `Bearer <NEW_TOKEN>`
   - Click "Authorize"

3. **Test API:**
   - Tìm `POST /api/payments/proposals/{proposalId}/accept`
   - Try it out
   - proposalId = 3
   - Execute

→ Thành công! ✅

---

## 📊 CHECKLIST

Kiểm tra từng bước:

### ✅ 1. User có role CLIENT không?

Kiểm tra trong database:
```sql
SELECT u.user_id, u.email, r.name as role_name
FROM users u
JOIN user_roles ur ON u.user_id = ur.user_id
JOIN roles r ON ur.role_id = r.role_id
WHERE u.user_id = 8;
```

**Kết quả phải có:** `role_name = CLIENT`

Nếu không có, thêm role:
```sql
INSERT INTO user_roles (user_id, role_id)
SELECT 8, r.role_id
FROM roles r
WHERE r.name = 'CLIENT';
```

---

### ✅ 2. Project có tồn tại và thuộc về user này không?

```sql
SELECT 
    p.proposal_id,
    pr.project_id,
    pr.title,
    pr.client_id,
    c.email as client_email
FROM proposals p
JOIN projects pr ON p.project_id = pr.project_id
JOIN users c ON pr.client_id = c.user_id
WHERE p.proposal_id = 3;
```

**Kiểm tra:** `client_id` phải là `8` (user ID của alice@company.com)

---

### ✅ 3. Proposal có status SUBMITTED không?

```sql
SELECT proposal_id, status FROM proposals WHERE proposal_id = 3;
```

**Kết quả phải là:** `status = SUBMITTED`

---

## 🔥 MẪU CURL COMMAND ĐÚNG

### **Windows CMD:**
```cmd
curl -X POST "http://localhost:8080/api/payments/proposals/3/accept" ^
  -H "accept: */*" ^
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.NEW_TOKEN_HERE"
```

### **Linux/Mac/Git Bash:**
```bash
curl -X POST 'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.NEW_TOKEN_HERE'
```

---

## 🎉 TÓM TẮT

**Vấn đề của bạn:**
1. ❌ Token đã hết hạn (expire 2026-03-10, hôm nay 2026-03-11)
2. ❌ Curl command không có header Authorization

**Giải pháp:**
1. ✅ Login lại để lấy token MỚI
2. ✅ Thêm header `-H 'Authorization: Bearer <NEW_TOKEN>'` vào curl
3. ✅ HOẶC test trong Swagger UI (đã authorize) cho dễ

**Lệnh đúng:**
```bash
# 1. Login lại
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@company.com","password":"YOUR_PASSWORD"}'

# 2. Copy token mới

# 3. Gọi API VỚI token mới
curl -X POST 'http://localhost:8080/api/payments/proposals/3/accept' \
  -H 'Authorization: Bearer <NEW_TOKEN>'
```

---

**Hãy login lại để lấy token mới và test lại nhé! 🚀**

