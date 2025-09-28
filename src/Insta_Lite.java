import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DBConnection {
    static final String URL = "jdbc:sqlite:Insta_Lite.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver missing.");
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            System.out.println("Initializing SQLite Database...");

            stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Username TEXT UNIQUE NOT NULL," +
                    "Password TEXT NOT NULL," +
                    "Email_id TEXT," +
                    "Mobile_no TEXT," +
                    "Friends INTEGER DEFAULT 0," +
                    "Bio TEXT," +
                    "is_active INTEGER DEFAULT 1" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS FriendRequests (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Sender_Id INTEGER," +
                    "Sender_name TEXT," +
                    "Receiver_Id INTEGER," +
                    "Status TEXT CHECK(Status IN ('PENDING', 'ACCEPTED', 'REJECTED')) DEFAULT 'PENDING'," +
                    "UNIQUE(Sender_Id, Receiver_Id)," +
                    "FOREIGN KEY (Sender_Id) REFERENCES Users(Id)," +
                    "FOREIGN KEY (Receiver_Id) REFERENCES Users(Id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Image (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "User_id INTEGER," +
                    "Username TEXT," +
                    "Caption TEXT," +
                    "Size_kb INTEGER," +
                    "File_name TEXT," +
                    "Time TEXT DEFAULT (datetime('now', 'localtime'))," +
                    "FOREIGN KEY (user_id) REFERENCES Users(Id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Vedio (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "User_id INTEGER," +
                    "Username TEXT," +
                    "Caption TEXT," +
                    "Size_kb INTEGER," +
                    "File_name TEXT," +
                    "Time TEXT DEFAULT (datetime('now', 'localtime'))," +
                    "FOREIGN KEY (user_id) REFERENCES Users(Id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Messages (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Sender_id INTEGER," +
                    "Receiver_id INTEGER," +
                    "Sender_name TEXT," +
                    "Content TEXT," +
                    "timestamp TEXT DEFAULT (datetime('now', 'localtime'))," +
                    "FOREIGN KEY (Sender_id) REFERENCES Users(Id)," +
                    "FOREIGN KEY (Receiver_id) REFERENCES Users(Id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Reels (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Caption TEXT," +
                    "File_path TEXT," +
                    "Time TEXT DEFAULT (datetime('now', 'localtime'))" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Likes (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Username TEXT," +
                    "User_id INTEGER," +
                    "Reel_id INTEGER," +
                    "UNIQUE(User_id, Reel_id)," +
                    "FOREIGN KEY (User_id) REFERENCES Users(Id)," +
                    "FOREIGN KEY (Reel_id) REFERENCES Reels(Id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Saves (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Username TEXT," +
                    "User_id INTEGER," +
                    "Reel_id INTEGER," +
                    "UNIQUE(User_id, Reel_id)," +
                    "FOREIGN KEY (User_id) REFERENCES Users(Id)," +
                    "FOREIGN KEY (Reel_id) REFERENCES Reels(Id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Wellbeing (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "User_id INTEGER," +
                    "Session_date TEXT," +
                    "Duration_minutes INTEGER," +
                    "FOREIGN KEY (User_id) REFERENCES Users(Id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Games (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "username TEXT NOT NULL," +
                    "game_name TEXT NOT NULL," +
                    "played_at TEXT NOT NULL," +
                    "won INTEGER NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES Users(Id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Stats (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER UNIQUE NOT NULL," +
                    "username TEXT NOT NULL," +
                    "streak INTEGER DEFAULT 0," +
                    "points INTEGER DEFAULT 0," +
                    "last_played_date TEXT," +
                    "FOREIGN KEY (user_id) REFERENCES Users(Id) ON DELETE CASCADE" +
                    ")");

            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM Reels;");
            if (rs.getInt(1) == 0) {
                String insertReels = "INSERT INTO Reels (Caption, File_path) VALUES " +
                        "('Move On isnt Easy But Keep Trying', 'Reels/1.mp4')," +
                        "('Alakh Pandey - PhysicsWallah Motivation', 'Reels/2.mp4')," +
                        "('Best decision are something to let go', 'Reels/6.mp4')," +
                        "('What Love Looks Like', 'Reels/7.mp4')," +
                        "('Motivation for Starting something new', 'Reels/11.mp4');";
                stmt.execute(insertReels);
            }

            System.out.println("Database Initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
}

class UserProfile {
    private String username;
    private String email;
    private String bio;

    public UserProfile(String username, String email, String bio) {
        this.username = username;
        this.email = email;
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getBio() {
        return bio;
    }

    public void displayProfile() {
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        System.out.println("Bio: " + bio);
    }
}

class User{
    Scanner sc = new Scanner(System.in);
    static int loggedInUserId = -1;
    static String loggedInUserName = null;

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }
    public static String getLoggedInUserName() {
        return loggedInUserName;
    }

    public void createAccount() {
        System.out.print("Enter Username : ");
        String username = sc.nextLine();
        try (Connection con = DBConnection.getConnection()) {
            String query = "Select Id from Users where Username = ?";
            PreparedStatement check = con.prepareStatement(query);
            check.setString(1,username);
            ResultSet r = check.executeQuery();
            if(r.next()){
                System.out.println("Username Already Exists , Try a Different One.\n");
                return;
            }
        }
        catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.print("Set Password : ");
        String pass = sc.nextLine();

        String mail;
        while (true) {
            System.out.print("Enter Your Email_id : ");
            mail = sc.nextLine();
            if(isValidEmail(mail)) {
                break;
            } else {
                System.out.println("Invalid Email, Please Enter a Valid (Gmail, Yahoo, or Hotmail) Address.\n");
            }
        }

        String phone;
        int Pass = 0;
        while (true) {
            System.out.print("Enter Mobile number: ");
            phone = sc.nextLine();
            if (phone.length() == 10 && phone.matches("\\d+")) {
                Random R = new Random();
                Pass = 100000 + R.nextInt(900000);
                System.out.println("\nDear Customer,\n"+ " ----- " + Pass + " ----- " + " is your One Time Password(OTP)."+
                        "\nPlease Do Not Share it With Anyone.\n"+"Thank You,\n"+"Team [Insta_Lite].\n");
                break;
            } else {
                System.out.println("Invalid Phone Number, Please Try Again.\n");
            }
        }

        boolean verified = false;
        for (int i = 0; i <= 2; i++) {
            System.out.print("Enter OTP sent to your Mobile: ");
            int Otp = -1;
            try { Otp = Integer.parseInt(sc.nextLine()); } catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }

            if (Otp == Pass) {
                System.out.println("OTP Verified Successfully!");
                verified = true; break;
            }else {
                System.out.println("--- Incorrect OTP---");
            }
        }
        if (!verified) {
            System.out.println("Maximum Attempts Reached, Account Creation Failed.\n");
            return;
        }

        System.out.print("Enter BIO : ");
        String bio = sc.nextLine();

        try (Connection con = DBConnection.getConnection()) {
            String Create = "INSERT INTO Users(Username,Password,Email_id,Mobile_no,Bio,is_active) VALUES(?,?,?,?,?,1)";
            PreparedStatement create = con.prepareCall(Create);
            create.setString(1, username);
            create.setString(2, pass);
            create.setString(3, mail);
            create.setString(4, phone);
            create.setString(5, bio);
            create.executeUpdate();
            System.out.println("Account Created.\n");
        }
        catch (SQLException e) {
            System.out.println("Error" + e.getMessage());
        }
    }

    public void log_In(){
        System.out.print("Enter Username : ");
        String username = sc.nextLine();
        System.out.print("Enter Password : ");
        String pass = sc.nextLine();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT Id FROM Users WHERE Username = ? AND Password = ? AND is_active = 1";
            PreparedStatement  log =  conn.prepareStatement(query);
            log.setString(1, username);
            log.setString(2, pass);
            ResultSet rs = log .executeQuery();
            if (rs.next()) {
                loggedInUserId = rs.getInt("Id");
                loggedInUserName = username;
                System.out.println("========= Logged in as " + username + " =========");
            } else {
                System.out.println("Invalid Credentials or Inactive Account.\n");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void logout() {
        loggedInUserId = -1;
        loggedInUserName = null;
        System.out.println("Logged out.");
    }

    public static boolean isLoggedIn() {
        if (loggedInUserId == -1) {
            System.out.println("------------------ PLEASE LOGIN FIRST -----------------");
            return false;
        }
        return true;
    }

    public void sendRequest(int senderId, int receiverId){
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement check = con.prepareStatement("SELECT * FROM FriendRequests WHERE Sender_id = ?" +
                    " AND Receiver_id = ? AND Status = 'PENDING'");
            check.setInt(1, senderId);
            check.setInt(2, receiverId);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                System.out.println("Friend request Already Sent.\n");
                return;
            }

            PreparedStatement checkReverse = con.prepareStatement("SELECT * FROM FriendRequests WHERE Sender_id = ?" +
                    " AND Receiver_id = ? AND Status = 'PENDING'");
            checkReverse.setInt(1, receiverId);
            checkReverse.setInt(2, senderId);
            ResultSet rsReverse = checkReverse.executeQuery();
            if (rsReverse.next()) {
                System.out.println("They have already sent you a friend request. Check your inbox to accept/decline.\n");
                return;
            }

            PreparedStatement add = con.prepareStatement(
                    "INSERT INTO FriendRequests(Sender_id,Sender_name,Receiver_id) VALUES (?,?,?)");
            add.setInt(1, senderId);
            add.setString(2,User.getLoggedInUserName());
            add.setInt(3, receiverId);
            add.executeUpdate();

            System.out.println("Friend request Sent.\n");
        } catch (SQLException e) {
            System.out.println("Error sending friend request: " + e.getMessage());
        }
    }

    public int check(String username){
        int id = -1;
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT Id FROM Users WHERE Username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt("Id");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return id;
    }

    public boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9._%+-]+@(gmail|yahoo|hotmail)\\.(com|in)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email.toLowerCase());
        return matcher.matches();
    }
}

