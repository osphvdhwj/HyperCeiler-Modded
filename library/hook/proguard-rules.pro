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
-keep class com.hchen.superlyricapi.** {*;}
-keep class org.luckypray.dexkit.**{ *; }
-keep class org.lsposed.**{ *; }

-keep class com.harry.hyperhand.hook.XposedInit { *; }
-keep class com.harry.hyperhand.hook.module.** { *; }

-keep class * extends com.harry.hyperhand.hook.module.base.BaseHook { <init>(); }

-keep class com.harry.hyperhand.hook.module.base.dexkit.**{ *; }
-keep class * extends com.harry.hyperhand.hook.module.base.BaseModule
-keep class com.harry.hyperhand.hook.module.base.BaseModule { *; }

-keep class com.harry.hyperhand.hook.utils.blur.*
-keep class com.harry.hyperhand.hook.utils.api.miuiStringToast.res.** { *; }
-keep class com.harry.hyperhand.hook.utils.ContentModel {*;}
-keep class com.harry.hyperhand.hook.utils.FileHelper {*;}

-keep class io.github.kyuubiran.ezxhelper.** { *; }
-keep class com.hchen.hooktool.** { *; }

-dontwarn de.robv.android.xposed.**
-dontwarn miui.**
-dontwarn android.app.AndroidAppHelper
-dontwarn android.content.res.**

-dontwarn android.app.ActivityTaskManager$RootTaskInfo
-dontwarn miui.app.MiuiFreeFormManager$MiuiFreeFormStackInfo
-dontwarn javax.annotation.processing.AbstractProcessor
-dontwarn javax.annotation.processing.SupportedAnnotationTypes
-dontwarn javax.annotation.processing.SupportedOptions
-dontwarn javax.annotation.processing.SupportedSourceVersion
-dontwarn javax.annotation.processing.Processor

-allowaccessmodification
