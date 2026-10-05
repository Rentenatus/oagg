/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLObject;

/**
 * Base class for adapters that wrap a domain object.
 *
 * @param <T> the domain object type
 */
public class DomainObjectAdapter<T extends XMLObject> extends XMLObjectAdapter {

    /**
     * Creates a new adapter for the specified domain object.
     *
     * @param domainObject The domain object to adapt
     */
    public DomainObjectAdapter(T domainObject) {
        super(domainObject);
    }

    /**
     * Gets the domain object being adapted.
     *
     * @return The domain object
     */
    @SuppressWarnings("unchecked")
    public T getDomainObject() {
        return (T) getWrappedObject();
    }
}
