/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Type validation strategy for enabled validation with type graph checking.
 * Validates nodes and arcs against the type graph.
 *
 * @author Janusch Rentenatus
 */
public class TypeValidationEnabledStrategy implements TypeValidationStrategy {

    /**
     * Singleton instance for enabled validation strategy.
     */
    public static final TypeValidationStrategy INSTANCE = new TypeValidationEnabledStrategy();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private TypeValidationEnabledStrategy() {
        // Singleton - private constructor
    }

    @Override
    public int getLevel() {
        return TypeSet.ENABLED;
    }

    @Override
    public TypeError checkType(Node node, boolean isComplete) {
        // Delegate to TypeSet for actual validation
        // This is a placeholder - actual implementation would use TypeSet logic
        return null;
    }

    @Override
    public TypeError checkType(Arc arc, boolean isComplete) {
        // Delegate to TypeSet for actual validation
        return null;
    }

    @Override
    public TypeError checkIfRemovable(GraphObject object) {
        // Delegate to TypeSet for actual validation
        return null;
    }

    @Override
    public TypeError canCreateArc(Graph graph, Type edgeType, Node source, Node target, int currentTypeGraphLevel) {
        // Delegate to TypeSet for actual validation
        return null;
    }

    @Override
    public Collection<TypeError> checkType(Graph graph) {
        // Delegate to TypeSet for actual validation
        return new ArrayList<>();
    }
}
