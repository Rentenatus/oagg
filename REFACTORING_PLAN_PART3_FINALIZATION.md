# 📁 AGG XML Extraction - Part 3: Finalization


## 🎯 Part 3 Overview
**Covers:** Phase 6-9 + Rollback + Appendices  
**Duration:** Week 16-25 (10 weeks)  
**Objective:** Complete migration, validate, optimize, and finalize extraction

---

## 6️⃣ Phase 6: Domain Object Migration (Week 16-20)

### 🎯 Goal
Migrate all domain classes to use the new XML serialization system through adapters.

**Note:** We do NOT modify the domain classes themselves - we only change how they are used by the XML system.

---

### 📌 Step 6.1: Create Adapters for All Domain Classes

**Priority Order:**

| Priority | Package | Classes | Count | Notes |
|----------|---------|---------|-------|-------|
| 1 | agg.xt_basis | Graph, Rule, Node, Arc, GraGra, TypeGraph, Match, TypeImpl | ~20 | Core domain |
| 1 | agg.attribute.impl | ValueTuple, VarTuple, CondTuple, DeclTuple | ~10 | Attributes |
| 2 | agg.parser | Containers, LayerFunction | ~15 | Parser |
| 2 | agg.cons | Formula, AtomConstraint | ~5 | Constraints |
| 2 | agg.ruleappl | ApplRuleSequence | ~5 | Rule application |
| 3 | agg.editor.impl | EdGraph, EdNode, EdArc, EdType, EdRule | ~30 | Editor |
| 4 | agg.gui.* | SaveLoad classes | ~5 | GUI |

**Total:** ~90 adapters to create

---

### 📌 Step 6.2: Adapter Implementation Strategy

#### **Template for Domain Adapters**

```java
package agg.xml.adapter;

import agg.xt_basis.[ClassName];
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializable;
import agg.xml.core.XMLSerializerContext;
import agg.xml.exception.XMLSerializationException;
import agg.xml.mapper.ReferenceResolver;

/**
 * Adapter for [ClassName] that bridges to the new XML serialization.
 */
public class [ClassName]Adapter implements XMLSerializable {
    
    private final [ClassName] [variableName];
    private final ReferenceResolver referenceResolver;
    
    public [ClassName]Adapter([ClassName] [variableName]) {
        this([variableName], null);
    }
    
    public [ClassName]Adapter([ClassName] [variableName], ReferenceResolver resolver) {
        if ([variableName] == null) {
            throw new IllegalArgumentException("[ClassName] cannot be null");
        }
        this.[variableName] = [variableName];
        this.referenceResolver = resolver;
    }
    
    @Override
    public void serialize(XMLSerializerContext context) throws XMLSerializationException {
        // Register for references
        String id = referenceResolver != null ? 
            referenceResolver.register([variableName]) : null;
        
        // Begin element
        context.beginObject("[ElementName]");
        
        // Write ID if using references
        if (id != null) {
            context.writeAttribute("id", id);
        }
        
        // Write all attributes from the domain object
        // Example:
        context.writeAttribute("name", [variableName].getName());
        
        // Write all child elements
        // For each collection or child object:
        // context.beginArray("ChildName", [variableName].getChildCount());
        // for (ChildType child : [variableName].getChildren()) {
        //     context.writeObject(new ChildAdapter(child, referenceResolver));
        // }
        // context.endArray();
        
        // End element
        context.endObject();
    }
    
    @Override
    public void deserialize(XMLDeserializerContext context) throws XMLSerializationException {
        // Read ID and register
        String id = context.readAttribute("id");
        if (id != null && referenceResolver != null) {
            referenceResolver.register([variableName], id);
        }
        
        // Read all attributes
        // Example:
        [variableName].setName(context.readAttribute("name"));
        
        // Read all child elements
        // For each expected child:
        // if (context.nextChild() && context.isCurrentChild("ChildName")) {
        //     // Read array or single element
        //     List<ChildType> children = context.readArray("ChildElement", ChildType.class);
        //     for (ChildType child : children) {
        //         [variableName].addChild(child);
        //     }
        // }
    }
}
```

---

### 📌 Step 6.3: List of All Required Adapters

#### **agg.xt_basis Package (20 adapters)**

1. **GraphAdapter** - Already created in Phase 4
2. **RuleAdapter** - Already created in Phase 4
3. **NodeAdapter** - Already created in Phase 4
4. **ArcAdapter** - Already created in Phase 4
5. **GraGraAdapter** - Already created in Phase 4
6. **TypeGraphAdapter**
7. **MatchAdapter**
8. **TypeImplAdapter**
9. **NodeTypeImplAdapter**
10. **ArcTypeImplAdapter**
11. **GraphObjectAdapter** (abstract - may not need)
12. **AGGBasicApplAdapter**
13. **BaseFactoryAdapter** (if needed)

#### **agg.attribute.impl Package (10 adapters)**

1. **ValueTupleAdapter**
2. **VarTupleAdapter**
3. **CondTupleAdapter**
4. **DeclTupleAdapter**
5. **ValueMemberAdapter**
6. **CondMemberAdapter**
7. **DeclMemberAdapter**
8. **AttrInstanceAdapter**
9. **AttrTypeAdapter**
10. **AttrVariableTupleAdapter**

#### **agg.parser Package (15 adapters)**

1. **ConflictsDependenciesContainerAdapter**
2. **DependencyPairContainerAdapter**
3. **ExcludePairContainerAdapter**
4. **LayeredDependencyPairContainerAdapter**
5. **LayeredExcludePairContainerAdapter**
6. **PriorityDependencyPairContainerAdapter**
7. **PriorityExcludePairContainerAdapter**
8. **LayerFunctionAdapter**
9. **PairContainerAdapter** (if abstract)

