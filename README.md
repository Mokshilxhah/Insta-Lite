# INSTA_LITE

INSTA_LITE is a Java-based console application inspired by Instagram. It allows users to interact socially with friends and explore content in a lightweight, offline-friendly environment. The app includes social media features, games, and wellbeing tools for a fun and balanced user experience.  

---

## 🌟 Features

### 1. User Management
- Register / Login: Create a new account or log in to an existing one.
- Profile Info: Store username, email, bio, and friend count.
- Account Management: Update profile details or delete account.

### 2. Social Features
- Friend Requests: Send, accept, or reject friend requests.
- Friends Count: Automatically updates when requests are accepted.
- Inbox: View pending requests and friend interactions.

### 3. Media & Posts
- Image / Reel Upload: Upload images and .mp4 reels stored as LONGBLOB in MySQL.
- Like / Save Reels: Users can like or save reels for later viewing.
- Scrolling Reels: Randomized reel browsing similar to Instagram feed.
- Profile View: Search and view other users’ profiles along with their posts/reels.

### 4. Search Functionality
- Search Users: Find users by username or email.
- View Content: Display their bio, posts, and reels.

### 5. Games Feature 🎮
- Play mini console-based games for fun and engagement.
- Track scores locally per user.
- Types include quizzes, puzzles, and memory games.
- Limitations: Single-player, text-based, no persistent server storage for scores , single game per day with time limitation.

### 6. Wellbeing Feature 🧘
- Provides daily wellbeing tips and reminders.
- Encourages mindfulness, stress management, and balanced app usage.
- Mood tracking and simple journaling support.
- Limitations: Basic console implementation, not a professional health tool.

---

## ⚙️ How It Works
1. Launch the application in your console.
2. Register or login to your account.
3. Explore features:
   - Send and manage friend requests.
   - Upload, view, like, and save media.
   - Search for other users and view their profiles.
   - Play mini-games or use wellbeing tips for relaxation.
4. Data is stored in a MySQL database (Users, Posts/Reels) for persistent user interactions.

---

## 📝 Technology Stack
- Backend: Java (console-based)
- Database: MySQL (for user data, posts, reels)
- Media Storage: Images & Reels stored as LONGBLOB in MySQL
- Features: JDBC for database interaction, PreparedStatement & Statement usage.

---

## 🚀 What Makes INSTA_LITE Different
- Lightweight: No heavy UI frameworks, perfect for low-resource machines.
- Offline-Friendly: Works primarily with local console and MySQL backend.
- Integrated Fun & Wellbeing: Combines social media, games, and wellness tips.
- Customizable: Can extend games, reels, and wellbeing features easily.

---

## ⚠️ Limitations
- Console-based interface (no graphics or rich UI).
- Games are simple, single-player, and text-based.
- Wellbeing features are basic; not a substitute for professional guidance.
- Reels and media storage are limited to MySQL LONGBLOB size constraints.

---

## 📂 Setup Instructions
1. Clone the repository: 
   ```bash
   git clone <repo-url>
