package com.sksamuel.avro4k.schema

import com.sksamuel.avro4k.Avro
import com.sksamuel.avro4k.io.AvroInputStream
import com.sksamuel.avro4k.io.AvroOutputStream
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import kotlinx.serialization.Serializable
import java.io.File

fun main() {

   @Serializable
   data class Ingredient(val name: String, val sugar: Double, val fat: Double)

   @Serializable
   data class Pizza(val name: String, val ingredients: List<Ingredient>, val vegetarian: Boolean, val kcals: Int)

   val veg = Pizza("veg", listOf(Ingredient("peppers", 0.1, 0.3), Ingredient("onion", 1.0, 0.4)), true, 265)
   val hawaiian = Pizza("hawaiian", listOf(Ingredient("ham", 1.5, 5.6), Ingredient("pineapple", 5.2, 0.2)), false, 391)

   val schema = Avro.default.schema(Pizza.serializer())

   val output = AvroOutputStream.binary(schema, Pizza.serializer()).to(File("pizzas.avro"))
   output.write(listOf(veg, hawaiian))
   output.close()

   val input = AvroInputStream.binary(Pizza.serializer(), schema).from(File("pizzas.avro"))
   input.iterator().forEach { println(it) }
   input.close()
}

@Serializable
data class Nested(val goo: String)

@Serializable
data class Test(val foo: String, val nested: Nested)

class BasicSchemaTest : FunSpec({

  test("schema for basic types") {

    @Serializable
    data class Foo(val a: String,
                   val b: Double,
                   val c: Boolean,
                   val d: Float,
                   val e: Long,
                   val f: Int,
                   val g: Short,
                   val h: Byte)


    val expected = org.apache.avro.Schema.Parser().parse(this::class.java.getResourceAsStream("/basic.json"))
    val schema = Avro.default.schema(Foo.serializer())
    schema.toString(true) shouldBe expected.toString(true)
  }

  test("accept nested case classes") {
    val expected = org.apache.avro.Schema.Parser().parse(this::class.java.getResourceAsStream("/nested.json"))
    val schema = Avro.default.schema(Test.serializer())
    schema.toString(true) shouldBe expected.toString(true)
  }

  test("accept multiple nested case classes") {

    @Serializable
    data class Inner(val goo: String)

    @Serializable
    data class Middle(val inner: Inner)

    @Serializable
    data class Outer(val middle: Middle)

    val expected = org.apache.avro.Schema.Parser().parse(this::class.java.getResourceAsStream("/nested_multiple.json"))
    val schema = Avro.default.schema(Outer.serializer())
    schema.toString(true) shouldBe expected.toString(true)
  }

  test("accept deep nested structure") {

    @Serializable
    data class Level4(val str: String)

    @Serializable
    data class Level3(val level4: Level4)

    @Serializable
    data class Level2(val level3: Level3)

    @Serializable
    data class Level1(val level2: Level2)

    val expected = org.apache.avro.Schema.Parser().parse(this::class.java.getResourceAsStream("/deepnested.json"))
    val schema = Avro.default.schema(Level1.serializer())
    schema.toString(true) shouldBe expected.toString(true)
  }
})