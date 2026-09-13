/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Graph;
import agg.xt_basis.OrdinaryMorphism;
import agg.xt_basis.Rule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * Adapter for Rule objects with real DOM serialization logic.
 *
 * <p>Serializes a Rule as a {@code <Rule>} DOM element containing:
 * <ul>
 *   <li>LHS and RHS graphs as {@code <Graph>} children</li>
 *   <li>{@code <Morphism>} with {@code <Mapping>} elements for the rule morphism</li>
 *   <li>{@code <ApplCondition>} with NAC/PAC/nested AC children</li>
 *   <li>{@code <TaggedValue>} for layer and priority</li>
 * </ul></p>
 */
public class RuleAdapter extends DomainObjectAdapter<Rule> {

    /**
     * Creates a new adapter for the specified Rule.
     *
     * @param rule The Rule to adapt
     */
    public RuleAdapter(Rule rule) {
        super(rule);
    }

    /**
     * Gets the adapted Rule instance.
     *
     * @return The Rule
     */
    public Rule getRule() {
        return getDomainObject();
    }

    /**
     * Serializes this rule to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The Rule DOM element
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        Rule rule = getRule();
        if (rule == null) {
            return null;
        }

        Element ruleElem = doc.createElement("Rule");
        String ruleId = registry.register(rule);
        ruleElem.setAttribute("ID", ruleId);
        ruleElem.setAttribute("name", rule.getName() != null ? rule.getName() : "");

        // Attributes (matching Rule.XwriteObject)
        String formStr = rule.getFormulaStr();
        if (formStr != null && !formStr.isEmpty()) {
            ruleElem.setAttribute("formula", formStr);
        }
        if (!rule.isEnabled()) {
            ruleElem.setAttribute("enabled", "false");
        }
        if (rule.isTriggerOfLayer()) {
            ruleElem.setAttribute("trigger", "true");
        }

        // LHS graph
        Graph lhs = rule.getLeft();
        if (lhs != null) {
            lhs.setKind("LHS");
            GraphAdapter lhsAdapter = new GraphAdapter(lhs);
            Element lhsElem = lhsAdapter.serializeToElement(doc, registry);
            if (lhsElem != null) {
                ruleElem.appendChild(lhsElem);
            }
        }

        // RHS graph
        Graph rhs = rule.getRight();
        if (rhs != null) {
            rhs.setKind("RHS");
            GraphAdapter rhsAdapter = new GraphAdapter(rhs);
            Element rhsElem = rhsAdapter.serializeToElement(doc, registry);
            if (rhsElem != null) {
                ruleElem.appendChild(rhsElem);
            }
        }

        // Morphism (LHS -> RHS mapping)
        serializeMorphism(rule, doc, registry, ruleElem);

        // Application conditions (NACs, PACs, nested ACs)
        serializeApplConditions(rule, doc, registry, ruleElem);

        // TaggedValues for layer and priority
        Element layerElem = doc.createElement("TaggedValue");
        layerElem.setAttribute("Tag", "layer");
        layerElem.setAttribute("TagValue", String.valueOf(rule.getLayer()));
        ruleElem.appendChild(layerElem);

        Element priorityElem = doc.createElement("TaggedValue");
        priorityElem.setAttribute("Tag", "priority");
        priorityElem.setAttribute("TagValue", String.valueOf(rule.getPriority()));
        ruleElem.appendChild(priorityElem);

        return ruleElem;
    }

    /**
     * Serializes the morphism (Mapping elements) of a rule or application condition.
     *
     * @param morphism  The morphism to serialize
     * @param doc       The DOM document
     * @param registry  The ID registry
     * @param parent    The parent element to append to
     */
    static void serializeMorphism(OrdinaryMorphism morphism, Document doc,
            DOMSerializationRegistry registry, Element parent) {
        if (morphism == null) {
            return;
        }

        Element morphElem = doc.createElement("Morphism");
        morphElem.setAttribute("name", morphism.getName() != null ? morphism.getName() : "");

        // Serialize mappings
        Iterator<agg.xt_basis.GraphObject> domain = morphism.getDomain();
        while (domain.hasNext()) {
            agg.xt_basis.GraphObject orig = domain.next();
            agg.xt_basis.GraphObject image = morphism.getImage(orig);

            Element mappingElem = doc.createElement("Mapping");

            String origId = registry.getId(orig);
            if (origId.isEmpty()) {
                origId = registry.register(orig);
            }
            mappingElem.setAttribute("orig", origId);

            if (image != null) {
                String imageId = registry.getId(image);
                if (imageId.isEmpty()) {
                    imageId = registry.register(image);
                }
                mappingElem.setAttribute("image", imageId);
            }

            morphElem.appendChild(mappingElem);
        }

        parent.appendChild(morphElem);
    }

    private void serializeApplConditions(Rule rule, Document doc,
            DOMSerializationRegistry registry, Element ruleElem) {
        boolean hasConditions = false;
        Element applElem = doc.createElement("ApplCondition");

        // NACs
        List<OrdinaryMorphism> nacs = rule.getNACsList();
        if (nacs != null) {
            for (OrdinaryMorphism nac : nacs) {
                hasConditions = true;
                Element nacElem = doc.createElement("NAC");
                if (!nac.isEnabled()) {
                    nacElem.setAttribute("enabled", "false");
                }

                // NAC graph
                Graph nacGraph = nac.getTarget();
                if (nacGraph != null) {
                    nacGraph.setKind("NAC");
                    GraphAdapter nacGraphAdapter = new GraphAdapter(nacGraph);
                    Element nacGraphElem = nacGraphAdapter.serializeToElement(doc, registry);
                    if (nacGraphElem != null) {
                        nacElem.appendChild(nacGraphElem);
                    }
                }

                // NAC morphism
                serializeMorphism(nac, doc, registry, nacElem);

                applElem.appendChild(nacElem);
            }
        }

        // PACs
        List<OrdinaryMorphism> pacs = rule.getPACsList();
        if (pacs != null) {
            for (OrdinaryMorphism pac : pacs) {
                hasConditions = true;
                Element pacElem = doc.createElement("PAC");
                if (!pac.isEnabled()) {
                    pacElem.setAttribute("enabled", "false");
                }

                Graph pacGraph = pac.getTarget();
                if (pacGraph != null) {
                    pacGraph.setKind("PAC");
                    GraphAdapter pacGraphAdapter = new GraphAdapter(pacGraph);
                    Element pacGraphElem = pacGraphAdapter.serializeToElement(doc, registry);
                    if (pacGraphElem != null) {
                        pacElem.appendChild(pacGraphElem);
                    }
                }

                serializeMorphism(pac, doc, registry, pacElem);

                applElem.appendChild(pacElem);
            }
        }

        // Nested ACs
        List<OrdinaryMorphism> acs = rule.getNestedACsList();
        if (acs != null) {
            for (OrdinaryMorphism ac : acs) {
                hasConditions = true;
                Element acElem = doc.createElement("NestedAC");
                if (!ac.isEnabled()) {
                    acElem.setAttribute("enabled", "false");
                }

                Graph acGraph = ac.getTarget();
                if (acGraph != null) {
                    acGraph.setKind("AC");
                    GraphAdapter acGraphAdapter = new GraphAdapter(acGraph);
                    Element acGraphElem = acGraphAdapter.serializeToElement(doc, registry);
                    if (acGraphElem != null) {
                        acElem.appendChild(acGraphElem);
                    }
                }

                serializeMorphism(ac, doc, registry, acElem);

                applElem.appendChild(acElem);
            }
        }

        if (hasConditions) {
            ruleElem.appendChild(applElem);
        }
    }
}
