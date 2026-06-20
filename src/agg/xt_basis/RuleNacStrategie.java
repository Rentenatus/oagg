/**
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

import agg.attribute.AttrContext;

/**
 * Strategy class for managing Negative Application Conditions (NAC) in a Rule.
 */
public class RuleNacStrategie extends RuleConditionStrategy {

    /**
     * Creates a new NAC strategy for the specified rule.
     *
     * @param rule the rule this strategy belongs to
     */
    public RuleNacStrategie(Rule rule) {
        super(rule);
    }

    @Override
    public OrdinaryMorphism createAc() {
        final OrdinaryMorphism negativeApplCond = new OrdinaryMorphism(
                getRule().getLeft(),
                BaseFactory.theFactory().createGraph(getRule().getRight().getTypeSet()),
                getRule().getRight().getAttrContext());
        this.itsACs.add(negativeApplCond);
        AttrContext negativeApplCondContext = negativeApplCond.getAttrContext();
        negativeApplCond.getImage().setAttrContext(negativeApplCondContext);
        negativeApplCond.getImage().setKind(GraphKind.NAC);
        return negativeApplCond;
    }

    /**
     * Creates a new negative application condition (NAC) and adds it to this rule. The target graph of the new NAC is
     * constructed based on the RHS of this rule.
     *
     * @return a new NAC with target graph constructed from the RHS
     */
    public OrdinaryMorphism createAcDuetoRHS() {
        final OrdinaryMorphism negativeApplCond = createAc();
        getRule().makeACDuetoRHS(negativeApplCond);
        return negativeApplCond;
    }

    @Override
    public boolean addAc(int index, final OrdinaryMorphism negativeApplCond) {
        if (!this.itsACs.contains(negativeApplCond)) {
            negativeApplCond.getTarget().setKind(GraphKind.NAC);
            if (index >= 0 && index < this.itsACs.size()) {
                this.itsACs.add(index, negativeApplCond);
            } else {
                this.itsACs.add(negativeApplCond);
            }
            getRule().changed = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean isAcValid(OrdinaryMorphism nac) {
        return true;
    }
}
