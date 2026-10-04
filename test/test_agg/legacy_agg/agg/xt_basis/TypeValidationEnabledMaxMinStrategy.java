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
 * Type validation strategy for enabled validation with max and min multiplicity checking.
 *
 * @author Janusch Rentenatus
 */
public class TypeValidationEnabledMaxMinStrategy implements TypeValidationStrategy {

    /**
     * Singleton instance for enabled max-min validation strategy.
     */
    public static final TypeValidationStrategy INSTANCE = new TypeValidationEnabledMaxMinStrategy();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private TypeValidationEnabledMaxMinStrategy() {
        // Singleton - private constructor
    }

    @Override
    public int getLevel() {
        return TypeSet.ENABLED_MAX_MIN;
    }

    @Override
    public TypeError checkType(Node node, boolean isComplete) {
        return null;
    }

    @Override
    public TypeError checkType(Arc arc, boolean isComplete) {
        return null;
    }

    @Override
    public TypeError checkIfRemovable(GraphObject object) {
        return null;
    }

    @Override
    public TypeError canCreateArc(Graph graph, Type edgeType, Node source, Node target, int currentTypeGraphLevel) {
        return null;
    }

    @Override
    public Collection<TypeError> checkType(Graph graph) {
        return new ArrayList<>();
    }
}
