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
package agg.xt_basis;

/**
 * Represents an error that occurred during type checking. The
 * {@link TypeSet#checkTypeGraph()} and {@link TypeSet#checkType()} methods will
 * return an enumeration of such objects if an error occurred.
 *
 * From this object you can obtain information about the kind of error and the
 * incorrectly typed graph objects. This object also provides a number for the
 * error that occurred.
 *
 * @see TypeSet#checkTypeGraph()
 * @see TypeSet#checkType()
 */
public class TypeError {

    /**
     * Error number for undefined errors. No method in the official distribution
     * will return this, but you can use it for testing or if your error does not
     * fit in one of the other categories. All contained objects may be null.
     */
    public static final int NOT_DEFINED = 0;

    /**
     * Error number if no type graph was defined. This error will occur when
     * you try to check the type or when you try to check some other graph
     * with an empty type graph or no type graph. All contained objects should
     * be null.
     */
    public static final int NO_TYPE_GRAPH = 1;

    /**
     * Error number if a type is not present in the type graph.
     * {@link #getType()} will return the missing type and
     * {@link #getContainingGraph()} will return the used type graph.
     */
    public static final int TYPE_UNDEFINED = 2;

    /**
     * Error number if a type is already defined in the type graph (two nodes of
     * the same type or two edges of the same type between the same nodes).
     * {@link #getType()} will return the type,
     * {@link #getGraphObject()} will return the last found graph object with this type,
     * and {@link #getContainingGraph()} will return the used type graph.
     */
    public static final int TYPE_ALREADY_DEFINED = 3;

    /**
     * Error number if you tried to remove a graph object from the type graph, but
     * there are graph objects in the other graphs of this type.
     * {@link #getType()} will return the type,
     * {@link #getGraphObject()} will return the graph object you tried to remove.
     * If you tried to remove the type, {@link #getType()} will return the type.
     * {@link #getContainingGraph()} will return the used type graph.
     */
    public static final int TYPE_IS_IN_USE = 4;

    // Merge of two type sets
    /**
     * Error number if you tried to merge two type sets and there are used types
     * unknown (merging happens if you use another TypeSet to check a graph).
     * {@link #getType()} will return the missing type,
     * {@link #getGraphObject()} will return the graph object using this type,
     * and {@link #getContainingGraph()} will return the checked graph.
     */
    public static final int TYPE_UNKNOWN_HERE = 11;

    // Type check
    /**
     * Error number if a graph object was found whose type is not defined in the
     * type graph (but defined in the TypeSet).
     * {@link #getType()} will return the type,
     * {@link #getGraphObject()} will return the graph object with the wrong type,
     * and {@link #getContainingGraph()} will return the checked graph.
     */
    public static final int NO_SUCH_TYPE = 21;

    /**
     * Error number if there were more arcs of a type than allowed by the type
     * graph. {@link #getType()} will return the type,
     * {@link #getGraphObject()} will return the last found graph object with this type,
     * and {@link #getContainingGraph()} will return the checked graph.
     * It is possible that more than one error object will be produced for one
     * occurrence of this mismatch (e.g., one for each arc).
     */
    public static final int TO_MUCH_ARCS = 22;

    /**
     * Error number if there were fewer arcs of a type than allowed by the type
     * graph. {@link #getType()} will return the type and
     * {@link #getContainingGraph()} will return the checked graph.
     */
    public static final int TO_LESS_ARCS = 23;

    /**
     * Error number if there were fewer nodes of a type than allowed.
     */
    public static final int TO_LESS_NODES = 24;

    /**
     * Error number if there were more nodes of a type than allowed.
     */
    public static final int TO_MUCH_NODES = 25;

    /**
     * Error number if a parent type is not allowed.
     */
    public static final int PARENT_NOT_ALLOWED = 26;

    /**
     * Error number if the type is not compatible.
     */
    public static final int NOT_COMPATIBLE_TYPE = 27;

