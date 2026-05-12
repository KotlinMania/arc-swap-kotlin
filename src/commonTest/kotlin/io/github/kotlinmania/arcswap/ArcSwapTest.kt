// port-lint: source src/lib.rs
package io.github.kotlinmania.arcswap

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ArcSwapTest {

    /**
     * Similar to the doc tests of [ArcSwap], but happens more times.
     */
    @Test
    fun swapLoad() {
        repeat(100) {
            val first = "forty-two"
            val arcSwap: ArcSwap<String> = ArcSwapAny(first)
            assertEquals("forty-two", arcSwap.load().value)
            // It can be read multiple times
            assertEquals("forty-two", arcSwap.load().value)

            // Put a new one in there
            val replacement = "zero"
            assertSame(first, arcSwap.swap(replacement))
            assertEquals("zero", arcSwap.load().value)
        }
    }

    @Test
    fun loadNull() {
        val shared: ArcSwapOption<Int> = arcSwapOptionEmpty()
        val guard = shared.load()
        assertNull(guard.value)
        shared.store(42)
        assertEquals(42, shared.load().value)
    }

    @Test
    fun fromInto() {
        val a = "forty-two"
        val shared: ArcSwap<String> = ArcSwapAny(a)
        val guard = shared.load()
        val inner = shared.intoInner()
        assertEquals("forty-two", inner)
        assertEquals("forty-two", guard.value)
    }

    @Test
    fun loadOption() {
        val shared: ArcSwapOption<Int> = ArcSwapAny(42)
        // The type here is not needed in real code, it's just additional test the type matches.
        val opt: Int? = Guard.intoInner(shared.load())
        assertEquals(42, opt)

        shared.store(null)
        assertNull(shared.load().value)
    }

    /**
     * Check stuff can get formatted.
     */
    @Test
    fun debugImpl() {
        val shared: ArcSwap<Int> = ArcSwapAny(42)
        assertEquals("ArcSwapAny(42)", shared.toString())
        assertEquals("42", shared.load().toString())
    }

    /**
     * Handling null values.
     */
    @Test
    fun nulls() {
        val shared: ArcSwapOption<Int> = ArcSwapAny(0)
        val orig = shared.swap(null)
        assertEquals(0, orig)
        val nullGuard = shared.load()
        assertNull(nullGuard.value)
        val a = 42
        val firstSwap = shared.compareAndSwap(null, a)
        assertNull(firstSwap.value)
        val secondSwap = Guard.intoInner(shared.compareAndSwap(null, null))
        // The CAS above guessed wrong (current is `a`, not null), so nothing happened
        // and we observed `a`.
        assertEquals(a, secondSwap)
    }

    /**
     * Multiple sequential RCUs.
     */
    @Test
    fun rcu() {
        val iterations = 50
        val shared: ArcSwap<Int> = ArcSwapAny(0)
        repeat(iterations) {
            shared.rcu { old -> old + 1 }
        }
        assertEquals(iterations, shared.load().value)
    }

    /**
     * Make sure the reference identity and compareAndSwap works as expected.
     */
    @Test
    fun casRefIdentity() {
        val iterations = 50
        // Use boxed Integers so reference identity is meaningful across replacements.
        val zero: Any = Any()
        val shared: ArcSwap<Any> = ArcSwapAny(zero)
        var current = zero
        for (i in 0 until iterations) {
            val next = Any()
            // Success
            val prev = shared.compareAndSwap(current, next)
            assertSame(current, prev.value)
            assertSame(next, shared.load().value)
            // Failure: pass a stale `current`.
            val stale = Any()
            val secondPrev = Guard.intoInner(shared.compareAndSwap(stale, Any()))
            assertSame(next, secondPrev)
            assertSame(next, shared.load().value)
            current = next
        }
    }

    /**
     * We have a callback in RCU. Check what happens if we access the value from within.
     */
    @Test
    fun recursive() {
        val shared: ArcSwap<Int> = ArcSwapAny(0)
        shared.rcu { i ->
            if (i < 10) {
                shared.rcu { j -> j + 1 }
            }
            i
        }
        assertEquals(10, shared.load().value)
    }

    @Test
    fun guardFromInner() {
        val g1: Guard<Int> = Guard.fromInner(7)
        assertEquals(7, g1.value)
        assertEquals(7, Guard.intoInner(g1))
    }

    @Test
    fun storeReplaces() {
        val shared: ArcSwap<String> = ArcSwapAny("a")
        shared.store("b")
        assertEquals("b", shared.load().value)
        assertTrue(shared.toString().contains("b"))
    }
}
