# SavedProject API Documentation

## Overview
The SavedProject feature allows students to bookmark/save projects for later viewing. This provides a convenient way for users to keep track of projects they're interested in.

## Endpoints

### 1. Save a Project
**POST** `/api/saved-projects/{projectId}`

Bookmark a project for the authenticated user.

#### Request
- **Method**: POST
- **URL**: `/api/saved-projects/{projectId}`
- **Authentication**: Required (Bearer Token)
- **Path Parameters**:
  - `projectId` (Long) - ID of the project to save

#### Response
**Success (201 Created)**
```json
{
  "statusCode": 201,
  "message": "Project saved successfully",
  "data": "Project saved successfully"
}
```

**If Already Saved (201 Created)**
```json
{
  "statusCode": 201,
  "message": "Project saved successfully",
  "data": "Project already saved"
}
```

**Error (404 Not Found)**
```json
{
  "statusCode": 404,
  "message": "Project not found"
}
```

#### Example cURL
```bash
curl -X POST "http://localhost:8080/api/saved-projects/123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Unsave a Project
**DELETE** `/api/saved-projects/{projectId}`

Remove a project from the authenticated user's saved projects.

#### Request
- **Method**: DELETE
- **URL**: `/api/saved-projects/{projectId}`
- **Authentication**: Required (Bearer Token)
- **Path Parameters**:
  - `projectId` (Long) - ID of the project to unsave

#### Response
**Success (200 OK)**
```json
{
  "statusCode": 200,
  "message": "Project unsaved successfully",
  "data": "Project unsaved successfully"
}
```

**Error (404 Not Found)**
```json
{
  "statusCode": 404,
  "message": "Saved project not found"
}
```

#### Example cURL
```bash
curl -X DELETE "http://localhost:8080/api/saved-projects/123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Get Saved Projects
**GET** `/api/saved-projects`

Retrieve all projects saved/bookmarked by the authenticated user with pagination.

#### Request
- **Method**: GET
- **URL**: `/api/saved-projects`
- **Authentication**: Required (Bearer Token)
- **Query Parameters**:
  - `page` (int, optional) - Page number (0-indexed, default: 0)
  - `size` (int, optional) - Number of items per page (default: 10, max: 100)

#### Response
**Success (200 OK)**
```json
{
  "statusCode": 200,
  "message": "Saved projects retrieved successfully",
  "data": {
    "content": [
      {
        "projectId": 123,
        "clientId": 456,
        "clientName": "Alice Johnson",
        "title": "Build a Mobile App",
        "description": "We need a mobile app for...",
        "projectType": "FIXED_PRICE",
        "budgetMin": 5000000.00,
        "budgetMax": 10000000.00,
        "currency": "VND",
        "status": "OPEN",
        "skills": ["Java", "React Native", "Firebase"],
        "location": "Ho Chi Minh City",
        "employmentType": "Part-time",
        "salaryUnit": "MONTH",
        "requirements": [
          "3+ years experience",
          "Mobile development expertise"
        ],
        "company": {
          "companyId": 789,
          "name": "Tech Company ABC",
          "location": "Ho Chi Minh City",
          "industry": "Technology",
          "companySize": "50-100"
        },
        "isSaved": true,
        "hasApplied": false,
        "postedAgo": "Posted 2 days ago",
        "headcountMin": 1,
        "headcountMax": 2,
        "deadline": "2026-04-15",
        "benefits": [
          "Flexible hours",
          "Remote work"
        ],
        "createdAt": "2026-03-05T10:30:00",
        "updatedAt": "2026-03-05T10:30:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "size": 10,
    "number": 0,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "numberOfElements": 1,
    "first": true,
    "empty": false
  }
}
```

#### Example cURL
```bash
# Get first page (10 items)
curl -X GET "http://localhost:8080/api/saved-projects?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get second page (20 items per page)
curl -X GET "http://localhost:8080/api/saved-projects?page=1&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Integration with Existing APIs

### Project Detail API Enhancement
The existing project detail endpoint now includes the `isSaved` field to indicate if the current user has saved the project:

**GET** `/api/projects/{projectId}`

Response includes:
```json
{
  "statusCode": 200,
  "message": "Project retrieved successfully",
  "data": {
    "projectId": 123,
    ...
    "isSaved": true,  // <-- New field
    "hasApplied": false,
    ...
  }
}
```

---

## Database Schema

### saved_projects Table
```sql
CREATE TABLE saved_projects (
    saved_project_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    project_id BIGINT NOT NULL REFERENCES projects(project_id),
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_project UNIQUE (user_id, project_id)
);

CREATE INDEX idx_saved_projects_user ON saved_projects(user_id);
CREATE INDEX idx_saved_projects_project ON saved_projects(project_id);
```

---

## Business Rules

1. **Uniqueness**: A user can only save a project once. Attempting to save again returns "Project already saved"
2. **Authentication**: All endpoints require authentication
3. **Project Existence**: Project must exist to be saved
4. **Deletion**: Only the user who saved a project can unsave it
5. **Pagination**: Saved projects are returned in paginated format
6. **Order**: Saved projects are returned in the order they were saved (most recent first by default)

---

## Error Handling

| Error | Status Code | Description |
|-------|-------------|-------------|
| Project not found | 404 | The specified project doesn't exist |
| Saved project not found | 404 | User hasn't saved this project |
| User not found | 404 | Authenticated user doesn't exist (shouldn't happen) |
| Unauthorized | 401 | Missing or invalid JWT token |

---

## Use Cases

### For Students
1. **Browse and Save**: Browse open projects and save interesting ones for later
2. **Review Later**: Access saved projects list to review and apply
3. **Track Opportunities**: Keep track of potential job opportunities
4. **Organize Workflow**: Save projects while researching, apply after preparation

### Example Workflow
```
1. Student browses projects: GET /api/projects/public
2. Student sees interesting project: GET /api/projects/123
3. Student saves for later: POST /api/saved-projects/123
4. Later, student reviews saved projects: GET /api/saved-projects
5. Student decides to apply: POST /api/proposals
6. After applying, student unsaves: DELETE /api/saved-projects/123
   (Or keeps it saved for reference)
```

---

## Testing

### Manual Testing Scenario

1. **Save a project**:
   ```bash
   POST /api/saved-projects/1
   ```

2. **Verify it's saved**:
   ```bash
   GET /api/saved-projects
   # Should see project in the list
   
   GET /api/projects/1
   # isSaved should be true
   ```

3. **Try to save again**:
   ```bash
   POST /api/saved-projects/1
   # Should return "Project already saved"
   ```

4. **Unsave the project**:
   ```bash
   DELETE /api/saved-projects/1
   ```

5. **Verify it's unsaved**:
   ```bash
   GET /api/saved-projects
   # Project should not be in the list
   
   GET /api/projects/1
   # isSaved should be false
   ```

---

## Swagger/OpenAPI

All SavedProject endpoints are documented in the Swagger UI:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

Navigate to the "Saved Projects" tag in Swagger UI to interact with these endpoints.
