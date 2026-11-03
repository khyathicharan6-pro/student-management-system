# Student Management System (SMS) - System Architecture

**Document Version:** 1.0.0  
**Phase:** Phase 0 (Planning & Architecture)  
**Status:** Approved for Implementation  

---

## 1. Executive Summary

The **Student Management System (SMS)** is an enterprise-grade academic management platform built specifically for a single college institution. The system enforces strict role-based separation between two independent portals:

1. **Teacher Portal:** Full administrative and academic authority over assigned classes, student rosters, subject allocation, session-based attendance, examinations, marks entry, configurable grading scales, weekly timetables with conflict detection, announcements, and academic analytics.
2. **Student Portal:** Read-only access for enrolled students to inspect their personal profile, subject-wise and session-wise attendance statistics, examination marks (published only), calculated grades, weekly and daily timetables, and college/class notifications.

All operations synchronize through a single centralized PostgreSQL relational database accessed via a high-performance Python FastAPI backend and rendered with a Next.js (App Router) + TypeScript + Tailwind CSS dark-enterprise web application.

---

## 2. High-Level Architecture Diagram

```mermaid
flowchart TD
    subgraph ClientLayer["Frontend Client Layer (Next.js 15+ / React 19 / TypeScript)"]
        TP["Teacher Portal (/teacher/*)"]
        SP["Student Portal (/student/*)"]
        AuthView["Authentication & Login (/login)"]
        UIComponents["Premium Dark UI Components (Tailwind CSS, Lucide, Recharts)"]
    end

    subgraph APILayer["Backend API Gateway (FastAPI / Pydantic v2)"]
        AuthMiddleware["JWT / Session Auth & RBAC Middleware"]
        OwnershipGuard["Teacher & Student Ownership Guards"]
        APIRouters["REST API Routers (/api/v1/*)"]
        AuditService["Audit Logging Service"]
    end

    subgraph ServiceLayer["Core Domain Services"]
        AttendanceEngine["Attendance Calculation Engine"]
        GradeEngine["Configurable Grade Scale Engine"]
        TimetableEngine["Conflict Detection & Timetable Engine"]
        MarksEngine["Draft/Publish Marks Engine"]
        ReportEngine["Analytics & Report Generator"]
    end

    subgraph DataLayer["Persistence & Database Layer (PostgreSQL 16)"]
        ORM["SQLAlchemy 2.0 Async ORM"]
        Alembic["Alembic Migrations"]
        DB[(Centralized PostgreSQL DB)]
    end

    TP --> AuthMiddleware
    SP --> AuthMiddleware
    AuthView --> AuthMiddleware
    AuthMiddleware --> OwnershipGuard
    OwnershipGuard --> APIRouters
    APIRouters --> ServiceLayer
    ServiceLayer --> AuditService
    ServiceLayer --> ORM
    AuditService --> ORM
    ORM --> DB
    Alembic --> DB
```

---

## 3. Technology Stack & Component Justification

| Layer | Technology | Version / Spec | Justification |
| :--- | :--- | :--- | :--- |
| **Frontend Framework** | Next.js (App Router) | 15.x / React 19 | Server-side rendering, layout nesting for independent portals, optimal performance, native TypeScript support. |
| **Language (FE)** | TypeScript | 5.x | Strict type safety across portals, shared API interfaces, compile-time error reduction. |
| **Styling & UI** | Tailwind CSS + CSS Variables | 3.4+ / 4.x | Complete control over the `#0B1020` Enterprise Dark palette, utility-first ergonomics, micro-interactions. |
| **Icons & Charts** | Lucide React + Recharts | Latest stable | Clean enterprise iconography and SVG responsive charting for attendance and grade distributions. |
| **Backend Framework** | FastAPI | 0.115+ | High throughput async Python, automatic OpenAPI/Swagger documentation, strict Pydantic v2 data validation. |
| **ORM & Migrations** | SQLAlchemy + Alembic | 2.0+ / 1.14+ | Production-grade async relational mapper, schema version control, complex join capabilities, transactional integrity. |
| **Database** | PostgreSQL | 15+ / 16 | ACID compliance, JSONB support where needed, strict foreign key constraints, native UUID generation (`gen_random_uuid()`). |
| **Security & Auth** | Argon2 / Passlib + PyJWT | Latest stable | Industry-standard password hashing resilient against GPU brute force; signed JWT access/refresh token pairs. |

---

## 4. Portals & Separation of Concerns

```mermaid
graph LR
    subgraph Users["Users"]
        T[Teacher / Faculty]
        S[Student]
    end

    subgraph Gateway["Role Validation & Security Gateway"]
        AuthCheck{Role == TEACHER?}
    end

    subgraph TeacherDomain["Teacher Domain (Read / Write)"]
        T1[Assigned Class Roster]
        T2[Take & Correct Attendance]
        T3[Draft & Publish Marks]
        T4[Manage Timetable & Conflicts]
        T5[Create Announcements]
    end

    subgraph StudentDomain["Student Domain (Read Only)"]
        S1[Own Profile & Enrollment]
        S2[Personal Attendance Rates]
        S3[Only Published Exam Marks]
        S4[Semester Grade Reports]
        S5[Live Schedule & Notices]
    end

    T --> Gateway
    S --> Gateway
    Gateway -- "Role: TEACHER" --> TeacherDomain
    Gateway -- "Role: STUDENT" --> StudentDomain
    Gateway -. "DENIED: Student -> TeacherDomain" .-> T1
```

