// port-lint: source access.rs
package io.github.kotlinmania.arcswap

/**
 * Abstracts over ways code can get access to a value of type [T].
 *
 * This is the interface that parts of code will use when accessing a subpart of a
 * data structure.
 */
interface Access<out T> {
    /**
     * The loading method.
     *
     * This returns the guard that holds the actual value. Should be called anew each time
     * a fresh value is needed.
     */
    fun load(): Guard<T>
}

/**
 * Dynamic access interface allowing object-safe type erasure.
 */
interface DynAccess<out T> {
    /**
     * Loads dynamic guard holding the value.
     */
    fun load(): DynGuard<T>
}

/**
 * A guard object returned by dynamic access handles.
 */
class DynGuard<out T>(
    private val guardedValue: T,
) {
    /**
     * The value protected by this guard.
     */
    val value: T get() = guardedValue
}

/**
 * A guard object returned by mapped access handles.
 */
class MapGuard<out T>(
    private val guardedValue: T,
) {
    /**
     * The value protected by this guard.
     */
    val value: T get() = guardedValue
}

/**
 * An implementation of [Access] that applies a transformation projection to the loaded value.
 */
class Map<T, out U>(
    private val inner: Access<T>,
    private val projection: (T) -> U,
) : Access<U> {
    override fun load(): Guard<U> {
        val g = inner.load()
        return Guard(projection(g.value))
    }

    /**
     * Creates a chained [Map] projection.
     */
    fun <V> map(nextProjection: (U) -> V): Map<T, V> = Map(inner) { nextProjection(projection(it)) }

    companion object {
        /**
         * Creates a new [Map] wrapper.
         */
        fun <T, U> new(inner: Access<T>, projection: (T) -> U): Map<T, U> = Map(inner, projection)
    }
}

/**
 * Type alias for [Map] access.
 */
typealias MapAccess<T, U> = Map<T, U>

/**
 * Guard object for [Constant] access.
 */
class ConstantDeref<out T>(
    private val constantValue: T,
) {
    /**
     * The value held by this constant deref.
     */
    val value: T get() = constantValue
}

/**
 * An implementation of [Access] that holds a constant value.
 */
class Constant<out T>(
    private val value: T,
) : Access<T> {
    override fun load(): Guard<T> = Guard(value)

    /**
     * Returns a dynamic guard holding the constant value.
     */
    fun loadDyn(): DynGuard<T> = DynGuard(value)

    /**
     * Returns a constant deref holding the value.
     */
    fun deref(): ConstantDeref<T> = ConstantDeref(value)

    /**
     * Returns a [DynAccess] view of this constant.
     */
    fun asDyn(): DynAccess<T> =
        object : DynAccess<T> {
            override fun load(): DynGuard<T> = DynGuard(value)
        }

    companion object {
        /**
         * Creates a new [Constant] access.
         */
        fun <T> new(value: T): Constant<T> = Constant(value)
    }
}
