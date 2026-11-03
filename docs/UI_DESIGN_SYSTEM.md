# Student Management System (SMS) - Premium Dark UI/UX Design System

**Document Version:** 1.0.0  
**Phase:** Phase 0 (Planning & Architecture)  
**Theme:** Enterprise Academic Dark  

---

## 1. Color Palette Tokens

The design system is built upon a high-contrast, modern dark enterprise color architecture engineered specifically for reduced eye fatigue during long academic grading and scheduling sessions.

| Token Name | Hex Code | Tailwind Utility Class | Semantic Purpose |
| :--- | :--- | :--- | :--- |
| **Main Background** | `#0B1020` | `bg-[#0B1020]` / `bg-sms-dark-900` | Primary application canvas, login backdrop, main body |
| **Sidebar Background**| `#111827` | `bg-[#111827]` / `bg-sms-dark-800` | Navigation sidebar, collapsed drawer, command bar |
| **Card Background** | `#1E293B` | `bg-[#1E293B]` / `bg-sms-dark-700` | Metric cards, tables, modals, input containers |
| **Card Hover** | `#26354A` | `hover:bg-[#26354A]` | Table row hover, list item hover, interactive cards |
| **Primary Blue** | `#3B82F6` | `bg-blue-500` / `text-blue-500` | Primary action buttons, active navigation indicator, key highlights |
| **Success Green** | `#10B981` | `bg-emerald-500` / `text-emerald-500`| Present status, published marks, high attendance (>75%), passing grades |
| **Warning Orange** | `#F59E0B` | `bg-amber-500` / `text-amber-500` | Late status, pending drafts, warning attendance (65%-75%) |
| **Danger Red** | `#EF4444` | `bg-red-500` / `text-red-500` | Absent status, failed grade, low attendance (<65%), timetable conflict alert |
| **Primary Text** | `#F8FAFC` | `text-slate-50` / `text-sms-text-primary`| Page titles, headers, primary metric values, data table cells |
| **Secondary Text** | `#94A3B8` | `text-slate-400` / `text-sms-text-secondary`| Labels, subtitles, table headers, breadcrumbs, placeholder text |
| **Border & Divider** | `#334155` | `border-slate-700` / `border-sms-border` | Card borders, table dividers, input borders, separated panels |

---

## 2. Typography & Hierarchy

Font Family: `Inter`, `Geist Sans`, or system `-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`.

| Element | Tailwind Class | Font Size | Weight | Line Height |
| :--- | :--- | :--- | :--- | :--- |
| **Display / Page Title** | `text-2xl font-bold tracking-tight text-[#F8FAFC]` | 24px (1.5rem) | 700 | 1.25 |
| **Section Header** | `text-lg font-semibold text-[#F8FAFC]` | 18px (1.125rem) | 600 | 1.35 |
| **Card Metric Number** | `text-3xl font-extrabold text-[#F8FAFC]` | 30px (1.875rem) | 800 | 1.1 |
| **Body Primary** | `text-sm font-normal text-[#F8FAFC]` | 14px (0.875rem) | 400 | 1.5 |
| **Body Secondary / Meta**| `text-xs font-medium text-[#94A3B8]` | 12px (0.75rem) | 500 | 1.4 |
| **Table Header** | `text-xs font-semibold uppercase tracking-wider text-[#94A3B8]` | 12px (0.75rem) | 600 | 1.2 |

---

## 3. Component Design Specifications

### 3.1 Cards & Containers
- **Corner Radius:** `rounded-xl` (12px) for cards, `rounded-lg` (8px) for buttons and inputs.
- **Borders:** Subtle `1px` border using `#334155` (`border border-[#334155]`).
- **Elevation / Shadow:** Minimal soft shadow `shadow-lg shadow-black/20` to maintain a crisp dark enterprise feel without fuzzy light glows.

### 3.2 Navigation Sidebar
- **Width:** 260px expanded, 72px collapsed.
- **Active Navigation Item:** Primary Blue pill background `bg-blue-600/15 text-blue-400 font-semibold border-r-2 border-blue-500`.
- **Inactive Navigation Item:** `text-[#94A3B8] hover:text-[#F8FAFC] hover:bg-slate-800/60 transition-colors`.

### 3.3 Data Tables
- **Header:** Sticky `#111827` background with uppercase slate-400 labels and subtle bottom border `#334155`.
- **Row Styling:** Alternating subtle zebra striping or uniform `#1E293B` with hover state `#26354A`.
- **Badges:**
  - `Present`: `bg-emerald-500/10 text-emerald-400 border border-emerald-500/20`
  - `Absent`: `bg-red-500/10 text-red-400 border border-red-500/20`
  - `Late`: `bg-amber-500/10 text-amber-400 border border-amber-500/20`
  - `Excused`: `bg-blue-500/10 text-blue-400 border border-blue-500/20`
  - `Draft`: `bg-slate-700/50 text-slate-300 border border-slate-600`
  - `Published`: `bg-emerald-500/15 text-emerald-300 border border-emerald-500/30`

### 3.4 Feedback & Dynamic States
- **Loading Skeletons:** Animated pulsing skeletons `animate-pulse bg-slate-800 rounded` mimicking table rows, metric cards, and chart panels.
- **Empty States:** Centered illustration/icon with bold title and clear action button (e.g. "No examinations scheduled yet", with "Create Exam" button).
- **Toast Notifications:** Fixed bottom-right popups with icon, status color border, and auto-dismiss timer.
- **Modal Forms:** Backdrop overlay `bg-black/70 backdrop-blur-sm`, modal dialog centered with `#1E293B` background and `#334155` border.

