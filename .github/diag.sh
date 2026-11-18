mkdir -p shots
adb install -r app-android/app/build/outputs/apk/debug/app-debug.apk
adb logcat -c
adb shell monkey -p com.example.agroanalyst -c android.intent.category.LAUNCHER 1 > /dev/null
sleep 15
echo "=== PROCESSO ==="
adb shell pidof com.example.agroanalyst || echo "app NAO esta rodando"
echo "=== AQUECER SOLO (cache do servidor) ==="
for i in $(seq 1 40); do c=$(curl -s -o /dev/null -w "%{http_code}" --max-time 60 "https://app-agroanalyst-mobile.onrender.com/publico/solo?lat=-23.67&lon=-46.46"); echo "solo $i -> $c"; [ "$c" = "200" ] && break; sleep 10; done
echo "=== E2E ==="
python3 .github/e2e.py
adb exec-out screencap -p > shots/final.png
echo "=== CRASH ==="
adb logcat -d -b crash | head -60
echo "=== PROCESSO FINAL ==="
adb shell pidof com.example.agroanalyst || echo "app NAO esta rodando"
echo "=== SEM REDE ==="
adb shell am force-stop com.example.agroanalyst
adb shell svc wifi disable
adb shell svc data disable
adb shell cmd connectivity airplane-mode enable
sleep 4
adb shell monkey -p com.example.agroanalyst -c android.intent.category.LAUNCHER 1 > /dev/null
sleep 4
adb shell uiautomator dump /sdcard/u.xml > /dev/null 2>&1
adb shell cat /sdcard/u.xml | grep -o 'text="[^"]\\+"' | head -25
