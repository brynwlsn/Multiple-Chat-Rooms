-- 1. Buat tabel users terlebih dahulu
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Buat tabel chat_rooms
CREATE TABLE chat_rooms (
    room_name VARCHAR(100) PRIMARY KEY,
    owner_name VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_name) REFERENCES users(username) ON DELETE SET NULL
);

-- 3. Buat tabel messages
CREATE TABLE messages (
    id INT IDENTITY(1,1) PRIMARY KEY,
    room_name VARCHAR(100),
    sender_name VARCHAR(50),
    message_text TEXT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_name) REFERENCES chat_rooms(room_name) ON DELETE CASCADE,
    FOREIGN KEY (sender_name) REFERENCES users(username) ON DELETE SET NULL
);

-- 4. Buat tabel room_participants
CREATE TABLE room_participants (
    room_name VARCHAR(100),
    username VARCHAR(50),
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_name, username),
    FOREIGN KEY (room_name) REFERENCES chat_rooms(room_name) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);