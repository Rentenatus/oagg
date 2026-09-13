/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLObject;
import agg.util.XMLHelper;

/**
 * Base adapter class for AGG domain objects (Graph, Rule, Node, Arc, etc.)
 * that implement XMLObject.
 * 
 * <p>This adapter provides common functionality for adapting domain objects
 * to the new XMLSerializable interface. Specific domain object adapters
 * can extend this class.</p>
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
    
    /**
     * Convenience method to set the XMLHelper for this adapter.
     * 
     * @param xmlHelper The XMLHelper instance
     */
    public void setXMLHelper(XMLHelper xmlHelper) {
        super.setXMLHelper(xmlHelper);
    }
    
    /**
     * Convenience method to get the XMLHelper for this adapter.
     * 
     * @return The XMLHelper instance
     */
    public XMLHelper getXMLHelper() {
        return super.getXMLHelper();
    }
}
