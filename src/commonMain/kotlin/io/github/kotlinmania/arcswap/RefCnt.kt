// port-lint: source ref_cnt.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.arcswap

import kotlin.native.HiddenFromObjC

/**
 * An opaque identity pointer used by [RefCnt].
 *
 * Kotlin common source does not expose memory addresses. This value carries the
 * same observable identity relationship the upstream file needs: two pointers
 * are equal only when they point to the same object, and [nullPtr] represents
 * the nullable smart-reference case.
 */
@HiddenFromObjC
class RefPtr<out T : Any> private constructor(
    val value: T?,
) {
    val isNull: Boolean get() = value == null

    infix fun pointsToSameObjectAs(other: RefPtr<*>): Boolean =
        value === other.value

    override fun equals(other: Any?): Boolean =
        other is RefPtr<*> && pointsToSameObjectAs(other)

    override fun hashCode(): Int = 0

    override fun toString(): String =
        if (isNull) "RefPtr(null)" else "RefPtr($value)"

    companion object {
        fun <T : Any> of(value: T): RefPtr<T> = RefPtr(value)

        fun <T : Any> nullPtr(): RefPtr<T> = RefPtr(null)
    }
}

/**
 * A trait describing smart reference counted pointers.
 *
 * A nullable smart reference, represented here by [NullableRefCnt], is also a
 * smart reference counted pointer; it is just one that can hold [RefPtr.nullPtr].
 *
 * Implementations must preserve the invariants this library relies on. If two
 * pointer values compare equal, they must point to the same object. Having
 * fewer references tracked than the number of live Kotlin references is fine as
 * long as the object remains reachable through normal Kotlin references.
 *
 * This is mostly a reuse hook for non-null and nullable variants. Downstream
 * libraries are not expected to implement it unless they provide their own
 * reference-counted holder.
 */
@HiddenFromObjC
interface RefCnt<P, Base : Any> {
    /**
     * Makes another smart reference to the same object.
     */
    fun clonePointer(me: P): P

    /**
     * Converts the smart pointer into an opaque identity pointer without
     * changing the effective reference count.
     */
    fun intoPtr(me: P): RefPtr<Base>

    /**
     * Provides a borrowed view into the smart pointer as an opaque identity
     * pointer.
     */
    fun asPtr(me: P): RefPtr<Base>

    /**
     * Converts an opaque identity pointer back into the smart pointer without
     * changing the effective reference count.
     *
     * This is intended only for values previously returned by [intoPtr] or
     * [asPtr]. Kotlin garbage collection keeps the pointed object alive while
     * ordinary references to it remain reachable.
     */
    fun fromPtr(ptr: RefPtr<Base>): P

    /**
     * Increments the reference count by one and returns the pointer to the
     * inner object as a side effect.
     */
    fun inc(me: P): RefPtr<Base> = intoPtr(clonePointer(me))

    /**
     * Decrements the reference count by one.
     *
     * Kotlin has no deterministic reference-count decrement for ordinary
     * objects, so converting the pointer back is the complete observable effect.
     */
    fun dec(ptr: RefPtr<Base>) {
        fromPtr(ptr).let { }
    }
}

/**
 * [RefCnt] implementation for ordinary non-null Kotlin references.
 *
 * The upstream non-null holder implementations both collapse to this shape in
 * common Kotlin because the runtime, not the library, owns object reachability.
 */
@HiddenFromObjC
class StrongRefCnt<T : Any> : RefCnt<T, T> {
    override fun clonePointer(me: T): T = me

    override fun intoPtr(me: T): RefPtr<T> = RefPtr.of(me)

    override fun asPtr(me: T): RefPtr<T> = RefPtr.of(me)

    override fun fromPtr(ptr: RefPtr<T>): T =
        requireNotNull(ptr.value) { "a null pointer cannot restore a non-null reference" }
}

/**
 * [RefCnt] implementation for nullable smart references.
 */
@HiddenFromObjC
class NullableRefCnt<T : Any>(
    private val inner: RefCnt<T, T> = StrongRefCnt(),
) : RefCnt<T?, T> {
    override fun clonePointer(me: T?): T? = me

    override fun intoPtr(me: T?): RefPtr<T> =
        me?.let(inner::intoPtr) ?: RefPtr.nullPtr()

    override fun asPtr(me: T?): RefPtr<T> =
        me?.let(inner::asPtr) ?: RefPtr.nullPtr()

    override fun fromPtr(ptr: RefPtr<T>): T? =
        if (ptr.isNull) null else inner.fromPtr(ptr)
}

/**
 * A pinned reference wrapper.
 *
 * Kotlin common code does not expose movable storage to callers. This
 * wrapper preserves the upstream intent by keeping a stable object reference
 * behind a distinct smart-reference type.
 */
@HiddenFromObjC
class PinnedRef<out T : Any> private constructor(
    val value: T,
) {
    companion object {
        fun <T : Any> of(value: T): PinnedRef<T> = PinnedRef(value)
    }
}

/**
 * [RefCnt] implementation for pinned references.
 */
@HiddenFromObjC
class PinnedRefCnt<T : Any>(
    private val inner: RefCnt<T, T> = StrongRefCnt(),
) : RefCnt<PinnedRef<T>, T> {
    override fun clonePointer(me: PinnedRef<T>): PinnedRef<T> = me

    override fun intoPtr(me: PinnedRef<T>): RefPtr<T> =
        inner.intoPtr(me.value)

    override fun asPtr(me: PinnedRef<T>): RefPtr<T> =
        inner.asPtr(me.value)

    override fun fromPtr(ptr: RefPtr<T>): PinnedRef<T> =
        PinnedRef.of(inner.fromPtr(ptr))
}
