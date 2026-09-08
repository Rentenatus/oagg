# 📁 AGG XML Extraction - Part 2: Core Infrastructure & Adapters


## 🎯 Part 2 Overview
**Covers:** Phase 3 (Core Infrastructure) + Phase 4 (Adapter Layer)  
**Duration:** Week 5-12 (8 weeks)  
**Objective:** Build new XML module and create bridge to legacy code

---

## 3️⃣ Phase 3: Core Infrastructure (Week 5-7)

### 🎯 Goal
Implement foundational classes for new XML serialization module.

### 📦 Module Structure
```
agg-xml/
├── core/
│   ├── XMLSerializable.java (interface)
│   ├── XMLSerializer.java (interface)
│   ├── XMLDeserializer.java (interface)
│   ├── XMLSerializerContext.java (interface)
│   ├── XMLDeserializerContext.java (interface)
│   └── XMLSerializationException.java
├── mapper/
│   ├── ReferenceResolver.java
│   └── TypeRegistry.java
├── util/
│   └── XMLUtils.java
└── exception/
    └── XMLSerializationException.java
```

---

### 📌 Step 3.1: Create Core Interfaces

#### XMLSerializable.java
```java
package agg.xml.core;
public interface XMLSerializable {
    void serialize(XMLSerializerContext context) throws XMLSerializationException;
    void deserialize(XMLDeserializerContext context) throws XMLSerializationException;
}
```

#### XMLSerializer.java
```java
package agg.xml.core;
public interface XMLSerializer {
    void serialize(Object object, OutputStream output) throws XMLSerializationException;
    void serialize(Object object, File file) throws XMLSerializationException;
    String serializeToString(Object object) throws XMLSerializationException;
    void serialize(Object object, Writer writer) throws XMLSerializationException;
}
```

#### XMLDeserializer.java
```java
package agg.xml.core;
public interface XMLDeserializer {
    <T> T deserialize(InputStream input, Class<T> type) throws XMLSerializationException;
    <T> T deserialize(File file, Class<T> type) throws XMLSerializationException;
    <T> T deserializeFromString(String xml, Class<T> type) throws XMLSerializationException;
    <T> T deserialize(Reader reader, Class<T> type) throws XMLSerializationException;
}
```

---

### 📌 Step 3.2: Create Context Interfaces

#### XMLSerializerContext.java
Provides methods for writing XML during serialization:
```java
void beginObject(String name)
void beginObject(String name, String type)
void endObject()
void writeAttribute(String name, String/int/long/double/boolean value)
void writeText(String/int/long/double/boolean value)
void beginArray(String name)
void beginArray(String name, int size)
void endArray()
void writeReference(String name, String targetId, Class<?> targetType)
void writeObject(String name, Object object)
void writeObject(Object object)
ReferenceResolver getReferenceResolver()
```

#### XMLDeserializerContext.java
Provides methods for reading XML during deserialization:
```java
boolean nextChild()
String getCurrentChildName()
boolean isCurrentChild(String name)
boolean isCurrentChild(String... names)
String readAttribute(String name)
int/long/double/boolean readInt/Long/Double/BooleanAttribute(String name, T defaultValue)
String readText()
int/long/double/boolean readInt/Long/Double/Boolean(T defaultValue)
<T> List<T> readArray(String elementName, Class<T> elementType)
void readArray(String elementName, Consumer<XMLDeserializerContext> consumer)
<T> String readReference(String elementName, Class<T> targetType)
<T> T readObject(Class<T> type)
ReferenceResolver getReferenceResolver()
boolean hasNextSibling()
boolean nextSibling()
```

---

### 📌 Step 3.3: Create Mapper Classes

#### ReferenceResolver.java
Manages object references to handle circular dependencies:
```java
public class ReferenceResolver {
    public String register(Object object)  // Auto-generate ID
    public void register(Object object, String id)  // Use specific ID
    public String getId(Object object)
    public Object getObject(String id)
    public <T> T getObject(String id, Class<T> type)
    public boolean isRegistered(Object/String)
    public void clear()
    public int size()
}
```

