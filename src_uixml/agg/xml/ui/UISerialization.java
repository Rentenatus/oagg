/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.ui;

import agg.editor.impl.EdArc;
import agg.editor.impl.EdGraGra;
import agg.editor.impl.EdGraph;
import agg.editor.impl.EdNode;
import agg.editor.impl.EdRule;
import agg.layout.evolutionary.LayoutArc;
import agg.layout.evolutionary.LayoutNode;
import agg.xml.adapter.DOMSerializationRegistry;
import agg.xml.adapter.GraGraAdapter;
import agg.xml.core.DOMXMLDeserializerContext;
import agg.xml.core.DOMXMLSerializerContext;
import agg.xml.core.XMLSerializationException;
import agg.xml.util.XMLUtils;
import agg.xt_basis.GraGra;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * DOM-based serialization of a complete editor grammar (EdGraGra).
 *
 * <p>The core grammar (types, graphs, rules, ...) is serialized by
 * {@link GraGraAdapter} in agg-xml. This class orchestrates the core
 * serialization with a registry it controls and then attaches the UI
 * segments (NodeLayout, EdgeLayout, additionalLayout) to the core
 * Node/Edge elements, mirroring the legacy EdNode/EdArc/LayoutNode/
 * LayoutArc XwriteObject methods.</p>
 *
 * <p>Deserialization is the mirror image: the core is loaded by
 * {@link GraGraAdapter}, wrapped into an {@link EdGraGra} and the UI
 * segments are applied to the editor objects.</p>
 */
public final class UISerialization {

    private static final String ENCODING = "UTF-8";
    private static final String INDENT_STRING = "  ";

    private UISerialization() {
    }

    /**
     * Saves an EdGraGra to a .ggx file using DOM serialization.
     *
     * @param edGraGra The editor grammar to save
     * @param filename The output filename
     * @return true if saving succeeded
     */
    public static boolean saveWithDom(EdGraGra edGraGra, String filename) {
        if (edGraGra == null || filename == null) {
            return false;
        }
        try {
            String outfileName = filename;
            if (!outfileName.toLowerCase().endsWith(".ggx")) {
                outfileName = outfileName + ".ggx";
            }
            Document doc = serializeToDocument(edGraGra);
            writeDocumentToFile(doc, new File(outfileName));
            return true;
        } catch (XMLSerializationException e) {
            return false;
        }
    }

    /**
     * Serializes an EdGraGra to a DOM document: core content first,
     * then the UI segments attached to the Node/Edge elements.
     *
     * @param edGraGra The editor grammar to serialize
     * @return The DOM document
     * @throws XMLSerializationException if serialization fails
     */
    public static Document serializeToDocument(EdGraGra edGraGra)
            throws XMLSerializationException {
        if (edGraGra == null) {
            throw new XMLSerializationException("EdGraGra is null");
        }
        GraGra core = edGraGra.getBasisGraGra();
        if (core == null) {
            throw new XMLSerializationException("EdGraGra has no basis GraGra");
        }

        DOMXMLSerializerContext context = new DOMXMLSerializerContext();
        DOMSerializationRegistry registry = new DOMSerializationRegistry();
        GraGraAdapter adapter = new GraGraAdapter(core);
        adapter.serialize(context, registry);

        attachUiSegments(edGraGra, context.getDocument(), registry);
        return context.getDocument();
    }

    /**
     * Loads an EdGraGra from a .ggx file using DOM deserialization.
     *
     * @param filename The input filename
     * @return The loaded editor grammar
     * @throws XMLSerializationException if deserialization fails
     */
    public static EdGraGra loadWithDom(String filename)
            throws XMLSerializationException {
        if (filename == null) {
            throw new XMLSerializationException("Filename is null");
        }
        File file = new File(filename);
        if (!file.exists()) {
            throw new XMLSerializationException("File not found: " + filename);
        }

        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(file);
        return deserializeFromContext(context);
    }

