#!/data/data/com.termux/files/usr/bin/bash
set -e
pkg install aapt2 apksigner dx openjdk-21 zip -y
mkdir -p lib
if [ ! -f lib/android.jar ]; then
  curl -L -o android.jar.zip "https://dl.google.com/android/repository/platform-33_r02.zip"
  unzip -o android.jar.zip "android-13/android.jar" -d lib/
  mv lib/android-13/android.jar lib/android.jar
  rm -rf android.jar.zip lib/android-13
fi
if [ ! -f debug.keystore ]; then
  keytool -genkey -v -keystore debug.keystore -alias debug \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass android -keypass android -dname "CN=Debug"
fi
