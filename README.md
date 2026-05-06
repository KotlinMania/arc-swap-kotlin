# arc-swap-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Farc--swap--kotlin-blue.svg)](https://github.com/KotlinMania/arc-swap-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/arc-swap-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/arc-swap-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/arc-swap-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/arc-swap-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`vorner/arc-swap`](https://github.com/vorner/arc-swap).

**Original Project:** This port is based on [`vorner/arc-swap`](https://github.com/vorner/arc-swap). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `vorner/arc-swap`

> The text below is reproduced and lightly edited from [`https://github.com/vorner/arc-swap`](https://github.com/vorner/arc-swap). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## ArcSwap

[![Actions Status](https://github.com/vorner/arc-swap/workflows/test/badge.svg)](https://github.com/vorner/arc-swap/actions)
[![Codecov](https://codecov.io/gh/vorner/arc-swap/branch/master/graph/badge.svg?token=3KA3R2D9fV)](https://codecov.io/gh/vorner/arc-swap)
[![Docs](https://docs.rs/arc-swap/badge.svg)](https://docs.rs/arc-swap)

This provides something similar to what `RwLock<Arc<T>>` is or what
`Atomic<Arc<T>>` would be if it existed, optimized for read-mostly write-seldom
scenarios, with consistent performance characteristics.

Read [the documentation](https://docs.rs/arc-swap) before using.

## Rust version policy

The 1. version will build on any edition 2018 capable compiler. This does not
include:

* Tests. Tests build and run on recent compilers, mostly because of
  dependencies.
* Additional feature flags. Most feature flags are guaranteed to build since the
  version they are introduced. Experimental features are without any guarantees.

## License

Licensed under either of

 * Apache License, Version 2.0, ([LICENSE-APACHE](https://github.com/vorner/arc-swap/blob/HEAD/LICENSE-APACHE) or
   https://www.apache.org/licenses/LICENSE-2.0)
 * MIT license ([LICENSE-MIT](https://github.com/vorner/arc-swap/blob/HEAD/LICENSE-MIT) or
   https://opensource.org/license/mit)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be
dual licensed as above, without any additional terms or conditions.

[`Arc`]: https://doc.rust-lang.org/std/sync/struct.Arc.html
[`AtomicPtr`]: https://doc.rust-lang.org/std/sync/atomic/struct.AtomicPtr.html
[`ArcSwap`]: https://docs.rs/arc-swap/*/arc_swap/type.ArcSwap.html

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:arc-swap-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`vorner/arc-swap`](https://github.com/vorner/arc-swap). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the arc-swap authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`vorner/arc-swap`](https://github.com/vorner/arc-swap) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
