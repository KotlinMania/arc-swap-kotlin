// port-lint: source lib.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.arcswap

import kotlin.concurrent.atomics.AtomicReference
import kotlin.native.HiddenFromObjC

/**
 * An atomic storage for a reference-counted smart pointer.
 *
 * This is a storage where a value may live. It can be read and written atomically from
 * several threads, but doesn't act like a pointer itself.
 *
 * One can be created from a value. To get the value back, use [load].
 *
 * # Note
 *
 * This is the common generic implementation. It allows sharing the same code for storing
 * both non-null and nullable references.
 *
 * In your code, you most probably want to interact with it through the [ArcSwap] and
 * [ArcSwapOption] aliases. However, the methods they share are described here and are
 * applicable to both of them.
 *
 * # Type parameters
 *
 * * `T`: The value type to be kept inside. Use [ArcSwap] for non-null values and
 *   [ArcSwapOption] for nullable ones.
 *
 * # Examples
 *
 * ```kotlin
 * val arcSwap = ArcSwap(42)
 * check(arcSwap.load().value == 42)
 * // It can be read multiple times
 * check(arcSwap.load().value == 42)
 *
 * // Put a new one in there
 * check(arcSwap.swap(0) == 42)
 * check(arcSwap.load().value == 0)
 * ```
 */
@HiddenFromObjC
class ArcSwapAny<T>(value: T) {

    /**
     * The actual reference holder. Kotlin's garbage collector handles reclamation, so no
     * hazard-pointer machinery is required.
     */
    private val ptr: AtomicReference<T> = AtomicReference(value)

    /**
     * Extracts the value inside.
     *
     * In Rust this consumed `self`. Kotlin's garbage collector reclaims the storage when
     * no references remain, so the call simply returns the currently held value.
     */
    fun intoInner(): T = ptr.load()

    /**
     * Loads the value.
     *
     * This makes another copy of the held reference and returns it, atomically (it is
     * safe even when another thread stores into the same instance at the same time).
     */
    fun loadFull(): T = ptr.load()

    /**
     * Provides a temporary borrow of the object inside.
     *
     * This returns a proxy object allowing access to the thing held inside.
     *
     * # Consistency
     *
     * In case multiple related operations are to be done on the loaded value, it is
     * generally recommended to call [load] just once and keep the result over calling it
     * multiple times. First, keeping it is usually faster. But more importantly, the
     * value can change between the calls to [load], returning different objects, which
     * could lead to logical inconsistency. Keeping the result makes sure the same object
     * is used.
     *
     * ```kotlin
     * data class Point(val x: Int, val y: Int)
     *
     * fun printBroken(p: ArcSwap<Point>) {
     *     // This is broken, because the x and y may come from different points,
     *     // combining into an invalid point that never existed.
     *     println("X: ${p.load().value.x}")
     *     // If someone changes the content now, between these two loads, we
     *     // have a problem
     *     println("Y: ${p.load().value.y}")
     * }
     *
     * fun printCorrect(p: ArcSwap<Point>) {
     *     // Here we take a snapshot of one specific point so both x and y come
     *     // from the same one.
     *     val point = p.load().value
     *     println("X: ${point.x}")
     *     println("Y: ${point.y}")
     * }
     * ```
     */
    fun load(): Guard<T> = Guard(ptr.load())

    /**
     * Replaces the value inside this instance.
     *
     * Further loads will yield the new value. Uses [swap] internally.
     */
    fun store(value: T) {
        swap(value)
    }

    /**
     * Exchanges the value inside this instance.
     */
    fun swap(new: T): T = ptr.exchange(new)

    /**
     * Swaps the stored value if it equals to `current` by reference identity.
     *
     * If the current value of the [ArcSwapAny] is reference-equal to `current`, the `new`
     * is stored inside. If not, nothing happens.
     *
     * The previous value (no matter if the swap happened or not) is returned. Therefore,
     * if the returned value is reference-equal to `current`, the swap happened. Use the
     * `===` operator for the comparison.
     *
     * In other words, if the caller "guesses" the value of current correctly, it acts
     * like [swap], otherwise it acts like [loadFull].
     */
    fun compareAndSwap(current: T, new: T): Guard<T> =
        Guard(ptr.compareAndExchange(current, new))

    /**
     * Read-Copy-Update of the value inside.
     *
     * This is useful in read-heavy situations with several threads that sometimes update
     * the data pointed to. The readers can just repeatedly use [load] without any
     * locking. The writer uses this method to perform the update.
     *
     * In case there's only one thread that does updates or in case the next version is
     * independent of the previous one, simple [swap] or [store] is enough. Otherwise, it
     * may be needed to retry the update operation if some other thread made an update in
     * between. This is what this method does.
     *
     * # Examples
     *
     * This will *not* work as expected, because between loading and storing, some other
     * thread might have updated the value.
     *
     * ```kotlin
     * val cnt = ArcSwap(0)
     * // From multiple threads, concurrently:
     * val inner = cnt.loadFull()
     * // Another thread might have stored some other number than what we have
     * // between the load and store.
     * cnt.store(inner + 1)
     * ```
     *
     * This will, but it can call the closure multiple times to retry:
     *
     * ```kotlin
     * val cnt = ArcSwap(0)
     * // From multiple threads, concurrently:
     * cnt.rcu { inner -> inner + 1 }
     * ```
     *
     * Due to the retries, you might want to perform all the expensive operations
     * *before* the rcu. As an example, if there's a cache of some computations as a map,
     * and the map is cheap to clone but the computations are not, you could do something
     * like this:
     *
     * ```kotlin
     * fun expensiveComputation(x: Int): Int = x * 2 // Pretend multiplication is expensive
     *
     * val cache: ArcSwap<Map<Int, Int>> = ArcSwap(emptyMap())
     *
     * fun cachedComputation(x: Int): Int {
     *     val current = cache.load().value
     *     current[x]?.let { return it }
     *     // Not in cache. Compute and store.
     *     // The expensive computation goes outside, so it is not retried.
     *     val result = expensiveComputation(x)
     *     cache.rcu { snapshot ->
     *         // The cheaper clone of the cache can be retried if need be.
     *         snapshot + (x to result)
     *     }
     *     return result
     * }
     * ```
     */
    fun rcu(f: (T) -> T): T {
        var cur = load()
        while (true) {
            val new = f(cur.value)
            val prev = compareAndSwap(cur.value, new)
            if (cur.value === prev.value) {
                return Guard.intoInner(prev)
            } else {
                cur = prev
            }
        }
    }

    override fun toString(): String = "ArcSwapAny(${ptr.load()})"

    override fun equals(other: Any?): Boolean =
        other is ArcSwapAny<*> && ptr.load() == other.ptr.load()

    override fun hashCode(): Int = ptr.load().hashCode()
}