    /**
     * Deserializes an EdGraGra from a deserializer context: core content
     * first, then the UI segments applied onto the editor objects.
     *
     * @param context The deserializer context positioned at the document
     * @return The loaded editor grammar
     * @throws XMLSerializationException if deserialization fails
     */
    public static EdGraGra deserializeFromContext(DOMXMLDeserializerContext context)
            throws XMLSerializationException {
        if (context == null || context.getDocument() == null) {
            throw new XMLSerializationException("Deserializer context has no document");
        }

        GraGra core = new GraGra();
        DOMSerializationRegistry registry = new DOMSerializationRegistry();
        GraGraAdapter adapter = new GraGraAdapter(core);
        adapter.deserialize(context, registry);

        EdGraGra edGraGra = new EdGraGra(core);
        applyUiSegments(edGraGra, registry);
        return edGraGra;
    }

    // ------------------------------------------------------------------
    // Save: attach UI segments to the core Node/Edge elements
    // ------------------------------------------------------------------

    /**
     * Attaches the UI segments of all editor graph objects to their core
     * Node/Edge DOM elements.
     */
    private static void attachUiSegments(EdGraGra edGraGra, Document doc,
            DOMSerializationRegistry registry) {
        for (EdGraph graph : collectGraphs(edGraGra)) {
            for (EdNode node : graph.getNodes()) {
                attachNodeSegments(node, doc, registry);
            }
            for (EdArc arc : graph.getArcs()) {
                attachArcSegments(arc, doc, registry);
            }
        }
    }

    /**
     * Attaches NodeLayout and additionalLayout segments to the core node
     * element, mirroring EdNode.XwriteObject and LayoutNode.XwriteObject.
     */
    private static void attachNodeSegments(EdNode node, Document doc,
            DOMSerializationRegistry registry) {
        Element nodeElem = registry.getElement(node.getBasisNode());
        if (nodeElem == null) {
            return;
        }

        Element layoutElem = doc.createElement("NodeLayout");
        layoutElem.setAttribute("X", String.valueOf(node.getX()));
        layoutElem.setAttribute("Y", String.valueOf(node.getY()));
        nodeElem.appendChild(layoutElem);

        LayoutNode layoutNode = node.getLayoutNode();
        if (layoutNode != null) {
            Element addElem = doc.createElement("additionalLayout");
            addElem.setAttribute("age", String.valueOf(layoutNode.getAge()));
            addElem.setAttribute("force", String.valueOf(layoutNode.getForce()));
            addElem.setAttribute("frozen", String.valueOf(layoutNode.isFrozen()));
            addElem.setAttribute("zone", String.valueOf(layoutNode.getZone()));
            nodeElem.appendChild(addElem);
        }
    }

    /**
     * Attaches EdgeLayout and additionalLayout segments to the core edge
     * element, mirroring EdArc.XwriteObject and LayoutArc.XwriteObject.
     */
    private static void attachArcSegments(EdArc arc, Document doc,
            DOMSerializationRegistry registry) {
        Element arcElem = registry.getElement(arc.getBasisArc());
        if (arcElem == null) {
            return;
        }

        Element layoutElem = doc.createElement("EdgeLayout");
        java.awt.Point textOffset = arc.getTextOffset();
        layoutElem.setAttribute("textOffsetX",
            String.valueOf(textOffset != null ? textOffset.x : 0));
        layoutElem.setAttribute("textOffsetY",
            String.valueOf(textOffset != null ? textOffset.y : 0));
        if (arc.isLine()) {
            if (arc.hasDefaultAnchor()) {
                layoutElem.setAttribute("bendX", "0");
                layoutElem.setAttribute("bendY", "0");
            } else {
                layoutElem.setAttribute("bendX", String.valueOf(arc.getX()));
                layoutElem.setAttribute("bendY", String.valueOf(arc.getY()));
            }
        } else {
            layoutElem.setAttribute("bendX", String.valueOf(arc.getX()));
            layoutElem.setAttribute("bendY", String.valueOf(arc.getY()));
            // Raw dimension, mirroring EdArc.XwriteObject (NOT getWidthOfLoop,
            // which substitutes Loop.DEFAULT_SIZE for a zero width)
            layoutElem.setAttribute("loopW", String.valueOf(arc.getWidth()));
            layoutElem.setAttribute("loopH", String.valueOf(arc.getHeight()));
        }
        if (arc.isElementOfTypeGraph()) {
            java.awt.Point srcOffset = arc.getSrcMultiplicityOffset();
            java.awt.Point trgOffset = arc.getTrgMultiplicityOffset();
            layoutElem.setAttribute("sourceMultiplicityOffsetX",
                String.valueOf(srcOffset != null ? srcOffset.x : 0));
            layoutElem.setAttribute("sourceMultiplicityOffsetY",
                String.valueOf(srcOffset != null ? srcOffset.y : 0));
            layoutElem.setAttribute("targetMultiplicityOffsetX",
                String.valueOf(trgOffset != null ? trgOffset.x : 0));
            layoutElem.setAttribute("targetMultiplicityOffsetY",
                String.valueOf(trgOffset != null ? trgOffset.y : 0));
        }
        arcElem.appendChild(layoutElem);

        LayoutArc layoutArc = arc.getLayoutArc();
        if (layoutArc != null) {
            Element addElem = doc.createElement("additionalLayout");
            addElem.setAttribute("preflength",
                String.valueOf(layoutArc.getPrefLength()));
            addElem.setAttribute("aktlength",
                String.valueOf(layoutArc.getAktLength()));
            addElem.setAttribute("force", String.valueOf(layoutArc.getForce()));
            arcElem.appendChild(addElem);
        }
    }

