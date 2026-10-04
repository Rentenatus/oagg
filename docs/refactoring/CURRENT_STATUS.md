# Current Status - AGG XML Serialization Refactoring

**Project:** AGG XML Serialization Extraction Refactoring
**Date:** 2026-10-04
**Overall Status:** DOM path complete for the .ggx format (core and UI layer), regression suite green (497 tests). Removing the legacy XML code from `src` is the next phase; its preconditions are open (see roadmap below).

---

## Architecture (current)

| Directory | Module (artifactId) | Role |
|-----------|---------------------|------|
| `src` | `agg-core` | Domain core (GraGra, Graph, Rule, Node, Arc, types, attributes) - still contains the legacy `XwriteObject`/`XreadObject` code |
| `src_ui` | `agg-ui` | Editor/GUI layer (Ed* wrappers, layout, `GraGraSave`/`GraGraLoad`) - save/load entry points still use `XMLHelper` |
| `src_xml` | `agg-xml` | DOM-based XML serialization of the core |
| `src_uixml` | `agg-ui-xml` | DOM-based XML serialization of the UI layer (single `UISerialization` orchestrator; no per-class adapters) |
| `test/test_agg/legacy_agg` | `agg-core-legacy` | Frozen legacy reference (verbatim clone of `src` + `src_ui`); runs the preparation suite and writes reference XML into `assets_test_xml/target/legacy_prep` |

Test roots: `test_xml` (DOM side, in `src_xml`), `test_xml_prep` (preparation, in `agg-core-legacy`), `test_xml_ui` (DOM side of the UI layer, in `src_uixml`), `test_xml_common` (shared helpers). Test working directory: `assets_test_xml`.

---

## Test Coverage

All suites green as of 2026-10-04 (branch `review/014-XML-ex`):

| Suite | Module | Tests | Purpose |
|-------|--------|-------|---------|
| `agg.xml.prep.LegacyPreparationTest` | `agg-core-legacy` | 16 | Writes fresh reference XML from the in-memory model using the frozen legacy save; also covers the attribute and morphism matrices, stability pairs and UI references |
| DOM suite (`test_xml`) | `agg-xml` | 474 | DOM load/save roundtrips, cross-system compatibility, path stability, stress and performance |
| `agg.xml.ui.UiDomRegressionTest` | `agg-ui-xml` | 7 | UI layer serialization scenarios (basic, rule, constraint, no type graph, undirected, composite, rule scheme) |

### Generated scenario matrix (28/28 complete)

7 features crossed with directed/undirected and with/without type graph:

| Feature | dir+TG | undir+TG | dir+noTG | undir+noTG |
|---|---|---|---|---|
| basic_graph_attrs | x | x | x | x |
| rule_nac_pac | x | x | x | x |
| constraints | x | x | x | x |
| match | x | x | x | x |
| rule_scheme | x | x | x | x |
| rule_sequence | x | x | x | x |
| composite_all | x | x | x | x |

All non-RuleScheme scenarios pass a canonical comparison against the fresh legacy reference. RuleScheme scenarios are verified structurally plus DOM stability over two roundtrips (the legacy temp load renames rule graphs, see the regression oracle section in the root README).

### Further matrices

- 39 legacy .ggx fixtures (11 base scenarios x orientation x type graph) plus `.rsx` and `.cpx` fixtures, each exercised through DOM roundtrip, path stability and element preservation.
- Attribute matrix: 6 attribute types with boundary values (0, -42, MAX_INT, empty string, special characters, ...) plus mapping forms (constant, expression, variable, condition, edge).
- Morphism matrix: 56 fixtures over LHS/RHS/mapping structures x orientation x type graph.

### Regression oracle

Four paths are compared against each other: legacy load, legacy save (fresh reference from the in-memory model), DOM load, DOM save. The deliberate oracle decision (DOM preserves the file, not the legacy reader's model side effects) is documented in the root README.

---

## Known Delegations and Gaps

1. **RuleScheme**: `GraGraAdapter` serializes/deserializes rule schemes by delegating to the legacy `XwriteObject`/`XreadObject` through a temporary GraGra load. Documented decision (the legacy reader is deeply intertwined with `RuleScheme.XreadObject`); blocks legacy removal.
2. **`.rsx` (ApplRuleSequence) and `.cpx` (ConflictsDependenciesContainer)**: legacy-only paths; the regression tests exercise the frozen path and do not enforce canonical equality.
3. **GUI save/load entry points**: `src_ui/agg/gui/saveload/GraGraSave`/`GraGraLoad` (plus `GraphBrowserImpl`, `GraGraTreeView`, `ParserDialog`) call `XMLHelper` directly. No file in `src_ui` references `XMLSerialization`; the migration flag (`XMLSerialization.setUseNewXml` -> `GraGraMigration`) exists but is only used by tests.
4. **convert tools**: `AGG2ColorGraph`, `ConverterWSDL`, `WSDL2ggx` use `XMLHelper` directly.
5. **Scale**: 61 files in `src`/`src_ui` reference `XMLHelper`, 51 implement `XwriteObject`/`XreadObject` (xt_basis core, `attribute.impl`, `parser`, `ruleappl`).

---

## Legacy Removal Roadmap

The .ggx roundtrip is complete and fully regression-tested, and the frozen clone keeps the test oracle independent of `src`. Removal sequence:

| Step | Content | Status |
|------|---------|--------|
| 1 | Wire GUI save/load through the migration flag and smoke-test the application | open |
| 2 | Native DOM adapter for RuleScheme (remove the legacy delegation) | open |
| 3 | DOM coverage for `.rsx`/`.cpx` including canonical comparison | open |
| 4 | Slice-wise removal of the legacy XML code from `src` (xt_basis -> attribute -> parser -> ruleappl -> convert) | open |

---

## History

- Phase 1 (inventory, analysis, planning): [PHASE1_FINAL_STATUS.md](PHASE1_FINAL_STATUS.md)
- Phase 2 (module skeleton, first adapters): [PHASE2_COMPLETE.md](PHASE2_COMPLETE.md)
- Adapter and coverage work, UI serialization, scenario matrix completion: see the git history of branch `review/014-XML-ex`

Unrelated pending cleanups in the core (string concatenation, iterator typing, JavaDoc) are tracked in [OPTIMIZATIONS_xt_basis.md](../../OPTIMIZATIONS_xt_basis.md).

---

*Last updated: 2026-10-04*
