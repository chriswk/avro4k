package com.sksamuel.avro4k

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import kotlinx.serialization.Serializable

class RecordEncoderTest : FunSpec({

   test("encoding basic data class") {

      @Serializable
      data class Foo(val a: String, val b: Double, val c: Boolean)

      // Avro.default.encodeToByteArray(Foo.serializer(), Foo("hello", 123.456, true)) shouldBe ""
   }

   test("to/from records of sets of ints") {

      @Serializable
      data class S(val t: Set<Int>)

      val r = Avro.default.toRecord(S.serializer(), S(setOf(1)))
      val s = Avro.default.fromRecord(S.serializer(), r)  // this line fails
      s.t shouldBe setOf(1)
   }
})