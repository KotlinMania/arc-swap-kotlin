// port-lint: source lib.rs
package io.github.kotlinmania.arcswap

/**
 * A temporary storage of the pointer.
 *
 * This guard object is returned from most loading methods (with the notable exception of
 * [ArcSwapAny.loadFull]). It exposes the value loaded, so most operations are to be done
 * using that.
 */
class Guard<out T> internal constructor(
    internal val protected: T,
) {
    /**
     * The value held by this guard.
     */
    val value: T get() = protected

    override fun toString(): String = protected.toString()

    override fun equals(other: Any?): Boolean =
        other is Guard<*> && protected == other.protected

    override fun hashCode(): Int = protected.hashCode()

    companion object {
        /**
         * Converts a guard into the held value.
         *
         * This, on occasion, may be a tiny bit faster than cloning the value or whatever
         * is being held inside.
         */
        fun <T> intoInner(lease: Guard<T>): T = lease.protected

        /**
         * Create a guard for a given value `inner`.
         *
         * This can be useful on occasion to pass a specific object to code that expects
         * or wants to store a [Guard].
         *
         * Example:
         * ```kotlin
         * val p = ArcSwap(42)
         * // Create two guards pointing to the same object
         * val g1 = p.load()
         * val g2 = Guard.fromInner(g1.value)
         * ```
         */
        fun <T> fromInner(inner: T): Guard<T> = Guard(inner)
    }
}