#### TypeRegistry.java
Maps between XML type names and Java classes:
```java
public class TypeRegistry {
    public void registerType(Class<?> type, String typeName)
    public void registerType(Class<?> type)  // Uses simple class name
    public Class<?> getClassForName(String typeName)
    public String getTypeNameForClass(Class<?> type)
    public boolean isTypeRegistered(String typeName)
    public boolean isClassRegistered(Class<?> type)
    public Object createInstance(String typeName)
    public <T> T createInstance(String typeName, Class<T> expectedType)
    public Object deserializeInstance(String typeName, XMLDeserializerContext context)
    public void clear()
    public int size()
}
```

**Pre-registration**: Register all known AGG types in constructor

---

### 📌 Step 3.4: Create Utility Class

#### XMLUtils.java
```java
// Document creation
Document createDocument()
Element createElement(Document doc, String name)
Element createElementWithText(Document doc, String name, String text)

// Content reading
String getTextContent(Element element)

// Attribute reading
String getAttribute(Element element, String name)
int/long/double/boolean getInt/Long/Double/BooleanAttribute(Element, String, T defaultValue)

// Child element access
List<Element> getChildElements(Element element, String childName)
Element getFirstChildElement(Element element, String childName)
```

---

### 📌 Step 3.5: Create Context Implementations

#### DefaultXMLSerializerContext.java
- Uses **stack-based approach** for element tracking
- Maintains current element and element stack
- Integrates with ReferenceResolver
- Implements all XMLSerializerContext methods

**Key Methods:**
```java
public void beginObject(String name) {
    Element element = XMLUtils.createElement(document, name);
    currentElement.appendChild(element);
    elementStack.push(element);
    currentElement = element;
}

public void writeAttribute(String name, String value) {
    if (currentElement != null && value != null) {
        currentElement.setAttribute(name, value);
    }
}

public void writeObject(String name, Object object) {
    if (object instanceof XMLSerializable) {
        beginObject(name);
        ((XMLSerializable) object).serialize(this);
        endObject();
    }
}
```

#### DefaultXMLDeserializerContext.java
- Uses **cursor-based navigation** through child elements
- Maintains current position in XML tree
- Integrates with ReferenceResolver
- Implements all XMLDeserializerContext methods

**Key Methods:**
```java
public boolean nextChild() {
    if (currentChildIndex >= currentChildren.size()) return false;
    currentElement = currentChildren.get(currentChildIndex++);
    loadCurrentChildren();
    return true;
}

public <T> T readObject(Class<T> type) {
    T instance = type.getDeclaredConstructor().newInstance();
    if (instance instanceof XMLSerializable) {
        ((XMLSerializable) instance).deserialize(this);
    }
    return instance;
}
```

---

### 📌 Step 3.6: Create Serializer/Deserializer Implementations

#### DOMXMLSerializer.java
Uses Apache Xerces DOM for serialization:
```java
public class DOMXMLSerializer implements XMLSerializer {
    public void serialize(Object object, File file) {
        Document doc = XMLUtils.createDocument();
        XMLSerializerContext context = new DefaultXMLSerializerContext(doc);
        context.writeObject(object);
        writeDocument(doc, file);
    }
    
    private void writeDocument(Document doc, File file) {
        OutputFormat format = new OutputFormat(doc, "UTF-8", true);
        XMLSerializer serializer = new XMLSerializer(new FileOutputStream(file), format);
        serializer.serialize(doc);
    }
}
```

#### DOMXMLDeserializer.java
Uses Apache Xerces DOM for deserialization:
```java
public class DOMXMLDeserializer implements XMLDeserializer {
    public <T> T deserialize(File file, Class<T> type) {
        Document doc = parseDocument(file);
        Element root = doc.getDocumentElement();
        XMLDeserializerContext context = new DefaultXMLDeserializerContext(root);
        return deserializeRoot(root, type, context);
    }
    
    private Document parseDocument(File file) {
        DOMParser parser = new DOMParser();
        parser.parse(new InputSource(new FileInputStream(file)));
        return parser.getDocument();
    }
}
```

