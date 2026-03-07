-- Seed Data for Profile Testing
-- This script provides sample data to test profile APIs

-- IMPORTANT: Make sure you have at least one user in the users table first
-- You can create a test user through the registration API or manually

-- Example: Assuming we have users with IDs 1, 2, 3 already created

-- 1. Insert sample user profiles
INSERT INTO skillnest.user_profiles (user_id, university, major, year_of_study, gpa, bio, address)
VALUES
    (1, 'Đại học Bách Khoa Hà Nội', 'Công nghệ Thông tin', 'Năm 3', 3.5,
     'Sinh viên năng động, đam mê lập trình web và công nghệ AI. Có kinh nghiệm với React và Python.',
     'Hà Nội'),
    (2, 'Đại học FPT', 'Software Engineering', 'Năm 4', 3.8,
     'Passionate about full-stack development and machine learning. Looking for internship opportunities.',
     'Hồ Chí Minh')
ON CONFLICT (user_id) DO NOTHING;

-- 2. Insert sample skills for profiles
INSERT INTO skillnest.profile_skills (profile_id, skill)
VALUES
    (1, 'React'),
    (1, 'JavaScript'),
    (1, 'Python'),
    (1, 'HTML'),
    (1, 'CSS'),
    (2, 'Node.js'),
    (2, 'Java'),
    (2, 'Spring Boot'),
    (2, 'PostgreSQL'),
    (2, 'Docker');

-- 3. Insert sample interests
INSERT INTO skillnest.profile_interests (profile_id, interest)
VALUES
    (1, 'Web Development'),
    (1, 'AI/ML'),
    (1, 'Mobile Development'),
    (2, 'Backend Development'),
    (2, 'Cloud Computing'),
    (2, 'DevOps');

-- 4. Insert sample preferred locations
INSERT INTO skillnest.profile_preferred_locations (profile_id, location)
VALUES
    (1, 'Hà Nội'),
    (1, 'Remote'),
    (2, 'Hồ Chí Minh'),
    (2, 'Đà Nẵng'),
    (2, 'Remote');

-- 5. Insert sample preferred job types
INSERT INTO skillnest.profile_preferred_job_types (profile_id, job_type)
VALUES
    (1, 'Thực tập'),
    (1, 'Part-time'),
    (2, 'Full-time'),
    (2, 'Thực tập');

-- 6. Insert sample job applications
INSERT INTO skillnest.job_applications (user_id, job_id, job_title, company_name, status, applied_date)
VALUES
    (1, 101, 'Frontend Developer Intern', 'TechViet Solutions', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (1, 102, 'UI/UX Design Intern', 'Creative Studio VN', 'INTERVIEW', CURRENT_TIMESTAMP - INTERVAL '1 week'),
    (1, 103, 'Data Analyst Intern', 'VinTech Analytics', 'REJECTED', CURRENT_TIMESTAMP - INTERVAL '2 weeks'),
    (2, 201, 'Backend Developer', 'FPT Software', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (2, 202, 'Full-stack Developer Intern', 'Sendo', 'INTERVIEW', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    (2, 203, 'Java Developer', 'Viettel Solutions', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '1 week');

-- Verification queries (optional, for testing)
-- SELECT * FROM skillnest.user_profiles;
-- SELECT * FROM skillnest.profile_skills;
-- SELECT * FROM skillnest.job_applications ORDER BY applied_date DESC;

