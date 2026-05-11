// port-lint: source src/lib.rs
package io.github.kotlinmania.arcswap

/**
 * An atomic storage that doesn't share the internal generation locks with others.
 *
 * The upstream Rust crate distinguished `ArcSwap` from `IndependentArcSwap` because
 * the former shared a global hazard-pointer scheme between instances while the latter
 * gave each instance its own state. Kotlin's garbage collector replaces the
 * hazard-pointer machinery entirely, so both variants collapse to the same type.
 *
 * Being phased out upstream — kept as an alias here for source-level compatibility.
 */
typealias IndependentArcSwap<T> = ArcSwapAny<T>
