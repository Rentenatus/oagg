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
 * Type validation strategy for inheritance level validation.
 * Only checks inheritance relations, no full type graph validation.
 *
 * @author Janusch Rentenatus
 */
public class TypeValidationInheritanceStrategy implements TypeValidationStrategy {

    /**
     * Singleton instance for inheritance validation strategy.
     */
    public static final TypeValidationStrategy INSTANCE = new TypeValidationInheritanceStrategy();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private TypeValidationInheritanceStrategy() {
        // Singleton - private constructor
    }

    @Override
    public int getLevel() {
        return TypeSet.ENABLED_INHERITANCE;
    }

    @Override
    public TypeError checkType(Node node, boolean isComplete) {
        // Inheritance level - only check inheritance
        return null;
    }

    @Override
    public TypeError checkType(Arc arc, boolean isComplete) {
        // Inheritance level - only check inheritance
        return null;
    }

    @Override
    public TypeError checkIfRemovable(GraphObject object) {
        // Inheritance level - no removability checks
        return null;
    }

    @Override
    public TypeError canCreateArc(Graph graph, Type edgeType, Node source, Node target, int currentTypeGraphLevel) {
        // Inheritance level - only check inheritance
        return null;
    }

    @Override
    public Collection<TypeError> checkType(Graph graph) {
        // Inheritance level - return empty collection
        return new ArrayList<>();
    }
}
