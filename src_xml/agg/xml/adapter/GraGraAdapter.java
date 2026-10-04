/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.BaseFactory;
import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xt_basis.Rule;
import agg.xt_basis.Type;
import agg.xt_basis.TypeSet;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;
import agg.xml.core.XMLSerializerContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Iterator;

/**
 * Adapter for GraGra objects with real DOM serialization logic.
 *
 * <p>This adapter controls the serialization process directly: it builds
 * the GraphTransformationSystem document from the GraGra model through the
 * sub-adapters (TypeSet, Graph, Rule, RuleScheme, Match, ...). The
 * resulting document is transferred to/from the
 * XMLSerializerContext/XMLDeserializerContext.</p>
 */
public class GraGraAdapter extends DomainObjectAdapter<GraGra> {

    /**
     * Number of host graph elements already loaded by this adapter. The
     * first host element reuses the default host graph (itsGraph); every
     * further element creates a new graph, mirroring the legacy reader
     * (GraGra.XreadObject loads one fresh graph per &lt;Graph&gt; element).
     */
    private int hostGraphsLoaded = 0;

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
     * <p>This method creates a Document root element and a
     * GraphTransformationSystem element, then serializes the GraGra content
     * through the sub-adapters into the serializer context.</p>
     *
     * @param context The serializer context to write to
     * @throws XMLSerializationException if serialization fails
     */
    @Override
    public void serialize(XMLSerializerContext context) throws XMLSerializationException {
        serialize(context, new DOMSerializationRegistry());
    }

    /**
     * Serializes the GraGra to the specified context using a caller-provided
     * registry.
     *
     * <p>The caller-controlled registry allows the UI layer (agg-ui-xml) to
     * look up the DOM elements of core objects after serialization, e.g. to
     * attach NodeLayout/EdgeLayout segments.</p>
     *
     * @param context  The serializer context to write to
     * @param registry The ID registry to use
     * @throws XMLSerializationException if serialization fails
     */
    public void serialize(XMLSerializerContext context, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        GraGra graGra = getGraGra();
        if (graGra == null) {
            throw new XMLSerializationException("GraGra is null");
        }

        try {
            Element contextRoot = context.getCurrentElement();
            Document contextDoc = contextRoot.getOwnerDocument();

            contextRoot.appendChild(serializeGraphTransformationSystem(contextDoc, registry));
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to serialize GraGra", e);
        }
    }

