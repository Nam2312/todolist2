@echo off
echo ========================================
echo    KIEM TRA SETUP FIREBASE
echo ========================================
echo.

set "JSON_FILE=app\google-services.json"

if not exist "%JSON_FILE%" (
    echo [LOI] File google-services.json KHONG TON TAI!
    echo.
    echo Ban can:
    echo 1. Tao Firebase project tai: https://console.firebase.google.com/
    echo 2. Them Android app (package: com.example.todolist2)
    echo 3. Tai file google-services.json
    echo 4. Copy file vao: app\google-services.json
    echo.
    pause
    exit /b 1
)

echo [OK] File google-services.json ton tai
echo.

findstr /C:"todolist-placeholder" "%JSON_FILE%" >nul 2>&1
if %errorlevel% == 0 (
    echo [LOI] File google-services.json la PLACEHOLDER!
    echo.
    echo Ban can thay the file nay bang file that tu Firebase Console.
    echo.
    echo Huong dan:
    echo 1. Vao: https://console.firebase.google.com/
    echo 2. Chon project cua ban
    echo 3. Vao Project Settings
    echo 4. Scroll xuong, tim "Your apps"
    echo 5. Click vao Android app
    echo 6. Tai file google-services.json
    echo 7. Copy file vao: app\google-services.json
    echo.
    pause
    exit /b 1
)

echo [OK] File google-services.json KHONG phai placeholder
echo.

findstr /C:"project_id" "%JSON_FILE%" >nul 2>&1
if %errorlevel% == 0 (
    echo [OK] File co project_id
) else (
    echo [LOI] File khong co project_id
    pause
    exit /b 1
)

echo.
echo ========================================
echo    KET QUA KIEM TRA
echo ========================================
echo.
echo [OK] File google-services.json ton tai
echo [OK] File khong phai placeholder
echo [OK] File co project_id
echo.
echo Ban co the tiep tuc build app!
echo.
echo Luu y: Ban cung can kiem tra trong Firebase Console:
echo 1. Authentication da bat Email/Password
echo 2. Firestore Database da duoc tao
echo 3. Firestore Rules da duoc setup
echo.
pause

