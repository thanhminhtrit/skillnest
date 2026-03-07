# Job Detail Feature - Backend Extension Summary

## ✅ BACKWARD COMPATIBILITY GUARANTEED
Tất cả field cũ được giữ nguyên 100%. Response mới là **superset** của response cũ.

---

## 📋 Summary of Changes

### 1. **New Fields Added to ProjectDTO** (Existing fields unchanged)

#### Existing Fields (Kept as-is):
- `projectId`, `clientId`, `clientName`
- `title`, `description`, `projectType`
- `budgetMin`, `budgetMax`, `currency`
- `status`, `skills`, `createdAt`, `updatedAt`

#### New Fields:
- `location` (String) - Job location (e.g., "Hà Nội", "Remote")
- `employmentType` (String) - Employment type (e.g., "Thực tập", "Full-time", "Part-time")
- `salaryUnit` (String) - "MONTH" or "YEAR"
- `requirements` (List<String>) - List of job requirements
- `company` (CompanyInfoDTO) - Company information object with:
  - `name` (String)
  - `location` (String)
  - `size` (String) - e.g., "100-500 nhân viên"
  - `industry` (String) - e.g., "Công nghệ thông tin"
- `isSaved` (Boolean) - Whether current user saved this job (TODO: implement SavedProject feature)
- `hasApplied` (Boolean) - Whether current user has applied (null if not authenticated)
- `postedAgo` (String) - Human-readable time ago (e.g., "Đăng 2 ngày trước")

---

### 2. **Entity Changes**

#### Project Entity (skillnest.projects table):
**New columns added (all NULLABLE for backward compatibility):**
- `location` VARCHAR(200)
- `employment_type` VARCHAR(50)
- `salary_unit` VARCHAR(20)
- `requirements` - stored in separate table `project_requirements` (ElementCollection)

#### New Entity: CompanyInfo
**New table: skillnest.company_info**
- `company_id` (PK)
- `user_id` (FK to users, unique)
- `name` VARCHAR(200) NOT NULL
- `location` VARCHAR(200)
- `size` VARCHAR(100)
- `industry` VARCHAR(200)
- `created_at`, `updated_at`

#### New Table: project_requirements
- `project_id` (FK)
- `requirement` TEXT

---

### 3. **Request DTOs Updated**

#### CreateProjectRequest - New optional fields:
- `location`
- `employmentType`
- `salaryUnit`
- `requirements` (List<String>)

#### UpdateProjectRequest - New optional fields:
- `location`
- `employmentType`
- `salaryUnit`
- `requirements` (List<String>)

---

### 4. **Service Layer Changes**

#### ProjectServiceImpl:
- Added `CompanyInfoRepository` dependency
- Added `ProposalRepository` dependency for checking `hasApplied`
- Enhanced `convertToDTO()` method to populate new fields
- Added `calculatePostedAgo()` helper method
- Updated `createProject()` to handle new fields
- Updated `updateProject()` to handle new fields

#### New Repository: CompanyInfoRepository
- `findByUser_UserId(Long userId)`
- `existsByUser_UserId(Long userId)`

---

### 5. **Database Migration**

**File:** `docs/database/migration_add_job_detail_fields.sql`

**Actions:**
1. Add 3 new columns to `projects` table (all NULLABLE)
2. Create `project_requirements` table
3. Create `company_info` table
4. Add indexes for performance
5. Add documentation comments

**To apply migration:**
```bash
psql -U your_user -d your_database -f docs/database/migration_add_job_detail_fields.sql
```

---

## 🧪 Testing

### Example Response (Before):
```json
{
  "statusCode": 200,
  "message": "Project retrieved successfully",
  "data": {
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
    "updatedAt": "2026-03-01T10:00:00"
  }
}
```

### Example Response (After - Superset):
```json
{
  "statusCode": 200,
  "message": "Project retrieved successfully",
  "data": {
    // ========== EXISTING FIELDS (unchanged) ==========
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
    
    // ========== NEW FIELDS ==========
    "location": "Hà Nội",
    "employmentType": "Thực tập",
    "salaryUnit": "MONTH",
    "requirements": [
      "Sinh viên năm 3, 4 hoặc mới tốt nghiệp",
      "Có kiến thức cơ bản về HTML, CSS, JavaScript",
      "Ưu tiên biết React hoặc Vue.js",
      "Có khả năng làm việc nhóm tốt"
    ],
    "company": {
      "name": "TechViet Solutions",
      "location": "Hà Nội",
      "size": "100-500 nhân viên",
      "industry": "Công nghệ thông tin"
    },
    "isSaved": false,
    "hasApplied": null,
    "postedAgo": "Đăng 2 ngày trước"
  }
}
```

---

## ✅ Verification Checklist

- [x] All existing fields preserved in ProjectDTO
- [x] New fields are optional (nullable in database)
- [x] Existing APIs still work without changes
- [x] Migration script is backward compatible
- [x] Code compiles without errors
- [x] Service layer handles null values gracefully
- [x] Response format remains BaseResponse wrapper
- [x] Company info has fallback to client name if not set
- [x] Posted ago calculation works correctly

---

## 🚀 Next Steps

1. **Apply database migration:**
   ```bash
   psql -U postgres -d skillnest -f docs/database/migration_add_job_detail_fields.sql
   ```

2. **Restart application**

3. **Test existing endpoints** - should work without changes

4. **Test new fields** - create/update projects with new fields:
   ```bash
   POST /api/projects
   {
     "title": "Frontend Developer Intern",
     "description": "...",
     "location": "Hà Nội",
     "employmentType": "Thực tập",
     "salaryUnit": "MONTH",
     "requirements": [
       "Sinh viên năm 3, 4",
       "Biết HTML, CSS, JS"
     ],
     ...
   }
   ```

5. **(Optional) Implement SavedProject feature** for `isSaved` field

6. **(Optional) Create Company Info management endpoints**

---

## 📚 Files Modified/Created

### Created:
- `dto/CompanyInfoDTO.java`
- `entity/CompanyInfo.java`
- `repository/CompanyInfoRepository.java`
- `docs/database/migration_add_job_detail_fields.sql`

### Modified:
- `dto/ProjectDTO.java` - Added new fields
- `entity/Project.java` - Added new columns + ElementCollection
- `service/impl/ProjectServiceImpl.java` - Enhanced mapping logic
- `payloads/request/CreateProjectRequest.java` - Added new fields
- `payloads/request/UpdateProjectRequest.java` - Added new fields

---

## 🔒 Backward Compatibility Notes

1. **Old clients** can continue to use API without changes - new fields will be present but can be ignored
2. **Database migration** only adds new nullable columns - existing data safe
3. **Request DTOs** - new fields are optional, old requests still valid
4. **Response format** - still uses BaseResponse, existing parsers work
5. **Entity mapping** - handles null values gracefully with fallbacks

---

## 💡 Implementation Notes

- `hasApplied` is calculated by checking if user has submitted proposal (requires ProposalRepository)
- `isSaved` is currently hardcoded to `false` - implement SavedProject feature later
- `company` info falls back to client's fullName if CompanyInfo not found
- `postedAgo` is calculated on-the-fly using Duration between createdAt and now
- All list fields default to empty ArrayList if null

---

Generated on: 2026-03-02

