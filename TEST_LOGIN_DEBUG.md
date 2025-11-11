# 🧪 Test & Debug Firebase Authentication

## ✅ Logic kiểm tra đăng nhập đã được CẢI THIỆN

### 📋 Các thay đổi:

#### 1. **Validation chi tiết:**
```kotlin
✅ Email:
   - Không được trống
   - Phải đúng format (abc@domain.com)
   - Tự động trim() khoảng trắng

✅ Password:
   - Không được trống  
   - Tối thiểu 6 ký tự
   - Cảnh báo nếu không có số (vẫn cho phép)

✅ Display Name (đăng ký):
   - Không được trống
   - Tối thiểu 2 ký tự
```

#### 2. **Error messages tiếng Việt:**
```kotlin
❌ "API key not valid" 
   → "⚠️ Chưa cấu hình Firebase!..."

❌ "password is invalid"
   → "Mật khẩu không đúng"

❌ "no user record"
   → "Email chưa được đăng ký"

❌ "email address is already in use"
   → "Email đã được đăng ký"

❌ "network error"
   → "Không có kết nối mạng"

❌ "too many requests"
   → "Quá nhiều lần thử. Vui lòng đợi"
```

#### 3. **Logging chi tiết:**
```kotlin
🔐 Bắt đầu đăng nhập...
⏳ Đang gửi request đến Firebase...
✅ Đăng nhập thành công: user@email.com
❌ Lỗi đăng nhập: API key not valid
```

---

## 🧪 CÁCH TEST

### Bước 1: Build & Install
```bash
cd C:\Users\ACER\AndroidStudioProjects\todolist2
./gradlew clean assembleDebug
./gradlew installDebug
```

### Bước 2: Xem Logs Real-time
```bash
# Xem TẤT CẢ logs của app
adb logcat -s SignUpViewModel LoginViewModel FirebaseAuthDataSource

# Hoặc filter theo tag cụ thể
adb logcat -s SignUpViewModel:* LoginViewModel:*

# Hoặc xem logs Firebase
adb logcat | findstr "Firebase"
```

### Bước 3: Test Cases

#### ✅ Test Case 1: Đăng ký thành công
```
Input:
  Tên: Test User
  Email: test123@gmail.com
  Password: 123456

Expected Log:
  📝 Bắt đầu đăng ký...
  ⏳ Đang tạo tài khoản trên Firebase...
  ✅ Đăng ký thành công!
  🆔 User ID: abc123...

Expected Behavior:
  - Loading spinner hiện
  - Navigate to Home screen
  - User xuất hiện trong Firebase Console → Authentication
```

#### ❌ Test Case 2: Email đã tồn tại
```
Input:
  Email: test123@gmail.com (email vừa đăng ký)
  Password: 123456

Expected Log:
  ❌ Lỗi đăng ký: email address is already in use
  🔍 Phân tích lỗi: ...

Expected Behavior:
  - Hiện error: "Email đã được đăng ký"
  - Loading tắt
```

#### ❌ Test Case 3: Email không hợp lệ
```
Input:
  Email: invalidemail (không có @)

Expected Log:
  ❌ Email không hợp lệ: invalidemail

Expected Behavior:
  - Hiện error ngay lập tức
  - KHÔNG gọi Firebase API
```

#### ❌ Test Case 4: Mật khẩu quá ngắn
```
Input:
  Password: 123 (< 6 ký tự)

Expected Log:
  ❌ Mật khẩu quá ngắn

Expected Behavior:
  - Hiện error ngay lập tức
  - KHÔNG gọi Firebase API
```

#### ❌ Test Case 5: Firebase chưa setup
```
Expected Log:
  ❌ Lỗi đăng ký: API key not valid
  🚨 FIREBASE CHƯA SETUP!

Expected Behavior:
  - Hiện error với hướng dẫn chi tiết
  - "⚠️ Chưa cấu hình Firebase!..."
```

#### ✅ Test Case 6: Đăng nhập thành công
```
Input:
  Email: test123@gmail.com
  Password: 123456

Expected Log:
  🔐 Bắt đầu đăng nhập...
  ⏳ Đang gửi request đến Firebase...
  ✅ Đăng nhập thành công: test123@gmail.com

Expected Behavior:
  - Loading spinner hiện
  - Navigate to Home screen
```