class Account{
    static Scanner sc = new Scanner(System.in);
    public void delete(){
        if (!User.isLoggedIn()) return;
        System.out.print("Are you sure you want to delete your account permanently? (yes/no): ");
        String confirm = sc.nextLine();
        if (!confirm.equalsIgnoreCase("Yes")) {
            System.out.println("Deletion Cancelled.\n"); return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement delete = con.prepareStatement("DELETE FROM Users WHERE Username = ?");
            delete.setString(1,User.getLoggedInUserName());
            delete.executeUpdate();
            System.out.println("Account Deleted.\n");
            User.logout();
        }
        catch(SQLException e){
            System.out.println("Error - " + e.getMessage());
        }
    }

    public void deactivate(){
        if (!User.isLoggedIn()) return;
        System.out.print("Are you sure you want to deactivate your account permanently? (yes/no): ");
        String confirm = sc.nextLine();
        if (!confirm.equalsIgnoreCase("Yes")) {
            System.out.println("Deactivation Cancelled.\n"); return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement deactivate = conn.prepareStatement("UPDATE Users SET is_active = 0 WHERE Username = ?");
            deactivate.setString(1, User.getLoggedInUserName());
            deactivate.executeUpdate();
            System.out.println("Account Deactivated.\n");
            User.logout();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void forgotPassword(){
        System.out.print("Enter Registered Username : ");
        String username = sc.nextLine();
        String phone;
        int Pass = 0;

        while (true) {
            System.out.print("Enter Mobile number: ");
            phone = sc.nextLine();
            if (phone.length() == 10 && phone.matches("\\d+")) {
                Random R = new Random();
                Pass = 100000 + R.nextInt(900000);
                System.out.println("\nDear Customer,\n"+ " ----- " + Pass + " ----- " + " is your One Time Password(OTP)."+
                        "\nPlease Do Not Share it With Anyone.\n"+"Thank You,\n"+"Team [Insta_Lite].\n");
                break;
            } else {
                System.out.println("Invalid Phone Number, Please Try Again.\n");
            }
        }

        boolean verified = false;
        for (int i = 0; i <= 2; i++) {
            System.out.print("Enter OTP sent to your Mobile: ");
            int Otp = -1;
            try { Otp = Integer.parseInt(sc.nextLine()); } catch (NumberFormatException e) { System.out.println("Invalid input."); continue; }

            if (Otp == Pass) {
                System.out.println("OTP Verified Successfully!");
                try(Connection con = DBConnection.getConnection()){
                    PreparedStatement check = con.prepareStatement("SELECT Username FROM Users WHERE Username = ?");
                    check.setString(1,username);
                    ResultSet r = check.executeQuery();
                    if(r.next()) {
                        String user = r.getString("Username");
                        System.out.print("Set New Password : ");
                        String pass = sc.nextLine();
                        PreparedStatement up = con.prepareStatement("UPDATE Users SET Password = ? WHERE Username = ?");
                        up.setString(1, pass);
                        up.setString(2, user);
                        up.executeUpdate();
                        System.out.println("Password Updated.\n");
                    } else {
                        System.out.println("Username Not Found.\n");
                    }
                }
                catch(SQLException e){
                    System.out.println("Error - " + e.getMessage());
                }
                verified = true; break;
            }else {
                System.out.println("--- Incorrect OTP---");
            }
        }
        if (!verified) {
            System.out.println("Maximum Attempts Reached, Account Creation Failed.\n");
        }
    }
}

class Reels_Management{
    static Scanner sc = new Scanner(System.in);
    public void post(){
        if (!User.isLoggedIn()) return;
        System.out.print("Enter File Path (e.g., 'C:/myimage.jpg'): ");
        String path = sc.nextLine();
        File f = new File(path);
        System.out.print("Enter Image Caption: ");
        String caption = sc.nextLine();

        try{
            Connection con = DBConnection.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO Image(User_id,Username,Caption,Size_kb,File_name) VALUES(?,?,?,?,?)");
            st.setInt(1,User.getLoggedInUserId());
            st.setString(2,User.getLoggedInUserName());
            st.setString(3, caption);
            st.setInt(4, (int) f.length() / 1024);
            st.setString(5, f.getName());

            int r = st.executeUpdate();
            System.out.println((r>0)? "Successfully Posted.\n":"Failed Uploading.\n");
        }
        catch(Exception e){
            System.out.println("Error - " + e.getMessage());
        }
    }

    public void upload(){
        if (!User.isLoggedIn()) return;
        System.out.print("Enter File Path (e.g., 'C:/myvideo.mp4'): ");
        String path = sc.nextLine();
        File f = new File(path);
        System.out.print("Enter Reel Caption: ");
        String caption = sc.nextLine();

        try{
            Connection con = DBConnection.getConnection();
            PreparedStatement st = con.prepareStatement("INSERT INTO Vedio(User_id,Username,Caption,Size_kb,File_name) VALUES(?,?,?,?,?)");
            st.setInt(1,User.getLoggedInUserId());
            st.setString(2,User.getLoggedInUserName());
            st.setString(3, caption);
            st.setInt(4, (int) f.length() / 1024);
            st.setString(5, f.getName());

            int r = st.executeUpdate();
            System.out.println((r>0)? "Reel Successfully Posted.\n":"Failed Uploading.\n");
        }
        catch(Exception e){
            System.out.println("Error - " + e.getMessage());
        }
    }

    public void scrollReels(int userId) {
        if (!User.isLoggedIn()) return;
        while (true) {
            try (Connection con = DBConnection.getConnection()) {
                String query = "SELECT * FROM Reels ORDER BY RANDOM() LIMIT 1";
                PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int reelId = rs.getInt("Id");
                    String caption = rs.getString("Caption");

                    System.out.println("========================");
                    System.out.println("Reel ID : " + reelId);
                    System.out.println("Reel Caption : " + caption);
                    System.out.println("File Path (Local Ref): " + rs.getString("File_path"));
                    System.out.println("========================");
                    System.out.println("--- Choose an Option ---");
                    System.out.println("1. Like");
                    System.out.println("2. Save");
                    System.out.println("3. Scroll");
                    System.out.println("4. Exit");
                    System.out.print("Enter Choice : ");

                    int z = -1;
                    try { z = Integer.parseInt(sc.nextLine()); } catch (NumberFormatException e) { System.out.println("Invalid input. Try again."); continue; }


                    if (z == 1) {
                        try{
                            PreparedStatement insert = con.prepareStatement("INSERT INTO Likes(Username,User_id,Reel_id) VALUES(?,?,?)");
                            insert.setString(1,User.getLoggedInUserName());
                            insert.setInt(2,userId);
                            insert.setInt(3,reelId);
                            insert.executeUpdate();
                            System.out.println("Reel Liked.\n");
                        } catch (SQLException e) {
                            System.out.println("Reel Has Been Already Liked.\n");
                        }
                    }
                    else if (z == 2) {
                        try{
                            PreparedStatement insert =
                                    con.prepareStatement("INSERT INTO Saves(Username,User_id,Reel_id) VALUES(?,?,?)");
                            insert.setString(1,User.getLoggedInUserName());
                            insert.setInt(2,userId);
                            insert.setInt(3,reelId);
                            insert.executeUpdate();
                            System.out.println("Reel Saved.\n");
                        } catch (SQLException e) {
                            System.out.println("Reel Has Been Already Saved.\n");
                        }
                    }
                    else if (z == 3) {
                        System.out.println("Skipping to Next Reel...");
                    }
                    else if (z == 4) {
                        System.out.println("Exiting Reel Section...");
                        break;
                    } else {
                        System.out.println("Invalid Option. Try again.");
                    }
                } else {
                    System.out.println("No Reels Available.");
                    break;
                }

            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                break;
            }
        }
    }

    public void viewLikedReels(int userId) {
        if (!User.isLoggedIn()) return;
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement st = con.prepareStatement(
                    "SELECT R.ID, R.Caption FROM Reels R JOIN Likes L ON R.ID = L.Reel_id WHERE L.User_id = ?");
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            System.out.println("\n--- Liked Reels ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Reel ID: " + rs.getInt("ID") + " | Caption: " + rs.getString("Caption"));
            }
            if(!found) System.out.println("You have no liked reels.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewSavedReels(int userId) {
        if (!User.isLoggedIn()) return;
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement st = con.prepareStatement(
                    "SELECT R.ID, R.Caption FROM Reels R JOIN Saves S ON R.ID = S.Reel_id WHERE S.User_id = ?");
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            System.out.println("\n--- Saved Reels ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Reel ID: " + rs.getInt("ID") + " | Caption: " + rs.getString("Caption"));
            }
            if(!found) System.out.println("You have no saved reels.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class Games {
    Scanner sc = new Scanner(System.in);

    private void updateStats(int userId, String username, boolean won) throws SQLException {
        String gameName = "Number Guess";
        String insertGame = "INSERT INTO Games (user_id, username, game_name, played_at, won) VALUES (?, ?, ?, datetime('now', 'localtime'), ?)";
        String upsertStats;

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(insertGame);
            ps.setInt(1, userId);
            ps.setString(2, username);
            ps.setString(3, gameName);
            ps.setInt(4, won ? 1 : 0);
            ps.executeUpdate();

            if (won) {
                PreparedStatement check = con.prepareStatement("SELECT * FROM Stats WHERE user_id = ?");
                check.setInt(1, userId);
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    upsertStats = "UPDATE Stats SET streak = streak + 1, points = points + 10, last_played_date = date('now', 'localtime') WHERE user_id = ?";
                    PreparedStatement update = con.prepareStatement(upsertStats);
                    update.setInt(1, userId);
                    update.executeUpdate();
                } else {
                    upsertStats = "INSERT INTO Stats (user_id, username, streak, points, last_played_date) VALUES (?, ?, 1, 10, date('now', 'localtime'))";
                    PreparedStatement insert = con.prepareStatement(upsertStats);
                    insert.setInt(1, userId);
                    insert.setString(2, username);
                    insert.executeUpdate();
                }
            } else {
                PreparedStatement check = con.prepareStatement("SELECT * FROM Stats WHERE user_id = ?");
                check.setInt(1, userId);
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    upsertStats = "UPDATE Stats SET streak = 0, last_played_date = date('now', 'localtime') WHERE user_id = ?";
                    PreparedStatement update = con.prepareStatement(upsertStats);
                    update.setInt(1, userId);
                    update.executeUpdate();
                } else {
                    upsertStats = "INSERT INTO Stats (user_id, username, streak, points, last_played_date) VALUES (?, ?, 0, 0, date('now', 'localtime'))";
                    PreparedStatement insert = con.prepareStatement(upsertStats);
                    insert.setInt(1, userId);
                    insert.setString(2, username);
                    insert.executeUpdate();
                }
            }
        }
    }

    public void play(int userId, String username) {
        if (!User.isLoggedIn()) return;
        System.out.println("\n--- Welcome to the Quick Guessing Game! ---");
        System.out.println("Try to guess the number (1-100) before the 30 seconds timer runs out.");

        Random rand = new Random();
        int targetNumber = rand.nextInt(100) + 1;
        int guesses = 0;
        final long DURATION_SECONDS = 30;
        final long DURATION_MILLIS = DURATION_SECONDS * 1000;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + DURATION_MILLIS;
        boolean won = false;

        while (System.currentTimeMillis() < endTime) {
            long remainingTime = (endTime - System.currentTimeMillis()) / 1000;
            if (remainingTime <= 0) break;

            System.out.println("\n[TIME LEFT: " + remainingTime + " seconds]");
            System.out.print("Enter your guess: ");

            int guess = -1;
            try {
                String input = sc.nextLine();
                guess = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid guess. Please enter a valid number.");
                continue;
            }

            guesses++;

            if (guess == targetNumber) {
                System.out.println("\n🎉 CONGRATULATIONS! You guessed the number " + targetNumber +
                        " in " + guesses + " guesses!");
                long timeTaken = DURATION_SECONDS - remainingTime;
                System.out.println("Total Time Taken: " + timeTaken + " seconds.");
                won = true;
                break;
            } else if (guess < targetNumber) {
                System.out.println("Too Low. Try a higher number.");
            } else {
                System.out.println("Too High. Try a lower number.");
            }

            try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        if (!won) {
            System.out.println("\n⌛ TIME'S UP! Game Over.");
            System.out.println("The number was: " + targetNumber);
        }

        try {
            updateStats(userId, username, won);
        } catch (SQLException e) {
            System.out.println("Error saving game session or updating stats: " + e.getMessage());
        }
    }

    public void showLeaderboard() {
        if (!User.isLoggedIn()) return;
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT username, points, streak, last_played_date FROM Stats ORDER BY points DESC, streak DESC LIMIT 10";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Top 10 Game Leaderboard ---");
            System.out.printf("%-5s %-15s %-8s %-8s %s\n", "Rank", "Username", "Points", "Streak", "Last Played");
            System.out.println("-----------------------------------------------------");

            int rank = 1;
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-15s %-8d %-8d %s\n",
                        rank++,
                        rs.getString("username"),
                        rs.getInt("points"),
                        rs.getInt("streak"),
                        rs.getString("last_played_date"));
            }
            if (!found) {
                System.out.println("Leaderboard is empty. Play a game to join!");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching leaderboard: " + e.getMessage());
        }
    }
}

