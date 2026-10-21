// port-lint: source serde.rs
package io.github.kotlinmania.arcswap

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.nullableDeserialize
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer

private class ValueSerialize(
    private val value: Any,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        when (value) {
            is Serialize -> value.serialize(serializer)
            is Boolean -> serializer.serializeBool(value)
            is Byte -> serializer.serializeI8(value)
            is Short -> serializer.serializeI16(value)
            is Int -> serializer.serializeI32(value)
            is Long -> serializer.serializeI64(value)
            is UByte -> serializer.serializeU8(value)
            is UShort -> serializer.serializeU16(value)
            is UInt -> serializer.serializeU32(value)
            is ULong -> serializer.serializeU64(value)
            is Float -> serializer.serializeF32(value)
            is Double -> serializer.serializeF64(value)
            is Char -> serializer.serializeChar(value)
            is String -> serializer.serializeStr(value)
            is ByteArray -> serializer.serializeBytes(value)
            else -> SerdeResult.failure(SerdeError.custom("Cannot serialize ${value::class}"))
        }
}

/**
 * Serializes an [ArcSwapAny] by loading the inner value and delegating serialization.
 */
fun <T, Ok> serialize(arcSwap: ArcSwapAny<T>, serializer: Serializer<Ok>): SerdeResult<Ok> {
    val loaded = arcSwap.load().value
    return if (arcSwap.isOption) {
        if (loaded != null) {
            serializer.serializeSome(ValueSerialize(loaded))
        } else {
            serializer.serializeNone()
        }
    } else {
        if (loaded != null) {
            ValueSerialize(loaded).serialize(serializer)
        } else {
            serializer.serializeNone()
        }
    }
}

/**
 * Deserializes an [ArcSwapAny] using the given deserializer and inner type deserializer.
 */
fun <T, D : Deserializer> deserialize(
    deserializer: D,
    innerDeserialize: Deserialize<T>,
): SerdeResult<ArcSwap<T>> =
    innerDeserialize.deserialize(deserializer).map { ArcSwap(it) }

/**
 * Compares two [ArcSwapAny] instances for equality by their loaded inner values.
 */
fun <T> eq(a: ArcSwapAny<T>, b: ArcSwapAny<T>): Boolean =
    a.load().value == b.load().value

/**
 * [Deserialize] implementation for [ArcSwap].
 */
class ArcSwapDeserialize<T>(
    private val innerDeserialize: Deserialize<T>,
) : Deserialize<ArcSwap<T>> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<ArcSwap<T>> =
        deserialize(deserializer, innerDeserialize)
}

/**
 * [Deserialize] implementation for nullable [ArcSwapOption].
 */
class ArcSwapOptionDeserialize<T>(
    private val innerDeserialize: Deserialize<T>,
) : Deserialize<ArcSwapOption<T>> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<ArcSwapOption<T>> =
        nullableDeserialize(innerDeserialize).deserialize(deserializer).map { arcSwapOption(it) }
}
