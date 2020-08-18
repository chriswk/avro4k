package com.sksamuel.avro4k

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors

fun SerialDescriptor.leafsOfSealedClasses(): List<SerialDescriptor> {
   return if (this.kind == PolymorphicKind.SEALED) {
      elementDescriptors.filter { it.kind == SerialKind.CONTEXTUAL }.flatMap { it.elementDescriptors }
         .flatMap { it.leafsOfSealedClasses() }
   } else {
      listOf(this)
   }
}

fun SerialDescriptor.serializer(): KSerializer<out Any?> {
   val notNullableSerializer = when (kind) {
      PrimitiveKind.BOOLEAN -> Boolean.serializer()
      PrimitiveKind.INT -> Int.serializer()
      PrimitiveKind.LONG -> Long.serializer()
      PrimitiveKind.FLOAT -> Float.serializer()
      PrimitiveKind.BOOLEAN -> Boolean.serializer()
      PrimitiveKind.BYTE -> Byte.serializer()
      PrimitiveKind.SHORT -> Short.serializer()
      PrimitiveKind.STRING -> String.serializer()
      StructureKind.LIST -> ListSerializer(this.elementDescriptors.single().serializer())
      else -> TODO("only implemented primitive types for now")
   }
   return if (this.isNullable) {
      notNullableSerializer.nullable
   } else {
      notNullableSerializer
   }

}