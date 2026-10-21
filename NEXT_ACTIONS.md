# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 6/21 (28.6%)
- **Function parity:** 41/119 matched (target 122) — 34.5%
- **Class/type parity:** 27/55 matched (target 45) — 49.1%
- **Combined symbol parity:** 68/174 matched (target 167) — 39.1%
- **Average inline-code cosine:** 0.39 (function body across 6 matched files)
- **Average documentation cosine:** 0.38 (doc text across 6 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 4 files with <0.60 function similarity

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

### 2. as_raw

- **Target:** `arcswap.AsRaw`
- **Similarity:** 0.10
- **Dependents:** 3
- **Priority Score:** 3000309.0
- **Functions:** 1/1 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_

### 3. cache

- **Target:** `arcswap.Cache`
- **Similarity:** 0.71
- **Dependents:** 1
- **Priority Score:** 1011602.9
- **Functions:** 11/11 matched (target 13)
- **Missing functions:** _none_
- **Types:** 4/5 matched (target 6)
- **Missing types:** `Access`
- **Tests:** 4/4 matched

### 4. lib

- **Target:** `arcswap.Guard`
- **Similarity:** 0.06
- **Dependents:** 0
- **Priority Score:** 222909.4
- **Functions:** 3/22 matched (target 19)
- **Missing functions:** `deref`, `from`, `default`, `fmt`, `ptr_eq`, `drop`, `new`, `with_strategy`, `load_full`, `load`, `store`, `swap`, `compare_and_swap`, `map`, `from_pointee`, `empty`, `const_empty`, `load_cnt`, `lease_overflow`
- **Types:** 4/7 matched (target 5)
- **Missing types:** `Target`, `ArcSwapAny`, `ArcSwapWeak`
- **Tests:** 0/2 matched

### 5. access

- **Target:** `arcswap.Access`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 133706.7
- **Functions:** 13/22 matched (target 24)
- **Missing functions:** `check_static_dispatch_direct`, `check_static_dispatch`, `check_dyn_dispatch_direct`, `check_dyn_dispatch`, `check_transition`, `_expect_access`, `_dyn_access`, `_dyn_access_send`, `_dyn_access_send_sync`
- **Types:** 11/15 matched (target 12)
- **Missing types:** `Guard`, `DirectDeref`, `Target`, `AccessConvert`
- **Tests:** 10/19 matched

### 6. serde

- **Target:** `arcswap.Serde`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 10902.1
- **Functions:** 5/6 matched (target 28)
- **Missing functions:** `eq`
- **Types:** 3/3 matched (target 9)
- **Missing types:** _none_
- **Tests:** 3/4 matched

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

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

