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

-keep class com.example.cursbnr.Inventar.Utile.FakeApiResponse.** {*;}
-keep class * extends android.view.View
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class com.example.cursbnr.Inventar.Utile.JsonFakeApi.** {*;}
-keep class com.example.cursbnr.Inventar.Utile.BarcodeScan.** {*;}

-keep class com.example.cursbnr.CursBNR.CursValutar.Activitati.** {*;}
-keep class com.example.cursbnr.CursBNR.CursValutar.Utile.** {*;}
-keep class com.example.cursbnr.CursBNR.AnimationUtils.** {*;}

-keep class com.example.cursbnr.CursBNR.GenerareRapoarte.Activitati.** {*;}
-keep class com.example.cursbnr.CursBNR.GenerareRapoarte.Utile.** {*;}

-keep class com.example.cursbnr.CursBNR.IstoricRapoarte.Activitati.** {*;}
-keep class com.example.cursbnr.CursBNR.IstoricRapoarte.listener.** {*;}
-keep class com.example.cursbnr.CursBNR.IstoricRapoarte.Utile.** {*;}
-keep class com.example.cursbnr.CursBNR.** {*;}

-keep class com.example.cursbnr.Inventar.Listener.** {*;}
-keep class com.example.cursbnr.Inventar.retrofit.** {*;}
-keep class com.example.cursbnr.Inventar.Utile.** {*;}
-keep class com.example.cursbnr.Inventar.** {*;}
-keep class com.example.cursbnr.** {*;}


-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepattributes InnerClasses
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# Specifies to write out some more information during processing.
# If the program terminates with an exception, this option will print out the entire stack trace, instead of just the exception message.
-verbose