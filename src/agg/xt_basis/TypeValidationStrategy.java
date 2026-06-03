/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

import java.util.Collection;

/**
 * Strategy interface for type validation in graphs.
 * Different implementations provide validation at different levels of strictness.
 *
 * @author Janusch Rentenatus
 */
public interface TypeValidationStrategy {

    /**
     * Returns the level of this validation strategy.
     *
     * @return the validation level constant from TypeSet
     */
    int getLevel();

    /**
     * Checks if a node can be created in the graph.
     *
     * @param node the node to validate
     * @param isComplete whether the graph is complete
     * @return TypeError if validation fails, null otherwise
     */
    TypeError checkType(Node node, boolean isComplete);

    /**
     * Checks if an arc can be created in the graph.
     *
     * @param arc the arc to validate
     * @param isComplete whether the graph is complete
     * @return TypeError if validation fails, null otherwise
     */
    TypeError checkType(Arc arc, boolean isComplete);

    /**
     * Checks if an object can be removed from the graph.
     *
     * @param object the object to check
     * @return TypeError if removal is not allowed, null otherwise
     */
    TypeError checkIfRemovable(GraphObject object);

    /**
     * Checks if an arc can be created between source and target.
     *
     * @param graph the graph context
     * @param edgeType the type of the edge
     * @param source the source node
     * @param target the target node
     * @param currentTypeGraphLevel the current type graph check level
     * @return TypeError if creation is not allowed, null otherwise
     */
    TypeError canCreateArc(Graph graph, Type edgeType, Node source, Node target, int currentTypeGraphLevel);

    /**
     * Validates a complete graph against the type graph.
     *
     * @param graph the graph to validate
     * @return collection of TypeErrors, or empty collection if valid
     */
    Collection<TypeError> checkType(Graph graph);
}
