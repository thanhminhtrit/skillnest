-- Migration Script: Add Recruitment Form Fields to Projects
-- This script adds new columns and table to support full recruitment form
-- BACKWARD COMPATIBLE: All new columns are NULLABLE, existing data will not break

-- 1. Add new columns to projects table
ALTER TABLE skillnest.projects
ADD COLUMN IF NOT EXISTS headcount_min INTEGER,
ADD COLUMN IF NOT EXISTS headcount_max INTEGER,
ADD COLUMN IF NOT EXISTS deadline DATE;

-- 2. Create project_benefits table (for ElementCollection)
CREATE TABLE IF NOT EXISTS skillnest.project_benefits (
    project_id BIGINT NOT NULL,
    benefit TEXT NOT NULL,
    CONSTRAINT fk_project_benefits FOREIGN KEY (project_id)
        REFERENCES skillnest.projects(project_id) ON DELETE CASCADE
);

-- 3. Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_project_benefits_project_id
    ON skillnest.project_benefits(project_id);

CREATE INDEX IF NOT EXISTS idx_projects_deadline
    ON skillnest.projects(deadline);

-- 4. Add comments for documentation
COMMENT ON COLUMN skillnest.projects.headcount_min IS 'Số lượng tuyển dụng tối thiểu';
COMMENT ON COLUMN skillnest.projects.headcount_max IS 'Số lượng tuyển dụng tối đa (e.g., 2-3 người)';
COMMENT ON COLUMN skillnest.projects.deadline IS 'Hạn nộp hồ sơ (e.g., 31/12/2024)';
COMMENT ON TABLE skillnest.project_benefits IS 'Danh sách quyền lợi của công việc/dự án';

-- Verification queries (optional)
-- SELECT column_name, data_type, is_nullable
-- FROM information_schema.columns
-- WHERE table_schema = 'skillnest' AND table_name = 'projects'
-- AND column_name IN ('headcount_min', 'headcount_max', 'deadline')
-- ORDER BY ordinal_position;

