/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Rule;
import agg.xml.core.XMLSerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Adapter for RuleScheme objects with native DOM serialization logic.
 *
 * <p>Serializes a RuleScheme as a {@code <RuleScheme>} DOM element
 * containing the kernel rule ({@code <Kernel>}), the multi rules with their
 * embedding morphisms ({@code <Multi>} with {@code <EmbeddingLeft>} and
 * {@code <EmbeddingRight>}) and the scheme layer/priority tagged values.
 * The kernel and multi rules are serialized through {@link RuleAdapter},
 * so application conditions and attributes inside a scheme are covered by
 * the same code as plain rules.</p>
 *
 * <p>Deserialization reproduces the model wiring of the legacy
 * {@code RuleScheme.XreadObject}: the kernel rule is populated in place,
 * each {@code <Multi>} element creates an empty multi rule that is then
 * filled by the rule adapter, and the embedding morphisms are applied via
 * {@code applyEmbeddedRuleMapping} and {@code mapKernel2MultiObject}
 * before the kernel graphs register the multi rule as observer.</p>
 */
public class RuleSchemeAdapter extends DomainObjectAdapter<agg.xt_basis.agt.RuleScheme> {

    /**
     * Creates a new adapter for the specified RuleScheme.
     *
     * @param ruleScheme The RuleScheme to adapt
     */
    public RuleSchemeAdapter(agg.xt_basis.agt.RuleScheme ruleScheme) {
        super(ruleScheme);
    }

    /**
     * Gets the adapted RuleScheme instance.
     *
     * @return The RuleScheme
     */
    public agg.xt_basis.agt.RuleScheme getRuleScheme() {
        return getDomainObject();
    }

