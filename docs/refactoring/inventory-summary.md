# XML Serialization Inventory Summary


## Counts

| Category | Count | Notes |
|----------|-------|-------|
| XMLObject implementations | 98 classes | Includes all classes implementing XMLObject interface |
| XMLHelper usages | 59 classes | Classes that import or instantiate XMLHelper |
| save_to_xml/read_from_xml calls | 39 occurrences | Method calls across the codebase |
| XwriteObject/XreadObject methods | 94 methods | Serialization method implementations |

## Detailed Breakdown

### XMLObject Implementations (98 classes)

#### Core Domain Classes (agg.xt_basis) - 15 classes
- Graph, Rule, Node, Arc
- GraGra, TypeGraph, Match
- GraphObject (abstract)
- TypeImpl, NodeTypeImpl, ArcTypeImpl
- GraphOrientationDirected, GraphOrientationUndirected
- ApplRuleSequencesGraTraImpl, ParallelRule
- RuleAcStrategie, RuleNacStrategie, RulePacStrategie

#### Attribute Classes (agg.attribute) - 2 classes
- AttrManager
- AttrTupleManager

#### Attribute Parser Classes (agg.attribute.parser.javaExpr) - 47 classes
- ASTAction, ASTAddNode, ASTAllocationExpression, ASTArrayAllocation
- ASTArrayIndex, ASTCharConstNode, ASTClassName, ASTCondExpr
- ASTEmptyDimension, ASTExpression, ASTFalseNode, ASTField
- ASTFloatConstNode, ASTId, ASTIntConstNode, ASTMemberName
- ASTMethod, ASTModNode, ASTNullLiteral, ASTPrimaryExpression
- ASTStringConstNode, ASTTrueNode
- BoolNode, BOOLtoBOOLnode, BOOLxBOOLtoBOOLnode, MemberNode
- NUMtoNUMnode, NUMxNUMtoBOOLnode, NUMxNUMtoNUMnode
- ObjectConstNode, OpMemberNode, TYPE1xTYPE1toBOOL

#### Parser Classes (agg.parser) - 11 classes
- ConflictsDependenciesContainer, DependencyPairContainer
- ExcludePairContainer, LayeredDependencyPairContainer
- LayeredExcludePairContainer, PriorityDependencyPairContainer
- PriorityExcludePairContainer, LayerFunction

#### Rule Application Classes (agg.ruleappl) - 2 classes
- ApplRuleSequence
- RuleSequence

#### Convert Classes (agg.convert) - 1 class
- WSDL2ggx

#### Editor Classes (agg.editor.impl) - 13 classes
- EdConstraint, EdGraGra, EdGraph, EdArc
- EdAtomic, EdNAC, EdNode, EdNestedApplCond
- EdPAC, EdRuleScheme, EdRule, EdType

#### GUI Classes (agg.gui) - 15+ classes
- ApplRuleSequenceSaveLoad
- ConflictsDependenciesContainerSaveLoad (in cpa)
- Various treeview nodedata classes
- Layout classes (LayoutArc, LayoutNode, LayoutPattern)

#### Utility Classes (agg.util) - 1 class
- LinkedGOHashSet

#### AGT Classes (agg.xt_basis.agt) - 4 classes
- AmalgamatedRule, KernelRule, MultiRule, RuleScheme

---

### XMLHelper Usages (59 classes)

#### Core Domain (agg.xt_basis) - 14 classes
- Graph, Rule, Node, Arc, GraGra
- GraphElement, GraphObject, Match
- TypeGraph, TypeImpl, NodeTypeImpl, ArcTypeImpl
- OrdinaryMorphism, AGGBasicAppl

#### Attribute Implementation (agg.attribute.impl) - 6 classes
- CondMember, CondTuple, DeclMember, DeclTuple
- ValueMember, ValueTuple, VarTuple

#### Constraints (agg.cons) - 2 classes
- AtomConstraint, Formula

#### Parser (agg.parser) - 9 classes
- ComputeCriticalPairs, ConflictsDependenciesContainer
- DependencyPairContainer, ExcludePairContainer
- LayeredDependencyPairContainer, LayeredExcludePairContainer
- PriorityDependencyPairContainer, PriorityExcludePairContainer
- LayerFunction

#### Rule Application (agg.ruleappl) - 2 classes
- ApplRuleSequence, RuleSequence

#### Convert (agg.convert) - 3 classes
- AGG2ColorGraph, ConverterWSDL, WSDL2ggx

#### Editor (agg.editor.impl) - 10 classes
- EdArc, EdAtomic, EdConstraint, EdGraGra, EdGraph
- EdNestedApplCond, EdNode, EdRule, EdRuleScheme, EdType

#### GUI (agg.gui) - 9 classes
- ConflictsDependenciesContainerSaveLoad
- GraGraLoad, GraGraSave, GraGraTreeView
- PairIOGUI, ApplRuleSequenceSaveLoad

#### Layout (agg.layout.evolutionary) - 3 classes
- LayoutArc, LayoutNode, LayoutPattern

---

### save_to_xml/read_from_xml Calls (39 occurrences)

#### XMLHelper Definition (2 occurrences)
- XMLHelper.java:119 - save_to_xml method definition
- XMLHelper.java:155 - read_from_xml method definition

#### Core Usage (6 occurrences)
- AGGBasicAppl.java: 4 calls (3 read, 1 save)
- GraGra.java: 3 calls (1 save, 2 read)

