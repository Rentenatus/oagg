/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.parser.ConflictsDependenciesContainer;
import agg.parser.CriticalPair;
import agg.parser.CriticalPairOption;
import agg.parser.DependencyPairContainer;
import agg.parser.ExcludePairContainer;
import agg.parser.ParserFactory;
import agg.util.Pair;
import agg.xt_basis.Arc;
import agg.xt_basis.BadMappingException;
import agg.xt_basis.BaseFactory;
import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xt_basis.GraphObject;
import agg.xt_basis.Node;
import agg.xt_basis.OrdinaryMorphism;
import agg.xt_basis.Rule;
import agg.xt_basis.Type;
import agg.xt_basis.TypeException;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;
import agg.xml.core.XMLSerializerContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Adapter for ConflictsDependenciesContainer objects with native DOM
 * serialization logic (.cpx format).
 *
 * <p>Serializes the container as a {@code <CriticalPairs>} element in the
 * legacy .cpx format, mirroring
 * {@code ConflictsDependenciesContainer.writeCriticalPairs}: the complete
 * grammar is embedded as a {@code <GraphTransformationSystem>} element
 * (reused from {@link GraGraAdapter}), followed by the
 * {@code <cpaOptions>} element, the {@code <conflictContainer>} /
 * {@code <conflictFreeContainer>} and {@code <dependencyContainer>} /
 * {@code <dependencyFreeContainer>} sections with their rule sets, the
 * computed critical pair entries ({@code <Rule R1>/<Rule R2>} with
 * {@code <Overlapping_Pair>} content, including the overlap morphisms for
 * plain, NAC and PAC overlaps), and finally the
 * {@code <ConflictDependencyGraph>} section when a CPA basis graph
 * exists.</p>
 *
 * <p>Deserialization mirrors
 * {@code ConflictsDependenciesContainer.XreadObject} for the same surface.
 * The overlap morphisms are reconstructed exactly like
 * {@code readOverlappingMorphisms}: NAC overlaps through
 * {@code BaseFactory.extendLeftGraphByNAC} / {@code extendRightGraphByNAC},
 * PAC overlaps through an isomorphic LHS copy plus
 * {@code completeDiagram2}.</p>
 *
 * <p><b>Scope limits:</b> old-style overlap morphisms without a
 * {@code source} attribute (the {@code readOldOverlappingMorphisms} variant)
 * are rejected with an explicit {@link XMLSerializationException}; the free
 * container sections are bound to their pair containers by tag name
 * ({@code conflictFreeContainer} to the conflict container,
 * {@code dependencyFreeContainer} to the dependency container); the
 * {@code <ConflictDependencyGraph>} section is only read when present,
 * unlike the legacy reader, which always creates an empty CPA basis
 * graph.</p>
 */
