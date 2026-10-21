import Testing
import ArcSwap

@Suite("ArcSwap Swift Export Tests")
struct ArcSwapExportTests {
    @Test("Guard holds and returns value")
    func guardHoldsValue() throws {
        let guardObj = Guard.Companion.shared.fromInner(inner: "test-value")
        #expect(guardObj.value != nil)
    }

    @Test("MapGuard holds and returns value")
    func mapGuardHoldsValue() throws {
        let mapGuard = MapGuard(guardedValue: "map-value")
        #expect(mapGuard.value != nil)
    }
}
