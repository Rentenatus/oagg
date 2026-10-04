# Dependency Analysis


## Overview

This document provides a comprehensive analysis of dependencies in the AGG project, specifically focusing on XML serialization dependencies that need to be addressed during the refactoring.

---

## Circular Dependencies

### Main Cycle

```
XMLHelper → XMLObject → Domain Classes → XMLHelper
```

**Description:**
- `XMLHelper` knows about the `XMLObject` interface and calls its methods (`XwriteObject`, `XreadObject`)
- All domain classes implement `XMLObject` and receive `XMLHelper` as a parameter
- Domain classes call methods on `XMLHelper` within their `XwriteObject`/`XreadObject` implementations
- This creates a tight coupling that makes separation difficult

### Breaking the Cycle

**Recommended Approach:** Use the **Adapter Pattern** with dependency inversion

1. **Keep XMLObject in agg-core**: The interface remains in the core module as it's implemented by domain classes
2. **Move XMLHelper to agg-xml**: The implementation moves to the new XML module
3. **Create Adapter Layer**: Adapters in agg-xml bridge between XMLObject (old) and XMLSerializable (new)
4. **Dependency Flow**:
   ```
   agg-core (XMLObject) ← agg-xml (Adapters + XMLHelper)
   ```
   - agg-core depends only on the XMLObject interface (which it already contains)
   - agg-xml depends on agg-core for the XMLObject interface
   - This breaks the circular dependency

---

## Target Module Dependency Flow (2026-10 update)

The module layering mirrors the core/UI separation of the source tree:

```
                    ┌─────────────┐
                    │  agg-core   │  (src)
                    │  domain     │
                    └──────┬──────┘
               ┌────────────┼────────────┐
               ▼            ▼            │
      ┌────────────┐  ┌────────────┐    │
      │  agg-ui    │  │  agg-xml   │    │
      │  (src_ui)  │  │ (src_xml)  │    │
      │ editor     │  │ DOM XML of │    │
      │ layer      │  │ the core   │    │
      └─────┬──────┘  └─────┬──────┘    │
            │               │           │
            └───────┬───────┘           │
                    ▼                   │
           ┌─────────────────┐          │
           │  agg-ui-xml     │          │
           │  (src_uixml)    │          │
           │  DOM XML of the │          │
           │  UI layer       │          │
           └─────────────────┘          │
```

* `agg-core` (src): no UI, no DOM XML.
* `agg-ui` (src_ui): editor layer of the core (EdGraGra, EdNode, layout,
  GraGraSave/GraGraLoad).
* `agg-xml` (src_xml): DOM-based serialization of the core, packages
  `agg.xml.*`.
* `agg-ui-xml` (src_uixml): DOM-based serialization of the UI layer
  (Ed* adapters, NodeLayout, layout objects), packages `agg.xml.ui.*`;
  depends on `agg-xml` and `agg-ui`.

This keeps the core usable without the UI and keeps the DOM XML module
free of UI concerns. A .ggx file written by the GUI contains both
layers: the core layer (agg-xml) and the UI layer (agg-ui-xml).

The frozen legacy reference (test/test_agg/legacy_agg, module
agg-core-legacy) is a verbatim clone of src + src_ui at the freeze point
and carries both layers of the old XML path.

---

## Dependency Matrix

### Source to Target Dependencies

