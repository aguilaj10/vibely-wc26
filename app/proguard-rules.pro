# Baseline rules — Hilt, Room, kotlinx.serialization, AGP R8 9.2.
# Keep generated metadata annotations needed at runtime.

-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations
-keepattributes Signature,InnerClasses,EnclosingMethod

# kotlinx.serialization — keep @Serializable classes and their generated companions.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
    static <1>$$serializer INSTANCE;
}
-if @kotlinx.serialization.Serializable class **
-keep class <1>$$serializer { *; }
-keepclassmembers,allowoptimization enum * { **[] values(); ** valueOf(java.lang.String); }
