# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/21 (23.8%)
- **Function parity:** 42/119 matched (target 103) — 35.3%
- **Class/type parity:** 25/55 matched (target 38) — 45.5%
- **Combined symbol parity:** 67/174 matched (target 141) — 38.5%
- **Average inline-code cosine:** 0.34 (function body across 5 matched files)
- **Average documentation cosine:** 0.46 (doc text across 5 matched files)
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
- **Similarity:** 0.20
- **Dependents:** 0
- **Priority Score:** 152908.0
- **Functions:** 9/22 matched (target 28)
- **Missing functions:** `deref`, `from`, `default`, `fmt`, `ptr_eq`, `drop`, `new`, `with_strategy`, `from_pointee`, `empty`, `const_empty`, `load_cnt`, `lease_overflow`
- **Types:** 5/7 matched (target 6)
- **Missing types:** `Target`, `ArcSwapWeak`
- **Tests:** 0/2 matched

### 5. access

- **Target:** `arcswap.Access`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 133706.7
- **Functions:** 13/22 matched (target 24)
- **Missing functions:** `check_static_dispatch_direct`, `check_static_dispatch`, `check_dyn_dispatch_direct`, `check_dyn_dispatch`, `check_transition`, `_expect_access`, `_dyn_access`, `_dyn_access_send`, `_dyn_access_send_sync`
- **Types:** 11/15 matched (target 13)
- **Missing types:** `Guard`, `DirectDeref`, `Target`, `AccessConvert`
- **Tests:** 10/19 matched

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

