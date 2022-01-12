# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-verbose
-flattenpackagehierarchy

#
-keep,includedescriptorclasses class com.google.android.gms.** { *; }
-keep,includedescriptorclasses class com.google.android.gms.internal.** { *; }
-keep class com.google.android.gms.internal.** { com.google.android.gms.internal.** initialize(android.content.Context); }
-keep class com.google.android.gms.iid.** { com.google.android.gms.iid.** get(java.lang.String); }
-keep,includedescriptorclasses class com.google.android.** { *; }

#
-keepattributes InnerClasses
-keep class com.rtsoft.growtopia.**
-keepclassmembers class com.rtsoft.growtopia.** { *; }
-keep class com.anzu.sdk.**
-keepclassmembers class com.anzu.sdk.** { *; }
