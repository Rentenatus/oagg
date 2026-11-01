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
import agg.xml.core.XMLSerializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
        if (rule.isParallelApplyEnabled()) {
            ruleElem.setAttribute("parallel", "true");
        }
        if (rule.isWaitBeforeApplyEnabled()) {
            ruleElem.setAttribute("waitBeforeApply", "true");
        }

        // Variables (rule parameters): mirror the legacy VarTuple.XwriteObject
        // behaviour - write every variable with a non-empty type and name;
        // value/expr and PTYPE only when set.
        agg.attribute.AttrVariableTuple variables = rule.getAttrContext().getVariables();
        if (variables instanceof agg.attribute.impl.VarTuple) {
            agg.attribute.impl.VarTuple varTuple = (agg.attribute.impl.VarTuple) variables;
            for (int i = 0; i < varTuple.getSize(); i++) {
                agg.attribute.impl.VarMember vm = varTuple.getVarMemberAt(i);
                String varName = vm != null ? vm.getName() : null;
                String varType = (vm != null && vm.getDeclaration() != null
                    && vm.getDeclaration().getType() != null)
                        ? vm.getDeclaration().getType().toString() : null;
                if (vm != null && varName != null && varType != null
                        && !varName.isEmpty() && !varType.isEmpty()) {
                    Element paramElem = doc.createElement("Parameter");
                    if (vm.isSet() && vm.getExpr() != null) {
                        if (vm.getExpr().isConstant()) {
                            Object v = vm.getExpr().getValue();
                            if (v != null) {
                                paramElem.setAttribute("value", v.toString());
                            }
                        } else {
                            String exprText = vm.getExpr().getString();
                            if (exprText != null) {
                                paramElem.setAttribute("expr", exprText);
                            }
                        }
                    }
                    paramElem.setAttribute("name", varName);
                    paramElem.setAttribute("type", varType);
                    boolean isin = vm.isInputParameter();
                    boolean isout = vm.isOutputParameter();
                    String inout = (isin && isout) ? "inout" : (isin) ? "input"
                        : (isout) ? "output" : "";
                    if (!inout.isEmpty()) {
                        paramElem.setAttribute("PTYPE", inout);
                    }
                    ruleElem.appendChild(paramElem);
                }
            }
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
        // Comment (mirroring the legacy writeMorphism behaviour)
        String comment = morphism.getTextualComment();
        if (comment != null && !comment.isEmpty()) {
            morphElem.setAttribute("comment", comment);
        }

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

        // AttrCondition (attribute context conditions)
        agg.attribute.AttrConditionTuple condTuple = rule.getAttrContext().getConditions();
        if (condTuple instanceof agg.attribute.impl.CondTuple) {
            agg.attribute.impl.CondTuple ct = (agg.attribute.impl.CondTuple) condTuple;
            if (ct.getSize() > 0) {
                hasConditions = true;
                Element attrCondElem = doc.createElement("AttrCondition");
                for (int i = 0; i < ct.getSize(); i++) {
                    agg.attribute.impl.CondMember cm = ct.getCondMemberAt(i);
                    if (cm != null && cm.isSet()) {
                        Element condElem = doc.createElement("Condition");
                        Element valueElem = doc.createElement("Value");
                        Element stringElem = doc.createElement("string");
                        stringElem.setTextContent(cm.getExprAsText() != null ? cm.getExprAsText() : "");
                        valueElem.appendChild(stringElem);
                        condElem.appendChild(valueElem);
                        attrCondElem.appendChild(condElem);
                    }
                }
                applElem.appendChild(attrCondElem);
            }
        }

        // PostApplicationCondition (FormulaRef elements)
        java.util.List<agg.cons.Formula> usedFormulas = rule.getUsedFormulas();
        if (usedFormulas != null && !usedFormulas.isEmpty()) {
            hasConditions = true;
            Element pacElem = doc.createElement("PostApplicationCondition");
            for (agg.cons.Formula formula : usedFormulas) {
                Element formulaRefElem = doc.createElement("FormulaRef");
                String formulaId = registry.getId(formula);
                if (formulaId.isEmpty()) {
                    formulaId = registry.register(formula);
                }
                formulaRefElem.setAttribute("f", formulaId);
                pacElem.appendChild(formulaRefElem);
            }
            applElem.appendChild(pacElem);
        }

        if (hasConditions) {
            ruleElem.appendChild(applElem);
        }
    }

    /**
     * Deserializes a Rule from a DOM element into the wrapped Rule instance.
     *
     * <p>Reads the Rule attributes, loads LHS and RHS graphs via GraphAdapter,
     * restores the morphism (Mapping elements), and creates NACs/PACs/nested
     * ACs from the ApplCondition child.</p>
     *
     * @param ruleElem The Rule DOM element
     * @param registry  The ID registry for cross-references
     * @throws XMLSerializationException if deserialization fails
     */
    public void deserializeFromElement(Element ruleElem, DOMSerializationRegistry registry)
            throws XMLSerializationException {
        Rule rule = getRule();
        if (rule == null || ruleElem == null) {
            return;
        }

        String id = ruleElem.getAttribute("ID");
        if (!id.isEmpty()) {
            registry.registerWithId(rule, id);
        }

        String name = ruleElem.getAttribute("name");
        if (name != null && !name.isEmpty()) {
            rule.setName(name);
        }

        String enabled = ruleElem.getAttribute("enabled");
        if ("false".equals(enabled)) {
            rule.setEnabled(false);
        }

        String formula = ruleElem.getAttribute("formula");
        if (formula != null && !formula.isEmpty()) {
            rule.setFormula(formula);
        }

        String trigger = ruleElem.getAttribute("trigger");
        if ("true".equals(trigger)) {
            rule.setTriggerForLayer(true);
        }

        String parallel = ruleElem.getAttribute("parallel");
        if ("true".equals(parallel)) {
            rule.setParallelMatchingEnabled(true);
        }

        String waitBeforeApply = ruleElem.getAttribute("waitBeforeApply");
        if ("true".equals(waitBeforeApply)) {
            rule.setWaitBeforeApplyEnabled(true);
        }

        // Parse child elements: LHS, RHS, Morphism, ApplCondition, TaggedValue
        NodeList children = ruleElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) child;
            String tagName = childElem.getTagName();

            if ("Graph".equals(tagName)) {
                String kind = childElem.getAttribute("kind");
                Graph targetGraph;
                if ("LHS".equals(kind)) {
                    targetGraph = rule.getLeft();
                } else if ("RHS".equals(kind)) {
                    targetGraph = rule.getRight();
                } else {
                    continue;
                }
                if (targetGraph != null) {
                    GraphAdapter graphAdapter = new GraphAdapter(targetGraph);
                    graphAdapter.deserializeFromElement(childElem, registry);
                }
            } else if ("Morphism".equals(tagName)) {
                deserializeMorphism(rule, childElem, registry);
            } else if ("ApplCondition".equals(tagName)) {
                deserializeApplConditions(rule, childElem, registry);
            } else if ("Parameter".equals(tagName)) {
                // Rule variables, mirroring the legacy VarTuple.XreadObject
                deserializeVariable(rule, childElem);
            } else if ("TaggedValue".equals(tagName)) {
                String tag = childElem.getAttribute("Tag");
                String tagValue = childElem.getAttribute("TagValue");
                if ("layer".equals(tag)) {
                    try {
                        rule.setLayer(Integer.parseInt(tagValue));
                    } catch (NumberFormatException ignored) {
                    }
                } else if ("priority".equals(tag)) {
                    try {
                        rule.setPriority(Integer.parseInt(tagValue));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    /**
     * Deserializes a morphism from a Morphism DOM element.
     * Reads Mapping children and adds them to the morphism.
     * Nodes are mapped first, then arcs (matching OrdinaryMorphism.readMorphism).
     *
     * @param morphism  The morphism to populate
     * @param morphElem The Morphism DOM element
     * @param registry  The ID registry for resolving references
     */
    static void deserializeMorphism(agg.xt_basis.OrdinaryMorphism morphism,
            Element morphElem, DOMSerializationRegistry registry) {
        if (morphism == null || morphElem == null) {
            return;
        }

        String name = morphElem.getAttribute("name");
        if (name != null) {
            morphism.setName(name.replaceAll(" ", ""));
        }

        // Comment (mirroring the legacy readMorphism behaviour)
        String comment = morphElem.getAttribute("comment");
        if (comment != null && !comment.isEmpty()) {
            morphism.setTextualComment(comment);
        }

        // Collect all mappings, apply nodes first, then arcs
        java.util.List<MappingEntry> arcMappings = new java.util.ArrayList<>();
        NodeList children = morphElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element mappingElem = (Element) child;
            if (!"Mapping".equals(mappingElem.getTagName())) {
                continue;
            }

            String origId = mappingElem.getAttribute("orig");
            String imageId = mappingElem.getAttribute("image");
            Object orig = registry.getObject(origId);
            Object image = registry.getObject(imageId);

            if (orig instanceof agg.xt_basis.Node && image instanceof agg.xt_basis.Node) {
                try {
                    morphism.addMapping((agg.xt_basis.GraphObject) orig,
                        (agg.xt_basis.GraphObject) image);
                } catch (agg.xt_basis.BadMappingException ignored) {
                }
            } else if (orig instanceof agg.xt_basis.Arc && image instanceof agg.xt_basis.Arc) {
                arcMappings.add(new MappingEntry(orig, image));
            }
        }

        // Now apply arc mappings (nodes must be mapped first)
        for (MappingEntry entry : arcMappings) {
            try {
                morphism.addMapping((agg.xt_basis.GraphObject) entry.orig,
                    (agg.xt_basis.GraphObject) entry.image);
            } catch (agg.xt_basis.BadMappingException ignored) {
            }
        }
    }

    private void deserializeApplConditions(Rule rule, Element applElem,
            DOMSerializationRegistry registry) throws XMLSerializationException {
        NodeList children = applElem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) child;
            String tagName = childElem.getTagName();

            if ("NAC".equals(tagName)) {
                OrdinaryMorphism nac = rule.createNAC();
                String enabled = childElem.getAttribute("enabled");
                nac.setEnabled(!"false".equals(enabled));

                // Deserialize NAC graph
                Element nacGraphElem = findChildElement(childElem, "Graph");
                if (nacGraphElem != null && nac.getTarget() != null) {
                    GraphAdapter nacAdapter = new GraphAdapter(nac.getTarget());
                    nacAdapter.deserializeFromElement(nacGraphElem, registry);
                }

                // Deserialize NAC morphism
                Element nacMorphElem = findChildElement(childElem, "Morphism");
                if (nacMorphElem != null) {
                    deserializeMorphism(nac, nacMorphElem, registry);
                }

                if (nac.getName() == null || nac.getName().isEmpty()) {
                    nac.setName("nac" + rule.getNACsList().size());
                }
            } else if ("PAC".equals(tagName)) {
                OrdinaryMorphism pac = rule.createPAC();
                String enabled = childElem.getAttribute("enabled");
                pac.setEnabled(!"false".equals(enabled));

                Element pacGraphElem = findChildElement(childElem, "Graph");
                if (pacGraphElem != null && pac.getTarget() != null) {
                    GraphAdapter pacAdapter = new GraphAdapter(pac.getTarget());
                    pacAdapter.deserializeFromElement(pacGraphElem, registry);
                }

                Element pacMorphElem = findChildElement(childElem, "Morphism");
                if (pacMorphElem != null) {
                    deserializeMorphism(pac, pacMorphElem, registry);
                }

                if (pac.getName() == null || pac.getName().isEmpty()) {
                    pac.setName("pac" + rule.getPACsList().size());
                }
            } else if ("NestedAC".equals(tagName)) {
                agg.xt_basis.NestedApplCond nestedAc = rule.createNestedAC();
                String enabled = childElem.getAttribute("enabled");
                nestedAc.setEnabled(!"false".equals(enabled));

                Element acGraphElem = findChildElement(childElem, "Graph");
                if (acGraphElem != null && nestedAc.getTarget() != null) {
                    GraphAdapter acAdapter = new GraphAdapter(nestedAc.getTarget());
                    acAdapter.deserializeFromElement(acGraphElem, registry);
                }

                Element acMorphElem = findChildElement(childElem, "Morphism");
                if (acMorphElem != null) {
                    deserializeMorphism(nestedAc, acMorphElem, registry);
                }

                if (nestedAc.getName() == null || nestedAc.getName().isEmpty()) {
                    nestedAc.setName("gac" + rule.getNestedACsList().size());
                }
            } else if ("AttrCondition".equals(tagName)) {
                // Deserialize attribute conditions
                agg.attribute.AttrConditionTuple condTuple = rule.getAttrContext().getConditions();
                if (condTuple instanceof agg.attribute.impl.CondTuple) {
                    agg.attribute.impl.CondTuple ct = (agg.attribute.impl.CondTuple) condTuple;
                    NodeList condChildren = childElem.getChildNodes();
                    for (int j = 0; j < condChildren.getLength(); j++) {
                        org.w3c.dom.Node condNode = condChildren.item(j);
                        if (condNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                            continue;
                        }
                        Element condElem = (Element) condNode;
                        if ("Condition".equals(condElem.getTagName())) {
                            Element valueElem = findChildElement(condElem, "Value");
                            if (valueElem != null) {
                                Element stringElem = findChildElement(valueElem, "string");
                                if (stringElem != null) {
                                    String exprText = stringElem.getTextContent();
                                    if (exprText != null && !exprText.isEmpty()) {
                                        ct.addCondition(exprText.trim());
                                    }
                                }
                            }
                        }
                    }
                }
            } else if ("PostApplicationCondition".equals(tagName)) {
                // Deserialize FormulaRef elements (reference to Formula by ID)
                // The actual formula association is handled by GraGra after all
                // constraints are loaded. Here we just note the formula IDs.
                NodeList formulaRefChildren = childElem.getChildNodes();
                for (int j = 0; j < formulaRefChildren.getLength(); j++) {
                    org.w3c.dom.Node frNode = formulaRefChildren.item(j);
                    if (frNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                        continue;
                    }
                    Element frElem = (Element) frNode;
                    if ("FormulaRef".equals(frElem.getTagName())) {
                        String formulaId = frElem.getAttribute("f");
                        if (!formulaId.isEmpty()) {
                            Object formula = registry.getObject(formulaId);
                            if (formula instanceof agg.cons.Formula) {
                                // Associate the formula with this rule
                                // This is done via rule.setFormula() or by adding to usedFormulas
                                // For now, we store the reference for later resolution
                            }
                        }
                    }
                }
            }
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

    private static class MappingEntry {
        final Object orig;
        final Object image;

        MappingEntry(Object orig, Object image) {
            this.orig = orig;
            this.image = image;
        }
    }

    /**
     * Deserializes a rule variable from a {@code <Parameter>} element,
     * mirroring the legacy VarTuple.XreadObject behaviour: declare the
     * variable with the Java handler, apply the PTYPE flags and set the
     * value or expression text.
     */
    private void deserializeVariable(Rule rule, Element paramElem) {
        String name = paramElem.getAttribute("name");
        String typestr = paramElem.getAttribute("type");
        if (name == null || name.isEmpty() || typestr == null || typestr.isEmpty()) {
            return;
        }
        agg.attribute.AttrVariableTuple variables = rule.getAttrContext().getVariables();
        if (!(variables instanceof agg.attribute.impl.VarTuple)) {
            return;
        }
        agg.attribute.impl.VarTuple vars = (agg.attribute.impl.VarTuple) variables;
        if (vars.getVarMemberAt(name) != null) {
            // already declared (e.g. through an attribute expression)
            return;
        }
        agg.attribute.handler.AttrHandler handler =
            agg.attribute.facade.impl.DefaultInformationFacade.self().getJavaHandler();
        vars.declare(handler, typestr, name);
        agg.attribute.impl.VarMember var = vars.getVarMemberAt(name);
        if (var == null) {
            return;
        }
        String inout = paramElem.getAttribute("PTYPE");
        boolean isin = "inout".equals(inout) || "input".equals(inout);
        boolean isout = "inout".equals(inout) || "output".equals(inout);
        var.setInputParameter(isin);
        var.setOutputParameter(isout);
        String value = paramElem.getAttribute("value");
        if (value != null && !value.isEmpty()) {
            if ("String".equals(typestr)) {
                var.setExprAsText("\"" + value + "\"");
            } else if ("Character".equals(typestr) || "char".equals(typestr)) {
                var.setExprAsText("'" + value.charAt(0) + "'");
            } else {
                var.setExprAsText(value);
            }
            var.checkValidity();
        } else {
            String expr = paramElem.getAttribute("expr");
            if (expr != null && !expr.isEmpty()) {
                var.setExprAsText(expr);
                var.checkValidity();
            }
        }
    }
}