class Game_Stats extends Games {}

class Wellbeing {
    Scanner sc = new Scanner(System.in);

    public void trackSession(int userId) {
        if (!User.isLoggedIn()) return;
        System.out.print("Enter session duration in minutes: ");
        int duration = -1;
        try { duration = Integer.parseInt(sc.nextLine()); } catch (NumberFormatException e) { System.out.println("Invalid input. Session not tracked."); return; }

        if (duration <= 0) {
            System.out.println("Duration must be positive.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String query = "INSERT INTO Wellbeing (User_id, Session_date, Duration_minutes) VALUES (?, date('now', 'localtime'), ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, userId);
            ps.setInt(2, duration);
            ps.executeUpdate();
            System.out.println("Wellbeing session tracked successfully! (" + duration + " minutes)");
        } catch (SQLException e) {
            System.out.println("Error tracking session: " + e.getMessage());
        }
    }

    public void viewSummary(int userId) {
        if (!User.isLoggedIn()) return;
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT Session_date, Duration_minutes FROM Wellbeing WHERE User_id = ? ORDER BY Session_date DESC LIMIT 7";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Last 7 Wellbeing Sessions ---");
            boolean found = false;
            int totalDuration = 0;

            while (rs.next()) {
                found = true;
                int duration = rs.getInt("Duration_minutes");
                totalDuration += duration;
                System.out.println("Date: " + rs.getString("Session_date") + " | Duration: " + duration + " minutes");
            }

            if (found) {
                System.out.println("---------------------------------");
                System.out.println("Total duration in last 7 entries: " + totalDuration + " minutes");
            } else {
                System.out.println("No wellbeing sessions tracked yet.");
            }
        } catch (SQLException e) {
            System.out.println("Error viewing summary: " + e.getMessage());
        }
    }
}