| Source \ Target | XMLHelper | XMLObject | Graph | Rule | Node | Arc | EdgeType | NodeType |
|-----------------|-----------|----------|-------|-------|------|------|----------|----------|
| **Graph** | ✅ Yes | Implements | - | - | - | - | - | - |
| **Rule** | ✅ Yes | Implements | - | - | - | - | - | - |
| **Node** | ✅ Yes | Implements | - | - | - | - | - | - |
| **Arc** | ✅ Yes | Implements | - | - | - | - | - | - |
| **GraGra** | ✅ Yes | Implements | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | - | - |
| **TypeGraph** | ✅ Yes | Implements | - | - | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **Match** | ✅ Yes | Implements | - | - | ✅ Yes | ✅ Yes | - | - |
| **TypeImpl** | ✅ Yes | Implements | - | - | - | - | - | - |
| **NodeTypeImpl** | ✅ Yes | Implements | - | - | - | - | - | - |
| **ArcTypeImpl** | ✅ Yes | Implements | - | - | - | - | - | - |
| **EdGraGra** | ✅ Yes | Implements | ✅ Yes | - | - | - | - | - |
| **EdGraph** | ✅ Yes | Implements | ✅ Yes | - | ✅ Yes | ✅ Yes | - | - |
| **EdNode** | ✅ Yes | Implements | - | - | ✅ Yes | ✅ Yes | - | - |
| **EdArc** | ✅ Yes | Implements | - | - | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **XMLHelper** | - | ✅ Uses | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |

---

## Dependency Types

### Direct XMLHelper Usage

Classes that directly instantiate or import XMLHelper:

#### Core Domain Classes (agg.xt_basis) - 14 classes
- Graph
- Rule
- Node
- Arc
- GraGra
- TypeGraph
- Match
- TypeImpl
- NodeTypeImpl
- ArcTypeImpl
- GraphElement
- GraphObject
- OrdinaryMorphism
- AGGBasicAppl

#### Attribute Implementation Classes (agg.attribute.impl) - 6 classes
- CondMember
- CondTuple
- DeclMember
- DeclTuple
- ValueMember
- ValueTuple
- VarTuple

#### Constraint Classes (agg.cons) - 2 classes
- AtomConstraint
- Formula

#### Parser Classes (agg.parser) - 9 classes
- ConflictsDependenciesContainer
- DependencyPairContainer
- ExcludePairContainer
- LayeredDependencyPairContainer
- LayeredExcludePairContainer
- PriorityDependencyPairContainer
- PriorityExcludePairContainer
- LayerFunction
- ComputeCriticalPairs

#### Rule Application Classes (agg.ruleappl) - 2 classes
- ApplRuleSequence
- RuleSequence

#### Convert Classes (agg.convert) - 3 classes
- AGG2ColorGraph
- ConverterWSDL
- WSDL2ggx

#### Editor Classes (agg.editor.impl) - 10 classes
- EdArc
- EdAtomic
- EdConstraint
- EdGraGra
- EdGraph
- EdNestedApplCond
- EdNode
- EdRule
- EdRuleScheme
- EdType

#### GUI Classes (agg.gui) - 5 classes
- ConflictsDependenciesContainerSaveLoad
- GraGraLoad
- GraGraSave
- GraGraTreeView
- PairIOGUI
- ApplRuleSequenceSaveLoad

#### Layout Classes (agg.layout.evolutionary) - 3 classes
- LayoutArc
- LayoutNode
- LayoutPattern

**Total: 59 classes directly use XMLHelper**

### XMLObject Implementations

Classes that implement the XMLObject interface:

- All 59 classes that use XMLHelper also implement XMLObject (except for some GUI classes)
- Total: 96 classes implement XMLObject

---

## Package-Level Dependencies

### Dependency Graph by Package

