# 🔥 Hướng dẫn Setup Firebase - Từng bước chi tiết

## ⚠️ QUAN TRỌNG
File `google-services.json` hiện tại chỉ là **placeholder**. Bạn **PHẢI** tạo Firebase project thật và tải file mới về.

---

## 📋 Checklist Setup Firebase

### ✅ Bước 1: Tạo Firebase Project (5 phút)

1. **Mở trình duyệt**, truy cập: https://console.firebase.google.com/
2. **Đăng nhập** bằng tài khoản Google của bạn
3. Click nút **"Add project"** (Thêm dự án) hoặc **"Tạo dự án"**
4. **Nhập tên project**: 
   - Ví dụ: `TaskMaster` hoặc `TodoList2`
   - Click **Continue** (Tiếp tục)
5. **Google Analytics** (tùy chọn):
   - Có thể **TẮT** (không bắt buộc)
   - Hoặc bật nếu muốn
   - Click **Continue**
6. **Đợi 30 giây** để Firebase tạo project
7. Click **Continue** khi hoàn thành

✅ **Kết quả**: Bạn đã có Firebase project mới!

---

### ✅ Bước 2: Thêm Android App vào Firebase (3 phút)

1. Trong Firebase Console, bạn sẽ thấy màn hình **Project Overview**
2. Tìm icon **Android** (🤖) hoặc click **"Add app"** → chọn **Android**
3. **Nhập thông tin**:
   ```
   Android package name: com.example.todolist2
   App nickname (optional): TaskMaster
   Debug signing certificate SHA-1: (có thể bỏ qua)
   ```
4. Click **Register app** (Đăng ký ứng dụng)

✅ **Kết quả**: Android app đã được thêm vào Firebase!

---

### ✅ Bước 3: Tải file google-services.json (2 phút)

1. Firebase sẽ hiển thị hướng dẫn tải file `google-services.json`
2. Click nút **"Download google-services.json"** (Tải xuống google-services.json)
3. File sẽ được tải về thư mục **Downloads** của bạn
4. **Copy file này** vào thư mục project:
   ```
   C:\Users\ACER\AndroidStudioProjects\todolist2\app\google-services.json
   ```
5. **Thay thế** file cũ (file placeholder)
6. Quay lại Firebase Console, click **Next** → **Next** → **Continue to console**

✅ **Kết quả**: File `google-services.json` đã được cập nhật!

---

### ✅ Bước 4: Bật Authentication - Email/Password (2 phút)

1. Trong Firebase Console, click menu **Build** (Xây dựng) ở bên trái
2. Click **Authentication** (Xác thực)
3. Click nút **"Get started"** (Bắt đầu)
4. Click tab **"Sign-in method"** (Phương thức đăng nhập)
5. Tìm **"Email/Password"** trong danh sách
6. Click vào **"Email/Password"**
7. **Bật toggle** ở dòng đầu tiên (Enable)
8. Click **Save** (Lưu)

✅ **Kết quả**: Email/Password authentication đã được bật!

---

### ✅ Bước 5: Bật Billing (MIỄN PHÍ) - QUAN TRỌNG! (2 phút)

⚠️ **LƯU Ý**: Firestore yêu cầu billing được bật, nhưng bạn có thể dùng **Spark Plan (MIỄN PHÍ)**!

1. Click vào link mà Firebase cung cấp:
   ```
   https://console.developers.google.com/billing/enable?project=todolist2-528bb
   ```
   Hoặc:
   - Vào Firebase Console → Click vào **⚙️ Settings** (bên trái) → **Project settings**
   - Scroll xuống phần **Usage and billing**
   - Click **"Upgrade project"** hoặc **"Manage billing"**

2. **Chọn Spark Plan (Free)**:
   - Firebase sẽ hỏi bạn chọn plan
   - Chọn **"Spark Plan"** (MIỄN PHÍ - Free forever)
   - Click **Continue**

3. **Thiết lập billing account** (nếu chưa có):
   - Nếu chưa có billing account, Google sẽ yêu cầu tạo
   - Click **"Create billing account"**
   - Chọn quốc gia: **Vietnam** (hoặc quốc gia của bạn)
   - Nhập thông tin (có thể để trống một số trường)
   - **QUAN TRỌNG**: Chọn **Spark Plan** (không phải Blaze Plan)
   - Click **"Start free trial"** hoặc **"Continue"**

4. **Xác nhận**:
   - Đọc và chấp nhận điều khoản
   - Click **"Enable billing"** hoặc **"Submit"**

5. **Đợi 2-3 phút** để billing được kích hoạt

✅ **Kết quả**: Billing đã được bật (Spark Plan - MIỄN PHÍ)!

**💡 Lưu ý về Spark Plan (Free)**:
- ✅ **MIỄN PHÍ** mãi mãi
- ✅ 1GB storage
- ✅ 50K reads/ngày
- ✅ 20K writes/ngày
- ✅ 20K deletes/ngày
- ✅ Đủ cho app TodoList cá nhân hoặc nhỏ

---

### ✅ Bước 6: Tạo Firestore Database (3 phút)

1. Trong Firebase Console, click menu **Build** → **Firestore Database**
2. Click nút **"Create database"** (Tạo cơ sở dữ liệu)
3. **Chọn chế độ bảo mật**:
   - Chọn **"Start in production mode"** (Bắt đầu ở chế độ sản xuất)
   - Click **Next**
4. **Chọn location** (Vị trí):
   - Chọn **asia-southeast1** (Singapore) - gần Việt Nam nhất
   - Hoặc chọn location khác nếu muốn
   - Click **Enable** (Bật)