public class ConflictsDependenciesContainerAdapter
        extends DomainObjectAdapter<ConflictsDependenciesContainer> {

    /**
     * Creates a new adapter for the specified container.
     *
     * @param container The ConflictsDependenciesContainer to adapt
     */
    public ConflictsDependenciesContainerAdapter(ConflictsDependenciesContainer container) {
        super(container);
    }

    /**
     * Gets the adapted container.
     *
     * @return The ConflictsDependenciesContainer
     */
    public ConflictsDependenciesContainer getContainer() {
        return getDomainObject();
    }

    /**
     * Serializes this container into the serializer context.
     *
     * @param context The serializer context to write to
     * @throws XMLSerializationException if serialization fails
     */
    public void serialize(XMLSerializerContext context) throws XMLSerializationException {
        serialize(context, new DOMSerializationRegistry());
    }

    /**
     * Serializes this container into the serializer context using a
     * caller-provided registry.
     *
     * @param context  The serializer context to write to
     * @param registry The ID registry to use
     * @throws XMLSerializationException if serialization fails
     */
    public void serialize(XMLSerializerContext context, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        Element elem = serializeToElement(
            context.getCurrentElement().getOwnerDocument(), registry);
        if (elem != null) {
            context.getCurrentElement().appendChild(elem);
        }
    }

    /**
     * Serializes this container to a {@code <CriticalPairs>} DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The CriticalPairs element, or null if there is no grammar
     * @throws XMLSerializationException if serialization fails
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        ConflictsDependenciesContainer container = getContainer();
        if (container == null || container.getGrammar() == null) {
            return null;
        }

        ExcludePairContainer epc = container.getExcludePairContainer();
        DependencyPairContainer dpc = container.getDependencyPairContainer();

        Element cpElem = doc.createElement("CriticalPairs");
        cpElem.setAttribute("ID", registry.register(container));

        // Embedded grammar (mirrors writeGrammar)
        GraGraAdapter graAdapter = new GraGraAdapter(container.getGrammar());
        cpElem.appendChild(graAdapter.serializeGraphTransformationSystem(doc, registry));

        // cpaOptions: mirror writeCPAoptions. For containers restored from
        // a file the loaded options are written; for fresh in-memory
        // containers the current option fields are used.
        Element optionsElem = doc.createElement("cpaOptions");
        List<Pair<String, String>> options = container.getLoadedCPAOptions();
        if (options == null || options.isEmpty()) {
            if (epc != null) {
                options = epc.getCPAOptions();
            } else if (dpc != null) {
                options = dpc.getCPAOptions();
            }
        }
        if (options != null) {
            for (Pair<String, String> option : options) {
                if (option.first != null && option.second != null) {
                    optionsElem.setAttribute(option.first, option.second);
                }
            }
        }
        cpElem.appendChild(optionsElem);

        // conflictContainer / conflictFreeContainer
        if (epc != null) {
            Element conflictElem = doc.createElement("conflictContainer");
            conflictElem.setAttribute("kind", "exclude");
            appendRuleSet(conflictElem, "RuleSet", epc.getRules(), registry);
            appendRuleSet(conflictElem, "RuleSet2", epc.getRules2(), registry);
            appendExcludeEntries(conflictElem, epc, registry);
            cpElem.appendChild(conflictElem);

            if (epc.getConflictFreeContainer() != null) {
                Element conflictFreeElem = doc.createElement("conflictFreeContainer");
                appendFreeEntries(conflictFreeElem, epc, registry);
                cpElem.appendChild(conflictFreeElem);
            }
        }

        // dependencyContainer / dependencyFreeContainer
        if (dpc != null) {
            Element dependencyElem = doc.createElement("dependencyContainer");
            dependencyElem.setAttribute("kind",
                dpc.isSwitchDependencyEnabled()
                    ? "trigger_switch_dependency" : "trigger_dependency");
            appendRuleSet(dependencyElem, "RuleSet", dpc.getRules(), registry);
            appendRuleSet(dependencyElem, "RuleSet2", dpc.getRules2(), registry);
            appendExcludeEntries(dependencyElem, dpc, registry);
            cpElem.appendChild(dependencyElem);

            if (dpc.getConflictFreeContainer() != null) {
                Element dependencyFreeElem = doc.createElement("dependencyFreeContainer");
                appendFreeEntries(dependencyFreeElem, dpc, registry);
                cpElem.appendChild(dependencyFreeElem);
            }
        }

        // ConflictDependencyGraph (mirrors writeCPAGraph, only when set)
        appendCPAGraph(cpElem, container, registry);

        return cpElem;
    }

    /**
     * Appends the computed rule pair entries of the exclude container,
     * mirroring the Rule R1 / Rule R2 loops of writeCriticalPairs.
     */
    private void appendExcludeEntries(Element containerElem, ExcludePairContainer pc,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        Document doc = containerElem.getOwnerDocument();
        Map<Rule, Map<Rule, Pair<Boolean, List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>>>>> excludeContainer =
            pc.getExcludeContainer();
        if (excludeContainer == null) {
            return;
        }
        for (Rule r1 : excludeContainer.keySet()) {
            Element r1Elem = doc.createElement("Rule");
            r1Elem.setAttribute("R1", requireId(r1, registry));
            Map<Rule, Pair<Boolean, List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>>>> secondPart =
                excludeContainer.get(r1);
            for (Rule r2 : secondPart.keySet()) {
                Pair<Boolean, List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>>> p =
                    secondPart.get(r2);
                ExcludePairContainer.Entry entry = pc.getEntry(r1, r2);
                Element r2Elem = doc.createElement("Rule");
                r2Elem.setAttribute("R2", requireId(r2, registry));
                r2Elem.setAttribute("bool", String.valueOf(p.first));
                if (entry != null) {
                    r2Elem.setAttribute("duIndx", entry.getIndexOfDelUseProgress());
                    r2Elem.setAttribute("pfIndx", entry.getIndexOfProdForbidProgress());
                    r2Elem.setAttribute("caIndx", entry.getIndexOfChangeAttrProgress());
                }
                if (p.first.booleanValue() && p.second != null) {
                    for (Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>> p2i : p.second) {
                        r2Elem.appendChild(createOverlappingPair(doc, r1, r2, p2i, registry));
                    }
                }
                r1Elem.appendChild(r2Elem);
            }
            containerElem.appendChild(r1Elem);
        }
    }

    /**
     * Creates an Overlapping_Pair element with the overlap graph, its
     * critical objects and the two overlap morphisms.
     */
    private Element createOverlappingPair(Document doc, Rule r1, Rule r2,
            Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>> overlapping,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        Element overlapElem = doc.createElement("Overlapping_Pair");
        OrdinaryMorphism first = overlapping.first.first;
        Graph overlappingGraph = first.getImage();

        // add overlapping graph
        overlapElem.appendChild(new GraphAdapter(overlappingGraph)
            .serializeToElement(doc, registry));

        for (Iterator<Node> e = overlappingGraph.getNodesSet().iterator(); e.hasNext();) {
            GraphObject o = e.next();
            if (o.isCritical()) {
                appendCriticalObject(overlapElem, o, registry);
            }
        }
        for (Iterator<Arc> e = overlappingGraph.getArcsSet().iterator(); e.hasNext();) {
            GraphObject o = e.next();
            if (o.isCritical()) {
                appendCriticalObject(overlapElem, o, registry);
            }
        }

        writeOverlapMorphisms(overlapElem, r1, r2, overlapping, registry);
        return overlapElem;
    }

    private void appendCriticalObject(Element parent, GraphObject o,
            DOMSerializationRegistry registry) {
        Element criticalElem = parent.getOwnerDocument().createElement("Critical");
        criticalElem.setAttribute("object", requireId(o, registry));
        parent.appendChild(criticalElem);
    }

    /**
     * Appends a Mapping element with ID references (and an optional
     * pacname attribute for PAC entries).
     */
    private void appendMapping(Element morphismElem, GraphObject orig, GraphObject image,
            String pacname, DOMSerializationRegistry registry) {
        Element mappingElem = morphismElem.getOwnerDocument().createElement("Mapping");
        if (pacname != null) {
            mappingElem.setAttribute("pacname", pacname);
        }
        mappingElem.setAttribute("image", requireId(image, registry));
        mappingElem.setAttribute("orig", requireId(orig, registry));
        morphismElem.appendChild(mappingElem);
    }

    /**
     * Writes the two overlap morphisms of one overlapping pair, mirroring
     * writeOverlapMorphisms (including the NAC and PAC branches).
     */
    private void writeOverlapMorphisms(Element overlapElem, Rule r1, Rule r2,
            Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>> overlapping,
            DOMSerializationRegistry registry) {
        Document doc = overlapElem.getOwnerDocument();

        // write first (left) overlap morphism
        OrdinaryMorphism first = overlapping.first.first;
        Element firstElem = doc.createElement("Morphism");
        firstElem.setAttribute("name", first.getName());
        String firstSource = firstMorphismSource(first, r1, r2);
        if (firstSource != null) {
            firstElem.setAttribute("source", firstSource);
        }
        for (Iterator<GraphObject> e = first.getDomain(); e.hasNext();) {
            GraphObject s = e.next();
            appendMapping(firstElem, s, first.getImage(s), null, registry);
        }
        overlapElem.appendChild(firstElem);

        // write second (right) overlap morphism
        OrdinaryMorphism second = overlapping.first.second;
        Pair<OrdinaryMorphism, OrdinaryMorphism> p2 = overlapping.second;
        Element secondElem = doc.createElement("Morphism");
        secondElem.setAttribute("name", second.getName());
        if (p2 == null) {
            String source = null;
            if (second.getSource() == r2.getLeft()) {
                source = "LHS";
            } else if (second.getSource() == r1.getRight()) {
                source = "RHS_R1";
            } else if (second.getSource() == r1.getLeft()) {
                source = "LHS_R1_2";
            }
            if (source != null) {
                secondElem.setAttribute("source", source);
            }
            for (Iterator<GraphObject> e = second.getDomain(); e.hasNext();) {
                GraphObject s = e.next();
                appendMapping(secondElem, s, second.getImage(s), null, registry);
            }
        } else if (p2.second != null && p2.first.getTarget() == p2.second.getSource()) {
            // handle PACs
            OrdinaryMorphism embedPAC2 = p2.first.compose(p2.second);
            if (second.getSource() == r2.getLeft()) {
                secondElem.setAttribute("source", "PAC+LHS");
            } else if (second.getSource() == r1.getRight()) {
                secondElem.setAttribute("source", "PAC+RHS_R1");
            }
            OrdinaryMorphism pac = getPAC(r2, embedPAC2.getSource());
            List<GraphObject> pacgos = new ArrayList<GraphObject>();
            // write nodes and arcs of the overlap graph
            for (Iterator<Node> en = second.getTarget().getNodesSet().iterator(); en.hasNext();) {
                GraphObject go = en.next();
                if (!second.hasInverseImage(go)) {
                    if (embedPAC2.hasInverseImage(go)) {
                        pacgos.add(embedPAC2.firstOfInverseImage(go));
                    }
                } else {
                    appendMapping(secondElem, second.firstOfInverseImage(go), go, null, registry);
                }
            }
            for (Iterator<Arc> ea = second.getTarget().getArcsSet().iterator(); ea.hasNext();) {
                GraphObject go = ea.next();
                if (!second.hasInverseImage(go)) {
                    if (embedPAC2.hasInverseImage(go)) {
                        pacgos.add(embedPAC2.firstOfInverseImage(go));
                    }
                } else {
                    appendMapping(secondElem, second.firstOfInverseImage(go), go, null, registry);
                }
            }
            for (GraphObject s : pacgos) {
                GraphObject t = embedPAC2.getImage(s);
                if (t != null) {
                    // s belongs to a PAC, t belongs to the overlap graph
                    appendMapping(secondElem, s, t,
                        pac != null ? pac.getName() : null, registry);
                }
            }
        } else {
            // handle NACs
            OrdinaryMorphism morphL2iso = p2.first;
            OrdinaryMorphism morphNACiso = p2.second;
            if (morphL2iso.getSource() == r1.getRight()
                    || morphL2iso.getSource() == r2.getRight()) {
                secondElem.setAttribute("source", "NAC+RHS");
            } else {
                secondElem.setAttribute("source", "NAC+LHS");
            }
            for (Iterator<GraphObject> e = second.getDomain(); e.hasNext();) {
                GraphObject src = e.next();
                GraphObject t = second.getImage(src);
                GraphObject s = null;
                if (morphL2iso.hasInverseImage(src)) {
                    s = morphL2iso.firstOfInverseImage(src);
                } else if (morphNACiso.hasInverseImage(src)) {
                    s = morphNACiso.firstOfInverseImage(src);
                }
                if (s != null) {
                    appendMapping(secondElem, s, t, null, registry);
                }
            }
        }
        overlapElem.appendChild(secondElem);
    }

    /**
     * Computes the source attribute of the first overlap morphism,
     * mirroring the name-pattern dispatch of writeOverlapMorphisms. Returns
     * null when the legacy writer omits the attribute.
     */
    private String firstMorphismSource(OrdinaryMorphism first, Rule r1, Rule r2) {
        String targetName = first.getTarget().getName();
        if (targetName == null) {
            targetName = "";
        }
        if (targetName.indexOf("deliver-delete-dependency") >= 0
                && first.getSource() == r2.getLeft()) {
            return "LHS_R2";
        } else if (targetName.indexOf("forbid-produce-dependency") >= 0
                && first.getSource() == r2.getRight()) {
            return "RHS_R2";
        } else if ((targetName.indexOf("change-change-dependency") >= 0
                || targetName.indexOf("deliver-change-dependency") >= 0)
                && first.getSource() == r2.getLeft()) {
            return "LHS_R2";
        } else if (targetName.indexOf("-switch-") >= 0) {
            if (first.getSource() == r2.getLeft()) {
                return "LHS_R2";
            } else if (first.getSource() == r2.getRight()) {
                return "RHS_R2";
            }
            return null;
        } else if (targetName.indexOf("produceEdge-deleteNode-") >= 0) {
            if (first.getSource() == r2.getLeft()) {
                return "LHS_R2_1";
            }
            return null;
        } else {
            if (first.getSource() == r1.getLeft()) {
                return "LHS";
            } else if (first.getSource() == r1.getRight()) {
                return "RHS";
            }
            return null;
        }
    }

    private OrdinaryMorphism getPAC(final Rule r, final Graph pacGraph) {
        final List<OrdinaryMorphism> pacs = r.getPACsList();
        for (int l = 0; l < pacs.size(); l++) {
            final OrdinaryMorphism pac = pacs.get(l);
            if (pac.getTarget() == pacGraph) {
                return pac;
            }
        }
        return null;
    }

    /**
     * Appends the rule pair entries of a free container, mirroring the
     * conflictFreeContainer / dependencyFreeContainer loops of
     * writeCriticalPairs.
     */
    private void appendFreeEntries(Element freeElem, ExcludePairContainer pc,
            DOMSerializationRegistry registry) {
        Document doc = freeElem.getOwnerDocument();
        Map<Rule, Map<Rule, Pair<Boolean, List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>>>>> conflictFreeContainer =
            pc.getConflictFreeContainer();
        if (conflictFreeContainer == null) {
            return;
        }
        for (Rule r1 : conflictFreeContainer.keySet()) {
            Element r1Elem = doc.createElement("Rule");
            r1Elem.setAttribute("R1", requireId(r1, registry));
            Map<Rule, Pair<Boolean, List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>>>> secondPart =
                conflictFreeContainer.get(r1);
            for (Rule r2 : secondPart.keySet()) {
                ExcludePairContainer.Entry entry = pc.getEntry(r1, r2);
                Element r2Elem = doc.createElement("Rule");
                r2Elem.setAttribute("R2", requireId(r2, registry));
                r2Elem.setAttribute("bool", String.valueOf(secondPart.get(r2).first));
                if (entry != null
                        && entry.getStatus() == ExcludePairContainer.Entry.NOT_COMPUTABLE) {
                    r2Elem.setAttribute("status", "not_computable");
                }
                r1Elem.appendChild(r2Elem);
            }
            freeElem.appendChild(r1Elem);
        }
    }

    /**
     * Appends the ConflictDependencyGraph section, mirroring writeCPAGraph.
     * The section is only written when a CPA basis graph exists.
     */
    private void appendCPAGraph(Element cpElem, ConflictsDependenciesContainer container,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        Graph cpaBasisGraph = container.getCPABasisGraph();
        if (cpaBasisGraph == null) {
            return;
        }
        Document doc = cpElem.getOwnerDocument();
        Element cpaGraphElem = doc.createElement("ConflictDependencyGraph");
        Element typesElem = doc.createElement("Types");
        if (cpaBasisGraph.getTypeSet() != null) {
            de.jare.ndimcol.ref.IteratorWalker<Type> typeIter =
                cpaBasisGraph.getTypeSet().getTypeWalker();
            while (typeIter != null && typeIter.hasNext()) {
                Type type = typeIter.next();
                Element typeElem = serializeTypeElement(doc, type, registry);
                if (typeElem != null) {
                    typesElem.appendChild(typeElem);
                }
            }
        }
        cpaGraphElem.appendChild(typesElem);
        cpaGraphElem.appendChild(new GraphAdapter(cpaBasisGraph)
            .serializeToElement(doc, registry));
        cpElem.appendChild(cpaGraphElem);
    }

    private Element serializeTypeElement(Document doc, Type type,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        if (type instanceof agg.xt_basis.TypeImpl) {
            return new TypeImplAdapter((agg.xt_basis.TypeImpl) type)
                .serializeToElement(doc, registry);
        } else if (type instanceof agg.xt_basis.NodeTypeImpl) {
            return new NodeTypeImplAdapter((agg.xt_basis.NodeTypeImpl) type)
                .serializeToElement(doc, registry);
        } else if (type instanceof agg.xt_basis.ArcTypeImpl) {
            return new ArcTypeImplAdapter((agg.xt_basis.ArcTypeImpl) type)
                .serializeToElement(doc, registry);
        }
        return null;
    }

    private String requireId(Object obj, DOMSerializationRegistry registry) {
        String id = registry.getId(obj);
        if (id == null || id.isEmpty()) {
            id = registry.register(obj);
        }
        return id;
    }

    /**
     * Appends a rule set element (size + rule ID references), mirroring
     * writeRuleSet.
     */
    private void appendRuleSet(Element parent, String tagname, List<Rule> ruleSet,
            DOMSerializationRegistry registry) {
        Element setElem = parent.getOwnerDocument().createElement(tagname);
        setElem.setAttribute("size", String.valueOf(ruleSet.size()));
        for (int i = 0; i < ruleSet.size(); i++) {
            String ruleId = registry.getId(ruleSet.get(i));
            if (ruleId.isEmpty()) {
                ruleId = registry.register(ruleSet.get(i));
            }
            setElem.setAttribute("i" + i, ruleId);
        }
        parent.appendChild(setElem);
    }

    /**
     * Deserializes this container from the deserializer context, using a
     * fresh ID registry.
     *
     * @param context The deserializer context to read from
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserialize(XMLDeserializerContext context) throws XMLSerializationException {
        deserialize(context, new DOMSerializationRegistry());
    }

    /**
     * Deserializes this container from the deserializer context using a
     * caller-provided registry.
     *
     * @param context  The deserializer context to read from
     * @param registry The ID registry to use
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserialize(XMLDeserializerContext context, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        Element contextElement = context.getCurrentElement();
        Element cpElement = findChildElement(contextElement, "CriticalPairs");
        if (cpElement == null) {
            cpElement = findChildElement(
                contextElement.getOwnerDocument().getDocumentElement(),
                "CriticalPairs");
        }
        if (cpElement == null) {
            throw new XMLSerializationException(
                "CriticalPairs element not found in document");
        }
        deserializeFromElement(cpElement, registry);
    }

    /**
     * Deserializes a ConflictsDependenciesContainer from a
     * {@code <CriticalPairs>} DOM element.
     *
     * @param cpElem  The CriticalPairs DOM element
     * @param registry The ID registry for cross-references
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserializeFromElement(Element cpElem, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        ConflictsDependenciesContainer container = getContainer();
        if (container == null || cpElem == null) {
            return;
        }

        String id = cpElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(container, id);
        }

        // Embedded grammar (mirrors readGrammar, except that
        // prepareRuleInfo is deliberately NOT called: it regenerates the
        // formula attribute of rules with nested ACs (true -> 1, 1&2, ...),
        // and the DOM path preserves the file values instead, like the
        // .ggx load path does)
        GraGra grammar = agg.xt_basis.BaseFactory.theFactory().createGraGra();
        Element gtsElem = findChildElement(cpElem, "GraphTransformationSystem");
        if (gtsElem != null) {
            GraGraAdapter graAdapter = new GraGraAdapter(grammar);
            graAdapter.deserializeFromElement(gtsElem, registry);
        }
        boolean layered = grammar.isLayered();
        boolean priority = grammar.trafoByPriority();

        // cpaOptions
        Element optionsElem = findChildElement(cpElem, "cpaOptions");
        if (optionsElem != null) {
            NamedNodeMap attrs = optionsElem.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
                container.addLoadedCPAOption(attr.getName(), attr.getValue());
            }
        }

        ExcludePairContainer epc = null;
        DependencyPairContainer dpc = null;
        List<Rule> conflictRules = null;
        List<Rule> conflictRules2 = null;
        List<Rule> dependencyRules = null;
        List<Rule> dependencyRules2 = null;

        // conflictContainer
        Element conflictElem = findChildElement(cpElem, "conflictContainer");
        if (conflictElem == null) {
            conflictElem = findChildElement(cpElem, "conflictsContainer");
            if (conflictElem == null) {
                conflictElem = findChildElement(cpElem, "excludeContainer");
            }
        }
        if (conflictElem != null) {
            epc = (ExcludePairContainer) ParserFactory.createEmptyCriticalPairs(
                grammar, CriticalPairOption.EXCLUDEONLY, layered);
            // the container pre-fills its rule lists from the grammar;
            // the file's rule sets replace them (mirrors resetRules)
            List<Rule> tmpRules = new Vector<Rule>();
            List<Rule> tmpRules2 = new Vector<Rule>();
            if (deserializeRuleSet(conflictElem, "RuleSet", tmpRules, registry)) {
                conflictRules = tmpRules;
            }
            if (deserializeRuleSet(conflictElem, "RuleSet2", tmpRules2, registry)) {
                conflictRules2 = tmpRules2;
            }
            readExcludeEntries(conflictElem, epc, grammar, registry, true);
        }

        // conflictFreeContainer
        Element conflictFreeElem = findChildElement(cpElem, "conflictFreeContainer");
        if (conflictFreeElem != null && epc != null) {
            readFreeEntries(conflictFreeElem, epc, registry, false, layered, priority);
        }

        // dependencyContainer
        Element dependencyElem = findChildElement(cpElem, "dependencyContainer");
        if (dependencyElem == null) {
            dependencyElem = findChildElement(cpElem, "dependenciesContainer");
        }
        if (dependencyElem != null) {
            boolean switchDependency =
                "trigger_switch_dependency".equals(dependencyElem.getAttribute("kind"));
            dpc = (DependencyPairContainer) ParserFactory.createEmptyCriticalPairs(
                grammar,
                switchDependency
                    ? CriticalPair.TRIGGER_SWITCH_DEPENDENCY
                    : CriticalPair.TRIGGER_DEPENDENCY,
                layered);
            dpc.enableSwitchDependency(switchDependency);
            List<Rule> tmpRules = new Vector<Rule>();
            List<Rule> tmpRules2 = new Vector<Rule>();
            if (deserializeRuleSet(dependencyElem, "RuleSet", tmpRules, registry)) {
                dependencyRules = tmpRules;
            }
            if (deserializeRuleSet(dependencyElem, "RuleSet2", tmpRules2, registry)) {
                dependencyRules2 = tmpRules2;
            }
            // mirrors the legacy reader, which restores the progress
            // indices only for the first container section
            readExcludeEntries(dependencyElem, dpc, grammar, registry, false);
        }

        // dependencyFreeContainer
        Element dependencyFreeElem = findChildElement(cpElem, "dependencyFreeContainer");
        if (dependencyFreeElem != null && dpc != null) {
            readFreeEntries(dependencyFreeElem, dpc, registry, true, layered, priority);
        }

        // read CPA rule graph (only when the section is present)
        readCPAGraph(cpElem, container, registry);

        // reset rule sets (rules and rules2) of the containers,
        // mirroring the resetRules calls at the end of XreadObject
        if (epc != null) {
            epc.resetRules(conflictRules, conflictRules2);
        }
        if (dpc != null) {
            dpc.resetRules(dependencyRules, dependencyRules2);
        }

        container.setLoadedContainers(epc, dpc, layered, priority);
    }

    /**
     * Reads a rule set element (size + rule ID references), mirroring
     * readRuleSet. Returns true when the element was found and parsed.
     */
    private boolean deserializeRuleSet(Element parent, String tagname, List<Rule> ruleSet,
            DOMSerializationRegistry registry) {
        Element setElem = findChildElement(parent, tagname);
        if (setElem == null) {
            return false;
        }
        String sizeStr = setElem.getAttribute("size");
        try {
            int size = Integer.parseInt(sizeStr);
            for (int i = 0; i < size; i++) {
                Object rule = registry.getObject(setElem.getAttribute("i" + i));
                if (rule instanceof Rule) {
                    ruleSet.add((Rule) rule);
                }
            }
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Reads the computed rule pair entries of a container section,
     * mirroring the Rule R1 / Rule R2 loops of XreadObject. The progress
     * index attributes are restored for the first container section only,
     * like the legacy reader.
     */
    private void readExcludeEntries(Element containerElem, ExcludePairContainer pc,
            GraGra grammar, DOMSerializationRegistry registry, boolean readProgress)
            throws XMLSerializationException {
        List<Element> r1Elems = childElements(containerElem, "Rule");
        if (r1Elems.isEmpty()) {
            r1Elems = childElements(containerElem, "Regel");
        }
        for (Element r1Elem : r1Elems) {
            Rule r1 = (Rule) registry.getObject(r1Elem.getAttribute("R1"));
            if (r1 == null) {
                continue;
            }
            List<Element> r2Elems = childElements(r1Elem, "Rule");
            if (r2Elems.isEmpty()) {
                r2Elems = childElements(r1Elem, "Regel");
            }
            for (Element r2Elem : r2Elems) {
                Rule r2 = (Rule) registry.getObject(r2Elem.getAttribute("R2"));
                if (r2 == null) {
                    continue;
                }
                boolean b = "true".equals(r2Elem.getAttribute("bool"));
                String duIndxStr = r2Elem.getAttribute("duIndx");
                String pfIndxStr = r2Elem.getAttribute("pfIndx");
                String caIndxStr = r2Elem.getAttribute("caIndx");
                List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> allOverlappings = null;
                if (b) {
                    allOverlappings = new Vector<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>>();
                    for (Element overlapElem : childElements(r2Elem, "Overlapping_Pair")) {
                        Graph overlapGraph =
                            agg.xt_basis.BaseFactory.theFactory().createGraph(
                                grammar.getTypeSet());
                        Element graphElem = findChildElement(overlapElem, "Graph");
                        if (graphElem == null) {
                            throw new XMLSerializationException(
                                "Overlapping_Pair without an overlap graph element");
                        }
                        new GraphAdapter(overlapGraph)
                            .deserializeFromElement(graphElem, registry);
                        for (Element criticalElem : childElements(overlapElem, "Critical")) {
                            Object o = registry.getObject(criticalElem.getAttribute("object"));
                            if (o instanceof GraphObject) {
                                ((GraphObject) o).setCritical(true);
                            }
                        }
                        allOverlappings.add(
                            readOverlappingMorphisms(overlapElem, r1, r2, overlapGraph, registry));
                    }
                }
                pc.addQuadruple(pc.getExcludeContainer(), r1, r2, b, allOverlappings);
                ExcludePairContainer.Entry entry = pc.getEntry(r1, r2, true);
                if (entry != null && readProgress) {
                    if (duIndxStr.indexOf(':') >= 0) {
                        entry.setIndexOfDelUseProgress(duIndxStr);
                    } else {
                        entry.setIndexOfDelUseProgress(duIndxStr.concat(":"));
                    }
                    if (pfIndxStr.indexOf(':') >= 0) {
                        entry.setIndexOfProdForbidProgress(pfIndxStr);
                    } else {
                        entry.setIndexOfProdForbidProgress(pfIndxStr.concat(":"));
                    }
                    if (caIndxStr.indexOf(':') >= 0) {
                        entry.setIndexOfChangeAttrProgress(caIndxStr);
                    } else {
                        entry.setIndexOfChangeAttrProgress(caIndxStr.concat(":"));
                    }
                }
            }
        }
    }

    /**
     * Reads the rule pair entries of a free container section, mirroring
     * the conflictFreeContainer / dependencyFreeContainer loops of
     * XreadObject, including the DISABLED and NOT_RELATED entry states.
     */
    private void readFreeEntries(Element freeElem, ExcludePairContainer pc,
            DOMSerializationRegistry registry, boolean dependencyFree,
            boolean layered, boolean priority) {
        List<Element> r1Elems = childElements(freeElem, "Rule");
        if (r1Elems.isEmpty()) {
            r1Elems = childElements(freeElem, "Regel");
        }
        for (Element r1Elem : r1Elems) {
            Rule r1 = (Rule) registry.getObject(r1Elem.getAttribute("R1"));
            if (r1 == null) {
                continue;
            }
            List<Element> r2Elems = childElements(r1Elem, "Rule");
            if (r2Elems.isEmpty()) {
                r2Elems = childElements(r1Elem, "Regel");
            }
            for (Element r2Elem : r2Elems) {
                Rule r2 = (Rule) registry.getObject(r2Elem.getAttribute("R2"));
                if (r2 == null) {
                    continue;
                }
                boolean b = "true".equals(r2Elem.getAttribute("bool"));
                String status = r2Elem.getAttribute("status");
                pc.addQuadruple(pc.getConflictFreeContainer(), r1, r2, b, null);
                ExcludePairContainer.Entry entry = pc.getEntry(r1, r2, true);
                if (dependencyFree && entry != null
                        && "not_computable".equals(status)) {
                    entry.setStatus(ExcludePairContainer.Entry.NOT_COMPUTABLE);
                }
                // mirror the DISABLED / NOT_RELATED states of the legacy reader
                ExcludePairContainer.Entry stateEntry = pc.getEntry(r1, r2);
                if (stateEntry == null) {
                    continue;
                }
                if (!r1.isEnabled() || !r2.isEnabled()) {
                    stateEntry.setLoadedState(ExcludePairContainer.Entry.DISABLED);
                } else {
                    if (layered && r1.getLayer() != r2.getLayer()) {
                        stateEntry.setLoadedState(ExcludePairContainer.Entry.NOT_RELATED);
                    }
                    if (priority && r1.getPriority() != r2.getPriority()) {
                        stateEntry.setLoadedState(ExcludePairContainer.Entry.NOT_RELATED);
                    }
                }
            }
        }
    }

    /**
     * Reconstructs the two overlap morphisms of one Overlapping_Pair,
     * mirroring readOverlappingMorphisms (including the NAC and PAC
     * branches).
     */
    private Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>
            readOverlappingMorphisms(Element overlapElem, Rule r1, Rule r2,
                    Graph overlapGraph, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        List<Element> morphismElems = childElements(overlapElem, "Morphism");
        if (morphismElems.isEmpty()) {
            throw new XMLSerializationException(
                "Overlapping_Pair needs at least one Morphism element");
        }

        // read first overlap morphism
        Element firstElem = morphismElems.get(0);
        String firstName = firstElem.getAttribute("name");
        String firstSource = firstElem.getAttribute("source");
        if (firstSource.isEmpty()) {
            throw new XMLSerializationException(
                "Old-style Overlapping_Pair morphisms without a source"
                    + " attribute are not supported by the DOM path");
        }
        OrdinaryMorphism first;
        if ("LHS".equals(firstSource)) {
            first = BaseFactory.theFactory().createMorphism(r1.getLeft(), overlapGraph);
        } else if ("RHS".equals(firstSource)) {
            first = BaseFactory.theFactory().createMorphism(r1.getRight(), overlapGraph);
        } else if ("LHS_R2".equals(firstSource)) {
            first = BaseFactory.theFactory().createMorphism(r2.getLeft(), overlapGraph);
        } else if ("RHS_R2".equals(firstSource)) {
            first = BaseFactory.theFactory().createMorphism(r2.getRight(), overlapGraph);
        } else if ("LHS_R2_1".equals(firstSource)) {
            first = BaseFactory.theFactory().createMorphism(r2.getLeft(), overlapGraph);
        } else {
            throw new XMLSerializationException(
                "Unknown source of the first overlap morphism: " + firstSource);
        }
        first.setName(firstName.replaceAll(" ", ""));
        for (Element mappingElem : childElements(firstElem, "Mapping")) {
            GraphObject o = (GraphObject) registry.getObject(mappingElem.getAttribute("orig"));
            GraphObject i = (GraphObject) registry.getObject(mappingElem.getAttribute("image"));
            if (o != null && i != null) {
                addOverlapMapping(first, o, i, "first");
            }
        }

        if (morphismElems.size() < 2) {
            throw new XMLSerializationException(
                "Overlapping_Pair needs a second Morphism element (found: "
                    + morphismElems.size() + ")");
        }

        // read second overlap morphism
        Element secondElem = morphismElems.get(1);
        String secondName = secondElem.getAttribute("name");
        String source = secondElem.getAttribute("source");
        OrdinaryMorphism second = null;
        OrdinaryMorphism morphL2iso = null;
        OrdinaryMorphism morphNACiso = null;
        OrdinaryMorphism morphL2PACiso = null;
        OrdinaryMorphism embedPac = null;
        OrdinaryMorphism pac = null;
        final Map<GraphObject, GraphObject> orig2copy = new HashMap<GraphObject, GraphObject>();
        if ("LHS".equals(source)) {
            second = BaseFactory.theFactory().createMorphism(r2.getLeft(), overlapGraph);
        } else if ("RHS_R1".equals(source)) {
            second = BaseFactory.theFactory().createMorphism(r1.getRight(), overlapGraph);
        } else if ("LHS_R1_2".equals(source)) {
            second = BaseFactory.theFactory().createMorphism(r1.getLeft(), overlapGraph);
        } else if ("NAC+LHS".equals(source) || "NAC+RHS".equals(source)) {
            OrdinaryMorphism nac = null;
            if (overlapGraph.getName().indexOf("forbid-switch-") >= 0
                    || overlapGraph.getName().indexOf("forbid-produce-dependency") >= 0) {
                for (OrdinaryMorphism n : r1.getNACsList()) {
                    if (overlapGraph.getHelpInfoAboutNAC().indexOf(n.getName()) != -1) {
                        nac = n;
                        break;
                    }
                }
                Pair<OrdinaryMorphism, OrdinaryMorphism> nacRhs =
                    BaseFactory.theFactory().extendRightGraphByNAC(r1, nac);
                if (nacRhs != null) {
                    morphL2iso = nacRhs.first;
                    morphNACiso = nacRhs.second;
                }
            } else {
                for (OrdinaryMorphism n : r2.getNACsList()) {
                    if (overlapGraph.getHelpInfoAboutNAC().indexOf(n.getName()) != -1) {
                        nac = n;
                        break;
                    }
                }
                Pair<OrdinaryMorphism, OrdinaryMorphism> nacLhs =
                    BaseFactory.theFactory().extendLeftGraphByNAC(r2, nac);
                if (nacLhs != null) {
                    morphL2iso = nacLhs.first;
                    morphNACiso = nacLhs.second;
                }
            }
            if (morphL2iso != null) {
                second = BaseFactory.theFactory().createMorphism(
                    morphL2iso.getTarget(), overlapGraph);
            }
        } else if ("PAC+LHS".equals(source)) {
            final List<OrdinaryMorphism> pacs = r2.getPACsList();
            for (int i = 0; i < pacs.size(); i++) {
                OrdinaryMorphism pa = pacs.get(i);
                if (overlapGraph.getHelpInfoAboutPAC().equals(pa.getName())) {
                    pac = pa;
                    break;
                }
            }
            if (pac != null) {
                morphL2iso = r2.getLeft().isoCopy();
                morphL2PACiso = BaseFactory.theFactory().createMorphism(
                    morphL2iso.getTarget(), overlapGraph);
                embedPac = BaseFactory.theFactory().createMorphism(
                    pac.getTarget(), morphL2PACiso.getSource());
            }
            second = BaseFactory.theFactory().createMorphism(r2.getLeft(), overlapGraph);
        } else if ("PAC+RHS_R1".equals(source)) {
            morphL2iso = r1.getRight().isomorphicCopy();
            if (morphL2iso != null) {
                morphL2PACiso = BaseFactory.theFactory().createMorphism(
                    morphL2iso.getTarget(), overlapGraph);
            }
            // NOTE: r1.getRight() == morphL2iso.getSource()
            second = BaseFactory.theFactory().createMorphism(r1.getRight(), overlapGraph);
        } else {
            throw new XMLSerializationException(
                "Unknown or missing source of the second overlap morphism: " + source);
        }

        Pair<OrdinaryMorphism, OrdinaryMorphism> p2 = null;
        if (second != null) {
            second.setName(secondName.replaceAll(" ", ""));
            for (Element mappingElem : childElements(secondElem, "Mapping")) {
                String pacname = mappingElem.getAttribute("pacname");
                GraphObject o = (GraphObject) registry.getObject(mappingElem.getAttribute("orig"));
                GraphObject i = (GraphObject) registry.getObject(mappingElem.getAttribute("image"));
                if (o == null || i == null) {
                    continue;
                }
                if ("LHS".equals(source)
                        || "RHS_R1".equals(source)
                        || "LHS_R1_2".equals(source)) {
                    addOverlapMapping(second, o, i, "second");
                } else if ("NAC+LHS".equals(source) || "NAC+RHS".equals(source)) {
                    GraphObject s = null;
                    if (morphL2iso != null) {
                        s = morphL2iso.getImage(o);
                    }
                    if (s == null && morphNACiso != null) {
                        s = morphNACiso.getImage(o);
                    }
                    if (s != null) {
                        addOverlapMapping(second, s, i, "second");
                    }
                } else if ("PAC+LHS".equals(source)) {
                    if (pacname == null || pacname.length() == 0) {
                        addOverlapMapping(second, o, i, "second");
                        if (o.isNode() && morphL2iso != null) {
                            orig2copy.put(o, morphL2iso.getImage(o));
                        }
                    } else {
                        if (o.isNode() && morphL2iso != null) {
                            try {
                                Node n = morphL2iso.getTarget().copyNode((Node) o);
                                try {
                                    if (morphL2PACiso != null) {
                                        morphL2PACiso.addMapping(n, i);
                                        orig2copy.put(o, n);
                                        embedPac.addMapping(o, n);
                                    }
                                } catch (BadMappingException bme) {
                                }
                            } catch (TypeException te) {
                            }
                        } else {
                            try {
                                Node src = (Node) orig2copy.get(((Arc) o).getSource());
                                Node tar = (Node) orig2copy.get(((Arc) o).getTarget());
                                if (src != null && tar != null && morphL2iso != null) {
                                    Arc a = morphL2iso.getTarget().copyArc((Arc) o, src, tar);
                                    try {
                                        if (morphL2PACiso != null) {
                                            morphL2PACiso.addMapping(a, i);
                                            embedPac.addMapping(o, a);
                                        }
                                    } catch (BadMappingException bme) {
                                    }
                                }
                            } catch (TypeException te) {
                            }
                        }
                    }
                } else if ("PAC+RHS_R1".equals(source)) {
                    if (pacname == null || pacname.length() == 0) {
                        addOverlapMapping(second, o, i, "second");
                    }
                }
            }
            if ("NAC+LHS".equals(source) || "NAC+RHS".equals(source)) {
                p2 = new Pair<OrdinaryMorphism, OrdinaryMorphism>(morphL2iso, morphNACiso);
            } else if ("PAC+LHS".equals(source)) {
                if (embedPac != null) {
                    embedPac.completeDiagram2(pac, morphL2iso);
                }
                p2 = new Pair<OrdinaryMorphism, OrdinaryMorphism>(embedPac, morphL2PACiso);
            } else if ("PAC+RHS_R1".equals(source)) {
                p2 = new Pair<OrdinaryMorphism, OrdinaryMorphism>(morphL2iso, morphL2PACiso);
            }
        } else {
            throw new XMLSerializationException(
                "Failed to reconstruct the second overlap morphism (source: "
                    + source + ")");
        }

        return new Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>(
            new Pair<OrdinaryMorphism, OrdinaryMorphism>(first, second), p2);
    }

    private void addOverlapMapping(OrdinaryMorphism morph, GraphObject o, GraphObject i,
            String role) {
        try {
            morph.addMapping(o, i);
        } catch (BadMappingException ex) {
            System.out.println("ConflictsDependenciesContainerAdapter"
                + ".readOverlappingMorphisms:: (" + role + ") "
                + ex.getLocalizedMessage());
        }
    }

    /**
     * Reads the ConflictDependencyGraph section, mirroring readCPAGraph.
     * Unlike the legacy reader, no empty CPA basis graph is created when
     * the section is absent.
     */
    private void readCPAGraph(Element cpElem, ConflictsDependenciesContainer container,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        Element cpaGraphElem = findChildElement(cpElem, "ConflictDependencyGraph");
        if (cpaGraphElem == null) {
            return;
        }
        Graph cpaBasisGraph = new Graph();
        Element typesElem = findChildElement(cpaGraphElem, "Types");
        if (typesElem != null) {
            for (Element typeElem : childElements(typesElem, "NodeType")) {
                Type t = cpaBasisGraph.getTypeSet().createNodeType(false);
                deserializeType(typeElem, t, registry);
                if (t.getAdditionalRepr().equals("")) {
                    t.setAdditionalRepr("[NODE]");
                }
            }
            for (Element typeElem : childElements(typesElem, "EdgeType")) {
                Type t = cpaBasisGraph.getTypeSet().createArcType(false);
                deserializeType(typeElem, t, registry);
                if (t.getAdditionalRepr().equals("")) {
                    t.setAdditionalRepr("[EDGE]");
                }
            }
        }
        Element graphElem = findChildElement(cpaGraphElem, "Graph");
        if (graphElem != null) {
            new GraphAdapter(cpaBasisGraph).deserializeFromElement(graphElem, registry);
        }
        // improve old CPA Graph name
        String gn = cpaBasisGraph.getName();
        if (gn != null && gn.contains("ofRules")) {
            cpaBasisGraph.setName("CPA_RuleGraph:Conflicts_(red)-Dependencies_(blue)");
        }
        container.setCPABasisGraph(cpaBasisGraph);
    }

    private void deserializeType(Element typeElem, Type type,
            DOMSerializationRegistry registry) {
        GraGraAdapter.loadTypeDetails(type, typeElem, registry);
    }

    private static List<Element> childElements(Element parent, String name) {
        List<Element> result = new ArrayList<Element>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && name.equals(child.getNodeName())) {
                result.add((Element) child);
            }
        }
        return result;
    }

    private static Element findChildElement(org.w3c.dom.Node parent, String name) {
        if (!(parent instanceof Element)) {
            return null;
        }
        NodeList children = parent.getChildNodes();
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
