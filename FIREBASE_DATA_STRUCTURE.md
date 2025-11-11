# 🔥 Firebase Data Structure - Username & Password

## ✅ Cấu trúc dữ liệu đã được CẬP NHẬT

### 📊 Firebase Architecture

```
Firebase Project
├── Authentication (xử lý email & password)
│   └── Users
│       ├── uid: abc123...
│       │   ├── Email: test@gmail.com
│       │   └── Password: ****** (MÃ HÓA, không ai đọc được)
│       └── ...
│
└── Firestore Database (lưu profile)
    └── users (collection)
        └── {userId} (document)
            ├── id: "abc123..."
            ├── email: "test@gmail.com"
            ├── username: "test_user"  ← KHÔNG có password!
            ├── avatarUrl: ""
            ├── createdAt: 1234567890
            └── ... (gamification stats)
```

---

## 🔐 Tại sao KHÔNG lưu password trong Firestore?

### ❌ Nếu lưu password trong Firestore:
```javascript
// NGUY HIỂM!
users/{userId} {
  username: "test_user",
  password: "123456"  // ❌ Bất kỳ ai có quyền đọc đều thấy được!
}

// Firestore Rules:
allow read: if request.auth != null;  // Tất cả user đăng nhập đều đọc được!

// Ai cũng có thể:
firestore.collection("users").get().then(users => {
  users.forEach(user => {
    console.log(user.data().password);  // ❌ Lộ password!
  });
});
```

**Hậu quả:**
- ❌ Bất kỳ user nào cũng có thể đọc password của người khác
- ❌ Hack dễ dàng
- ❌ Vi phạm bảo mật nghiêm trọng
- ❌ Không thể mã hóa (Firestore lưu plain text)

---

### ✅ Firebase Authentication xử lý password:

```javascript
// Firebase Auth tự động:
auth.createUserWithEmailAndPassword(email, password)
  ↓
1. Hash password với bcrypt + salt
2. Lưu vào Firebase Authentication (backend Google)
3. KHÔNG ai đọc được (kể cả admin)
4. Chỉ Firebase server mới verify được
```

**Ưu điểm:**
- ✅ Password được MÃ HÓA mạnh (bcrypt, scrypt)
- ✅ Không ai đọc được (kể cả bạn - admin project)
- ✅ Firebase tự động xử lý:
  - Password reset
  - Brute force protection
  - Rate limiting
  - Security updates

---

## 📋 Data Flow

### Đăng ký (Sign Up):
```kotlin
// Step 1: User nhập form
username: "test_user"
email: "test@gmail.com"
password: "123456"

// Step 2: Gửi đến Firebase Authentication
auth.createUserWithEmailAndPassword(email, password)
  ↓
Firebase Auth tự động:
- Hash password: "123456" → "$2a$10$N9qo8uL..."
- Lưu vào Authentication table
- Trả về userId: "abc123xyz..."

// Step 3: Lưu profile vào Firestore (KHÔNG có password!)
firestore.collection("users").doc(userId).set({
  id: "abc123xyz...",
  email: "test@gmail.com",
  username: "test_user",  // ✅ CHỈ có username
  createdAt: 1234567890
  // ❌ KHÔNG có password!
})
```

### Đăng nhập (Login):
```kotlin
// Step 1: User nhập form
email: "test@gmail.com"
password: "123456"

// Step 2: Firebase Authentication verify
auth.signInWithEmailAndPassword(email, password)
  ↓
Firebase Auth tự động:
- Hash password input: "123456" → hash
- So sánh với hash đã lưu
- Nếu đúng → Trả về userId + token
- Nếu sai → Error: "password is invalid"

// Step 3: Lấy profile từ Firestore
firestore.collection("users").doc(userId).get()
  ↓
Trả về:
{
  id: "abc123xyz...",
  email: "test@gmail.com",
  username: "test_user",  // ✅ Chỉ có thông tin profile
  createdAt: 1234567890
  // ❌ KHÔNG có password!
}
```

---

## 🔍 Kiểm tra trong Firebase Console

### 1. Authentication (Email + Password):
```
Vào: Firebase Console → Authentication → Users

Table:
┌────────────────────────────┬──────────────────────┬──────────────┐
│ Identifier                 │ Providers            │ Created      │
├────────────────────────────┼──────────────────────┼──────────────┤
│ test@gmail.com             │ Email/Password       │ 2 mins ago   │
│ user123@gmail.com          │ Email/Password       │ 1 hour ago   │
│ google@gmail.com           │ Google.com           │ 1 day ago    │
└────────────────────────────┴──────────────────────┴──────────────┘

❌ KHÔNG THỂ xem password! (kể cả admin)
```

