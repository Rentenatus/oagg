# Plan: Native DOM Path Completion (Roadmap Steps 2 and 3)

**Date:** 2026-10-04
**Scope:** Roadmap steps 2 and 3 from [CURRENT_STATUS.md](CURRENT_STATUS.md) - remove the RuleScheme legacy delegation and bring `.rsx`/`.cpx` onto the DOM path with canonical verification. These are the preconditions for roadmap step 4 (removing the legacy XML code from `src`).

---

## Part A - Native RuleScheme (remove the legacy delegation)

### Current state

- **Serialize** (`GraGraAdapter.serializeRuleScheme`): temporary `XMLHelper` + `RuleScheme.XwriteObject`, import of the produced subtree, then an ID remap (`remapSchemeElementIds`, `collectSchemeTypes`, `addSchemeIdMapping`, `remapElementIds`, leak net) because the helper's private ID space uses the same `I<n>` format as `DOMSerializationRegistry`.
- **Deserialize** (`GraGraAdapter.deserializeRuleScheme`): writes a temp `.ggx` (Types + RuleScheme), loads it via the legacy `GraGra.load` (the reader side of `RuleScheme.XreadObject`), then adapts types (`adaptSchemeGraphTypes`) and registers the scheme's objects.
- **Format** (pinned by `gen_rule_scheme*.ggx` references): `RuleScheme` attributes (ID, atLeastOneMultiMatch, checkConflict, disjointMultis, index, name, parallelKernel) containing `Kernel > Rule`, `Multi > Rule` plus `EmbeddingLeft`/`EmbeddingRight` (`Morphism` with `Mapping image/orig` node refs), and `TaggedValue` layer/priority.

### Key insight

The model wiring performed by the legacy reader is *public model API*, not XML code: `MultiRule.setRuleScheme`, `applyEmbeddedRuleMapping(kernelRule)`, `mapKernel2MultiObject(mr)`, the kernel LHS/RHS observer registration and `kernel.setChanged(false)` (src/agg/xt_basis/agt/RuleScheme.java, XreadObject). A native DOM adapter can call exactly these methods; no core changes are required.

### Design

1. **New `RuleSchemeAdapter`** (src_xml/agg/xml/adapter/), built on `DOMSerializationRegistry` only:
   - `serializeToElement`: scheme attributes; `Kernel`/`Multi` rule elements produced with the existing `RuleAdapter` (proven canonically by the rule_nac_pac scenarios, includes NAC/PAC/nested AC); `EmbeddingLeft`/`EmbeddingRight` as `Morphism`/`Mapping` elements with kernel and multi node IDs from the registry; `TaggedValue` layer/priority.
   - `deserializeFromElement`: attributes; kernel rule filled via `RuleAdapter` into the existing `rs.getKernelRule()`; one `createEmptyMultiRule()` per `Multi` element, filled via `RuleAdapter`; embedding mappings resolved via the registry; then the wiring calls listed above. Amalgamated rules are not written by the reference scenarios (skip initially, document the decision).
2. **Switch the call sites** in `GraGraAdapter` (serialize loop and `deserializeRuleScheme`) to the adapter.
3. **Delete after the switch** (same commit once the suite is green): the temp-helper serialize path, the temp-file deserialize path, `remapSchemeElementIds`, `collectSchemeTypes`, `addGraphTypes`, `addSchemeIdMapping`, `remapElementIds`, `collectUnmappedIds`, `isIdAttribute`, `applyIdRemap`, `adaptSchemeGraphTypes`, `adaptGraphObjectTypes`, `adaptGraphObjectType`. `GraGraAdapter` must no longer reference `agg.util.XMLHelper`.

### Risks and decisions

- **Graph naming:** the fresh model names rule graphs `Left`/`Right`; the legacy *reader* renames them to `LeftOf_<rule>` (documented reader side effect). The native DOM reader does not rename, so a DOM save reproduces the fresh reference exactly - this is what unlocks canonical comparison for rule_scheme (consistent with the oracle decision in the root README).
- **Pre-existing files:** files written by the legacy reader (with `LeftOf_*` names) load unchanged; the DOM path preserves whatever is in the file.
- **Kernel deserialization into an existing instance:** verify at start that `RuleAdapter.deserializeFromElement` can populate the pre-created kernel rule (`RuleScheme` constructor creates it). Adjust the adapter if it assumes fresh rules.
- **UI pairing:** `UISerialization.attachRuleSchemeSegments` pairs structurally (order-based), not name-based; the `uiRuleSchemeScenarioRoundtrip` test must stay green.

### Tests and acceptance

1. All existing tests stay green (the six rule_scheme scenarios switch to the native path transparently).
2. **Acceptance criterion:** upgrade `runRuleSchemeRoundtrip` to canonical comparison against the fresh reference (remove the structural-only exception in `XmlGeneratedRegressionTest` and in the `rule_scheme` special cases of `LegacyDomRegressionTest.testDomPathStability`).
3. Extend `TestDataGenerator.createWithRuleScheme` with a kernel NAC (new `gen_rule_scheme_nac` scenario, all four orientation/TG variants) to prove the `RuleAdapter` composition inside a scheme. This grows the matrix from 28 to 32.
4. Grep check: no `agg.util.XMLHelper` references left for scheme code.

