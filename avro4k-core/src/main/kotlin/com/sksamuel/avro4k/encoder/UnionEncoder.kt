package com.sksamuel.avro4k.encoder

import com.sksamuel.avro4k.Record
import com.sksamuel.avro4k.RecordNaming
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import org.apache.avro.Schema

class UnionEncoder(private val unionSchema : Schema,
                   override val serializersModule: SerializersModule,
                   private val callback: (Record) -> Unit) : AbstractEncoder() {

   override fun beginStructure(descriptor: SerialDescriptor, vararg typeSerializers: KSerializer<*>): CompositeEncoder {
      return when (descriptor.kind) {
         is StructureKind.CLASS -> {
            //Hand in the concrete schema for the specified SerialDescriptor so that fields can be correctly decoded.
            val leafSchema = unionSchema.types.first{
               val schemaName = RecordNaming(it.fullName, emptyList())
               val serialName = RecordNaming(descriptor)
               serialName.name() == schemaName.name() && serialName.namespace() == schemaName.namespace()
            }
            RecordEncoder(schema = leafSchema, serializersModule = serializersModule, callback = callback)
         }
         else -> throw SerializationException("Unsupported root element passed to root record encoder")
      }
   }

   override fun endStructure(descriptor: SerialDescriptor) {

   }
}