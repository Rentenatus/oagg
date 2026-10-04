/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Registry for object-to-ID mappings during DOM serialization and deserialization.
 *
 * <p>This class replaces XMLHelper's object2index/index2object maps. Each domain
 * object (Type, Node, Arc, Graph) gets a unique ID (I0, I1, ...) that is used
 * as an XML attribute value for cross-references (e.g. type="I1", source="I8").</p>
 *
 * <p>During serialization, objects are registered and their IDs are written as
 * attributes. During deserialization, IDs are read from attributes and used
 * to look up the corresponding objects.</p>
 */
public class DOMSerializationRegistry {

    private final Map<Object, String> objectToId;
    private final Map<String, Object> idToObject;
    private final java.util.Map<Object, org.w3c.dom.Element> objectToElement;
    private int nextId;

    /**
     * Creates a new empty registry.
     */
    public DOMSerializationRegistry() {
        this.objectToId = new IdentityHashMap<>();
        this.idToObject = new HashMap<>();
        this.objectToElement = new IdentityHashMap<>();
        this.nextId = 0;
    }

    /**
     * Binds the DOM element that represents the given object. Used by the
     * UI layer (agg-ui-xml) to attach editor segments (NodeLayout,
     * EdgeLayout, ...) to the core elements after serialization and to
     * read them back after deserialization.
     *
     * @param obj The domain object
     * @param element The DOM element representing the object
     */
    public void bindElement(Object obj, org.w3c.dom.Element element) {
        if (obj != null && element != null) {
            objectToElement.put(obj, element);
        }
    }

    /**
     * Returns the DOM element bound to the given object, or null.
     *
     * @param obj The domain object
     * @return The bound element, or null if none was bound
     */
    public org.w3c.dom.Element getElement(Object obj) {
        return obj == null ? null : objectToElement.get(obj);
    }

    /**
     * Registers an object and returns its ID. If the object is already
     * registered, the existing ID is returned.
     *
     * @param obj The object to register
     * @return The ID string (e.g. "I0", "I1")
     */
    public String register(Object obj) {
        if (obj == null) {
            return "";
        }
        String existingId = objectToId.get(obj);
        if (existingId != null) {
            return existingId;
        }
        String id = "I" + nextId++;
        objectToId.put(obj, id);
        idToObject.put(id, obj);
        return id;
    }

    /**
     * Registers an object with a specific ID (used during deserialization
     * when reading IDs from XML attributes).
     *
     * @param obj The object to register
     * @param id  The ID to assign
     */
    public void registerWithId(Object obj, String id) {
        if (obj == null || id == null || id.isEmpty()) {
            return;
        }
        objectToId.put(obj, id);
        idToObject.put(id, obj);
        int numericPart = Integer.parseInt(id.substring(1));
        if (numericPart >= nextId) {
            nextId = numericPart + 1;
        }
    }

    /**
     * Gets the ID for a registered object.
     *
     * @param obj The object to look up
     * @return The ID string, or empty string if not registered
     */
    public String getId(Object obj) {
        if (obj == null) {
            return "";
        }
        String id = objectToId.get(obj);
        return id != null ? id : "";
    }

    /**
     * Gets the object for a registered ID.
     *
     * @param id The ID to look up
     * @return The object, or null if not registered
     */
    public Object getObject(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return idToObject.get(id);
    }

    /**
     * Checks if an object is already registered.
     *
     * @param obj The object to check
     * @return true if registered
     */
    public boolean isRegistered(Object obj) {
        return obj != null && objectToId.containsKey(obj);
    }

    /**
     * Checks if an ID is already registered.
     *
     * @param id The ID to check
     * @return true if registered
     */
    public boolean isIdRegistered(String id) {
        return id != null && !id.isEmpty() && idToObject.containsKey(id);
    }

    /**
     * Clears all registrations.
     */
    public void clear() {
        objectToId.clear();
        idToObject.clear();
        nextId = 0;
    }
}