#### **agg.cons Package (5 adapters)**

1. **FormulaAdapter**
2. **AtomConstraintAdapter**
3. **ConstraintAdapter** (if exists)

#### **agg.ruleappl Package (5 adapters)**

1. **ApplRuleSequenceAdapter**
2. **RuleSequenceAdapter**

#### **agg.editor.impl Package (30 adapters)**

1. **EdGraphAdapter**
2. **EdNodeAdapter**
3. **EdArcAdapter**
4. **EdTypeAdapter**
5. **EdRuleAdapter**
6. **EdRuleSchemeAdapter**
7. **EdAtomicAdapter**
8. **EdConstraintAdapter**
9. **EdNestedApplCondAdapter**
10. **EdGraGraAdapter** (extends GraGraAdapter)

#### **agg.gui.* Packages (10 adapters)**

1. **ConflictsDependenciesContainerSaveLoadAdapter**
2. **ApplRuleSequenceSaveLoadAdapter**

---

### 📌 Step 6.4: Batch Adapter Creation

**Use Code Generation Script:**

Create a script to generate adapter skeletons:

```bash
#!/bin/bash
# generate-adapters.sh

# Configuration
PACKAGE="agg.xt_basis"
OUTPUT_DIR="agg-xml/src/main/java/agg/xml/adapter"
TEMPLATE="adapter-template.txt"

# List of classes to create adapters for
CLASSES=(
    "Graph"
    "Rule" 
    "Node"
    "Arc"
    "GraGra"
    "TypeGraph"
    "Match"
    "TypeImpl"
    "NodeTypeImpl"
    "ArcTypeImpl"
)

for CLASS in "${CLASSES[@]}"; do
    # Create filename
    FILENAME="${CLASS}Adapter.java"
    
    # Replace placeholders in template
    sed "s/\[ClassName\]/${CLASS}/g" "${TEMPLATE}" | \
    sed "s/\[ElementName\]/${CLASS}/g" | \
    sed "s/\[VariableName\]/obj/g" | \
    sed "s/\[Package\]/agg.xt_basis/g" > "${OUTPUT_DIR}/${FILENAME}"
    
    echo "Created: ${FILENAME}"
done
```

**Template File (adapter-template.txt):**
```java
package agg.xml.adapter;

import agg.[Package].[ClassName];
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializable;
import agg.xml.core.XMLSerializerContext;
import agg.xml.exception.XMLSerializationException;
import agg.xml.mapper.ReferenceResolver;

public class [ClassName]Adapter implements XMLSerializable {
    
    private final [ClassName] obj;
    private final ReferenceResolver referenceResolver;
    
    public [ClassName]Adapter([ClassName] obj) {
        this(obj, null);
    }
    
    public [ClassName]Adapter([ClassName] obj, ReferenceResolver resolver) {
        if (obj == null) {
            throw new IllegalArgumentException("[ClassName] cannot be null");
        }
        this.obj = obj;
        this.referenceResolver = resolver;
    }
    
    @Override
    public void serialize(XMLSerializerContext context) throws XMLSerializationException {
        String id = referenceResolver != null ? referenceResolver.register(obj) : null;
        
        context.beginObject("[ElementName]");
        if (id != null) context.writeAttribute("id", id);
        
        // TODO: Serialize attributes and children
        // context.writeAttribute("name", obj.getName());
        // context.writeObject("child", new ChildAdapter(obj.getChild(), referenceResolver));
        
        context.endObject();
    }
    
    @Override
    public void deserialize(XMLDeserializerContext context) throws XMLSerializationException {
        String id = context.readAttribute("id");
        if (id != null && referenceResolver != null) {
            referenceResolver.register(obj, id);
        }
        
        // TODO: Deserialize attributes and children
        // obj.setName(context.readAttribute("name"));
        // if (context.nextChild() && context.isCurrentChild("child")) {
        //     obj.setChild(new ChildAdapter().deserialize(context));
        // }
    }
}
```

---

### 📌 Step 6.5: Implement Adapters Incrementally

**Week 16-17: High Priority Adapters**
- Complete all agg.xt_basis adapters
- Complete all agg.attribute.impl adapters
- Test each adapter after creation

**Week 18-19: Medium Priority Adapters**
- Complete agg.parser adapters
- Complete agg.cons adapters
- Complete agg.ruleappl adapters
- Test each adapter

**Week 20: Low Priority Adapters**
- Complete agg.editor.impl adapters
- Complete agg.gui.* adapters
- Final testing

---

### 📌 Step 6.6: Test All Adapters

**Testing Strategy:**

1. **Unit Tests for Each Adapter**
   - Test serialization
   - Test deserialization
   - Test roundtrip
   - Test with references

2. **Integration Tests**
   - Test complex object graphs
   - Test circular references
   - Test with existing .ggx files

3. **Performance Tests**
   - Compare with baseline
   - Identify bottlenecks

