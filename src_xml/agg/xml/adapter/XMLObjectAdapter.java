/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLObject;
import agg.util.XMLHelper;
import agg.xml.core.XMLSerializable;
import agg.xml.core.XMLSerializerContext;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;

/**
 * Adapter class that wraps legacy XMLObject implementations to make them
 * compatible with the new XMLSerializable interface.
 * 
 * <p>This adapter follows the Adapter Pattern, allowing existing classes that
 * implement XMLObject (with XwriteObject/XreadObject methods) to work seamlessly
 * with the new XML serialization infrastructure.</p>
 * 
 * <p>The adapter maintains a reference to both the wrapped XMLObject and the
 * XMLHelper instance needed for serialization.</p>
 */
public class XMLObjectAdapter implements XMLSerializable {
    
    private final XMLObject wrappedObject;
    private XMLHelper xmlHelper;
    
    /**
     * Creates a new adapter wrapping the specified XMLObject.
     * 
     * @param wrappedObject The legacy XMLObject to adapt
     */
    public XMLObjectAdapter(XMLObject wrappedObject) {
        if (wrappedObject == null) {
            throw new IllegalArgumentException("XMLObject cannot be null");
        }
        this.wrappedObject = wrappedObject;
    }
    
    /**
     * Gets the wrapped XMLObject instance.
     * 
     * @return The wrapped XMLObject
     */
    public XMLObject getWrappedObject() {
        return wrappedObject;
    }
    
    /**
     * Sets the XMLHelper instance to use for serialization.
     * 
     * @param xmlHelper The XMLHelper instance
     */
    public void setXMLHelper(XMLHelper xmlHelper) {
        this.xmlHelper = xmlHelper;
    }
    
    /**
     * Gets the XMLHelper instance.
     * 
     * @return The XMLHelper instance, or null if not set
     */
    public XMLHelper getXMLHelper() {
        return xmlHelper;
    }
    
    @Override
    public void serialize(XMLSerializerContext context) throws XMLSerializationException {
        if (xmlHelper == null) {
            throw new XMLSerializationException("XMLHelper not set for adapter");
        }
        
        try {
            // Delegate to the legacy XwriteObject method
            wrappedObject.XwriteObject(xmlHelper);
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to serialize XMLObject", e);
        }
    }
    
    @Override
    public void deserialize(XMLDeserializerContext context) throws XMLSerializationException {
        if (xmlHelper == null) {
            throw new XMLSerializationException("XMLHelper not set for adapter");
        }
        
        try {
            // Delegate to the legacy XreadObject method
            wrappedObject.XreadObject(xmlHelper);
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to deserialize XMLObject", e);
        }
    }
}
