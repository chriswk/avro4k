package com.sksamuel.avro4k.io

import com.sksamuel.avro4k.Avro
import kotlinx.serialization.SerializationStrategy
import org.apache.avro.Schema
import org.apache.avro.file.CodecFactory
import org.apache.avro.generic.GenericRecord
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * An [AvroOutputStream] will write instances of T to an underlying
 * representation.
 *
 * There are three implementations of this stream
 *  - a Data stream,
 *  - a Binary stream
 *  - a Json stream
 *
 * See the methods on the companion object to create instances of each
 * of these types of stream.
 */
interface AvroOutputStream<T> : AutoCloseable {

   fun flush()
   fun fSync()
   fun write(t: T): AvroOutputStream<T>

   fun write(ts: List<T>): AvroOutputStream<T> {
      ts.forEach { write(it) }
      return this
   }

   companion object {

      /**
       * An [AvroOutputStream] that writes [GenericRecord]s as binary without the schema. Use this when
       * you want the smallest messages possible at the cost of not having the schema available
       * in the messages for downstream clients.
       */
      @Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro.default")
      fun binary(schema: Schema): AvroOutputStreamBuilder<GenericRecord> =
         AvroOutputStreamBuilder(schema, { it }, AvroFormat.BinaryFormat)

      /**
       * An [AvroOutputStream] that writes as binary without the schema. Use this when
       * you want the smallest messages possible at the cost of not having the schema available
       * in the messages for downstream clients.
       */
      @Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro.default")
      fun <T> binary(schema: Schema,
                     serializer: SerializationStrategy<T>,
                     avro: Avro = Avro.default): AvroOutputStreamBuilder<T> =
         AvroOutputStreamBuilder(schema, { avro.toRecord(serializer, it) }, AvroFormat.BinaryFormat)

      /**
       * An [AvroOutputStream] that writes as binary without the schema. Use this when
       * you want the smallest messages possible at the cost of not having the schema available
       * in the messages for downstream clients.
       */
      @Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro.default")
      fun <T> binary(serializer: SerializationStrategy<T>,
                     avro: Avro = Avro.default): AvroOutputStreamBuilder<T> =
         binary(avro.schema(serializer), serializer, avro)

      /**
       * An [AvroOutputStream] that writes [GenericRecord]s as JSON.
       * The schema is not included in the output.
       */
      @Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro.default")
      fun json(schema: Schema): AvroOutputStreamBuilder<GenericRecord> =
         AvroOutputStreamBuilder(schema, { it }, AvroFormat.JsonFormat)

      /**
       * An [AvroOutputStream] that writes values of T as JSON.
       * The schema is not included in the output.
       */
      @Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro.default")
      fun <T> json(schema: Schema,
                   serializer: SerializationStrategy<T>,
                   avro: Avro = Avro.default): AvroOutputStreamBuilder<T> =
         AvroOutputStreamBuilder(schema, { avro.toRecord(serializer, it) }, AvroFormat.JsonFormat)

      /**
       * An [AvroOutputStream] that writes values of T as JSON.
       * The schema is not included in the output.
       */
      @Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro.default")
      fun <T> json(serializer: SerializationStrategy<T>,
                   avro: Avro = Avro.default): AvroOutputStreamBuilder<T> =
         json(avro.schema(serializer), serializer, avro)

      /**
       * An [AvroOutputStream] that writes [GenericRecord]s as binary with the inclusion of the schema.
       */
      @Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro")
      fun data(schema: Schema): AvroOutputStreamBuilder<GenericRecord> =
         AvroOutputStreamBuilder(schema, { it }, AvroFormat.DataFormat)

      /**
       * An [AvroOutputStream] that writes values of <T> as binary with the inclusion of the schema.
       * The serializer must be supplied so that Ts can be converted to Avro records.
       * The schema is generated from the serializer.
       */
      @Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro.default")
      fun <T> data(serializer: SerializationStrategy<T>,
                   avro: Avro = Avro.default) =
         data(avro.schema(serializer), serializer, avro)

      /**
       * An [AvroOutputStream] that writes as binary with the inclusion of the given schema.
       */
      @Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro.default")
      fun <T> data(schema: Schema,
                   serializer: SerializationStrategy<T>,
                   avro: Avro = Avro.default) =
         AvroOutputStreamBuilder<T>(schema, { avro.toRecord(serializer, it) }, AvroFormat.DataFormat)
   }
}

@Deprecated("Create an instance of [AvroOutputStream] using the openOutputStream method on Avro.default")
class AvroOutputStreamBuilder<T>(private val schema: Schema,
                                 private val converter: (T) -> GenericRecord,
                                 private val format: AvroFormat,
                                 private val codec: CodecFactory = CodecFactory.nullCodec()) {

   fun withCodec(codec: CodecFactory) = AvroOutputStreamBuilder(schema, converter, format, codec)

   fun to(path: Path): AvroOutputStream<T> = to(Files.newOutputStream(path))
   fun to(path: String): AvroOutputStream<T> = to(Paths.get(path))
   fun to(file: File): AvroOutputStream<T> = to(file.toPath())

   fun to(output: OutputStream): AvroOutputStream<T> = when (format) {
      AvroFormat.DataFormat -> AvroDataOutputStream(output, converter, schema, codec)
      AvroFormat.JsonFormat -> AvroJsonOutputStream(output, converter, schema)
      AvroFormat.BinaryFormat -> AvroBinaryOutputStream(output, converter, schema)
   }
}