class Message{
    static Scanner sc = new Scanner(System.in);
    public void sendMessage(int senderId) {
        if (!User.isLoggedIn()) return;
        try (Connection conn = DBConnection.getConnection()) {

            System.out.print("Enter the Username: ");
            String receiverUsername = sc.nextLine().trim();

            PreparedStatement getReceiverId = conn.prepareStatement("SELECT Id FROM Users WHERE Username = ?");
            getReceiverId.setString(1, receiverUsername);
            ResultSet rs = getReceiverId.executeQuery();

            if (!rs.next()) {
                System.out.println("User not found!");
                return;
            }
            int receiverId = rs.getInt("Id");
            System.out.print("Enter your message: ");
            String content = sc.nextLine();

            PreparedStatement send = conn.prepareStatement(
                    "INSERT INTO Messages (Sender_id, Receiver_id, Sender_name, Content) VALUES (?, ?, ?, ?)");
            send.setInt(1, senderId);
            send.setInt(2, receiverId);
            send.setString(3, User.getLoggedInUserName());
            send.setString(4, content);
            send.executeUpdate();

            System.out.println("Message Sent Successfully!");
        } catch (SQLException e) {
            System.out.println("Error Sending Message: " + e.getMessage());
        }
    }

    public void inbox() {
        if (!User.isLoggedIn()) return;
        try (Connection conn = DBConnection.getConnection()) {
            int userId = User.getLoggedInUserId();
            String requestQuery = "SELECT fr.Id, u.Username, u.Email_id, u.Bio " +
                    "FROM FriendRequests fr " +
                    "JOIN Users u ON fr.Sender_id = u.Id " +
                    "WHERE fr.Receiver_id = ? AND fr.Status = 'PENDING'";
            PreparedStatement requestStmt = conn.prepareStatement(requestQuery);
            requestStmt.setInt(1, userId);
            ResultSet reqRs = requestStmt.executeQuery();

            boolean hasRequests = false;
            while (reqRs.next()) {
                hasRequests = true;
                int requestId = reqRs.getInt("Id");
                String username = reqRs.getString("Username");
                String email = reqRs.getString("Email_id");
                String bio = reqRs.getString("Bio");

                System.out.println("\nFriend Request from:");
                System.out.println("--------------------------");
                System.out.println("Username: " + username);
                System.out.println("Email: " + email);
                System.out.println("Bio: " + bio);
                System.out.println("--------------------------\n");
                System.out.println("1. Accept");
                System.out.println("2. Decline");
                System.out.print("Enter your choice: ");

                int choice = -1;
                try { choice = Integer.parseInt(sc.nextLine()); } catch (NumberFormatException e) { System.out.println("Invalid input. Skipping request."); continue; }

                if (choice == 1) {
                    String updateRequest = "UPDATE FriendRequests SET Status = 'ACCEPTED' WHERE Id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateRequest);
                    updateStmt.setInt(1, requestId);
                    updateStmt.executeUpdate();

                    String updateFriendCount = "UPDATE Users SET Friends = Friends + 1 WHERE Id IN (" +
                            "(SELECT Sender_id FROM FriendRequests WHERE Id = ?), ?)";
                    PreparedStatement friendCountStmt = conn.prepareStatement(updateFriendCount);
                    friendCountStmt.setInt(1, requestId);
                    friendCountStmt.setInt(2, userId);
                    friendCountStmt.executeUpdate();

                    System.out.println("Friend request accepted.\n");
                } else if (choice == 2) {
                    String decline = "UPDATE FriendRequests SET Status = 'REJECTED' WHERE Id = ?";
                    PreparedStatement declineStmt = conn.prepareStatement(decline);
                    declineStmt.setInt(1, requestId);
                    declineStmt.executeUpdate();
                    System.out.println("Friend request Declined.\n");
                } else {
                    System.out.println("Invalid option. Skipping request.");
                }
            }
            if (!hasRequests) {
                System.out.println("No Pending Friend Requests.\n");
            }

            String messageQuery = "SELECT Sender_name, Content FROM Messages WHERE Receiver_id = ?";
            PreparedStatement msgStmt = conn.prepareStatement(messageQuery);
            msgStmt.setInt(1, userId);
            ResultSet msgRs = msgStmt.executeQuery();

            boolean hasMessages = false;
            System.out.println("\n--- Messages ---");
            while (msgRs.next()) {
                hasMessages = true;
                System.out.println("@" + msgRs.getString("Sender_name") + " : " + msgRs.getString("Content"));
            }

            if (!hasMessages) {
                System.out.println("Inbox is empty.");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching inbox: " + e.getMessage());
        }
    }
}

class Search {
    public void display(String name) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement call = con.prepareStatement("SELECT Id,Username,Email_id,Bio FROM Users WHERE Username = ?");
            call.setString(1, name);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                int searchedUserId = rs.getInt("Id");
                UserProfile profile = new UserProfile(rs.getString("Username"), rs.getString("Email_id"), rs.getString("Bio"));
                profile.displayProfile();

