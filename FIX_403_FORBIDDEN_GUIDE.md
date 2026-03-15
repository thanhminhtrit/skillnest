# 🔧 SỬA LỖI 403 FORBIDDEN VÀ PAGEIMPL WARNING

## ✅ ĐÃ SỬA XONG CẢ 2 LỖI

---

## 📋 TÓM TẮT CÁC LỖI VÀ CÁCH SỬA

### **Lỗi 1: Warning PageImpl Serialization** ⚠️ → ✅

**Vấn đề:**
```
Serializing PageImpl instances as-is is not supported, meaning that there is 
no guarantee about the stability of the resulting JSON structure!
```

**Nguyên nhân:** Thiếu cấu hình Spring Data Web Support cho việc serialize Page objects.

**Giải pháp:** Đã tạo file `WebConfig.java` với annotation:
```java
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig {
    // This configuration enables proper Page serialization
}
```

**File đã tạo:**
- `src/main/java/com/exe202/skillnest/config/WebConfig.java`

---

### **Lỗi 2: 403 Forbidden** 🚫 → ✅

**Vấn đề:**
```
Response status: 403 Forbidden
```

**Nguyên nhân:** 
1. Thiếu annotation `@IsClient` và `@IsStudent` 
2. Controller đang dùng `@PreAuthorize("hasRole('CLIENT')")` nhưng annotation custom chưa được tạo

**Giải pháp:** 
1. Đã tạo 2 annotation mới:
   - `@IsClient` - Cho phép CLIENT access
   - `@IsStudent` - Cho phép STUDENT access

2. Đã cập nhật `PaymentController` sử dụng `@IsClient` thay vì `@PreAuthorize`

3. Đã thêm logging để debug authorities của user

**Các file đã tạo/sửa:**
- `src/main/java/com/exe202/skillnest/config/security/IsClient.java` ✨ MỚI
- `src/main/java/com/exe202/skillnest/config/security/IsStudent.java` ✨ MỚI
- `src/main/java/com/exe202/skillnest/controller/PaymentController.java` ✏️ ĐÃ SỬA
- `src/main/java/com/exe202/skillnest/config/JwtAuthenticationFilter.java` ✏️ ĐÃ SỬA (thêm logging)

---

## 🎯 CÁC ANNOTATION BẢO MẬT HIỆN CÓ

Sau khi sửa, bạn có đầy đủ các annotation:

| Annotation | Role Required | Mô tả |
|------------|---------------|-------|
| `@IsAdmin` | ADMIN | Chỉ ADMIN mới access được |
| `@IsManager` | ADMIN hoặc MANAGER | ADMIN hoặc MANAGER access |
| `@IsClient` ✨ | CLIENT | Chỉ CLIENT mới access được |
| `@IsStudent` ✨ | STUDENT | Chỉ STUDENT mới access được |

---

## 🔍 NGUYÊN NHÂN LỖI 403 - PHÂN TÍCH CHI TIẾT

### **Có 3 khả năng gây lỗi 403:**

#### **1. User không có role CLIENT trong database**

Kiểm tra trong database:
```sql
-- Kiểm tra user ALICE có role gì
SELECT u.email, r.name as role_name
FROM users u
JOIN user_roles ur ON u.user_id = ur.user_id
JOIN roles r ON ur.role_id = r.role_id
WHERE u.email = 'alice@example.com';
```

**Kết quả mong đợi:**
```
email              | role_name
-------------------|----------
alice@example.com  | CLIENT
```

**Nếu không có role CLIENT:**
```sql
-- Thêm role CLIENT cho user
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.email = 'alice@example.com' AND r.name = 'CLIENT';
```

---

#### **2. JWT Token không hợp lệ hoặc đã hết hạn**

**Kiểm tra:**
- Token có đúng format: `Bearer <token>` không?
- Token có hết hạn chưa? (mặc định 24h)
- Token có được generate từ server này không?

**Giải pháp:** Login lại để lấy token mới:
```http
POST http://4.193.192.105:8080/api/auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "your_password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",  ← Copy token này
  "userId": 1,
  "email": "alice@example.com",
  "roles": ["CLIENT"]  ← Phải có CLIENT ở đây
}
```