**Test Template:**
```java
package agg.xml.adapter;

import agg.xt_basis.[ClassName];
import agg.xml.core.XMLSerializer;
import agg.xml.core.XMLSerializerFactory;
import agg.xml.core.XMLDeserializer;
import agg.xml.core.XMLDeserializerFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class [ClassName]AdapterTest {

    @Test
    void testSerialization() throws Exception {
        [ClassName] obj = createTest[ClassName]();
        [ClassName]Adapter adapter = new [ClassName]Adapter(obj);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        String xml = serializer.serializeToString(adapter);
        
        assertThat(xml).isNotEmpty();
        assertThat(xml).contains("[ElementName]");
    }

    @Test
    void testDeserialization() throws Exception {
        [ClassName] obj = createTest[ClassName]();
        [ClassName]Adapter adapter = new [ClassName]Adapter(obj);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        String xml = serializer.serializeToString(adapter);
        
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        [ClassName] deserialized = deserializer.deserializeFromString(
            xml, [ClassName].class);
        
        // Use adapter to deserialize into new object
        // This may need adjustment based on actual implementation
        
        assertThat(deserialized).isNotNull();
    }

    @Test
    void testRoundtrip() throws Exception {
        [ClassName] original = createTest[ClassName]();
        [ClassName]Adapter adapter = new [ClassName]Adapter(original);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        File tempFile = File.createTempFile("test-", ".ggx");
        tempFile.deleteOnExit();
        
        serializer.serialize(adapter, tempFile);
        
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        [ClassName] deserialized = deserializer.deserialize(tempFile, [ClassName].class);
        
        // Compare original and deserialized
        assertThat(deserialized).isEqualToComparingFieldByField(original);
    }

    private [ClassName] createTest[ClassName]() {
        // Create and return a test instance
        [ClassName] obj = new [ClassName]();
        // Set test values
        return obj;
    }
}
```

---

### 📌 Step 6.7: Verify All Existing Files Can Be Loaded

**Test with Baseline Files:**
```java
package agg.refactoring;

import agg.xml.core.XMLDeserializer;
import agg.xml.core.XMLDeserializerFactory;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.*;

class BaselineFileCompatibilityTest {

    private static final String[] BASELINE_FILES = {
        "test-data/baseline/samples/small.ggx",
        "test-data/baseline/samples/medium.ggx",
        "test-data/baseline/samples/large.ggx"
    };

    @Test
    void testLoadAllBaselineFiles() throws Exception {
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        
        for (String filename : BASELINE_FILES) {
            File file = new File(filename);
            assertThat(file).exists();
            
            // Load using new deserializer
            Object loaded = deserializer.deserialize(file, Object.class);
            assertThat(loaded).isNotNull();
        }
    }

    @Test
    void testLoadAndSaveBaselineFiles() throws Exception {
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        
        for (String filename : BASELINE_FILES) {
            File inputFile = new File(filename);
            File tempFile = File.createTempFile("test-", ".ggx");
            tempFile.deleteOnExit();
            
            // Load
            Object loaded = deserializer.deserialize(inputFile, Object.class);
            
            // Save
            serializer.serialize(loaded, tempFile);
            
            // Verify file was created
            assertThat(tempFile).exists();
            assertThat(tempFile.length()).isGreaterThan(0);
        }
    }
}
```

---

### 📌 Step 6.8: Finalize Phase 6

**Checklist:**
- [ ] All agg.xt_basis adapters created and tested
- [ ] All agg.attribute.impl adapters created and tested
- [ ] All agg.parser adapters created and tested
- [ ] All agg.cons adapters created and tested
- [ ] All agg.ruleappl adapters created and tested
- [ ] All agg.editor.impl adapters created and tested
- [ ] All agg.gui.* adapters created and tested
- [ ] All adapter unit tests passing
- [ ] All baseline files can be loaded
- [ ] Roundtrip tests passing for all adapters
- [ ] Performance acceptable

**Documentation:**
- [ ] Update `docs/refactoring/MIGRATION_TRACKER.md`
- [ ] Document adapter completion
- [ ] Update phase completion status

**Next Phase**: Phase 7 - Testing & Validation

---

---

## 7️⃣ Phase 7: Testing & Validation (Week 21-22)

### 🎯 Goal
Comprehensive testing and validation of the new XML serialization system.

---

### 📌 Step 7.1: Comprehensive Test Suite

#### **Test Categories**

1. **Unit Tests** - Individual classes
2. **Integration Tests** - Multiple classes working together
3. **Roundtrip Tests** - Save and load without data loss
4. **Compatibility Tests** - Load old files, save new files
5. **Performance Tests** - Measure and compare performance
6. **Stress Tests** - Large files, complex objects
7. **Edge Case Tests** - Special characters, empty values, null values

---

### 📌 Step 7.2: Test Execution Plan

**Daily:**
```bash
# Run all XML module tests
mvn test -pl agg-xml

# Run all migration tests
mvn test -Dtest=*MigrationTest,*AdapterTest
```

**Weekly:**
```bash
# Run full test suite
mvn clean test

# Run performance benchmarks
mvn test -Dtest=*Benchmark
```

**Before Release:**
```bash
# Run comprehensive validation
mvn clean verify
```

---

### 📌 Step 7.3: Roundtrip Testing

