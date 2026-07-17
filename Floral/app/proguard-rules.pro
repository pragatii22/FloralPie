# Floral release ProGuard/R8 rules.
#
# Preserve stack trace line numbers for Crashlytics-readable crash reports.
-keepattributes SourceFile,LineNumberTable

# --- Firebase Realtime Database model deserialization ---
# DataSnapshot.getValue(Class) uses reflection to match JSON keys to fields/constructors. A single
# scoped keep on the model package is used here rather than per-class @Keep annotations, since
# every class under model/ is in fact read back from RTDB this way. If a future model in this
# package is NOT read via getValue(), prefer moving it out of model/ (or adding @Keep to sibling
# classes and narrowing this rule) rather than assuming R8 will do the right thing by default.
-keep class com.example.floral.model.** {
    <init>(...);
    <fields>;
}
-keepclassmembers class com.example.floral.model.** {
    <init>(...);
}

# --- Firebase Functions callable responses ---
# CheckoutRepoImpl/OrderRepoImpl parse callable responses via raw Map access (no reflection-based
# deserialization), so no additional keep rule is required there.

# --- Coil (already ships consumer ProGuard rules; no project-level rule needed) ---

# --- Firebase Cloud Messaging service (must survive obfuscation to be resolved from the manifest) ---
-keep class com.example.floral.notification.FloralMessagingService { *; }

# --- Firebase Crashlytics / Performance Monitoring (both ship consumer rules; kept explicit for clarity) ---
-keepattributes *Annotation*
-keep class com.google.firebase.crashlytics.** { *; }
-keep class com.google.firebase.perf.** { *; }

# The Crashlytics AAR references its own optional KTX entry point (com.google.firebase.ktx.Firebase)
# even though this project uses the classic (non-KTX) Crashlytics API and never depends on that
# artifact -- that reference is dead code on our classpath, not a real missing dependency. R8
# reports it as a missing class and refuses to proceed without this rule (confirmed by an actual
# `./gradlew assembleRelease` run, not assumed).
-dontwarn com.google.firebase.ktx.Firebase

# Uncomment to hide the original source file name (keep line numbers only) once verified in a
# real release build:
#-renamesourcefileattribute SourceFile