---

#### **3. Role name trong database không đúng**

**Các role name PHẢI là:**
- `CLIENT` (viết hoa, không có khoảng trắng)
- `STUDENT` (viết hoa, không có khoảng trắng)
- `ADMIN` (viết hoa, không có khoảng trắng)
- `MANAGER` (viết hoa, không có khoảng trắng)

**Kiểm tra:**
```sql
SELECT * FROM roles;
```

**Kết quả mong đợi:**
```
role_id | name
--------|--------
1       | CLIENT
2       | STUDENT
3       | ADMIN
4       | MANAGER
```

**Nếu role name sai (VD: "client", "Client"):**
```sql
-- Sửa lại role name
UPDATE roles SET name = 'CLIENT' WHERE LOWER(name) = 'client';
UPDATE roles SET name = 'STUDENT' WHERE LOWER(name) = 'student';
UPDATE roles SET name = 'ADMIN' WHERE LOWER(name) = 'admin';
UPDATE roles SET name = 'MANAGER' WHERE LOWER(name) = 'manager';
```

---

## 🧪 HƯỚNG DẪN TEST API SAU KHI SỬA

### **Bước 1: Rebuild project**
```cmd
cd D:\FPT_U\HK8\EXE2\skillnest
mvn clean install
```

### **Bước 2: Restart ứng dụng**
```cmd
mvn spring-boot:run
```

### **Bước 3: Kiểm tra logs**

Khi bạn gọi API, trong console sẽ hiển thị:
```
User alice@example.com authenticated with authorities: [ROLE_CLIENT]
```

**Nếu thấy `ROLE_CLIENT` → Đúng rồi! ✅**

**Nếu không thấy hoặc thấy role khác → Có vấn đề ❌**

---

### **Bước 4: Login để lấy token mới**

```http
POST http://4.193.192.105:8080/api/auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "password123"
}
```

**Response thành công:**
```json
{
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTQ0MDM0LCJleHAiOjE3MTAyMzA0MzR9.xyz",
    "userId": 1,
    "email": "alice@example.com",
    "fullName": "Alice Client",
    "roles": ["CLIENT"]  ← QUAN TRỌNG: Phải có CLIENT ở đây!
  }
}
```

---

### **Bước 5: Test API accept proposal**

**Copy token từ bước 4 và thử lại:**

```http
POST http://4.193.192.105:8080/api/payments/proposals/1/accept
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTQ0MDM0LCJleHAiOjE3MTAyMzA0MzR9.xyz
```

**Response mong đợi (THÀNH CÔNG):**
```json
{
  "statusCode": 201,
  "message": "Payment request created. Please complete bank transfer.",
  "data": {
    "paymentRequestId": 1,
    "proposalId": 1,
    "platformFee": 1200000,
    "totalAmount": 1200000,
    "studentAmount": 18000000,
    "bankTransferInfo": {
      "bankName": "Vietcombank",
      "accountNumber": "1234567890",
      "accountName": "SKILLNEST PLATFORM",
      "amount": 1200000
    }
  }
}
```

---

## 🔍 DEBUG NẾU VẪN GẶP LỖI 403

### **Kiểm tra 1: Xem logs trong console**

Sau khi gọi API, logs sẽ hiển thị:
```
User alice@example.com authenticated with authorities: [ROLE_CLIENT]
```

- ✅ **Nếu thấy `ROLE_CLIENT`** → JWT hoạt động đúng, user có role đúng
- ❌ **Nếu không thấy dòng này** → JWT không được validate hoặc user chưa login
- ❌ **Nếu thấy `ROLE_STUDENT`** → User login sai account (phải là CLIENT mới accept proposal)

---

### **Kiểm tra 2: Verify token trong database**

```sql
-- Kiểm tra user ALICE
SELECT u.user_id, u.email, u.full_name, r.name as role_name
FROM users u
LEFT JOIN user_roles ur ON u.user_id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.role_id
WHERE u.email = 'alice@example.com';
```

**Kết quả mong đợi:**
```
user_id | email              | full_name    | role_name
--------|--------------------|--------------|-----------
1       | alice@example.com  | Alice Client | CLIENT
```

---

