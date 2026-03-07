# Recruitment Form Fields Extension - Summary

## ✅ BACKWARD COMPATIBILITY GUARANTEED
Tất cả field cũ được giữ nguyên 100%. Request/Response mới là **superset** của cũ.

---

## 📋 Summary of Changes

### 1. **New Fields Added to CreateProjectRequest** (Existing fields unchanged)

#### Existing Fields (13 fields - Kept as-is):
- `title` (required)
- `description` (required)
- `projectType` (default: "FIXED_PRICE")
- `budgetMin`, `budgetMax`, `currency` (default: "VND")
- `skillIds` (Set<Long>)
- `location`, `employmentType`, `salaryUnit`
- `requirements` (List<String>)

#### **NEW Fields (4 fields - All optional):**
- `headcountMin` (Integer) - Số lượng tuyển dụng tối thiểu (e.g., 2 from "2-3")
- `headcountMax` (Integer) - Số lượng tuyển dụng tối đa (e.g., 3 from "2-3")
- `deadline` (LocalDate) - Hạn nộp hồ sơ (format: "2024-12-31")
- `benefits` (List<String>) - Danh sách quyền lợi (mỗi phần tử 1 benefit)

---

### 2. **Request Body Examples**

#### Before (Old request still works):
```json
{
  "title": "Frontend Developer Intern",
  "description": "Tìm kiếm sinh viên...",
  "projectType": "FIXED_PRICE",
  "budgetMin": 3500000,
  "budgetMax": 5000000,
  "currency": "VND",
  "skillIds": [1, 2, 3],
  "location": "Hà Nội",
  "employmentType": "Thực tập",
  "salaryUnit": "MONTH",
  "requirements": [
    "Sinh viên năm 3, 4",
    "Có kiến thức HTML, CSS, JavaScript"
  ]
}
```

#### After (Full recruitment form - Superset):
```json
{
  "title": "Frontend Developer Intern",
  "description": "Tìm kiếm sinh viên năng động, đam mê lập trình web để tham gia dự án phát triển web application.",
  "projectType": "FIXED_PRICE",
  "budgetMin": 3500000,
  "budgetMax": 5000000,
  "currency": "VND",
  "skillIds": [1, 2, 3, 4],
  "location": "Hà Nội",
  "employmentType": "Thực tập",
  "salaryUnit": "MONTH",
  "requirements": [
    "Sinh viên năm 3, 4 hoặc mới tốt nghiệp",
    "Có kiến thức cơ bản về HTML, CSS, JavaScript",
    "Ưu tiên biết React hoặc Vue.js",
    "Có khả năng làm việc nhóm tốt"
  ],
  "headcountMin": 2,
  "headcountMax": 3,
  "deadline": "2024-12-31",
  "benefits": [
    "Môi trường làm việc chuyên nghiệp",
    "Cơ hội thăng tiến",
    "Đào tạo kỹ năng"
  ]
}
```

---

### 3. **Response Example (GET /api/projects/public/{id})**

```json
{
  "statusCode": 200,
  "message": "Project retrieved successfully",
  "data": {
    // ====== EXISTING FIELDS (13) ======
    "projectId": 1,
    "clientId": 7,
    "clientName": "John Doe",
    "title": "Frontend Developer Intern",
    "description": "...",
    "projectType": "FIXED_PRICE",
    "budgetMin": 3500000,
    "budgetMax": 5000000,
    "currency": "VND",
    "status": "OPEN",
    "skills": ["React", "JavaScript", "CSS", "HTML"],
    "createdAt": "2026-03-01T10:00:00",
    "updatedAt": "2026-03-01T10:00:00",
    
    // ====== PREVIOUS EXTENSION FIELDS (8) ======
    "location": "Hà Nội",
    "employmentType": "Thực tập",
    "salaryUnit": "MONTH",
    "requirements": [
      "Sinh viên năm 3, 4",
      "Có kiến thức HTML, CSS, JS"
    ],
    "company": {
      "name": "TechViet Solutions",
      "location": "Hà Nội",
      "size": "100-500 nhân viên",
      "industry": "Công nghệ thông tin"
    },
    "isSaved": false,
    "hasApplied": null,
    "postedAgo": "Đăng 2 ngày trước",
    
    // ====== NEW RECRUITMENT FORM FIELDS (4) ======
    "headcountMin": 2,
    "headcountMax": 3,
    "deadline": "2024-12-31",
    "benefits": [
      "Môi trường làm việc chuyên nghiệp",
      "Cơ hội thăng tiến",
      "Đào tạo kỹ năng"
    ]
  }
}
```

---

## 🗄️ Database Changes

### Projects Table - 3 new columns (all NULLABLE):
```sql
ALTER TABLE skillnest.projects
ADD COLUMN headcount_min INTEGER,
ADD COLUMN headcount_max INTEGER,
ADD COLUMN deadline DATE;
```

### New Table: project_benefits
```sql
CREATE TABLE skillnest.project_benefits (
    project_id BIGINT NOT NULL,
    benefit TEXT NOT NULL,
    CONSTRAINT fk_project_benefits FOREIGN KEY (project_id) 
        REFERENCES skillnest.projects(project_id) ON DELETE CASCADE
);
```

**Indexes created:**
- `idx_project_benefits_project_id` on `project_benefits(project_id)`
- `idx_projects_deadline` on `projects(deadline)`

---