```
┌─────────────────────────────────────────────────────────────────┐
│                      PACKAGE DEPENDENCY GRAPH                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  agg.util                                                        │
│  ├── XMLHelper          ──► org.apache.xerces.parsers.DOMParser    │
│  ├── XMLHelper          ──► org.apache.xerces.dom.DocumentImpl      │
│  ├── XMLHelper          ──► org.apache.xml.serialize.XMLSerializer  │
│  ├── XMLHelper          ──► org.w3c.dom.* (JDK classes)            │
│  └── XMLObject (interface)                                       │
│                                                                  │
│  agg.xt_basis                                                    │
│  ├── Graph             ──► agg.util.XMLHelper                      │
│  ├── Rule              ──► agg.util.XMLHelper                      │
│  ├── Node              ──► agg.util.XMLHelper                      │
│  ├── Arc               ──► agg.util.XMLHelper                      │
│  ├── GraGra            ──► agg.util.XMLHelper                      │
│  ├── TypeGraph         ──► agg.util.XMLHelper                      │
│  ├── Match             ──► agg.util.XMLHelper                      │
│  ├── TypeImpl          ──► agg.util.XMLHelper                      │
│  ├── NodeTypeImpl      ──► agg.util.XMLHelper                      │
│  ├── ArcTypeImpl       ──► agg.util.XMLHelper                      │
│  └── implements XMLObject                                         │
│                                                                  │
│  agg.attribute.impl                                              │
│  ├── CondMember        ──► agg.util.XMLHelper                      │
│  ├── CondTuple         ──► agg.util.XMLHelper                      │
│  ├── DeclMember        ──► agg.util.XMLHelper                      │
│  ├── DeclTuple         ──► agg.util.XMLHelper                      │
│  ├── ValueMember       ──► agg.util.XMLHelper                      │
│  ├── ValueTuple        ──► agg.util.XMLHelper                      │
│  └── VarTuple          ──► agg.util.XMLHelper                      │
│                                                                  │
│  agg.editor.impl                                                 │
│  ├── EdArc             ──► agg.util.XMLHelper                      │
│  ├── EdAtomic          ──► agg.util.XMLHelper                      │
│  ├── EdConstraint      ──► agg.util.XMLHelper                      │
│  ├── EdGraGra          ──► agg.util.XMLHelper                      │
│  ├── EdGraph           ──► agg.util.XMLHelper                      │
│  ├── EdNestedApplCond  ──► agg.util.XMLHelper                      │
│  ├── EdNode            ──► agg.util.XMLHelper                      │
│  ├── EdRule            ──► agg.util.XMLHelper                      │
│  └── EdType            ──► agg.util.XMLHelper                      │
│                                                                  │
│  agg.gui.*                       (depends on editor and core)      │
│  ├── GraGraSave       ──► agg.util.XMLHelper                      │
│  ├── GraGraLoad       ──► agg.util.XMLHelper                      │
│  └── ... (other GUI save/load classes)                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## External Dependencies

### XML Libraries

XMLHelper depends on the following external libraries:

| Library | Purpose | Version |
|---------|---------|---------|
| Apache Xerces | DOM Parser | 2.12.2 |
| Apache XML Commons | XML Serializer | - |
| JDK XML | org.w3c.dom.* | Built-in |
| JDK SAX | org.xml.sax.* | Built-in |

### Import Statements Found (22 unique XML-related imports)

```java
// Apache Xerces
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;

// Apache XML Commons
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

// JDK XML (W3C DOM)
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

// JDK XML (SAX)
import org.xml.sax.*;

