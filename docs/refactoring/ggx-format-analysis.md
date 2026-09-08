# AGG XML File Format (.ggx) Analysis


## File Extension
- **Primary**: `.ggx` (AGG XML Files)
- **Alternative**: `.xml` (some older files may use this extension)

---

## Root Element

All .ggx files start with the following root element:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document version="1.0">
  ...content...
</Document>
```

### Version Information
| Version | Description | First Seen |
|---------|-------------|------------|
| 1.0 | Current version | All analyzed files |

---

## Character Encoding
- **Encoding**: UTF-8 (declared in XML header)
- **Special Characters**: German umlauts (ä, ö, ü, ß) are present in some files
- **Whitespace**: Mixed (tabs and spaces)

---

## Top-Level Elements

### 1. Document (Root)
- **Attributes**: `version` (required, always "1.0")
- **Children**: One or more top-level container elements
- **Cardinality**: Exactly one per file

### 2. GraphTransformationSystem
- **Purpose**: Main container for AGG grammar systems
- **Attributes**:
  - `ID` (string, required) - Unique identifier
  - `directed` (boolean) - Whether the graph is directed
  - `name` (string) - Human-readable name
  - `parallel` (boolean) - Whether parallel execution is enabled
- **Children**: Multiple sections including Types, Rules, Graph, etc.
- **Cardinality**: Typically one per file (can be multiple in some cases)

### 3. Graph (Standalone)
- **Purpose**: Standalone graph that is not part of a transformation system
- **Attributes**:
  - `ID` (string, required)
  - `kind` (string) - Type of graph (e.g., "HOST", "TG" for TypeGraph)
  - `name` (string)
- **Can appear**: As top-level element or nested within GraphTransformationSystem

### 4. Rule
- **Purpose**: Transformation rule definition
- **Can appear**: As top-level or nested within GraphTransformationSystem/Rules

---

## Structure Elements

### Type Definitions

#### Types Container
- **Purpose**: Container for all type definitions
- **Children**: NodeType, EdgeType

#### NodeType
- **Purpose**: Defines a node type in the graph grammar
- **Attributes**:
  - `ID` (string, required) - Unique identifier
  - `abstract` (boolean) - Whether this is an abstract type
  - `name` (string) - Display name with encoding format
- **Name Format**: `DisplayName%:SHAPE:COLOR:STYLE:`
  - Example: `Person%:RECT:java.awt.Color[r=0,g=0,b=0]:[NODE]:`
  - Components:
    - `Person` - Display name
    - `RECT` - Shape type
    - `java.awt.Color[r=0,g=0,b=0]` - Color specification
    - `[NODE]` - Style indicator
- **Children**: AttrType elements (attribute type definitions)

#### EdgeType
- **Purpose**: Defines an edge/arc type
- **Attributes**: Same as NodeType plus:
  - `comment` (string, optional) - Description
- **Name Format**: Similar to NodeType but with `[EDGE]` style
  - Example: `loves%:SOLID_LINE:java.awt.Color[r=0,g=0,b=0]:[EDGE]:`

#### AttrType
- **Purpose**: Defines an attribute type within a NodeType
- **Attributes**:
  - `ID` (string, required)
  - `attrname` (string) - Attribute name
  - `typename` (string) - Java type name (String, int, float, etc.)
  - `visible` (boolean) - Whether attribute is visible in UI

### Graph Structure

#### Graph Container
- **Attributes**:
  - `ID` (string, required)
  - `kind` (string) - Graph kind (HOST, TG, etc.)
  - `name` (string)

#### Nodes Container
- **Purpose**: Container for graph nodes
- **Children**: Node elements

#### Node
- **Purpose**: Represents a graph node
- **Attributes**:
  - `ID` (string, required) - Unique identifier
  - `type` (string) - Reference to NodeType ID
- **Children**:
  - Attribute (for attribute values)
  - NodeLayout (for visual positioning)
  - additionalLayout (for layout preferences)

#### Arcs/Edges Container
- **Purpose**: Container for graph edges/arcs
- **Note**: Can appear as `<Arcs>` or `<Edges>`
- **Children**: Arc or Edge elements

#### Arc/Edge
- **Purpose**: Represents a graph edge/arc
- **Attributes**:
  - `ID` (string, required)
  - `type` (string) - Reference to EdgeType ID
  - `source` (string) - Source node ID
  - `target` (string) - Target node ID
  - `sourcemin`, `sourcemax`, `targetmin`, `targetmax` (integers, optional) - Cardinality constraints
- **Children**:
  - EdgeLayout (for visual positioning)

### Layout Elements

#### NodeLayout
- **Purpose**: Visual positioning of nodes
- **Attributes**:
  - `X` (integer) - X coordinate
  - `Y` (integer) - Y coordinate

#### EdgeLayout
- **Purpose**: Visual positioning of edges
- **Attributes**:
  - `bendX` (integer) - X coordinate of bend point
  - `bendY` (integer) - Y coordinate of bend point

#### additionalLayout
- **Purpose**: Additional layout preferences
- **Attributes**:
  - `age` (integer)
  - `force` (integer)
  - `frozen` (boolean)
  - `zone` (integer)
  - `preflength` (integer)
  - `aktlength` (integer)

### Rule Structure

#### Rule Container
- **Purpose**: Container for transformation rules
- **Children**: Rule elements

#### Rule Element
- **Attributes**:
  - `ID` (string, required)
  - `name` (string)
  - `enabled` (boolean)
  - `priority` (integer)
  - `layer` (string)
- **Children**:
  - LHS (Left-Hand Side - precondition)
  - RHS (Right-Hand Side - postcondition)
  - NACs (Negative Application Conditions)
  - PACs (Positive Application Conditions)
  - Parameters
  - AttrConditions

#### LHS/RHS
- **Purpose**: Left/Right hand side of a rule
- **Children**:
  - Nodes
  - Arcs/Edges
  - AttrConditions (optional)

#### Nodes (within LHS/RHS)
- **Purpose**: Container for nodes in rule patterns
- **Children**: Node elements (same structure as graph nodes)

#### Arcs (within LHS/RHS)
- **Purpose**: Container for arcs in rule patterns
- **Children**: Arc/Edge elements

### Application Conditions

#### NAC (Negative Application Condition)
- **Purpose**: Negative condition that must NOT be satisfied
- **Children**: Similar structure to rule patterns

#### PAC (Positive Application Condition)
- **Purpose**: Positive condition that must be satisfied
- **Children**: Similar structure to rule patterns

#### AttrCondition
- **Purpose**: Attribute constraint condition
- **Children**: Formula elements

#### Formula
- **Purpose**: Mathematical or logical formula
- **Attributes**:
  - `formula` (string) - The formula expression
  - `type` (string) - Type of formula
- **Example**: `(x&gt;=16)&amp;&amp;(x&lt;70)`

---

## Attribute Value Elements

### Attribute
- **Purpose**: Assigns a value to an attribute
- **Attributes**:
  - `constant` (boolean) - Whether the value is constant
  - `type` (string) - Reference to AttrType ID
  - `variable` (boolean, optional) - Whether the attribute is variable
- **Children**: Value element

### Value
- **Purpose**: Contains the actual attribute value
- **Children**: Type-specific value elements

#### Type-Specific Value Elements
- **`<string>`**: String value
- **`<int>`**: Integer value
- **`<float>`**: Floating-point value

---

## Special Elements

### TaggedValue
- **Purpose**: Key-value pairs for configuration and metadata
- **Attributes**:
  - `Tag` (string) - The key/name
  - `TagValue` (string) - The value
- **Usage**: Used extensively for system configuration
- **Common Tags**:
  - `AttrHandler` - Attribute handler type (e.g., "Java Expr")
  - `Package` - Java package imports
  - `CSP` - Constraint satisfaction problem settings
  - `injective` - Injection settings
  - `dangling` - Dangling edge handling
  - `identification` - Object identification settings
  - `NACs` - Negative application condition settings
  - `PACs` - Positive application condition settings
  - `GACs` - Global application condition settings
  - `consistency` - Consistency checking
  - `TypeGraphLevel` - Type graph level (DISABLED, ENABLED_MAX, etc.)
  - `layered` - Layered transformation
  - `breakAllLayer` - Layer breaking settings
  - `showGraphAfterStep` - UI preference

### SerializedData
- **Purpose**: Contains Java serialized data
- **Format**: Base64-encoded serialized Java objects
- **Example**: `<SerializedData>aced000573720005456e747279...</SerializedData>`
- **Note**: Used for complex objects that couldn't be properly serialized to XML
- **Recommendation**: Avoid in new implementation; use proper XML serialization

### Match
- **Purpose**: Graph matching results
- **Children**: Morphism elements

### Morphism
- **Purpose**: Mapping between source and target graphs
- **Children**: Mapping elements

### Mapping
- **Purpose**: Individual node/edge mapping
- **Attributes**:
  - `source` (string)
  - `target` (string)

---

## Value Formats

### Strings
- Plain text, can contain special characters
- Escaped HTML entities (e.g., `&gt;` for `>`, `&lt;` for `<`, `&amp;` for `&`)
- Can contain German umlauts (ä, ö, ü, ß)

### Integers
- Standard decimal format (e.g., `13`, `0`, `-5`)

### Floats
- Standard decimal format (e.g., `1.5`, `0.0`, `3.14159`)

### Booleans
- Format: `"true"` or `"false"` (as string values)
- Sometimes: `"1"` or `"0"`

### References
- ID strings (e.g., `"I1"`, `"I123"`, `"ref-1"`)
- Used in `type`, `source`, `target` attributes

### Colors
- Java AWT Color format: `java.awt.Color[r=0,g=0,b=0]`
- RGB values from 0-255

---

## Nesting Hierarchy

```
Document
└── GraphTransformationSystem
    ├── TaggedValue*
    ├── Types
    │   ├── NodeType*
    │   │   └── AttrType*
    │   └── EdgeType*
    ├── Rules
    │   └── Rule*
    │       ├── Parameters (optional)
    │       ├── LHS
    │       │   ├── Nodes
    │       │   │   └── Node*
    │       │   │       ├── Attribute*
    │       │   │       │   └── Value
    │       │   │       │       └── string|int|float
    │       │   │       └── NodeLayout (optional)
    │       │   │       └── additionalLayout (optional)
    │       │   └── Arcs/Edges
    │       │       └── Arc/Edge*
    │       │           └── EdgeLayout (optional)
    │       ├── RHS (similar to LHS)
    │       ├── NACs (optional)
    │       ├── PACs (optional)
    │       └── AttrConditions (optional)
    │           └── AttrCondition*
    │               └── Formula*
    └── Graph*
        ├── Nodes
        │   └── Node*
        │       ├── Attribute*
        │       └── NodeLayout (optional)
        └── Arcs/Edges
            └── Arc/Edge*
                └── EdgeLayout (optional)