### 2. Firestore Database (Profile):
```
Vào: Firebase Console → Firestore Database → users

Collection: users
└── abc123xyz... (document)
    {
      "id": "abc123xyz...",
      "email": "test@gmail.com",
      "username": "test_user",      ← ✅ CÓ username
      "avatarUrl": "",
      "createdAt": 1234567890,
      "totalPoints": 0,
      "currentLevel": 1
      // ❌ KHÔNG có password!
    }
```

---

## 🧪 Test với Logcat

### Logs khi đăng ký:
```
SignUpViewModel: 📝 Bắt đầu đăng ký...
SignUpViewModel: 👤 Username: test_user
SignUpViewModel: 📧 Email: test@gmail.com
SignUpViewModel: 🔐 Password: *** (sẽ được mã hóa bởi Firebase Auth)

FirebaseAuth: Creating user with email...
FirebaseAuth: ✅ User created: uid=abc123xyz...

FirebaseAuthDataSource: Step 1: Create user in Firebase Authentication (password is handled securely)
FirebaseAuthDataSource: Step 2: Create user profile in Firestore (WITHOUT password!)
FirebaseAuthDataSource: Step 3: Save to Firestore collection "users" with attributes: username, email (NO PASSWORD!)

Firestore: Writing document: users/abc123xyz...
Firestore: Document written:
{
  "id": "abc123xyz...",
  "email": "test@gmail.com",
  "username": "test_user",
  "createdAt": 1234567890
}

SignUpViewModel: ✅ Đăng ký thành công!
```

**Quan sát:**
- ✅ Password KHÔNG xuất hiện trong Firestore logs
- ✅ Chỉ có username và email trong Firestore
- ✅ Firebase Auth xử lý password riêng biệt

---

## 🎯 Tổng kết

| Thuộc tính | Lưu ở đâu? | Ai đọc được? |
|------------|------------|--------------|
| **Email** | Authentication + Firestore | ✅ User đó + admins |
| **Password** | Authentication (mã hóa) | ❌ KHÔNG AI (kể cả admin) |
| **Username** | Firestore | ✅ User đó + admins |
| **Avatar, XP, Level, ...** | Firestore | ✅ User đó + admins |

### Firestore Collection Structure:
```javascript
users (collection)
└── {userId} (document)
    ├── id: string          // User ID từ Firebase Auth
    ├── email: string       // Email của user
    ├── username: string    // ✅ Tên người dùng (thay displayName)
    ├── avatarUrl: string
    ├── preferredTheme: enum
    ├── createdAt: timestamp
    ├── totalPoints: number
    ├── currentLevel: number
    ├── currentStreak: number
    ├── tasksCompleted: number
    └── badgesEarned: array
    // ❌ KHÔNG có password!
```

---

## 🔒 Security Rules (Firestore)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      // Mỗi user chỉ đọc/ghi data của chính mình
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

**Bảo mật:**
- ✅ User A KHÔNG đọc được username của User B
- ✅ Mỗi user chỉ thấy data của mình
- ✅ Password KHÔNG tồn tại trong Firestore → Không lo bị đọc trộm

---

## 📝 Code Changes Summary

### 1. User Model (`User.kt`):
```kotlin
data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",  // ✅ Changed from displayName
    // ❌ NO password field!
    ...
)
```

### 2. SignUpViewModel:
```kotlin
data class SignUpState(
    val username: String = "",  // ✅ Changed from displayName
    val password: String = "",  // ⚠️ Only for Firebase Auth, NOT saved to Firestore
    ...
)
```

### 3. FirebaseAuthDataSource:
```kotlin
suspend fun signUpWithEmail(...): Resource<User> {
    // Step 1: Create in Authentication (password is encrypted)
    auth.createUserWithEmailAndPassword(email, password)
    
    // Step 2: Save profile to Firestore (WITHOUT password!)
    val user = User(
        id = firebaseUser.uid,
        email = email,
        username = displayName  // ✅ Only username
        // ❌ NO password!
    )
    
    firestore.collection("users").document(userId).set(user)
}
```

---

## ✅ Checklist

- [x] Model User có field `username` (không có password)
- [x] SignUpViewModel validate username format
- [x] FirebaseAuthDataSource lưu username vào Firestore
- [x] Password KHÔNG xuất hiện trong Firestore
- [x] Firebase Authentication xử lý password
- [x] Logs confirm password không lưu trong Firestore
- [x] UI có text giải thích về password

---

🔥 **Đã cập nhật xong! Username được lưu trong Firestore, Password được Firebase Authentication xử lý bảo mật.**


