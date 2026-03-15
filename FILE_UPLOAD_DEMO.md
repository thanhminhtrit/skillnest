# KỊCH BẢN DEMO API UPLOAD FILE - SKILLNEST PLATFORM

**Base URL Production:** `http://4.193.192.105:8080`  
**Base URL Local:** `http://localhost:8080`

---

## 📋 MỤC LỤC
1. [Tổng quan Upload API](#tổng-quan-upload-api)
2. [Các API Upload có sẵn](#các-api-upload-có-sẵn)
3. [Kịch bản Demo Upload](#kịch-bản-demo-upload)
4. [Test với Postman](#test-với-postman)
5. [Test với cURL](#test-với-curl)

---

## 🎯 TỔNG QUAN UPLOAD API

### Hệ thống đã được tạo hoàn chỉnh bao gồm:

✅ **FileStorageService** - Service lưu file vào server  
✅ **FileValidator** - Validate file type, size, security  
✅ **FileUploadController** - 6 endpoints upload khác nhau  
✅ **ProfileController** - Upload/delete avatar  

### Các loại file được hỗ trợ:

| Loại File | Formats | Max Size | Endpoint |
|-----------|---------|----------|----------|
| **Avatar** | JPG, PNG, GIF, WebP | 5MB | `/api/files/upload/avatar` |
| **Chat Files** | Images + PDF, DOC, DOCX | 10MB | `/api/files/upload/chat-file` |
| **Documents** | PDF, DOC, DOCX, XLS, XLSX | 20MB | `/api/files/upload/document` |
| **Dispute Evidence** | Images + PDF, DOC | 15MB | `/api/files/upload/dispute-evidence` |
| **Multiple Files** | Mixed types | 50MB total | `/api/files/upload/multiple` |

---

## 📚 CÁC API UPLOAD CÓ SẴN

### 1️⃣ **Upload Avatar (Ảnh đại diện)**

```bash
POST /api/files/upload/avatar
Authorization: Bearer {TOKEN}
Content-Type: multipart/form-data

Form data:
- file: [image file]
```

**Validation:**
- ✅ Chỉ chấp nhận: JPG, PNG, GIF, WebP
- ✅ Max size: 5MB
- ✅ Kiểm tra MIME type thực sự (không chỉ dựa vào extension)

**Response:**
```json
{
  "statusCode": 201,
  "message": "Avatar uploaded successfully",
  "data": {
    "fileUrl": "http://4.193.192.105:8080/files/avatars/abc123-uuid.jpg",
    "fileName": "my-avatar.jpg",
    "fileSize": "1048576"
  }
}
```

---

### 2️⃣ **Upload Chat File (File trong tin nhắn)**

```bash
POST /api/files/upload/chat-file
Authorization: Bearer {TOKEN}
Content-Type: multipart/form-data

Form data:
- file: [image or document file]
```

**Validation:**
- ✅ Chấp nhận: Images (JPG, PNG, GIF) + Documents (PDF, DOC, DOCX)
- ✅ Max size: 10MB

**Response:**
```json
{
  "statusCode": 201,
  "message": "File uploaded successfully",
  "data": {
    "fileUrl": "http://4.193.192.105:8080/files/chat-files/def456-uuid.pdf",
    "fileName": "contract.pdf",
    "fileSize": "2097152",
    "fileType": "application/pdf"
  }
}
```

---

### 3️⃣ **Upload Document (Tài liệu)**

```bash
POST /api/files/upload/document
Authorization: Bearer {TOKEN}
Content-Type: multipart/form-data

Form data:
- file: [document file]
```

**Validation:**
- ✅ Chấp nhận: PDF, DOC, DOCX, XLS, XLSX
- ✅ Max size: 20MB

**Response:**
```json
{
  "statusCode": 201,
  "message": "Document uploaded successfully",
  "data": {
    "fileUrl": "http://4.193.192.105:8080/files/documents/ghi789-uuid.pdf",
    "fileName": "proposal.pdf",
    "fileSize": "5242880",
    "fileType": "application/pdf"
  }
}
```

---

### 4️⃣ **Upload Dispute Evidence (Bằng chứng tranh chấp)**

```bash
POST /api/files/upload/dispute-evidence
Authorization: Bearer {TOKEN}
Content-Type: multipart/form-data

Form data:
- file: [evidence file]
```

**Validation:**
- ✅ Chấp nhận: Images + PDF, DOC, DOCX
- ✅ Max size: 15MB

**Response:**
```json
{
  "statusCode": 201,
  "message": "Evidence uploaded successfully",
  "data": {
    "fileUrl": "http://4.193.192.105:8080/files/dispute-evidence/jkl012-uuid.jpg",
    "fileName": "evidence-screenshot.jpg",
    "fileSize": "3145728",
    "fileType": "image/jpeg"
  }
}
```

---

### 5️⃣ **Upload Multiple Files (Nhiều file cùng lúc)**

```bash
POST /api/files/upload/multiple?directory=documents
Authorization: Bearer {TOKEN}
Content-Type: multipart/form-data

Form data:
- files: [array of files]
```

**Validation:**
- ✅ Max 10 files
- ✅ Max 50MB tổng cộng

**Response:**
```json
{
  "statusCode": 201,
  "message": "Files uploaded successfully",
  "data": {
    "fileUrls": [
      "http://4.193.192.105:8080/files/documents/file1-uuid.pdf",
      "http://4.193.192.105:8080/files/documents/file2-uuid.jpg",
      "http://4.193.192.105:8080/files/documents/file3-uuid.docx"
    ],
    "count": 3
  }
}
```

---

### 6️⃣ **Delete File (Xóa file)**

```bash
DELETE /api/files/delete?fileUrl=http://4.193.192.105:8080/files/avatars/abc123-uuid.jpg
Authorization: Bearer {TOKEN}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "File deleted successfully",
  "data": null
}
```

---

### 7️⃣ **Upload Avatar qua Profile (Cập nhật luôn vào User)**

```bash
POST /api/profile/avatar
Authorization: Bearer {TOKEN}
Content-Type: multipart/form-data

Form data:
- file: [image file]
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Avatar uploaded successfully",
  "data": {
    "avatarUrl": "http://4.193.192.105:8080/files/avatars/xyz789-uuid.jpg",
    "profile": {
      "userId": 1,
      "email": "alice@company.com",
      "fullName": "Alice Nguyen",
      "avatarUrl": "http://4.193.192.105:8080/files/avatars/xyz789-uuid.jpg",
      "phone": "0901234567",
      ...
    }
  }
}
```

---

### 8️⃣ **Delete Avatar qua Profile**

```bash
DELETE /api/profile/avatar
Authorization: Bearer {TOKEN}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Avatar deleted successfully",
  "data": {
    "userId": 1,
    "email": "alice@company.com",
    "fullName": "Alice Nguyen",
    "avatarUrl": null,
    ...
  }
}
```

---

## 🎬 KỊCH BẢN DEMO UPLOAD

### **Scenario 1: Alice upload avatar**

#### Bước 1: Login để lấy token
```bash
POST http://4.193.192.105:8080/api/auth/login
Content-Type: application/json

{
  "email": "alice@company.com",
  "password": "Alice@123"
}
```

**💡 Lưu:** `TOKEN_ALICE`

---

#### Bước 2: Upload avatar qua Profile API (Recommend - tự động cập nhật User)
```bash
POST http://4.193.192.105:8080/api/profile/avatar
Authorization: Bearer {TOKEN_ALICE}
Content-Type: multipart/form-data

Form data:
- file: alice-avatar.jpg
```

**Result:** Avatar URL được lưu vào User.avatarUrl

---

#### Bước 3: Verify avatar đã update
```bash
GET http://4.193.192.105:8080/api/auth/me
Authorization: Bearer {TOKEN_ALICE}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "User retrieved successfully",
  "data": {
    "userId": 1,
    "email": "alice@company.com",
    "fullName": "Alice Nguyen",
    "avatarUrl": "http://4.193.192.105:8080/files/avatars/abc123-uuid.jpg",
    ...
  }
}
```

---

### **Scenario 2: Bob gửi file trong chat**

#### Bước 1: Upload file trước
```bash
POST http://4.193.192.105:8080/api/files/upload/chat-file
Authorization: Bearer {TOKEN_BOB}
Content-Type: multipart/form-data

Form data:
- file: project-mockup.pdf
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "File uploaded successfully",
  "data": {
    "fileUrl": "http://4.193.192.105:8080/files/chat-files/def456-uuid.pdf",
    "fileName": "project-mockup.pdf",
    "fileSize": "2097152",
    "fileType": "application/pdf"
  }
}
```

**💡 Lưu:** `FILE_URL = http://4.193.192.105:8080/files/chat-files/def456-uuid.pdf`

---

#### Bước 2: Gửi tin nhắn kèm file URL
```bash
POST http://4.193.192.105:8080/api/conversations/1/messages
Authorization: Bearer {TOKEN_BOB}
Content-Type: application/json

{
  "content": "Here's the project mockup for review",
  "type": "FILE",
  "fileUrl": "http://4.193.192.105:8080/files/chat-files/def456-uuid.pdf"
}
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Message sent successfully",
  "data": {
    "messageId": 5,
    "conversationId": 1,
    "senderId": 2,
    "senderName": "Bob Tran",
    "type": "FILE",
    "content": "Here's the project mockup for review",
    "fileUrl": "http://4.193.192.105:8080/files/chat-files/def456-uuid.pdf",
    "sentAt": "2026-03-11T14:30:00"
  }
}
```

---

### **Scenario 3: Upload bằng chứng dispute**

#### Bước 1: Upload evidence file
```bash
POST http://4.193.192.105:8080/api/files/upload/dispute-evidence
Authorization: Bearer {TOKEN_BOB}
Content-Type: multipart/form-data

Form data:
- file: payment-proof.jpg
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Evidence uploaded successfully",
  "data": {
    "fileUrl": "http://4.193.192.105:8080/files/dispute-evidence/ghi789-uuid.jpg",
    "fileName": "payment-proof.jpg",
    "fileSize": "1572864",
    "fileType": "image/jpeg"
  }
}
```

**💡 Lưu:** `EVIDENCE_URL`

---

#### Bước 2: Tạo dispute kèm evidence (Cần extend Dispute entity để hỗ trợ evidenceUrls)
```bash
POST http://4.193.192.105:8080/api/disputes
Authorization: Bearer {TOKEN_BOB}
Content-Type: application/json

{
  "contractId": 1,
  "reason": "Client has not released payment as agreed. See attached proof.",
  "evidenceUrl": "http://4.193.192.105:8080/files/dispute-evidence/ghi789-uuid.jpg"
}
```

---

### **Scenario 4: Upload nhiều file cùng lúc**

```bash
POST http://4.193.192.105:8080/api/files/upload/multiple?directory=documents
Authorization: Bearer {TOKEN_ALICE}
Content-Type: multipart/form-data

Form data:
- files[]: contract.pdf
- files[]: terms.docx
- files[]: payment-schedule.xlsx
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Files uploaded successfully",
  "data": {
    "fileUrls": [
      "http://4.193.192.105:8080/files/documents/aaa111-uuid.pdf",
      "http://4.193.192.105:8080/files/documents/bbb222-uuid.docx",
      "http://4.193.192.105:8080/files/documents/ccc333-uuid.xlsx"
    ],
    "count": 3
  }
}
```

---

## 🧪 TEST VỚI SWAGGER UI (KHUYẾN NGHỊ - DỄ NHẤT!)

### ✅ **Swagger UI HOÀN TOÀN HỖ TRỢ UPLOAD FILE!**

Bạn **KHÔNG CẦN** dùng Postman, có thể test ngay trên Swagger UI với giao diện trực quan.

### Hướng dẫn chi tiết upload trên Swagger UI:

#### **Bước 1: Mở Swagger UI**
```
http://4.193.192.105:8080/swagger-ui/index.html
```

#### **Bước 2: Authorize (Login)**
1. Click nút **"Authorize"** ở góc trên bên phải (hoặc icon ổ khóa 🔒)
2. Nhập JWT token vào ô **"Value"**:
   ```
   Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbGljZUBjb21wYW55LmNvbSIsInVzZXJJZCI6MSwiaWF0IjoxNzEwMTUwMDAwLCJleHAiOjE3MTAyMzY0MDB9.xxx
   ```
   ⚠️ **Lưu ý:** Nhập **CẢ "Bearer "** vào (có dấu cách sau Bearer)
3. Click **"Authorize"**
4. Click **"Close"**

#### **Bước 3: Tìm endpoint Upload**
1. Scroll xuống hoặc tìm section **"File Upload"** hoặc **"Profile"**
2. Ví dụ: Tìm `POST /api/profile/avatar`

#### **Bước 4: Upload File**
1. Click vào endpoint `POST /api/profile/avatar`
2. Click nút **"Try it out"** (góc trên bên phải của endpoint)
3. Bạn sẽ thấy một **ô chọn file** xuất hiện:
   ```
   file * (required)
   [Choose File] No file chosen
   ```
4. Click vào **"Choose File"** hoặc **"Browse"**
5. Chọn file ảnh từ máy tính của bạn (JPG, PNG, GIF)
6. Click **"Execute"** (nút màu xanh dương)

#### **Bước 5: Xem Response**
Scroll xuống phần **"Responses"** để xem kết quả:
```json
{
  "statusCode": 200,
  "message": "Avatar uploaded successfully",
  "data": {
    "avatarUrl": "http://4.193.192.105:8080/files/avatars/abc123-uuid.jpg",
    "profile": {...}
  }
}
```

### 📸 **Screenshot minh họa Swagger UI:**

```
┌─────────────────────────────────────────────────────────────┐
│ POST /api/profile/avatar                          🔒 Try it out│
├─────────────────────────────────────────────────────────────┤
│ Upload avatar image for current user                        │
│                                                             │
│ Parameters                                                  │
│ ┌─────────────────────────────────────────────────────┐   │
│ │ file * (required)                                   │   │
│ │ [Choose File] avatar.jpg                            │   │
│ └─────────────────────────────────────────────────────┘   │
│                                                             │
│ [Execute] [Clear]                                           │
└─────────────────────────────────────────────────────────────┘
```

### 🎯 **Demo nhanh trên Swagger UI:**

#### **Test 1: Upload Avatar**
```
1. Mở: http://4.193.192.105:8080/swagger-ui/index.html
2. Authorize với JWT token
3. Tìm: POST /api/profile/avatar
4. Try it out → Choose File → Select avatar.jpg
5. Execute
6. Xem response với avatarUrl
```

#### **Test 2: Upload Chat File**
```
1. Tìm: POST /api/files/upload/chat-file
2. Try it out → Choose File → Select document.pdf
3. Execute
4. Copy fileUrl từ response
5. Dùng fileUrl này để gửi message
```

#### **Test 3: Upload Multiple Files**
```
1. Tìm: POST /api/files/upload/multiple
2. Try it out
3. Trong parameter "directory", nhập: documents
4. Choose Files → Chọn NHIỀU file (Ctrl+Click hoặc Shift+Click)
5. Execute
6. Xem array of URLs trong response
```

### ⚠️ **Lưu ý khi upload trên Swagger UI:**

✅ **Swagger UI hỗ trợ đầy đủ:**
- ✅ Upload single file
- ✅ Upload multiple files
- ✅ Tất cả loại file (images, PDF, DOC, XLS)
- ✅ Hiển thị progress
- ✅ Xem response trực tiếp

❌ **Những gì Swagger KHÔNG hỗ trợ tốt:**
- ❌ Upload file > 100MB (browser limitation)
- ❌ Drag & drop file
- ❌ Preview file trước khi upload

### 🔍 **Troubleshooting Swagger UI Upload:**

**Problem:** Không thấy nút "Choose File"
- **Solution:** Click "Try it out" trước

**Problem:** Upload nhưng báo lỗi 401 Unauthorized
- **Solution:** Check Authorization token đã nhập chưa

**Problem:** File quá lớn, browser bị lag
- **Solution:** Dùng Postman hoặc cURL cho file > 50MB

**Problem:** Chọn file rồi nhưng không thấy tên file
- **Solution:** Refresh page và thử lại

---

## 🧪 TEST VỚI POSTMAN

### Cách test Upload trong Postman:

#### 1. Tạo Request mới
```
Method: POST
URL: http://4.193.192.105:8080/api/files/upload/avatar
```

#### 2. Add Authorization
```
Tab: Authorization
Type: Bearer Token
Token: {paste your JWT token here}
```

#### 3. Add File
```
Tab: Body
Select: form-data

Key: file
Type: File (click dropdown and select "File")
Value: [Click "Select Files" and choose your image]
```

#### 4. Send Request

---

## 🔧 TEST VỚI cURL

### Upload Avatar:
```bash
curl -X POST "http://4.193.192.105:8080/api/files/upload/avatar" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/avatar.jpg"
```

### Upload Chat File:
```bash
curl -X POST "http://4.193.192.105:8080/api/files/upload/chat-file" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/document.pdf"
```

### Upload Multiple Files:
```bash
curl -X POST "http://4.193.192.105:8080/api/files/upload/multiple?directory=documents" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "files=@/path/to/file1.pdf" \
  -F "files=@/path/to/file2.jpg" \
  -F "files=@/path/to/file3.docx"
```

### Upload Avatar via Profile:
```bash
curl -X POST "http://4.193.192.105:8080/api/profile/avatar" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/avatar.jpg"
```

### Delete File:
```bash
curl -X DELETE "http://4.193.192.105:8080/api/files/delete?fileUrl=http://4.193.192.105:8080/files/avatars/abc123-uuid.jpg" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📋 CHECKLIST DEMO

### Trước khi demo:
- [ ] Server đang chạy: `http://4.193.192.105:8080`
- [ ] Đã có account và token
- [ ] Chuẩn bị các file test:
  - [ ] Avatar image (JPG/PNG, < 5MB)
  - [ ] Document (PDF, < 20MB)
  - [ ] Screenshot/photo (JPG, < 10MB)
- [ ] Test Postman/cURL đã hoạt động
- [ ] Kiểm tra thư mục `uploads/` có quyền ghi

### Trong demo:
1. ✅ **Upload avatar** → Show user profile with new avatar
2. ✅ **Upload chat file** → Send message with file attachment
3. ✅ **Upload document** → Show file URL
4. ✅ **Upload multiple files** → Show array of URLs
5. ✅ **Delete file** → Verify file removed

---

## 🔒 SECURITY & VALIDATION

### File Validation Rules:

#### Avatar:
- ✅ Only: JPG, PNG, GIF, WebP
- ✅ Max size: 5MB
- ✅ MIME type check
- ✅ No executable files

#### Chat Files:
- ✅ Images: JPG, PNG, GIF
- ✅ Documents: PDF, DOC, DOCX
- ✅ Max size: 10MB
- ✅ Content-type validation

#### Documents:
- ✅ PDF, DOC, DOCX, XLS, XLSX
- ✅ Max size: 20MB
- ✅ No macros/scripts

#### Dispute Evidence:
- ✅ Images + Documents
- ✅ Max size: 15MB

#### Multiple Files:
- ✅ Max 10 files
- ✅ Max 50MB total

### Security Features:
- ✅ Unique filename (UUID)
- ✅ No path traversal (sanitize filename)
- ✅ MIME type validation
- ✅ File size limits
- ✅ JWT authentication required

---

## 📊 API SUMMARY TABLE

| # | Endpoint | Method | Auth | File Type | Max Size | Description |
|---|----------|--------|------|-----------|----------|-------------|
| 1 | `/api/files/upload/avatar` | POST | ✅ | Images | 5MB | Upload avatar |
| 2 | `/api/files/upload/chat-file` | POST | ✅ | Images + Docs | 10MB | Upload chat file |
| 3 | `/api/files/upload/document` | POST | ✅ | Documents | 20MB | Upload document |
| 4 | `/api/files/upload/dispute-evidence` | POST | ✅ | Images + Docs | 15MB | Upload evidence |
| 5 | `/api/files/upload/multiple` | POST | ✅ | Mixed | 50MB total | Upload multiple |
| 6 | `/api/files/delete` | DELETE | ✅ | - | - | Delete file |
| 7 | `/api/profile/avatar` | POST | ✅ | Images | 5MB | Upload + update avatar |
| 8 | `/api/profile/avatar` | DELETE | ✅ | - | - | Delete avatar |

---

## 🚀 QUICK START

### Swagger UI Test:
```
http://4.193.192.105:8080/swagger-ui/index.html

1. Authorize with JWT token
2. Find "File Upload" section
3. Try "POST /api/files/upload/avatar"
4. Upload file
5. Copy fileUrl from response
```

### Postman Collection:
```json
{
  "info": {
    "name": "SkillNest File Upload APIs"
  },
  "item": [
    {
      "name": "Upload Avatar",
      "request": {
        "method": "POST",
        "url": "{{BASE_URL}}/api/files/upload/avatar",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{TOKEN}}"
          }
        ],
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "file",
              "type": "file",
              "src": ""
            }
          ]
        }
      }
    }
  ]
}
```

---

## 💡 TIPS & NOTES

### File Storage:
- Files are stored in: `uploads/{directory}/{uuid-filename}`
- Example: `uploads/avatars/abc123-def456-uuid.jpg`
- URLs are public: `http://4.193.192.105:8080/files/avatars/abc123-def456-uuid.jpg`

### Best Practices:
1. **Upload file trước**, lấy URL
2. **Sau đó** gửi URL vào API khác (message, dispute, etc.)
3. **Không** upload trực tiếp trong API chính

### Error Handling:
- **413 Payload Too Large**: File quá lớn
- **400 Bad Request**: File type không hợp lệ
- **401 Unauthorized**: Thiếu hoặc sai token
- **500 Internal Error**: Lỗi lưu file

---

**Happy Uploading! 📤**