    /**
     * Error number for unknown errors.
     */
    public static final int UNKNOWN_ERROR = 28;

    /**
     * Error number if no parallel arc is allowed.
     */
    public static final int NO_PARALLEL_ARC = 29;

    /**
     * A short error message.
     */
    String message = null;

    /**
     * A number describing the error.
     */
    int errorNumber = 0;

    /**
     * The invalid GraphObject.
     */
    GraphObject wrongObject = null;

    /**
     * The invalid Type.
     */
    Type wrongType = null;

    /**
     * The graph which was checked.
     */
    Graph containingGraph = null;

    /**
     * Creates an error object. The values cannot be changed after creation.
     *
     * @param errorNumber a code for the error that occurred. As described above (see
     *        {@link #NOT_DEFINED}), the code also defines which other parameters are set.
     * @param message a short English description of the error. The description
     *        should not contain more information than given by the errorNumber and the
     *        other parameters.
     *
     * @see #setContainingGraph(Graph)
     * @see #NOT_DEFINED
     * @see #NO_TYPE_GRAPH
     */
    public TypeError(int errorNumber, String message) {
        this.message = message;
        this.errorNumber = errorNumber;
    }

    /**
     * Creates an error object. The values cannot be changed after creation.
     *
     * @param errorNumber a code for the error that occurred. As described above (see
     *        {@link #NOT_DEFINED}), the code also defines which other parameters are set.
     * @param message a short English description of the error. The description
     *        should not contain more information than given by the errorNumber and the
     *        other parameters.
     * @param wrongType the invalid {@link Type}. The role this type plays is described
     *        in the comment of the error number constant.
     *
     * @see #setContainingGraph(Graph)
     * @see #NOT_DEFINED
     * @see #TYPE_UNDEFINED
     */
    public TypeError(int errorNumber, String message, Type wrongType) {
        this.message = message;
        this.errorNumber = errorNumber;
        this.wrongType = wrongType;
    }

    /**
     * Creates an error object. The values cannot be changed after creation.
     *
     * @param errorNumber a code for the error that occurred. As described above (see
     *        {@link #NOT_DEFINED}), the code also defines which other parameters are set.
     * @param message a short English description of the error. The description
     *        should not contain more information than given by the errorNumber and the
     *        other parameters.
     * @param wrongObject the invalid {@link GraphObject}.
     * @param wrongType the invalid {@link Type}. The roles the GraphObject and the
     *        Type play are described in the comment of the error number constant.
     *
     * @see #setContainingGraph(Graph)
     * @see #NOT_DEFINED
     * @see #NO_TYPE_GRAPH
     * @see #TYPE_UNDEFINED
     * @see #TYPE_ALREADY_DEFINED
     * @see #TYPE_UNKNOWN_HERE
     * @see #NO_SUCH_TYPE
     * @see #TO_MUCH_ARCS
     * @see #TO_LESS_ARCS
     */
    public TypeError(int errorNumber, String message, GraphObject wrongObject,
            Type wrongType) {
        this.message = message;
        this.errorNumber = errorNumber;
        this.wrongType = wrongType;
        this.wrongObject = wrongObject;
    }

    /**
     * Creates an error object. The values cannot be changed after creation.
     *
     * @param errorNumber a code for the error that occurred. As described above (see
     *        {@link #NOT_DEFINED}), the code also defines which other parameters are set.
     * @param message a short English description of the error. The description
     *        should not contain more information than given by the errorNumber and the
     *        other parameters.
     * @param contGraph the graph which contains the wrong objects.
     *
     * @see #NOT_DEFINED
     * @see #NO_TYPE_GRAPH
     */
    public TypeError(int errorNumber, String message, Graph contGraph) {
        this.message = message;
        this.errorNumber = errorNumber;
        this.containingGraph = contGraph;
    }

