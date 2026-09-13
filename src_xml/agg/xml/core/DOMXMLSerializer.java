/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * DOM-based implementation of {@link XMLSerializer}.
 *
 * <p>This serializer uses a {@link DOMXMLSerializerContext} to build a DOM document
 * from an object's {@link XMLSerializable#serialize} call, then writes the document
 * to a file, output stream, or string.</p>
 */
public class DOMXMLSerializer extends AbstractXMLSerializer {

    private final String encoding;
    private final boolean indent;
    private final String indentString;

    /**
     * Creates a new serializer with UTF-8 encoding and indentation enabled.
     */
    public DOMXMLSerializer() {
        this("UTF-8", true, "  ");
    }

    /**
     * Creates a new serializer with the specified settings.
     *
     * @param encoding      The character encoding
     * @param indent        Whether to indent the output
     * @param indentString  The indentation string
     */
    public DOMXMLSerializer(String encoding, boolean indent, String indentString) {
        this.encoding = encoding != null ? encoding : "UTF-8";
        this.indent = indent;
        this.indentString = indentString != null ? indentString : "  ";
    }

    @Override
    public void serialize(Object object, XMLSerializerContext context) throws XMLSerializationException {
        if (object == null) {
            return;
        }
        if (object instanceof XMLSerializable) {
            ((XMLSerializable) object).serialize(context);
        } else {
            context.setTextContent(object.toString());
        }
    }

    @Override
    public boolean beginSerialize(Object object, XMLSerializerContext context) throws XMLSerializationException {
        if (object instanceof XMLSerializable) {
            ((XMLSerializable) object).serialize(context);
            return true;
        }
        return false;
    }

    @Override
    public void endSerialize(Object object, XMLSerializerContext context) throws XMLSerializationException {
        // No-op: serialization is complete after beginSerialize
    }

    /**
     * Serializes an object to a DOM document.
     *
     * @param object The object to serialize
     * @return The DOM document
     * @throws XMLSerializationException if serialization fails
     */
    public Document serializeToDocument(Object object) throws XMLSerializationException {
        DOMXMLSerializerContext context = new DOMXMLSerializerContext();
        serialize(object, context);
        return context.getDocument();
    }

    /**
     * Serializes an object to an XML string.
     *
     * @param object The object to serialize
     * @return The XML string representation
     * @throws XMLSerializationException if serialization fails
     */
    public String serializeToString(Object object) throws XMLSerializationException {
        Document doc = serializeToDocument(object);
        return writeDocumentToString(doc);
    }

    /**
     * Serializes an object to a file.
     *
     * @param object The object to serialize
     * @param file   The output file
     * @throws XMLSerializationException if serialization or writing fails
     */
    public void serializeToFile(Object object, File file) throws XMLSerializationException {
        Document doc = serializeToDocument(object);
        writeDocumentToFile(doc, file);
    }

    /**
     * Serializes an object to an output stream.
     *
     * @param object       The object to serialize
     * @param outputStream The output stream
     * @throws XMLSerializationException if serialization or writing fails
     */
    public void serializeToStream(Object object, OutputStream outputStream) throws XMLSerializationException {
        Document doc = serializeToDocument(object);
        writeDocumentToStream(doc, outputStream);
    }

    /**
     * Serializes an object to a writer.
     *
     * @param object The object to serialize
     * @param writer The writer
     * @throws XMLSerializationException if serialization or writing fails
     */
    public void serializeToWriter(Object object, Writer writer) throws XMLSerializationException {
        Document doc = serializeToDocument(object);
        writeDocumentToWriter(doc, writer);
    }

    private String writeDocumentToString(Document doc) throws XMLSerializationException {
        try {
            java.io.StringWriter writer = new java.io.StringWriter();
            writeDocumentToWriter(doc, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to convert document to string", e);
        }
    }

    private void writeDocumentToFile(Document doc, File file) throws XMLSerializationException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            writeDocumentToStream(doc, fos);
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to write document to file: " + file, e);
        }
    }

    private void writeDocumentToStream(Document doc, OutputStream outputStream) throws XMLSerializationException {
        try {
            Transformer transformer = createTransformer();
            transformer.transform(
                new DOMSource(doc),
                new StreamResult(outputStream)
            );
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to write document to stream", e);
        }
    }

    private void writeDocumentToWriter(Document doc, Writer writer) throws XMLSerializationException {
        try {
            Transformer transformer = createTransformer();
            transformer.transform(
                new DOMSource(doc),
                new StreamResult(writer)
            );
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to write document to writer", e);
        }
    }

    private Transformer createTransformer() throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        if (indent) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            try {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                    String.valueOf(indentString.length()));
            } catch (IllegalArgumentException ignored) {
                // Apache Xalan-specific property; not all XSLT engines support it
            }
        }
        return transformer;
    }
}
