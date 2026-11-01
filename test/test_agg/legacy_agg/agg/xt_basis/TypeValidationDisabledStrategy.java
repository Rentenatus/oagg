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
 * Type validation strategy for disabled validation.
 * No type checking is performed, all operations are allowed.
 *
 * @author Janusch Rentenatus
 */
public class TypeValidationDisabledStrategy implements TypeValidationStrategy {

    /**
     * Singleton instance for disabled validation strategy.
     */
    public static final TypeValidationStrategy INSTANCE = new TypeValidationDisabledStrategy();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private TypeValidationDisabledStrategy() {
        // Singleton - private constructor
    }

    @Override
    public int getLevel() {
        return TypeSet.DISABLED;
    }

    @Override
    public TypeError checkType(Node node, boolean isComplete) {
        // No validation - always allowed
        return null;
    }

    @Override
    public TypeError checkType(Arc arc, boolean isComplete) {
        // No validation - always allowed
        return null;
    }

    @Override
    public TypeError checkIfRemovable(GraphObject object) {
        // No validation - always allowed
        return null;
    }

    @Override
    public TypeError canCreateArc(Graph graph, Type edgeType, Node source, Node target, int currentTypeGraphLevel) {
        // No validation - always allowed
        return null;
    }

    @Override
    public Collection<TypeError> checkType(Graph graph) {
        // No validation - return empty collection
        return new ArrayList<>();
    }
}