    // ------------------------------------------------------------------
    // Load: apply UI segments onto the editor objects
    // ------------------------------------------------------------------

    /**
     * Applies the UI segments of all core Node/Edge elements to the editor
     * graph objects.
     */
    private static void applyUiSegments(EdGraGra edGraGra,
            DOMSerializationRegistry registry) {
        for (EdGraph graph : collectGraphs(edGraGra)) {
            for (EdNode node : graph.getNodes()) {
                applyNodeSegments(node, registry);
            }
            for (EdArc arc : graph.getArcs()) {
                applyArcSegments(arc, registry);
            }
        }
    }

    /**
     * Reads the NodeLayout and additionalLayout segments of the core node
     * element onto the editor node, mirroring EdNode.XreadObject and
     * LayoutNode.XreadObject.
     */
    private static void applyNodeSegments(EdNode node,
            DOMSerializationRegistry registry) {
        Element nodeElem = registry.getElement(node.getBasisNode());
        if (nodeElem == null) {
            return;
        }

        Element layoutElem = XMLUtils.getFirstChildElement(nodeElem, "NodeLayout");
        if (layoutElem != null) {
            node.setX(XMLUtils.getIntAttribute(layoutElem, "X", 20));
            node.setY(XMLUtils.getIntAttribute(layoutElem, "Y", 20));
        }

        Element addElem = XMLUtils.getFirstChildElement(nodeElem, "additionalLayout");
        LayoutNode layoutNode = node.getLayoutNode();
        if (addElem != null && layoutNode != null) {
            layoutNode.setAge(XMLUtils.getIntAttribute(addElem, "age", 0));
            layoutNode.setForce(XMLUtils.getIntAttribute(addElem, "force", 10));
            layoutNode.setFrozen(
                XMLUtils.getBooleanAttribute(addElem, "frozen", false));
            layoutNode.setZone(XMLUtils.getIntAttribute(addElem, "zone", 50));
        }
    }

