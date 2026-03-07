# Company Info Management API

## Overview
API này cho phép CLIENT users quản lý thông tin công ty của họ. Thông tin này sẽ được hiển thị trong Job Detail khi người dùng xem project.

---

## Endpoints

### 1. Create Company Info
**POST** `/api/company-info`

**Description:** Tạo thông tin công ty mới (mỗi user chỉ được tạo 1 lần)

**Auth:** Required (CLIENT role)

**Request Body:**
```json
{
  "name": "TechViet Solutions",
  "location": "Hà Nội",
  "size": "100-500 nhân viên",
  "industry": "Công nghệ thông tin"
}
```

**Validation:**
- `name`: Required, max 200 characters
- `location`: Optional, max 200 characters
- `size`: Optional, max 100 characters
- `industry`: Optional, max 200 characters

**Response (201):**
```json
{
  "statusCode": 201,
  "message": "Company info created successfully",
  "data": {
    "name": "TechViet Solutions",
    "location": "Hà Nội",
    "size": "100-500 nhân viên",
    "industry": "Công nghệ thông tin"
  }
}
```

**Error (400):**
```json
{
  "statusCode": 400,
  "message": "Company info already exists. Use update endpoint instead.",
  "data": null
}
```

---

### 2. Update Company Info
**PUT** `/api/company-info`

**Description:** Cập nhật thông tin công ty

**Auth:** Required (CLIENT role)

**Request Body:** (All fields optional)
```json
{
  "name": "TechViet Solutions Ltd.",
  "location": "Hà Nội & Hồ Chí Minh",
  "size": "500-1000 nhân viên",
  "industry": "Công nghệ thông tin & Dịch vụ số"
}
```

**Response (200):**
```json
{
  "statusCode": 200,
  "message": "Company info updated successfully",
  "data": {
    "name": "TechViet Solutions Ltd.",
    "location": "Hà Nội & Hồ Chí Minh",
    "size": "500-1000 nhân viên",
    "industry": "Công nghệ thông tin & Dịch vụ số"
  }
}
```

**Error (404):**
```json
{
  "statusCode": 404,
  "message": "Company info not found. Please create it first.",
  "data": null
}
```

---

### 3. Get My Company Info
**GET** `/api/company-info`

**Description:** Lấy thông tin công ty của user hiện tại

**Auth:** Required (CLIENT role)

**Response (200):**
```json
{
  "statusCode": 200,
  "message": "Company info retrieved successfully",
  "data": {
    "name": "TechViet Solutions",
    "location": "Hà Nội",
    "size": "100-500 nhân viên",
    "industry": "Công nghệ thông tin"
  }
}
```

**Error (404):**
```json
{
  "statusCode": 404,
  "message": "Company info not found",
  "data": null
}
```

---

### 4. Delete Company Info
**DELETE** `/api/company-info`

**Description:** Xóa thông tin công ty

**Auth:** Required (CLIENT role)

**Response (200):**
```json
{
  "statusCode": 200,
  "message": "Company info deleted successfully",
  "data": null
}
```

**Error (404):**
```json
{
  "statusCode": 404,
  "message": "Company info not found",
  "data": null
}
```

---

## Integration with Project API

Khi CLIENT có company info, thông tin này sẽ tự động được hiển thị trong response của Project Detail:

**GET** `/api/projects/public/{projectId}`

**Response:**
```json
{
  "statusCode": 200,
  "message": "Project retrieved successfully",
  "data": {
    "projectId": 1,
    "title": "Frontend Developer Intern",
    "description": "...",
    // ... other fields ...
    
    "company": {
      "name": "TechViet Solutions",
      "location": "Hà Nội",
      "size": "100-500 nhân viên",
      "industry": "Công nghệ thông tin"
    }
  }
}
```

**Nếu CLIENT chưa có company info:**
- `company.name` sẽ fallback về `clientName` (fullName của user)
- Các field khác (`location`, `size`, `industry`) sẽ là `null`

---

## Usage Flow

### For CLIENT Users:

1. **Sau khi đăng ký/đăng nhập lần đầu:**
   ```bash
   POST /api/company-info
   {
     "name": "Công ty của tôi",
     "location": "Hà Nội",
     "size": "10-50 nhân viên",
     "industry": "IT"
   }
   ```

2. **Cập nhật thông tin khi cần:**
   ```bash
   PUT /api/company-info
   {
     "size": "50-100 nhân viên"
   }
   ```

3. **Xem thông tin công ty hiện tại:**
   ```bash
   GET /api/company-info
   ```

4. **Tạo project** - Company info sẽ tự động được attach:
   ```bash
   POST /api/projects
   {
     "title": "Backend Developer",
     "description": "...",
     ...
   }
   ```

5. **Người dùng khác xem project** → Sẽ thấy company info đầy đủ

---

## Testing with Postman/Swagger

### 1. Create Company Info

**Swagger:** http://localhost:8080/swagger-ui.html
- Endpoint: `POST /api/company-info`
- Authorize với JWT token (CLIENT user)
- Request body mẫu:
```json
{
  "name": "TechViet Solutions",
  "location": "Hà Nội",
  "size": "100-500 nhân viên",
  "industry": "Công nghệ thông tin"
}
```

### 2. Get Company Info

- Endpoint: `GET /api/company-info`
- Authorize với JWT token
- Không cần body

### 3. Update Company Info

- Endpoint: `PUT /api/company-info`
- Request body (các field optional):
```json
{
  "name": "New Company Name"
}
```

### 4. Verify in Project Detail

- Tạo project mới: `POST /api/projects`
- Get project detail: `GET /api/projects/public/{projectId}`
- Kiểm tra field `company` trong response

---

## Business Rules

1. **One Company Info per User:**
   - Mỗi CLIENT user chỉ được tạo 1 company info
   - Nếu đã có, phải dùng UPDATE thay vì CREATE

2. **Authentication Required:**
   - Tất cả endpoints đều yêu cầu JWT token
   - Chỉ CLIENT users mới nên sử dụng (recommend thêm role check sau)

3. **Auto-Integration:**
   - Company info tự động được pull vào Project DTO
   - Không cần manual linking

4. **Fallback Mechanism:**
   - Nếu CLIENT chưa có company info, system dùng clientName làm company name

---

## Database Schema

**Table:** `skillnest.company_info`

| Column | Type | Constraints |
|--------|------|-------------|
| company_id | BIGSERIAL | PRIMARY KEY |
| user_id | BIGINT | NOT NULL, UNIQUE, FK(users) |
| name | VARCHAR(200) | NOT NULL |
| location | VARCHAR(200) | |
| size | VARCHAR(100) | |
| industry | VARCHAR(200) | |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |

---

## Error Handling

Tất cả errors đều trả về BaseResponse format:

- **400 Bad Request:** Validation failed hoặc business rule violation
- **404 Not Found:** User hoặc company info không tồn tại
- **401 Unauthorized:** Chưa đăng nhập
- **403 Forbidden:** Không có quyền (nếu thêm role check)

---

## Next Steps / Recommendations

1. **Add Role Check:**
   ```java
   @PreAuthorize("hasRole('CLIENT')")
   public ResponseEntity<BaseResponse> createCompanyInfo(...)
   ```

2. **Add Company Logo:**
   - Thêm field `logoUrl` vào CompanyInfo entity
   - Upload logo qua separate endpoint

3. **Company Verification:**
   - Thêm field `verified` boolean
   - Admin có thể verify company

4. **Public Company Profile:**
   - Tạo endpoint GET `/api/companies/{companyId}` (public)
   - List all projects của một company

---

Generated: 2026-03-02

