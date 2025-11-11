# 💳 Hướng dẫn Bật Billing cho Firebase (MIỄN PHÍ)

## ⚠️ QUAN TRỌNG

Firestore Database **YÊU CẦU** billing được bật, nhưng bạn có thể dùng **Spark Plan - HOÀN TOÀN MIỄN PHÍ**!

---

## 🚀 Cách Bật Billing (Spark Plan - MIỄN PHÍ)

### Cách 1: Click link trực tiếp

1. Click vào link này (thay `todolist2-528bb` bằng project ID của bạn):
   ```
   https://console.developers.google.com/billing/enable?project=todolist2-528bb
   ```

2. Chọn **Spark Plan** (Free tier)
3. Tạo billing account (nếu chưa có)
4. Đợi 2-3 phút

### Cách 2: Từ Firebase Console

1. Vào Firebase Console: https://console.firebase.google.com/
2. Chọn project của bạn
3. Click **⚙️ Settings** (bên trái) → **Project settings**
4. Scroll xuống phần **Usage and billing**
5. Click **"Upgrade project"** hoặc **"Manage billing"**
6. Chọn **Spark Plan** (Free)
7. Tạo billing account nếu cần
8. Đợi 2-3 phút

---

## 📋 Chi tiết từng bước

### Bước 1: Chọn Plan

Khi bạn click vào link hoặc "Upgrade project", Firebase sẽ hỏi bạn chọn plan:

- **Spark Plan** (Free) ← **CHỌN CÁI NÀY!**
- Blaze Plan (Pay as you go) ← Không chọn (trừ khi bạn muốn trả tiền)

**Chọn Spark Plan** và click **Continue**

### Bước 2: Tạo Billing Account (nếu chưa có)

Nếu bạn chưa có billing account, Google sẽ yêu cầu tạo:

1. **Chọn quốc gia**: Vietnam (hoặc quốc gia của bạn)
2. **Nhập thông tin**:
   - Tên: Tên của bạn
   - Địa chỉ: Có thể để trống hoặc nhập địa chỉ
   - Số điện thoại: (tùy chọn)
3. **Chọn Spark Plan** (không phải Blaze)
4. Click **"Start free trial"** hoặc **"Continue"**

### Bước 3: Xác nhận

1. Đọc điều khoản
2. Tích vào ô đồng ý
3. Click **"Enable billing"** hoặc **"Submit"**

### Bước 4: Đợi kích hoạt

- Đợi **2-3 phút** để billing được kích hoạt
- Bạn sẽ thấy thông báo "Billing enabled" hoặc "Spark Plan active"

---

## ✅ Kiểm tra Billing đã bật

1. Vào Firebase Console → **⚙️ Settings** → **Project settings**
2. Scroll xuống phần **Usage and billing**
3. Bạn sẽ thấy:
   - **Plan**: Spark (free)
   - **Status**: Active

---

## 💡 Spark Plan - Giới hạn MIỄN PHÍ

Spark Plan cung cấp **MIỄN PHÍ** các giới hạn sau:

### Firestore:
- ✅ **1GB** storage
- ✅ **50,000 reads/ngày**
- ✅ **20,000 writes/ngày**
- ✅ **20,000 deletes/ngày**

### Authentication:
- ✅ **Không giới hạn** users

### Cloud Messaging:
- ✅ **Không giới hạn** messages

**💡 Đủ cho app TodoList cá nhân hoặc nhỏ!**

---

## ⚠️ Lưu ý quan trọng

1. **Spark Plan là MIỄN PHÍ mãi mãi** - không bao giờ hết hạn
2. **Không cần thẻ tín dụng** cho Spark Plan (một số quốc gia có thể yêu cầu, nhưng không bị charge)
3. **Nếu bạn chọn nhầm Blaze Plan**: Có thể đổi lại về Spark Plan trong Project settings
4. **Sau khi bật billing**: Đợi 2-3 phút rồi mới tạo Firestore Database

---

## 🐛 Xử lý lỗi

### Lỗi: "Billing account creation failed"
**Giải pháp**: 
- Thử lại sau vài phút
- Kiểm tra thông tin đã nhập đúng chưa
- Thử dùng trình duyệt khác

### Lỗi: "Cannot enable billing"
**Giải pháp**:
- Đảm bảo bạn đã đăng nhập đúng tài khoản Google
- Kiểm tra project ID có đúng không
- Thử cách 2 (từ Firebase Console)

### Vẫn không tạo được Firestore sau khi bật billing
**Giải pháp**:
- Đợi thêm 2-3 phút
- Refresh trang Firebase Console
- Thử tạo Firestore lại

---

## 🎯 Sau khi bật billing

Bạn có thể:
1. ✅ Tạo Firestore Database
2. ✅ Sử dụng tất cả tính năng Firebase (trong giới hạn Spark Plan)
3. ✅ App sẽ hoạt động bình thường

---

## 📞 Cần hỗ trợ?

- Firebase Support: https://firebase.google.com/support
- Firebase Documentation: https://firebase.google.com/docs

---

🔥 **Chúc bạn setup thành công!**

