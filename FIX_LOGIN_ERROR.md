# 🔴 FIX LỖI: "API key not valid"

## ❌ Lỗi bạn đang gặp:
```
An internal error has occurred. 
[ API key not valid. Please pass a valid API key. ]
```

## 🎯 Nguyên nhân:
File `google-services.json` là **PLACEHOLDER** (file giả), không phải Firebase project thật.

## ✅ CÁCH FIX (5 phút):

---

### Bước 1️⃣: Mở Firebase Console
```
🔗 https://console.firebase.google.com/
```
- Đăng nhập bằng Gmail
- Click "Add project" (Thêm dự án)

---

### Bước 2️⃣: Tạo Project
```
Project name: TaskMaster2025
(hoặc tên bất kỳ)
```
- Click Continue
- **TẮT** Google Analytics (không cần)
- Click "Create project"
- Đợi 30 giây
- Click "Continue"

---

### Bước 3️⃣: Thêm Android App

**Trong Firebase Console:**
1. Click **biểu tượng Android** (Add app)
2. Điền thông tin:

```
Android package name: com.example.todolist2
App nickname: TaskMaster
Debug signing certificate SHA-1: (BỎ QUA - để trống)
```

3. Click **"Register app"**

---

### Bước 4️⃣: Tải file google-services.json MỚI

**⚠️ QUAN TRỌNG NHẤT:**

1. Trong Firebase Console, click **"Download google-services.json"**

2. **Copy file vừa tải vào:**
   ```
   C:\Users\ACER\AndroidStudioProjects\todolist2\app\
   ```

3. **THAY THẾ** file cũ (placeholder)

4. Click "Next" → "Next" → "Continue to console"

---

### Bước 5️⃣: Bật Email/Password Authentication

1. Trong Firebase Console, menu bên trái:
   ```
   Build → Authentication
   ```

2. Click **"Get started"**

3. Tab **"Sign-in method"**

4. Click **"Email/Password"**

5. **Bật toggle đầu tiên** (Enable)

6. Click **"Save"**

✅ **XONG! Authentication đã BẬT**

---

### Bước 6️⃣: Tạo Firestore Database

1. Menu bên trái:
   ```
   Build → Firestore Database
   ```

2. Click **"Create database"**

3. **Location**: Chọn `asia-southeast1 (Singapore)`

4. Click **"Next"**

5. **Mode**: Chọn **"Start in production mode"**

6. Click **"Enable"**

---

### Bước 7️⃣: Setup Firestore Security Rules

1. Trong Firestore, tab **"Rules"**

2. **XÓA** toàn bộ code cũ

3. **DÁN** code này vào:

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

4. Click **"Publish"**

✅ **XONG! Firebase đã setup xong**

---

## 🚀 Build & Test App

### 1. Sync lại project:
```bash
cd C:\Users\ACER\AndroidStudioProjects\todolist2
./gradlew clean
```

### 2. Build lại:
```bash
./gradlew assembleDebug
```

### 3. Cài app:
```bash
./gradlew installDebug
```

### 4. Mở app và thử đăng ký:
```
Email: test@gmail.com
Password: 123456
```

✅ **Sẽ đăng ký THÀNH CÔNG!**

---

## 🎯 Kiểm tra đã fix chưa:

### Trong Firebase Console:

1. **Authentication → Users**
   - Sẽ thấy email `test@gmail.com` vừa đăng ký

2. **Firestore Database → Data**
   - Sẽ thấy collection `users` với data user

---

## ⏱️ Tổng thời gian: 5-7 phút

**Checklist:**
- ✅ Tạo Firebase project (1 phút)
- ✅ Thêm Android app (1 phút)
- ✅ Download + Replace google-services.json (30 giây)
- ✅ Bật Email/Password Auth (1 phút)
- ✅ Tạo Firestore (1 phút)
- ✅ Setup Rules (1 phút)
- ✅ Build & Test (2 phút)

---

## 🆘 Cần hỗ trợ?

**Nếu vẫn lỗi sau khi làm xong 7 bước:**
- Chụp màn hình Firebase Console
- Chụp màn hình lỗi trong app
- Gửi log: `adb logcat | grep -i firebase`

---

## 📌 Lưu ý quan trọng:

1. **File google-services.json MỚI** phải có:
   ```json
   "project_id": "taskmaster2025-xxxxx"
   ```
   (KHÔNG PHẢI "todolist-placeholder")

2. **Email/Password** phải được BẬT trong Authentication

3. **Firestore** phải được TẠO

4. **Rules** phải được SETUP

**Thiếu 1 trong 4 → VẪN LỖI!**

---

🔥 **Let's fix it now!**


