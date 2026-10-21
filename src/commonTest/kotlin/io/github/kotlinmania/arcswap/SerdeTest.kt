// port-lint: tests serde.rs
package io.github.kotlinmania.arcswap

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.IgnoredAny
import io.github.kotlinmania.serdecore.de.MapAccess
import io.github.kotlinmania.serdecore.de.StringDeserialize
import io.github.kotlinmania.serdecore.de.U64Deserialize
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertTokens
import kotlin.test.Test

class ArcSwapAnyEq<T>(
    val inner: ArcSwapAny<T>,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        inner.serialize(serializer)

    fun eq(other: ArcSwapAnyEq<T>): Boolean =
        inner.load().value == other.inner.load().value

    override fun equals(other: Any?): Boolean =
        other is ArcSwapAnyEq<*> && inner.load().value == other.inner.load().value

    override fun hashCode(): Int = inner.load().value?.hashCode() ?: 0

    override fun toString(): String = "ArcSwapAnyEq(${inner.load().value})"

    companion object {
        fun <T> deserialize(innerDeserialize: Deserialize<T>): Deserialize<ArcSwapAnyEq<T>> =
            object : Deserialize<ArcSwapAnyEq<T>> {
                override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<ArcSwapAnyEq<T>> =
                    ArcSwapDeserialize(innerDeserialize).deserialize(deserializer).map { ArcSwapAnyEq(it) }
            }

        fun <T> optionDeserialize(innerDeserialize: Deserialize<T>): Deserialize<ArcSwapAnyEq<T?>> =
            object : Deserialize<ArcSwapAnyEq<T?>> {
                override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<ArcSwapAnyEq<T?>> =
                    ArcSwapOptionDeserialize(innerDeserialize).deserialize(deserializer).map { ArcSwapAnyEq(it) }
            }
    }
}

data class Foo(
    val field0: ULong,
    val field1: String,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
        val struct = serializer.serializeStruct("Foo", 2).getOrThrow()
        struct
            .serializeField(
                "field0",
                object : Serialize {
                    override fun <O> serialize(serializer: Serializer<O>): SerdeResult<O> = serializer.serializeU64(field0)
                },
            ).getOrThrow()
        struct
            .serializeField(
                "field1",
                object : Serialize {
                    override fun <O> serialize(serializer: Serializer<O>): SerdeResult<O> = serializer.serializeStr(field1)
                },
            ).getOrThrow()
        return struct.end()
    }

    companion object : Deserialize<Foo> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Foo> =
            deserializer.deserializeStruct("Foo", listOf("field0", "field1"), FooVisitor)
    }

    private object FooVisitor : Visitor<Foo> {
        override fun expecting(): String = "struct Foo"

        override fun <A : MapAccess> visitMap(access: A): SerdeResult<Foo> {
            var field0: ULong? = null
            var field1: String? = null
            while (true) {
                val key = access.nextKey(StringDeserialize).getOrThrow() ?: break
                when (key) {
                    "field0" -> field0 = access.nextValue(U64Deserialize).getOrThrow()
                    "field1" -> field1 = access.nextValue(StringDeserialize).getOrThrow()
                    else -> access.nextValue(IgnoredAny).getOrThrow()
                }
            }
            if (field0 == null || field1 == null) {
                return SerdeResult.failure(SerdeError.custom("missing field"))
            }
            return SerdeResult.success(Foo(field0, field1))
        }
    }
}

data class Bar(
    val field0: ArcSwapAnyEq<ULong>,
    val field1: ArcSwapAnyEq<String?>,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
        val struct = serializer.serializeStruct("Bar", 2).getOrThrow()
        struct.serializeField("field0", field0).getOrThrow()
        struct.serializeField("field1", field1).getOrThrow()
        return struct.end()
    }

    companion object : Deserialize<Bar> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Bar> =
            deserializer.deserializeStruct("Bar", listOf("field0", "field1"), BarVisitor)
    }

    private object BarVisitor : Visitor<Bar> {
        override fun expecting(): String = "struct Bar"

        override fun <A : MapAccess> visitMap(access: A): SerdeResult<Bar> {
            var field0: ArcSwapAnyEq<ULong>? = null
            var field1: ArcSwapAnyEq<String?>? = null
            while (true) {
                val key = access.nextKey(StringDeserialize).getOrThrow() ?: break
                when (key) {
                    "field0" -> field0 = access.nextValue(ArcSwapAnyEq.deserialize(U64Deserialize)).getOrThrow()
                    "field1" -> field1 = access.nextValue(ArcSwapAnyEq.optionDeserialize(StringDeserialize)).getOrThrow()
                    else -> access.nextValue(IgnoredAny).getOrThrow()
                }
            }
            if (field0 == null || field1 == null) {
                return SerdeResult.failure(SerdeError.custom("missing field"))
            }
            return SerdeResult.success(Bar(field0, field1))
        }
    }
}

class SerdeTest {
    @Test
    fun testSerializeDeserialize() {
        val field0 = ULong.MAX_VALUE
        val field1 = "FOO_-0123456789"

        val dataOrig =
            Foo(
                field0 = field0,
                field1 = field1,
            )
        val data = ArcSwapAnyEq(ArcSwap.fromPointee(dataOrig))
        assertTokens(
            data,
            ArcSwapAnyEq.deserialize(Foo),
            listOf(
                Token.Struct("Foo", 2),
                Token.Str("field0"),
                Token.U64(ULong.MAX_VALUE),
                Token.Str("field1"),
                Token.StringValue(field1),
                Token.StructEnd,
            ),
        )

        val bar =
            Bar(
                field0 = ArcSwapAnyEq(ArcSwap.fromPointee(field0)),
                field1 = ArcSwapAnyEq(arcSwapOptionFromPointee(field1)),
            )
        assertTokens(
            bar,
            Bar,
            listOf(
                Token.Struct("Bar", 2),
                Token.Str("field0"),
                Token.U64(ULong.MAX_VALUE),
                Token.Str("field1"),
                Token.Some,
                Token.StringValue(field1),
                Token.StructEnd,
            ),
        )
    }

    @Test
    fun testSerializeDeserializeOption() {
        val field0 = ULong.MAX_VALUE
        val field1 = "FOO_-0123456789"

        val dataOrig =
            Foo(
                field0 = field0,
                field1 = field1,
            )
        val data = ArcSwapAnyEq(arcSwapOptionFromPointee(dataOrig))
        assertTokens(
            data,
            ArcSwapAnyEq.optionDeserialize(Foo),
            listOf(
                Token.Some,
                Token.Struct("Foo", 2),
                Token.Str("field0"),
                Token.U64(ULong.MAX_VALUE),
                Token.Str("field1"),
                Token.StringValue(field1),
                Token.StructEnd,
            ),
        )
    }

    @Test
    fun testSerializeDeserializeOptionNone() {
        val data = ArcSwapAnyEq<Foo?>(arcSwapOptionFromPointee(null))

        assertTokens(
            data,
            ArcSwapAnyEq.optionDeserialize(Foo),
            listOf(Token.None),
        )
    }
}
