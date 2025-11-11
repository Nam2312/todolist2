#!/bin/bash
# Script kiểm tra Firebase đã setup đúng chưa

echo "🔍 Kiểm tra Firebase Setup..."
echo ""

# Check file google-services.json
echo "1️⃣ Kiểm tra google-services.json:"
PROJECT_ID=$(grep "project_id" app/google-services.json | head -1)

if [[ $PROJECT_ID == *"placeholder"* ]]; then
    echo "❌ VẪN DÙNG FILE PLACEHOLDER!"
    echo "   → Phải download file mới từ Firebase Console"
    echo ""
    exit 1
else
    echo "✅ File google-services.json hợp lệ"
    echo "   $PROJECT_ID"
fi

echo ""
echo "2️⃣ Build project:"
./gradlew clean assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build SUCCESS"
else
    echo "❌ Build FAILED"
    exit 1
fi

echo ""
echo "3️⃣ Cài app:"
./gradlew installDebug

echo ""
echo "🎯 Checklist Firebase Console:"
echo "   □ Authentication → Sign-in method → Email/Password: ENABLED"
echo "   □ Firestore Database → Data: Database đã tạo"
echo "   □ Firestore Database → Rules: Rules đã publish"
echo ""
echo "✅ Nếu 3 mục trên đã làm → App sẽ đăng ký được!"
echo ""
echo "🧪 Test: Mở app → Đăng ký → Kiểm tra Firebase Console → Users"