---

### 📌 Step 3.7: Create Factory Classes

#### XMLSerializerFactory.java
```java
public final class XMLSerializerFactory {
    public static XMLSerializer createSerializer() {
        return new DOMXMLSerializer();
    }
    public static XMLSerializer createSerializer(String encoding, boolean indent, String indentString) {
        return new DOMXMLSerializer(encoding, indent, indentString);
    }
}
```

#### XMLDeserializerFactory.java
```java
public final class XMLDeserializerFactory {
    public static XMLDeserializer createDeserializer() {
        return new DOMXMLDeserializer();
    }
    public static XMLDeserializer createDeserializer(TypeRegistry typeRegistry) {
        return new DOMXMLDeserializer(typeRegistry);
    }
}
```

---

### 📌 Step 3.8: Test Core Infrastructure

Create comprehensive unit and integration tests:

**Test Classes:**
- ReferenceResolverTest.java
- TypeRegistryTest.java
- XMLUtilsTest.java
- DefaultXMLSerializerContextTest.java
- DefaultXMLDeserializerContextTest.java
- DOMXMLSerializerTest.java
- DOMXMLDeserializerTest.java
- CoreInfrastructureIntegrationTest.java

**Test Scenarios:**
- Basic serialization/deserialization
- Circular reference handling
- Type registration and lookup
- Array serialization
- Reference serialization
- Roundtrip tests (serialize → deserialize → compare)
- Performance tests
- Error handling tests

**Verification:**
```bash
cd /d/git_oagg
mvn test -pl agg-xml
```

---

## 4️⃣ Phase 4: Adapter Layer Implementation (Week 8-12)

### 🎯 Goal
Create bridge between legacy XMLObject interface and new XMLSerializable interface.

**Key Challenge**: Domain classes cannot be modified, so we need adapters.

---

### 📌 Step 4.1: Understand the Problem

**Legacy Code (Cannot Change):**
```java
class Graph implements XMLObject {
    public void XwriteObject(XMLHelper h) {
        h.openObject(this);
        h.addAttr("name", this.name);
        // ... more legacy code
    }
    public void XreadObject(XMLHelper h) {
        this.name = h.readAttr("name");
        // ... more legacy code
    }
}
```

**Desired New Code:**
```java
class NewCode {
    void save(Graph graph) {
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        serializer.serialize(graph); // Graph doesn't implement XMLSerializable!
    }
}
```

**Solution - Adapter Pattern:**
```java
class GraphAdapter implements XMLSerializable {
    private final Graph legacyGraph;
    
    public GraphAdapter(Graph graph) {
        this.legacyGraph = graph;
    }
    
    @Override
    public void serialize(XMLSerializerContext context) {
        // Call legacy method and extract result
    }
    
    @Override
    public void deserialize(XMLDeserializerContext context) {
        // Build XMLHelper state and call legacy method
    }
}
```

---

### 📌 Step 4.2: Create Adapter Factory

#### AdapterFactory.java
```java
public class AdapterFactory {
    private static final AdapterFactory INSTANCE = new AdapterFactory();
    private final Map<Class<?>, Function<Object, XMLSerializable>> creators = new HashMap<>();
    
    private AdapterFactory() {
        // Register built-in adapters
        register(Graph.class, obj -> new GraphAdapter((Graph) obj));
        register(Rule.class, obj -> new RuleAdapter((Rule) obj));
        register(Node.class, obj -> new NodeAdapter((Node) obj));
        register(Arc.class, obj -> new ArcAdapter((Arc) obj));
        register(GraGra.class, obj -> new GraGraAdapter((GraGra) obj));
        // ... register all other types
    }
    
    public static AdapterFactory getInstance() { return INSTANCE; }
    
    public void register(Class<?> type, Function<Object, XMLSerializable> creator) {
        creators.put(type, creator);
    }
    
    public XMLSerializable createAdapter(Object object) {
        // Find matching creator (exact type, superclass, interface)
        // Fall back to generic adapter if XMLObject
        // Return null if no adapter available
    }
    
    public XMLSerializable createAdapter(Object object, ReferenceResolver resolver) {
        // Create adapter with reference support
    }
}
```