// JavaX XML
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
```

---

## Recommended Refactoring Order

### Priority 1: Low Risk (No Domain Changes)
These steps can be done without modifying domain classes:

1. ✅ **Create agg-xml module** - Already planned
2. ✅ **Implement core interfaces** - XMLSerializable, XMLSerializer, XMLDeserializer
3. ✅ **Create adapter layer** - XMLObjectAdapter as bridge
4. ✅ **Move XMLHelper to agg-xml** - With backward compatibility wrapper
5. ✅ **Update imports** - Change agg.util.XMLHelper to agg.xml.legacy.XMLHelperWrapper

### Priority 2: Medium Risk (Save/Load Classes)
These classes are application-layer and can be migrated with feature flags:

1. **GraGraSave** - Uses XMLHelper.save_to_xml
2. **GraGraLoad** - Uses XMLHelper.read_from_xml
3. **EdGraGra.saveToXML** - Uses XMLHelper
4. **ApplRuleSequenceSaveLoad** - Uses XMLHelper
5. **ConflictsDependenciesContainerSaveLoad** - Uses XMLHelper
6. **PairIOGUI** - Uses XMLHelper for save/load
7. **AGG2ColorGraph** - Uses XMLHelper.read_from_xml
8. **ConverterWSDL** - Uses XMLHelper.read_from_xml
9. **WSDL2ggx** - Uses XMLHelper and implements XMLObject

### Priority 3: High Risk (Domain Classes)
These are the core domain classes that implement XMLObject. Migration requires:
- Creating adapters for each class
- Updating save/load logic to use new serializers
- Ensuring backward compatibility

**Core Domain (agg.xt_basis):**
1. Graph
2. Rule
3. Node
4. Arc
5. GraGra
6. TypeGraph
7. Match
8. TypeImpl
9. NodeTypeImpl
10. ArcTypeImpl

**Attribute Classes:**
1. AttrManager
2. AttrTupleManager
3. CondMember, CondTuple, DeclMember, DeclTuple
4. ValueMember, ValueTuple, VarTuple
5. AtomConstraint, Formula

**Editor Classes:**
1. EdArc, EdAtomic, EdConstraint
2. EdGraGra, EdGraph
3. EdNestedApplCond, EdNode
4. EdRule, EdRuleScheme, EdType

**Parser Classes:**
1. ConflictsDependenciesContainer
2. DependencyPairContainer, ExcludePairContainer
3. LayeredDependencyPairContainer, LayeredExcludePairContainer
4. PriorityDependencyPairContainer, PriorityExcludePairContainer
5. LayerFunction, ComputeCriticalPairs

**Rule Application:**
1. ApplRuleSequence
2. RuleSequence

**Layout Classes:**
1. LayoutArc
2. LayoutNode
3. LayoutPattern

---

## Migration Strategy for Breaking Dependencies

### Step 1: Extract XMLHelper
- Create `agg-xml` module
- Move XMLHelper to `agg.xml.legacy` package
- Create backward-compatible wrapper in agg-core that delegates to new location
- Update all imports to use the wrapper

### Step 2: Create Adapter Layer
- Create `XMLObjectAdapter` that implements `XMLSerializable`
- XMLObjectAdapter wraps XMLObject instances
- Delegates to XwriteObject/XreadObject methods
- Converts between old XMLHelper and new serialization context

### Step 3: Create New Interfaces
- `XMLSerializable` - New interface for serialization
- `XMLSerializer` - Serializer interface
- `XMLDeserializer` - Deserializer interface
- `XMLSerializerContext` - Context for serialization
- `XMLDeserializerContext` - Context for deserialization

### Step 4: Migrate Save/Load Classes
- Start with GUI save/load classes (GraGraSave, GraGraLoad)
- Replace direct XMLHelper usage with new serializers
- Use feature flags to switch between old and new implementation
- Test thoroughly with existing .ggx files

### Step 5: Migrate Domain Classes
- Create specific adapters for each domain class
- Gradually replace XMLHelper usage
- Maintain backward compatibility
- Test roundtrip (save → load → compare)

---

## Dependency Statistics

| Category | Count | Notes |
|----------|-------|-------|
| Total AGG imports | 432 | All import statements from agg.* packages |
| XML-related imports | 22 | Imports from xml, w3c, xerces, sax packages |
| Classes using XMLHelper | 61 | Classes that import or use XMLHelper |
| XMLObject implementations | 96 | Classes implementing XMLObject interface |
| Direct dependencies on XMLHelper | 59 | From classes-using-xmlhelper.txt |

---

## Files Generated

| File | Description | Size |
|------|-------------|------|
| [`all-agg-imports.txt`](all-agg-imports.txt) | All AGG package imports | 432 lines |
| [`xml-imports.txt`](xml-imports.txt) | XML-related imports | 22 lines |
| [`classes-using-xmlhelper.txt`](classes-using-xmlhelper.txt) | Classes using XMLHelper | 61 lines |
| [`dependencies.puml`](dependencies.puml) | PlantUML dependency diagram | - |
| [`dependencies.md`](dependencies.md) | This document | - |

---

## Visualization

To render the dependency diagram:

1. **Online**: Copy the content of [`dependencies.puml`](dependencies.puml) to [PlantUML Online](https://www.plantuml.com/plantuml/)
2. **Local**: Install PlantUML and run:
   ```bash
   java -jar plantuml.jar docs/refactoring/dependencies.puml
   ```
   This will generate `dependencies.png`

---

*Generated: 2026-09-08*
*Source: Step 1.3 - Create Dependency Graph*
