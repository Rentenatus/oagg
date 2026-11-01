/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.mapper;

import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages object references during XML serialization and deserialization
 * to handle circular and shared dependencies.
 *
 * <p>During serialization, each serialized object is registered with a unique ID.
 * When the same object is encountered again, the existing ID is reused instead
 * of writing a full copy. During deserialization, objects are registered by ID
 * so that references can be resolved back to the actual objects.</p>
 *
 * <p>This class uses an {@link IdentityHashMap} for the object-to-ID mapping,
 * ensuring that reference equality (not {@code equals()}) is used to track
 * object identity.</p>
 */
public class ReferenceResolver {

    private final Map<Object, String> objectToId;
    private final Map<String, Object> idToObject;
    private final AtomicInteger idCounter;

    /**
     * Creates a new, empty reference resolver.
     */
    public ReferenceResolver() {
        this.objectToId = new IdentityHashMap<>();
        this.idToObject = new HashMap<>();
        this.idCounter = new AtomicInteger(0);
    }

    /**
     * Registers an object and auto-generates a unique ID for it.
     * If the object is already registered, the existing ID is returned.
     *
     * @param object The object to register (must not be null)
     * @return The unique ID assigned to this object
     */
    public String register(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        String existingId = objectToId.get(object);
        if (existingId != null) {
            return existingId;
        }
        String id = "R" + idCounter.getAndIncrement();
        objectToId.put(object, id);
        idToObject.put(id, object);
        return id;
    }

    /**
     * Registers an object with a specific ID.
     * If the ID is already in use by a different object, an exception is thrown.
     * If the object is already registered with a different ID, an exception is thrown.
     *
     * @param object The object to register (must not be null)
     * @param id     The ID to assign (must not be null or empty)
     */
    public void register(Object object, String id) {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        String existingId = objectToId.get(object);
        if (existingId != null && !existingId.equals(id)) {
            throw new IllegalStateException(
                "Object already registered with a different ID: " + existingId);
        }
        Object existingObject = idToObject.get(id);
        if (existingObject != null && existingObject != object) {
            throw new IllegalStateException(
                "ID already in use by a different object: " + id);
        }
        objectToId.put(object, id);
        idToObject.put(id, object);
    }

    /**
     * Gets the ID for a registered object.
     *
     * @param object The object to look up
     * @return The ID, or null if the object is not registered
     */
    public String getId(Object object) {
        if (object == null) {
            return null;
        }
        return objectToId.get(object);
    }

    /**
     * Gets the object registered with the specified ID.
     *
     * @param id The ID to look up
     * @return The object, or null if no object is registered with this ID
     */
    public Object getObject(String id) {
        if (id == null) {
            return null;
        }
        return idToObject.get(id);
    }

    /**
     * Gets the object registered with the specified ID, cast to the expected type.
     *
     * @param <T>          The expected type
     * @param id           The ID to look up
     * @param expectedType The expected class
     * @return The object cast to the expected type, or null if not found
     * @throws ClassCastException if the object is not of the expected type
     */
    public <T> T getObject(String id, Class<T> expectedType) {
        Object object = getObject(id);
        if (object == null) {
            return null;
        }
        return expectedType.cast(object);
    }

    /**
     * Checks if an object is already registered.
     *
     * @param object The object to check
     * @return true if the object is registered, false otherwise
     */
    public boolean isRegistered(Object object) {
        if (object == null) {
            return false;
        }
        return objectToId.containsKey(object);
    }

    /**
     * Checks if an ID is already in use.
     *
     * @param id The ID to check
     * @return true if the ID is registered, false otherwise
     */
    public boolean isRegistered(String id) {
        if (id == null) {
            return false;
        }
        return idToObject.containsKey(id);
    }

    /**
     * Clears all registered objects and IDs.
     */
    public void clear() {
        objectToId.clear();
        idToObject.clear();
        idCounter.set(0);
    }

    /**
     * Returns the number of registered objects.
     *
     * @return The number of registered objects
     */
    public int size() {
        return objectToId.size();
    }
}
