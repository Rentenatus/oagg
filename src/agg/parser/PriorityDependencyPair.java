/**
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universität Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.parser;

import java.util.Vector;

import agg.cons.Formula;
import agg.util.Pair;
import agg.xt_basis.OrdinaryMorphism;
import agg.xt_basis.Rule;
import agg.xt_basis.Graph;
import java.util.List;

/**
 * These pairs extends the <CODE>DependencyPairs</CODE> with layers.
 *
 * @author $Author: olga $
 */
public class PriorityDependencyPair extends DependencyPair {

    /**
     * Creates a new object to compute critical pairs.
     */
    public PriorityDependencyPair() {
        super();
    }

    /**
     * computes if there is a critical pair of a special kind. Remenber: null is returned if the pair is not critical
     * otherwise an object which can explain in which way this pair is critical. One possible object can be a
     * <code>Vector</code> of overlaping graphs. If a kind kind is requested which cannot be computed a
     * <code>InvalidAlgorithmException</code> is thrown.
     *
     * @param kind specifies the kind of critical pair
     * @param r1 defines the first part which can be critical
     * @param r2 the second part which can be critical
     * @throws InvalidAlgorithmException Thrown if a illegal algorithm is selected.
     * @return The object which is critic of the two rules
     */
    public List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> isCritical(int kind, Rule r1, Rule r2)
            throws InvalidAlgorithmException {
        // System.out.println("LayeredExcludePair.isCritical ");
        if (kind == EXCLUDE || kind == CONFLICTFREE) {
            List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> result = null;
            boolean samePrior = r1.getPriority() == r2.getPriority();
            if (kind == EXCLUDE) {
                if (samePrior) {
                    result = super.isCritical(kind, r1, r2);
                }
            } else if (kind == CONFLICTFREE) {
                if (samePrior) {
                    result = super.isCritical(kind, r1, r2);
                }
            }
            return result;
        }
        throw new InvalidAlgorithmException("No such Algorithm", kind);
    }

    protected boolean checkGraphConsistency(Graph g, int l) {
        // System.out.println("PriorityDependencyPair.checkGraphConsistency...");
        Vector<Formula> constraints = this.grammar.getConstraintsForPriority(-1);
        if (this.grammar.checkGraphConsistency(g, constraints)) {
            constraints = this.grammar.getConstraintsForPriority(l);
            if (this.grammar.checkGraphConsistency(g, constraints)) {
                return true;
            }

            return false;
        }
        return false;
    }
}