    /**
     * Creates an error object. The values cannot be changed after creation.
     *
     * @param errorNumber a code for the error that occurred. As described above (see
     *        {@link #NOT_DEFINED}), the code also defines which other parameters are set.
     * @param message a short English description of the error. The description
     *        should not contain more information than given by the errorNumber and the
     *        other parameters.
     * @param wrongType the invalid {@link Type}. The role this type plays is described
     *        in the comment of the error number constant.
     * @param containingGraph the graph which contains the wrong objects.
     *
     * @see #NOT_DEFINED
     * @see #TYPE_UNDEFINED
     */
    public TypeError(int errorNumber, String message, Type wrongType,
            Graph containingGraph) {
        this.message = message;
        this.errorNumber = errorNumber;
        this.wrongType = wrongType;
        this.containingGraph = containingGraph;
    }

    /**
     * Creates an error object. The values cannot be changed after creation.
     *
     * @param errorNumber a code for the error that occurred. As described above (see
     *        {@link #NOT_DEFINED}), the code also defines which other parameters are set.
     * @param message a short English description of the error. The description
     *        should not contain more information than given by the errorNumber and the
     *        other parameters.
     * @param wrongObject the invalid {@link GraphObject}.
     * @param wrongType the invalid {@link Type}. The roles the GraphObject and the
     *        Type play are described in the comment of the error number constant.
     * @param containingGraph the graph which contains the wrong objects.
     *
     * @see #NOT_DEFINED
     * @see #NO_TYPE_GRAPH
     * @see #TYPE_UNDEFINED
     * @see #TYPE_ALREADY_DEFINED
     * @see #TYPE_UNKNOWN_HERE
     * @see #NO_SUCH_TYPE
     * @see #TO_MUCH_ARCS
     * @see #TO_LESS_ARCS
     */
    public TypeError(int errorNumber, String message, GraphObject wrongObject,
            Type wrongType, Graph containingGraph) {
        this.message = message;
        this.errorNumber = errorNumber;
        this.wrongType = wrongType;
        this.wrongObject = wrongObject;
        this.containingGraph = containingGraph;
    }

    /**
     * Returns the error number. As described above (see {@link #NOT_DEFINED}),
     * the code also defines which other parameters are set.
     *
     * @return the error number code
     *
     * @see #NOT_DEFINED
     * @see #NO_TYPE_GRAPH
     * @see #TYPE_UNDEFINED
     * @see #TYPE_ALREADY_DEFINED
     * @see #TYPE_UNKNOWN_HERE
     * @see #NO_SUCH_TYPE
     * @see #TO_MUCH_ARCS
     * @see #TO_LESS_ARCS
     */
    public int getErrorNumber() {
        return this.errorNumber;
    }

    /**
     * Returns a short English description of the error. The description should
     * not contain more information than given by the errorNumber and the other
     * parameters.
     *
     * @return a short description of the error
     *
     * @see #getErrorNumber()
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns the GraphObject of the error. Which role this object plays is
     * described in the comment for the error number constant (see
     * {@link #NOT_DEFINED}).
     *
     * @return the invalid graph object, or null if not set
     */
    public GraphObject getGraphObject() {
        return this.wrongObject;
    }

    /**
     * Returns the Type of the error. Which role this object plays is described in
     * the comment for the error number constant (see {@link #NOT_DEFINED}).
     *
     * @return the invalid type, or null if not set
     */
    public Type getType() {
        return this.wrongType;
    }

    /**
     * Returns the graph which was checked and which contains the errors.
     *
     * @return the graph containing the error, or null if not set
     */
    public Graph getContainingGraph() {
        return this.containingGraph;
    }

    /**
     * Sets the graph containing the error.
     *
     * @param containingGraph the graph to set as containing the error
     */
    public void setContainingGraph(Graph containingGraph) {
        this.containingGraph = containingGraph;
    }

    /**
     * Returns a short string with error number and message for testing purposes.
     *
     * @return a string representation of this error
     */
    @Override
    public String toString() {
        return "TypeError: " + this.getMessage() + " [" + this.getErrorNumber()
                + "] in " + this.getContainingGraph().getName();
    }
}
