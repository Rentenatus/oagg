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
import agg.editor.impl.EdRuleScheme;
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
        applyUiSegments(edGraGra, registry, context.getDocument());
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
                Element nodeElem = registry.getElement(node.getBasisNode());
                if (nodeElem != null) {
                    attachNodeSegments(node, nodeElem, doc);
                }
            }
            for (EdArc arc : graph.getArcs()) {
                Element arcElem = registry.getElement(arc.getBasisArc());
                if (arcElem != null) {
                    attachArcSegments(arc, arcElem, doc);
                }
            }
        }
        attachRuleSchemeSegments(edGraGra, doc);
    }

    /**
     * Attaches the UI segments of rule scheme kernel/multi rule graphs.
     *
     * <p>The RuleScheme section of the document is produced by the core
     * path through a legacy helper document, so its graph objects are not
     * bound to their elements in the registry. The elements are therefore
     * paired structurally: the i-th RuleScheme element matches the i-th
     * EdRuleScheme, the kernel/multi Rule elements match the kernel/multi
     * rules in order, and Node/Edge elements match the editor graph
     * objects in list order.</p>
     */
    private static void attachRuleSchemeSegments(EdGraGra edGraGra, Document doc) {
        List<EdRuleScheme> schemes = collectRuleSchemes(edGraGra);
        List<Element> schemeElems = collectSchemeElements(doc);
        for (int i = 0; i < schemeElems.size() && i < schemes.size(); i++) {
            EdRuleScheme scheme = schemes.get(i);
            Element kernelElem = XMLUtils.getFirstChildElement(schemeElems.get(i), "Kernel");
            if (kernelElem != null && scheme.getKernelRule() != null) {
                Element kernelRuleElem = XMLUtils.getFirstChildElement(kernelElem, "Rule");
                if (kernelRuleElem != null) {
                    attachRuleElementSegments(kernelRuleElem, scheme.getKernelRule(), doc);
                }
            }
            Element multiElem = XMLUtils.getFirstChildElement(schemeElems.get(i), "Multi");
            if (multiElem != null) {
                List<Element> multiRuleElems = XMLUtils.getChildElements(multiElem, "Rule");
                for (int j = 0; j < multiRuleElems.size()
                        && j < scheme.getMultiRules().size(); j++) {
                    attachRuleElementSegments(
                        multiRuleElems.get(j), scheme.getMultiRules().get(j), doc);
                }
            }
        }
    }

    /**
     * Attaches the UI segments of the LHS/RHS graphs of one kernel or
     * multi rule element of a RuleScheme section.
     */
    private static void attachRuleElementSegments(Element ruleElem, EdRule rule,
            Document doc) {
        List<Element> graphElems = XMLUtils.getChildElements(ruleElem, "Graph");
        for (Element graphElem : graphElems) {
            EdGraph edGraph = graphForKind(rule, graphElem.getAttribute("kind"));
            if (edGraph != null) {
                attachGraphElementSegments(graphElem, edGraph, doc);
            }
        }
    }

    private static void attachGraphElementSegments(Element graphElem, EdGraph edGraph,
            Document doc) {
        List<Element> nodeElems = XMLUtils.getChildElements(graphElem, "Node");
        for (int i = 0; i < nodeElems.size() && i < edGraph.getNodes().size(); i++) {
            attachNodeSegments(edGraph.getNodes().get(i), nodeElems.get(i), doc);
        }
        List<Element> edgeElems = XMLUtils.getChildElements(graphElem, "Edge");
        for (int i = 0; i < edgeElems.size() && i < edGraph.getArcs().size(); i++) {
            attachArcSegments(edGraph.getArcs().get(i), edgeElems.get(i), doc);
        }
    }

    /**
     * Attaches NodeLayout and additionalLayout segments to the core node
     * element, mirroring EdNode.XwriteObject and LayoutNode.XwriteObject.
     */
    private static void attachNodeSegments(EdNode node, Element nodeElem,
            Document doc) {
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
    private static void attachArcSegments(EdArc arc, Element arcElem,
            Document doc) {
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
            DOMSerializationRegistry registry, Document doc) {
        for (EdGraph graph : collectGraphs(edGraGra)) {
            for (EdNode node : graph.getNodes()) {
                Element nodeElem = registry.getElement(node.getBasisNode());
                if (nodeElem != null) {
                    applyNodeSegments(node, nodeElem);
                }
            }
            for (EdArc arc : graph.getArcs()) {
                Element arcElem = registry.getElement(arc.getBasisArc());
                if (arcElem != null) {
                    applyArcSegments(arc, arcElem);
                }
            }
        }
        applyRuleSchemeSegments(edGraGra, registry, doc);
    }

    /**
     * Applies the UI segments of rule scheme kernel/multi rule graphs onto
     * the editor objects, using the same structural pairing as the save
     * side (see {@link #attachRuleSchemeSegments}).
     */
    private static void applyRuleSchemeSegments(EdGraGra edGraGra,
            DOMSerializationRegistry registry, Document doc) {
        List<EdRuleScheme> schemes = collectRuleSchemes(edGraGra);
        List<Element> schemeElems = collectSchemeElements(doc);
        for (int i = 0; i < schemeElems.size() && i < schemes.size(); i++) {
            EdRuleScheme scheme = schemes.get(i);
            Element kernelElem = XMLUtils.getFirstChildElement(schemeElems.get(i), "Kernel");
            if (kernelElem != null && scheme.getKernelRule() != null) {
                Element kernelRuleElem = XMLUtils.getFirstChildElement(kernelElem, "Rule");
                if (kernelRuleElem != null) {
                    applyRuleElementSegments(kernelRuleElem, scheme.getKernelRule());
                }
            }
            Element multiElem = XMLUtils.getFirstChildElement(schemeElems.get(i), "Multi");
            if (multiElem != null) {
                List<Element> multiRuleElems = XMLUtils.getChildElements(multiElem, "Rule");
                for (int j = 0; j < multiRuleElems.size()
                        && j < scheme.getMultiRules().size(); j++) {
                    applyRuleElementSegments(
                        multiRuleElems.get(j), scheme.getMultiRules().get(j));
                }
            }
        }
    }

    /**
     * Applies the UI segments of the LHS/RHS graphs of one kernel or multi
     * rule element of a RuleScheme section onto the editor objects.
     */
    private static void applyRuleElementSegments(Element ruleElem, EdRule rule) {
        List<Element> graphElems = XMLUtils.getChildElements(ruleElem, "Graph");
        for (Element graphElem : graphElems) {
            EdGraph edGraph = graphForKind(rule, graphElem.getAttribute("kind"));
            if (edGraph != null) {
                applyGraphElementSegments(graphElem, edGraph);
            }
        }
    }

    private static void applyGraphElementSegments(Element graphElem, EdGraph edGraph) {
        List<Element> nodeElems = XMLUtils.getChildElements(graphElem, "Node");
        for (int i = 0; i < nodeElems.size() && i < edGraph.getNodes().size(); i++) {
            applyNodeSegments(edGraph.getNodes().get(i), nodeElems.get(i));
        }
        List<Element> edgeElems = XMLUtils.getChildElements(graphElem, "Edge");
        for (int i = 0; i < edgeElems.size() && i < edGraph.getArcs().size(); i++) {
            applyArcSegments(edGraph.getArcs().get(i), edgeElems.get(i));
        }
    }

    /**
     * Reads the NodeLayout and additionalLayout segments of the core node
     * element onto the editor node, mirroring EdNode.XreadObject and
     * LayoutNode.XreadObject.
     */
    private static void applyNodeSegments(EdNode node, Element nodeElem) {
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
    private static void applyArcSegments(EdArc arc, Element arcElem) {
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
            // Rule schemes are handled separately (structural pairing);
            // their graph objects are not registered in the registry
            if (rule instanceof EdRuleScheme) {
                continue;
            }
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
     * Returns the LHS or RHS editor graph of the rule for the given graph
     * kind attribute, or null for unknown kinds.
     */
    private static EdGraph graphForKind(EdRule rule, String kind) {
        if ("LHS".equals(kind)) {
            return rule.getLeft();
        }
        if ("RHS".equals(kind)) {
            return rule.getRight();
        }
        return null;
    }

    /**
     * Returns the editor rule schemes of the grammar in rule order.
     */
    private static List<EdRuleScheme> collectRuleSchemes(EdGraGra edGraGra) {
        List<EdRuleScheme> schemes = new ArrayList<>();
        for (int i = 0; i < edGraGra.getRules().size(); i++) {
            EdRule rule = edGraGra.getRules().get(i);
            if (rule instanceof EdRuleScheme) {
                schemes.add((EdRuleScheme) rule);
            }
        }
        return schemes;
    }

    /**
     * Returns the RuleScheme elements of the document in document order.
     */
    private static List<Element> collectSchemeElements(Document doc) {
        Element gtsElem = XMLUtils.getFirstChildElement(
            doc.getDocumentElement(), "GraphTransformationSystem");
        if (gtsElem != null) {
            return XMLUtils.getChildElements(gtsElem, "RuleScheme");
        }
        return new ArrayList<>();
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