### Staging

- Commit A1: `RuleSchemeAdapter` + call-site switch + canonical upgrade of the rule_scheme tests + new scenario variants.
- Commit A2: deletion of the delegation and remap machinery; roadmap update in CURRENT_STATUS.md.

---

## Part B - `.rsx` (ApplRuleSequence)

### Current state

- **Format:** `Document > RuleSequenceApplicability > [complete GTS] + RuleSequences > Sequence` (name; `ConcurrentRule` with depth/complete/completecpa/ignoredanglingedge; `Item` entries with kind=applicable/nonapplicable, result, criterion; `Graph` reference). Sequences are matched against the embedded GTS by name (ApplRuleSequence.XreadObject, src/agg/ruleappl/ApplRuleSequence.java:394).
- `ApplRuleSequenceAdapter` in src_xml is an incompatible draft (no embedded GTS, no items, no deserialize). Rewrite it.

### Design

1. `serializeToElement`: `RuleSequenceApplicability` element; embed the full GraGra via the existing `GraGraAdapter.serializeToElement`; then the `RuleSequences` section mirroring `ApplRuleSequence.XwriteObject` (src/agg/ruleappl/ApplRuleSequence.java:312) exactly (sequence name, ConcurrentRule settings, items, graph reference).
2. `deserializeFromElement`: embedded GTS via `GraGraAdapter.deserializeFromElement`, then the sequences per the legacy reader semantics (match by name, create missing ones, ConcurrentRule, items, graph).
3. Entry points: `saveWithDom`/`loadWithDom` overloads for `ApplRuleSequence` in `XMLSerialization`; dispatch the existing `save`/`load` overloads by the feature flag.
4. ID allocation order must follow the legacy write order for canonical equality (embedded GTS first, items after).

### Risks

- `ApplRuleSequence` may hold per-rule applicability results beyond the sequences - read `XwriteObject` fully before implementing and serialize exactly what it emits.
- Accessors for the embedded GraGra need checking (`applRuleSeq.getGraGra()`); add a minimal accessor only if missing.

### Tests and acceptance

1. Prep references exist (`gen_appl_rule_sequence.rsx`, frozen legacy save).
2. Rewrite `testLegacyApplRuleSequenceRoundtrip` into a DOM roundtrip with canonical comparison; keep a legacy-path variant as cross-system check.

---

## Part C - `.cpx` (ConflictsDependenciesContainer)

### Current state

- **Format:** `Document > CriticalPairs > [complete GTS] + cpaOptions + exclude/dependency pair containers` (`writeCriticalPairs`; the writer dispatches over plain, layered and priority container variants). `ParserAdapters` in src_xml are empty shells - implement from scratch.
- Largest of the three formats: pair containers reference rules of the embedded GTS and carry critical pair entries with overlappings/morphisms.

### Design

1. **Format study first:** read `ConflictsDependenciesContainer.XwriteObject/XreadObject` (src/agg/parser/ConflictsDependenciesContainer.java:161/540), `ExcludePairContainer`/`DependencyPairContainer` read/write, and the pinned reference `gen_conflicts_deps.cpx`; record the element map in this document before coding.
2. `ConflictsDependenciesContainerAdapter`: `CriticalPairs` element; embedded GTS via `GraGraAdapter`; `cpaOptions`; container sections per variant (plain first, layered/priority are attribute extensions).
3. Entry points and feature flag dispatch as in Part B.

### Risks

- Pair entries may embed morphism mappings; if they reference GTS objects by ID, the registry must pre-register the embedded GTS objects in write order.
- Canonical comparison may expose genuine legacy instabilities (as with .rsx before) - measure first and document an oracle exception if needed, rather than forcing equality.

### Tests and acceptance

1. Rewrite `testLegacyConflictsDependenciesRoundtrip` into a DOM roundtrip with canonical comparison (same pattern as Part B).
2. Prep reference exists (`gen_conflicts_deps.cpx`).

---

## Sequencing and effort

| Order | Part | Effort | Rationale |
|-------|------|--------|-----------|
| 1 | A - RuleScheme native | Medium | Biggest leverage for step 4; removes the ID remap machinery and unlocks canonical rule_scheme verification |
| 2 | B - .rsx | Small | Reuses GraGraAdapter end to end; small format |
| 3 | C - .cpx | Medium-large | Largest format surface; benefits from the patterns established in A and B |

Each part: adapter, entry point dispatch, regression switch, roadmap update in CURRENT_STATUS.md.

## Open questions (resolve at implementation start)

1. Can `RuleAdapter.deserializeFromElement` populate an existing (pre-created) rule instance?
2. Which `ApplRuleSequence` accessors exist for the embedded GraGra and per-rule results?
3. Do .cpx pair entries reference GTS objects by ID or by name?
4. Is canonical comparison stable for .cpx, or does it need a documented oracle exception?
