-- =========================================================================
-- V1: Initial Schema for Student Management System (SMS)
-- Engine: PostgreSQL / PostgreSQL-compatible
-- Standards: UUID primary keys, foreign key constraints, indexes, audit trail
-- =========================================================================

-- 1. USERS
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- 2. DEPARTMENTS
CREATE TABLE departments (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. ACADEMIC YEARS
CREATE TABLE academic_years (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. SEMESTERS
CREATE TABLE semesters (
    id VARCHAR(36) PRIMARY KEY,
    academic_year_id VARCHAR(36) NOT NULL,
    semester_number INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_semesters_ay FOREIGN KEY (academic_year_id) REFERENCES academic_years(id) ON DELETE CASCADE,
    CONSTRAINT uq_ay_semester UNIQUE (academic_year_id, semester_number)
);

-- 5. TEACHER PROFILES
CREATE TABLE teacher_profiles (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20),
    department_id VARCHAR(36) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    qualification VARCHAR(100),
    profile_photo_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_teacher_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
);

-- 6. STUDENT PROFILES
CREATE TABLE student_profiles (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    registration_number VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20),
    department_id VARCHAR(36) NOT NULL,
    course VARCHAR(100) NOT NULL,
    current_academic_year_id VARCHAR(36) NOT NULL,
    current_semester_id VARCHAR(36) NOT NULL,
    profile_photo_url TEXT,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_student_ay FOREIGN KEY (current_academic_year_id) REFERENCES academic_years(id) ON DELETE RESTRICT,
    CONSTRAINT fk_student_sem FOREIGN KEY (current_semester_id) REFERENCES semesters(id) ON DELETE RESTRICT
);

CREATE INDEX idx_student_roll ON student_profiles(roll_number);

-- 7. CLASSES
CREATE TABLE classes (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department_id VARCHAR(36) NOT NULL,
    academic_year_id VARCHAR(36) NOT NULL,
    semester_id VARCHAR(36) NOT NULL,
    section VARCHAR(10) NOT NULL,
    capacity INT NOT NULL DEFAULT 60,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_class_ay FOREIGN KEY (academic_year_id) REFERENCES academic_years(id) ON DELETE RESTRICT,
    CONSTRAINT fk_class_sem FOREIGN KEY (semester_id) REFERENCES semesters(id) ON DELETE RESTRICT,
    CONSTRAINT uq_class_cohort UNIQUE (department_id, academic_year_id, semester_id, section)
);

-- 8. CLASS TEACHERS (Teacher Ownership)
CREATE TABLE class_teachers (
    id VARCHAR(36) PRIMARY KEY,
    class_id VARCHAR(36) NOT NULL,
    teacher_id VARCHAR(36) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ct_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_ct_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uq_class_teacher UNIQUE (class_id, teacher_id)
);

-- 9. SUBJECTS
CREATE TABLE subjects (
    id VARCHAR(36) PRIMARY KEY,
    department_id VARCHAR(36) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    credits NUMERIC(4, 1) NOT NULL DEFAULT 3.0,
    is_elective BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subject_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
);

-- 10. CLASS SUBJECTS (Subject allocation & assigned teacher)
CREATE TABLE class_subjects (
    id VARCHAR(36) PRIMARY KEY,
    class_id VARCHAR(36) NOT NULL,
    subject_id VARCHAR(36) NOT NULL,
    teacher_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cs_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_cs_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    CONSTRAINT fk_cs_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_profiles(id) ON DELETE RESTRICT,
    CONSTRAINT uq_class_subject UNIQUE (class_id, subject_id)
);

-- 11. ENROLLMENTS
CREATE TABLE enrollments (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL,
    class_id VARCHAR(36) NOT NULL,
    academic_year_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enr_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_enr_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_enr_ay FOREIGN KEY (academic_year_id) REFERENCES academic_years(id) ON DELETE RESTRICT,
    CONSTRAINT uq_student_year UNIQUE (student_id, academic_year_id)
);

CREATE INDEX idx_enrollment_class_student ON enrollments(class_id, student_id);

-- 12. EXAMS
CREATE TABLE exams (
    id VARCHAR(36) PRIMARY KEY,
    academic_year_id VARCHAR(36) NOT NULL,
    semester_id VARCHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    exam_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exam_ay FOREIGN KEY (academic_year_id) REFERENCES academic_years(id) ON DELETE RESTRICT,
    CONSTRAINT fk_exam_sem FOREIGN KEY (semester_id) REFERENCES semesters(id) ON DELETE RESTRICT
);

