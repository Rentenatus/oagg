/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLObject;
import agg.xml.core.XMLSerializable;
import agg.xml.core.XMLSerializerContext;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;

/**
 * Generic fallback adapter for XMLObject types without a native adapter.
 *
 * <p>The former legacy delegation to {@code XwriteObject}/{@code XreadObject}
 * through an {@code XMLHelper} has been removed: serializing through this
 * adapter fails with an explicit {@link XMLSerializationException} so that
 * format coverage gaps surface immediately instead of silently falling back
 * to the legacy XML path.</p>
 */
public class XMLObjectAdapter implements XMLSerializable {

    private final XMLObject wrappedObject;

    /**
     * Creates a new adapter wrapping the specified XMLObject.
     *
     * @param wrappedObject The XMLObject to adapt
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

    @Override
    public void serialize(XMLSerializerContext context) throws XMLSerializationException {
        throw new XMLSerializationException(
            "No native XML adapter is registered for "
                + wrappedObject.getClass().getName()
                + "; the legacy XwriteObject delegation has been removed");
    }

    @Override
    public void deserialize(XMLDeserializerContext context) throws XMLSerializationException {
        throw new XMLSerializationException(
            "No native XML adapter is registered for "
                + wrappedObject.getClass().getName()
                + "; the legacy XreadObject delegation has been removed");
    }
}
