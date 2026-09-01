// port-lint: tests cache.rs
package io.github.kotlinmania.arcswap

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CacheTest {
    @Test
    fun cachedValue() {
        val a = ArcSwap(42)
        val c1 = Cache(a)
        val c2 = Cache(a)

        assertEquals(42, c1.load())
        assertEquals(42, c2.load())

        a.store(43)
        assertEquals(42, c1.loadNoRevalidate())
        assertEquals(43, c1.load())
    }

    @Test
    fun cachedThroughArc() {
        val a = ArcSwap(42)
        val c = Cache(a)
        assertEquals(42, c.load())
        a.store(0)
        assertEquals(0, c.load())
    }

    @Test
    fun cacheOption() {
        val a = ArcSwapOption<Int>(42)
        val c = Cache(a)

        assertEquals(42, c.load())
        a.store(null)
        assertNull(c.load())
    }

    private data class Inner(
        val answer: Int,
    )

    private data class Outer(
        val inner: Inner,
    )

    @Test
    fun mapCache() {
        val a = ArcSwap(Outer(inner = Inner(answer = 42)))

        val cache = Cache(a)
        val inner = cache.map { it.inner }
        val answer = cache.map { it.inner.answer }

        assertEquals(42, cache.load().inner.answer)
        assertEquals(42, inner.load().answer)
        assertEquals(42, answer.load())

        a.store(Outer(inner = Inner(answer = 24)))

        assertEquals(24, cache.load().inner.answer)
        assertEquals(24, inner.load().answer)
        assertEquals(24, answer.load())
    }
}
