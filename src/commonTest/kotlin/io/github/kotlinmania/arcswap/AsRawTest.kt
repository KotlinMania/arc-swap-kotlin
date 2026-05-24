// port-lint: source as_raw.rs
package io.github.kotlinmania.arcswap

import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AsRawTest {
    @Test
    fun valueAdapterUsesIdentityPointer() {
        val value = Any()
        val first = asRaw(value)
        val second = asRaw(value)

        assertTrue(first.asRaw() pointsToSameObjectAs second.asRaw())
    }

    @Test
    fun guardAdapterUsesGuardedValue() {
        val value = Any()
        val guard = Guard.fromInner(value)

        assertTrue(asRawGuard(guard).asRaw() pointsToSameObjectAs asRaw(value).asRaw())
    }

    @Test
    fun nullableAdapterCanRepresentEmptyPointer() {
        val empty = asRawNullable<Any>(null)

        assertTrue(empty.asRaw().isNull)
    }

    @Test
    fun nullableGuardAdapterUsesGuardedValue() {
        val value = Any()
        val guard = Guard.fromInner<Any?>(value)

        assertTrue(asRawNullableGuard(guard).asRaw() pointsToSameObjectAs asRaw(value).asRaw())
    }

    @Test
    fun existingPointerAdapterPreservesPointerObject() {
        val pointer = RefPtr.of(Any())

        assertSame(pointer, asRawPtr(pointer).asRaw())
    }

    @Test
    fun refCntAdapterUsesReferenceCountingView() {
        val value = Any()
        val refCnt = StrongRefCnt<Any>()

        assertTrue(
            asRawRefCnt(value, refCnt).asRaw() pointsToSameObjectAs refCnt.asPtr(value),
        )
    }
}
