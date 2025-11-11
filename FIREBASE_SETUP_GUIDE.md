# 🔥 Hướng dẫn Setup Firebase - BẮT BUỘC

## ⚠️ Quan trọng: App KHÔNG THỂ đăng nhập nếu chưa setup Firebase!

File `google-services.json` hiện tại chỉ là **placeholder**. Bạn cần setup Firebase thật.

---

## 📝 Setup Firebase (5-10 phút)

### Bước 1: Tạo Firebase Project

1. Truy cập: **https://console.firebase.google.com/**
2. Click **"Add project"** (Thêm dự án)
3. Đặt tên: `TaskMaster` (hoặc tên bất kỳ)
4. Click **Continue** → Tắt Google Analytics (không bắt buộc) → **Create project**
5. Đợi 30 giây → Click **Continue**

---

### Bước 2: Thêm Android App

1. Trong Firebase Console, click **icon Android** (⚙️ Project Overview → Add app → Android)
2. Nhập thông tin:
   ```
   Android package name: com.example.todolist2
   App nickname: TaskMaster
   SHA-1: (có thể bỏ qua)
   ```
3. Click **Register app**

---

### Bước 3: Tải google-services.json

1. Click **Download google-services.json**
2. **Copy file này vào:**
   ```
   C:\Users\ACER\AndroidStudioProjects\todolist2\app\
   ```
3. **Thay thế** file cũ (placeholder)
4. Click **Next** → **Next** → **Continue to console**

---

### Bước 4: Bật Authentication

1. Trong Firebase Console, click **Build** → **Authentication**
2. Click **Get started**
3. Tab **Sign-in method**
4. Click **Email/Password**
5. **Bật toggle đầu tiên** (Email/Password)
6. Click **Save**

✅ **XONG! Email/Password đã được bật**

---

### Bước 5: Tạo Firestore Database

1. Click **Build** → **Firestore Database**
2. Click **Create database**
3. Chọn location: **asia-southeast1** (Singapore)
4. Click **Next**
5. **Chọn**: Start in **production mode**
6. Click **Create**

---

### Bước 6: Setup Firestore Rules

1. Trong Firestore, click tab **Rules**
2. **Thay thế** toàn bộ code bằng:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to read/write their own data
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
      
      // User's task lists
      match /lists/{listId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
        
        // Tasks in lists
        match /tasks/{taskId} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
        }
      }
      
      // User's focus sessions
      match /focus_sessions/{sessionId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

3. Click **Publish**

✅ **HOÀN THÀNH SETUP FIREBASE!**

---

## 🚀 Build & Run App

### Option 1: Android Studio
```
1. Sync project: File → Sync Project with Gradle Files
2. Run: Run → Run 'app'
3. Chọn emulator hoặc device
```

### Option 2: Command line
```bash
cd C:\Users\ACER\AndroidStudioProjects\todolist2
./gradlew installDebug
```

---

## ✅ Test App

### 1. Sign Up (Đăng ký)
```
- Mở app → Click "Đăng ký ngay"
- Nhập:
  + Tên: Test User
  + Email: test@gmail.com
  + Password: 123456
- Click "Đăng ký"
- ✅ Nếu thành công → Chuyển sang Home screen
```

### 2. Kiểm tra Firebase
```
1. Vào Firebase Console → Authentication → Users
2. Bạn sẽ thấy email test@gmail.com vừa đăng ký
```

### 3. Add Task
```
- Click nút + (FAB)
- Nhập tên task
- Click "Thêm"
- ✅ Task xuất hiện trong list
```

### 4. Kiểm tra Firestore
```
1. Vào Firebase Console → Firestore Database
2. Bạn sẽ thấy:
   users/{userId}/lists/{listId}/tasks/{taskId}
```

---

## 🐛 Troubleshooting

### Lỗi: "An internal error has occurred"
**Nguyên nhân**: Chưa bật Email/Password trong Authentication  
**Fix**: Làm lại Bước 4

### Lỗi: "Permission denied"
**Nguyên nhân**: Firestore Rules chưa đúng  
**Fix**: Làm lại Bước 6

### Lỗi: "Network error"
**Nguyên nhân**: Không có internet hoặc file google-services.json sai  
**Fix**: 
- Kiểm tra internet
- Làm lại Bước 3 (tải file mới)

### App crash khi mở
**Nguyên nhân**: File google-services.json chưa thay thế  
**Fix**:
```bash
# Kiểm tra file
cat app/google-services.json

# Nếu thấy "project_id": "todolist-placeholder"
# → File cũ! Phải tải file mới từ Firebase
```

---

## 📊 Kiểm tra Setup thành công

✅ Firebase Console → Authentication → Users: Có user  
✅ Firebase Console → Firestore: Có data  
✅ App: Đăng nhập được  
✅ App: Add task được  
✅ App: Offline vẫn hoạt động  

---

## 🎯 Tổng kết

**Bắt buộc phải có:**
1. ✅ Firebase project (5 phút)
2. ✅ google-services.json thật (không phải placeholder)
3. ✅ Email/Password Authentication đã bật
4. ✅ Firestore Database đã tạo
5. ✅ Firestore Rules đã setup

**Sau đó:**
- App sẽ đăng ký/đăng nhập được
- Data sẽ sync lên Firebase
- Offline mode vẫn hoạt động

---

🔥 **Ready to test!**


