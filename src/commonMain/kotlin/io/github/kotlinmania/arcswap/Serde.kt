// port-lint: source serde.rs
package io.github.kotlinmania.arcswap

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.nullableDeserialize
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes an arbitrary [value] into [serializer].
 */
@Suppress("UNCHECKED_CAST")
fun <Ok> serializeAnyValue(value: Any?, serializer: Serializer<Ok>): SerdeResult<Ok> =
    when (value) {
        null -> serializer.serializeNone()
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
        is ArcSwapAny<*> -> value.serialize(serializer)
        is Guard<*> -> serializeAnyValue(value.value, serializer)
        else -> SerdeResult.failure(SerdeError.custom("Cannot serialize ${value::class}"))
    }

/**
 * A wrapper allowing arbitrary values to be passed to APIs expecting [Serialize].
 */
class ValueSerializer(
    private val value: Any?,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializeAnyValue(value, serializer)
}

/**
 * Deserializer implementation for [ArcSwapAny].
 */
fun <T> arcSwapDeserialize(innerDeserialize: Deserialize<T>): Deserialize<ArcSwap<T>> =
    object : Deserialize<ArcSwap<T>> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<ArcSwap<T>> =
            innerDeserialize.deserialize(deserializer).map { ArcSwap(it) }
    }

/**
 * Deserializer implementation for nullable [ArcSwapOption].
 */
fun <T> arcSwapOptionDeserialize(innerDeserialize: Deserialize<T>): Deserialize<ArcSwapOption<T>> =
    object : Deserialize<ArcSwapOption<T>> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<ArcSwapOption<T>> =
            nullableDeserialize(innerDeserialize).deserialize(deserializer).map { arcSwapOption(it) }
    }

/**
 * kotlinx.serialization serializer for [ArcSwapAny].
 */
class ArcSwapSerializer<T>(
    private val elementSerializer: KSerializer<T>,
) : KSerializer<ArcSwap<T>> {
    override val descriptor: SerialDescriptor = elementSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ArcSwap<T>) {
        elementSerializer.serialize(encoder, value.load().value)
    }

    override fun deserialize(decoder: Decoder): ArcSwap<T> =
        ArcSwap(elementSerializer.deserialize(decoder))
}