---

### 📌 Step 4.3: Create Specific Adapters

#### GraphAdapter.java
```java
public class GraphAdapter implements XMLSerializable {
    private final Graph graph;
    private final ReferenceResolver referenceResolver;
    
    public GraphAdapter(Graph graph) { this(graph, null); }
    public GraphAdapter(Graph graph, ReferenceResolver resolver) {
        this.graph = graph;
        this.referenceResolver = resolver;
    }
    
    @Override
    public void serialize(XMLSerializerContext context) {
        String id = referenceResolver != null ? referenceResolver.register(graph) : null;
        
        context.beginObject("Graph");
        if (id != null) context.writeAttribute("id", id);
        context.writeAttribute("name", graph.getName());
        
        // Serialize types
        context.beginObject("Types");
        // ... serialize type set
        context.endObject();
        
        // Serialize nodes
        context.beginArray("Nodes", graph.getNodes().size());
        for (Node node : graph.getNodes()) {
            context.writeObject(new NodeAdapter(node, referenceResolver));
        }
        context.endArray();
        
        // Serialize arcs
        context.beginArray("Arcs", graph.getArcs().size());
        for (Arc arc : graph.getArcs()) {
            context.writeObject(new ArcAdapter(arc, referenceResolver));
        }
        context.endArray();
        
        context.endObject();
    }
    
    @Override
    public void deserialize(XMLDeserializerContext context) {
        String id = context.readAttribute("id");
        if (id != null && referenceResolver != null) {
            referenceResolver.register(graph, id);
        }
        graph.setName(context.readAttribute("name"));
        
        // Deserialize types
        if (context.nextChild() && context.isCurrentChild("Types")) {
            // ... deserialize type set
        }
        
        // Deserialize nodes
        if (context.nextChild() && context.isCurrentChild("Nodes")) {
            int size = context.readIntAttribute("size", 0);
            for (int i = 0; i < size; i++) {
                if (context.nextChild() && context.isCurrentChild("Node")) {
                    Node node = new NodeAdapter().deserialize(context);
                    graph.addNode(node);
                }
            }
        }
        
        // Deserialize arcs
        if (context.nextChild() && context.isCurrentChild("Arcs")) {
            int size = context.readIntAttribute("size", 0);
            for (int i = 0; i < size; i++) {
                if (context.nextChild() && context.isCurrentChild("Arc")) {
                    Arc arc = new ArcAdapter().deserialize(context);
                    graph.addArc(arc);
                }
            }
        }
    }
}
```

#### NodeAdapter.java, ArcAdapter.java, RuleAdapter.java, etc.
- Similar pattern for each domain class
- Handle class-specific attributes and children
- Use reference resolver for circular references

---

### 📌 Step 4.4: Create Legacy Compatibility Layer

#### LegacyCompatibility.java
```java
public final class LegacyCompatibility {
    private LegacyCompatibility() {}
    
    public static void serializeToFile(XMLObject xmlObject, File file) throws XMLSerializationException {
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(xmlObject);
        if (!helper.save_to_xml(file.getAbsolutePath())) {
            throw new XMLSerializationException("Failed to serialize to " + file);
        }
    }
    
    public static <T extends XMLObject> T deserializeFromFile(File file, Class<T> type) 
            throws XMLSerializationException {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            XMLHelper helper = new XMLHelper();
            if (!helper.read_from_xml(file.getAbsolutePath())) {
                throw new XMLSerializationException("Failed to read from " + file);
            }
            helper.getTopObject(instance);
            return instance;
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to deserialize from " + file, e);
        }
    }
    
    public static String serializeToString(XMLObject xmlObject) throws XMLSerializationException {
        // Use temp file approach
    }
}
```

---

