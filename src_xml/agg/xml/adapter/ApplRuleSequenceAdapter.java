/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.ruleappl.ApplRuleSequence;
import agg.ruleappl.RuleSequence;
import agg.util.Pair;
import agg.xt_basis.GraGra;
import agg.xt_basis.Rule;
import agg.xml.core.XMLDeserializerContext;
import agg.xml.core.XMLSerializationException;
import agg.xml.core.XMLSerializerContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Map;

/**
 * Adapter for ApplRuleSequence objects with native DOM serialization logic.
 *
 * <p>Serializes an ApplRuleSequence as a {@code <RuleSequenceApplicability>}
 * DOM element in the legacy .rsx format: the complete grammar is embedded as
 * a {@code <GraphTransformationSystem>} element (reused from
 * {@link GraGraAdapter}), followed by a {@code <RuleSequences>} section with
 * one {@code <Sequence>} per rule sequence carrying the concurrent rule
 * settings, the applicability items, the start graph reference and the
 * per-rule results.</p>
 *
 * <p>Deserialization mirrors {@code ApplRuleSequence.XreadObject}: the
 * embedded grammar is loaded through {@link GraGraAdapter}, the sequences of
 * the container are rebound to the grammar's sequences, and the
 * {@code <Sequence>} elements update the matching sequences by name
 * (creating missing ones with the container's critical pair option).</p>
 */
public class ApplRuleSequenceAdapter extends DomainObjectAdapter<ApplRuleSequence> {

    /**
     * Creates a new adapter for the specified ApplRuleSequence.
     *
     * @param sequence The ApplRuleSequence to adapt
     */
    public ApplRuleSequenceAdapter(ApplRuleSequence sequence) {
        super(sequence);
    }

    /**
     * Gets the adapted ApplRuleSequence instance.
     *
     * @return The ApplRuleSequence
     */
    public ApplRuleSequence getApplRuleSequence() {
        return getDomainObject();
    }

    /**
     * Serializes this rule sequence applicability into the serializer
     * context (the DOM document root).
     *
     * @param context The serializer context to write to
     * @throws XMLSerializationException if serialization fails
     */
    public void serialize(XMLSerializerContext context) throws XMLSerializationException {
        serialize(context, new DOMSerializationRegistry());
    }

    /**
     * Serializes this rule sequence applicability into the serializer
     * context using a caller-provided registry.
     *
     * @param context  The serializer context to write to
     * @param registry The ID registry to use
     * @throws XMLSerializationException if serialization fails
     */
    public void serialize(XMLSerializerContext context, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        Element rsaElem = serializeToElement(
            context.getCurrentElement().getOwnerDocument(), registry);
        if (rsaElem != null) {
            context.getCurrentElement().appendChild(rsaElem);
        }
    }