**Test all domain classes:**
```java
package agg.refactoring;

import agg.xt_basis.BaseFactory;
import agg.xt_basis.Graph;
import agg.xt_basis.GraGra;
import agg.xt_basis.Rule;
import agg.xml.adapter.*;
import agg.xml.core.XMLSerializer;
import agg.xml.core.XMLSerializerFactory;
import agg.xml.core.XMLDeserializer;
import agg.xml.core.XMLDeserializerFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ComprehensiveRoundtripTest {

    @Test
    void testGraphRoundtrip() throws Exception {
        Graph original = createComplexGraph();
        GraphAdapter adapter = new GraphAdapter(original);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        
        File tempFile = File.createTempFile("graph-", ".ggx");
        tempFile.deleteOnExit();
        
        serializer.serialize(adapter, tempFile);
        Graph loaded = deserializer.deserialize(tempFile, Graph.class);
        
        assertGraphsEqual(original, loaded);
    }

    @Test
    void testGraGraRoundtrip() throws Exception {
        GraGra original = createComplexGraGra();
        GraGraAdapter adapter = new GraGraAdapter(original);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        
        File tempFile = File.createTempFile("gragra-", ".ggx");
        tempFile.deleteOnExit();
        
        serializer.serialize(adapter, tempFile);
        GraGra loaded = deserializer.deserialize(tempFile, GraGra.class);
        
        assertGraGrasEqual(original, loaded);
    }

    @Test
    void testRuleRoundtrip() throws Exception {
        Rule original = createComplexRule();
        RuleAdapter adapter = new RuleAdapter(original);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        
        File tempFile = File.createTempFile("rule-", ".ggx");
        tempFile.deleteOnExit();
        
        serializer.serialize(adapter, tempFile);
        Rule loaded = deserializer.deserialize(tempFile, Rule.class);
        
        assertRulesEqual(original, loaded);
    }

    // More roundtrip tests for other classes...

    private Graph createComplexGraph() {
        Graph graph = BaseFactory.theFactory().createGraph(false);
        // Add nodes, arcs, attributes, etc.
        return graph;
    }

    private GraGra createComplexGraGra() {
        GraGra graGra = BaseFactory.theFactory().createGraGra(false);
        // Add types, rules, graph, etc.
        return graGra;
    }

    private Rule createComplexRule() {
        Rule rule = BaseFactory.theFactory().createRule();
        // Add LHS, RHS, NACs, etc.
        return rule;
    }

    private void assertGraphsEqual(Graph original, Graph loaded) {
        assertThat(loaded.getName()).isEqualTo(original.getName());
        assertThat(loaded.getNodes().size()).isEqualTo(original.getNodes().size());
        assertThat(loaded.getArcs().size()).isEqualTo(original.getArcs().size());
        // More detailed comparisons
    }

    private void assertGraGrasEqual(GraGra original, GraGra loaded) {
        assertThat(loaded.getName()).isEqualTo(original.getName());
        assertThat(loaded.getRules().size()).isEqualTo(original.getRules().size());
        assertThat(loaded.getGraphs().size()).isEqualTo(original.getGraphs().size());
        // More detailed comparisons
    }

    private void assertRulesEqual(Rule original, Rule loaded) {
        assertThat(loaded.getName()).isEqualTo(original.getName());
        // More detailed comparisons
    }
}
```

---

### 📌 Step 7.4: Compatibility Testing

**Test loading all existing .ggx files:**
```java
package agg.refactoring;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.*;

class FileCompatibilityTest {

    @Test
    void testLoadAllSampleFiles() throws Exception {
        File sampleDir = new File("test-data/compatibility/samples");
        File[] files = sampleDir.listFiles();
        
        assertThat(files).isNotEmpty();
        
        for (File file : files) {
            if (file.getName().endsWith(".ggx")) {
                testLoadFile(file);
            }
        }
    }

    private void testLoadFile(File file) throws Exception {
        // Try to load with new system
        // Try to save with new system
        // Verify no errors
    }
}
```

---

### 📌 Step 7.5: Performance Testing

**Compare old vs new:**
```java
package agg.refactoring;

import agg.xt_basis.GraGra;
import agg.util.XMLHelper;
import agg.xml.core.XMLSerializer;
import agg.xml.core.XMLSerializerFactory;
import agg.xml.core.XMLDeserializer;
import agg.xml.core.XMLDeserializerFactory;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.*;

class PerformanceComparisonTest {

    @Test
    void testPerformanceComparison() throws Exception {
        GraGra graGra = createTestGraGra();
        File tempFile = File.createTempFile("perf-", ".ggx");
        tempFile.deleteOnExit();
        
        // Test legacy performance
        long legacyStart = System.currentTimeMillis();
        XMLHelper legacyHelper = new XMLHelper();
        legacyHelper.addTopObject(graGra);
        legacyHelper.save_to_xml(tempFile.getAbsolutePath());
        long legacyTime = System.currentTimeMillis() - legacyStart;
        
        // Test new performance
        long newStart = System.currentTimeMillis();
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        serializer.serialize(graGra, tempFile);
        long newTime = System.currentTimeMillis() - newStart;
        
        // New should not be more than 2x slower than legacy
        assertThat(newTime).isLessThan(legacyTime * 2);
        
        System.out.println("Legacy time: " + legacyTime + "ms");
        System.out.println("New time: " + newTime + "ms");
        System.out.println("Ratio: " + (double)newTime/legacyTime);
    }

    private GraGra createTestGraGra() {
        // Create a moderately complex GraGra for testing
    }
}
```

---

### 📌 Step 7.6: Stress Testing

**Test with large files:**
```java
package agg.refactoring;

import agg.xt_basis.GraGra;
import agg.xml.core.XMLSerializer;
import agg.xml.core.XMLSerializerFactory;
import agg.xml.core.XMLDeserializer;
import agg.xml.core.XMLDeserializerFactory;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.*;

class StressTest {

    @Test
    void testLargeGraph() throws Exception {
        // Create a large graph (1000+ nodes, 5000+ arcs)
        GraGra graGra = createLargeGraGra(1000, 5000);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        
        File tempFile = File.createTempFile("large-", ".ggx");
        tempFile.deleteOnExit();
        
        // Should complete without error
        serializer.serialize(graGra, tempFile);
        assertThat(tempFile.length()).isGreaterThan(100000); // > 100KB
        
        GraGra loaded = deserializer.deserialize(tempFile, GraGra.class);
        assertThat(loaded).isNotNull();
    }

    @Test
    void testVeryLargeGraph() throws Exception {
        // Create a very large graph (10000+ nodes, 50000+ arcs)
        // This may take a while and use significant memory
        GraGra graGra = createLargeGraGra(10000, 50000);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        
        File tempFile = File.createTempFile("very-large-", ".ggx");
        tempFile.deleteOnExit();
        
        serializer.serialize(graGra, tempFile);
        assertThat(tempFile.length()).isGreaterThan(1000000); // > 1MB
        
        GraGra loaded = deserializer.deserialize(tempFile, GraGra.class);
        assertThat(loaded).isNotNull();
    }

    private GraGra createLargeGraGra(int nodeCount, int arcCount) {
        // Create a GraGra with specified number of nodes and arcs
    }
}
```

