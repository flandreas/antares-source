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
-keep,includedescriptorclasses class ch.scorpion.jabbah.app.rating.**$$serializer { *; }
-keepclassmembers class ch.scorpion.jabbah.app.rating.** {
    *** Companion;
}
-keepclasseswithmembers class ch.scorpion.jabbah.app.rating.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class ch.scorpion.jabbah.graph.login.**$$serializer { *; }
-keepclassmembers class ch.scorpion.jabbah.graph.login.** {
    *** Companion;
}
-keepclasseswithmembers class ch.scorpion.jabbah.graph.login.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class ch.scorpion.jabbah.graph.project.**$$serializer { *; }
-keepclassmembers class ch.scorpion.jabbah.graph.project.** {
    *** Companion;
}
-keepclasseswithmembers class ch.scorpion.jabbah.graph.project.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class ch.scorpion.jabbah.base.invocation.**$$serializer { *; }
-keepclassmembers class ch.scorpion.jabbah.base.invocation.** {
    *** Companion;
}
-keepclasseswithmembers class ch.scorpion.jabbah.base.invocation.** {
    kotlinx.serialization.KSerializer serializer(...);
}