    /**
     * Serializes this rule sequence applicability to a
     * {@code <RuleSequenceApplicability>} DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The RuleSequenceApplicability element, or null if there is
     *         no grammar to embed
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        ApplRuleSequence applSeq = getApplRuleSequence();
        if (applSeq == null || applSeq.getGraGra() == null) {
            return null;
        }

        Element rsaElem = doc.createElement("RuleSequenceApplicability");
        rsaElem.setAttribute("ID", registry.register(applSeq));

        // Embedded grammar (mirrors h.addObject("GraGra", gragra, true))
        GraGraAdapter graAdapter = new GraGraAdapter(applSeq.getGraGra());
        Element gtsElem = graAdapter.serializeGraphTransformationSystem(doc, registry);
        rsaElem.appendChild(gtsElem);

        // RuleSequences section (mirrors ApplRuleSequence.XwriteObject)
        Element sequencesElem = doc.createElement("RuleSequences");
        for (RuleSequence seq : applSeq.getRuleSequences()) {
            sequencesElem.appendChild(serializeSequence(seq, doc, registry));
        }
        rsaElem.appendChild(sequencesElem);

        return rsaElem;
    }

    /**
     * Serializes one rule sequence with its settings and results.
     */
    private Element serializeSequence(RuleSequence seq, Document doc,
            DOMSerializationRegistry registry) {
        Element seqElem = doc.createElement("Sequence");
        seqElem.setAttribute("name", seq.getName() != null ? seq.getName() : "");

        // ConcurrentRule settings
        Element concurrentElem = doc.createElement("ConcurrentRule");
        if (seq.getDepthOfConcurrentRule() == -1) {
            concurrentElem.setAttribute("depth", "undefined");
        } else {
            concurrentElem.setAttribute("depth",
                String.valueOf(seq.getDepthOfConcurrentRule()));
        }
        concurrentElem.setAttribute("complete",
            String.valueOf(seq.getCompleteConcurrency()));
        concurrentElem.setAttribute("completecpa",
            String.valueOf(seq.getCompleteCPAOfConcurrency()));
        concurrentElem.setAttribute("ignoredanglingedge",
            String.valueOf(seq.getIgnoreDanglingEdgeOfDelNode()));
        seqElem.appendChild(concurrentElem);

        // Main applicability results
        Pair<Boolean, String> appl = seq.getApplicabilityResult();
        Pair<Boolean, String> nonAppl = seq.getNonApplicabilityResult();
        appendItem(seqElem, "applicable",
            appl != null ? appl.first : Boolean.FALSE,
            appl != null ? appl.second : null);
        appendItem(seqElem, "nonapplicable",
            nonAppl != null ? nonAppl.first : Boolean.FALSE,
            nonAppl != null ? nonAppl.second : null);

        // Start graph reference
        if (seq.getGraph() != null) {
            Element graphElem = doc.createElement("Graph");
            String graphId = registry.getId(seq.getGraph());
            if (graphId.isEmpty()) {
                graphId = registry.register(seq.getGraph());
            }
            graphElem.setAttribute("id", graphId);
            seqElem.appendChild(graphElem);
        }

        // Per-rule results
        Map<String, Pair<Boolean, List<String>>> ruleRes = seq.getRuleResults();
        List<Rule> rules = seq.getRules();
        for (int j = 0; j < rules.size(); j++) {
            Rule rule = rules.get(j);
            Element ruleElem = doc.createElement("Rule");
            String ruleId = registry.getId(rule);
            if (ruleId.isEmpty()) {
                ruleId = registry.register(rule);
            }
            ruleElem.setAttribute("id", ruleId);
            if (ruleRes != null) {
                for (String key : ruleRes.keySet()) {
                    if (key.indexOf(String.valueOf(j).concat(rule.getName())) == 0) {
                        Pair<Boolean, List<String>> resultPair = ruleRes.get(key);
                        if (resultPair != null) {
                            Element itemElem = doc.createElement("Item");
                            itemElem.setAttribute("result",
                                String.valueOf(resultPair.first));
                            itemElem.setAttribute("criterion", resultPair.second.get(0));
                            itemElem.setAttribute("text", resultPair.second.get(1));
                            ruleElem.appendChild(itemElem);
                        }
                    }
                }
            } else {
                ruleElem.setAttribute("result", "true");
                ruleElem.setAttribute("criterion", "undefined");
                ruleElem.setAttribute("text", "undefined");
            }
            seqElem.appendChild(ruleElem);
        }
        return seqElem;
    }

    private void appendItem(Element parent, String kind, Boolean result, String criterion) {
        Element itemElem = parent.getOwnerDocument().createElement("Item");
        itemElem.setAttribute("kind", kind);
        itemElem.setAttribute("result", String.valueOf(result));
        if (criterion != null) {
            itemElem.setAttribute("criterion", criterion);
        }
        parent.appendChild(itemElem);
    }