-- 13. ASSESSMENTS
CREATE TABLE assessments (
    id VARCHAR(36) PRIMARY KEY,
    exam_id VARCHAR(36) NOT NULL,
    class_id VARCHAR(36) NOT NULL,
    subject_id VARCHAR(36) NOT NULL,
    created_by_teacher_id VARCHAR(36) NOT NULL,
    maximum_marks NUMERIC(6, 2) NOT NULL,
    passing_marks NUMERIC(6, 2) NOT NULL,
    assessment_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    published_by VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ass_exam FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    CONSTRAINT fk_ass_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ass_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ass_teacher FOREIGN KEY (created_by_teacher_id) REFERENCES teacher_profiles(id) ON DELETE RESTRICT,
    CONSTRAINT uq_assessment_cohort UNIQUE (exam_id, class_id, subject_id)
);

-- 14. MARKS
CREATE TABLE marks (
    id VARCHAR(36) PRIMARY KEY,
    assessment_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    marks_obtained NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    is_absent BOOLEAN NOT NULL DEFAULT FALSE,
    remarks VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_marks_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE,
    CONSTRAINT fk_marks_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uq_assessment_student UNIQUE (assessment_id, student_id)
);

CREATE INDEX idx_marks_assessment_student ON marks(assessment_id, student_id);

-- 15. GRADE SCALES (Configurable)
CREATE TABLE grade_scales (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    min_percentage NUMERIC(5, 2) NOT NULL,
    max_percentage NUMERIC(5, 2) NOT NULL,
    grade_letter VARCHAR(5) NOT NULL,
    grade_point NUMERIC(4, 2) NOT NULL,
    description VARCHAR(100),
    is_passing BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 16. ATTENDANCE SESSIONS
CREATE TABLE attendance_sessions (
    id VARCHAR(36) PRIMARY KEY,
    class_id VARCHAR(36) NOT NULL,
    subject_id VARCHAR(36) NOT NULL,
    teacher_id VARCHAR(36) NOT NULL,
    session_date DATE NOT NULL,
    slot_number INT,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_att_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_att_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT,
    CONSTRAINT fk_att_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_profiles(id) ON DELETE RESTRICT,
    CONSTRAINT uq_attendance_session UNIQUE (class_id, subject_id, session_date, slot_number)
);

CREATE INDEX idx_attendance_session_date ON attendance_sessions(class_id, session_date);

-- 17. ATTENDANCE RECORDS
CREATE TABLE attendance_records (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ar_session FOREIGN KEY (session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_ar_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uq_session_student UNIQUE (session_id, student_id)
);

CREATE INDEX idx_attendance_student_session ON attendance_records(student_id, session_id);

-- 18. TIMETABLE SLOTS
CREATE TABLE timetable_slots (
    id VARCHAR(36) PRIMARY KEY,
    class_id VARCHAR(36) NOT NULL,
    subject_id VARCHAR(36) NOT NULL,
    teacher_id VARCHAR(36) NOT NULL,
    weekday VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    classroom VARCHAR(50) NOT NULL,
    academic_year_id VARCHAR(36) NOT NULL,
    semester_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tt_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tt_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_profiles(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tt_ay FOREIGN KEY (academic_year_id) REFERENCES academic_years(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tt_sem FOREIGN KEY (semester_id) REFERENCES semesters(id) ON DELETE RESTRICT
);

CREATE INDEX idx_timetable_teacher_slot ON timetable_slots(teacher_id, weekday);
CREATE INDEX idx_timetable_class_slot ON timetable_slots(class_id, weekday);
CREATE INDEX idx_timetable_room_slot ON timetable_slots(classroom, weekday);

-- 19. ANNOUNCEMENTS
CREATE TABLE announcements (
    id VARCHAR(36) PRIMARY KEY,
    author_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    target_class_id VARCHAR(36),
    target_role VARCHAR(20) DEFAULT 'ALL',
    publish_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ann_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ann_class FOREIGN KEY (target_class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- 20. NOTIFICATIONS
CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    link VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 21. AUDIT LOGS
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    actor_id VARCHAR(36),
    ip_address VARCHAR(45),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    before_state TEXT,
    after_state TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_actor_time ON audit_logs(actor_id, timestamp DESC);

