
---
# ScheDORA — Book Smart. Learn Better.

A JavaFX desktop app for managing classroom bookings and relocations in academic institutions.

---

## Overview

ScheDORA lets students, lecturers, and facility managers coordinate room reservations, manage timetables, and handle booking approvals. It detects scheduling conflicts in real time and enforces rules: weekdays only, 6:30 AM–8:30 PM, max 3-hour sessions.

---

## Tech Stack

Java 21 · JavaFX 21.0.2 · SQLite 3 · Apache Maven 3.9.x · SHA-256 password hashing

---

## Architecture

MVC — FXML views → Controllers → Services → DAOs → SQLite

---

## Prerequisites

- JDK 21+
- Maven 3.9.x (bundled under `apache-maven-3.9.16/`)
- JavaFX SDK 21.0.2 (bundled under `javafx-sdk-21.0.2/`)

No external database needed — SQLite runs embedded.

---
## Setup and Installation

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   ```

2. **Verify Java version**

   ```bash
   java -version
   ```

   Ensure the output shows version 21 or later.

3. **Build the project**

   Using the bundled Maven wrapper:

   ```bash
   apache-maven-3.9.16/bin/mvn clean compile
   ```

   Or with a system-installed Maven:

   ```bash
   mvn clean compile
   ```

## Running the Application

Launch the application using the JavaFX Maven plugin:

```bash
mvn javafx:run
```

Or using the bundled Maven:

```bash
apache-maven-3.9.16/bin/mvn javafx:run
```

The application opens with a splash screen that transitions to the login page. On first launch, the database is automatically initialized with the schema, default users, blocks, rooms, sample timetable entries, and sample bookings

On first launch, the database is auto-created and seeded with default data.

---

## User Roles

| Role | Key Access |
|---|---|
| Main Admin | Full system access |
| Facility Manager | Approve/reject bookings for assigned blocks |
| Lecturer | Submit bookings, view reservations and timetable |
| Student Rep | Submit bookings on behalf of a course |
| Student | View timetable and availability (read-only) |

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Main Admin | `admin` | `admin123` |
| Facility Manager | `facility1` | `facility123` |
| Facility Manager | `facility2` | `facility123` |
| Lecturer | `lecturer` | `lecturer123` |
| Lecturer | `taylor` | `lecturer123` |
| Student Rep | `PS/CSC/23/0201` | `rep123` |
| Student | `PS/CSC/23/0202` | `student123` |

> The student account is flagged for a mandatory password change on first login.

---

## Key Features

- Booking lifecycle: Pending → Approved → Rejected → Canceled → Released
- Conflict detection against timetable and existing bookings
- Auto-routing to the facility manager responsible for the target block
- Bulk import of students and timetable entries via CSV
- Room utilization reports with CSV export
- In-app notifications for booking status changes
- Dark/light themes with accent colour options, persisted per user

---

## Database

SQLite file: `classroom_scheduler.db` (auto-created on first run)

Seeded with 5 campus blocks, 93 classrooms, 7 user accounts, sample timetable entries, and sample bookings.
