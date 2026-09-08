# AGG XML Serialization Module (agg-xml)

## Overview

This module provides XML serialization and deserialization capabilities for AGG (Attribute Grammar Generator) domain objects using a modern, decoupled architecture based on the Adapter Pattern.

## Architecture

The module follows a clean separation of concerns:

```
agg-xml/
├── core/              # Core interfaces and base implementations
│   ├── XMLSerializable.java          # Main serialization interface
│   ├── XMLSerializer.java            # Serializer interface
│   ├── XMLDeserializer.java          # Deserializer interface
│   ├── XMLSerializerContext.java      # Serialization context
│   ├── XMLDeserializerContext.java    # Deserialization context
│   ├── XMLSerializationException.java # Custom exception
│   ├── AbstractXMLSerializer.java     # Base serializer class
│   ├── DOMXMLSerializerContext.java  # DOM-based serializer context
│   └── DOMXMLDeserializerContext.java # DOM-based deserializer context
│
├── adapter/           # Adapter classes for legacy integration
│   ├── XMLObjectAdapter.java          # Generic XMLObject adapter
│   ├── DomainObjectAdapter.java       # Base domain object adapter
│   ├── GraphAdapter.java              # Graph-specific adapter
│   ├── NodeAdapter.java               # Node-specific adapter
│   ├── ArcAdapter.java                # Arc-specific adapter
│   ├── RuleAdapter.java               # Rule-specific adapter
│   ├── XMLHelperSerializerContext.java # XMLHelper wrapper for serialization
│   ├── XMLHelperDeserializerContext.java # XMLHelper wrapper for deserialization
│   └── XMLAdapterFactory.java          # Factory for creating adapters
│
├── mapper/            # (Planned) Type-specific mappers
├── legacy/            # (Planned) Legacy compatibility layer
├── util/              # (Planned) Utility classes
└── pom.xml            # Maven build configuration
```

## Key Design Principles

1. **Separation of Concerns**: Domain objects know nothing about XML serialization
2. **Dependency Inversion**: Modules depend on abstractions (interfaces), not concretions
3. **Adapter Pattern**: Legacy XMLObject implementations are adapted to the new XMLSerializable interface
4. **Zero Changes to Domain**: Existing Graph, Rule, Node, Arc classes remain unchanged
5. **Gradual Migration**: Step-by-step migration with feature flags

## Core Interfaces

### XMLSerializable

Marker interface for objects that can be serialized to and deserialized from XML:

```java
public interface XMLSerializable {
    void serialize(XMLSerializerContext context) throws XMLSerializationException;
    void deserialize(XMLDeserializerContext context) throws XMLSerializationException;
}
```

### XMLSerializer / XMLDeserializer

Interfaces for serializing/deserializing objects to/from XML.

### XMLSerializerContext / XMLDeserializerContext

Context interfaces that provide services for reading/writing XML elements and attributes.

## Concrete Implementations

### DOMXMLSerializerContext

A DOM-based implementation of XMLSerializerContext that:
- Creates and manages W3C DOM documents
- Provides element and attribute manipulation methods
- Supports nested element structures via a stack
- Can serialize the document to an XML string

### DOMXMLDeserializerContext

A DOM-based implementation of XMLDeserializerContext that:
- Parses XML documents from files or input streams
- Provides navigation methods (moveToFirstChild, moveToNextSibling, moveToParent)
- Provides attribute and text content reading methods
- Supports object deserialization

## Adapter Classes

### XMLObjectAdapter

Generic adapter that wraps any XMLObject implementation:

```java
XMLObject legacyObject = new Graph();
XMLSerializable adapter = new XMLObjectAdapter(legacyObject);
adapter.serialize(context);  // Delegates to legacyObject.XwriteObject()
```

### Domain-Specific Adapters

Specialized adapters for common AGG domain objects:
- GraphAdapter
- NodeAdapter
- ArcAdapter
- RuleAdapter

### XMLAdapterFactory

Factory class for creating appropriate adapters:

```java
XMLObject xmlObject = ...;
XMLSerializable adapter = XMLAdapterFactory.createAdapter(xmlObject);
```

## Usage Example

```java
// Create a serializer context
XMLSerializerContext context = new DOMXMLSerializerContext();

// Create and adapt a domain object
Graph graph = new Graph();
XMLSerializable graphAdapter = new GraphAdapter(graph);

// Serialize
context.pushElement(context.createAndAppendElement("AGG-Graph"));
graphAdapter.serialize(context);

// Get XML string
String xml = context.toXMLString();

// Deserialize
XMLDeserializerContext deserializerContext = 
    new DOMXMLDeserializerContext(new File("graph.ggx"));
graphAdapter.deserialize(deserializerContext);
```

## Build Configuration

The module uses Maven for build management. See `pom.xml` for dependencies:
- Xerces 2.12.2 for XML parsing
- JUnit 4.13.2 for testing
- SLF4J for logging

## Migration Status

### Phase 1 (Completed)
- ✅ Inventory of XMLObject implementations (96 classes)
- ✅ Analysis of .ggx file format (38 elements, 46 attributes)
- ✅ Dependency graph and analysis
- ✅ Test baseline infrastructure
- ✅ Performance benchmarking framework
- ✅ Migration tracker with priority assignments

### Phase 2 (In Progress)
- ✅ XML module structure created
- ✅ Core interfaces defined
- ✅ DOM-based context implementations
- ✅ Adapter classes for legacy integration
- ✅ Basic unit tests
- ⏳ Integration with existing XMLHelper
- ⏳ Serialization/deserialization implementations

### Phase 3 (Planned)
- Mappers for specific domain types
- Legacy compatibility layer
- Performance optimization
- Full integration tests

## Files Created

### Core Module (src_xml/)
- 6 Core interfaces
- 2 Abstract/base classes
- 2 DOM implementations
- 8 Adapter classes
- 1 Factory class
- Maven pom.xml

### Tests
- XMLSerializableTest.java
- DOMXMLContextTest.java

## Known Issues

1. **Missing de.jare.ndimcol classes**: The existing AGG code depends on `de.jare.ndimcol.ref.IteratorWalker` and related classes, which are not available in the classpath. This prevents full compilation of the adapter classes with the existing domain objects.

2. **XMLHelper private methods**: Some methods in XMLHelper (like `pop()`) are private, which limits the integration options for the adapter contexts.

3. **Xerces dependency**: The module requires Xerces 2.12.2 for XML parsing, which must be available in the classpath.

## Next Steps

1. Locate or create stub implementations for de.jare.ndimcol classes
2. Refactor XMLHelper to expose necessary methods for integration
3. Implement concrete serializers/deserializers for domain objects
4. Add integration tests with real .ggx files
5. Gradually migrate domain classes to implement XMLSerializable directly