```

---

## File Size Ranges

Based on analyzed samples:

| Category | Size Range | Example Files | Description |
|----------|------------|---------------|-------------|
| **Small** | < 100 KB | `small_graph.ggx` (11-12 KB) | Simple graphs with few nodes/edges |
| **Medium** | 100 KB - 1 MB | `medium_graph.ggx` (96 KB) | Typical grammars with multiple rules |
| **Large** | 1-10 MB | `large_graph.ggx` (283 KB) | Complex grammars with many rules and types |
| **Very Large** | > 10 MB | Not found in samples | Rare, very complex grammars |

---

## Element Frequency Analysis

### Most Common Elements (from analyzed samples)

| Rank | Element | Count | Category |
|------|---------|-------|----------|
| 1 | additionalLayout | 1084 | Layout |
| 2 | Node | 647 | Graph Structure |
| 3 | NodeLayout | 608 | Layout |
| 4 | Edge | 491 | Graph Structure |
| 5 | EdgeLayout | 476 | Layout |
| 6 | Value | 354 | Attribute Values |
| 7 | Attribute | 348 | Attributes |
| 8 | Mapping | 272 | Matching |
| 9 | string | 240 | Value Types |
| 10 | Graph | 155 | Graph Structure |

### Type Elements
| Rank | Element | Count |
|------|---------|-------|
| 1 | NodeType | 20 |
| 2 | EdgeType | 12 |
| 3 | AttrType | 75 |

### Rule Elements
| Rank | Element | Count |
|------|---------|-------|
| 1 | Rule | 44 |
| 2 | NAC | 42 |
| 3 | PAC | 11 |
| 4 | ApplCondition | 25 |

### Condition Elements
| Rank | Element | Count |
|------|---------|-------|
| 1 | Condition | 6 |
| 2 | Formula | 2 |
| 3 | AttrCondition | 2 |

---

## Attribute Analysis

### Common Attributes (46 unique attributes found)

#### Identifier Attributes
- `ID` - Unique identifier (most common)
- `name` - Human-readable name
- `type` - Type reference
- `source` - Source node reference
- `target` - Target node reference

#### Graph Structure Attributes
- `directed` - Whether graph is directed
- `parallel` - Parallel execution flag
- `kind` - Graph kind (HOST, TG, etc.)
- `abstract` - Abstract type flag
- `visible` - Visibility flag
- `constant` - Constant value flag
- `variable` - Variable flag

#### Layout Attributes
- `X` - X coordinate
- `Y` - Y coordinate
- `bendX` - Edge bend X coordinate
- `bendY` - Edge bend Y coordinate
- `force` - Layout force
- `frozen` - Frozen position flag
- `zone` - Layout zone
- `preflength` - Preferred length
- `aktlength` - Actual length
- `textOffsetX` - Text offset X
- `textOffsetY` - Text offset Y
- `loopH` - Loop height
- `loopW` - Loop width

#### Type Definition Attributes
- `typename` - Java type name (String, int, float, etc.)
- `attrname` - Attribute name
- `comment` - Description/comment
- `encoding` - Character encoding (in XML declaration)
- `version` - Version (in Document and other elements)

#### Edge Cardinality Attributes
- `sourcemin` - Minimum source cardinality
- `sourcemax` - Maximum source cardinality
- `targetmin` - Minimum target cardinality
- `targetmax` - Maximum target cardinality

#### System Configuration Attributes
- `Tag` - Tag name (in TaggedValue)
- `TagValue` - Tag value (in TaggedValue)
- `enabled` - Enabled flag
- `priority` - Priority level
- `layer` - Layer assignment
- `image` - Image reference
- `orig` - Original reference
- `formula` - Formula expression
- `value` - Generic value

### Attribute Value Types
1. **String**: Most common, for names, comments, formulas
2. **Boolean**: true/false, for flags
3. **Integer**: For coordinates, IDs, counts
4. **Float**: For layout preferences, measurements
5. **Color**: Special format for colors

---

## Encoding and Special Characters

### Character Encoding
- **Declared**: UTF-8
- **Used Characters**:
  - Standard ASCII
  - German umlauts: ä, ö, ü, ß
  - Mathematical symbols in formulas: >, <, =, &
  - HTML entities: &gt;, &lt;, &amp;

### Whitespace
- Mixed usage of tabs and spaces
- Indentation varies (typically 2-4 spaces)

---

## Sample Files Used for Analysis

| File | Size | Type | Description |
|------|------|------|-------------|
| small_graph.ggx | 11 KB | Simple | Lovers Graph example |
| small_graph_layered.ggx | 12 KB | Simple | Layered Lovers Graph |
| medium_graph.ggx | 96 KB | Medium | Knots Semantik |
| large_graph.ggx | 283 KB | Large | TicTacToe Semantik |

---

## Observations and Notes

1. **Rich Metadata**: The format includes extensive metadata via TaggedValue elements for configuration.

2. **Visual Information**: Layout information (NodeLayout, EdgeLayout, additionalLayout) is embedded directly in the XML, making the format both data and presentation-oriented.

3. **Type Safety**: Strong typing with AttrType definitions and type references.

4. **Complex Nesting**: Deeply nested structure, especially for rules with multiple condition types.

5. **Java Integration**: Close integration with Java types (java.awt.Color, type names like String/int/float).

6. **HTML Entity Escaping**: Formulas and special characters use HTML entity escaping.

7. **ID-based References**: Extensive use of ID references for linking elements together.

---

## Recommendations for New Implementation

1. **Preserve Structure**: Maintain the same nesting hierarchy for backward compatibility.

2. **Handle Escaping**: Properly handle HTML entities in string values, especially in formulas.

3. **Support All Attributes**: Ensure all 46 identified attributes are supported.

4. **Type System**: Implement the type system with AttrType definitions.

5. **Layout Information**: Decide whether to preserve layout information or separate it.

6. **TaggedValue**: Support the TaggedValue mechanism for extensibility.

7. **Validation**: Validate ID references and type references.

---

*Generated: 2026-09-08*
*Source: Step 1.2 - Analyze .ggx File Format*
*Sample Files: 4 .ggx files (11KB - 283KB)*
