@echo off
echo Building and installing...
call "%~dp0..\gradlew.bat" installDebug

if %ERRORLEVEL% NEQ 0 (
    echo Build failed. Exiting.
    exit /b %ERRORLEVEL%
)

echo Build successful. Launching app...
adb shell am start -n com.nimroddayan.couponmanager/com.nimroddayan.couponmanager.MainActivity
