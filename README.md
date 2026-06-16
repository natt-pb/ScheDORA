# Schedora -- Book Smart. Learn Better.

A desktop classroom relocation and booking management system built with JavaFX. Schedora enables students, lecturers, and facility managers to coordinate room reservations, manage official timetables, and handle booking approvals through a role-based interface with real-time conflict detection.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup and Installation](#setup-and-installation)
- [Running the Application](#running-the-application)
- [User Roles and Permissions](#user-roles-and-permissions)
- [Default Credentials](#default-credentials)
- [Database Schema](#database-schema)
- [Application Modules](#application-modules)
- [Configuration](#configuration)

---

## Overview

Schedora addresses the challenge of managing classroom relocations and room bookings in academic institutions. When a scheduled venue becomes unavailable (equipment failure, maintenance, capacity issues), authorized users can submit relocation requests that are routed to the appropriate facility manager for approval. The system enforces scheduling rules including conflict detection against both the official timetable and existing bookings, operating hour constraints (6:30 AM -- 8:30 PM), weekday-only scheduling, and a maximum session duration of 3 hours.

---

## Features

### Booking and Reservation Management
- Submit room booking requests with course details, time slots, and justification
- Automatic conflict detection against the official timetable and active bookings
- Booking lifecycle management: Pending, Approved, Rejected, Cancelled, Released
- Automatic routing of requests to the facility manager responsible for the target block
- Alternative room suggestions on rejection
- Room release functionality to free approved bookings

### Timetable Management
- View official class schedules filtered by programme, level, and semester
- Import timetable data from CSV files
- Timetable entries linked to specific rooms and time slots
- Cross-referencing with bookings for conflict detection

### Room and Block Management
- 93 pre-seeded classrooms across 5 campus blocks (NLT, CALC, SW, LT, G)
- Room attributes: capacity, type (Classroom, Lecture Theatre, Auditorium), equipment (projector, AC, whiteboard)
- Room availability tracking with real-time status
- Block-to-manager assignment for decentralized approval workflows

### User Management
- Role-based access control with 5 distinct roles
- Bulk student import via CSV
- Admin-managed user creation and editing
- Forced password change on first login for imported accounts

### Approval Workflow
- Facility managers review bookings only for blocks they manage
- Approve or reject with reason and alternative room suggestion
- Notification dispatch on status changes

### Notifications
- In-app notification center for booking status updates
- Notification types: Booking Submitted, Approved, Rejected, Room Released, Announcements
- Read/unread tracking

### Reporting and Analytics
- Room utilization reports by date range
- Booking history with filtering by status, room, and date
- CSV export of booking data

### Settings and Personalization
- Theme selection: Dark and Light modes with live preview
- Accent colour options: Indigo, Teal, Amber, Rose
- Font size configuration (Small, Medium, Large)
- Preferences persisted per user in the database
- Profile management and password change

---

## Technology Stack

| Component       | Technology                         |
|-----------------|------------------------------------|
| Language        | Java 21                            |
| UI Framework    | JavaFX 21.0.2 (FXML + CSS)        |
| Database        | SQLite 3 (via sqlite-jdbc 3.45.1)  |
| Build Tool      | Apache Maven 3.9.x                 |
| Password Hashing| SHA-256                            |

---

## Architecture

The application follows a layered MVC architecture:

```
Presentation Layer (FXML Views + CSS)
         |
   Controller Layer (JavaFX Controllers)
         |
     Service Layer (Business Logic)
         |
  Data Access Layer (DAO Classes)
         |
    Database Layer (SQLite via JDBC)
```

- **Model**: Plain Java objects representing domain entities (User, Room, Booking, Timetable, Block, Notification, ActivityLog, Course)
- **View**: FXML layout files with a shared CSS stylesheet for consistent theming
- **Controller**: JavaFX controllers that handle user interaction and delegate to services
- **Service**: Business logic including conflict detection, authentication, CSV import, booking workflows, and report generation
- **DAO**: Data access objects encapsulating all SQL operations
- **Util**: Cross-cutting concerns such as session management, input validation, and alert dialogs

---

## Project Structure

```
ScheDULE/
├── pom.xml                          # Maven project configuration
├── classroom_scheduler.db           # SQLite database file (auto-created)
├── src/
│   └── main/
│       ├── java/com/classroomscheduler/
│       │   ├── Main.java                        # Application entry point
│       │   ├── controller/
│       │   │   ├── ApprovalController.java      # Booking approval/rejection
│       │   │   ├── AvailabilityController.java  # Room availability view
│       │   │   ├── BlockController.java         # Block management
│       │   │   ├── BookingController.java       # Booking request form
│       │   │   ├── ChangePasswordController.java# Password change dialog
│       │   │   ├── DashboardController.java     # Dashboard with metrics
│       │   │   ├── LoginController.java         # Authentication screen
│       │   │   ├── MainLayoutController.java    # Sidebar navigation shell
│       │   │   ├── NotificationsController.java # Notification center
│       │   │   ├── ReportsController.java       # Reporting and CSV export
│       │   │   ├── ReservationsController.java  # User's booking history
│       │   │   ├── RoomController.java          # Room CRUD operations
│       │   │   ├── SettingsController.java      # User preferences
│       │   │   ├── SplashController.java        # Splash screen
│       │   │   ├── TimetableController.java     # Timetable viewer
│       │   │   └── UserController.java          # User CRUD (admin)
│       │   ├── dao/
│       │   │   ├── ActivityLogDAO.java           # Activity log persistence
│       │   │   ├── BlockDAO.java                 # Block data access
│       │   │   ├── BookingDAO.java               # Booking data access
│       │   │   ├── NotificationDAO.java          # Notification data access
│       │   │   ├── RoomDAO.java                  # Room data access
│       │   │   ├── TimetableDAO.java             # Timetable data access
│       │   │   ├── UserDAO.java                  # User data access
│       │   │   └── UserPreferencesDAO.java       # Preferences data access
│       │   ├── database/
│       │   │   ├── DatabaseConnection.java       # JDBC connection manager
│       │   │   └── DatabaseInitializer.java      # Schema creation and seeding
│       │   ├── model/
│       │   │   ├── ActivityLog.java
│       │   │   ├── Block.java
│       │   │   ├── Booking.java
│       │   │   ├── Course.java
│       │   │   ├── Notification.java
│       │   │   ├── Room.java
│       │   │   ├── Timetable.java
│       │   │   └── User.java
│       │   ├── service/
│       │   │   ├── ActivityLogService.java       # Activity logging
│       │   │   ├── AuthenticationService.java    # Login, registration, password
│       │   │   ├── BookingService.java           # Booking workflow orchestration
│       │   │   ├── ConflictDetectionService.java # Time-slot conflict checks
│       │   │   ├── CsvImportService.java         # Student and timetable import
│       │   │   ├── NotificationService.java      # Notification dispatch
│       │   │   └── ReportService.java            # Utilization reports and export
│       │   └── util/
│       │       ├── AlertUtil.java                # JavaFX alert dialogs
│       │       ├── SessionManager.java           # In-memory session and role checks
│       │       └── ValidationUtil.java           # Input validation helpers
│       └── resources/
│           ├── css/
│           │   └── style.css                     # Global stylesheet (theming)
│           ├── database/
│           │   └── Schema.sql                    # DDL for all tables
│           ├── fxml/                             # FXML view layouts
│           │   ├── approval.fxml
│           │   ├── availability.fxml
│           │   ├── block_management.fxml
│           │   ├── booking.fxml
│           │   ├── change_password.fxml
│           │   ├── dashboard.fxml
│           │   ├── login.fxml
│           │   ├── main_layout.fxml
│           │   ├── notifications.fxml
│           │   ├── reports.fxml
│           │   ├── reservations.fxml
│           │   ├── room_management.fxml
│           │   ├── settings.fxml
│           │   ├── splash.fxml
│           │   ├── timetable.fxml
│           │   └── users.fxml
│           └── images/                           # Application icons and assets
```

---

## Prerequisites

- **Java Development Kit (JDK)** 21 or later
- **Apache Maven** 3.9.x (bundled in the repository under `apache-maven-3.9.16/`)
- **JavaFX SDK** 21.0.2 (bundled in the repository under `javafx-sdk-21.0.2/`)

No external database server is required. SQLite runs embedded and creates the database file automatically on first launch.

---

## Setup and Installation

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd ScheDULE
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

---

## Running the Application

Launch the application using the JavaFX Maven plugin:

```bash
mvn javafx:run
```

Or using the bundled Maven:

```bash
apache-maven-3.9.16/bin/mvn javafx:run
```

The application opens with a splash screen that transitions to the login page. On first launch, the database is automatically initialized with the schema, default users, blocks, rooms, sample timetable entries, and sample bookings.

---

## User Roles and Permissions

| Role              | Key Permissions                                                                              |
|-------------------|----------------------------------------------------------------------------------------------|
| Main Admin        | Full system access. Manage users, rooms, blocks, timetables, bookings. Approve/reject requests. View reports and activity logs. |
| Facility Manager  | Approve/reject booking requests for assigned blocks. View room availability and reports.     |
| Lecturer          | Submit booking requests. View personal reservations. View timetable and room availability.   |
| Student Rep       | Submit booking requests on behalf of a course. View personal reservations and timetable.     |
| Student           | View timetable and room availability. Read-only access to bookings.                          |

---

## Default Credentials

The following accounts are seeded on first launch for testing and demonstration purposes.

| Role              | Username          | Password       |
|-------------------|-------------------|----------------|
| Main Admin        | `admin`           | `admin123`     |
| Facility Manager  | `facility1`       | `facility123`  |
| Facility Manager  | `facility2`       | `facility123`  |
| Lecturer          | `lecturer`        | `lecturer123`  |
| Lecturer          | `taylor`          | `lecturer123`  |
| Student Rep       | `PS/CSC/23/0201`  | `rep123`       |
| Student           | `PS/CSC/23/0202`  | `student123`   |

> **Note:** The Student account (`PS/CSC/23/0202`) is flagged for mandatory password change on first login.

---

## Database Schema

The application uses a local SQLite database (`classroom_scheduler.db`) with the following tables:

| Table               | Purpose                                                      |
|---------------------|--------------------------------------------------------------|
| `blocks`            | Campus building blocks (NLT, CALC, SW, LT, G)               |
| `users`             | All user accounts with role and profile information          |
| `block_managers`    | Many-to-many mapping of facility managers to blocks          |
| `rooms`             | Classrooms with capacity, type, and equipment attributes     |
| `timetable`         | Official class schedule entries                              |
| `bookings`          | Room booking/relocation requests with full lifecycle status  |
| `user_courses`      | Course assignments for lecturers and student representatives |
| `notifications`     | In-app notification messages per user                        |
| `activity_log`      | Audit trail of user actions                                  |
| `user_preferences`  | Per-user theme, accent colour, and display settings          |

The schema is defined in `src/main/resources/database/Schema.sql` and is automatically applied on startup via `DatabaseInitializer`.

---

## Application Modules

### Authentication and Session Management
Handles login with SHA-256 password hashing and in-memory session tracking. Supports forced password change for imported accounts. Session state determines navigation visibility and feature access throughout the application.

### Conflict Detection Engine
Validates booking requests against two data sources: the official timetable (matching by room and day of week) and existing active bookings (matching by room and date). Enforces business rules including weekday-only scheduling, operating hours (6:30 AM -- 8:30 PM), and maximum 3-hour session duration.

### Booking Workflow
Orchestrates the full booking lifecycle. On submission, the system validates inputs, runs conflict detection, auto-assigns the responsible facility manager based on the room's block, persists the booking, logs the activity, and dispatches notifications to both the requester and the assigned manager.

### CSV Import
Supports bulk data loading for two entity types:
- **Students**: Expected columns -- `name, username, password, programme, level, semester`
- **Timetable**: Expected columns -- `course_code, course_name, lecturer, room_name, day, start_time, end_time, programme, level, semester`

Imported students are assigned the `STUDENT` role with forced password change enabled. Duplicate usernames are skipped.

### Reporting
Generates room utilization statistics (count of approved bookings per room) and provides filterable booking history. Reports can be exported to CSV format.

---

## Configuration

### Database Location
The SQLite database file is created in the application's working directory as `classroom_scheduler.db`. This path is configured in `DatabaseConnection.java`.

### Seeded Data
On first launch, the following data is automatically seeded:
- 5 campus blocks
- 7 user accounts across all roles
- Block-to-manager assignments
- 93 classrooms across all blocks
- 5 official timetable entries for Computer Science Level 300
- 3 sample booking requests

### Theming
The application supports Dark and Light themes with four accent colour options. Theme changes are applied live and persisted to the `user_preferences` table. Styles are defined in `src/main/resources/css/style.css` using CSS custom properties.
