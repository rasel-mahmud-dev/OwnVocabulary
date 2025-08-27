# Keep your API-related classes
#-keep class com.rs.ownvocabulary.api.** { *; }
#-keep class com.rs.ownvocabulary.configs.** { *; }

# Keep data classes (especially if used for JSON serialization)
#-keep class com.rs.ownvocabulary.api.HttpResponse { *; }
#-keep class com.rs.ownvocabulary.database.WordPartial { *; }

-keep class com.rs.ownvocabulary.sync.PullWordJob { *; }
-keep class com.rs.ownvocabulary.sync.PullNoteJobResponse { *; }

# Keep OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep companion objects
-keepclassmembers class * {
    public static ** Companion;
}

# If you're using Gson or other JSON libraries, add their rules too
# For Gson:
# -keep class * implements com.google.gson.TypeAdapterFactory
# -keep class * implements com.google.gson.JsonSerializer
# -keep class * implements com.google.gson.JsonDeserializer

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer



