--Database Name : InstaLite
--Tables : 11

CREATE TABLE Users (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password VARCHAR(25) NOT NULL,
    Email_id VARCHAR(50),
    Mobile_no VARCHAR(10),
    Friends Int DEFAULT 0,
    Bio VARCHAR(100),
    is_active TINYINT(1) DEFAULT 1
);

CREATE TABLE FriendRequests (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Sender_Id INT,
    Sender_name VARCHAR(50),
    Receiver_Id INT,
    Status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    FOREIGN KEY (Sender_Id) REFERENCES Users(Id),
    FOREIGN KEY (Receiver_Id) REFERENCES Users(Id)
);

CREATE TABLE Image (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    User_id INT,
    Username VARCHAR(50),
    Image LONGBLOB,
    Caption VARCHAR(500),
    Size_kb INT,
    File_name VARCHAR(255),
    Time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(Id)
);

CREATE TABLE Vedio (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    User_id INT,
    Username VARCHAR(50),
    Vedio LONGBLOB,
    Caption VARCHAR(500),
    Size_kb INT,
    File_name VARCHAR(255),
    Time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(Id)
);

CREATE TABLE Messages (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Sender_id INT,
    Receiver_id INT,
    Sender_name VARCHAR(50),
    Content TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (Sender_id) REFERENCES Users(Id),
    FOREIGN KEY (Receiver_id) REFERENCES Users(Id)
);


CREATE TABLE Reels (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Video LONGBLOB,
    Caption VARCHAR(500),
    File_path VARCHAR(255),
    Time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Likes (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50),
    User_id INT,
    Reel_id INT,
    UNIQUE(User_id, Reel_id),
    FOREIGN KEY (User_id) REFERENCES Users(Id),
    FOREIGN KEY (Reel_id) REFERENCES Reels(Id)
);

CREATE TABLE Saves (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50),
    User_id INT,
    Reel_id INT,
    UNIQUE(User_id, Reel_id),
    FOREIGN KEY (User_id) REFERENCES Users(Id),
    FOREIGN KEY (Reel_id) REFERENCES Reels(Id)
);


CREATE TABLE Wellbeing (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    User_id INT,
    Session_date DATE,
    Duration_minutes INT,
    FOREIGN KEY (User_id) REFERENCES Users(Id)
);

CREATE TABLE IF NOT EXISTS Games (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  username VARCHAR(100) NOT NULL,
  game_name VARCHAR(100) NOT NULL,
  played_at DATETIME NOT NULL,
  won TINYINT(1) NOT NULL,
  INDEX (user_id),
  FOREIGN KEY (user_id) REFERENCES Users(Id) ON DELETE CASCADE
);

ALTER TABLE Games 
  ADD COLUMN last_played_date DATE NULL;
ALTER TABLE Games 
  ADD COLUMN points INT NOT NULL DEFAULT 0,
  ADD COLUMN streak INT NOT NULL DEFAULT 0;


CREATE TABLE IF NOT EXISTS Stats (
  user_id INT PRIMARY KEY,
  streak INT NOT NULL DEFAULT 0,
  points INT NOT NULL DEFAULT 0,
  last_played_date DATE NULL,
  FOREIGN KEY (user_id) REFERENCES Users(Id) ON DELETE CASCADE
);



--Procedure and Triggers
--Procedure for Inserting at Games 
DELIMITER //
CREATE PROCEDURE SaveGameSession(
    IN p_userId INT,
    IN p_username VARCHAR(50),
    IN p_gameName VARCHAR(100),
    IN p_won BOOLEAN
)
BEGIN
    INSERT INTO Games (user_id, username, game_name, played_at, won)
    VALUES (p_userId, p_username, p_gameName, NOW(), p_won);
END //
DELIMITER ;


--Trigger for Updating stats
DELIMITER //
CREATE TRIGGER after_game_insert
AFTER INSERT ON Games
FOR EACH ROW
BEGIN
    IF NEW.won = 1 THEN
        INSERT INTO Stats (user_id, username, streak, points, last_played_date)
        VALUES (NEW.user_id, NEW.username, 1, 10, CURDATE())
        ON DUPLICATE KEY UPDATE
            streak = streak + 1,
            points = points + 10,
            last_played_date = CURDATE();
    ELSE
        INSERT INTO Stats (user_id, username, streak, points, last_played_date)
        VALUES (NEW.user_id, NEW.username, 0, 0, CURDATE())
        ON DUPLICATE KEY UPDATE
            streak = 0,
            last_played_date = CURDATE();
    END IF;
END //
DELIMITER ;



--Procedure for Deleting Account 
DELIMITER //
CREATE PROCEDURE DeleteUserProc(IN p_username VARCHAR(50))
BEGIN
    DELETE FROM Users WHERE Username = p_username;
END //
DELIMITER ;


--Trigger for Deleting Logs From Table
DELIMITER //
CREATE TRIGGER after_user_delete
AFTER DELETE ON Users
FOR EACH ROW
BEGIN
    INSERT INTO DeletedUsersLog (username) VALUES (OLD.Username);
END //
DELIMITER ;