---

### 📌 Step 7.7: Edge Case Testing

**Test special scenarios:**
```java
package agg.refactoring;

import agg.xt_basis.Graph;
import agg.xt_basis.Node;
import agg.xml.adapter.GraphAdapter;
import agg.xml.core.XMLSerializer;
import agg.xml.core.XMLSerializerFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EdgeCaseTest {

    @Test
    void testEmptyGraph() throws Exception {
        Graph graph = new Graph(false);
        GraphAdapter adapter = new GraphAdapter(graph);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        String xml = serializer.serializeToString(adapter);
        
        assertThat(xml).isNotEmpty();
        assertThat(xml).contains("Graph");
    }

    @Test
    void testNullValues() throws Exception {
        Graph graph = new Graph(false);
        graph.setName(null); // Set null name
        GraphAdapter adapter = new GraphAdapter(graph);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        String xml = serializer.serializeToString(adapter);
        
        assertThat(xml).isNotEmpty();
    }

    @Test
    void testSpecialCharacters() throws Exception {
        Graph graph = new Graph(false);
        graph.setName("Test mit Umlauten: ä, ö, ü, ß");
        GraphAdapter adapter = new GraphAdapter(graph);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        String xml = serializer.serializeToString(adapter);
        
        assertThat(xml).contains("ä");
        assertThat(xml).contains("ö");
        assertThat(xml).contains("ü");
        assertThat(xml).contains("ß");
    }

    @Test
    void testVeryLongNames() throws Exception {
        Graph graph = new Graph(false);
        String longName = "a".repeat(1000); // 1000 character name
        graph.setName(longName);
        GraphAdapter adapter = new GraphAdapter(graph);
        
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        String xml = serializer.serializeToString(adapter);
        
        assertThat(xml).contains(longName);
    }

    @Test
    void testCircularReferences() throws Exception {
        // Create objects with circular references
        // Test that serialization handles this correctly
    }
}
```

---

### 📌 Step 7.8: Final Validation

**Validation Checklist:**

- [ ] All unit tests passing
- [ ] All integration tests passing
- [ ] All roundtrip tests passing
- [ ] All baseline files can be loaded
- [ ] All baseline files can be saved and reloaded
- [ ] Performance within 2x of legacy
- [ ] Stress tests passing
- [ ] Edge case tests passing
- [ ] No data loss in any test
- [ ] All .ggx files loadable with new system

**Validation Report:**
Create `docs/refactoring/VALIDATION_REPORT.md` with:
- Test results summary
- Performance comparison
- Compatibility status
- Known issues
- Recommendations

**Sign-off:**
- [ ] Development team approval
- [ ] QA team approval
- [ ] Management approval

---

---

## 8️⃣ Phase 8: Cleanup & Optimization (Week 23-24)

### 🎯 Goal
Clean up the codebase, remove legacy code, and optimize performance.

---

### 📌 Step 8.1: Remove Feature Flags

**Replace feature flags with direct calls:**
```java
// BEFORE
private static final boolean USE_NEW_XML = true;

public boolean save() {
    if (USE_NEW_XML) {
        return saveUsingNewXML();
    } else {
        return saveUsingLegacyXML();
    }
}

// AFTER
public boolean save() {
    return saveUsingNewXML();
}

// Remove old method
private boolean saveUsingLegacyXML() { ... }
```

**Classes to update:**
- GraGraSave.java
- GraGraLoad.java
- EdGraGra.java
- All other classes using feature flags

---

### 📌 Step 8.2: Remove Legacy Code

**Identify and remove:**
1. Old saveUsingLegacyXML methods
2. Old loadUsingLegacyXML methods
3. Unused imports of XMLHelper
4. Unused XMLObject implementations (if any)

**Verify before removing:**
- All tests still passing
- No compilation errors
- No runtime errors

---

### 📌 Step 8.3: Optimize Performance

**Profile the new implementation:**
```bash
# Run performance tests
mvn test -Dtest=*PerformanceTest

# Use Java profiling tools
java -agentlib:hprof=cpu=samples,depth=10,interval=20,file=profile.txt -jar target/app.jar
```

**Optimization opportunities:**
1. **Caching** - Cache type registrations, adapters, etc.
2. **Lazy loading** - Load objects on demand
3. **Batch processing** - Process collections more efficiently
4. **DOM optimization** - Reduce DOM tree creation overhead
5. **Reference resolution** - Optimize ID generation and lookup

**Example optimizations:**

```java
// In AdapterFactory
private final Map<Class<?>, XMLSerializable> adapterCache = new ConcurrentHashMap<>();

public XMLSerializable createAdapter(Object object) {
    Class<?> type = object.getClass();
    
    // Check cache first
    XMLSerializable cached = adapterCache.get(type);
    if (cached != null) {
        return cached;
    }
    
    // Create new adapter
    XMLSerializable adapter = createAdapterInternal(object);
    
    // Cache if the object type is cacheable
    if (isCacheable(type)) {
        adapterCache.put(type, adapter);
    }
    
    return adapter;
}
```

```java
// In TypeRegistry
private final Map<String, Class<?>> typeNameCache = new ConcurrentHashMap<>();
private final Map<Class<?>, String> classTypeNameCache = new ConcurrentHashMap<>();

public void registerType(Class<?> type, String typeName) {
    typeNameCache.put(typeName, type);
    classTypeNameCache.put(type, typeName);
}

public Class<?> getClassForName(String typeName) {
    return typeNameCache.get(typeName);
}
```

---

### 📌 Step 8.4: Clean Up Code

