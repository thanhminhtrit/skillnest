-- Profile Management Migration Script
-- This script creates tables for user profiles and job applications

-- 1. Create user_profiles table
CREATE TABLE IF NOT EXISTS skillnest.user_profiles (
    profile_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    university VARCHAR(200),
    major VARCHAR(200),
    year_of_study VARCHAR(50),
    gpa DECIMAL(3,2),
    bio TEXT,
    address VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profile FOREIGN KEY (user_id) REFERENCES skillnest.users(user_id) ON DELETE CASCADE
);

-- 2. Create profile_skills table (many skills per profile)
CREATE TABLE IF NOT EXISTS skillnest.profile_skills (
    profile_id BIGINT NOT NULL,
    skill VARCHAR(100) NOT NULL,
    CONSTRAINT fk_profile_skills FOREIGN KEY (profile_id) REFERENCES skillnest.user_profiles(profile_id) ON DELETE CASCADE
);

-- 3. Create profile_interests table
CREATE TABLE IF NOT EXISTS skillnest.profile_interests (
    profile_id BIGINT NOT NULL,
    interest VARCHAR(100) NOT NULL,
    CONSTRAINT fk_profile_interests FOREIGN KEY (profile_id) REFERENCES skillnest.user_profiles(profile_id) ON DELETE CASCADE
);

-- 4. Create profile_preferred_locations table
CREATE TABLE IF NOT EXISTS skillnest.profile_preferred_locations (
    profile_id BIGINT NOT NULL,
    location VARCHAR(100) NOT NULL,
    CONSTRAINT fk_profile_locations FOREIGN KEY (profile_id) REFERENCES skillnest.user_profiles(profile_id) ON DELETE CASCADE
);

-- 5. Create profile_preferred_job_types table
CREATE TABLE IF NOT EXISTS skillnest.profile_preferred_job_types (
    profile_id BIGINT NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_profile_job_types FOREIGN KEY (profile_id) REFERENCES skillnest.user_profiles(profile_id) ON DELETE CASCADE
);

-- 6. Create application_status enum type (if not exists)
DO $$ BEGIN
    CREATE TYPE skillnest.application_status AS ENUM ('PENDING', 'INTERVIEW', 'REJECTED', 'ACCEPTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 7. Create job_applications table
CREATE TABLE IF NOT EXISTS skillnest.job_applications (
    application_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    job_title VARCHAR(200) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    status skillnest.application_status NOT NULL DEFAULT 'PENDING',
    applied_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_application FOREIGN KEY (user_id) REFERENCES skillnest.users(user_id) ON DELETE CASCADE
);

-- 8. Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_profile_user_id ON skillnest.user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_job_applications_user_id ON skillnest.job_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_job_applications_status ON skillnest.job_applications(status);
CREATE INDEX IF NOT EXISTS idx_job_applications_applied_date ON skillnest.job_applications(applied_date DESC);

-- 9. Add comments for documentation
COMMENT ON TABLE skillnest.user_profiles IS 'Extended profile information for users';
COMMENT ON TABLE skillnest.job_applications IS 'Job application history for students';
COMMENT ON COLUMN skillnest.user_profiles.gpa IS 'GPA on scale of 0.0 to 4.0';
COMMENT ON COLUMN skillnest.job_applications.status IS 'Current status of the job application';

