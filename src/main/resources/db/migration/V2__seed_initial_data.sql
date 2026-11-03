-- =========================================================================
-- V2: Seed Baseline Academic Organization and Grade Scales
-- =========================================================================

-- Baseline Department
INSERT INTO departments (id, code, name, description)
VALUES ('dept-cse-001', 'CSE', 'Computer Science and Engineering', 'Department of Computer Science and Engineering');

-- Baseline Academic Year
INSERT INTO academic_years (id, name, start_date, end_date, is_current)
VALUES ('ay-2025-2026', '2025-2026', '2025-08-01', '2026-06-30', TRUE);

-- Baseline Semester
INSERT INTO semesters (id, academic_year_id, semester_number, name, start_date, end_date, is_active)
VALUES ('sem-fall-2025', 'ay-2025-2026', 5, 'Semester 5 (Fall 2025)', '2025-08-01', '2025-12-20', TRUE);

-- Baseline Class
INSERT INTO classes (id, name, department_id, academic_year_id, semester_id, section, capacity)
VALUES ('class-cse-5a', 'CSE 5th Sem - Section A', 'dept-cse-001', 'ay-2025-2026', 'sem-fall-2025', 'A', 60);

-- Baseline Subjects
INSERT INTO subjects (id, department_id, code, name, credits, is_elective)
VALUES 
('subj-cs501', 'dept-cse-001', 'CS501', 'Database Management Systems', 4.0, FALSE),
('subj-cs502', 'dept-cse-001', 'CS502', 'Operating Systems & Concurrency', 4.0, FALSE),
('subj-cs503', 'dept-cse-001', 'CS503', 'Computer Networks & Security', 3.0, FALSE),
('subj-cs504', 'dept-cse-001', 'CS504', 'Software Engineering Principles', 3.0, FALSE);

-- Baseline Standard 10-Point Grade Scales (Configurable)
INSERT INTO grade_scales (id, name, min_percentage, max_percentage, grade_letter, grade_point, description, is_passing)
VALUES 
('gs-01', 'Standard Grading Scale', 90.00, 100.00, 'A+', 10.00, 'Outstanding', TRUE),
('gs-02', 'Standard Grading Scale', 80.00, 89.99,  'A',  9.00,  'Excellent', TRUE),
('gs-03', 'Standard Grading Scale', 70.00, 79.99,  'B',  8.00,  'Very Good', TRUE),
('gs-04', 'Standard Grading Scale', 60.00, 69.99,  'C',  7.00,  'Good', TRUE),
('gs-05', 'Standard Grading Scale', 50.00, 59.99,  'D',  6.00,  'Satisfactory', TRUE),
('gs-06', 'Standard Grading Scale', 40.00, 49.99,  'E',  5.00,  'Pass', TRUE),
('gs-07', 'Standard Grading Scale', 0.00,  39.99,  'F',  0.00,  'Fail', FALSE);

