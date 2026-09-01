// port-lint: tests access.rs
package io.github.kotlinmania.arcswap

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessTest {
    private fun checkStaticDispatchDirect(a: Access<Int>) {
        assertEquals(42, a.load().value)
    }

    private fun checkStaticDispatch(a: Access<Int>) {
        assertEquals(42, a.load().value)
    }

    private fun checkDynDispatchDirect(a: DynAccess<Int>) {
        assertEquals(42, a.load().value)
    }

    private fun checkDynDispatch(a: DynAccess<Int>) {
        assertEquals(42, a.load().value)
    }

    private fun checkTransition(a: Access<Int>) {
        checkStaticDispatchDirect(a)
    }

    @Test
    fun staticDispatch() {
        val a = ArcSwap(42)
        checkStaticDispatchDirect(a)
        checkStaticDispatch(a)
    }

    @Test
    fun dynDispatch() {
        val c = Constant(42)
        checkDynDispatchDirect(c.asDyn())
        checkDynDispatch(c.asDyn())
    }

    @Test
    fun transition() {
        val a = ArcSwap(42)
        checkTransition(a)
    }

    @Test
    fun indirect() {
        val a = ArcSwap(42)
        checkStaticDispatch(a)
    }

    private data class Cfg(
        val value: Int,
    )

    @Test
    fun map() {
        val a = ArcSwap(Cfg(value = 42))
        val map = a.map { it.value }
        checkStaticDispatchDirect(map)
    }

    @Test
    fun mapOptionSome() {
        val a = ArcSwapOption(Cfg(value = 42))
        val map = a.map { it?.value ?: 0 }
        checkStaticDispatchDirect(map)
    }

    @Test
    fun mapOptionNone() {
        val a = ArcSwapOption<Cfg>(null)
        val map = a.map { it?.value ?: 42 }
        checkStaticDispatchDirect(map)
    }

    @Test
    fun constant() {
        val c = Constant(42)
        checkStaticDispatchDirect(c)
        checkDynDispatchDirect(c.asDyn())
        checkStaticDispatchDirect(c)
        assertEquals(42, c.deref().value)
    }

    @Test
    fun mapReload() {
        val a = ArcSwap(Cfg(value = 0))
        val map = a.map { it.value }
        assertEquals(0, map.load().value)
        a.store(Cfg(value = 42))
        assertEquals(42, map.load().value)
    }

    private data class Inner(
        val val_: Int,
    )

    private data class Middle(
        val inner: Inner,
    )

    private data class Outer(
        val middle: Middle,
    )

    @Test
    fun doubleDynAccessComplex() {
        val outer = ArcSwap(Outer(middle = Middle(inner = Inner(val_ = 42))))
        val middle = outer.map { it.middle }
        val inner = middle.map { it.inner }
        val guard = inner.load()
        assertEquals(42, guard.value.val_)
    }
}
