# TaskMaster - Ứng dụng Quản lý Công việc Thông minh

## 📱 Tổng quan

TaskMaster là ứng dụng quản lý công việc (To-do List) hiện đại, kết hợp gamification và chế độ tập trung (Focus Mode) để giúp người dùng nâng cao năng suất.

### ✨ Tính năng chính

#### 📦 Module 1: Quản lý Tài khoản & Trải nghiệm Người dùng
- ✅ Đăng ký/Đăng nhập với Email
- 🔐 Firebase Authentication
- 🎨 Quản lý theme (Light/Dark/System)
- 👤 Hồ sơ người dùng với avatar
- 📧 Quên mật khẩu
- (Đang phát triển) Đăng nhập Google/Facebook

#### 📝 Module 2: Quản lý Công việc & Chế độ Tập trung
- ✅ CRUD công việc (Thêm, Sửa, Xóa, Hoàn thành)
- 📋 Quản lý danh sách công việc (Lists/Projects)
- 🔴 Mức độ ưu tiên (Thấp/Trung bình/Cao/Gấp)
- 📅 Đặt deadline và nhắc nhở
- 🏷️ Gắn nhãn (tags)
- 🔍 Tìm kiếm công việc
- ⏲️ Focus Mode (Pomodoro Timer)
- 💾 Offline-first (hoạt động không cần internet)

#### 🎮 Module 3: Gamification & Thống kê
- 🌟 Hệ thống điểm kinh nghiệm (XP)
- 📈 Hệ thống cấp độ (Level)
- 🏆 Huy hiệu thành tích (Badges)
- 🔥 Chuỗi hoàn thành (Streaks)
- 📊 Biểu đồ thống kê tiến độ
- 📉 Phân tích hiệu suất làm việc

## 🏗️ Kiến trúc

### Tech Stack

- **Frontend**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt (Dependency Injection)
- **Local DB**: Room Database
- **Cloud**: Firebase (Auth, Firestore, Cloud Messaging)
- **Navigation**: Jetpack Navigation Compose
- **Async**: Kotlin Coroutines + Flow
- **Charts**: Vico Charts

### Cấu trúc thư mục

```
app/src/main/java/com/example/todolist2/
├── data/
│   ├── local/           # Room Database (Offline storage)
│   │   ├── dao/
│   │   ├── entity/
│   │   └── TodoDatabase.kt
│   ├── remote/          # Firebase data sources
│   │   └── firebase/
│   ├── repository/      # Repository implementations
│   └── preferences/     # DataStore preferences
├── domain/
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Business logic use cases
├── presentation/
│   ├── auth/            # Module 1: Authentication screens
│   ├── task/            # Module 2: Task management screens
│   ├── focus/           # Module 2: Focus mode screens
│   ├── gamification/    # Module 3: Gamification screens
│   ├── profile/         # Module 1: Profile screens
│   ├── navigation/      # Navigation setup
│   └── components/      # Reusable UI components
├── di/                  # Hilt dependency injection modules
└── util/                # Utility classes
```

## 🚀 Hướng dẫn Setup

### Yêu cầu

- Android Studio Hedgehog (2023.1.1) hoặc mới hơn
- JDK 11 hoặc mới hơn
- Android SDK API 24+ (Android 7.0+)
- Tài khoản Firebase

### Bước 1: Clone project

```bash
git clone <repository-url>
cd todolist2
```

### Bước 2: Setup Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Thêm ứng dụng Android:
   - Package name: `com.example.todolist2`
   - App nickname: TaskMaster
4. Tải file `google-services.json`
5. Thay thế file `app/google-services.json` hiện tại bằng file vừa tải

6. **Bật các services trong Firebase:**
   - **Authentication**:
     - Email/Password
     - (Optional) Google Sign-In
   - **Cloud Firestore**:
     - Chế độ: Start in production mode
     - Location: asia-southeast1 (Singapore)
   - **Cloud Messaging** (FCM):
     - Để gửi notifications

### Bước 3: Cấu hình Firestore Rules

Vào Firebase Console → Firestore Database → Rules, paste rules sau:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // User profiles
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
      
      // User's lists
      match /lists/{listId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
        
        // Tasks in lists
        match /tasks/{taskId} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
        }
      }
      
      // Focus sessions
      match /focus_sessions/{sessionId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

### Bước 4: Build & Run

```bash
# Sync gradle
./gradlew build

# Run app
./gradlew installDebug
```

Hoặc sử dụng Android Studio:
- File → Sync Project with Gradle Files
- Run → Run 'app'

## 📚 Tài liệu API

### Domain Models

#### User
```kotlin
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String,
    val preferredTheme: ThemePreference,
    val totalPoints: Int,
    val currentLevel: Int,
    val currentStreak: Int
)
```

#### Task
```kotlin
data class Task(
    val id: String,
    val listId: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: Priority,
    val dueDate: Long?,
    val tags: List<String>
)
```

#### FocusSession
```kotlin
data class FocusSession(
    val id: String,
    val taskId: String?,
    val durationInMinutes: Int,
    val startTime: Long,
    val pointsEarned: Int
)
```

## 🎯 Roadmap

### Phase 1 (Hoàn thành) ✅
- [x] Setup project & dependencies
- [x] Clean Architecture structure
- [x] Module 1: Authentication (Email/Password)
- [x] Module 2: Basic Task Management
- [x] Offline-first với Room Database
- [x] Firebase Firestore integration

### Phase 2 (Đang phát triển) 🚧
- [ ] Module 2: Focus Mode với Pomodoro Timer
- [ ] Module 3: Gamification system (XP, Level, Badges)
- [ ] Module 3: Charts & Analytics
- [ ] Push Notifications cho reminders
- [ ] Google Sign-In
- [ ] Theme customization

### Phase 3 (Kế hoạch) 📅
- [ ] Task sharing & collaboration
- [ ] Subtasks support
- [ ] Recurring tasks
- [ ] Data export (PDF/CSV)
- [ ] Widgets
- [ ] Dark mode auto-schedule

## 🤝 Contributing

Đây là project học tập. Mọi đóng góp đều được hoan nghênh!

## 📄 License

MIT License - xem file LICENSE để biết thêm chi tiết.

## 👥 Team

- **Thành viên A**: Module 1 (Authentication & UX)
- **Thành viên B**: Module 2 (Task Management & Focus Mode)
- **Thành viên C**: Module 3 (Gamification & Analytics)

---

**Note**: File `google-services.json` hiện tại là placeholder. Bạn PHẢI thay thế nó bằng file thật từ Firebase Console để app hoạt động đầy đủ.

🚀 Happy Coding!







