/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.validation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Canonical XML comparator that normalises generated IDs and element ordering
 * before comparing two XML documents.
 *
 * <p>The normalisation process:
 * <ol>
 *   <li>Parse both XML inputs into DOM documents.</li>
 *   <li>Recursively sort children of every element by a canonical key
 *       (tag name + sorted non-ID attributes + child signatures).</li>
 *   <li>Assign canonical IDs in sorted-traversal order for every element
 *       that has an {@code ID} attribute, building a mapping from original
 *       ID to canonical ID.</li>
 *   <li>Replace all {@code ID} attribute values and all reference attribute
 *       values ({@code type}, {@code source}, {@code target}, {@code orig},
 *       {@code image}, {@code f}) with their canonical equivalents.</li>
 *   <li>Serialize both canonical trees to strings and compare.</li>
 * </ol>
 *
 * <p>This allows two XML documents that differ only in generated ID values
 * or child element ordering to compare as equal.</p>
 */
public final class XmlCanonicalComparator {

    /** Attributes that hold object IDs (generated, not semantically meaningful). */
    private static final String ID_ATTR = "ID";

    /** Attributes that reference object IDs (point to generated IDs). */
    private static final java.util.Set<String> REF_ATTRS = java.util.Set.of(
        "type", "source", "target", "orig", "image", "id", "Rule"
    );

    private XmlCanonicalComparator() {
    }

    // ---- Public API ----

    /**
     * Compares two XML files canonically.
     *
     * @param fileA first XML file
     * @param fileB second XML file
     * @return comparison result
     */
    public static ComparisonResult compareFiles(File fileA, File fileB) {
        try {
            Document docA = parseFile(fileA);
            Document docB = parseFile(fileB);
            return compareDocuments(docA, docB);
        } catch (Exception e) {
            return new ComparisonResult(false, "Parse error: " + e.getMessage());
        }
    }

    /**
     * Compares two XML strings canonically.
     *
     * @param xmlA first XML string
     * @param xmlB second XML string
     * @return comparison result
     */
    public static ComparisonResult compareStrings(String xmlA, String xmlB) {
        try {
            Document docA = parseString(xmlA);
            Document docB = parseString(xmlB);
            return compareDocuments(docA, docB);
        } catch (Exception e) {
            return new ComparisonResult(false, "Parse error: " + e.getMessage());
        }
    }

    /**
     * Compares two already-parsed DOM documents canonically.
     *
     * @param docA first document
     * @param docB second document
     * @return comparison result
     */
    public static ComparisonResult compareDocuments(Document docA, Document docB) {
        Element rootA = docA.getDocumentElement();
        Element rootB = docB.getDocumentElement();
        // Iterative refinement: sort → walk → re-sort with normalized refs
        Map<String, String> idMapA = new LinkedHashMap<>();
        Map<String, String> idMapB = new LinkedHashMap<>();
        for (int iteration = 0; iteration < 3; iteration++) {
            sortChildrenWithRefs(rootA, idMapA);
            sortChildrenWithRefs(rootB, idMapB);
            idMapA.clear();
            idMapB.clear();
            int[] counter = {0};
            String mismatch = parallelWalk(rootA, rootB, idMapA, idMapB, counter);
            if (mismatch != null) {
                return new ComparisonResult(false, mismatch);
            }
        }
        String canonA = serializeCanonical(rootA, idMapA);
        String canonB = serializeCanonical(rootB, idMapB);
        boolean equal = canonA.equals(canonB);
        if (equal) {
            return new ComparisonResult(true, "XML documents are canonically equal");
        }
        String diff = describeDifference(canonA, canonB);
        return new ComparisonResult(false, diff);
    }

    /**
     * Recursively sorts child elements of each element by a canonical key
     * that includes normalized reference attribute values.
     */
    private static void sortChildrenWithRefs(Element parent, Map<String, String> idMap) {
        List<Element> children = getChildElements(parent);
        for (Element child : children) {
            sortChildrenWithRefs(child, idMap);
        }
        if (children.size() > 1) {
            children.sort(Comparator.comparing(
                e -> elementSortKeyWithRefs(e, idMap)));
            for (Element child : children) {
                parent.appendChild(child);
            }
        }
    }