#### ❌ Test Case 7: Mật khẩu sai
```
Input:
  Email: test123@gmail.com
  Password: wrongpassword

Expected Log:
  ❌ Lỗi đăng nhập: password is invalid

Expected Behavior:
  - Hiện error: "Mật khẩu không đúng"
```

#### ❌ Test Case 8: Email chưa đăng ký
```
Input:
  Email: notexist@gmail.com
  Password: 123456

Expected Log:
  ❌ Lỗi đăng nhập: no user record

Expected Behavior:
  - Hiện error: "Email chưa được đăng ký"
```

---

## 🐛 DEBUG STEPS

### Lỗi: "API key not valid"

#### 1. Kiểm tra file google-services.json
```bash
cd C:\Users\ACER\AndroidStudioProjects\todolist2\app
cat google-services.json | findstr "project_id"

# Nếu thấy "todolist-placeholder" → PHẢI THAY FILE!
```

#### 2. Tạo Firebase project mới
Xem: `FIX_LOGIN_ERROR.md`

#### 3. Kiểm tra Firebase Console
```
1. Vào: https://console.firebase.google.com/
2. Chọn project
3. Authentication → Sign-in method
4. Kiểm tra Email/Password: ENABLED ✅
```

---

### Lỗi: "Network error"

#### 1. Kiểm tra kết nối internet
```bash
ping google.com
```

#### 2. Kiểm tra Emulator/Device có internet
```bash
adb shell ping -c 3 8.8.8.8
```

#### 3. Kiểm tra Firewall
- Đảm bảo không chặn Firebase domains

---

### Lỗi: "Permission denied"

#### 1. Kiểm tra Firestore Rules
```
Vào: Firebase Console → Firestore Database → Rules

Phải có:
match /users/{userId} {
  allow read: if request.auth != null;
  allow write: if request.auth != null && request.auth.uid == userId;
}
```

---

## 📊 Logs Flow

### Flow đăng ký thành công:
```
SignUpViewModel: 📝 Bắt đầu đăng ký...
SignUpViewModel: ⏳ Đang tạo tài khoản trên Firebase...
SignUpViewModel: 📧 Email: test@gmail.com
SignUpViewModel: 👤 Tên: Test User
FirebaseAuth: Creating user...
Firestore: Writing user document...
SignUpViewModel: ✅ Đăng ký thành công!
SignUpViewModel: 🆔 User ID: abc123xyz...
SignUpViewModel: 📧 Email: test@gmail.com
```

### Flow đăng ký lỗi:
```
SignUpViewModel: 📝 Bắt đầu đăng ký...
SignUpViewModel: ⏳ Đang tạo tài khoản trên Firebase...
FirebaseAuth: Error - API key not valid
SignUpViewModel: ❌ Lỗi đăng ký: API key not valid
SignUpViewModel: 🔍 Phân tích lỗi: API key not valid
SignUpViewModel: 🚨 FIREBASE CHƯA SETUP!
```

---

## 🔍 Advanced Debug

### Xem full Firebase logs:
```bash
adb logcat | findstr /i "firebase auth firestore"
```

### Xem network requests:
```bash
adb logcat | findstr /i "okhttp"
```

### Xem crash logs:
```bash
adb logcat | findstr /i "AndroidRuntime FATAL"
```

### Clear app data (reset):
```bash
adb shell pm clear com.example.todolist2
```

---

## ✅ Checklist trước khi test:

- [ ] File `google-services.json` đã thay (KHÔNG phải placeholder)
- [ ] Firebase Authentication → Email/Password: ENABLED
- [ ] Firestore Database đã tạo
- [ ] Firestore Rules đã setup
- [ ] App đã build thành công
- [ ] Device/Emulator có internet

---

## 🎯 Kết luận

**Logic kiểm tra đăng nhập hiện tại:**
- ✅ Validation đầy đủ
- ✅ Error handling tốt
- ✅ Logging chi tiết
- ✅ Messages tiếng Việt
- ✅ Firebase integration hoàn chỉnh

**Nếu vẫn lỗi "API key not valid":**
→ 100% do Firebase chưa setup
→ Xem hướng dẫn: `FIX_LOGIN_ERROR.md`

---

🔥 **Ready to test!**


