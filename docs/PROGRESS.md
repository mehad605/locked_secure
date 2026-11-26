# LockedSecure — Optimization & Refactoring Progress Log

| Iter | Sector | Criterion | Change | Metric before -> after | Scores before -> after | Keep/Revert | Commit |
|---|---|---|---|---|---|---|---|
| 0 | Phase 0-1 | Baseline & safety net | Tag `baseline-v1` on `main`, branch `overhaul/v2`, capture `ANALYSIS.md`, `BASELINE.md`, `SCORECARD.md`, `PARITY.md`, and add 15 characterization tests + release smoke script locking C1-C8 | Tests: `0` -> `15` + smoke PASS; APK arm64: `4,148,087 B`; Cold start: `233.0 ms`; Idle PSS: `32,822 KB` | S8: `0` -> `10` (baseline gate ready) | KEEP | *(phase 0-1 commit)* |