    /**
     * Builds the GraphTransformationSystem element for this GraGra without
     * attaching it to a parent. Reused by adapters that embed a complete
     * grammar inside another document (e.g. ApplRuleSequence for .rsx).
     *
     * @param contextDoc The DOM document to create elements in
     * @param registry The ID registry to use
     * @return The GraphTransformationSystem element
     * @throws XMLSerializationException if serialization fails
     */
    public Element serializeGraphTransformationSystem(Document contextDoc,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        GraGra graGra = getGraGra();
        try {
            // Create GraphTransformationSystem element
            Element gtsElem = contextDoc.createElement("GraphTransformationSystem");
            String gtsId = registry.register(graGra);
            gtsElem.setAttribute("ID", gtsId);
            gtsElem.setAttribute("name", graGra.getName() != null ? graGra.getName() : "");

            TypeSet typeSet = graGra.getTypeSet();
            if (typeSet != null) {
                gtsElem.setAttribute("directed", String.valueOf(typeSet.isArcDirected()));
                gtsElem.setAttribute("parallel", String.valueOf(typeSet.isArcParallel()));
            }

            // TaggedValues: AttrHandler+Packages, Options, TypeGraphLevel
            serializeTaggedValues(graGra, contextDoc, gtsElem);

            // Types section
            Element typesElem = contextDoc.createElement("Types");
            if (typeSet != null) {
                // Serialize each type
                de.jare.ndimcol.ref.IteratorWalker<Type> typeIter = typeSet.getTypeWalker();
                while (typeIter != null && typeIter.hasNext()) {
                    Type type = typeIter.next();
                    if (type instanceof agg.xt_basis.TypeImpl) {
                        TypeImplAdapter typeAdapter =
                            new TypeImplAdapter((agg.xt_basis.TypeImpl) type);
                        Element typeElem = typeAdapter.serializeToElement(contextDoc, registry);
                        if (typeElem != null) {
                            typesElem.appendChild(typeElem);
                        }
                    } else if (type instanceof agg.xt_basis.NodeTypeImpl) {
                        NodeTypeImplAdapter nodeTypeAdapter =
                            new NodeTypeImplAdapter((agg.xt_basis.NodeTypeImpl) type);
                        Element typeElem = nodeTypeAdapter.serializeToElement(contextDoc, registry);
                        if (typeElem != null) {
                            typesElem.appendChild(typeElem);
                        }
                    } else if (type instanceof agg.xt_basis.ArcTypeImpl) {
                        ArcTypeImplAdapter arcTypeAdapter =
                            new ArcTypeImplAdapter((agg.xt_basis.ArcTypeImpl) type);
                        Element typeElem = arcTypeAdapter.serializeToElement(contextDoc, registry);
                        if (typeElem != null) {
                            typesElem.appendChild(typeElem);
                        }
                    }
                }

                // Type graph
                Graph typeGraph = typeSet.getTypeGraph();
                if (typeGraph != null) {
                    GraphAdapter tgAdapter = new GraphAdapter(typeGraph);
                    Element tgElem = tgAdapter.serializeToElement(contextDoc, registry);
                    if (tgElem != null) {
                        typesElem.appendChild(tgElem);
                    }
                }
            }
            gtsElem.appendChild(typesElem);

            // Host graphs
            for (Graph graph : graGra.getGraphsVec()) {
                graph.setKind("HOST");
                GraphAdapter graphAdapter = new GraphAdapter(graph);
                Element graphElem = graphAdapter.serializeToElement(contextDoc, registry);
                if (graphElem != null) {
                    gtsElem.appendChild(graphElem);
                }
            }

            // Constraints (if any)
            java.util.List<agg.cons.Formula> formulas = graGra.getConstraintsVec();
            java.util.Enumeration<agg.cons.AtomConstraint> atomics = graGra.getAtomics();
            boolean hasConstraints = (formulas != null && !formulas.isEmpty())
                || atomics.hasMoreElements();
            if (hasConstraints) {
                Element constraintsElem = contextDoc.createElement("Constraints");
                // Serialize atomics
                while (atomics.hasMoreElements()) {
                    agg.cons.AtomConstraint atom = atomics.nextElement();
                    AtomConstraintAdapter atomAdapter = new AtomConstraintAdapter(atom);
                    Element atomElem = atomAdapter.serializeToElement(contextDoc, registry);
                    if (atomElem != null) {
                        constraintsElem.appendChild(atomElem);
                    }
                }
                // Serialize formulas
                if (formulas != null) {
                    java.util.List<agg.cons.Evaluable> atomicList = new java.util.ArrayList<>();
                    java.util.Enumeration<agg.cons.AtomConstraint> atomicEnum = graGra.getAtomics();
                    while (atomicEnum.hasMoreElements()) {
                        atomicList.add(atomicEnum.nextElement());
                    }
                    for (agg.cons.Formula formula : formulas) {
                        FormulaAdapter formulaAdapter = new FormulaAdapter(formula);
                        Element formulaElem = formulaAdapter.serializeToElement(contextDoc, registry);
                        if (formulaElem != null) {
                            // Add formula string attribute (written by legacy GraGra.saveXML)
                            String formulaStr = formula.getAsString(atomicList);
                            if (formulaStr != null && !formulaStr.isEmpty()) {
                                formulaElem.setAttribute("f", formulaStr);
                            }
                            constraintsElem.appendChild(formulaElem);
                        }
                    }
                }
                gtsElem.appendChild(constraintsElem);
            }

            // Rules (including RuleSchemes)
            for (Rule rule : graGra.getRulesVec()) {
                rule.getSource().setKind("LHS");
                rule.getTarget().setKind("RHS");
                if (rule instanceof agg.xt_basis.agt.RuleScheme) {
                    agg.xt_basis.agt.RuleScheme rs = (agg.xt_basis.agt.RuleScheme) rule;
                    rs.storeIndexOfRuleList(graGra.getRulesVec().indexOf(rule));
                    RuleSchemeAdapter rsAdapter = new RuleSchemeAdapter(rs);
                    Element rsElem = rsAdapter.serializeToElement(contextDoc, registry);
                    if (rsElem != null) {
                        gtsElem.appendChild(rsElem);
                    }
                } else {
                    RuleAdapter ruleAdapter = new RuleAdapter(rule);
                    Element ruleElem = ruleAdapter.serializeToElement(contextDoc, registry);
                    if (ruleElem != null) {
                        gtsElem.appendChild(ruleElem);
                    }
                }
            }

            // RuleSequences
            java.util.List<agg.ruleappl.RuleSequence> ruleSeqs = graGra.getRuleSequences();
            if (ruleSeqs != null && !ruleSeqs.isEmpty()) {
                Element rsElem = contextDoc.createElement("RuleSequences");
                for (agg.ruleappl.RuleSequence seq : ruleSeqs) {
                    Element seqElem = contextDoc.createElement("Sequence");
                    seqElem.setAttribute("name", seq.getName() != null ? seq.getName() : "");
                    if (seq.isTrafoByARS()) {
                        seqElem.setAttribute(agg.ruleappl.RuleSequence.TRAFO_BY_ARS, "true");
                    }
                    if (seq.isTrafoByObjFlow()) {
                        seqElem.setAttribute(agg.ruleappl.RuleSequence.TRAFO_BY_OBJECT_FLOW, "true");
                    }
                    if (seq.getGraph() != null) {
                        Element seqGraphElem = contextDoc.createElement("Graph");
                        String graphId = registry.getId(seq.getGraph());
                        if (graphId.isEmpty()) {
                            graphId = registry.register(seq.getGraph());
                        }
                        seqGraphElem.setAttribute("id", graphId);
                        seqElem.appendChild(seqGraphElem);
                    }
                    java.util.List<agg.util.Pair<java.util.List<agg.util.Pair<String, String>>, String>> subSeqs = seq.getSubSequenceList();
                    if (subSeqs != null) {
                        for (agg.util.Pair<java.util.List<agg.util.Pair<String, String>>, String> subSeq : subSeqs) {
                            Element subSeqElem = contextDoc.createElement("Subsequence");
                            subSeqElem.setAttribute("iterations", subSeq.second != null ? subSeq.second : "");
                            if (subSeq.first != null) {
                                for (agg.util.Pair<String, String> item : subSeq.first) {
                                    Element itemElem = contextDoc.createElement("Item");
                                    itemElem.setAttribute("rule", item.first != null ? item.first : "");
                                    itemElem.setAttribute("iterations", item.second != null ? item.second : "");
                                    subSeqElem.appendChild(itemElem);
                                }
                            }
                            seqElem.appendChild(subSeqElem);
                        }
                    }
                    rsElem.appendChild(seqElem);
                }
                gtsElem.appendChild(rsElem);
            }

            // Matches
            java.util.List<agg.xt_basis.Match> allMatches = new java.util.ArrayList<>();
            for (Rule rule : graGra.getRulesVec()) {
                java.util.Iterator<agg.xt_basis.Match> matchIter = graGra.getMatches(rule);
                while (matchIter.hasNext()) {
                    agg.xt_basis.Match m = matchIter.next();
                    if (m != null) {
                        allMatches.add(m);
                    }
                }
            }
            if (!allMatches.isEmpty()) {
                Element matchesElem = contextDoc.createElement("Matches");
                for (agg.xt_basis.Match m : allMatches) {
                    if (m != null && m.getRule() != null && !m.isEmpty()) {
                        m.setName("MatchOf_" + m.getRule().getName());
                        Element matchOfElem = contextDoc.createElement("MatchOf");
                        // Rule reference
                        String ruleId = registry.getId(m.getRule());
                        if (ruleId.isEmpty()) {
                            ruleId = registry.register(m.getRule());
                        }
                        matchOfElem.setAttribute("Rule", ruleId);
                        // Match object
                        MatchAdapter matchAdapter = new MatchAdapter(m);
                        Element matchElem = matchAdapter.serializeToElement(contextDoc, registry);
                        if (matchElem != null) {
                            matchOfElem.appendChild(matchElem);
                        }
                        matchesElem.appendChild(matchOfElem);
                    }
                }
                gtsElem.appendChild(matchesElem);
            }
            return gtsElem;
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to serialize GraGra", e);
        }
    }

