import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.Date;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

// ------------------------------- DB CONNECTION -------------------------------
class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/Instalite";
    private static final String USER = "root";
    private static final String PASS = "";
    private DBConnection() {} //Abstraction

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

// ------------------------------- USER PROFILE (Encapsulation) -------------------------------
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

// ------------------------------- SOCIAL FEATURES (Abstraction) -------------------------------
interface SocialFeatures {
    void sendRequest(int senderId, int receiverId);
    void sendMessage(int senderId);
}

// ------------------------------- USER -------------------------------
class User implements SocialFeatures {
    Scanner sc = new Scanner(System.in);

    // Encapsulation - Protects sensitive information
    private static int loggedInUserId = -1;
    private static String loggedInUserName = "null";

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

        String pass;
        while (true) {
            System.out.print("Set Password : ");
            pass = sc.nextLine();

            if (pass.length() == 6) {
                int digit = 0, specialChar = 0;
                for (char ch : pass.toCharArray()) {
                    if (Character.isDigit(ch)) digit++;
                    else if (!Character.isLetterOrDigit(ch)) specialChar++;
                }

                if (digit == 5 && specialChar == 1) {
                    try (Connection con = DBConnection.getConnection()) {
                        PreparedStatement ps = con.prepareStatement("SELECT Id FROM Users WHERE Password = ?");
                        ps.setString(1, pass);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) System.out.println("This password already exists. Please choose another.");
                        else break;
                    } catch (SQLException e) {
                        System.out.println("Error checking password uniqueness: " + e.getMessage());
                    }
                }
            }
            System.out.println("Invalid Input, Please enter 6 chars (5 digits + 1 special).");
        }

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
            int Otp = sc.nextInt();
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

        System.out.print("Enter BIO : "); sc.nextLine();
        String bio = sc.nextLine();

        try (Connection con = DBConnection.getConnection()) {
            String Create = "Insert into Users(Username,Password,Email_id,Mobile_no,Bio,is_active) VALUES(?,?,?,?,?,1)";
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
            PreparedStatement log = conn.prepareStatement(query);
            log.setString(1, username);
            log.setString(2, pass);
            ResultSet rs = log.executeQuery();
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

    // Polymorphism
    @Override
    public void sendRequest(int senderId, int receiverId){
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement check = con.prepareStatement("SELECT * FROM FriendRequests WHERE Sender_id = ? " +
                    " AND Sender_name = ? AND Receiver_id = ? AND Status = 'PENDING'");
            check.setInt(1, senderId);
            check.setString(2,User.getLoggedInUserName());
            check.setInt(3, receiverId);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                System.out.println("Friend request Already Sent.\n");
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
        try {
            Connection con = DBConnection.getConnection();
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
        email = email.toLowerCase();
        return email.contains("@") &&
                (email.endsWith("gmail.com") || email.endsWith("yahoo.com") || email.endsWith("hotmail.com"));
    }

    @Override
    public void sendMessage(int senderId) {
        new Message().sendMessage(senderId);
    }
}

// ------------------------------- CONTENT CLASS -------------------------------
// Abstraction + Inheritance - Parent class for Post and Reel
abstract class Content {
    protected int userId;
    protected String username;
    protected String caption;
    protected File file;

    public Content(int userId, String username, String caption, File file) {
        this.userId = userId;
        this.username = username;
        this.caption = caption;
        this.file = file;
    }

    public abstract void upload();
}

// Inheritance(Class Share Structure from Other and Remove Duplicates of Code) +
// Polymorphism(Same method name = different behavior)

class ImagePost extends Content {
    public ImagePost(int userId, String username, String caption, File file) {
        super(userId, username, caption, file);
    }

    @Override
    public void upload() {
        try (FileInputStream fr = new FileInputStream(file)) {
            Connection con = DBConnection.getConnection();
            PreparedStatement st = con.prepareStatement("Insert INTO Image(User_id,Username,Image,Caption,Size_kb,File_name) VALUES(?,?,?,?,?,?)");
            st.setInt(1,userId);
            st.setString(2,username);
            st.setBinaryStream(3, fr, (int) file.length());
            st.setString(4, caption);
            st.setInt(5, (int) file.length() / 1024);
            st.setString(6, file.getName());
            st.executeUpdate();
            System.out.println("Successfully Posted.\n");
        } catch(Exception e){
            System.out.println("Error - " + e.getMessage());
        }
    }
}

class ReelContent extends Content {
    public ReelContent(int userId, String username, String caption, File file) {
        super(userId, username, caption, file);
    }

    @Override
    public void upload() {
        try (FileInputStream fr = new FileInputStream(file)) {
            Connection con = DBConnection.getConnection();
            PreparedStatement st = con.prepareStatement("Insert INTO Vedio(User_id,Username,Vedio,Caption,Size_kb,File_name) VALUES(?,?,?,?,?,?)");
            st.setInt(1,userId);
            st.setString(2,username);
            st.setBinaryStream(3, fr, (int) file.length());
            st.setString(4, caption);
            st.setInt(5, (int) file.length() / 1024);
            st.setString(6, file.getName());
            st.executeUpdate();
            System.out.println("Reel Successfully Posted.\n");
        } catch(Exception e){
            System.out.println("Error - " + e.getMessage());
        }
    }
}

// ------------------------------- ACCOUNT ----------------------------
class Account{
    static Scanner sc = new Scanner(System.in);
    public void delete(){
        if (!User.isLoggedIn()) return;
        System.out.print("Are you sure you want to delete your account permanently? (yes/no)");
        String confirm = sc.nextLine();
        if (!confirm.equalsIgnoreCase("Yes")) {
            System.out.println("Deletion Cancelled.\n"); return;
        }
        try { Connection con = DBConnection.getConnection();
            CallableStatement cs = con.prepareCall("{CALL DeleteUserProc(?)}");
            cs.setString(1, User.getLoggedInUserName());
            cs.execute();
            System.out.println("Account Deleted.\n");
            User.logout();
        }
        catch(SQLException e){
            System.out.println("Error - " + e.getMessage());
        }
    }

    public void deactivate(){
        if (!User.isLoggedIn()) return;
        System.out.print("Are you sure you want to deactivate your account permanently? (yes/no)");
        String confirm = sc.nextLine();
        if (!confirm.equalsIgnoreCase("Yes")) {
            System.out.println("Deactivation Cancelled.\n"); return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement deactivate = conn.prepareStatement("UPDATE Users SET is_active = 0 WHERE Username = ?");
            deactivate.setString(1, User.getLoggedInUserName());

            System.out.print("Are you sure you want to deactivate your account permanently? (yes/no)");
            String conf = sc.nextLine();
            if (!conf.equalsIgnoreCase("Yes")) {
                System.out.println("Deactivation Cancelled.\n"); return;
            }

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
                // \\d+ is a Regular Func which only allow digits in String and + for one or more
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
            int Otp = sc.nextInt();
            if (Otp == Pass) {
                System.out.println("OTP Verified Successfully!");

                try{ Connection con = DBConnection.getConnection();
                    PreparedStatement check = con.prepareStatement("Select Username from Users where Username = ?");
                    check.setString(1,username);
                    ResultSet r = check.executeQuery();
                    if(r.next()) {
                        String user = r.getString("Username");
                        sc.nextLine();

                        String pass;
                        while (true) {
                            System.out.print("Set Password : ");
                            pass = sc.nextLine();

                            if (pass.length() == 6) {
                                int digit = 0, specialChar = 0;
                                for (char ch : pass.toCharArray()) {
                                    if (Character.isDigit(ch)) digit++;
                                    else if (!Character.isLetterOrDigit(ch)) specialChar++;
                                }

                                if (digit == 5 && specialChar == 1) {
                                    try {
                                        PreparedStatement ps = con.prepareStatement("SELECT Id FROM Users WHERE Password = ?");
                                        ps.setString(1, pass);
                                        ResultSet rs = ps.executeQuery();
                                        if (rs.next()) System.out.println("This password already exists. Please choose another.");
                                        else break;
                                    } catch (SQLException e) {
                                        System.out.println("Error checking password uniqueness: " + e.getMessage());
                                    }
                                }
                            }
                            System.out.println("Invalid Input, Please enter 6 chars (5 digits + 1 special).");
                        }

                        PreparedStatement up = con.prepareStatement("Update Users set Password = ? where Username = ?");
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
            return;
        }
    }
}


// ------------------------------- REELS ------------------------------
class Reels_Management{
    static Scanner sc = new Scanner(System.in);

    public void post(){
        System.out.print("Enter File Path : ");
        String path = sc.nextLine();
        File f = new File(path);
        System.out.print("Enter Image Caption: ");
        String caption = sc.nextLine();

        try{FileInputStream fr = new FileInputStream(path);
            Connection con = DBConnection.getConnection();

            PreparedStatement st = con.prepareStatement("Insert INTO Image(User_id,Username,Image,Caption,Size_kb,File_name) VALUES(?,?,?,?,?,?)");
            st.setInt(1,User.getLoggedInUserId());
            st.setString(2,User.getLoggedInUserName());
            st.setBinaryStream(3, fr, (int) f.length());
            st.setString(4, caption);
            st.setInt(5, (int) f.length() / 1024);
            st.setString(6, f.getName());
            int r = st.executeUpdate();
            System.out.println((r>0)? "Successfully Posted\n.":"Failed Uploading\n");
        }
        catch(Exception e){
            System.out.println("Error - " + e.getMessage());
        }
    }

    public void upload(){
        System.out.print("Enter File Path : ");
        String path = sc.nextLine();
        File f = new File(path);
        System.out.print("Enter Reel Caption: ");
        String caption = sc.nextLine();

        try{ FileInputStream fr = new FileInputStream(path);
            Connection con = DBConnection.getConnection();
            PreparedStatement st = con.prepareStatement("Insert INTO Vedio(User_id,Username,Vedio,Caption,Size_kb,File_name) VALUES(?,?,?,?,?,?)");
            st.setInt(1,User.getLoggedInUserId());
            st.setString(2,User.getLoggedInUserName());
            st.setBinaryStream(3, fr, (int) f.length());
            st.setString(4, caption);
            st.setInt(5, (int) f.length() / 1024);
            st.setString(6, f.getName());
            int r = st.executeUpdate();
            System.out.println((r>0)? "Reel Successfully Posted\n.":"Failed Uploading\n");
        }
        catch(Exception e){
            System.out.println("Error - " + e.getMessage());
        }
    }

    public void scrollReels(int userId) {
        while (true) {
            try {
                Connection con = DBConnection.getConnection();
                String query = "SELECT * FROM Reels ORDER BY RAND() LIMIT 1";
                PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int reelId = rs.getInt("Id");
                    String caption = rs.getString("Caption");

                    System.out.println("========================");
                    System.out.println("Reel Id : " + reelId);
                    System.out.println("Reel Caption : " + caption);
                    System.out.println("========================");

                    System.out.println("--- Choose an Option ---");
                    System.out.println("1. Like");
                    System.out.println("2. Save");
                    System.out.println("3. Scroll");
                    System.out.println("4. Exit");
                    System.out.print("Enter Choice : ");
                    int z = sc.nextInt();
                    sc.nextLine();

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
                            PreparedStatement insert = con.prepareStatement("INSERT INTO Saves(Username,User_id,Reel_id) VALUES(?,?,?)");
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
            }
        }
    }

    public void viewLikedReels(int userId) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement st = con.prepareStatement(
                    "SELECT R.ID, R.Caption FROM Reels R JOIN Likes L ON R.ID = L.Reel_id WHERE L.User_id = ?");
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            System.out.println("\n--- Liked Reels ---");
            while (rs.next()) {
                System.out.println("Reel ID: " + rs.getInt("ID") + " | Caption: " + rs.getString("Caption"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewSavedReels(int userId) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement st = con.prepareStatement(
                    "SELECT R.ID, R.Caption FROM Reels R JOIN Saves S ON R.ID = S.Reel_id WHERE S.User_id = ?");
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            System.out.println("\n--- Saved Reels ---");
            while (rs.next()) {
                System.out.println("Reel ID: " + rs.getInt("ID") + " | Caption: " + rs.getString("Caption"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void digitalWellbeingReport(int userId) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT IFNULL(SUM(Duration_minutes),0) FROM Wellbeing WHERE User_id=? AND Session_date=CURDATE()");
            ps1.setInt(1, userId);
            ResultSet rs1 = ps1.executeQuery();
            int today = 0;
            if (rs1.next()) today = rs1.getInt(1);

            PreparedStatement ps2 = con.prepareStatement(
                    "SELECT IFNULL(SUM(Duration_minutes),0) FROM Wellbeing WHERE User_id=? AND Session_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)");
            ps2.setInt(1, userId);
            ResultSet rs2 = ps2.executeQuery();
            int week = 0;
            if (rs2.next()) week = rs2.getInt(1);

            System.out.println("---------- Digital Wellbeing Report ---------\n");
            System.out.println("Today: " + today + " min");
            System.out.println("This Week: " + week + " min");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class MyQueue<T> {
    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; this.next = null; }
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
    public boolean isEmpty() { return front == null; }
}

class MyLinkedList<T> {
    private static class Node<T> {
        T data; Node<T> next;
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
    }
    public void printAll() {
        if (head == null) {
            System.out.println("Inbox is Empty."); return;
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

// ------------------------------- MESSAGE ---------------------------
class Message{
    static Scanner sc = new Scanner(System.in);
    public void sendMessage(int senderId) {
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
        try (Connection conn = DBConnection.getConnection()) {
            int userId = User.getLoggedInUserId();

            String requestQuery = "SELECT fr.Id, u.Username, u.Email_id, u.Bio " +
                    "FROM FriendRequests fr " +
                    "JOIN Users u ON fr.Sender_id = u.Id " +
                    "WHERE fr.Receiver_id = ? AND fr.Status = 'Pending'";
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

                int choice = new Scanner(System.in).nextInt();
                if (choice == 1) {
                    String updateRequest = "UPDATE FriendRequests SET Status = 'Accepted' WHERE Id = ?";
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
                    String decline = "UPDATE FriendRequests SET Status = 'Rejected' WHERE Id = ?";
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

// ------------------------------- SEARCH -----------------------------
class Search {
    public void display(String name) {
        Scanner sc = new Scanner(System.in);

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement call = con.prepareStatement("Select Id,Username,Email_id,Bio from Users where Username = ?");
            call.setString(1, name);
            ResultSet rs = call.executeQuery();

            if (rs.next()) {
                int searchedUserId = rs.getInt("Id");

                System.out.println("Username: " + rs.getString("Username"));
                System.out.println("Email: " + rs.getString("Email_id"));
                System.out.println("Bio: " + rs.getString("Bio"));

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

//======================================== INSTALITE GAMES ==============================================

interface Game {
    String getName();
    boolean play();
}
abstract class Time implements Game {
    protected final Scanner sc = new Scanner(System.in);
    protected final Random rand = new Random();

    protected boolean startCountdown(int seconds, boolean[] timeUp) {
        Timer timer = new Timer(true); // daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            int sec = seconds;
            public void run() {
                if (sec >= 0) {
                    System.out.print("\rTime Left: " + sec + "s   ");
                    sec--;
                } else {
                    timeUp[0] = true;
                    System.out.println("\n⏳ Time Over! Game Over.");
                    timer.cancel();
                }
            }
        }, 0, 1000);
        return true;
    }

    protected boolean within(long start, long limit) {
        long leftt = (System.currentTimeMillis() - start) / 1000L;
        return leftt < limit;
    }

    protected long leftTime(long start, long limit) {
        long left = (System.currentTimeMillis() - start) / 1000L;
        long leftt = limit - left;
        return Math.max(0, left);
    }

    protected void displayTime(long start, long limit) {
        long left = leftTime(start, limit);
        System.out.print(String.format("  [⏳ %02d:%02d]", left / 90, left % 90));
    }

    protected void pause(long ms) {
        try { Thread.sleep(ms); }  catch (InterruptedException ignored) {}
    }

    protected String shuffle(String w) {
        List<Character> chars = new ArrayList<>();
        for (char c : w.toCharArray())
            chars.add(c);
        Collections.shuffle(chars, rand);
        StringBuilder sb = new StringBuilder();
        for (char c : chars)
            sb.append(c);
        return sb.toString();
    }
}

// ------------------------------- GAME #1: Word Scramble -------------------------------
class Wordscramble extends Time {
    static String[] Words = {
            "banana","planet","silver","puzzle","object","stream","camera","galaxy",
            "random","little","orange","bridge","forest","animal","memory"
    };
    public String getName() {
        return "Word Scramble";
    }

    public boolean play() {
        long start = System.currentTimeMillis();
        String target = Words[rand.nextInt(Words.length)];
        String scramble;
        do{
            scramble = shuffle(target);
        }while(scramble.equals(target));

        System.out.println("\nUnscramble the word:");
        System.out.println(">> " + scramble);
        System.out.print("Your Answer: ");
        displayTime(start, 90); System.out.println();
        String ans = sc.nextLine().trim();

        boolean win = ans.equalsIgnoreCase(target) && within(start, 90);
        System.out.println(win ? "Correct!" : "Wrong. Answer was: " + target);
        return win;
    }
}

// ------------------------------- GAME #2: Maths Blitz -------------------------------
class MathsBlitz extends Time {
    public String getName() {
        return "Maths Blitz";
    }

    public boolean play() {
        long start = System.currentTimeMillis();
        int correct = 0, total = 10;
        char[] ops = {'+','-','*','/'};

        System.out.println("\nSolve 10 problems in 90 seconds. Need at least 8 correct.");
        for(int i = 1; i <= total; i++) {
            if(!within(start, 90)) {
                System.out.println("Time Over!");
                break;
            }
            int a = rand.nextInt(41) + 9;  //Numbers from 9..50
            int b = rand.nextInt(41) + 9;
            char op = ops[rand.nextInt(ops.length)];
            if (op == '/') {
                int ans = Math.max(1, rand.nextInt(12)); // 1..11
                a = ans * Math.max(1, rand.nextInt(12));
                b = a / ans;
            }

            int ans = switch (op) {
                case '+' -> a + b;
                case '-' -> a - b;
                case '*' -> a * b;
                case '/' -> b != 0 ? a / b : a; // safe
                default -> 0;
            };

            System.out.print("Q" + i + ": " + a + " " + op + " " + b + " = ");
            displayTime(start, 90); System.out.print("\n= ");
            int user;
            try {
                user = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                user = Integer.MIN_VALUE;
            }
            if (user == ans) correct++;
        }
        System.out.println("Correct: " + correct + "/10");
        return correct >= 8 && within(start, 90);
    }
}

// ------------------------------- GAME #3: Memory Flip (6 numbers) -------------------------------
class MemoryFlip extends Time {
    public String getName() {
        return "Memory Flip";
    }

    public boolean play() {
        long start = System.currentTimeMillis();
        List<Integer> seq = new ArrayList<>();
        while (seq.size() < 6) {
            int v = rand.nextInt(90) + 10; // two-digit for recall
            if (!seq.contains(v)) seq.add(v);
        }
        System.out.println("\nMemorize the sequence (5 seconds):");
        System.out.println(seq);
        pause(5000);
        System.out.print("\rNow Enter the 6 Numbers Separated by Space");
        displayTime(start, 90); System.out.println();
        System.out.print("> ");

        String[] parts = sc.nextLine().trim().split("\\s+");
        if (parts.length != 6) {
            System.out.println("Need exactly 6 numbers."); return false;
        }
        boolean ok = true;
        for (int i = 0; i < 6; i++) {
            try {
                if (Integer.parseInt(parts[i]) != seq.get(i)) { ok = false; break; }
            } catch (Exception e) {
                ok = false; break;
            }
        }
        System.out.println(ok && within(start, 90) ? "Perfect memory!" : "Not matching.");
        return ok && within(start, 90);
    }
}

// ------------------------------- GAME #4: Odd One Out -------------------------------
class OddOneOut extends Time {
    private static final String[][] SETS = {
            {"Dog","Cat","Cow","Apple"},
            {"Rose","Lily","Mango","Tulip"},
            {"Car","Bus","Train","Banana"},
            {"Red","Blue","Green","Table"},
            {"Paris","London","Tokyo","Shirt"}
    };
    public String getName() {
        return "Odd One Out";
    }

    public boolean play() {
        long start = System.currentTimeMillis();
        int needed = 2, asked = 3, score = 0;

        System.out.println("\nPick the odd item. Need 2/3 to win.");
        List<Integer> bag = new ArrayList<>();
        for (int i=0;i<SETS.length;i++) bag.add(i);
        Collections.shuffle(bag, rand);

        for (int q = 0; q < asked; q++) {
            if (!within(start, 90)) {
                System.out.println("⏳ Time Over!"); break;
            }
            String[] arr = SETS[bag.get(q)];
            List<String> opts = new ArrayList<>(Arrays.asList(arr));
            int oddIndex = -1;
            oddIndex = arr.length - 1;
            Collections.shuffle(opts, rand);
            int correctIdx = opts.indexOf(arr[oddIndex]);

            System.out.println("\nQ" + (q+1) + ": Choose the odd one:");
            for (int i=0;i<opts.size();i++)
                System.out.println((i+1)+") "+opts.get(i));
            System.out.print("Your pick: ");
            displayTime(start, 90); System.out.println();

            int pick;
            try {
                pick = Integer.parseInt(sc.nextLine().trim()) - 1;
            }catch (Exception e) {
                pick = -1;
            }
            if (pick == correctIdx) { System.out.println("Correct!"); score++; }
            else System.out.println("Wrong. Odd: " + arr[oddIndex]);
        }
        boolean win = score >= needed && within(start, 90);
        System.out.println("Score: " + score + "/" + asked);
        return win;
    }
}

// ------------------------------- GAME #5: Pattern Memory (3x3) -------------------------------
class PatternMemory extends Time {
    public String getName() { return "Pattern Memory"; }

    public boolean play() {
        long start = System.currentTimeMillis();
        char[][] grid = new char[3][3];
        for (int r=0;r<3;r++)
            for (int c=0;c<3;c++) grid[r][c] = rand.nextBoolean() ? 'X' : 'O';

        System.out.println("\nMemorize 3x3 pattern (5 seconds):");
        for (int r=0;r<3;r++) {
            for (int c=0;c<3;c++) System.out.print(grid[r][c]+" ");
            System.out.println();
        }
        pause(5000);

        System.out.println("\nEnter the pattern row by row using X/O (e.g., X O X):");
        char[][] user = new char[3][3];
        for (int r=0;r<3;r++) {
            if (!within(start, 90)) {
                System.out.println("⏳ Time Over!"); return false;
            }
            System.out.print("Row "+(r+1)+": "); displayTime(start, 90); System.out.println();
            String[] parts = sc.nextLine().trim().split("\\s+");
            if (parts.length != 3) return false;
            for (int c=0;c<3;c++) {
                String p = parts[c].toUpperCase();
                if (!(p.equals("X") || p.equals("O"))) return false;
                user[r][c] = p.charAt(0);
            }
        }
        boolean same = Arrays.deepEquals(new Object[]{
                        grid[0],grid[1],grid[2]},
                new Object[]{user[0],user[1],user[2]});
        System.out.println(same && within(start, 90) ? "Matched!" : "Not matching.");
        return same && within(start, 90);
    }
}

// ------------------------------- GAME #6: Word Chain -------------------------------
class WordChain extends Time {
    private static final String[] START = {"apple","river","music","light","stone","dance","green","night"};
    public String getName() {
        return "Word Chain";
    }

    public boolean play() {
        long start = System.currentTimeMillis();
        String cur = START[rand.nextInt(START.length)];
        Set<String> used = new HashSet<>();
        used.add(cur);

        System.out.println("\nStart word: " + cur);
        System.out.println("Enter 6 valid next Words (each must start with last letter of previous). No repeats.");
        int need = 6, ok = 0;
        while (ok < need && within(start, 90)) {
            char last = cur.charAt(cur.length()-1);
            System.out.print("Word starting with '" + last + "': ");
            displayTime(start, 90); System.out.println();

            String nxt = sc.nextLine().trim().toLowerCase();
            if (nxt.length() >= 2 && nxt.charAt(0) == last && !used.contains(nxt) && nxt.matches("[a-z]+")) {
                used.add(nxt);
                cur = nxt;
                ok++;
            } else {
                System.out.println("Invalid chain word.");
            }
        }
        boolean win = ok >= need && within(start, 90);
        System.out.println(win ? "Great Chain!" : "Chain incomplete.");
        return win;
    }
}

// ------------------------------- GAME #7: Hangman (word length <= 6) -------------------------------
class Hangman extends Time {
    private static final String[] Words = {
            "apple","table","river","green","stone","chair","mouse","light","train","bridge"
    };
    public String getName() {
        return "Hangman";
    }

    public boolean play() {
        long start = System.currentTimeMillis();
        String word = Words[rand.nextInt(Words.length)];
        Set<Character> guessed = new HashSet<>();

        int lives = 6;
        System.out.println("\nGuess the word (" + word.length() + " letters). You have 6 lives.");
        while (lives > 0 && within(start, 90)) {
            StringBuilder mask = new StringBuilder();
            boolean allOpen = true;
            for (char c : word.toCharArray()) {
                if (guessed.contains(c)) mask.append(c).append(' ');
                else { mask.append("_ "); allOpen = false;
                }
            }
            System.out.println(mask.toString().trim());
            if (allOpen) { System.out.println("You solved it!");
                return true;
            }

            System.out.print("Guess a letter: ");
            displayTime(start, 90); System.out.println();

            String in = sc.nextLine().trim().toLowerCase();
            if (in.length() != 1 || !Character.isLetter(in.charAt(0))) {
                System.out.println("Enter 1 letter."); continue;
            }
            char g = in.charAt(0);
            if (guessed.contains(g)) {
                System.out.println("Already Tried.");
                continue;
            }
            guessed.add(g);
            if (word.indexOf(g) < 0) { lives--; System.out.println("Wrong! Lives left: " + lives); }
        }
        System.out.println("Word was: " + word);
        return false;
    }
}

// ------------------------------- GAME #8: Logic Puzzle (simple) -------------------------------
class LogicPuzzle extends Time {
    private static final String[][] QA = {
            {"All humans are mortal. Socrates is a human. Is Socrates mortal? (yes/no)", "yes"},
            {"If today is Monday, two days after tomorrow is? (fri/sat/sun/mon/tue/wed/thu)", "thursday"},
            {"A farmer has 17 sheep; all but 9 die. How many are left?", "9"},
            {"If a > b and b > c, is a > c? (yes/no)", "yes"},
            {"Even number plus odd number is odd or even? (odd/even)", "odd"}
    };
    public String getName() {
        return "Logic Puzzle";
    }

    public boolean play() {
        long start = System.currentTimeMillis();
        List<Integer> idx = new ArrayList<>();

        for (int i=0;i<QA.length;i++) idx.add(i);
        Collections.shuffle(idx, new Random());
        int correct = 0;
        for (int i=0;i<3;i++) {
            if (!within(start, 90)) {
                System.out.println("Time Over!"); break;
            }

            String[] q = QA[idx.get(i)];
            System.out.print("\nQ"+(i+1)+": "+q[0]+"  ");
            displayTime(start, 90); System.out.println();

            String ans = sc.nextLine().trim().toLowerCase();
            if (ans.equals(q[1])) { System.out.println("Correct"); correct++; }
            else System.out.println("Wrong");
        }
        boolean win = correct >= 2 && within(start, 90);
        System.out.println("Score: "+correct+"/3");
        return win;
    }
}

// ------------------------------- GAME MANAGER -------------------------------
class GameManager {
    private final List<Game> games = new ArrayList<>();

    public GameManager() {
        games.add(new Wordscramble());
        games.add(new MathsBlitz());
        games.add(new MemoryFlip());
        games.add(new OddOneOut());
        games.add(new PatternMemory());
        games.add(new WordChain());
        games.add(new Hangman());
        games.add(new LogicPuzzle());
    }

    private Game pickDailyGame(int userId) {
        LocalDate today = LocalDate.now();
        int idx = Math.abs(Objects.hash(userId, today)) % games.size();
        return games.get(idx);
    }

    public void playDailyGame(int userId, String username) {
        if (!canPlay(userId)) {
            System.out.println("\nYou already played today. Come back tomorrow!");
            return;
        }
        Game game = pickDailyGame(userId);
        System.out.println("\n------------🎮 Today’s Game: " + game.getName() + "------------");
        System.out.println("Only 1 Attempt So Play Wisely.");

        boolean won = game.play();
        saves(userId, username, game.getName(), won);
        if (won) {
            LeaderboardRepo.increment(userId, username, game.getName(), 10, true);
            System.out.println("Streak +1! , Win recorded.");
        } else {
            LeaderboardRepo.reset(userId);
            System.out.println("Streak reset to 0. Better luck tomorrow!");
        }
    }

    public void showLeaderboard() {
        System.out.println("\n🏆 Top 10 Players (by points, tiebreaker: streak desc, last played asc)\n");
        List<Lead> top = LeaderboardRepo.top10();
        if (top.isEmpty()) {
            System.out.println("No plays yet.");
            return;
        }

        int maxName = top.stream().mapToInt(e -> e.username.length()).max().orElse(5);
        int maxPts = top.stream().mapToInt(e -> e.points).max().orElse(1);
        for (int i = 0; i < top.size(); i++) {
            Lead e = top.get(i);
            int barLen = Math.max(1, (40 * e.points) / Math.max(1, maxPts));
            String bar = "#".repeat(barLen);
            System.out.printf("%2d) %-" + maxName + "s  pts:%4d  streak:%3d  | %s%n",
                    i + 1, e.username, e.points, e.streak, bar);
        }
        System.out.println("\n(ID, Name):");
        for (Lead e : top) {
            System.out.println(" - " + e.userId + ", " + e.username);
        }
    }

    private boolean canPlay(int userId) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT last_played_date FROM Stats WHERE user_id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                java.sql.Date last = rs.getDate("last_played_date");
                if (last == null) return true;
                LocalDate lastDate = last.toLocalDate();
                return !LocalDate.now().isEqual(lastDate);
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Error checking daily attempt: " + e.getMessage());
            return true;
        }
    }

    private void saves(int userId, String username, String gameName, boolean won) {
        try (Connection con = DBConnection.getConnection()) {
            CallableStatement cs = con.prepareCall("{CALL SaveGameSession(?, ?, ?, ?)}");
            cs.setInt(1, userId);
            cs.setString(2, username);
            cs.setString(3, gameName);
            cs.setBoolean(4, won);
            cs.execute();
        } catch (SQLException e) {
            System.out.println("Error saving session via procedure: " + e.getMessage());
        }
    }
}

// ------------------------------- LEADERBOARD -------------------------------
class Lead {
    int userId;
    String username;
    int points;
    int streak;

    Lead(int id, String name, int pts, int str) {
        this.userId = id;
        this.username = name;
        this.points = pts;
        this.streak = str;
    }
}

class LeaderboardRepo {
    public static void increment(int userId, String username, String gameName, int addPoints, boolean won) {
        try (Connection con = DBConnection.getConnection()) {
           if (won) {
                PreparedStatement upd = con.prepareStatement(
                        "INSERT INTO Stats (user_id, username, streak, points, last_played_date) " +
                                "VALUES (?, ?, 1, ?, CURDATE()) " +
                                "ON DUPLICATE KEY UPDATE streak = 1, points = ?, last_played_date = CURDATE()");
                upd.setInt(1, userId);
                upd.setString(2, username);
                upd.setInt(3, addPoints);
                upd.setInt(4, addPoints);
                upd.executeUpdate();
            } else {
                PreparedStatement upd = con.prepareStatement(
                        "INSERT INTO Stats (user_id, username, streak, points, last_played_date) " +
                                "VALUES (?, ?, 0, 0, CURDATE()) " +
                                "ON DUPLICATE KEY UPDATE streak = 0, last_played_date = CURDATE()");
                upd.setInt(1, userId);
                upd.setString(2, username);
                upd.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error updating Streak: " + e.getMessage());
        }
    }

    public static void reset(int userId) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Stats (user_id, username, streak, points, last_played_date) " +
                            "VALUES (?, ?, 0, 0, CURDATE()) " +
                            "ON DUPLICATE KEY UPDATE streak = 0, last_played_date = CURDATE()");
            ps.setInt(1, userId);
            String username = "";
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error Resetting streak: " + e.getMessage());
        }
    }

    public static List<Lead> top10() {
        List<Lead> out = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT u.Id AS user_id, u.Username, s.points, s.streak " +
                    "FROM Stats s JOIN Users u ON u.Id = s.user_id " +
                    "ORDER BY s.points DESC, s.streak DESC, s.last_played_date ASC LIMIT 10");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Lead e = new Lead(
                        rs.getInt("user_id"),
                        rs.getString("Username"),
                        rs.getInt("points"),
                        rs.getInt("streak"));
                out.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching leaderboard: " + e.getMessage());
        }
        return out;
    }
}

// ------------------------------- MAIN -------------------------------
class Games {
    private final GameManager manager = new GameManager();
    public void play(int userId, String username) {
        manager.playDailyGame(userId, username);
    }
    public void showLeaderboard() {
        manager.showLeaderboard();
    }
}

public class INSTA_LITE {
    static Scanner sc = new Scanner(System.in);
    public static void main(String[] args)throws Exception {
        User user = new User();   Search search = new Search();
        Account acc = new Account();  Message msg = new Message();
        Reels_Management reel = new Reels_Management();

        Games games = new Games();
        boolean b = true;
        do{
            if(!user.isLoggedIn()) {
                System.out.println("\n ----------- WELCOME TO INSTA_LITE CONSOLE  ----------- \n");
                System.out.println("1. Create Account");
                System.out.println("2. Login");
                System.out.println("3. Forget Password");
                System.out.println("4. Exit");
                System.out.print("Enter Choice : ");
                int choice = sc.nextInt(); System.out.println();

                switch (choice) {
                    case 1: user.createAccount(); break;
                    case 2: user.log_In(); break;
                    case 3: acc.forgotPassword(); break;
                    case 4: b=false; break;
                }
            }
            else{
                System.out.println("==========================");
                System.out.println("1.Create A Post");
                System.out.println("2.Upload Reel");
                System.out.println("3.Scroll Reels");
                System.out.println("4.Digital Wellbeing Report");
                System.out.println("5.Send a Message");
                System.out.println("6.Check Inbox");
                System.out.println("7.Search Profile");
                System.out.println("8.Send A Friend Request");
                System.out.println("9.View Liked Reels");
                System.out.println("10.View Saved Reels");
                System.out.println("11.Delete Account");
                System.out.println("12.Deactivate Account");
                System.out.println("13.Log Out");
                System.out.println("14.EXIT");
                System.out.println("15.Play InstaLite Game");
                System.out.println("16.View Leaderboard");
                System.out.println("==========================");
                System.out.print("Enter Choice : ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1: reel.post();break;
                    case 2: reel.upload();break;
                    case 3: reel.scrollReels(User.getLoggedInUserId());break;
                    case 4: reel.digitalWellbeingReport(User.getLoggedInUserId()); break;

                    case 5: msg.sendMessage(User.getLoggedInUserId());break;
                    case 6: msg.inbox();break;
                    case 7: System.out.print("Enter Username to Search: ");
                        String name = sc.next();
                        search.display(name);break;
                    case 8: System.out.print("Enter Username: ");
                        String rname = sc.nextLine();
                        String receiverUsername = sc.nextLine();
                        int receiverId = user.check(receiverUsername);

                        if (receiverId == -1) {
                            System.out.println("User not found.");
                        } else {
                            user.sendRequest(User.getLoggedInUserId(), receiverId);
                        }
                        break;
                    case 9: reel.viewLikedReels(User.getLoggedInUserId());break;
                    case 10: reel.viewSavedReels(User.getLoggedInUserId());break;
                    case 11: acc.delete();break;
                    case 12: acc.deactivate();break;
                    case 13: user.logout();break;
                    case 14:
                        System.out.println("Goodbye! ");
                        b = false;
                        break;
                    case 15: games.play(User.getLoggedInUserId(), User.getLoggedInUserName()); break;
                    case 16: games.showLeaderboard(); break;
                }
            }
        }while(b);
    }
}
