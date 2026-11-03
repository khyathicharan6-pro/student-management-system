# Student Management System (SMS) - API Architecture & Endpoint Specification

**Document Version:** 1.0.0  
**Phase:** Phase 0 (Planning & Architecture)  
**Standard:** OpenAPI 3.1 / RESTful JSON  
**Base URL:** `/api/v1`  

---

## 1. Response Envelope Standard

All API endpoints return responses encapsulated in a standardized JSON envelope to guarantee predictable parsing across the Next.js frontend client.

### Success Response Format
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully.",
  "meta": {
    "page": 1,
    "page_size": 20,
    "total_records": 142,
    "total_pages": 8
  }
}
```

### Error Response Format
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "OWNERSHIP_VIOLATION",
    "message": "Teacher does not have permission to modify records for this unassigned class.",
    "details": [
      {
        "field": "class_id",
        "issue": "Class 3b2a1a8c-9411 is not assigned to Teacher 9f4e2c10."
      }
    ]
  }
}
```

---

## 2. Global HTTP Status Codes

| Code | Status | Meaning |
| :--- | :--- | :--- |
| `200` | OK | Successful GET, PUT, or PATCH operation. |
| `201` | Created | Successful resource creation (POST). |
| `204` | No Content | Successful deletion or empty action. |
| `400` | Bad Request | Validation error, business logic conflict, or malformed body. |
| `401` | Unauthorized | Missing, expired, or invalid JWT authentication token. |
| `403` | Forbidden | Authenticated, but user lacks role or resource ownership. |
| `404` | Not Found | Requested entity UUID does not exist. |
| `409` | Conflict | Duplicate entry (e.g., student attendance already taken, timetable conflict). |
| `422` | Unprocessable Entity | Pydantic schema validation failure. |
| `500` | Internal Server Error | Unhandled server exception with tracked trace ID. |

---

## 3. Comprehensive Route Groups & Endpoints

### 3.1 `/api/v1/auth` (Authentication & Session)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | Public | Authenticate with username/email & password; returns access & refresh tokens with user role. |
| `POST` | `/auth/refresh` | Public | Exchange valid refresh token for a fresh short-lived access token. |
| `POST` | `/auth/logout` | Authenticated | Invalidate refresh token session. |
| `GET` | `/auth/me` | Authenticated | Return authenticated user identity, role, and profile details. |
| `POST` | `/auth/change-password` | Authenticated | Change current user password (verifies previous password). |

---

### 3.2 `/api/v1/teachers` (Teacher Management & Self)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/teachers/me/dashboard` | Teacher | Get consolidated dashboard metrics (assigned classes, student count, today's schedule, attendance rate). |
| `GET` | `/teachers/me/profile` | Teacher | Get teacher's profile, qualifications, assigned departments. |
| `PUT` | `/teachers/me/profile` | Teacher | Update permitted profile fields (phone, profile photo). |
| `GET` | `/teachers/me/classes` | Teacher | List all classes assigned to the requesting teacher. |
| `GET` | `/teachers/me/subjects` | Teacher | List all subjects taught by the requesting teacher. |

---