### 📌 Step 4.5: Create High-Level Save/Load API

#### XMLSaveLoad.java
```java
public final class XMLSaveLoad {
    private XMLSaveLoad() {}
    
    public static void saveObject(Object object, File file) throws XMLSerializationException {
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        serializer.serialize(object, file);
    }
    
    public static <T> T loadObject(File file, Class<T> type) throws XMLSerializationException {
        XMLDeserializer deserializer = XMLDeserializerFactory.createDeserializer();
        return deserializer.deserialize(file, type);
    }
    
    public static void saveObject(Object object, File file, String encoding, boolean indent) 
            throws XMLSerializationException {
        XMLSerializer serializer = XMLSerializerFactory.createSerializer(encoding, indent, "  ");
        serializer.serialize(object, file);
    }
}
```

---

### 📌 Step 4.6: Test Adapter Layer

**Test Strategy:**
1. Test each specific adapter (GraphAdapter, NodeAdapter, etc.)
2. Test adapter factory
3. Test legacy compatibility
4. Test roundtrip serialization
5. Test with existing .ggx files

**Test Classes:**
- GraphAdapterTest.java
- NodeAdapterTest.java
- ArcAdapterTest.java
- RuleAdapterTest.java
- AdapterFactoryTest.java
- LegacyCompatibilityTest.java
- AdapterLayerIntegrationTest.java

**Test Scenarios:**
- Simple object serialization
- Complex object with nested children
- Circular reference handling
- Roundtrip tests with existing files
- Performance comparison with legacy
- Error handling

**Verification:**
```bash
mvn test -pl agg-xml -Dtest=AdapterLayerTest
```

---

## 5️⃣ Phase 5: Migration of Save/Load Classes (Week 13-15)

### 🎯 Goal
Migrate application-level Save/Load classes to use new XML module.

---

### 📌 Step 5.1: Prioritize Classes

**High Priority (Week 13):**
- GraGraSave.java
- GraGraLoad.java

**Medium Priority (Week 14):**
- EdGraGra.saveToXML()
- ApplRuleSequenceSaveLoad.java

**Low Priority (Week 15):**
- ConflictsDependenciesContainerSaveLoad.java
- AGGBasicAppl.java
- ComputeCriticalPairs.java

---

### 📌 Step 5.2: Migration Strategy

**Use Feature Flags for Safe Transition:**
```java
// In each Save/Load class
private static final boolean USE_NEW_XML = false; // Start with false

public boolean save() {
    if (USE_NEW_XML) {
        return saveUsingNewXML();
    } else {
        return saveUsingLegacyXML();
    }
}
```

**Implementation Pattern:**
1. Keep original method (rename to saveUsingLegacyXML)
2. Create new method (saveUsingNewXML)
3. Add feature flag to switch between them
4. Test both implementations
5. Switch flag to true when ready
6. Remove legacy code after verification

---

### 📌 Step 5.3: Migrate GraGraSave

**Original Code:**
```java
XMLHelper xmlh = new XMLHelper();
xmlh.addTopObject(this.gra);
if (xmlh.save_to_xml(this.dirName + this.fileName)) {
    // success
}
```

**New Code:**
```java
File outputFile = new File(this.dirName + this.fileName);
XMLSaveLoad.saveObject(this.gra, outputFile);
```

