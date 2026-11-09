# R8 full mode strips classes ML Kit resolves reflectively during
# TextRecognizer init, crashing release builds with
# "NullPointerException ... Object.getClass() on a null object reference".
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.internal.mlkit_vision_common.**
-dontwarn com.google.android.gms.internal.mlkit_vision_text.**