    /**
     * Serializes this rule scheme to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The RuleScheme DOM element
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        agg.xt_basis.agt.RuleScheme rs = getRuleScheme();
        if (rs == null) {
            return null;
        }

        Element rsElem = doc.createElement("RuleScheme");
        String rsId = registry.register(rs);
        rsElem.setAttribute("ID", rsId);
        rsElem.setAttribute("name", rs.getSchemeName() != null ? rs.getSchemeName() : "");
        if (!rs.isEnabled()) {
            rsElem.setAttribute("enabled", "false");
        }
        rsElem.setAttribute("disjointMultis", String.valueOf(rs.disjointMultiMatches()));
        rsElem.setAttribute("parallelKernel", String.valueOf(rs.parallelKernelMatch()));
        rsElem.setAttribute("checkConflict",
            String.valueOf(rs.checkDeleteUseConflictRequired()));
        rsElem.setAttribute("atLeastOneMultiMatch",
            String.valueOf(rs.atLeastOneMultiMatchRequired()));
        rsElem.setAttribute("index", String.valueOf(rs.getStoredIndexOfRuleList()));

        // Kernel rule
        Rule kernel = rs.getKernelRule();
        if (kernel != null) {
            Element kernelElem = doc.createElement("Kernel");
            RuleAdapter kernelAdapter = new RuleAdapter(kernel);
            Element kernelRuleElem = kernelAdapter.serializeToElement(doc, registry);
            if (kernelRuleElem != null) {
                kernelElem.appendChild(kernelRuleElem);
            }
            rsElem.appendChild(kernelElem);
        }

        // Multi rules with their embedding morphisms
        for (Rule multi : rs.getMultiRules()) {
            Element multiElem = doc.createElement("Multi");
            RuleAdapter multiAdapter = new RuleAdapter(multi);
            Element multiRuleElem = multiAdapter.serializeToElement(doc, registry);
            if (multiRuleElem != null) {
                multiElem.appendChild(multiRuleElem);
            }
            if (multi instanceof agg.xt_basis.agt.MultiRule) {
                agg.xt_basis.agt.MultiRule mr = (agg.xt_basis.agt.MultiRule) multi;
                Element embedLeftElem = doc.createElement("EmbeddingLeft");
                RuleAdapter.serializeMorphism(mr.getEmbeddingLeft(), doc, registry,
                    embedLeftElem);
                multiElem.appendChild(embedLeftElem);
                Element embedRightElem = doc.createElement("EmbeddingRight");
                RuleAdapter.serializeMorphism(mr.getEmbeddingRight(), doc, registry,
                    embedRightElem);
                multiElem.appendChild(embedRightElem);
            }
            rsElem.appendChild(multiElem);
        }

        // Scheme layer and priority
        Element layerElem = doc.createElement("TaggedValue");
        layerElem.setAttribute("Tag", "layer");
        layerElem.setAttribute("TagValue", String.valueOf(rs.getLayer()));
        rsElem.appendChild(layerElem);

        Element priorityElem = doc.createElement("TaggedValue");
        priorityElem.setAttribute("Tag", "priority");
        priorityElem.setAttribute("TagValue", String.valueOf(rs.getPriority()));
        rsElem.appendChild(priorityElem);

        return rsElem;
    }

    /**
     * Deserializes a RuleScheme from a DOM element into the wrapped scheme.
     *
     * @param rsElem  The RuleScheme DOM element
     * @param registry The ID registry for cross-references
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserializeFromElement(Element rsElem, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        agg.xt_basis.agt.RuleScheme rs = getRuleScheme();
        if (rs == null || rsElem == null) {
            return;
        }

        String id = rsElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(rs, id);
        }

        String name = rsElem.getAttribute("name");
        if (name != null && !name.isEmpty()) {
            rs.setSchemeName(name);
        }
        String enabled = rsElem.getAttribute("enabled");
        rs.setEnabled(!"false".equals(enabled));
        String disjointMultis = rsElem.getAttribute("disjointMultis");
        if (!disjointMultis.isEmpty()) {
            rs.setDisjointMultiMatches(Boolean.parseBoolean(disjointMultis));
        }
        String parallelKernel = rsElem.getAttribute("parallelKernel");
        if (!parallelKernel.isEmpty()) {
            rs.setParallelKernelMatch(Boolean.parseBoolean(parallelKernel));
        }
        String checkConflict = rsElem.getAttribute("checkConflict");
        if (!checkConflict.isEmpty()) {
            rs.setCheckDeleteUseConflictRequired(Boolean.parseBoolean(checkConflict));
        }
        String atLeastOneMultiMatch = rsElem.getAttribute("atLeastOneMultiMatch");
        if (!atLeastOneMultiMatch.isEmpty()) {
            rs.setAtLeastOneMultiMatchRequired(Boolean.parseBoolean(atLeastOneMultiMatch));
        }
        String index = rsElem.getAttribute("index");
        if (!index.isEmpty()) {
            try {
                rs.storeIndexOfRuleList(Integer.parseInt(index));
            } catch (NumberFormatException ignored) {
            }
        }

        Rule kernel = rs.getKernelRule();

        NodeList children = rsElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) child;
            String tagName = childElem.getTagName();

            if ("Kernel".equals(tagName)) {
                Element kernelRuleElem = findChildElement(childElem, "Rule");
                if (kernelRuleElem != null && kernel != null) {
                    kernel.getLeft().setKind("LHS");
                    kernel.getRight().setKind("RHS");
                    ((agg.xt_basis.agt.KernelRule) kernel).setRuleScheme(rs);
                    RuleAdapter kernelAdapter = new RuleAdapter(kernel);
                    kernelAdapter.deserializeFromElement(kernelRuleElem, registry);
                }
            } else if ("Multi".equals(tagName)) {
                Element multiRuleElem = findChildElement(childElem, "Rule");
                if (multiRuleElem != null && kernel != null) {
                    agg.xt_basis.agt.MultiRule mr = rs.createEmptyMultiRule();
                    mr.getLeft().setKind("LHS");
                    mr.getRight().setKind("RHS");
                    mr.setRuleScheme(rs);
                    RuleAdapter multiAdapter = new RuleAdapter(mr);
                    multiAdapter.deserializeFromElement(multiRuleElem, registry);

                    Element embedLeftElem = findChildElement(childElem, "EmbeddingLeft");
                    if (embedLeftElem != null) {
                        Element morphElem = findChildElement(embedLeftElem, "Morphism");
                        if (morphElem != null) {
                            RuleAdapter.deserializeMorphism(mr.getEmbeddingLeft(),
                                morphElem, registry);
                        }
                    }
                    Element embedRightElem = findChildElement(childElem, "EmbeddingRight");
                    if (embedRightElem != null) {
                        Element morphElem = findChildElement(embedRightElem, "Morphism");
                        if (morphElem != null) {
                            RuleAdapter.deserializeMorphism(mr.getEmbeddingRight(),
                                morphElem, registry);
                        }
                    }

                    mr.applyEmbeddedRuleMapping(kernel);
                    rs.mapKernel2MultiObject(mr);
                    kernel.getLeft().addObserver(mr);
                    kernel.getRight().addObserver(mr);
                }
            } else if ("TaggedValue".equals(tagName)) {
                String tag = childElem.getAttribute("Tag");
                String tagValue = childElem.getAttribute("TagValue");
                if ("layer".equals(tag)) {
                    try {
                        rs.setLayer(Integer.parseInt(tagValue));
                    } catch (NumberFormatException ignored) {
                    }
                } else if ("priority".equals(tag)) {
                    try {
                        rs.setPriority(Integer.parseInt(tagValue));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        if (kernel != null) {
            ((agg.xt_basis.agt.KernelRule) kernel).setChanged(false);
        }
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
