/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.ruleappl.ApplRuleSequence;

/**
 * Adapter for ApplRuleSequence objects that implements the new XMLSerializable interface.
 */
public class ApplRuleSequenceAdapter extends DomainObjectAdapter<ApplRuleSequence> {

    public ApplRuleSequenceAdapter(ApplRuleSequence applRuleSequence) {
        super(applRuleSequence);
    }

    public ApplRuleSequence getApplRuleSequence() {
        return getDomainObject();
    }
}
