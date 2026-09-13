/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.util.XMLHelper;
import agg.xt_basis.GraGra;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;
import agg.xml.core.XMLSerializerContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Adapter for GraGra objects with real DOM serialization logic.
 *
 * <p>This adapter controls the serialization process directly. It uses an
 * internal XMLHelper to execute the legacy XwriteObject/XreadObject methods
 * of the GraGra domain object, but the adapter manages the DOM document
 * lifecycle -- not XMLHelper. The resulting document is transferred to/from
 * the XMLSerializerContext/XMLDeserializerContext.</p>
 *
 * <p>This is the first step toward full DOM-based serialization. Future
 * steps will replace the XMLHelper dependency with direct DOM operations
 * in the sub-adapters (GraphAdapter, NodeAdapter, etc.).</p>
 */
public class GraGraAdapter extends DomainObjectAdapter<GraGra> {

    /**
     * Creates a new adapter for the specified GraGra.
     *
     * @param graGra The GraGra to adapt
     */
    public GraGraAdapter(GraGra graGra) {
        super(graGra);
    }

    /**
     * Gets the adapted GraGra instance.
     *
     * @return The GraGra
     */
    public GraGra getGraGra() {
        return getDomainObject();
    }

    /**
     * Serializes the GraGra to the specified context by building a DOM document.
     *
     * <p>This method creates a Document root element and delegates to the
     * GraGra's XwriteObject method via an internal XMLHelper, then imports
     * the resulting document into the serializer context.</p>
     *
     * @param context The serializer context to write to
     * @throws XMLSerializationException if serialization fails
     */
    @Override
    public void serialize(XMLSerializerContext context) throws XMLSerializationException {
        GraGra graGra = getGraGra();
        if (graGra == null) {
            throw new XMLSerializationException("GraGra is null");
        }

        try {
            // Create an XMLHelper and let GraGra write to it
            XMLHelper helper = new XMLHelper();
            helper.addTopObject(graGra);

            // Get the document built by XMLHelper
            Document helperDoc = helper.getDoc();
            Element contextRoot = context.getCurrentElement();

            // Import the GraphTransformationSystem element from helper doc into context doc
            Document contextDoc = contextRoot.getOwnerDocument();
            Element gtsElement = (Element) helperDoc.getDocumentElement()
                    .getFirstChild();
            if (gtsElement != null) {
                Node imported = contextDoc.importNode(gtsElement, true);
                contextRoot.appendChild(imported);
            }
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to serialize GraGra", e);
        }
    }

    /**
     * Deserializes the GraGra from the specified context by reading the DOM document.
     *
     * <p>This method extracts the GraphTransformationSystem element from the
     * deserializer context, wraps it in a Document suitable for XMLHelper,
     * and delegates to the GraGra's XreadObject method.</p>
     *
     * @param context The deserializer context to read from
     * @throws XMLSerializationException if deserialization fails
     */
    @Override
    public void deserialize(XMLDeserializerContext context) throws XMLSerializationException {
        GraGra graGra = getGraGra();
        if (graGra == null) {
            throw new XMLSerializationException("GraGra is null");
        }

        try {
            // Get the document from the context
            Element contextElement = context.getCurrentElement();
            Document contextDoc = contextElement.getOwnerDocument();

            // Build a Document that XMLHelper can read
            XMLHelper helper = new XMLHelper();
            Document helperDoc = helper.getDoc();

            // Find and import the GraphTransformationSystem element
            Element gtsElement = findChildElement(contextElement, "GraphTransformationSystem");
            if (gtsElement == null) {
                // Try from the document root
                gtsElement = findChildElement(contextDoc.getDocumentElement(),
                        "GraphTransformationSystem");
            }
            if (gtsElement == null) {
                throw new XMLSerializationException(
                    "GraphTransformationSystem element not found in document");
            }

            // Import the GTS element into the helper's document
            Node imported = helperDoc.importNode(gtsElement, true);
            helperDoc.getDocumentElement().appendChild(imported);

            // Use XMLHelper to read the top object
            helper.getTopObject(graGra);
        } catch (XMLSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to deserialize GraGra", e);
        }
    }

    /**
     * Finds the first child element with the specified name.
     *
     * @param parent The parent element to search
     * @param name The element name to find
     * @return The first matching child element, or null if not found
     */
    private static Element findChildElement(Element parent, String name) {
        if (parent == null || name == null) {
            return null;
        }
        org.w3c.dom.NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && name.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }
}