### 4.1 Teacher Portal Security Boundaries
- Teachers can only read and write data for classes and subjects formally assigned to them in `class_teachers` or `class_subjects`.
- Marks management enforces a strict lifecycle: `DRAFT` $\to$ `VALIDATED` $\to$ `PUBLISHED`. Teachers can edit drafts; published marks require an audit-logged revision workflow.
- Attendance sessions can only be initiated for dates within the active academic semester.

### 4.2 Student Portal Security Boundaries
- Students query records strictly filtered by their `student_id` derived directly from their validated authentication token (never from client-supplied URL parameters).
- Draft marks and unverified exam entries are inaccessible to students at the SQL query level (e.g., `WHERE status = 'PUBLISHED'`).
- Students have zero write access across all academic endpoints (attendance, marks, schedules, profiles).

---

## 5. Network & API Request Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor Client as Next.js Client
    participant Auth as Auth Middleware
    participant Guard as RBAC & Ownership Guard
    participant Service as Business Service
    participant Repo as Data Repository / ORM
    participant DB as PostgreSQL Database
    participant Audit as Audit Logger

    Client->>Auth: Request with Bearer JWT Token
    Auth->>Auth: Verify Signature & Expiry
    alt Token Invalid
        Auth-->>Client: 401 Unauthorized
    else Token Valid
        Auth->>Guard: Token Payload (user_id, role, scopes)
        Guard->>Guard: Verify Role & Class/Student Ownership
        alt Ownership Failed
            Guard-->>Client: 403 Forbidden
        else Ownership Verified
            Guard->>Service: Execute Academic Operation
            Service->>Repo: Begin DB Transaction
            Repo->>DB: SQL Execution with Foreign Key Checks
            DB-->>Repo: Query Result / Row Mutation
            Service->>Audit: Write Action Log (actor_id, action, diff)
            Audit->>DB: Insert into audit_logs
            Service->>Repo: Commit Transaction
            Repo-->>Client: 200 OK with Normalized Response Envelope
        end
    end
```

---

## 6. Directory Structure Overview

```
student-portal/
├── docs/                      # Architectural and operational documentation
│   ├── ARCHITECTURE.md
│   ├── DATABASE_SCHEMA.md
│   ├── API_DOCUMENTATION.md
│   ├── SECURITY.md
│   └── DEVELOPMENT_ROADMAP.md
├── backend/                   # FastAPI Backend
│   ├── app/
│   │   ├── api/v1/            # Versioned API routes
│   │   │   ├── endpoints/
│   │   │   └── api.py
│   │   ├── core/              # Config, security, database session
│   │   ├── models/            # SQLAlchemy 2.0 declarative models
│   │   ├── schemas/           # Pydantic v2 validation models
│   │   ├── services/          # Pure business logic engines
│   │   ├── repositories/      # Database abstraction queries
│   │   ├── permissions/       # Ownership and RBAC dependencies
│   │   └── utils/             # Helpers, formatters, exporters
│   ├── migrations/            # Alembic schema migrations
│   ├── tests/                 # Pytest automated test suites
│   ├── requirements.txt
│   └── Dockerfile
└── frontend/                  # Next.js 15 Application
    ├── app/
    │   ├── login/             # Enterprise login view
    │   ├── teacher/           # Teacher portal route group
    │   │   ├── dashboard/
    │   │   ├── students/
    │   │   ├── classes/
    │   │   ├── subjects/
    │   │   ├── attendance/
    │   │   ├── exams/
    │   │   ├── marks/
    │   │   ├── grades/
    │   │   ├── timetable/
    │   │   ├── announcements/
    │   │   ├── reports/
    │   │   └── profile/
    │   └── student/           # Student portal route group
    │       ├── dashboard/
    │       ├── profile/
    │       ├── attendance/
    │       ├── marks/
    │       ├── grades/
    │       ├── timetable/
    │       └── settings/
    ├── components/
    │   ├── ui/                # Base design system primitives
    │   ├── layout/            # Sidebar, Header, MobileNav
    │   ├── dashboard/         # Stat cards, quick links
    │   ├── tables/            # Data tables with pagination & sorting
    │   ├── charts/            # Recharts dark-theme visualizations
    │   └── forms/             # Modal dialog forms with validation
    ├── hooks/                 # Custom React hooks (auth, data, state)
    ├── services/              # Axios/Fetch API client abstractions
    ├── types/                 # Shared TypeScript interfaces
    ├── lib/                   # Utility functions & formatting
    └── package.json
```

