adb install -r app-android/app/build/outputs/apk/debug/app-debug.apk
adb logcat -c
adb shell monkey -p com.example.agroanalyst -c android.intent.category.LAUNCHER 1
sleep 15
echo "=== PROCESSO ==="
adb shell pidof com.example.agroanalyst || echo "app NAO esta rodando"
echo "=== CRASH ==="
adb logcat -d -b crash | head -80
echo "=== RUNTIME ==="
adb logcat -d -s AndroidRuntime:E | head -80