                PreparedStatement post = con.prepareStatement("SELECT Caption FROM Image WHERE User_id = ?");
                post.setInt(1, searchedUserId);
                ResultSet posts = post.executeQuery();
                System.out.println("\nPosts:");
                boolean hasPosts = false;
                while (posts.next()) {
                    hasPosts = true;
                    System.out.println("→ " + posts.getString("Caption"));
                } if (!hasPosts) System.out.println("No Posts Uploaded.");

                PreparedStatement reel = con.prepareStatement("SELECT Caption FROM Vedio WHERE User_id = ?");
                reel.setInt(1, searchedUserId);
                ResultSet reels = reel.executeQuery();
                System.out.println("\nReels:");
                boolean hasReels = false;
                while (reels.next()) {
                    hasReels = true;
                    System.out.println("→ " + reels.getString("Caption"));
                } if (!hasReels) System.out.println("No reels uploaded.");}

            else System.out.println("No profile found with Username: " + name);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class MyQueue<T> {
    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; this.next = null;
        }
    }

    private Node<T> front, rear;
    public MyQueue() { front = rear = null;}
    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (rear == null) front = rear = newNode;
        else {
            rear.next = newNode;
            rear = newNode;
        }
    }
    public T dequeue() {
        if (front == null) return null;
        T data = front.data;
        front = front.next;
        if (front == null) rear = null;
        return data;
    }
    public boolean isEmpty() { return front == null;
    }
}