    /**
     * Reads the EdgeLayout and additionalLayout segments of the core edge
     * element onto the editor arc, mirroring EdArc.XreadObject and
     * LayoutArc.XreadObject.
     */
    private static void applyArcSegments(EdArc arc,
            DOMSerializationRegistry registry) {
        Element arcElem = registry.getElement(arc.getBasisArc());
        if (arcElem == null) {
            return;
        }

        Element layoutElem = XMLUtils.getFirstChildElement(arcElem, "EdgeLayout");
        if (layoutElem != null) {
            arc.setTextOffset(
                XMLUtils.getIntAttribute(layoutElem, "textOffsetX", 0),
                XMLUtils.getIntAttribute(layoutElem, "textOffsetY", 0));
            int bendX = XMLUtils.getIntAttribute(layoutElem, "bendX", 0);
            int bendY = XMLUtils.getIntAttribute(layoutElem, "bendY", 0);
            if (bendX < 0) {
                bendX = 0;
            }
            if (bendY < 0) {
                bendY = 0;
            }
            arc.setX(bendX);
            arc.setY(bendY);
            if (!arc.isLine()) {
                arc.setWidth(XMLUtils.getIntAttribute(layoutElem, "loopW", 0));
                arc.setHeight(XMLUtils.getIntAttribute(layoutElem, "loopH", 0));
            }
            arc.setHasDefaultAnchor(bendX == 0 && bendY == 0);
            if (arc.isElementOfTypeGraph()) {
                java.awt.Point srcOffset = arc.getSrcMultiplicityOffset();
                if (srcOffset != null) {
                    srcOffset.x = XMLUtils.getIntAttribute(
                        layoutElem, "sourceMultiplicityOffsetX", 0);
                    srcOffset.y = XMLUtils.getIntAttribute(
                        layoutElem, "sourceMultiplicityOffsetY", 0);
                }
                java.awt.Point trgOffset = arc.getTrgMultiplicityOffset();
                if (trgOffset != null) {
                    trgOffset.x = XMLUtils.getIntAttribute(
                        layoutElem, "targetMultiplicityOffsetX", 0);
                    trgOffset.y = XMLUtils.getIntAttribute(
                        layoutElem, "targetMultiplicityOffsetY", 0);
                }
            }
        }

        Element addElem = XMLUtils.getFirstChildElement(arcElem, "additionalLayout");
        LayoutArc layoutArc = arc.getLayoutArc();
        if (addElem != null && layoutArc != null) {
            layoutArc.setPrefLength(
                XMLUtils.getIntAttribute(addElem, "preflength", 200));
            layoutArc.setAktLength(
                XMLUtils.getIntAttribute(addElem, "aktlength", 200));
            layoutArc.setForce(XMLUtils.getIntAttribute(addElem, "force", 10));
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Collects all editor graphs whose objects carry UI segments: the type
     * graph, the host graphs and the rule graphs (left/right and their
     * application conditions).
     */
    private static List<EdGraph> collectGraphs(EdGraGra edGraGra) {
        List<EdGraph> graphs = new ArrayList<>();
        EdGraph typeGraph = edGraGra.getTypeGraph();
        if (typeGraph != null) {
            graphs.add(typeGraph);
        }
        for (EdGraph graph : edGraGra.getGraphs()) {
            if (graph != null && !graphs.contains(graph)) {
                graphs.add(graph);
            }
        }
        for (EdRule rule : edGraGra.getRules()) {
            addRuleGraphs(graphs, rule);
        }
        for (EdRule atomic : edGraGra.getAtomics()) {
            addRuleGraphs(graphs, atomic);
        }
        return graphs;
    }

    /**
     * Adds the graphs of a rule (or an atomic constraint, which extends
     * EdRule) whose objects carry UI segments.
     */
    private static void addRuleGraphs(List<EdGraph> graphs, EdRule rule) {
        if (rule == null) {
            return;
        }
        addGraphIfAbsent(graphs, rule.getLeft());
        addGraphIfAbsent(graphs, rule.getRight());
        // EdAtomic leaves the application condition lists null
        if (rule.getNACs() != null) {
            for (int i = 0; i < rule.getNACs().size(); i++) {
                addGraphIfAbsent(graphs, rule.getNACs().get(i));
            }
        }
        if (rule.getPACs() != null) {
            for (int i = 0; i < rule.getPACs().size(); i++) {
                addGraphIfAbsent(graphs, rule.getPACs().get(i));
            }
        }
        if (rule.getNestedACs() != null) {
            for (int i = 0; i < rule.getNestedACs().size(); i++) {
                addGraphIfAbsent(graphs, rule.getNestedACs().get(i));
            }
        }
    }

    private static void addGraphIfAbsent(List<EdGraph> graphs, EdGraph graph) {
        if (graph != null && !graphs.contains(graph)) {
            graphs.add(graph);
        }
    }

    /**
     * Writes a DOM document to a file, using the same output format as the
     * DOMXMLSerializer of agg-xml (UTF-8, indented).
     */
    private static void writeDocumentToFile(Document doc, File file)
            throws XMLSerializationException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            try {
                transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount",
                    String.valueOf(INDENT_STRING.length()));
            } catch (IllegalArgumentException ignored) {
                // Apache Xalan-specific property; not all XSLT engines support it
            }
            transformer.transform(new DOMSource(doc), new StreamResult(fos));
        } catch (Exception e) {
            throw new XMLSerializationException(
                "Failed to write document to file: " + file, e);
        }
    }
}