#### Convert (4 occurrences)
- AGG2ColorGraph.java: 1 read
- ConverterWSDL.java: 1 read
- WSDL2ggx.java: 2 calls

#### Parser (2 occurrences)
- ComputeCriticalPairs.java: 2 save, 1 read

#### Rule Application (4 occurrences)
- ApplRuleSequence.java: 1 save, 2 read
- RuleSequence.java: 1 save

#### Editor (1 occurrence)
- EdGraGra.java: 1 save

#### GUI (17 occurrences)
- PairIOGUI.java: 3 save, 2 read
- ApplRuleSequenceSaveLoad.java: 1 save
- GraGraLoad.java: 4 read
- GraGraSave.java: 2 save
- GraGraTreeView.java: 1 read

---

### XwriteObject/XreadObject Methods (94 methods)

#### Interface Definition (2 methods)
- XMLObject.java: XwriteObject, XreadObject interface methods

#### Abstract Class (2 methods)
- GraphObject.java: abstract XwriteObject, XreadObject

#### Core Domain (agg.xt_basis) - 12 methods
- Arc: XwriteObject, XreadObject
- ArcTypeImpl: XwriteObject, XreadObject
- Graph: XwriteObject, XreadObject
- GraGra: XwriteObject, XreadObject
- Match: XwriteObject, XreadObject
- Node: XwriteObject, XreadObject
- NodeTypeImpl: XwriteObject, XreadObject
- Rule: XwriteObject, XreadObject
- TypeGraph: XwriteObject, XreadObject
- TypeImpl: XwriteObject, XreadObject

#### Attribute Implementation (6 methods)
- CondMember: XwriteObject, XreadObject
- CondTuple: XwriteObject, XreadObject
- DeclMember: XwriteObject, XreadObject
- DeclTuple: XwriteObject, XreadObject
- ValueMember: XwriteObject, XreadObject
- ValueTuple: XwriteObject, XreadObject
- VarTuple: XwriteObject, XreadObject

#### Constraints (2 methods)
- AtomConstraint: XwriteObject, XreadObject
- Formula: XwriteObject, XreadObject

#### Parser (6 methods)
- ConflictsDependenciesContainer: XwriteObject, XreadObject
- DependencyPairContainer: XwriteObject, XreadObject
- ExcludePairContainer: XwriteObject, XreadObject
- LayeredDependencyPairContainer: XwriteObject, XreadObject
- LayeredExcludePairContainer: XwriteObject, XreadObject
- PriorityDependencyPairContainer: XwriteObject, XreadObject
- PriorityExcludePairContainer: XwriteObject, XreadObject
- LayerFunction: XwriteObject, XreadObject

#### Rule Application (2 methods)
- ApplRuleSequence: XwriteObject, XreadObject

#### AGT Classes (4 methods)
- MultiRule: XwriteObject, XreadObject
- RuleScheme: XwriteObject, XreadObject

#### Convert (2 methods)
- WSDL2ggx: XwriteObject, XreadObject

#### Editor (12 methods)
- EdArc: XwriteObject, XreadObject
- EdAtomic: XwriteObject, XreadObject
- EdConstraint: XwriteObject, XreadObject
- EdGraGra: XwriteObject, XreadObject
- EdGraph: XwriteObject, XreadObject
- EdNestedApplCond: XwriteObject, XreadObject
- EdNode: XwriteObject, XreadObject
- EdRule: XwriteObject, XreadObject
- EdRuleScheme: XwriteObject, XreadObject
- EdType: XwriteObject, XreadObject

#### GUI (4 methods)
- ApplRuleSequenceSaveLoad: XwriteObject, XreadObject

#### Layout (6 methods)
- LayoutArc: XwriteObject, XreadObject
- LayoutNode: XwriteObject, XreadObject
- LayoutPattern: XwriteObject, XreadObject

---

## Package Statistics

| Package | XMLObject Impl. | XMLHelper Usage | Xwrite/Xread Methods |
|---------|-----------------|-----------------|----------------------|
| agg.xt_basis | 15 | 14 | 12 |
| agg.attribute.impl | 6 | 6 | 6 |
| agg.cons | 2 | 2 | 2 |
| agg.parser | 11 | 9 | 14 |
| agg.ruleappl | 2 | 2 | 2 |
| agg.convert | 1 | 3 | 2 |
| agg.editor.impl | 13 | 10 | 12 |
| agg.gui.* | 15+ | 9 | 4 |
| agg.layout.* | 3 | 3 | 6 |
| agg.util | 1 | 0 | 0 |

---

## Key Findings

1. **High Concentration in Core Packages**: Die meisten XML-bezogenen Klassen befinden sich in `agg.xt_basis` und `agg.editor.impl`.

2. **Attribute Parser Classes**: Eine überraschend hohe Anzahl von Klassen im `agg.attribute.parser.javaExpr`-Paket implementieren XMLObject (47 Klassen). Dies sind wahrscheinlich AST-Knoten für den Attribut-Parsing-Baum.

3. **GUI Dependencies**: Viele GUI-Klassen nutzen XMLHelper direkt, insbesondere für Save/Load-Operationen.

4. **Dual Usage Pattern**: Viele Klassen sowohl implementieren XMLObject als auch XMLHelper direkt nutzen.

5. **Method Call Distribution**: Die meisten `save_to_xml`/`read_from_xml`-Aufrufe finden in GUI- und Editor-Klassen statt.

---

*Generated: 2026-09-08*
*Source: Step 1.1 - Create Complete Inventory*
