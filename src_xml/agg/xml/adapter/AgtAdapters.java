/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.agt.MultiRule;
import agg.xt_basis.agt.RuleScheme;

/**
 * Adapters for AGT (Advanced Graph Transformation) classes.
 */
public final class AgtAdapters {

    private AgtAdapters() {
    }

    public static final class MultiRuleAdapter
            extends DomainObjectAdapter<MultiRule>
            implements agg.xml.core.XMLSerializable {
        public MultiRuleAdapter(MultiRule multiRule) { super(multiRule); }
        public MultiRule getMultiRule() { return getDomainObject(); }
    }

    public static final class RuleSchemeAdapter
            extends DomainObjectAdapter<RuleScheme>
            implements agg.xml.core.XMLSerializable {
        public RuleSchemeAdapter(RuleScheme ruleScheme) { super(ruleScheme); }
        public RuleScheme getRuleScheme() { return getDomainObject(); }
    }
}
