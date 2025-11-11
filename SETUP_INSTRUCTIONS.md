# 🚀 Hướng dẫn Setup Nhanh

## ⚠️ QUAN TRỌNG: Phải setup Firebase trước khi chạy!

### Bước 1: Setup Firebase (BẮT BUỘC)

1. Truy cập https://console.firebase.google.com/
2. Tạo project mới: Click "Add project" → Đặt tên "TaskMaster"
3. Thêm Android app:
   ```
   Package name: com.example.todolist2
   App nickname: TaskMaster
   ```
4. Tải file `google-services.json`
5. **QUAN TRỌNG**: Copy file vào `app/google-services.json` (thay thế file hiện tại)

### Bước 2: Bật Services trong Firebase

#### Authentication
- Vào: Authentication → Sign-in method
- Bật: **Email/Password**
- (Optional) Bật: Google

#### Cloud Firestore
- Vào: Firestore Database → Create database
- Chọn: **Start in production mode**
- Location: **asia-southeast1** (Singapore)

#### Firestore Security Rules
Copy rules sau vào Firestore → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
      
      match /lists/{listId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
        
        match /tasks/{taskId} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
        }
      }
      
      match /focus_sessions/{sessionId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

#### Cloud Messaging (Optional - cho notifications)
- Vào: Cloud Messaging
- Không cần config gì thêm

### Bước 3: Build & Run

```bash
# 1. Sync Gradle
File → Sync Project with Gradle Files

# 2. Build project
Build → Make Project

# 3. Run
Run → Run 'app'
```

### ✅ Kiểm tra Setup thành công

1. App mở được màn hình Splash
2. Chuyển đến màn hình Login
3. Có thể đăng ký tài khoản mới
4. Sau khi đăng nhập, vào màn hình Home

### 🐛 Troubleshooting

#### Lỗi: "google-services.json is missing"
→ Bạn chưa thay thế file google-services.json. Làm theo Bước 1.

#### Lỗi: "Firebase: Error (auth/network-request-failed)"
→ Kiểm tra kết nối internet và đảm bảo đã bật Authentication trong Firebase.

#### Lỗi: "Missing permissions: INTERNET"
→ Đã có trong AndroidManifest.xml, clean & rebuild project.

#### App crash khi đăng nhập
→ Kiểm tra Firestore rules đã đúng chưa (Bước 2).

### 📱 Test Flow

1. **Đăng ký**: Tạo tài khoản mới với email + password
2. **Đăng nhập**: Login với tài khoản vừa tạo
3. **Thêm task**: Click FAB (+) → Nhập tên task → Thêm
4. **Complete task**: Click checkbox để đánh dấu hoàn thành
5. **Navigation**: Test các tab: Công việc, Tập trung, Thống kê, Cá nhân

### 🎯 Features đã hoàn thành

✅ Module 1: Authentication (Email/Password)  
✅ Module 2: Task Management cơ bản (CRUD)  
✅ Offline-first với Room Database  
✅ Firebase Firestore sync  
✅ Material Design 3 UI  
✅ Bottom Navigation  

### 🚧 Features đang phát triển

⏳ Focus Mode (Pomodoro)  
⏳ Gamification (XP, Badges, Levels)  
⏳ Analytics & Charts  
⏳ Push Notifications  
⏳ Google Sign-In  

---

**Need Help?** Check README.md for detailed documentation.


