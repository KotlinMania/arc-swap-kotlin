// port-lint: source as_raw.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.arcswap

import kotlin.native.HiddenFromObjC

/**
 * An interface describing things that can be turned into an opaque identity pointer.
 *
 * This is an abstraction of values that can be passed to compare-and-swap style
 * operations when the comparison is about object identity rather than value
 * equality.
 *
 * # Examples
 *
 * ```kotlin
 * val value = Any()
 * val shared: ArcSwapOption<Any> = ArcSwapAny(value)
 *
 * shared.compareAndSwap(value, value)
 * shared.compareAndSwap(null, value)
 * shared.compareAndSwap(shared.load().value, value)
 * shared.compareAndSwap(Guard.intoInner(shared.load()), value)
 * ```
 *
 * Due to a technical limitation, this is modeled through lightweight adapter
 * values instead of being implemented directly by every owned object and
 * nullable holder.
 */
@HiddenFromObjC
interface AsRaw<out T : Any> {
    /**
     * Converts the value into an opaque identity pointer.
     */
    fun asRaw(): RefPtr<T>
}

@HiddenFromObjC
private sealed interface Sealed

@HiddenFromObjC
private class RawIdentity<out T : Any>(
    private val pointer: RefPtr<T>,
) : AsRaw<T>, Sealed {
    override fun asRaw(): RefPtr<T> = pointer
}

/**
 * Creates an [AsRaw] adapter for an existing opaque pointer.
 */
fun <T : Any> asRawPtr(pointer: RefPtr<T>): AsRaw<T> =
    RawIdentity(pointer)

/**
 * Creates an [AsRaw] adapter for a non-null value.
 */
fun <T : Any> asRaw(value: T): AsRaw<T> =
    RawIdentity(RefPtr.of(value))

/**
 * Creates an [AsRaw] adapter for a nullable value.
 */
fun <T : Any> asRawNullable(value: T?): AsRaw<T> =
    RawIdentity(value?.let { RefPtr.of(it) } ?: RefPtr.nullPtr())

/**
 * Creates an [AsRaw] adapter for the value protected by a [Guard].
 */
fun <T : Any> asRawGuard(guard: Guard<T>): AsRaw<T> =
    asRaw(guard.value)

/**
 * Creates an [AsRaw] adapter for a nullable value protected by a [Guard].
 */
fun <T : Any> asRawNullableGuard(guard: Guard<T?>): AsRaw<T> =
    asRawNullable(guard.value)

/**
 * Creates an [AsRaw] adapter using the provided reference-counting operations.
 */
fun <Pointer, Base : Any> asRawRefCnt(
    value: Pointer,
    refCnt: RefCnt<Pointer, Base>,
): AsRaw<Base> =
    RawIdentity(refCnt.asPtr(value))