    /**
     * Deserializes the GraGra from the specified context into this adapter's
     * GraGra, using a fresh ID registry.
     *
     * @param context The deserializer context to read from
     * @throws XMLSerializationException if deserialization fails
     */
    @Override
    public void deserialize(XMLDeserializerContext context) throws XMLSerializationException {
        deserialize(context, new DOMSerializationRegistry());
    }

    /**
     * Deserializes the GraGra from the specified context using a
     * caller-provided registry.
     *
     * <p>The caller-controlled registry allows the UI layer (agg-ui-xml) to
     * look up the DOM elements of core objects after deserialization, e.g. to
     * read NodeLayout/EdgeLayout segments onto the editor objects.</p>
     *
     * @param context  The deserializer context to read from
     * @param registry The ID registry to use
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserialize(XMLDeserializerContext context, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        GraGra graGra = getGraGra();
        if (graGra == null) {
            throw new XMLSerializationException("GraGra is null");
        }

        try {
            Element contextElement = context.getCurrentElement();

            // Find the GraphTransformationSystem element
            Element gtsElement = findChildElement(contextElement, "GraphTransformationSystem");
            if (gtsElement == null) {
                gtsElement = findChildElement(
                    contextElement.getOwnerDocument().getDocumentElement(),
                    "GraphTransformationSystem");
            }
            if (gtsElement == null) {
                throw new XMLSerializationException(
                    "GraphTransformationSystem element not found in document");
            }

            deserializeFromElement(gtsElement, registry);
        } catch (XMLSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLSerializationException("Failed to deserialize GraGra", e);
        }
    }

    /**
     * Deserializes the GraGra from the given GraphTransformationSystem
     * element. Reused by adapters that embed a complete grammar inside
     * another document (e.g. ApplRuleSequence for .rsx).
     *
     * @param gtsElement The GraphTransformationSystem element
     * @param registry The ID registry to use
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserializeFromElement(Element gtsElement,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        GraGra graGra = getGraGra();
        if (graGra == null) {
            throw new XMLSerializationException("GraGra is null");
        }

            // Register GraGra
            String gtsId = gtsElement.getAttribute("ID");
            if (!gtsId.isEmpty()) {
                registry.registerWithId(graGra, gtsId);
            }

            // Read GTS attributes
            String name = gtsElement.getAttribute("name");
            if (name != null && !name.isEmpty()) {
                graGra.setName(name);
            }
            TypeSet typeSet = graGra.getTypeSet();
            if (typeSet != null) {
                String directed = gtsElement.getAttribute("directed");
                if (!directed.isEmpty()) {
                    typeSet.setArcDirected(Boolean.valueOf(directed));
                }
                String parallel = gtsElement.getAttribute("parallel");
                if (!parallel.isEmpty()) {
                    typeSet.setArcParallel(Boolean.valueOf(parallel));
                }
            }

            // Parse children in order: TaggedValues, Types, Graphs, Constraints, Rules
            // First pass: read TaggedValues (before Types)
            int loadedTypeGraphLevel = deserializeTaggedValues(gtsElement, graGra);

            // Second pass: read structural elements
            NodeList children = gtsElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                org.w3c.dom.Node child = children.item(i);
                if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    continue;
                }
                Element childElem = (Element) child;
                String tagName = childElem.getTagName();

                if ("Types".equals(tagName)) {
                    deserializeTypes(childElem, graGra, registry);
                    // After types loaded: refresh inheritance arcs
                    if (graGra.getTypeSet() != null) {
                        graGra.getTypeSet().refreshInheritanceArcs();
                    }
                } else if ("Graph".equals(tagName)) {
                    deserializeHostGraph(childElem, graGra, registry);
                } else if ("Constraints".equals(tagName)) {
                    deserializeConstraints(childElem, graGra, registry);
                } else if ("RuleScheme".equals(tagName)) {
                    agg.xt_basis.agt.RuleScheme rs = graGra.createRuleScheme();
                    RuleSchemeAdapter rsAdapter = new RuleSchemeAdapter(rs);
                    rsAdapter.deserializeFromElement(childElem, registry);
                } else if ("Rule".equals(tagName)) {
                    deserializeRule(childElem, graGra, registry);
                } else if ("Matches".equals(tagName)) {
                    deserializeMatches(childElem, graGra, registry);
                } else if ("RuleSequences".equals(tagName)) {
                    deserializeRuleSequences(childElem, graGra, registry);
                }
            }

            // Apply the type graph level like the legacy GraGra.XreadObject
            // does at the end of the load (after the types are available).
            if (loadedTypeGraphLevel == TypeSet.ENABLED_MAX
                    || loadedTypeGraphLevel == TypeSet.ENABLED_MAX_MIN) {
                graGra.setLevelOfTypeGraphCheck(loadedTypeGraphLevel);
            } else if (graGra.getTypeSet() != null) {
                graGra.getTypeSet().setLevelOfTypeGraph(loadedTypeGraphLevel);
            }

            // Post-load setup
            graGra.setUsedClassPackages();
            // NOTE: do NOT call graGra.isReadyToTransform() here. It has the
            // side effect checkUsedVariables -> deleteUnusedVars, which
            // removes rule variables that are declared but not referenced by
            // an attribute expression. The legacy load path does not call
            // it either, and the re-save must reproduce the frozen legacy
            // XML (including unused <Parameter> elements).
    }

    private void deserializeTypes(Element typesElem, GraGra graGra,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        TypeSet typeSet = graGra.getTypeSet();
        if (typeSet == null) {
            return;
        }

        NodeList children = typesElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element typeElem = (Element) child;
            String tagName = typeElem.getTagName();

            if ("NodeType".equals(tagName)) {
                Type type = graGra.createNodeType(hasAttrTypeChildren(typeElem));
                loadTypeDetails(type, typeElem, registry);
            } else if ("EdgeType".equals(tagName)) {
                Type type = graGra.createArcType(hasAttrTypeChildren(typeElem));
                loadTypeDetails(type, typeElem, registry);
            } else if ("Type".equals(tagName)) {
                Type type = graGra.createType();
                loadTypeDetails(type, typeElem, registry);
            } else if ("Graph".equals(tagName)) {
                // Type graph
                Graph typeGraph = graGra.createTypeGraph();
                if (typeGraph != null) {
                    GraphAdapter tgAdapter = new GraphAdapter(typeGraph);
                    tgAdapter.deserializeFromElement(typeElem, registry);
                    typeSet.setTypeGraph(typeGraph);
                }
            }
        }
    }

    /**
     * Loads type details from a DOM element onto a newly created Type.
     * Parses the name string (containing name%additionalRepr:[NODE/EDGE]:),
     * the abstract flag, and registers the DeclMember AttrType children.
     *
     * @param type     The newly created Type to populate
     * @param typeElem The type DOM element
     * @param registry The ID registry
     */
    private void loadTypeDetails(Type type, Element typeElem,
            DOMSerializationRegistry registry) {
        if (type == null || typeElem == null) {
            return;
        }

        String id = typeElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(type, id);
        }

