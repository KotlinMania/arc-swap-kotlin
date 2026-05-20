// port-lint: source lib.rs
package io.github.kotlinmania.arcswap

/**
 * An atomic storage for a nullable value of type [T].
 *
 * This is very similar to [ArcSwap], but allows storing `null` values, which is useful
 * in some situations.
 *
 * This is a type alias only. Most of the methods are described on [ArcSwapAny]. Even
 * though the examples there often use [ArcSwap], they are applicable to [ArcSwapOption]
 * with appropriate changes.
 *
 * # Examples
 *
 * ```kotlin
 * val shared: ArcSwapOption<Int> = ArcSwapOption(null)
 * check(shared.loadFull() == null)
 * check(shared.swap(42) == null)
 * check(shared.loadFull() == 42)
 * ```
 */
typealias ArcSwapOption<T> = ArcSwapAny<T?>

/**
 * A convenience constructor for an empty value.
 *
 * This is equivalent to `ArcSwapOption<T>(null)`.
 */
fun <T> arcSwapOptionEmpty(): ArcSwapOption<T> = ArcSwapAny(null)