    /**
     * Deserializes this rule sequence applicability from the deserializer
     * context, using a fresh ID registry.
     *
     * @param context The deserializer context to read from
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserialize(XMLDeserializerContext context) throws XMLSerializationException {
        deserialize(context, new DOMSerializationRegistry());
    }

    /**
     * Deserializes this rule sequence applicability from the deserializer
     * context using a caller-provided registry.
     *
     * @param context  The deserializer context to read from
     * @param registry The ID registry to use
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserialize(XMLDeserializerContext context, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        Element contextElement = context.getCurrentElement();
        Element rsaElement = findChildElement(contextElement, "RuleSequenceApplicability");
        if (rsaElement == null) {
            rsaElement = findChildElement(
                contextElement.getOwnerDocument().getDocumentElement(),
                "RuleSequenceApplicability");
        }
        if (rsaElement == null) {
            throw new XMLSerializationException(
                "RuleSequenceApplicability element not found in document");
        }
        deserializeFromElement(rsaElement, registry);
    }

    /**
     * Deserializes an ApplRuleSequence from a
     * {@code <RuleSequenceApplicability>} DOM element.
     *
     * @param rsaElem  The RuleSequenceApplicability DOM element
     * @param registry The ID registry for cross-references
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserializeFromElement(Element rsaElem, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        ApplRuleSequence applSeq = getApplRuleSequence();
        if (applSeq == null || rsaElem == null) {
            return;
        }

        String id = rsaElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(applSeq, id);
        }

        // Embedded grammar
        Element gtsElem = findChildElement(rsaElem, "GraphTransformationSystem");
        GraGra gra = applSeq.getGraGra();
        if (gra == null) {
            gra = new GraGra();
            applSeq.setGraGra(gra);
        }
        if (gtsElem != null) {
            GraGraAdapter graAdapter = new GraGraAdapter(gra);
            graAdapter.deserializeFromElement(gtsElem, registry);
        }

        // Rebind the container's sequences to the grammar's sequences,
        // mirroring ApplRuleSequence.XreadObject
        if (!gra.getRuleSequences().isEmpty()) {
            applSeq.setRuleSequences(gra.getRuleSequences());
        }

        Element sequencesElem = findChildElement(rsaElem, "RuleSequences");
        if (sequencesElem == null) {
            return;
        }
        NodeList children = sequencesElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element seqElem = (Element) child;
            if (!"Sequence".equals(seqElem.getTagName())) {
                continue;
            }
            deserializeSequence(seqElem, applSeq, gra, registry);
        }
    }

    /**
     * Deserializes one rule sequence, updating the matching sequence of the
     * container (creating a new one when no sequence with that name exists).
     */
    private void deserializeSequence(Element seqElem, ApplRuleSequence applSeq,
            GraGra gra, DOMSerializationRegistry registry) {
        String strName = seqElem.getAttribute("name");
        boolean newSequence = false;
        RuleSequence seq = findSequence(applSeq.getRuleSequences(), strName);
        if (seq == null) {
            seq = new RuleSequence(gra, "RuleSequence", applSeq.getCPAOption());
            if (!strName.equals("")) {
                seq.setName(strName);
            }
            applSeq.getRuleSequences().add(seq);
            newSequence = true;
        }

        // ConcurrentRule settings
        Element concurrentElem = findChildElement(seqElem, "ConcurrentRule");
        if (concurrentElem != null) {
            String depthstr = concurrentElem.getAttribute("depth");
            if ("undefined".equals(depthstr)) {
                seq.setDepthOfConcurrentRule(-1);
            } else {
                try {
                    seq.setDepthOfConcurrentRule(Integer.parseInt(depthstr));
                } catch (NumberFormatException ignored) {
                }
            }
            seq.setCompleteConcurrency(
                Boolean.parseBoolean(concurrentElem.getAttribute("complete")));
            seq.setCompleteCPAOfConcurrency(
                Boolean.parseBoolean(concurrentElem.getAttribute("completecpa")));
            seq.setIgnoreDanglingEdgeOfDelNode(
                Boolean.parseBoolean(concurrentElem.getAttribute("ignoredanglingedge")));
        }

        // Main applicability results
        Boolean result = null;
        String criterion = null;
        Boolean result2 = null;
        String criterion2 = null;
        int ruleIndx = -1;
        NodeList children = seqElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) child;
            String tagName = childElem.getTagName();
            if ("Item".equals(tagName)) {
                String kind = childElem.getAttribute("kind");
                String res = childElem.getAttribute("result");
                String str = childElem.getAttribute("criterion");
                if ("applicable".equals(kind)) {
                    result = Boolean.valueOf(res);
                    criterion = str;
                } else if ("nonapplicable".equals(kind)) {
                    result2 = Boolean.valueOf(res);
                    criterion2 = str;
                }
            } else if ("Graph".equals(tagName) && newSequence) {
                Object g = registry.getObject(childElem.getAttribute("id"));
                if (g instanceof agg.xt_basis.Graph) {
                    seq.setGraph((agg.xt_basis.Graph) g);
                }
            } else if ("Rule".equals(tagName)) {
                Object r = registry.getObject(childElem.getAttribute("id"));
                if (r instanceof Rule) {
                    Rule rule = (Rule) r;
                    if (newSequence) {
                        seq.addRule(rule);
                    }
                    ruleIndx++;
                    NodeList ruleChildren = childElem.getChildNodes();
                    for (int j = 0; j < ruleChildren.getLength(); j++) {
                        org.w3c.dom.Node ruleChild = ruleChildren.item(j);
                        if (ruleChild.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                            continue;
                        }
                        Element itemElem = (Element) ruleChild;
                        if ("Item".equals(itemElem.getTagName())) {
                            String criterionStr = itemElem.getAttribute("criterion");
                            String res = itemElem.getAttribute("result");
                            String text = itemElem.getAttribute("text");
                            seq.setRuleResult(ruleIndx, rule.getName(),
                                Boolean.parseBoolean(res), criterionStr, text);
                        }
                    }
                }
            }
        }

        if (result != null) {
            seq.setApplicabilityResult(result.booleanValue(), criterion);
        }
        if (result2 != null) {
            seq.setNonApplicabilityResult(result2.booleanValue(), criterion2);
        }
        seq.setChecked(!"undefined".equals(criterion) && !"undefined".equals(criterion2));
    }

    private RuleSequence findSequence(List<RuleSequence> sequences, String name) {
        for (RuleSequence seq : sequences) {
            if (seq.getName().equals(name)) {
                return seq;
            }
        }
        return null;
    }

    private static Element findChildElement(Element parent, String name) {
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