        // Parse name: the format is "name%:additionalRepr:[NODE]:" or "name%:additionalRepr:[EDGE]:"
        String name = typeElem.getAttribute("name");
        if (name != null && !name.isEmpty()) {
            int pct = name.indexOf('%');
            if (pct != -1) {
                String typeName = name.substring(0, pct);
                String additional = name.substring(pct + 1);
                // Set the string representation (the type name)
                if (type instanceof agg.xt_basis.TypeImpl) {
                    ((agg.xt_basis.TypeImpl) type).setStringRepr(typeName);
                } else if (type instanceof agg.xt_basis.NodeTypeImpl) {
                    ((agg.xt_basis.NodeTypeImpl) type).setStringRepr(typeName);
                } else if (type instanceof agg.xt_basis.ArcTypeImpl) {
                    ((agg.xt_basis.ArcTypeImpl) type).setStringRepr(typeName);
                }
                // Set the additional representation
                additional = additional.replaceAll("::", ":");
                type.setAdditionalRepr(additional);
            } else {
                if (type instanceof agg.xt_basis.TypeImpl) {
                    ((agg.xt_basis.TypeImpl) type).setStringRepr(name);
                } else if (type instanceof agg.xt_basis.NodeTypeImpl) {
                    ((agg.xt_basis.NodeTypeImpl) type).setStringRepr(name);
                } else if (type instanceof agg.xt_basis.ArcTypeImpl) {
                    ((agg.xt_basis.ArcTypeImpl) type).setStringRepr(name);
                }
            }
        }

