/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.mapper;

import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps between XML type names and Java classes for polymorphic deserialization.
 *
 * <p>When deserializing XML, the type name in the XML element is used to look up
 * the corresponding Java class, and a new instance is created. This registry
 * pre-registers all known AGG domain types so that deserialization can create
 * the correct concrete types.</p>
 */
public class TypeRegistry {

    private final Map<String, Class<?>> nameToClass;
    private final Map<Class<?>, String> classToName;

    /**
     * Creates a new, empty type registry.
     */
    public TypeRegistry() {
        this.nameToClass = new HashMap<>();
        this.classToName = new HashMap<>();
    }

    /**
     * Creates a new type registry and pre-registers known AGG domain types.
     *
     * @param preRegister true to pre-register AGG domain types
     */
    public TypeRegistry(boolean preRegister) {
        this();
        if (preRegister) {
            preRegisterAggTypes();
        }
    }

    /**
     * Registers a type with a specific XML type name.
     *
     * @param type     The Java class
     * @param typeName The XML type name
     */
    public void registerType(Class<?> type, String typeName) {
        if (type == null || typeName == null || typeName.isEmpty()) {
            throw new IllegalArgumentException("Type and typeName must not be null or empty");
        }
        nameToClass.put(typeName, type);
        classToName.put(type, typeName);
    }

    /**
     * Registers a type using its simple class name as the XML type name.
     *
     * @param type The Java class
     */
    public void registerType(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        registerType(type, type.getSimpleName());
    }

    /**
     * Gets the Java class for the specified XML type name.
     *
     * @param typeName The XML type name
     * @return The Java class, or null if not registered
     */
    public Class<?> getClassForName(String typeName) {
        if (typeName == null) {
            return null;
        }
        return nameToClass.get(typeName);
    }

    /**
     * Gets the XML type name for the specified Java class.
     *
     * @param type The Java class
     * @return The XML type name, or null if not registered
     */
    public String getTypeNameForClass(Class<?> type) {
        if (type == null) {
            return null;
        }
        return classToName.get(type);
    }

    /**
     * Checks if a type name is registered.
     *
     * @param typeName The XML type name
     * @return true if registered, false otherwise
     */
    public boolean isTypeRegistered(String typeName) {
        return typeName != null && nameToClass.containsKey(typeName);
    }

    /**
     * Checks if a class is registered.
     *
     * @param type The Java class
     * @return true if registered, false otherwise
     */
    public boolean isClassRegistered(Class<?> type) {
        return type != null && classToName.containsKey(type);
    }

    /**
     * Creates a new instance of the type identified by the specified type name.
     *
     * @param typeName The XML type name
     * @return A new instance, or null if the type is not registered
     * @throws XMLSerializationException if instantiation fails
     */
    public Object createInstance(String typeName) throws XMLSerializationException {
        Class<?> type = getClassForName(typeName);
        if (type == null) {
            return null;
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new XMLSerializationException(
                "Failed to create instance of type: " + typeName, e);
        }
    }

    /**
     * Creates a new instance of the type identified by the specified type name,
     * cast to the expected type.
     *
     * @param <T>          The expected type
     * @param typeName     The XML type name
     * @param expectedType The expected class
     * @return A new instance cast to the expected type, or null if not registered
     * @throws XMLSerializationException if instantiation fails
     * @throws ClassCastException if the created instance is not of the expected type
     */
    public <T> T createInstance(String typeName, Class<T> expectedType) throws XMLSerializationException {
        Object instance = createInstance(typeName);
        if (instance == null) {
            return null;
        }
        return expectedType.cast(instance);
    }

    /**
     * Creates a new instance of the type identified by the specified type name
     * and deserializes it using the provided deserializer context.
     *
     * @param typeName The XML type name
     * @param context   The deserializer context
     * @return The deserialized object, or null if the type is not registered
     * @throws XMLSerializationException if instantiation or deserialization fails
     */
    public Object deserializeInstance(String typeName, XMLDeserializerContext context)
            throws XMLSerializationException {
        Object instance = createInstance(typeName);
        if (instance == null) {
            return null;
        }
        if (instance instanceof agg.xml.core.XMLSerializable) {
            ((agg.xml.core.XMLSerializable) instance).deserialize(context);
        }
        return instance;
    }

    /**
     * Clears all registered types.
     */
    public void clear() {
        nameToClass.clear();
        classToName.clear();
    }

    /**
     * Returns the number of registered types.
     *
     * @return The number of registered types
     */
    public int size() {
        return nameToClass.size();
    }

    /**
     * Pre-registers known AGG domain types.
     * This method uses reflection to load the classes lazily, so missing classes
     * are silently skipped rather than causing a compilation failure.
     */
    private void preRegisterAggTypes() {
        String[] aggTypes = {
            "agg.xt_basis.Graph",
            "agg.xt_basis.Rule",
            "agg.xt_basis.Node",
            "agg.xt_basis.Arc",
            "agg.xt_basis.GraGra",
            "agg.xt_basis.TypeGraph",
            "agg.xt_basis.Match",
            "agg.xt_basis.TypeImpl",
            "agg.xt_basis.NodeTypeImpl",
            "agg.xt_basis.ArcTypeImpl",
            "agg.xt_basis.OrdinaryMorphism",
            "agg.xt_basis.NestedApplCond"
        };
        for (String className : aggTypes) {
            try {
                Class<?> type = Class.forName(className);
                registerType(type, type.getSimpleName());
            } catch (ClassNotFoundException e) {
                System.err.println("Warning: AGG type not found on classpath, skipping: " + className);
            }
        }
    }
}