## 📝 Files Modified/Created

### Modified (5 files):
1. **CreateProjectRequest.java** - Added 4 new optional fields
2. **UpdateProjectRequest.java** - Added 4 new optional fields
3. **Project.java** (Entity) - Added 3 columns + 1 ElementCollection
4. **ProjectDTO.java** - Added 4 new fields in response
5. **ProjectServiceImpl.java** - Updated create/update/convertToDTO methods

### Created (1 file):
1. **migration_add_recruitment_form_fields.sql** - Database migration script

---

## ✅ Verification Checklist

- [x] All existing fields preserved in DTOs and Entity
- [x] New fields are optional (nullable in database)
- [x] Old API requests still work without new fields
- [x] Migration script is backward compatible
- [x] Code compiles without errors
- [x] Service layer handles null values gracefully
- [x] Response format remains BaseResponse wrapper
- [x] Benefits stored as ElementCollection in separate table

---

## 🚀 Deployment Steps

### 1. Apply database migration:
```bash
psql -U postgres -d skillnest -f docs/database/migration_add_recruitment_form_fields.sql
```

### 2. Restart application

### 3. Test with old request (should still work):
```bash
POST /api/projects
{
  "title": "Test Job",
  "description": "Test description",
  "projectType": "FIXED_PRICE"
}
```

### 4. Test with new full request:
```bash
POST /api/projects
{
  "title": "Frontend Developer Intern",
  "description": "Full description...",
  "projectType": "FIXED_PRICE",
  "budgetMin": 3500000,
  "budgetMax": 5000000,
  "currency": "VND",
  "location": "Hà Nội",
  "employmentType": "Thực tập",
  "salaryUnit": "MONTH",
  "skillIds": [1, 2],
  "requirements": [
    "Sinh viên năm 3, 4",
    "Biết HTML, CSS"
  ],
  "headcountMin": 2,
  "headcountMax": 3,
  "deadline": "2024-12-31",
  "benefits": [
    "Môi trường chuyên nghiệp",
    "Đào tạo kỹ năng"
  ]
}
```

### 5. Verify response has all fields:
```bash
GET /api/projects/public/{projectId}
```

---

## 📊 Field Mapping to UI Form

| UI Form Field | Request Field | Type | Example |
|--------------|---------------|------|---------|
| Vị trí tuyển dụng | `title` | String | "Frontend Developer Intern" |
| Địa điểm | `location` | String | "Hà Nội" |
| Loại hình | `employmentType` | String | "Thực tập" |
| Mức lương | `budgetMin`, `budgetMax`, `salaryUnit` | Number, String | 3.5-5 triệu/tháng |
| Số lượng tuyển | `headcountMin`, `headcountMax` | Integer | "2-3" |
| Hạn nộp hồ sơ | `deadline` | LocalDate | "31/12/2024" |
| Mô tả công việc | `description` | String (textarea) | Full description |
| Yêu cầu ứng viên | `requirements` | List<String> | ["Requirement 1", "Requirement 2"] |
| Quyền lợi | `benefits` | List<String> | ["Benefit 1", "Benefit 2"] |
| Kỹ năng cần thiết | `skillIds` | Set<Long> | [1, 2, 3] |

---

## 🔒 Backward Compatibility Notes

1. ✅ **Old API clients** - Can continue using API without new fields
2. ✅ **Database** - New columns are NULLABLE, existing rows safe
3. ✅ **Request validation** - New fields are optional, old requests valid
4. ✅ **Response format** - Still uses BaseResponse, existing parsers work
5. ✅ **Service logic** - Handles null values with ArrayList defaults

---

## 💡 Business Rules

1. **Optional Fields:**
   - All 4 new fields are optional
   - System works without them (backward compatible)

2. **Headcount:**
   - Can specify only `headcountMin` (e.g., "Tuyển từ 2 người trở lên")
   - Can specify only `headcountMax` (e.g., "Tuyển tối đa 5 người")
   - Can specify both (e.g., "Tuyển 2-3 người")

3. **Deadline:**
   - Format: ISO date `yyyy-MM-dd` (e.g., "2024-12-31")
   - Can be null (no deadline)

4. **Benefits:**
   - List of strings, each string is one benefit
   - Can be empty list or null
   - Stored in separate table for efficient queries

---

## 🧪 Test Cases

### Test 1: Create project without new fields (Old API)
**Request:** Only required + old fields  
**Expected:** ✅ Success, new fields = null in DB

### Test 2: Create project with all fields (New API)
**Request:** All fields including new ones  
**Expected:** ✅ Success, all fields saved

### Test 3: Update project - add new fields
**Request:** Only new fields in update body  
**Expected:** ✅ Success, only new fields updated

### Test 4: Get project detail
**Request:** GET /api/projects/public/{id}  
**Expected:** ✅ Response includes all 25 fields (13 old + 8 previous + 4 new)

### Test 5: List projects
**Request:** GET /api/projects/public?page=0&size=10  
**Expected:** ✅ Page with all projects, each has full fields

---

## 📅 Summary

**Total Fields in Request DTO:** 17 (13 old + 4 new)  
**Total Fields in Response DTO:** 25 (13 old + 8 previous extension + 4 new)  
**Database Changes:** 3 new columns + 1 new table  
**Migration Script:** ✅ Backward compatible  
**Code Status:** ✅ Compiles without errors  

---

Generated: 2026-03-02

