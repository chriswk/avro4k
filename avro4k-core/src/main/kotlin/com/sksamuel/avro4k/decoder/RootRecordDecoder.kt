package com.sksamuel.avro4k.decoder

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import org.apache.avro.generic.GenericRecord

class RootRecordDecoder(private val record: GenericRecord) : AbstractDecoder() {
   var decoded = false
   override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
      return when (descriptor.kind) {
         StructureKind.CLASS -> RecordDecoder(descriptor, record)
         PolymorphicKind.SEALED -> SealedClassDecoder(descriptor,record)
         else -> throw SerializationException("Non-class structure passed to root record decoder")
      }
   }

   override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
      val index = if(decoded) DECODE_DONE else 0
      decoded = true
      return index
   }
}