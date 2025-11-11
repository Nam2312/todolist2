# 🚀 Bắt đầu nhanh - Setup Firebase

## 📌 Tóm tắt nhanh (5 phút)

### Bước 1: Tạo Firebase Project
1. Vào: https://console.firebase.google.com/
2. Click **"Add project"**
3. Đặt tên: `TaskMaster`
4. Click **Continue** → **Continue** → **Create project**

### Bước 2: Thêm Android App
1. Click icon **Android** 🤖
2. Nhập: `com.example.todolist2`
3. Click **Register app**
4. **Tải file `google-services.json`**
5. **Copy file vào**: `app/google-services.json` (thay thế file cũ)
6. Click **Next** → **Next** → **Continue**

### Bước 3: Bật Authentication
1. Click **Build** → **Authentication**
2. Click **Get started**
3. Click **Email/Password**
4. **Bật toggle** → **Save**

### Bước 4: Bật Billing (MIỄN PHÍ) ⚠️ QUAN TRỌNG!
1. Click link: https://console.developers.google.com/billing/enable?project=todolist2-528bb
2. Chọn **Spark Plan** (MIỄN PHÍ)
3. Tạo billing account (nếu chưa có)
4. Đợi 2-3 phút

### Bước 5: Tạo Firestore
1. Click **Build** → **Firestore Database**
2. Click **Create database**
3. Chọn **Production mode**
4. Chọn location: **asia-southeast1**
5. Click **Enable**

### Bước 6: Setup Rules
1. Click tab **Rules**
2. Copy code từ file `FIREBASE_SETUP_GUIDE.md` (Bước 6)
3. Click **Publish**

---

## ✅ Kiểm tra Setup

Chạy file: `kiem_tra_firebase.bat`

Hoặc kiểm tra thủ công:
- Mở file `app/google-services.json`
- Nếu thấy `"project_id": "todolist-placeholder"` → **SAI!** Cần tải file mới
- Nếu thấy `"project_id": "ten-project-cua-ban"` → **ĐÚNG!**

---

## 🎯 Xong rồi!

Bây giờ bạn có thể:
1. Mở Android Studio
2. Sync Project (File → Sync Project with Gradle Files)
3. Run app (Run → Run 'app')
4. Test đăng ký/đăng nhập

---

## 📖 Chi tiết đầy đủ

Xem file: `HUONG_DAN_SETUP_FIREBASE.md` để có hướng dẫn chi tiết từng bước.

---

## 🐛 Gặp lỗi?

Xem phần "Xử lý lỗi thường gặp" trong file `HUONG_DAN_SETUP_FIREBASE.md`

