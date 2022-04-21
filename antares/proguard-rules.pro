# --- Used for serialization of TOs in ktor REST API calls

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Change here com.yourcompany.yourpackage
-keep,includedescriptorclasses class ch.scorpion.jabbah.app.rating.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class ch.scorpion.jabbah.app.rating.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class ch.scorpion.jabbah.app.rating.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}
