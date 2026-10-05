# Current Status - AGG XML Serialization Refactoring

**Project:** AGG XML Serialization Extraction Refactoring
**Date:** 2026-10-05
**Overall Status:** DOM path complete for the .ggx format and for the full `.cpx`/`.rsx` surface (core and UI layer), regression suite green (507 tests). Removing the legacy XML code from `src` is the next phase; its preconditions are open (see roadmap below).

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

All suites green as of 2026-10-05 (branch `review/014-XML-ex`):

| Suite | Module | Tests | Purpose |
|-------|--------|-------|---------|
| `agg.xml.prep.LegacyPreparationTest` | `agg-core-legacy` | 17 | Writes fresh reference XML from the in-memory model using the frozen legacy save; also covers the attribute and morphism matrices, stability pairs, UI references and the computed-pair `.cpx` fixtures |
| DOM suite (`test_xml`) | `agg-xml` | 483 | DOM load/save roundtrips, cross-system compatibility, path stability, stress and performance |
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

All scenarios pass a canonical comparison against the fresh legacy reference, including the RuleScheme ones (native serialization since the delegation removal). Computed-pair `.cpx` scenarios: plain and NAC canonically against the raw reference, PAC for stability and structure (the PAC overlap reconstruction is lossy in the legacy reader as well, see the gaps below).

### Further matrices

- 39 legacy .ggx fixtures (11 base scenarios x orientation x type graph) plus `.rsx` and `.cpx` fixtures, each exercised through DOM roundtrip, path stability and element preservation.
- Computed critical pairs (`.cpx`): 3 generated fixtures (plain overlaps with conflicts, dependencies and `not_computable` entries; NAC overlap; PAC overlap). Plain and NAC are compared canonically against the raw legacy reference after DOM load and after in-memory DOM save; PAC is verified for stability and structure only because the overlap reconstruction is lossy (see gaps below).
- Attribute matrix: 6 attribute types with boundary values (0, -42, MAX_INT, empty string, special characters, ...) plus mapping forms (constant, expression, variable, condition, edge).
- Morphism matrix: 56 fixtures over LHS/RHS/mapping structures x orientation x type graph.

### Regression oracle

Four paths are compared against each other: legacy load, legacy save (fresh reference from the in-memory model), DOM load, DOM save. The deliberate oracle decision (DOM preserves the file, not the legacy reader's model side effects) is documented in the root README.

---

## Known Delegations and Gaps

1. **`.cpx` computed critical pair entries**: covered natively by the DOM path (`ConflictsDependenciesContainerAdapter` mirrors `writeCriticalPairs`/`XreadObject`, including the NAC and PAC overlap reconstruction). Remaining limits, all deliberate and explicit:
   - Old-style overlap morphisms without a `source` attribute (`readOldOverlappingMorphisms`) are rejected with an `XMLSerializationException`.
   - The free container sections are bound to their pair containers by tag name (`conflictFreeContainer` -> conflict container, `dependencyFreeContainer` -> dependency container); the legacy reader instead binds whichever section follows the first container read.
   - `ConflictDependencyGraph` is only read when the section is present; the legacy reader always creates an empty CPA basis graph.
   - The layered/priority container variants (`lepc`/`pepc`, `ldpc`/`pdpc`) have no fixtures yet; the DOM read path mirrors the legacy container dispatch, but the loaded containers are installed as the plain fields.
   - The PAC overlap is lossy in the reader: the `pacname` mapping of a pure PAC arc is not reconstructed (the `orig2copy` map only knows LHS nodes). The legacy reader has the same loss, so no canonical comparison is possible against a raw reference; the DOM output is stable across roundtrips.
   - The computed-pair fixtures are built synthetically (TestDataGenerator mirrors the reader's reconstruction) because the CPA engine itself currently finds no inclusions: upstream, `ExcludePairHelper.putGraphInclusionSet` has the `inclusions.add(goSet)` call commented out, so delete-use/produce-forbid never matches. Fixing the engine is out of scope for the serialization refactoring; the frozen legacy clone shows the same behaviour.
2. **GUI save/load entry points**: `src_ui/agg/gui/saveload/GraGraSave`/`GraGraLoad` (plus `GraphBrowserImpl`, `GraGraTreeView`, `ParserDialog`) call `XMLHelper` directly. No file in `src_ui` references `XMLSerialization`; the migration flag (`XMLSerialization.setUseNewXml` -> `GraGraMigration`) exists but is only used by tests.
3. **convert tools**: `AGG2ColorGraph`, `ConverterWSDL`, `WSDL2ggx` use `XMLHelper` directly.
4. **Scale**: 61 files in `src`/`src_ui` reference `XMLHelper`, 51 implement `XwriteObject`/`XreadObject` (xt_basis core, `attribute.impl`, `parser`, `ruleappl`).

---

## Legacy Removal Roadmap

The .ggx roundtrip is complete and fully regression-tested, and the frozen clone keeps the test oracle independent of `src`. Removal sequence:

| Step | Content | Status |
|------|---------|--------|
| 1 | Wire GUI save/load through the migration flag and smoke-test the application | open |
| 2 | Native DOM adapter for RuleScheme (remove the legacy delegation) | done (`RuleSchemeAdapter`, canonical verification) |
| 3 | DOM coverage for `.rsx`/`.cpx` including canonical comparison | done (`.rsx` complete; `.cpx` structural and computed pair entries, including NAC/PAC overlaps) |
| 4 | Slice-wise removal of the legacy XML code from `src` (xt_basis -> attribute -> parser -> ruleappl -> convert) | open |

---

## History

- Phase 1 (inventory, analysis, planning): [PHASE1_FINAL_STATUS.md](PHASE1_FINAL_STATUS.md)
- Phase 2 (module skeleton, first adapters): [PHASE2_COMPLETE.md](PHASE2_COMPLETE.md)
- Adapter and coverage work, UI serialization, scenario matrix completion: see the git history of branch `review/014-XML-ex`

Unrelated pending cleanups in the core (string concatenation, iterator typing, JavaDoc) are tracked in [OPTIMIZATIONS_xt_basis.md](../../OPTIMIZATIONS_xt_basis.md).

---

*Last updated: 2026-10-05*
