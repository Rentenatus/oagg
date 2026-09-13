/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Arc;
import agg.xt_basis.Graph;
import agg.xt_basis.Node;
import agg.xt_basis.Type;
import agg.xml.core.XMLSerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Set;

/**
 * Adapter for Graph objects with real DOM serialization logic.
 *
 * <p>Serializes a Graph as a {@code <Graph>} DOM element with child
 * {@code <Node>} and {@code <Edge>} elements. Type references are stored
 * as ID attributes using a {@link DOMSerializationRegistry}.</p>
 */
public class GraphAdapter extends DomainObjectAdapter<Graph> {

    /**
     * Creates a new adapter for the specified Graph.
     *
     * @param graph The Graph to adapt
     */
    public GraphAdapter(Graph graph) {
        super(graph);
    }

    /**
     * Gets the adapted Graph instance.
     *
     * @return The Graph
     */
    public Graph getGraph() {
        return getDomainObject();
    }

    /**
     * Serializes this graph to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The Graph DOM element
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        Graph graph = getGraph();
        if (graph == null) {
            return null;
        }

        Element graphElem = doc.createElement("Graph");
        String graphId = registry.register(graph);
        graphElem.setAttribute("ID", graphId);

        String kind = graph.getKind();
        if (kind != null && !kind.isEmpty()) {
            graphElem.setAttribute("kind", kind);
        }
        graphElem.setAttribute("name", graph.getName() != null ? graph.getName() : "");

        // Comment and info
        String comment = graph.getTextualComment();
        if (comment != null && !comment.isEmpty()) {
            graphElem.setAttribute("comment", comment);
        }
        String info = graph.getHelpInfo();
        if (info != null && !info.isEmpty()) {
            graphElem.setAttribute("info", info);
        }

        // Serialize nodes
        Set<Node> nodes = graph.getNodesSet();
        if (nodes != null) {
            for (Node node : nodes) {
                NodeAdapter nodeAdapter = new NodeAdapter(node);
                Element nodeElem = nodeAdapter.serializeToElement(doc, registry);
                if (nodeElem != null) {
                    graphElem.appendChild(nodeElem);
                }
            }
        }

        // Serialize arcs
        Set<Arc> arcs = graph.getArcsSet();
        if (arcs != null) {
            for (Arc arc : arcs) {
                ArcAdapter arcAdapter = new ArcAdapter(arc);
                Element arcElem = arcAdapter.serializeToElement(doc, registry);
                if (arcElem != null) {
                    graphElem.appendChild(arcElem);
                }
            }
        }

        return graphElem;
    }

    /**
     * Deserializes a Graph from a DOM element into the wrapped Graph instance.
     *
     * @param graphElem The Graph DOM element
     * @param registry  The ID registry for cross-references
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserializeFromElement(Element graphElem, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        Graph graph = getGraph();
        if (graph == null || graphElem == null) {
            return;
        }

        String id = graphElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(graph, id);
        }

        String name = graphElem.getAttribute("name");
        if (name != null && !name.isEmpty()) {
            graph.setName(name);
        }

        String comment = graphElem.getAttribute("comment");
        if (comment != null && !comment.isEmpty()) {
            graph.setTextualComment(comment);
        }

        String info = graphElem.getAttribute("info");
        if (info != null && !info.isEmpty()) {
            graph.setHelpInfo(info);
        }

        // Parse child nodes and edges
        NodeList children = graphElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) child;
            String tagName = childElem.getTagName();

            if ("Node".equals(tagName)) {
                deserializeNode(childElem, graph, registry);
            } else if ("Edge".equals(tagName)) {
                deserializeArc(childElem, graph, registry);
            }
        }
    }

    private void deserializeNode(Element nodeElem, Graph graph, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        String typeId = nodeElem.getAttribute("type");
        Type type = (Type) registry.getObject(typeId);
        if (type == null) {
            throw new XMLSerializationException(
                "Cannot resolve Node type reference: " + typeId);
        }

        try {
            Node node = graph.createNode(type);
            String nodeId = nodeElem.getAttribute("ID");
            if (!nodeId.isEmpty()) {
                registry.registerWithId(node, nodeId);
            }

            String visible = nodeElem.getAttribute("visible");
            if ("false".equals(visible)) {
                node.setVisible(false);
            }

            String objName = nodeElem.getAttribute("name");
            if (objName != null) {
                node.setObjectName(objName);
            }

            // Deserialize attributes (AttrInstance)
            agg.attribute.AttrInstance attrInst = node.getAttribute();
            if (attrInst instanceof agg.attribute.impl.ValueTuple) {
                AttributeSerializer.deserializeAttributes(
                    nodeElem, (agg.attribute.impl.ValueTuple) attrInst, registry);
            }
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to create Node in graph", e);
        }
    }

    private void deserializeArc(Element arcElem, Graph graph, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        String typeId = arcElem.getAttribute("type");
        String sourceId = arcElem.getAttribute("source");
        String targetId = arcElem.getAttribute("target");

        Type type = (Type) registry.getObject(typeId);
        Node source = (Node) registry.getObject(sourceId);
        Node target = (Node) registry.getObject(targetId);

        if (type == null) {
            throw new XMLSerializationException(
                "Cannot resolve Edge type reference: " + typeId);
        }
        if (source == null) {
            throw new XMLSerializationException(
                "Cannot resolve Edge source reference: " + sourceId);
        }
        if (target == null) {
            throw new XMLSerializationException(
                "Cannot resolve Edge target reference: " + targetId);
        }

        try {
            Arc arc = graph.createArc(type, source, target);
            String arcId = arcElem.getAttribute("ID");
            if (!arcId.isEmpty()) {
                registry.registerWithId(arc, arcId);
            }

            String visible = arcElem.getAttribute("visible");
            if ("false".equals(visible)) {
                arc.setVisible(false);
            }

            String objName = arcElem.getAttribute("name");
            if (objName != null) {
                arc.setObjectName(objName);
            }

            // Deserialize attributes (AttrInstance)
            agg.attribute.AttrInstance arcAttrInst = arc.getAttribute();
            if (arcAttrInst instanceof agg.attribute.impl.ValueTuple) {
                AttributeSerializer.deserializeAttributes(
                    arcElem, (agg.attribute.impl.ValueTuple) arcAttrInst, registry);
            }
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to create Arc in graph", e);
        }
    }
}