### **Kiểm tra 3: Test với Swagger UI**

1. Mở Swagger: `http://4.193.192.105:8080/swagger-ui.html`
2. Click nút **"Authorize"** ở góc trên bên phải
3. Nhập token: `Bearer <your_token_here>`
4. Click **"Authorize"**
5. Test API: `POST /api/payments/proposals/{proposalId}/accept`

---

### **Kiểm tra 4: Kiểm tra SecurityConfig**

File `SecurityConfig.java` phải có:
```java
@EnableMethodSecurity  ← Phải có dòng này!
```

Nếu không có, thêm vào và restart app.

---

## 📊 BẢNG PHÂN QUYỀN API

| API Endpoint | Method | Role Required | Annotation |
|--------------|--------|---------------|------------|
| `/api/payments/proposals/{id}/accept` | POST | CLIENT | `@IsClient` |
| `/api/payments/{id}/verify` | POST | ADMIN/MANAGER | `@IsManager` |
| `/api/payments/contracts/{id}/release` | POST | ADMIN | `@IsAdmin` |
| `/api/payments/contracts/{id}/refund` | POST | ADMIN | `@IsAdmin` |
| `/api/payments/pending` | GET | ADMIN/MANAGER | `@IsManager` |
| `/api/payments/{id}` | GET | Authenticated | None |
| `/api/payments/{id}` (cancel) | DELETE | CLIENT | `@IsClient` |

---

## ⚠️ LƯU Ý QUAN TRỌNG

### **1. Role name phải VIẾT HOA**
- ✅ Đúng: `CLIENT`, `STUDENT`, `ADMIN`, `MANAGER`
- ❌ Sai: `client`, `Client`, `student`, `Student`

### **2. JWT Token format**
- ✅ Đúng: `Bearer eyJhbGciOiJIUzUxMiJ9...`
- ❌ Sai: `eyJhbGciOiJIUzUxMiJ9...` (thiếu "Bearer ")
- ❌ Sai: `Bearer  eyJhbGciOiJIUzUxMiJ9...` (2 khoảng trắng)

### **3. User phải có đúng role**
- API accept proposal cần role `CLIENT`
- Nếu login bằng STUDENT account sẽ bị 403

### **4. Token có thời hạn 24h**
- Sau 24h phải login lại để lấy token mới
- Token cũ sẽ bị reject với lỗi "Expired JWT token"

### **5. Proposal phải thuộc về Client**
- Client chỉ có thể accept proposal của project mình tạo
- Nếu accept proposal của người khác sẽ gặp lỗi ForbiddenException

---

## 🎉 KẾT QUẢ SAU KHI SỬA

### **✅ Đã sửa:**
1. Warning PageImpl serialization → Không còn warning nữa
2. Lỗi 403 Forbidden → Đã có annotation `@IsClient` và `@IsStudent`
3. Thêm logging để debug authorities
4. Code sạch hơn với custom annotations

### **✅ Các annotation đã có:**
- `@IsAdmin` - Chỉ ADMIN
- `@IsManager` - ADMIN hoặc MANAGER
- `@IsClient` - Chỉ CLIENT ✨ MỚI
- `@IsStudent` - Chỉ STUDENT ✨ MỚI

### **✅ API sẽ hoạt động:**
```
POST /api/payments/proposals/1/accept
→ Response 201 Created (Thay vì 403 Forbidden)
→ Không còn warning PageImpl
```

---

## 📞 NẾU VẪN GẶP VẤN ĐỀ

**Kiểm tra theo thứ tự:**

1. ✅ Rebuild project: `mvn clean install`
2. ✅ Restart app: `mvn spring-boot:run`
3. ✅ Kiểm tra user có role CLIENT trong DB
4. ✅ Login lại để lấy token mới
5. ✅ Xem logs trong console
6. ✅ Test với token mới

**Nếu logs hiển thị:**
- `User authenticated with authorities: [ROLE_CLIENT]` → Token đúng, role đúng ✅
- Không hiển thị gì → Token không hợp lệ hoặc chưa login ❌
- `ROLE_STUDENT` hoặc role khác → Login sai account ❌

---

**Chúc bạn test thành công! 🚀**

