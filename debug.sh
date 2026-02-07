./gradlew assembleDebug
adb install ./app/debug/app-debug.apk
adb shell monkey -p com.rs.myvocabulary -c android.intent.category.LAUNCHER 1