        // Parse abstract attribute
        String abs = typeElem.getAttribute("abstract");
        if ("false".equals(abs)) {
            // createNodeType/createArcType default to non-abstract, nothing to do
        } else if ("true".equals(abs)) {
            if (type instanceof agg.xt_basis.TypeImpl) {
                ((agg.xt_basis.TypeImpl) type).setAbstract(true);
            } else if (type instanceof agg.xt_basis.NodeTypeImpl) {
                ((agg.xt_basis.NodeTypeImpl) type).setAbstract(true);
            } else if (type instanceof agg.xt_basis.ArcTypeImpl) {
                ((agg.xt_basis.ArcTypeImpl) type).setAbstract(true);
            }
        }

        // Parse comment
        String comment = typeElem.getAttribute("comment");
        if (comment != null && !comment.isEmpty()) {
            if (type instanceof agg.xt_basis.TypeImpl) {
                ((agg.xt_basis.TypeImpl) type).setTextualComment(comment);
            } else if (type instanceof agg.xt_basis.NodeTypeImpl) {
                ((agg.xt_basis.NodeTypeImpl) type).setTextualComment(comment);
            } else if (type instanceof agg.xt_basis.ArcTypeImpl) {
                ((agg.xt_basis.ArcTypeImpl) type).setTextualComment(comment);
            }
        }

