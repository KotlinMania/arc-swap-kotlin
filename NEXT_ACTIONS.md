# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/21 (9.5%)
- **Function parity:** 16/139 matched (target 52) — 11.5%
- **Class/type parity:** 8/55 matched (target 15) — 14.5%
- **Combined symbol parity:** 24/194 matched (target 67) — 12.4%
- **Average inline-code cosine:** 0.28 (function body across 2 matched files)
- **Average documentation cosine:** 0.43 (doc text across 2 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 2 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. ref_cnt
- **Similarity:** 0.37 (needs 48% improvement)
- **Dependencies:** 10
- **Priority Score:** 10011206.0
- **Functions:** 8/8 matched (target 25)
- **Missing functions:** _none_
- **Types:** 3/4 matched (target 9)
- **Missing types:** `Base`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. ref_cnt

- **Target:** `arcswap.RefCnt`
- **Similarity:** 0.37
- **Dependents:** 10
- **Priority Score:** 10011206.0
- **Functions:** 8/8 matched (target 25)
- **Missing functions:** _none_
- **Types:** 3/4 matched (target 9)
- **Missing types:** `Base`
- **Tests:** 3/3 matched

### 2. lib

- **Target:** `arcswap.Guard`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 162908.1
- **Functions:** 8/22 matched (target 27)
- **Missing functions:** `deref`, `from`, `default`, `fmt`, `ptr_eq`, `drop`, `new`, `with_strategy`, `map`, `from_pointee`, `empty`, `const_empty`, `load_cnt`, `lease_overflow`
- **Types:** 5/7 matched (target 6)
- **Missing types:** `Target`, `ArcSwapWeak`
- **Tests:** 0/2 matched

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../tmp/arc-swap/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/arcswap kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `debt.mod` | `debt.Mod` | 0 | `debt/mod.rs` | `debt/Mod.kt` |
| `docs.mod` | `docs.Mod` | 0 | `docs/mod.rs` | `docs/Mod.kt` |
| `strategy.mod` | `strategy.Mod` | 0 | `strategy/mod.rs` | `strategy/Mod.kt` |

