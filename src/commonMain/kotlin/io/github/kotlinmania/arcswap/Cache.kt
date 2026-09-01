// port-lint: source cache.rs
package io.github.kotlinmania.arcswap

/**
 * Generalization of caches providing access to [T].
 *
 * This abstracts over all kinds of caches that can provide cheap access to values of type [T].
 * This is useful in cases where some code doesn't care if the [T] is the whole structure or just
 * a part of it.
 */
interface CacheAccess<out T> {
    /**
     * Loads the value from cache.
     *
     * This revalidates the value in the cache, then provides access to the cached value.
     */
    fun load(): T
}

/**
 * Caching handle for [ArcSwapAny].
 *
 * Instead of loading on every request from shared storage, this keeps another copy inside
 * itself. Upon request it only cheaply revalidates that it is up to date. If it is, access
 * is significantly faster. If it is stale, the value is replaced.
 */
class Cache<T>(
    private val arcSwap: ArcSwapAny<T>,
) : CacheAccess<T> {
    private var cached: T = arcSwap.loadFull()

    /**
     * Creates a new caching handle for the given [arcSwap].
     */
    constructor(arcSwap: ArcSwapAny<T>, initial: T) : this(arcSwap) {
        this.cached = initial
    }

    /**
     * Gives access to the underlying [ArcSwapAny].
     */
    fun arcSwap(): ArcSwapAny<T> = arcSwap

    /**
     * Loads the currently held value.
     *
     * This first checks if the cached value is up to date.
     * If it is up to date, the cached value is simply returned. If it is outdated,
     * a load is done on the underlying shared storage.
     */
    override fun load(): T {
        revalidate()
        return loadNoRevalidate()
    }

    /**
     * Loads the cached value without checking if it is up to date.
     */
    fun loadNoRevalidate(): T = cached

    /**
     * Revalidates that the cached value is up to date.
     */
    fun revalidate() {
        val current = arcSwap.loadFull()
        if (cached !== current) {
            cached = current
        }
    }

    /**
     * Turns this cache into a cache with a projection inside the cached value.
     */
    fun <U> map(projection: (T) -> U): MapCache<T, U> = MapCache(this, projection)

    companion object {
        /**
         * Creates a new [Cache] from an [ArcSwapAny].
         */
        fun <T> new(arcSwap: ArcSwapAny<T>): Cache<T> = Cache(arcSwap)

        /**
         * Creates a new [Cache] from an [ArcSwapAny].
         */
        fun <T> from(arcSwap: ArcSwapAny<T>): Cache<T> = Cache(arcSwap)
    }
}

/**
 * An implementation of a cache with a projection into the accessed value.
 */
class MapCache<T, out U>(
    private val inner: Cache<T>,
    private val projection: (T) -> U,
) : CacheAccess<U> {
    override fun load(): U = projection(inner.load())

    /**
     * Creates a further mapped cache from this mapped cache.
     */
    fun <V> map(nextProjection: (U) -> V): MapCache<T, V> = MapCache(inner) { nextProjection(projection(it)) }
}