**With Feature Flag:**
```java
private static final boolean USE_NEW_XML = true;

private boolean saveUsingNewXML() {
    try {
        File outputFile = new File(this.dirName + this.fileName);
        XMLSaveLoad.saveObject(this.gra, outputFile);
        this.gra.setDirName(this.dirName);
        this.gra.setFileName(this.fileName);
        this.gra.getTypeSet().setResourcesPath(this.dirName);
        this.gra.setChanged(false);
        fireSave(new SaveEvent(this, SaveEvent.SAVED, outputFile.getAbsolutePath()));
        return true;
    } catch (XMLSerializationException e) {
        fireSave(new SaveEvent(this, SaveEvent.IO_ERROR, "Write file Error!",
                this.dirName + this.fileName));
        return false;
    }
}

private boolean saveUsingLegacyXML() {
    XMLHelper xmlh = new XMLHelper();
    xmlh.addTopObject(this.gra);
    if (xmlh.save_to_xml(this.dirName + this.fileName)) {
        this.gra.setDirName(this.dirName);
        this.gra.setFileName(this.fileName);
        this.gra.getTypeSet().setResourcesPath(this.dirName);
        this.gra.setChanged(false);
        fireSave(new SaveEvent(this, SaveEvent.SAVED, this.dirName + this.fileName));
        return true;
    } else {
        fireSave(new SaveEvent(this, SaveEvent.IO_ERROR, "Write file Error!",
                this.dirName + this.fileName));
        return false;
    }
}
```

---

### 📌 Step 5.4: Migrate GraGraLoad

**Similar pattern to GraGraSave:**
```java
private void loadUsingNewXML(File file) {
    try {
        GraGra graGra = XMLSaveLoad.loadObject(file, GraGra.class);
        this.gra = new EdGraGra(graGra);
        // ... rest of initialization
    } catch (XMLSerializationException e) {
        // Error handling
    }
}
```

---

### 📌 Step 5.5: Migrate EdGraGra.saveToXML

**Using Adapter Approach:**
```java
private boolean saveToXMLNew(String fname) {
    try {
        File outputFile = new File(this.dirName + File.separator + fname);
        XMLObjectToSerializableAdapter adapter = new XMLObjectToSerializableAdapter(this);
        XMLSerializer serializer = XMLSerializerFactory.createSerializer();
        serializer.serialize(adapter, outputFile);
        this.isChanged = false;
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

---

### 📌 Step 5.6: Update Dependencies

**agg-core/pom.xml:**
```xml
<dependency>
    <groupId>agg</groupId>
    <artifactId>agg-xml</artifactId>
    <version>${project.version}</version>
</dependency>
```

**This allows agg-core to use agg-xml classes.**

---

### 📌 Step 5.7: Test Migrated Classes

**Create Integration Tests:**
- Test roundtrip with new XML
- Test backward compatibility with old .ggx files
- Test feature flag switching
- Test error handling
- Test performance

**Test Classes:**
- GraGraSaveMigrationTest.java
- GraGraLoadMigrationTest.java
- EdGraGraMigrationTest.java
- SaveLoadIntegrationTest.java

**Verification Commands:**
```bash
# Run all migration tests
mvn test -Dtest=*MigrationTest

# Run specific test
mvn test -Dtest=GraGraSaveMigrationTest
```

---

## 📊 Phase 4-5 Checklists

### Phase 4 Complete Checklist
- [ ] AdapterFactory implemented
- [ ] GraphAdapter implemented
- [ ] NodeAdapter implemented
- [ ] ArcAdapter implemented
- [ ] RuleAdapter implemented
- [ ] GraGraAdapter implemented
- [ ] All other critical adapters implemented
- [ ] LegacyCompatibility implemented
- [ ] XMLSaveLoad API implemented
- [ ] All adapter tests passing
- [ ] All roundtrip tests passing
- [ ] Performance acceptable

### Phase 5 Complete Checklist
- [ ] GraGraSave migrated with feature flag
- [ ] GraGraLoad migrated with feature flag
- [ ] EdGraGra.saveToXML migrated
- [ ] ApplRuleSequenceSaveLoad migrated
- [ ] Other Save/Load classes migrated
- [ ] Dependencies updated
- [ ] Feature flags working
- [ ] Integration tests passing
- [ ] Backward compatibility verified

---

## 🔗 Navigation
- **Part 1**: [REFACTORING_PLAN_XML_EXTRACTION_PART1.md](REFACTORING_PLAN_XML_EXTRACTION_PART1.md) - Phases 1-2
- **Part 3**: [REFACTORING_PLAN_PART3_DOMAIN_AND_FINAL.md](REFACTORING_PART3_DOMAIN_AND_FINAL.md) - Phases 6-9