class MyLinkedList<T> {
    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) {
            this.data = data;
        }
    }
    private Node head;
    private int size = 0;
    public void addFirst(T data) {
        Node<T> node = new Node<>(data);
        node.next = head;
        head = node;
        size++;
    }
    public void printAll() {
        if (head == null) {
            System.out.println("Inbox is Empty.");
            return;
        }
        Node<T> temp = head;
        while (temp != null) {
            System.out.println("→ " + temp.data);
            temp = temp.next;
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }
}

public class Insta_Lite{
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        DBConnection.initializeDatabase();

        User user = new User();
        Search search = new Search();
        Account acc = new Account();
        Message msg = new Message();
        Reels_Management reel = new Reels_Management();
        Games games = new Games();
        Wellbeing wellbeing = new Wellbeing();
        boolean b = true;

        do{
            if(!User.isLoggedIn()) {
                System.out.println("\n ----------- WELCOME TO INSTA_LITE CONSOLE  ----------- \n");
                System.out.println("1. Create Account");
                System.out.println("2. Login");
                System.out.println("3. Forget Password");
                System.out.println("4. Exit");
                System.out.print("Enter Choice : ");

                int choice = -1;
                try { choice = Integer.parseInt(sc.nextLine()); } catch (NumberFormatException e) { System.out.println("Invalid input. Try again."); continue; }

                System.out.println();
                switch (choice) {
                    case 1: user.createAccount(); break;
                    case 2: user.log_In(); break;
                    case 3: acc.forgotPassword(); break;
                    case 4: b=false; break;
                    default: System.out.println("Invalid Choice.");
                }
            }
            else{
                System.out.println("==========================");
                System.out.println("1. Create A Post");
                System.out.println("2. Upload Reel");
                System.out.println("3. Scroll Reels");
                System.out.println("4. Send a Message");
                System.out.println("5. Check Inbox");
                System.out.println("6. Search Profile");
                System.out.println("7. Send A Friend Request");
                System.out.println("8. View Liked Reels");
                System.out.println("9. View Saved Reels");
                System.out.println("10. Delete Account");
                System.out.println("11. Deactivate Account");
                System.out.println("12. Log Out");
                System.out.println("13. Play a Quick Game 🕹️");
                System.out.println("14. Show Game Leaderboard 🏆");
                System.out.println("15. Track Wellbeing Session 🧘");
                System.out.println("16. View Wellbeing Summary");
                System.out.println("17. EXIT");
                System.out.println("==========================");
                System.out.print("Enter Choice : ");

                int choice = -1;
                try { choice = Integer.parseInt(sc.nextLine()); } catch (NumberFormatException e) { System.out.println("Invalid input. Try again."); continue; }

                switch (choice) {
                    case 1: reel.post();break;
                    case 2: reel.upload();break;
                    case 3: reel.scrollReels(User.getLoggedInUserId());break;
                    case 4: msg.sendMessage(User.getLoggedInUserId());break;
                    case 5: msg.inbox();break;
                    case 6:
                        System.out.print("Enter Username to Search: ");
                        String name = sc.nextLine();
                        search.display(name);
                        break;
                    case 7:
                        System.out.print("Enter Username to Send Friend Request: ");
                        String receiverUsername = sc.nextLine();
                        int receiverId = user.check(receiverUsername);

                        if (receiverId == -1) {
                            System.out.println("User not found.");
                        } else if (receiverId == User.getLoggedInUserId()) {
                            System.out.println("You cannot send a friend request to yourself.");
                        } else {
                            user.sendRequest(User.getLoggedInUserId(), receiverId);
                        }
                        break;
                    case 8: reel.viewLikedReels(User.getLoggedInUserId());break;
                    case 9: reel.viewSavedReels(User.getLoggedInUserId());break;
                    case 10: acc.delete();break;
                    case 11: acc.deactivate();break;
                    case 12: user.logout();break;
                    case 13: games.play(User.getLoggedInUserId(), User.getLoggedInUserName()); break;
                    case 14: games.showLeaderboard(); break;
                    case 15: wellbeing.trackSession(User.getLoggedInUserId()); break;
                    case 16: wellbeing.viewSummary(User.getLoggedInUserId()); break;
                    case 17:
                        System.out.println("Goodbye! ");
                        b = false;
                        break;
                    default: System.out.println("Invalid Choice.");
                }
            }
        }while(b);
    }
}