/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.mapper;

import agg.xml.core.XMLSerializationException;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

/**
 * Unit tests for {@link TypeRegistry}.
 */
public class TypeRegistryTest {

    private TypeRegistry registry;

    @BeforeMethod
    public void setUp() {
        registry = new TypeRegistry();
    }

    @Test
    public void testRegisterType() {
        registry.registerType(String.class, "string");
        assertTrue(registry.isTypeRegistered("string"), "Type should be registered");
        assertTrue(registry.isClassRegistered(String.class), "Class should be registered");
    }

    @Test
    public void testRegisterTypeWithSimpleName() {
        registry.registerType(Integer.class);
        assertTrue(registry.isTypeRegistered("Integer"), "Should use simple class name");
        assertEquals(registry.getTypeNameForClass(Integer.class), "Integer");
    }

    @Test
    public void testGetClassForName() {
        registry.registerType(String.class, "string");
        Class<?> type = registry.getClassForName("string");
        assertEquals(type, String.class, "Should return String class");
    }

    @Test
    public void testGetClassForUnknownName() {
        assertNull(registry.getClassForName("nonexistent"), "Should return null for unknown");
    }

    @Test
    public void testGetTypeNameForClass() {
        registry.registerType(String.class, "myString");
        assertEquals(registry.getTypeNameForClass(String.class), "myString");
    }

    @Test
    public void testCreateInstance() throws XMLSerializationException {
        registry.registerType(StringBuilder.class, "builder");
        Object instance = registry.createInstance("builder");
        assertNotNull(instance, "Should create instance");
        assertTrue(instance instanceof StringBuilder, "Should be StringBuilder");
    }

    @Test
    public void testCreateInstanceWithExpectedType() throws XMLSerializationException {
        registry.registerType(StringBuilder.class, "builder");
        StringBuilder sb = registry.createInstance("builder", StringBuilder.class);
        assertNotNull(sb, "Should create typed instance");
    }

    @Test
    public void testCreateInstanceUnknownType() throws XMLSerializationException {
        assertNull(registry.createInstance("nonexistent"), "Should return null for unknown");
    }

    @Test(expectedExceptions = XMLSerializationException.class)
    public void testCreateInstanceFailsForUninstantiableType() throws XMLSerializationException {
        registry.registerType(System.class, "system");
        registry.createInstance("system");
    }

    @Test
    public void testClear() {
        registry.registerType(String.class, "string");
        assertEquals(registry.size(), 1, "Should have 1 type");
        registry.clear();
        assertEquals(registry.size(), 0, "Should have 0 types after clear");
    }

    @Test
    public void testPreRegisterAggTypes() {
        TypeRegistry aggRegistry = new TypeRegistry(true);
        // Graph should be on the classpath since agg-core is a dependency
        assertTrue(aggRegistry.isTypeRegistered("Graph"),
            "Graph type should be pre-registered");
        assertTrue(aggRegistry.isTypeRegistered("Rule"),
            "Rule type should be pre-registered");
        assertTrue(aggRegistry.isTypeRegistered("Node"),
            "Node type should be pre-registered");
        assertTrue(aggRegistry.isTypeRegistered("Arc"),
            "Arc type should be pre-registered");
    }
}
