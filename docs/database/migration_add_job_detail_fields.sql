-- Migration Script: Add Job Detail Fields to Projects
-- This script adds new columns and tables to support enhanced project/job detail UI
-- BACKWARD COMPATIBLE: All new columns are NULLABLE, existing data will not break

-- 1. Add new columns to projects table
ALTER TABLE skillnest.projects
ADD COLUMN IF NOT EXISTS location VARCHAR(200),
ADD COLUMN IF NOT EXISTS employment_type VARCHAR(50),
ADD COLUMN IF NOT EXISTS salary_unit VARCHAR(20);

-- 2. Create project_requirements table (for ElementCollection)
CREATE TABLE IF NOT EXISTS skillnest.project_requirements (
    project_id BIGINT NOT NULL,
    requirement TEXT NOT NULL,
    CONSTRAINT fk_project_requirements FOREIGN KEY (project_id)
        REFERENCES skillnest.projects(project_id) ON DELETE CASCADE
);

-- 3. Create company_info table
CREATE TABLE IF NOT EXISTS skillnest.company_info (
    company_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    location VARCHAR(200),
    size VARCHAR(100),
    industry VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_company_user FOREIGN KEY (user_id)
        REFERENCES skillnest.users(user_id) ON DELETE CASCADE
);

-- 4. Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_project_requirements_project_id
    ON skillnest.project_requirements(project_id);

CREATE INDEX IF NOT EXISTS idx_company_info_user_id
    ON skillnest.company_info(user_id);

CREATE INDEX IF NOT EXISTS idx_projects_location
    ON skillnest.projects(location);

CREATE INDEX IF NOT EXISTS idx_projects_employment_type
    ON skillnest.projects(employment_type);

-- 5. Add comments for documentation
COMMENT ON COLUMN skillnest.projects.location IS 'Job location (e.g., "Hà Nội", "Remote")';
COMMENT ON COLUMN skillnest.projects.employment_type IS 'Employment type (e.g., "Thực tập", "Full-time", "Part-time")';
COMMENT ON COLUMN skillnest.projects.salary_unit IS 'Salary time unit: "MONTH" or "YEAR"';
COMMENT ON TABLE skillnest.project_requirements IS 'List of requirements for a project/job';
COMMENT ON TABLE skillnest.company_info IS 'Company information for client users';

-- Verification queries (optional)
-- SELECT column_name, data_type, is_nullable
-- FROM information_schema.columns
-- WHERE table_schema = 'skillnest' AND table_name = 'projects'
-- ORDER BY ordinal_position;

