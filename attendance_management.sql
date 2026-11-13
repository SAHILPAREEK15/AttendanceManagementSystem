CREATE DATABASE IF NOT EXISTS attendance_management;
USE attendance_management;

DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'TEACHER'
);

CREATE TABLE students (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    roll_no    VARCHAR(20)  NOT NULL UNIQUE,
    branch     VARCHAR(50)  NOT NULL,
    semester   INT          NOT NULL CHECK (semester BETWEEN 1 AND 8)
);

CREATE TABLE attendance (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    student_id      VARCHAR(20) NOT NULL,
    attendance_date DATE        NOT NULL,
    status          VARCHAR(10) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT')),
    CONSTRAINT uq_student_date UNIQUE (student_id, attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');

INSERT INTO students (student_id, name, roll_no, branch, semester) VALUES
('S001', 'Aarav Sharma', 'CS101', 'Computer Science', 2),
('S002', 'Priya Verma',  'CS102', 'Computer Science', 2),
('S003', 'Rohan Gupta',  'EC201', 'Electronics',      2);
