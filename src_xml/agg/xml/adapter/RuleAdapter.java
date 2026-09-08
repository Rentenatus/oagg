/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.xt_basis.Rule;
import agg.util.XMLHelper;
import agg.xml.core.XMLSerializable;

/**
 * Adapter for Rule objects that implements the new XMLSerializable interface.
 * This adapter wraps a Rule instance and delegates serialization calls to
 * its XMLObject methods (XwriteObject/XreadObject).
 */
public class RuleAdapter extends DomainObjectAdapter<Rule> implements XMLSerializable {
    
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
}
