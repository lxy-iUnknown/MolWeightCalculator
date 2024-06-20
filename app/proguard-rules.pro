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

-keep class androidx.compose.runtime.snapshots.SnapshotStateList {
    #noinspection ShrinkerUnresolvedReference
    java.lang.Object mutate(kotlin.jvm.functions.Function1);
}

-keep class androidx.compose.runtime.snapshots.SnapshotKt {
   androidx.compose.runtime.snapshots.Snapshot getSnapshotInitializer();
   notifyWrite(
       androidx.compose.runtime.snapshots.Snapshot,
       androidx.compose.runtime.snapshots.StateObject
   );
   #noinspection ShrinkerUnresolvedReference
   androidx.compose.runtime.snapshots.StateRecord overwritableRecord(
       androidx.compose.runtime.snapshots.StateRecord,
       androidx.compose.runtime.snapshots.StateObject,
       androidx.compose.runtime.snapshots.Snapshot,
       androidx.compose.runtime.snapshots.StateRecord
   );
}