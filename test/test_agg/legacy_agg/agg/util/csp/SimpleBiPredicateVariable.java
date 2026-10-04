/**
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.util.csp;

import java.util.function.BiPredicate;

public class SimpleBiPredicateVariable implements BiPredicate<Variable, Variable> {

    /**
     * A singleton instance of {@link SimpleBiPredicateVariable}.
     * <p>
     * This instance can be used wherever a reusable, stateless
     * {@code SimpleBiPredicateVariable} implementation is needed. It avoids
     * creating multiple instances of the same predicate.
     */
    public final static SimpleBiPredicateVariable INSTANCE = new SimpleBiPredicateVariable();

    /**
     * Return true iff the object domain of <code>var1</code> is smaller or
     * equall than the object domain of <code>var2</code>.
     * <p>
     * <b>Pre:</b> <code>var1,var2 instance of Variable</code>.
     *
     * @param var1
     * @param var2
     */
    @Override
    public final boolean test(Variable var1, Variable var2) {
        boolean result = (var1.getDomainSize() != var2.getDomainSize())
                ? var1.getDomainSize() < var2.getDomainSize()
                : true;
        return result;
    }
}
