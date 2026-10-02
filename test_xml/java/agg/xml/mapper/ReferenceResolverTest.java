/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.mapper;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

/**
 * Unit tests for {@link ReferenceResolver}.
 */
public class ReferenceResolverTest {

    private ReferenceResolver resolver;

    @BeforeMethod
    public void setUp() {
        resolver = new ReferenceResolver();
    }

    @Test
    public void testRegisterAutoId() {
        Object obj = new Object();
        String id = resolver.register(obj);
        assertNotNull(id, "ID should not be null");
        assertTrue(id.startsWith("R"), "ID should start with R");
        assertEquals(resolver.size(), 1, "Size should be 1 after register");
    }

    @Test
    public void testRegisterSameObjectReturnsSameId() {
        Object obj = new Object();
        String id1 = resolver.register(obj);
        String id2 = resolver.register(obj);
        assertEquals(id1, id2, "Same object should get same ID");
        assertEquals(resolver.size(), 1, "Size should still be 1");
    }

    @Test
    public void testRegisterDifferentObjectsGetDifferentIds() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        String id1 = resolver.register(obj1);
        String id2 = resolver.register(obj2);
        assertNotEquals(id1, id2, "Different objects should get different IDs");
        assertEquals(resolver.size(), 2, "Size should be 2");
    }

    @Test
    public void testRegisterWithSpecificId() {
        Object obj = new Object();
        resolver.register(obj, "myId");
        assertEquals(resolver.getId(obj), "myId", "ID should be myId");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testRegisterConflictingId() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        resolver.register(obj1, "sameId");
        resolver.register(obj2, "sameId");
    }

    @Test
    public void testGetObjectById() {
        Object obj = new Object();
        String id = resolver.register(obj);
        Object retrieved = resolver.getObject(id);
        assertSame(retrieved, obj, "Should get the same object back");
    }

    @Test
    public void testGetObjectByIdWithType() {
        String obj = "test";
        String id = resolver.register(obj);
        String retrieved = resolver.getObject(id, String.class);
        assertSame(retrieved, obj, "Should get the same string back");
    }

    @Test
    public void testGetObjectWithUnknownId() {
        assertNull(resolver.getObject("nonexistent"), "Should return null for unknown ID");
    }

    @Test
    public void testIsRegisteredByObject() {
        Object obj = new Object();
        assertFalse(resolver.isRegistered(obj), "Should not be registered initially");
        resolver.register(obj);
        assertTrue(resolver.isRegistered(obj), "Should be registered after register");
    }

    @Test
    public void testIsRegisteredById() {
        Object obj = new Object();
        String id = resolver.register(obj);
        assertTrue(resolver.isRegistered(id), "Should be registered by ID");
        assertFalse(resolver.isRegistered("nonexistent"), "Should not be registered");
    }

    @Test
    public void testClear() {
        Object obj = new Object();
        resolver.register(obj);
        assertEquals(resolver.size(), 1, "Should have 1 item");
        resolver.clear();
        assertEquals(resolver.size(), 0, "Should have 0 items after clear");
        assertFalse(resolver.isRegistered(obj), "Should not be registered after clear");
    }

    @Test
    public void testRegisterNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> resolver.register(null));
    }

    @Test
    public void testSequentialIds() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        String id1 = resolver.register(obj1);
        String id2 = resolver.register(obj2);
        String id3 = resolver.register(obj3);
        assertEquals(id1, "R0", "First ID should be R0");
        assertEquals(id2, "R1", "Second ID should be R1");
        assertEquals(id3, "R2", "Third ID should be R2");
    }
}