### 3.3 `/api/v1/students` (Student Directory & Self)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/students` | Teacher | List students with search, filters (department, class, semester), and pagination. |
| `POST` | `/students` | Teacher | Create student profile, generate student credentials, and enroll in class. |
| `GET` | `/students/{student_id}` | Teacher | Retrieve full student profile, enrollment details, and academic history. |
| `PUT` | `/students/{student_id}` | Teacher | Update student demographic information. |
| `PATCH` | `/students/{student_id}/status`| Teacher | Activate, deactivate, or suspend a student account. |
| `GET` | `/students/me/dashboard` | Student | Get personal student dashboard (attendance %, recent published marks, today's timetable). |
| `GET` | `/students/me/profile` | Student | View personal student profile details (read-only). |

---

### 3.4 `/api/v1/departments`, `/api/v1/classes`, `/api/v1/sections`

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/departments` | Authenticated | List all college departments. |
| `POST` | `/departments` | Teacher/Admin| Create a new academic department. |
| `GET` | `/classes` | Authenticated | List classes (teachers see only assigned classes unless filtering with admin scope). |
| `POST` | `/classes` | Teacher/Admin| Create class, section, semester assignment, and capacity. |
| `GET` | `/classes/{class_id}/roster`| Teacher | Get all students currently enrolled in the class. |
| `POST` | `/classes/{class_id}/teachers`| Teacher/Admin| Assign a teacher to a class. |

---

### 3.5 `/api/v1/subjects` & `/api/v1/enrollments`

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/subjects` | Authenticated | List subjects by department and semester. |
| `POST` | `/subjects` | Teacher/Admin| Create subject (code, name, credits). |
| `POST` | `/classes/{class_id}/subjects`| Teacher/Admin| Map subject to class and assign teacher. |
| `POST` | `/enrollments` | Teacher | Enroll a student into a class for an academic year. |
| `POST` | `/enrollments/bulk` | Teacher | Bulk enroll multiple students into a class. |

---

### 3.6 `/api/v1/attendance` (Session & Roll Call)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/attendance/sessions` | Teacher | Create an attendance session for a class, subject, and date. |
| `GET` | `/attendance/sessions/{id}` | Teacher | Get session metadata and line items for enrolled students. |
| `POST` | `/attendance/sessions/{id}/records` | Teacher | Submit/update bulk attendance records (`PRESENT`, `ABSENT`, `LATE`, `EXCUSED`). |
| `GET` | `/attendance/classes/{class_id}/monthly` | Teacher | Get monthly attendance grid and percentage summary per student. |
| `GET` | `/attendance/students/me` | Student | Get own attendance statistics (overall %, subject-wise, absent/late counts). |
| `GET` | `/attendance/students/{student_id}` | Teacher | Get detailed attendance history for an individual student. |

---

### 3.7 `/api/v1/exams` & `/api/v1/marks`

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/exams` | Authenticated | List examination terms/cycles. |
| `POST` | `/exams` | Teacher | Create examination cycle (Internal, Mid, Practical, Final). |
| `POST` | `/exams/{exam_id}/assessments` | Teacher | Create assessment for a Class + Subject with max & passing marks. |
| `GET` | `/assessments/{id}/marks` | Teacher | Get marks roster for all students in the assessment (draft or published). |
| `POST` | `/assessments/{id}/marks` | Teacher | Enter or batch-save marks (draft status). Validates `0 <= marks <= max_marks`. |
| `POST` | `/assessments/{id}/publish` | Teacher | Publish assessment marks. Triggers student notifications and unlocks student view. |
| `GET` | `/marks/students/me` | Student | Get personal marks for all **published** assessments only. |

---

### 3.8 `/api/v1/grades` (Configurable Scales & Calculations)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/grades/scales` | Authenticated | List institutional grade scale rules (percentage ranges, letters, grade points). |
| `POST` | `/grades/scales` | Teacher/Admin| Configure or adjust a grade scale band. |
| `GET` | `/grades/classes/{class_id}/calculate` | Teacher | Compute semester grades for all enrolled students based on active grade scale. |
| `GET` | `/grades/students/me` | Student | View calculated semester grades and GPA summary for published exams. |

---

### 3.9 `/api/v1/timetable` (Schedule & Conflict Detection)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/timetable/classes/{class_id}` | Authenticated | Get weekly timetable matrix for a class. |
| `GET` | `/timetable/teachers/me` | Teacher | Get weekly schedule for the requesting teacher. |
| `GET` | `/timetable/students/me` | Student | Get personal timetable for student's enrolled class. |
| `POST` | `/timetable/slots` | Teacher | Create slot with conflict validation (room clash, teacher clash, section clash). |
| `PUT` | `/timetable/slots/{id}` | Teacher | Update timetable slot with conflict check. |
| `DELETE` | `/timetable/slots/{id}` | Teacher | Remove a timetable slot. |

---

### 3.10 `/api/v1/announcements` & `/api/v1/notifications`

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/announcements` | Authenticated | List announcements relevant to user's class or role. |
| `POST` | `/announcements` | Teacher | Create and broadcast announcement with target audience and expiry. |
| `GET` | `/notifications` | Authenticated | List unread and recent personal notifications. |
| `PATCH` | `/notifications/{id}/read` | Authenticated | Mark a notification as read. |

---

### 3.11 `/api/v1/reports` (Analytics & Export)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/reports/attendance/class/{id}` | Teacher | Export class attendance summary (JSON or CSV). |
| `GET` | `/reports/marks/class/{id}/exam/{id}` | Teacher | Export exam marks sheet (JSON or CSV). |
| `GET` | `/reports/student/{id}/transcript` | Teacher/Student| Generate printable student academic summary report. |

