// port-lint: source src/lib.rs
package io.github.kotlinmania.arcswap

/**
 * An atomic storage for a value of type [T].
 *
 * This is a type alias only. Most of its methods are described on [ArcSwapAny].
 *
 * The upstream Rust type is `ArcSwapAny<Arc<T>>`. In Kotlin the reference-counting role
 * of `Arc` is fulfilled by the garbage collector, so the wrapper collapses to the value
 * itself.
 */
typealias ArcSwap<T> = ArcSwapAny<T>
