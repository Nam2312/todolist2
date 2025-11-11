@echo off
REM Script kiểm tra Firebase đã setup đúng chưa

echo.
echo 🔍 Kiểm tra Firebase Setup...
echo.

REM Check file google-services.json
echo 1️⃣ Kiểm tra google-services.json:
findstr "project_id" app\google-services.json | findstr "placeholder" >nul
if %errorlevel% equ 0 (
    echo ❌ VẪN DÙNG FILE PLACEHOLDER!
    echo    → Phải download file mới từ Firebase Console
    echo.
    pause
    exit /b 1
) else (
    echo ✅ File google-services.json hợp lệ
    findstr "project_id" app\google-services.json
)

echo.
echo 2️⃣ Build project:
call gradlew.bat clean assembleDebug

if %errorlevel% equ 0 (
    echo ✅ Build SUCCESS
) else (
    echo ❌ Build FAILED
    pause
    exit /b 1
)

echo.
echo 3️⃣ Cài app:
call gradlew.bat installDebug

echo.
echo 🎯 Checklist Firebase Console:
echo    □ Authentication → Sign-in method → Email/Password: ENABLED
echo    □ Firestore Database → Data: Database đã tạo
echo    □ Firestore Database → Rules: Rules đã publish
echo.
echo ✅ Nếu 3 mục trên đã làm → App sẽ đăng ký được!
echo.
echo 🧪 Test: Mở app → Đăng ký → Kiểm tra Firebase Console → Users
echo.
pause


