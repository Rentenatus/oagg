/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.ruleappl.ApplRuleSequence;
import agg.ruleappl.RuleSequence;
import agg.xt_basis.Rule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Adapter for ApplRuleSequence objects with real DOM serialization logic.
 *
 * <p>Serializes an ApplRuleSequence as a {@code <RuleSequenceApplicability>}
 * DOM element containing rule sequences with their applicability results,
 * concurrent rule settings, and per-rule results.</p>
 */
public class ApplRuleSequenceAdapter extends DomainObjectAdapter<ApplRuleSequence> {

    public ApplRuleSequenceAdapter(ApplRuleSequence sequence) {
        super(sequence);
    }

    public ApplRuleSequence getApplRuleSequence() {
        return getDomainObject();
    }

    /**
     * Serializes this rule sequence applicability to a DOM element.
     *
     * @param doc     The DOM document to create elements in
     * @param registry The ID registry for cross-references
     * @return The RuleSequenceApplicability DOM element
     */
    public Element serializeToElement(Document doc, DOMSerializationRegistry registry) {
        ApplRuleSequence applSeq = getApplRuleSequence();
        if (applSeq == null) {
            return null;
        }

        Element rsElem = doc.createElement("RuleSequenceApplicability");
        String rsId = registry.register(applSeq);
        rsElem.setAttribute("ID", rsId);

        // RuleSequences container
        Element sequencesElem = doc.createElement("RuleSequences");
        List<RuleSequence> sequences = applSeq.getRuleSequences();
        if (sequences != null) {
            for (RuleSequence seq : sequences) {
                Element seqElem = doc.createElement("Sequence");
                seqElem.setAttribute("name",
                    seq.getName() != null ? seq.getName() : "");

                // ConcurrentRule settings
                Element concurrentElem = doc.createElement("ConcurrentRule");
                int depth = seq.getDepthOfConcurrentRule();
                if (depth == -1) {
                    concurrentElem.setAttribute("depth", "undefined");
                } else {
                    concurrentElem.setAttribute("depth", String.valueOf(depth));
                }
                concurrentElem.setAttribute("complete",
                    String.valueOf(seq.getCompleteConcurrency()));
                concurrentElem.setAttribute("completecpa",
                    String.valueOf(seq.getCompleteCPAOfConcurrency()));
                concurrentElem.setAttribute("ignoredanglingedge",
                    String.valueOf(seq.getIgnoreDanglingEdgeOfDelNode()));
                seqElem.appendChild(concurrentElem);

                // Graph reference
                if (seq.getGraph() != null) {
                    Element graphRefElem = doc.createElement("Graph");
                    String graphId = registry.getId(seq.getGraph());
                    if (graphId.isEmpty()) {
                        graphId = registry.register(seq.getGraph());
                    }
                    graphRefElem.setAttribute("id", graphId);
                    seqElem.appendChild(graphRefElem);
                }

                // Rule references
                List<Rule> rules = seq.getRules();
                if (rules != null) {
                    for (Rule rule : rules) {
                        Element ruleRefElem = doc.createElement("Rule");
                        String ruleId = registry.getId(rule);
                        if (ruleId.isEmpty()) {
                            ruleId = registry.register(rule);
                        }
                        ruleRefElem.setAttribute("id", ruleId);
                        seqElem.appendChild(ruleRefElem);
                    }
                }

                sequencesElem.appendChild(seqElem);
            }
        }
        rsElem.appendChild(sequencesElem);

        return rsElem;
    }
}
