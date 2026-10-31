#!/data/data/com.termux/files/usr/bin/bash
set -e
rm -rf build && mkdir -p build/classes build/dex

aapt2 compile --dir res -o build/res.zip
aapt2 link -o build/unsigned.apk -I lib/android.jar -A assets \
  --manifest AndroidManifest.xml build/res.zip \
  --java build/classes --min-sdk-version 26 --target-sdk-version 33

javac -source 8 -target 8 -cp lib/android.jar:lib/json.jar -d build/classes $(find src -name "*.java")

find build/classes -name "*.class" > build/classes.txt
d8 --lib lib/android.jar --min-api 26 --output build/dex @build/classes.txt

cd build/dex && zip -j ../unsigned.apk classes.dex && cd ../..
apksigner sign --ks debug.keystore --ks-pass pass:android \
  --key-pass pass:android --out build/app.apk build/unsigned.apk
echo "APK: build/app.apk"
