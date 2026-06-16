-- Database Schema for Classroom Relocation and Booking Management System

-- Blocks (campus building blocks)
CREATE TABLE IF NOT EXISTS blocks (
    block_id INTEGER PRIMARY KEY AUTOINCREMENT,
    block_name TEXT UNIQUE NOT NULL
);

-- Users (all roles)
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL,
    programme TEXT,
    level TEXT,
    semester TEXT,
    staff_id TEXT,
    department TEXT,
    must_change_password INTEGER DEFAULT 0
);

-- Block-Manager assignments (many-to-many)
CREATE TABLE IF NOT EXISTS block_managers (
    user_id INTEGER NOT NULL,
    block_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, block_id),
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY(block_id) REFERENCES blocks(block_id) ON DELETE CASCADE
);

-- Rooms
CREATE TABLE IF NOT EXISTS rooms (
    room_id INTEGER PRIMARY KEY AUTOINCREMENT,
    room_name TEXT UNIQUE,
    block_id INTEGER,
    building TEXT,
    capacity INTEGER,
    room_type TEXT,
    has_projector INTEGER DEFAULT 0,
    has_ac INTEGER DEFAULT 0,
    has_whiteboard INTEGER DEFAULT 1,
    availability_status TEXT DEFAULT 'AVAILABLE',
    FOREIGN KEY(block_id) REFERENCES blocks(block_id)
);

-- Timetable (official class schedule)
CREATE TABLE IF NOT EXISTS timetable (
    schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_code TEXT,
    course_name TEXT,
    lecturer TEXT,
    room_id INTEGER,
    day TEXT,
    start_time TEXT,
    end_time TEXT,
    programme TEXT,
    level TEXT,
    semester TEXT,
    FOREIGN KEY(room_id) REFERENCES rooms(room_id)
);

-- Bookings (relocation/room requests)
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    room_id INTEGER,
    course TEXT,
    lecturer TEXT,
    representative TEXT,
    booking_date TEXT,
    start_time TEXT,
    end_time TEXT,
    booking_type TEXT,
    reason TEXT,
    status TEXT,
    rejection_reason TEXT,
    alternative_room_id INTEGER,
    assigned_manager_id INTEGER,
    FOREIGN KEY(user_id) REFERENCES users(user_id),
    FOREIGN KEY(room_id) REFERENCES rooms(room_id),
    FOREIGN KEY(alternative_room_id) REFERENCES rooms(room_id),
    FOREIGN KEY(assigned_manager_id) REFERENCES users(user_id)
);

-- User-Course assignments (for reps and lecturers)
CREATE TABLE IF NOT EXISTS user_courses (
    user_id INTEGER NOT NULL,
    course_code TEXT NOT NULL,
    PRIMARY KEY (user_id, course_code),
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Notifications
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT DEFAULT 'INFO',
    is_read INTEGER DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Activity Log (for admin dashboard)
CREATE TABLE IF NOT EXISTS activity_log (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    action TEXT NOT NULL,
    details TEXT,
    timestamp TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- User Preferences (theme, accent, display settings)
CREATE TABLE IF NOT EXISTS user_preferences (
    user_id INTEGER PRIMARY KEY,
    theme TEXT DEFAULT 'dark',
    accent_color TEXT DEFAULT 'indigo',
    font_size TEXT DEFAULT 'Medium',
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