**Refactoring opportunities:**
1. **Extract common code** - Identify duplicate code in adapters
2. **Improve error handling** - Better error messages, more specific exceptions
3. **Improve logging** - Add debug logging for troubleshooting
4. **Improve documentation** - Add JavaDoc, improve comments
5. **Consistent coding style** - Match project conventions

**Code quality checks:**
```bash
# Run Checkstyle
mvn checkstyle:check

# Run PMD
mvn pmd:check

# Run FindBugs
mvn findbugs:check
```

---

### 📌 Step 8.5: Update Documentation

**Update all documentation:**
- [ ] README.md - Update with new XML module info
- [ ] API documentation - Update JavaDoc
- [ ] Architecture documentation - Update diagrams
- [ ] User documentation - Update save/load instructions
- [ ] Developer documentation - Update contributing guidelines

**Create new documentation:**
- [ ] `docs/xml-module.md` - XML module documentation
- [ ] `docs/migration-guide.md` - Guide for future migrations
- [ ] `docs/serialization-guide.md` - How to serialize new classes

---

### 📌 Step 8.6: Final Testing

**Run complete test suite:**
```bash
mvn clean test
```

**Verify:**
- All tests passing
- No warnings
- No errors
- Performance acceptable

---

### 📌 Step 8.7: Finalize Phase 8

**Checklist:**
- [ ] Feature flags removed
- [ ] Legacy code removed
- [ ] Performance optimized
- [ ] Code cleaned up
- [ ] Documentation updated
- [ ] All tests passing
- [ ] Code quality checks passing

**Documentation:**
- [ ] Update `docs/refactoring/MIGRATION_TRACKER.md`
- [ ] Document cleanup and optimization
- [ ] Update phase completion status

**Next Phase**: Phase 9 - Final Cutover

---

---

## 9️⃣ Phase 9: Final Cutover (Week 25)

### 🎯 Goal
Complete the extraction and prepare for release.

---

### 📌 Step 9.1: Final Verification

**Run all validation tests:**
```bash
# Full clean build
mvn clean install

# Run all tests
mvn test

# Run verification
mvn verify
```

**Verify checklist:**
- [ ] All modules compile successfully
- [ ] All tests pass
- [ ] All baseline files can be loaded
- [ ] All baseline files can be saved
- [ ] Performance acceptable
- [ ] No breaking changes
- [ ] Backward compatibility maintained

---

### 📌 Step 9.2: Update Version Numbers

**Update all POM files:**
```xml
<!-- agg-parent/pom.xml -->
<version>2.0.0</version>

<!-- agg-core/pom.xml -->
<version>2.0.0</version>

<!-- agg-xml/pom.xml -->
<version>2.0.0</version>
```

---

### 📌 Step 9.3: Create Release Notes

**Create `RELEASE_NOTES_v2.0.0.md`:**
```markdown
# AGG v2.0.0 Release Notes

## Major Changes

### XML Serialization Extraction

**What changed:**
- XML serialization logic has been extracted to a separate `agg-xml` module
- All XML processing now uses the new serialization infrastructure
- Domain classes remain unchanged but use adapters for XML serialization

**Why it changed:**
- Improved separation of concerns
- Better maintainability
- Easier testing
- Preparation for future enhancements

**Impact:**
- **No breaking changes** - All existing .ggx files remain compatible
- **Zero changes to domain classes** - Graph, Rule, Node, Arc, etc. unchanged
- **Improved performance** - Comparable to previous implementation
- **Better extensibility** - Easy to add new serializable classes

**Migration:**
- No migration required for end users
- Developers can now use the new `agg-xml` module for XML serialization
- See `docs/migration-guide.md` for details on using the new API

## New Features

### New XML Serialization API

The new `agg-xml` module provides a clean, modern API for XML serialization:

```java
// Save an object
XMLSaveLoad.saveObject(graph, new File("graph.ggx"));

// Load an object
Graph graph = XMLSaveLoad.loadObject(new File("graph.ggx"), Graph.class);

// Or use the full API
XMLSerializer serializer = XMLSerializerFactory.createSerializer();
serializer.serialize(graph, new File("graph.ggx"));

XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
Graph loaded = deserializer.deserialize(new File("graph.ggx"), Graph.class);
```

### Adapter Pattern Support

The new system uses adapters to bridge between old and new code:

```java
// Create an adapter for a domain object
GraphAdapter adapter = new GraphAdapter(graph);

// Serialize using the adapter
serializer.serialize(adapter, new File("graph.ggx"));
```

## Bug Fixes

- Fixed various XML serialization edge cases
- Improved error handling and error messages
- Better handling of circular references
- Improved support for special characters (Umlauts)

## Known Issues

- [List any known issues]

## Upgrade Instructions

No action required for end users. The upgrade is transparent.

For developers:
1. Update dependency to agg-xml:2.0.0
2. See migration guide for API changes
3. Report any issues

## Compatibility

- **Backward compatible** with all existing .ggx files
- **Java version**: Requires Java 17+
- **Dependencies**: Apache Xerces 2.12.2+

## Performance

Performance is comparable to the previous implementation:
- Load time: ~95% of previous
- Save time: ~105% of previous
- Memory usage: ~100% of previous
```

---

### 📌 Step 9.4: Create Final Backup

**Create backup of current state:**
```bash
# Create backup directory
git clone /d/git_oagg /d/git_oagg-backup-pre-release

# Tag the current commit
git tag -a v2.0.0-rc1 -m "Release candidate 1 for v2.0.0"
git push origin v2.0.0-rc1

# Create zip archive
cd /d/git_oagg
zip -r agg-v2.0.0-rc1.zip . -x ".git/*" -x "target/*" -x "*.iml" -x ".idea/*"
```

