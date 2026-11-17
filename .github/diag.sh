mkdir -p shots
adb install -r app-android/app/build/outputs/apk/debug/app-debug.apk
adb logcat -c
adb shell monkey -p com.example.agroanalyst -c android.intent.category.LAUNCHER 1
sleep 20

tela() {
  nome=$1
  adb exec-out screencap -p > shots/$nome.png
  echo "=== TELA $nome ==="
  adb shell uiautomator dump /sdcard/u.xml > /dev/null 2>&1
  adb shell cat /sdcard/u.xml | grep -o 'text="[^"]\+"' | head -60
}
toque() { adb shell input tap $1 $2; sleep 6; }

echo "=== PROCESSO ==="
adb shell pidof com.example.agroanalyst || echo "app NAO esta rodando"
tela 1_inicio
toque 96 610
tela 2_mercado
toque 160 610
tela 3_mapa
toque 224 610
tela 4_analise
toque 288 610
tela 5_login_meus
echo "=== CRASH ==="
adb logcat -d -b crash | head -60
echo "=== PROCESSO FINAL ==="
adb shell pidof com.example.agroanalyst || echo "app NAO esta rodando"