✅ **Kết quả**: Firestore Database đã được tạo!

---

### ✅ Bước 7: Setup Firestore Security Rules (2 phút)

1. Trong Firestore Database, click tab **"Rules"** (Quy tắc)
2. **Xóa toàn bộ** code hiện tại
3. **Copy và paste** code sau vào:

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

4. Click **"Publish"** (Xuất bản)

✅ **Kết quả**: Firestore Rules đã được setup!

---

## 🎉 HOÀN THÀNH SETUP!

Bây giờ bạn đã:
- ✅ Firebase project đã tạo
- ✅ Android app đã được thêm
- ✅ File `google-services.json` đã được tải về
- ✅ Authentication đã được bật
- ✅ Billing đã được bật (Spark Plan - MIỄN PHÍ)
- ✅ Firestore Database đã được tạo
- ✅ Firestore Rules đã được setup

---

## 🚀 Bước tiếp theo: Build và Test App

### 1. Sync Project trong Android Studio

1. Mở Android Studio
2. Mở project: `C:\Users\ACER\AndroidStudioProjects\todolist2`
3. Click **File** → **Sync Project with Gradle Files**
4. Đợi sync hoàn thành

### 2. Kiểm tra file google-services.json

Mở file: `app/google-services.json`

**File ĐÚNG** sẽ có:
- `"project_id"`: Tên project bạn vừa tạo (KHÔNG phải "todolist-placeholder")
- `"project_number"`: Số thực (không phải "123456789")
- `"api_key"`: Key thật (không phải "AIzaSyDummyKey...")

**File SAI** (placeholder):
- `"project_id": "todolist-placeholder"` ← Đây là file cũ!

### 3. Run App

1. Kết nối điện thoại hoặc mở emulator
2. Click **Run** → **Run 'app'**
3. Chọn device
4. Đợi app build và cài đặt

### 4. Test Đăng ký

1. Mở app
2. Click **"Đăng ký ngay"**
3. Nhập thông tin:
   - Tên: Test User
   - Email: test@gmail.com
   - Password: 123456
4. Click **"Đăng ký"**
5. ✅ Nếu thành công → Chuyển sang màn hình Home

### 5. Kiểm tra Firebase Console

1. Vào Firebase Console → **Authentication** → **Users**
2. ✅ Bạn sẽ thấy email `test@gmail.com` vừa đăng ký
3. Vào **Firestore Database** → **Data**
4. ✅ Bạn sẽ thấy data: `users/{userId}/lists/...`

---

## 🐛 Xử lý lỗi thường gặp

### ❌ Lỗi: "An internal error has occurred"
**Nguyên nhân**: Chưa bật Email/Password trong Authentication  
**Giải pháp**: Làm lại **Bước 4**

### ❌ Lỗi: "This API method requires billing to be enabled"
**Nguyên nhân**: Chưa bật billing cho Firebase project  
**Giải pháp**: Làm **Bước 5** - Bật billing (chọn Spark Plan - MIỄN PHÍ)

### ❌ Lỗi: "Permission denied" trong Firestore
**Nguyên nhân**: Firestore Rules chưa đúng  
**Giải pháp**: Làm lại **Bước 7**, kiểm tra code rules

### ❌ Lỗi: "Network error" hoặc "Connection failed"
**Nguyên nhân**: 
- File `google-services.json` chưa được thay thế
- Không có internet
**Giải pháp**: 
- Kiểm tra file `google-services.json` có đúng không
- Kiểm tra kết nối internet
- Làm lại **Bước 3**

### ❌ App crash khi mở
**Nguyên nhân**: File `google-services.json` vẫn là placeholder  
**Giải pháp**: 
- Mở file `app/google-services.json`
- Nếu thấy `"project_id": "todolist-placeholder"` → File cũ!
- Làm lại **Bước 3** để tải file mới

### ❌ Không thấy "Add app" button
**Nguyên nhân**: Chưa tạo project  
**Giải pháp**: Làm lại **Bước 1**

---

## 📸 Hình ảnh minh họa (nếu cần)

Nếu bạn gặp khó khăn, có thể tham khảo:
- Firebase Console: https://console.firebase.google.com/
- Firebase Documentation: https://firebase.google.com/docs/android/setup

---

## ✅ Checklist cuối cùng

Trước khi test app, đảm bảo:

- [ ] Firebase project đã tạo
- [ ] Android app đã được thêm (package: `com.example.todolist2`)
- [ ] File `google-services.json` đã được tải về và thay thế
- [ ] Authentication → Email/Password đã được bật
- [ ] Firestore Database đã được tạo
- [ ] Firestore Rules đã được setup
- [ ] Android Studio đã sync project
- [ ] App đã được build và chạy

---

## 🎯 Tổng kết

**Thời gian setup**: ~15-20 phút

**Các bước chính**:
1. Tạo Firebase project
2. Thêm Android app
3. Tải `google-services.json`
4. Bật Authentication
5. **Bật Billing (Spark Plan - MIỄN PHÍ)** ⚠️ QUAN TRỌNG!
6. Tạo Firestore Database
7. Setup Firestore Rules

**Sau khi setup xong**:
- App sẽ đăng ký/đăng nhập được
- Data sẽ sync lên Firebase
- Offline mode vẫn hoạt động

---

🔥 **Chúc bạn setup thành công!**

Nếu gặp vấn đề, hãy kiểm tra lại từng bước hoặc xem phần "Xử lý lỗi thường gặp" ở trên.