---

### 📌 Step 9.5: Deploy to Repository

**If using Maven:**
```bash
# Deploy to local repository
mvn install

# Deploy to remote repository
mvn deploy
```

**If using Git:**
```bash
# Commit all changes
git add .
git commit -m "Complete XML serialization extraction - v2.0.0"

# Push to remote
git push origin main

# Create release tag
git tag -a v2.0.0 -m "v2.0.0: XML serialization extraction complete"
git push origin v2.0.0
```

---

### 📌 Step 9.6: Finalize Phase 9

**Checklist:**
- [ ] Final verification complete
- [ ] All tests passing
- [ ] Version numbers updated
- [ ] Release notes created
- [ ] Backup created
- [ ] Code deployed
- [ ] Documentation complete

**Documentation:**
- [ ] Update `docs/refactoring/MIGRATION_TRACKER.md`
- [ ] Mark project as complete
- [ ] Final status update

**Celebrate!** 🎉
- XML serialization successfully extracted
- All domain classes unchanged
- Full backward compatibility maintained
- New module ready for future development

---

---

## 🚨 Rollback Plan

### When to Rollback

Rollback if any of the following occur:
- Critical bugs in production
- Data loss or corruption
- Performance degradation > 200%
- Incompatibility with existing files
- Security vulnerabilities introduced

---

### Rollback Procedure

#### Step 1: Identify the Issue
- Determine what is failing
- Identify which component is causing the issue
- Assess the severity

#### Step 2: Attempt Hotfix
- Try to fix the issue without rollback
- Test the fix thoroughly
- Deploy if successful

#### Step 3: Execute Rollback

**Option A: Git Rollback (Recommended)**
```bash
# Rollback to previous version
git checkout v1.x.x  # Previous stable version

# Or revert specific commits
git revert HEAD~5..HEAD  # Revert last 5 commits

# Build and deploy
mvn clean install
mvn deploy
```

**Option B: Configuration Rollback**
If feature flags are still in place:
```java
// Change feature flag back to false
private static final boolean USE_NEW_XML = false;

// Rebuild and redeploy
mvn clean install
```

**Option C: Dependency Rollback**
Revert to using only agg-core without agg-xml:
```xml
<!-- In agg-core/pom.xml -->
<!-- Remove agg-xml dependency -->

<!-- In application -->
<!-- Use only XMLHelper directly -->
```

---

### Rollback Testing

**After rollback, verify:**
- [ ] Original functionality restored
- [ ] No data loss
- [ ] All tests passing
- [ ] All files can be loaded
- [ ] All files can be saved

---

### Rollback Documentation

**Create `docs/refactoring/ROLLBACK_PLAN.md`:**
```markdown
# Rollback Plan for XML Serialization Extraction

## Overview

This document describes the rollback procedure in case of issues with the XML serialization extraction.

## Rollback Triggers

| Severity | Issue | Action |
|----------|-------|--------|
| Critical | Data loss or corruption | Immediate rollback |
| Critical | Security vulnerability | Immediate rollback |
| High | Performance degradation > 200% | Rollback within 24h |
| High | Incompatibility with existing files | Rollback within 24h |
| Medium | Non-critical bugs | Hotfix or rollback within 48h |
| Low | Minor issues | Hotfix, no rollback |

## Rollback Methods

### Method 1: Git Rollback (Fastest)

**Steps:**
1. Identify the last known good commit
2. Checkout that commit or tag
3. Build and deploy
4. Verify functionality

**Commands:**
```bash
# Find last good commit
git log --oneline | head -20

# Checkout last good tag
git checkout v1.x.x

# Or revert specific commits
git revert NEW_COMMIT_1 NEW_COMMIT_2...

# Build
mvn clean install

# Deploy
mvn deploy
```

**Time:** 5-10 minutes

---

### Method 2: Feature Flag Rollback

**If feature flags are still in place:**

1. Change feature flags to use legacy code
2. Rebuild
3. Redeploy
4. Verify

**Example:**
```java
// In GraGraSave.java
private static final boolean USE_NEW_XML = false;  // Changed from true

// In GraGraLoad.java
private static final boolean USE_NEW_XML = false;  // Changed from true
```

**Time:** 2-5 minutes

**Note:** This only works if Phase 8 (Cleanup) has not been completed yet.

---

### Method 3: Dependency Rollback

**If agg-xml dependency has been added:**

1. Remove agg-xml dependency from agg-core
2. Revert all changes to Save/Load classes
3. Rebuild
4. Redeploy

**Steps:**
```bash
# Edit agg-core/pom.xml
# Remove:
# <dependency>
#     <groupId>agg</groupId>
#     <artifactId>agg-xml</artifactId>
#     <version>2.0.0</version>
# </dependency>

# Revert Save/Load classes to use XMLHelper directly
# (Use git to restore old versions)

git checkout HEAD~1 -- agg/gui/saveload/GraGraSave.java
Git checkout HEAD~1 -- agg/gui/saveload/GraGraLoad.java
# etc.

# Build
mvn clean install

