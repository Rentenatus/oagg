/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.cons.Formula;

/**
 * Adapter for Formula objects that implements the new XMLSerializable interface.
 */
public class FormulaAdapter extends DomainObjectAdapter<Formula> {

    public FormulaAdapter(Formula formula) {
        super(formula);
    }

    public Formula getFormula() {
        return getDomainObject();
    }
}
