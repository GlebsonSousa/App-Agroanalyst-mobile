adb install -r app-android/app/build/outputs/apk/debug/app-debug.apk
adb logcat -c
adb shell monkey -p com.example.agroanalyst -c android.intent.category.LAUNCHER 1
sleep 20
echo "=== PROCESSO ==="
adb shell pidof com.example.agroanalyst || echo "app NAO esta rodando"
echo "=== LOGCAT ==="
adb logcat -d -t 3000 | grep -E "AndroidRuntime|FATAL EXCEPTION|com.example.agroanalyst|Caused by|ClassNotFound|NoSuchMethod|Resources\\$NotFound" | head -120