    /**
     * Computes a sort key including normalized reference attributes.
     */
    private static String elementSortKeyWithRefs(Element elem, Map<String, String> idMap) {
        StringBuilder sb = new StringBuilder(elem.getTagName());
        Map<String, String> attrs = new TreeMap<>();
        for (int i = 0; i < elem.getAttributes().getLength(); i++) {
            Node attr = elem.getAttributes().item(i);
            String name = attr.getNodeName();
            String value = attr.getNodeValue();
            if (ID_ATTR.equals(name)) {
                continue; // skip ID for sorting
            }
            if (REF_ATTRS.contains(name) && idMap != null && !idMap.isEmpty()) {
                value = idMap.getOrDefault(value, value);
            }
            attrs.put(name, value);
        }
        for (Map.Entry<String, String> e : attrs.entrySet()) {
            sb.append('|').append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.toString();
    }

    // ---- Parallel walk ----

    /**
     * Walks two sorted element trees in parallel, assigning the same canonical
     * ID to elements at equivalent positions. Returns null on success, or an
     * error message if the structures differ.
     */
    private static String parallelWalk(Element elemA, Element elemB,
            Map<String, String> idMapA, Map<String, String> idMapB, int[] counter) {
        String tagA = elemA.getTagName();
        String tagB = elemB.getTagName();
        if (!tagA.equals(tagB)) {
            return "Tag mismatch: <" + tagA + "> vs <" + tagB + ">";
        }
        // Compare non-ID, non-ref attributes
        Map<String, String> attrsA = collectNonIdRefAttrs(elemA);
        Map<String, String> attrsB = collectNonIdRefAttrs(elemB);
        if (!attrsA.equals(attrsB)) {
            return "Attribute mismatch in <" + tagA + ">: " + attrsA + " vs " + attrsB;
        }
        // Assign canonical ID if both have ID
        String idA = elemA.getAttribute(ID_ATTR);
        String idB = elemB.getAttribute(ID_ATTR);
        if (!idA.isEmpty() && !idB.isEmpty()) {
            String canonical = "C" + counter[0]++;
            idMapA.put(idA, canonical);
            idMapB.put(idB, canonical);
        }
        // Walk children in parallel
        List<Element> childrenA = getChildElements(elemA);
        List<Element> childrenB = getChildElements(elemB);
        if (childrenA.size() != childrenB.size()) {
            return "Child count mismatch in <" + tagA + ">: "
                + childrenA.size() + " vs " + childrenB.size();
        }
        for (int i = 0; i < childrenA.size(); i++) {
            String err = parallelWalk(childrenA.get(i), childrenB.get(i),
                idMapA, idMapB, counter);
            if (err != null) {
                return err;
            }
        }
        return null;
    }

    /**
     * Collects non-ID, non-reference attributes as a sorted map.
     */
    private static Map<String, String> collectNonIdRefAttrs(Element elem) {
        Map<String, String> attrs = new TreeMap<>();
        for (int i = 0; i < elem.getAttributes().getLength(); i++) {
            Node attr = elem.getAttributes().item(i);
            String name = attr.getNodeName();
            if (!ID_ATTR.equals(name) && !REF_ATTRS.contains(name)) {
                String value = attr.getNodeValue();
                // Normalize :: to : (legacy XreadObject does this replacement
                // on type additionalRepr, so old-save has :: but new-save has :)
                if ("name".equals(name)) {
                    value = value.replace("::", ":");
                }
                attrs.put(name, value);
            }
        }
        return attrs;
    }

    /**
     * Recursively sorts child elements of each element by a canonical key.
     */
    private static void sortChildren(Element parent) {
        List<Element> children = getChildElements(parent);
        if (children.size() <= 1) {
            for (Element child : children) {
                sortChildren(child);
            }
            return;
        }
        for (Element child : children) {
            sortChildren(child);
        }
        children.sort(Comparator.comparing(
            XmlCanonicalComparator::elementSortKey));
        // Re-append in sorted order (DOM moves nodes)
        for (Element child : children) {
            parent.appendChild(child);
        }
    }

    /**
     * Computes a sort key for an element: tagname + sorted non-ID attributes.
     */
    private static String elementSortKey(Element elem) {
        StringBuilder sb = new StringBuilder(elem.getTagName());
        // Sorted attributes (excluding ID and reference attrs for sorting)
        Map<String, String> attrs = new TreeMap<>();
        for (int i = 0; i < elem.getAttributes().getLength(); i++) {
            Node attr = elem.getAttributes().item(i);
            String name = attr.getNodeName();
            if (!ID_ATTR.equals(name) && !REF_ATTRS.contains(name)) {
                String value = attr.getNodeValue();
                if ("name".equals(name)) {
                    value = value.replace("::", ":");
                }
                attrs.put(name, value);
            }
        }
        for (Map.Entry<String, String> e : attrs.entrySet()) {
            sb.append('|').append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.toString();
    }

    /**
     * Serializes an element to a canonical string, replacing IDs and references.
     */
    private static String serializeCanonical(Element elem, Map<String, String> idMap) {
        StringBuilder sb = new StringBuilder();
        serializeCanonicalRecursive(elem, idMap, sb, 0);
        return sb.toString();
    }

    private static void serializeCanonicalRecursive(Element elem,
            Map<String, String> idMap, StringBuilder sb, int depth) {
        String indent = "  ".repeat(depth);
        sb.append(indent).append('<').append(elem.getTagName());

        // Sorted attributes
        Map<String, String> attrs = new TreeMap<>();
        for (int i = 0; i < elem.getAttributes().getLength(); i++) {
            Node attr = elem.getAttributes().item(i);
            String name = attr.getNodeName();
            String value = attr.getNodeValue();
            if (ID_ATTR.equals(name)) {
                value = idMap.getOrDefault(value, value);
            } else if (REF_ATTRS.contains(name)) {
                value = idMap.getOrDefault(value, value);
            } else if ("name".equals(name)) {
                // Normalize :: to : (legacy XreadObject does this replacement)
                value = value.replace("::", ":");
            }
            attrs.put(name, value);
        }
        for (Map.Entry<String, String> e : attrs.entrySet()) {
            sb.append(' ').append(e.getKey()).append("=\"")
              .append(escapeXml(e.getValue())).append('"');
        }

        List<Element> children = getChildElements(elem);
        String text = elem.getTextContent() != null
            ? elem.getTextContent().trim() : "";
        // Only treat as text if there are no child elements
        if (children.isEmpty() && !text.isEmpty()) {
            sb.append('>').append(escapeXml(text)).append("</")
              .append(elem.getTagName()).append(">\n");
        } else if (children.isEmpty()) {
            sb.append("/>\n");
        } else {
            sb.append(">\n");
            for (Element child : children) {
                serializeCanonicalRecursive(child, idMap, sb, depth + 1);
            }
            sb.append(indent).append("</").append(elem.getTagName()).append(">\n");
        }
    }

    // ---- Helpers ----

    private static List<Element> getChildElements(Element parent) {
        List<Element> elements = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) child);
            }
        }
        return elements;
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static Document parseFile(File file) throws Exception {
        DocumentBuilderFactory factory = createSecureFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    private static Document parseString(String xml) throws Exception {
        DocumentBuilderFactory factory = createSecureFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(
            new ByteArrayInputStream(xml.getBytes("UTF-8"))));
    }

    private static DocumentBuilderFactory createSecureFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setNamespaceAware(true);
        } catch (Exception ignored) {
            // Best-effort XXE protection
        }
        return factory;
    }

    private static String describeDifference(String a, String b) {
        String[] linesA = a.split("\n");
        String[] linesB = b.split("\n");
        int max = Math.min(linesA.length, linesB.length);
        for (int i = 0; i < max; i++) {
            if (!linesA[i].equals(linesB[i])) {
                return "First difference at line " + (i + 1) + ":\n"
                    + "  A: " + truncate(linesA[i]) + "\n"
                    + "  B: " + truncate(linesB[i]);
            }
        }
        if (linesA.length != linesB.length) {
            return "Different line count: A=" + linesA.length + " B=" + linesB.length;
        }
        return "Canonical strings differ (identical content, possible encoding issue)";
    }

    private static String truncate(String s) {
        return s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }

    // ---- Result class ----

    /**
     * Result of a canonical XML comparison.
     */
    public static final class ComparisonResult {
        private final boolean equal;
        private final String message;

        public ComparisonResult(boolean equal, String message) {
            this.equal = equal;
            this.message = message;
        }

        public boolean isEqual() {
            return equal;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return (equal ? "EQUAL" : "DIFFERENT") + ": " + message;
        }
    }
}
