/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

/**
 * Visitor interface for the Rule hierarchy.This allows operations on rules without using instanceof checks.
 *
 * @author Janusch Rentenatus
 * @param <T>
 */
public interface RuleVisitor<T> {

    /**
     * Visits a regular rule.
     *
     * @param rule the rule to visit
     * @return the result of visiting
     */
    T visit(Rule rule);

    /**
     * Visits a kernel rule.
     *
     * @param kernelRule the kernel rule to visit
     * @return the result of visiting
     */
    T visit(agg.xt_basis.agt.KernelRule kernelRule);

    /**
     * Visits a multi rule.
     *
     * @param multiRule the multi rule to visit
     * @return the result of visiting
     */
    T visit(agg.xt_basis.agt.MultiRule multiRule);

    /**
     * Visits a rule scheme.
     *
     * @param ruleScheme the rule scheme to visit
     * @return the result of visiting
     */
    T visit(agg.xt_basis.agt.RuleScheme ruleScheme);

    /**
     * Visits a parallel rule.
     *
     * @param parallelRule the parallel rule to visit
     * @return the result of visiting
     */
    T visit(ParallelRule parallelRule);

    /**
     * Visits a concurrent rule.
     *
     * @param concurrentRule the concurrent rule to visit
     * @return the result of visiting
     */
    T visit(ConcurrentRule concurrentRule);
}
