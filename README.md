# oagg
Open Attributed Graph Grammar System

## Support 🐾
If you like my projects, consider [supporting my work](https://github.com/sponsors/Rentenatus) (and feeding Mistral 🐱)! 

---

## History

AGG is a development environment for attributed graph transformation systems supporting an algebraic approach to graph transformation.
It aims at specifying and rapid prototyping applications with complex, graph structured data. AGG may be (re)used (without GUI)
as a general purpose graph transformation engine in high level JAVA applications employing graph transformation methods.

The work on AGG started in the beginning of 1997. The system is developed and managed by Olga Runge (https://www.user.tu-berlin.de/o.runge/AGG/WWW/agg.html) at Technische Universität Berlin (https://www.tu.berlin/).

The Mirror Open AGG is managed by Janusch Rentenatus, providing code review since 2021 and Java upgrades.

Discord: https://discord.gg/JZtHQbxC

---

**Dependencies:**

* xerces-2_12_1
* ndimcol4j (https://github.com/Rentenatus/ndimcol4j)

---

## Module Architecture

The repository is layered so that the core can be used without the UI and
without the DOM XML module:

| Directory | Module (artifactId) | Contains | Depends on |
|-----------|---------------------|----------|------------|
| `src` | `agg-core` | Domain core (GraGra, Graph, Rule, Node, Arc, types, attributes) — no UI, no DOM XML | — |
| `src_ui` | `agg-ui` | Editor/GUI layer of the core (EdGraGra, EdNode, layout, GraGraSave/GraGraLoad) | `agg-core` |
| `src_xml` | `agg-xml` | DOM-based XML serialization of the core (adapters for GraGra, Rule, Graph, Types, Match, ...) | `agg-core` |
| `src_uixml` | `agg-ui-xml` | DOM-based XML serialization of the UI layer (adapters for Ed* wrappers, NodeLayout, layout objects) | `agg-xml`, `agg-ui` |

Layering rules:

* `src` stays free of UI code and DOM XML code.
* Core serialization goes into `src_xml` (packages `agg.xml.*`); UI/editor
  serialization goes into `src_uixml` (packages `agg.xml.ui.*`) — mirroring
  how `src_ui` extends `src`.
* The frozen legacy reference of the old XML path lives in
  `test/test_agg/legacy_agg` (verbatim clone of `src` + `src_ui`, module
  `agg-core-legacy`). The preparation suite in `test_xml_prep` runs against
  this clone and writes reference XML into `assets_test_xml/target/legacy_prep`;
  the DOM-side regression tests in `test_xml` run against the current code
  and compare against those frozen references.
* Test roots: `test_xml` (DOM side, compiled into `src_xml`),
  `test_xml_prep` (preparation, compiled into `agg-core-legacy`),
  `test_xml_ui` (DOM side of the UI layer, compiled into `src_uixml`),
  `test_xml_common` (shared test helpers, compiled into all test modules).
  Shared test data lives in `assets_test_xml` (test working directory).

**Regression oracle (deliberate decision):** DOM load → DOM save is compared
canonically against the FRESH legacy save (the reference file the frozen clone
writes from the in-memory model). The DOM path must preserve what is in the
file; it does NOT replicate side effects of the old READER, which mutates the
model during load:

| Legacy reader side effect (load → legacy save) | DOM behaviour (kept) |
|---|---|
| Rule graphs renamed to `LeftOf_<rule>` / `RightOf_<rule>` (`Rule.XreadObject`) | names preserved from the file |
| `formula` attribute regenerated from the conditions (`true` → `1`, `1&2`, ...) | value preserved from the file |
| Atomic constraint graphs renamed to `Premise/Conclusion of <name>` | names preserved from the file |
| `LayoutNode.frozen` re-derived from `age` (ignores the `frozen` attribute) | value preserved from the file |

Exception: the RuleScheme section is loaded through the legacy reader by the
core path (temp GraGra load in `GraGraAdapter`), so its roundtrip shows those
renames. Like the core suite, the UI tests verify RuleScheme scenarios
structurally plus DOM stability over two roundtrips instead of a canonical
comparison against the fresh reference.

**Test coverage:** 497 tests green (preparation 16, core DOM 474, UI 7). The
generated scenario matrix is complete: 7 features (basic graph with attributes,
rule with NAC/PAC/nested AC, constraints, match, rule scheme, rule sequence,
composite of all) crossed with directed/undirected and with/without type
graph (28 combinations). 39 legacy .ggx fixtures plus attribute and morphism
matrices exercise the remaining dimensions. Current status, known delegations
and the legacy removal roadmap: [docs/refactoring/CURRENT_STATUS.md](docs/refactoring/CURRENT_STATUS.md).

---

**Performance:**
The current version completes reference tests in ~4% of the target time (36.21s vs. 950s) thanks to refactoring with Mistral support and integration of the ndimcol repository.

---

**License Notice:**

This description is subject to the **Eclipse Public License 2.0**. For more information about the license, please visit [here](https://www.eclipse.org/legal/epl-2.0/).
