-- Users (Password is 'pass' for all)
-- Admin
INSERT INTO users (id, name, email, password, role)
VALUES ('10000000-0000-0000-0000-000000000001', 'Admin User', 'admin@healthcare.com', '$2a$10$Dow1F7fZ9qYwF0Y9JjM5nO0Jp0m6C3U1Z5D5ZpH9kF2zQm2QqX9lK', 'ADMIN')
ON CONFLICT (id) DO NOTHING;

-- Doctor 1
INSERT INTO users (id, name, email, password, role)
VALUES ('20000000-0000-0000-0000-000000000001', 'Dr. John Smith', 'john.smith@healthcare.com', '$2a$10$Dow1F7fZ9qYwF0Y9JjM5nO0Jp0m6C3U1Z5D5ZpH9kF2zQm2QqX9lK', 'DOCTOR')
ON CONFLICT (id) DO NOTHING;

-- Doctor 2
INSERT INTO users (id, name, email, password, role)
VALUES ('20000000-0000-0000-0000-000000000002', 'Dr. Sarah Wilson', 'sarah.wilson@healthcare.com', '$2a$10$Dow1F7fZ9qYwF0Y9JjM5nO0Jp0m6C3U1Z5D5ZpH9kF2zQm2QqX9lK', 'DOCTOR')
ON CONFLICT (id) DO NOTHING;

-- Patient 1
INSERT INTO users (id, name, email, password, role)
VALUES ('30000000-0000-0000-0000-000000000001', 'Alice Brown', 'alice@gmail.com', '$2a$10$Dow1F7fZ9qYwF0Y9JjM5nO0Jp0m6C3U1Z5D5ZpH9kF2zQm2QqX9lK', 'PATIENT')
ON CONFLICT (id) DO NOTHING;

-- Patient 2 (The one you use for testing)
INSERT INTO users (id, name, email, password, role)
VALUES ('30000000-0000-0000-0000-000000000002', 'Test Patient', 'p2@gmail.com', '$2a$10$Dow1F7fZ9qYwF0Y9JjM5nO0Jp0m6C3U1Z5D5ZpH9kF2zQm2QqX9lK', 'PATIENT')
ON CONFLICT (id) DO NOTHING;


-- Doctors Details
INSERT INTO doctors (id, user_id, name, specialization, qualification, experience, consultation_fee, rating, about, clinic_address, profile_image)
VALUES ('20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Dr. John Smith', 'Cardiologist', 'MBBS, MD', 15, 150.00, 4.8, 'Expert in heart diseases.', '123 Heart Lane, NY', 'https://example.com/dr_john.jpg')
ON CONFLICT (id) DO NOTHING;

INSERT INTO doctors (id, user_id, name, specialization, qualification, experience, consultation_fee, rating, about, clinic_address, profile_image)
VALUES ('20000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'Dr. Sarah Wilson', 'Dermatologist', 'MBBS, DDVL', 10, 120.00, 4.9, 'Specialist in skin care.', '456 Skin St, LA', 'https://example.com/dr_sarah.jpg')
ON CONFLICT (id) DO NOTHING;


-- Patients Details
INSERT INTO patients (id, user_id, age, gender, blood_group)
VALUES ('60000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 30, 'Female', 'O+')
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, user_id, age, gender, blood_group)
VALUES ('60000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 25, 'Male', 'A+')
ON CONFLICT (id) DO NOTHING;


-- Doctor Availability (Dr. John Smith)
INSERT INTO doctor_availability (id, doctor_id, day, start_time, end_time)
VALUES (gen_random_uuid(), '20000000-0000-0000-0000-000000000001', 'MON', '09:00:00', '12:00:00');

INSERT INTO doctor_availability (id, doctor_id, day, start_time, end_time)
VALUES (gen_random_uuid(), '20000000-0000-0000-0000-000000000001', 'MON', '14:00:00', '17:00:00');

INSERT INTO doctor_availability (id, doctor_id, day, start_time, end_time)
VALUES (gen_random_uuid(), '20000000-0000-0000-0000-000000000001', 'WED', '10:00:00', '14:00:00');

-- Doctor Availability (Dr. Sarah Wilson)
INSERT INTO doctor_availability (id, doctor_id, day, start_time, end_time)
VALUES (gen_random_uuid(), '20000000-0000-0000-0000-000000000002', 'TUE', '09:00:00', '13:00:00');

INSERT INTO doctor_availability (id, doctor_id, day, start_time, end_time)
VALUES (gen_random_uuid(), '20000000-0000-0000-0000-000000000002', 'THU', '15:00:00', '19:00:00');
