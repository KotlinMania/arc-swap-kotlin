// port-lint: source ref_cnt.rs
package io.github.kotlinmania.arcswap

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RefCntTest {
    data class Data(
        val value: Int,
    )

    data class Unmovable(
        val value: Int,
    )

    @Test
    fun refCntArc() {
        val refCnt = StrongRefCnt<Data>()
        val data = Data(114514)
        val ptr = refCnt.asPtr(data)
        assertEquals(ptr, refCnt.intoPtr(data))

        val restored = refCnt.fromPtr(ptr)
        assertEquals(114514, restored.value)
        assertSame(data, restored)
        assertEquals(ptr, refCnt.asPtr(restored))
        assertEquals(ptr, refCnt.intoPtr(restored))

        refCnt.dec(ptr)
    }

    @Test
    fun refCntNullableReference() {
        val refCnt = NullableRefCnt<Data>()
        val data = Data(114514)
        val ptr = refCnt.asPtr(data)
        assertEquals(ptr, refCnt.intoPtr(data))

        val restored = refCnt.fromPtr(ptr)
        assertEquals(114514, restored?.value)
        assertSame(data, restored)

        val nullPtr = refCnt.asPtr(null)
        assertTrue(nullPtr.isNull)
        assertNull(refCnt.fromPtr(nullPtr))
    }

    @Test
    fun refCntPinArc() {
        val refCnt = PinnedRefCnt<Unmovable>()
        val pinned = PinnedRef.of(Unmovable(114514))
        val ptr = refCnt.asPtr(pinned)
        assertEquals(ptr, refCnt.intoPtr(pinned))

        val restored = refCnt.fromPtr(ptr)
        assertEquals(114514, restored.value.value)
        assertSame(pinned.value, restored.value)
        assertEquals(ptr, refCnt.asPtr(restored))
        assertEquals(ptr, refCnt.intoPtr(restored))

        refCnt.dec(ptr)
    }

    @Test
    fun refCntPinRc() {
        val refCnt = PinnedRefCnt<Unmovable>()
        val pinned = PinnedRef.of(Unmovable(114514))
        val ptr = refCnt.asPtr(pinned)
        assertEquals(ptr, refCnt.intoPtr(pinned))

        val restored = refCnt.fromPtr(ptr)
        assertEquals(114514, restored.value.value)
        assertSame(pinned.value, restored.value)
        assertEquals(ptr, refCnt.asPtr(restored))
        assertEquals(ptr, refCnt.intoPtr(restored))

        refCnt.dec(ptr)
    }
}