        // Register AttrType children (DeclMember) for reference resolution
        // Each AttrType element has an ID that is referenced by Attribute elements
        NodeList attrChildren = typeElem.getChildNodes();
        for (int i = 0; i < attrChildren.getLength(); i++) {
            org.w3c.dom.Node ac = attrChildren.item(i);
            if (ac.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) ac;
            String childTag = childElem.getTagName();

            if ("Parent".equals(childTag)) {
                // Resolve parent type reference (multiple inheritance)
                String parentId = childElem.getAttribute("pID");
                if (!parentId.isEmpty() && type instanceof agg.xt_basis.TypeImpl) {
                    Object parentObj = registry.getObject(parentId);
                    if (parentObj instanceof agg.xt_basis.Type) {
                        ((agg.xt_basis.TypeImpl) type).addParent((agg.xt_basis.Type) parentObj);
                    }
                }
            } else if ("AttrType".equals(childTag)) {
                String attrId = childElem.getAttribute("ID");
                String attrName = childElem.getAttribute("attrname");
                String typeName = childElem.getAttribute("typename");
                String visible = childElem.getAttribute("visible");
                // Create a DeclMember from the XML data (matching DeclTuple.XreadObject)
                if (attrName != null && !attrName.isEmpty()
                        && typeName != null && !typeName.isEmpty()
                        && type.getAttrType() instanceof agg.attribute.impl.DeclTuple) {
                    agg.attribute.impl.DeclTuple declTuple =
                        (agg.attribute.impl.DeclTuple) type.getAttrType();
                    String handlerName = agg.attribute.handler.impl.javaExpr.JexHandler
                        .getLabelName();
                    agg.attribute.handler.AttrHandler handler =
                        agg.attribute.impl.AttrTupleManager.getDefaultManager()
                            .getHandler(handlerName);
                    agg.attribute.impl.DeclMember dm =
                        (agg.attribute.impl.DeclMember) declTuple.addMember(
                            handler, typeName, attrName);
                    if (dm != null) {
                        if ("true".equals(visible) || visible.isEmpty()) {
                            dm.setVisible(true);
                        } else {
                            dm.setVisible(false);
                        }
                        if (!attrId.isEmpty()) {
                            registry.registerWithId(dm, attrId);
                        }
                    }
                }
            }
        }
    }

    private void deserializeHostGraph(Element graphElem, GraGra graGra,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        // Skip the type graph (kind="TG") -- it's handled in Types
        String kind = graphElem.getAttribute("kind");
        if ("TG".equals(kind)) {
            return;
        }

        Graph graph;
        if (hostGraphsLoaded == 0) {
            // First host graph: reuse the existing default host graph
            // (graGra.getGraph() / itsGraph) so that Match objects (which
            // reference itsGraph as target) work correctly.
            graph = graGra.getGraph();
        } else {
            // Additional host graphs: create a new graph per element,
            // mirroring the legacy reader which loads one fresh graph
            // per <Graph> element.
            graph = agg.xt_basis.BaseFactory.theFactory()
                .createGraph(graGra.getTypeSet());
            if (graph != null) {
                graGra.addGraph(graph);
            }
        }
        if (graph != null) {
            GraphAdapter graphAdapter = new GraphAdapter(graph);
            graphAdapter.deserializeFromElement(graphElem, registry);
        }
        hostGraphsLoaded++;
    }

    private void deserializeConstraints(Element constraintsElem, GraGra graGra,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        NodeList children = constraintsElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) child;
            String tagName = childElem.getTagName();

            if ("Graphconstraint_Atomic".equals(tagName)) {
                agg.cons.AtomConstraint ac = graGra.createAtomic("");
                if (ac != null) {
                    AtomConstraintAdapter acAdapter = new AtomConstraintAdapter(ac);
                    acAdapter.deserializeFromElement(childElem, registry);
                }
            } else if ("Formula".equals(tagName)) {
                agg.cons.Formula formula = graGra.createConstraint("");
                if (formula != null) {
                    FormulaAdapter fAdapter = new FormulaAdapter(formula);
                    fAdapter.deserializeFromElement(childElem, registry);
                    // Apply the formula string over the atomic constraints,
                    // mirroring the legacy GraGra.XreadObject behaviour:
                    // f.setFormula(getListOfAtomicObjects(), s)
                    String fString = childElem.getAttribute("f");
                    if (fString != null && !fString.isEmpty()) {
                        formula.setFormula(graGra.getListOfAtomicObjects(), fString);
                    }
                }
            }
        }
    }

    private void deserializeRule(Element ruleElem, GraGra graGra,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        Rule rule = graGra.createRule();
        if (rule != null) {
            RuleAdapter ruleAdapter = new RuleAdapter(rule);
            ruleAdapter.deserializeFromElement(ruleElem, registry);
        }
    }

    /**
     * Deserializes Matches from a {@code <Matches>} element.
     * Each child is a {@code <MatchOf>} containing a Rule reference and a Match.
     */
    private void deserializeMatches(Element matchesElem, GraGra graGra,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        NodeList children = matchesElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element matchOfElem = (Element) child;
            if (!"MatchOf".equals(matchOfElem.getTagName())) {
                continue;
            }

            // Resolve Rule reference
            String ruleId = matchOfElem.getAttribute("Rule");
            if (ruleId == null || ruleId.isEmpty()) {
                // Try to find Rule child element with id attribute
                Element ruleRefElem = findChildElement(matchOfElem, "Rule");
                if (ruleRefElem != null) {
                    ruleId = ruleRefElem.getAttribute("id");
                }
            }
            if (ruleId != null && !ruleId.isEmpty()) {
                Object ruleObj = registry.getObject(ruleId);
                if (ruleObj instanceof Rule) {
                    Rule rule = (Rule) ruleObj;
                    agg.xt_basis.Match match = graGra.createMatch(rule);
                    if (match != null) {
                        // Find and deserialize the Match child
                        Element matchElem = findChildElement(matchOfElem, "Match");
                        if (matchElem != null) {
                            MatchAdapter matchAdapter = new MatchAdapter(match);
                            matchAdapter.deserializeFromElement(matchElem, registry);
                        }
                    }
                }
            }
        }
    }

    /**
     * Deserializes RuleSequences from a {@code <RuleSequences>} element.
     * Each child is a {@code <Sequence>} with name, subsequences, and items.
     */
    private void deserializeRuleSequences(Element rsElem, GraGra graGra,
            DOMSerializationRegistry registry) {
        NodeList children = rsElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element seqElem = (Element) child;
            if (!"Sequence".equals(seqElem.getTagName())) {
                continue;
            }

            String seqName = seqElem.getAttribute("name");
            agg.ruleappl.RuleSequence seq = graGra.createRuleSequence(
                seqName != null ? seqName : "");

            // Parse TRAFO flags
            if ("true".equals(seqElem.getAttribute(agg.ruleappl.RuleSequence.TRAFO_BY_ARS))) {
                seq.setTrafoByARS(true);
            }
            if ("true".equals(seqElem.getAttribute(agg.ruleappl.RuleSequence.TRAFO_BY_OBJECT_FLOW))) {
                seq.setTrafoByObjFlow(true);
            }

            // Parse the graph reference child (start graph of the sequence)
            NodeList seqChildren = seqElem.getChildNodes();
            for (int j = 0; j < seqChildren.getLength(); j++) {
                org.w3c.dom.Node seqChild = seqChildren.item(j);
                if (seqChild.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    continue;
                }
                if ("Graph".equals(seqChild.getNodeName())) {
                    Element graphRef = (Element) seqChild;
                    String graphId = graphRef.getAttribute("id");
                    Object graph = registry.getObject(graphId);
                    if (graph instanceof agg.xt_basis.Graph) {
                        seq.setGraph((agg.xt_basis.Graph) graph);
                        // mirror the legacy GraGra.XreadObject behaviour
                        seq.setCheckAtGraph(true);
                    }
                }
            }

            // Parse Subsequence children
            seqChildren = seqElem.getChildNodes();
            for (int j = 0; j < seqChildren.getLength(); j++) {
                org.w3c.dom.Node seqChild = seqChildren.item(j);
                if (seqChild.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    continue;
                }
                Element subElem = (Element) seqChild;
                String subTag = subElem.getTagName();

                if ("Subsequence".equals(subTag)) {
                    String iterations = subElem.getAttribute("iterations");
                    java.util.List<agg.util.Pair<String, String>> items =
                        new java.util.ArrayList<>();
                    NodeList itemChildren = subElem.getChildNodes();
                    for (int k = 0; k < itemChildren.getLength(); k++) {
                        org.w3c.dom.Node itemNode = itemChildren.item(k);
                        if (itemNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                            continue;
                        }
                        Element itemElem = (Element) itemNode;
                        if ("Item".equals(itemElem.getTagName())) {
                            String ruleName = itemElem.getAttribute("rule");
                            String itemIterations = itemElem.getAttribute("iterations");
                            items.add(new agg.util.Pair<>(ruleName, itemIterations));
                        }
                    }
                    seq.addSubsequence(items, iterations);
                }
            }

            // Resolve the subsequence rule names into the sequence's rule
            // list, mirroring the legacy GraGra.XreadObject behaviour
            seq.makeFlatSequence();
        }
    }

    /**
     * Serializes TaggedValues (AttrHandler+Packages, Options, TypeGraphLevel)
     * as child elements of the GraphTransformationSystem element.
     */
    private void serializeTaggedValues(GraGra graGra, Document doc, Element gtsElem) {
        // AttrHandler + Packages
        java.util.List<agg.util.Pair<String, java.util.List<String>>> packages = graGra.getPackages();
        if (packages != null) {
            for (agg.util.Pair<String, java.util.List<String>> p : packages) {
                Element handlerElem = doc.createElement("TaggedValue");
                handlerElem.setAttribute("Tag", "AttrHandler");
                handlerElem.setAttribute("TagValue", p.first != null ? p.first : "");
                if (p.second != null) {
                    for (String pkg : p.second) {
                        Element pkgElem = doc.createElement("TaggedValue");
                        pkgElem.setAttribute("Tag", "Package");
                        pkgElem.setAttribute("TagValue", pkg);
                        handlerElem.appendChild(pkgElem);
                    }
                }
                gtsElem.appendChild(handlerElem);
            }
        }

        // GraTra options (CSP, injective, dangling, NACs, PACs, GACs, consistency)
        java.util.List<String> options = graGra.getGraTraOptions();
        if (options != null) {
            for (String opt : options) {
                Element optElem = doc.createElement("TaggedValue");
                optElem.setAttribute("Tag", opt);
                optElem.setAttribute("TagValue", "true");
                gtsElem.appendChild(optElem);
            }
        }

        // TypeGraphLevel
        TypeSet typeSet = graGra.getTypeSet();
        if (typeSet != null) {
            Element tglElem = doc.createElement("TaggedValue");
            tglElem.setAttribute("Tag", "TypeGraphLevel");
            int level = graGra.getLevelOfTypeGraphCheck();
            String levelStr;
            switch (level) {
                case TypeSet.ENABLED:
                    levelStr = "ENABLED";
                    break;
                case TypeSet.ENABLED_MAX:
                    levelStr = "ENABLED_MAX";
                    break;
                case TypeSet.ENABLED_MAX_MIN:
                    levelStr = "ENABLED_MAX_MIN";
                    break;
                default:
                    levelStr = "DISABLED";
            }
            tglElem.setAttribute("TagValue", levelStr);
            gtsElem.appendChild(tglElem);
        }
    }

    /**
     * Deserializes TaggedValues from the GraphTransformationSystem element.
     * Reads AttrHandler+Packages, GraTra options, and TypeGraphLevel.
     */
    private int deserializeTaggedValues(Element gtsElem, GraGra graGra) {
        java.util.List<String> options = new java.util.ArrayList<>();
        int loadedLevel = TypeSet.DISABLED;

        NodeList children = gtsElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element tvElem = (Element) child;
            if (!"TaggedValue".equals(tvElem.getTagName())) {
                continue;
            }

            String tag = tvElem.getAttribute("Tag");
            String tagValue = tvElem.getAttribute("TagValue");

            if ("AttrHandler".equals(tag)) {
                // Read nested Package TaggedValues
                java.util.List<String> packs = new java.util.ArrayList<>();
                NodeList pkgChildren = tvElem.getChildNodes();
                for (int j = 0; j < pkgChildren.getLength(); j++) {
                    org.w3c.dom.Node pkgChild = pkgChildren.item(j);
                    if (pkgChild.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        Element pkgElem = (Element) pkgChild;
                        if ("TaggedValue".equals(pkgElem.getTagName())
                                && "Package".equals(pkgElem.getAttribute("Tag"))) {
                            String pkg = pkgElem.getAttribute("TagValue");
                            if (pkg != null && !pkg.isEmpty()) {
                                packs.add(pkg.trim());
                            }
                        }
                    }
                }
                // Add to packages list
                graGra.getPackages().add(
                    new agg.util.Pair<>(tagValue, packs));
            } else if ("TypeGraphLevel".equals(tag)) {
                if ("ENABLED".equalsIgnoreCase(tagValue)) {
                    loadedLevel = TypeSet.ENABLED;
                } else if ("ENABLED_MAX".equalsIgnoreCase(tagValue)) {
                    loadedLevel = TypeSet.ENABLED_MAX;
                } else if ("ENABLED_MAX_MIN".equalsIgnoreCase(tagValue)) {
                    loadedLevel = TypeSet.ENABLED_MAX_MIN;
                } else {
                    loadedLevel = TypeSet.DISABLED;
                }
            } else if (tag != null && !tag.isEmpty() && !tag.equals("AttrHandler")) {
                // GraTra option
                if (tagValue != null && !tagValue.isEmpty()) {
                    if ("true".equalsIgnoreCase(tagValue)) {
                        options.add(tag);
                    }
                } else {
                    options.add(tag);
                }
            }
        }

        // Set options
        if (!options.isEmpty()) {
            graGra.setGraTraOptions(options);
        }

        // The type graph level is returned to the caller; it must be applied
        // AFTER the types are loaded (mirroring the legacy GraGra.XreadObject).
        return loadedLevel;
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

    /**
     * Checks whether a type element has AttrType children.
     */
    private static boolean hasAttrTypeChildren(Element typeElem) {
        if (typeElem == null) {
            return false;
        }
        org.w3c.dom.NodeList children = typeElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && "AttrType".equals(child.getNodeName())) {
                return true;
            }
        }
        return false;
    }
}
