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
        // Iterative refinement: sort -> walk -> re-sort with normalized refs.
        //
        // Strategy: the first iteration sorts children WITHOUT reference
        // attributes (their raw values differ per document and would
        // mispair semantically identical children); the walk pairs children
        // in document order and fills the id map. Later iterations sort
        // WITH the normalized references: this reorders children that differ
        // only in references (e.g. <Mapping orig image>, whose document
        // order is NOT deterministic in the legacy writer) into the same
        // semantic order in both documents.
        //
        // An iteration is successful only when BOTH the walk reports no
        // structural mismatch AND the canonical serializations are equal.
        // Only a mismatch that survives all iterations is real.
        Map<String, String> idMapA = new LinkedHashMap<>();
        Map<String, String> idMapB = new LinkedHashMap<>();
        String mismatch = null;
        String canonA = "";
        String canonB = "";
        for (int iteration = 0; iteration < 5; iteration++) {
            boolean withRefs = iteration > 0;
            sortChildrenWithRefs(rootA, idMapA, withRefs);
            sortChildrenWithRefs(rootB, idMapB, withRefs);
            idMapA.clear();
            idMapB.clear();
            int[] counter = {0};
            String[] firstError = {null};
            parallelWalk(rootA, rootB, idMapA, idMapB, counter, firstError);
            mismatch = firstError[0];
            canonA = serializeCanonical(rootA, idMapA);
            canonB = serializeCanonical(rootB, idMapB);
            if (mismatch == null && canonA.equals(canonB)) {
                return new ComparisonResult(true, "XML documents are canonically equal");
            }
        }
        if (mismatch != null) {
            return new ComparisonResult(false, mismatch);
        }
        String diff = describeDifference(canonA, canonB);
        return new ComparisonResult(false, diff);
    }

    /**
     * Recursively sorts child elements of each element by a canonical key
     * that includes normalized reference attribute values.
     */
    private static void sortChildrenWithRefs(Element parent, Map<String, String> idMap,
            boolean includeRefs) {
        List<Element> children = getChildElements(parent);
        for (Element child : children) {
            sortChildrenWithRefs(child, idMap, includeRefs);
        }
        if (children.size() > 1) {
            children.sort(Comparator.comparing(
                e -> elementSortKey(e, idMap, includeRefs)));
            for (Element child : children) {
                parent.appendChild(child);
            }
        }
    }

    /**
     * Computes a sort key from the tag name and the non-ID attributes.
     * When {@code includeRefs} is false (first iteration), reference
     * attributes (type, source, target, orig, image, ...) are excluded:
     * their raw values differ per document and would mispair semantically
     * identical children. When true (later iterations), reference
     * attributes are normalized through the id map and included, which
     * orders children that differ only in references (e.g. morphism
     * mappings) by their semantic content - the document order of such
     * children is not deterministic in the legacy writer.
     */
    private static String elementSortKey(Element elem, Map<String, String> idMap,
            boolean includeRefs) {
        StringBuilder sb = new StringBuilder(elem.getTagName());
        Map<String, String> attrs = new TreeMap<>();
        for (int i = 0; i < elem.getAttributes().getLength(); i++) {
            Node attr = elem.getAttributes().item(i);
            String name = attr.getNodeName();
            String value = attr.getNodeValue();
            if (ID_ATTR.equals(name)) {
                continue; // skip ID for sorting
            }
            if (REF_ATTRS.contains(name)) {
                if (!includeRefs) {
                    continue; // skip raw references in the first iteration
                }
                if (idMap != null && !idMap.isEmpty()) {
                    value = idMap.getOrDefault(value, value);
                }
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
    private static void parallelWalk(Element elemA, Element elemB,
            Map<String, String> idMapA, Map<String, String> idMapB, int[] counter,
            String[] firstError) {
        parallelWalk(elemA, elemB, idMapA, idMapB, counter, firstError,
            "/" + elemA.getTagName());
    }

    private static void parallelWalk(Element elemA, Element elemB,
            Map<String, String> idMapA, Map<String, String> idMapB, int[] counter,
            String[] firstError, String path) {
        String tagA = elemA.getTagName();
        String tagB = elemB.getTagName();
        if (!tagA.equals(tagB)) {
            recordError(firstError, "Tag mismatch at " + path + ": <" + tagA
                + "> vs <" + tagB + ">");
            return;
        }
        String pathA = path + "[" + positionAmongSiblings(elemA) + "]";
        // Compare non-ID, non-ref attributes
        Map<String, String> attrsA = collectNonIdRefAttrs(elemA);
        Map<String, String> attrsB = collectNonIdRefAttrs(elemB);
        if (!attrsA.equals(attrsB)) {
            recordError(firstError, "Attribute mismatch at " + pathA + " in <"
                + tagA + ">: " + attrsA + " vs " + attrsB);
        }
        // Assign canonical ID if both have ID
        String idA = elemA.getAttribute(ID_ATTR);
        String idB = elemB.getAttribute(ID_ATTR);
        if (!idA.isEmpty() && !idB.isEmpty()) {
            String canonical = "C" + counter[0]++;
            idMapA.put(idA, canonical);
            idMapB.put(idB, canonical);
        }
        // Walk children in parallel; on a count mismatch continue with the
        // smaller count so the idMap keeps filling for the next iteration
        List<Element> childrenA = getChildElements(elemA);
        List<Element> childrenB = getChildElements(elemB);
        if (childrenA.size() != childrenB.size()) {
            recordError(firstError, "Child count mismatch in <" + tagA + "> at "
                + pathA + ": " + childrenA.size() + " vs " + childrenB.size());
        }
        int common = Math.min(childrenA.size(), childrenB.size());
        for (int i = 0; i < common; i++) {
            parallelWalk(childrenA.get(i), childrenB.get(i),
                idMapA, idMapB, counter, firstError,
                pathA + "/" + childrenA.get(i).getTagName());
        }
    }

    /**
     * Records the first error; later errors do not overwrite it.
     */
    private static void recordError(String[] firstError, String message) {
        if (firstError[0] == null) {
            firstError[0] = message;
        }
    }

    /**
     * Returns the one-based position of the element among its sibling
     * elements with the same tag name.
     */
    private static int positionAmongSiblings(Element elem) {
        int pos = 1;
        org.w3c.dom.Node parent = elem.getParentNode();
        if (parent == null) {
            return 1;
        }
        for (org.w3c.dom.Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n == elem) {
                return pos;
            }
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && ((Element) n).getTagName().equals(elem.getTagName())) {
                pos++;
            }
        }
        return pos;
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
                if ("name".equals(name)) {
                    // Normalize :: to : (legacy XreadObject does this replacement
                    // on type additionalRepr)
                    value = value.replace("::", ":");
                    // Normalize generated graph names: legacy XreadObject sets
                    // "LeftOf_<ruleName>" / "RightOf_<ruleName>" but original
                    // XwriteObject writes "Left" / "Right" (from constructor)
                    value = value.replaceAll("LeftOf_.*", "Left");
                    value = value.replaceAll("RightOf_.*", "Right");
                } else if ("comment".equals(name)) {
                    // Strip generated "Formula: ..." comments that Rule.XreadObject
                    // adds to morphisms but original XwriteObject doesn't write
                    if (value.startsWith("Formula: ")) {
                        value = "";
                    }
                    // Skip empty comments (one side may have comment="",
                    // the other may not have the attribute at all)
                    if (value.isEmpty()) {
                        continue;
                    }
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
                    value = value.replaceAll("LeftOf_.*", "Left");
                    value = value.replaceAll("RightOf_.*", "Right");
                } else if ("comment".equals(name)) {
                    if (value.startsWith("Formula: ")) {
                        value = "";
                    }
                    if (value.isEmpty()) {
                        continue;
                    }
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
                // Normalize generated graph names (LeftOf_/RightOf_ → Left/Right)
                value = value.replaceAll("LeftOf_.*", "Left");
                value = value.replaceAll("RightOf_.*", "Right");
            } else if ("comment".equals(name)) {
                // Strip generated "Formula: ..." comments
                if (value.startsWith("Formula: ")) {
                    value = "";
                }
                // Skip empty comments
                if (value.isEmpty()) {
                    continue;
                }
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