# Deploy
mvn deploy
```

**Time:** 15-30 minutes

---

## Rollback Verification

After rollback, run the following tests:

1. **Baseline Tests**
   ```bash
   mvn test -Dtest=TestBaseline
   ```

2. **Integration Tests**
   ```bash
   mvn test -Dtest=*IT
   ```

3. **Manual Verification**
   - Load existing .ggx files
   - Save and reload test files
   - Verify GUI functionality

---

## Rollback Communication

### Internal Communication

1. Notify development team
2. Notify QA team
3. Update issue tracker
4. Update status page (if applicable)

### External Communication (if applicable)

1. Notify users if public release
2. Provide instructions if needed
3. Estimate time for fix

---

## Rollback Timeline

| Time | Action |
|------|--------|
| 0 min | Identify issue |
| 5 min | Attempt hotfix |
| 15 min | Decide on rollback |
| 20 min | Execute rollback |
| 30 min | Verify rollback |
| 60 min | Notify stakeholders |
| 2h | Investigate root cause |

---

## Rollback Contacts

| Role | Name | Contact |
|------|------|---------|
| Project Lead | [Name] | [Email/Phone] |
| Developer | [Name] | [Email/Phone] |
| QA Lead | [Name] | [Email/Phone] |
| Operations | [Name] | [Email/Phone] |

---

## Rollback History

| Date | Version | Issue | Rollback Method | Resolution |
|------|---------|-------|-----------------|------------|
| - | - | - | - | - |
```

---

---

## 📚 Appendices

---

### Appendix A: Glossary

| Term | Definition |
|------|------------|
| XMLObject | Legacy interface for XML serialization in AGG |
| XMLHelper | Legacy class for XML processing in AGG |
| XMLSerializable | New interface for XML serialization |
| XMLSerializer | New interface for XML serialization operations |
| XMLDeserializer | New interface for XML deserialization operations |
| Adapter Pattern | Design pattern to bridge between incompatible interfaces |
| DOM | Document Object Model - W3C standard for XML processing |
| SAX | Simple API for XML - Event-based XML parsing |
| Xerces | Apache Xerces - Java XML parser implementation |
| .ggx | AGG XML file format extension |

---

### Appendix B: File Naming Conventions

**Java Files:**
- Interface: `PascalCase.java` (e.g., `XMLSerializable.java`)
- Class: `PascalCase.java` (e.g., `GraphAdapter.java`)
- Test: `PascalCaseTest.java` (e.g., `GraphAdapterTest.java`)
- Exception: `PascalCaseException.java` (e.g., `XMLSerializationException.java`)

**Packages:**
- All lowercase (e.g., `agg.xml.core`)
- No underscores

**Directories:**
- All lowercase
- Hyphens for multi-word names (e.g., `agg-xml`)

---

### Appendix C: Code Formatting

**Use project's existing conventions:**
- Indentation: 4 spaces (or tabs if existing code uses tabs)
- Brace style: K&R style (opening brace on same line)
- Line length: 120 characters max
- Imports: Grouped by package, alphabetical within groups

**Example:**
```java
package agg.xml.adapter;

import agg.xt_basis.Graph;
import agg.xml.core.XMLSerializable;

import java.util.List;

public class GraphAdapter implements XMLSerializable {
    private final Graph graph;
    
    public GraphAdapter(Graph graph) {
        this.graph = graph;
    }
    
    @Override
    public void serialize(XMLSerializerContext context) {
        // Implementation
    }
}
```

---

### Appendix D: Useful Commands

**Build:**
```bash
mvn clean install
mvn install -pl agg-xml
mvn install -pl agg-xml -am
```

**Test:**
```bash
mvn test
mvn test -pl agg-xml
mvn test -Dtest=GraphAdapterTest
mvn test -Dtest=*MigrationTest
```

**Find Classes:**
```bash
# Find all XMLObject implementations
find . -name "*.java" -exec grep -l "implements XMLObject" {} \;

# Find all XMLHelper usages
find . -name "*.java" -exec grep -l "XMLHelper" {} \;

# Find all save_to_xml calls
grep -r "save_to_xml" --include="*.java" .
```

**Code Analysis:**
```bash
# Count lines of code
find . -name "*.java" -exec wc -l {} \; | awk '{sum+=$1} END {print sum}'

# Find largest files
find . -name "*.java" -exec wc -l {} \; | sort -nr | head -20
```

---

### Appendix E: Troubleshooting

**Problem: ClassNotFoundException**
```
Solution: Check Maven dependencies
- Verify agg-xml is in agg-core's dependencies
- Verify all transitive dependencies are available
- Run mvn dependency:tree to check
```

**Problem: NoSuchMethodError**
```
Solution: Version mismatch
- Check that agg-xml version matches agg-core version
- Clean and rebuild: mvn clean install
- Check for multiple versions in classpath
```

**Problem: XML serialization produces invalid XML**
```
Solution: Validate XML output
- Use xmllint to validate: xmllint output.ggx
- Check for special characters
- Verify encoding
- Check DOM tree structure
```

**Problem: Performance degradation**
```
Solution: Profile and optimize
- Use VisualVM or JProfiler
- Check for N+1 queries in loops
- Optimize DOM tree creation
- Use caching where appropriate
```

---

### Appendix F: References

**Design Patterns:**
- Adapter Pattern: https://en.wikipedia.org/wiki/Adapter_pattern
- Strategy Pattern: https://en.wikipedia.org/wiki/Strategy_pattern
- Factory Pattern: https://en.wikipedia.org/wiki/Factory_method_pattern

**Java XML:**
- JAXP: https://docs.oracle.com/javase/tutorial/jaxp/
- W3C DOM: https://www.w3.org/DOM/
- SAX: https://www.saxproject.org/
- Apache Xerces: https://xerces.apache.org/

**Maven:**
- Maven Documentation: https://maven.apache.org/guides/
- Maven POM Reference: https://maven.apache.org/pom.html

---

---

## 🔗 Navigation

- **Part 1**: [REFACTORING_PLAN_XML_EXTRACTION_PART1.md](REFACTORING_PLAN_XML_EXTRACTION_PART1.md) - Phases 1-2
- **Part 2**: [REFACTORING_PLAN_PART2_CORE_AND_ADAPTERS.md](REFACTORING_PLAN_PART2_CORE_AND_ADAPTERS.md) - Phases 3-5

---

*Last Updated: 2026-09-07*
*Version: 1.0*
