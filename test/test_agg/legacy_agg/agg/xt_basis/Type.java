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

import agg.attribute.AttrType;
import agg.util.XMLObject;
import java.util.HashMap;
import java.util.List;

/**
 * Instances of this interface are used for dynamic typing of graph objects. Each
 * type is associated with a name (also called "string representation"). Note
 * that two types with the same name need not be equal.
 *
 * <p>This interface provides methods for managing type hierarchies, attribute types,
 * and type graph validation.
 */
public interface Type extends XMLObject {

    /**
     * Constant representing an undefined type.
     */
    int UNDEFINED = -1;

    /**
     * Returns the string representation of this type. Mostly used as the name of the type.
     *
     * @return the string representation of this type
     */
    String getStringRepr();

    /**
     * Checks if this type has a parent type.
     *
     * @return true if this type has at least one parent, false otherwise
     */
    boolean hasParent();

    /**
     * Returns the last direct parent of this type.
     *
     * @return the last direct parent type, or null if this type has no parent
     */
    Type getParent();

    /**
     * Returns all direct parents of this type.
     *
     * @return a list of all direct parent types
     */
    List<Type> getParents();

    /**
     * Removes the specified child type from this type's children.
     *
     * @param t the child type to remove
     */
    void removeChild(final Type t);

    /**
     * Returns the name of this type.
     *
     * @return the name of this type
     */
    String getName();

    /**
     * Sets the string representation of this type. Mostly used as the name of the type.
     *
     * @param n the new string representation
     */
    void setStringRepr(String n);

    /**
     * Sets the parent type of this type. This replaces any existing parent.
     *
     * @param t the new parent type
     */
    void setParent(Type t);

    /**
     * Adds a parent type to this type. This type can have multiple parents.
     *
     * @param t the parent type to add
     */
    void addParent(Type t);

    /**
     * Removes a parent type from this type.
     *
     * @param t the parent type to remove
     */
    void removeParent(Type t);

    /**
     * Checks if this type has at least one child type.
     *
     * @return true if this type has children, false otherwise
     */
    boolean hasChild();

    /**
     * Returns the direct children of this type only.
     *
     * @return a list of direct child types
     */
    List<Type> getChildren();

    /**
     * Returns the associated attribute type for this type.
     *
     * @return the attribute type, or null if no attribute type is associated
     */
    AttrType getAttrType();

    /**
     * Compares the given type with this object.
     *
     * @param t the type to compare with
     * @return true if the given type has the same name, attributes and additional string
     */
    boolean compareTo(Type t);

    /**
     * Sets an additional graphical string, which is saved together with the name
     * string representation. Here you can save additional information used in
     * another layer.
     *
     * <p>Predefined additional strings:
     * <ul>
     *   <li>If the specified String repr is "NODE" or "[NODE]", then additionalRepr =
     *       ":RECT:java.awt.Color[r=0,g=0,b=0]::[NODE]:"</li>
     *   <li>If the specified String repr is "EDGE" or "[EDGE]", then additionalRepr =
     *       ":SOLID_LINE:java.awt.Color[r=0,g=0,b=0]::[EDGE]:"</li>
     * </ul>
     * This format of additional type information is used for the graphical layout
     * information of nodes and edges.
     *
     * @param repr the additional graphical string representation
     */
    void setAdditionalRepr(String repr);

    /**
     * Returns the additional string representation.
     *
     * @return the additional string representation
     * @see #setAdditionalRepr(String)
     */
    String getAdditionalRepr();

    /**
     * Sets the filename for an image associated with this type.
     *
     * @param imageFilename the image filename to set
     */
    void setImageFilename(String imageFilename);

    /**
     * Returns the filename for an image associated with this type.
     *
     * @return the image filename, or null if not set
     */
    String getImageFilename();

    /**
     * Sets a textual comment for this type.
     *
     * @param comment the textual comment to set
     */
    void setTextualComment(String comment);

    /**
     * Returns the textual comment for this type.
     *
     * @return the textual comment, or null if not set
     */
    String getTextualComment();

    /**
     * Checks if this type is an arc type.
     *
     * @return true if this type represents an arc type, false otherwise
     */
    boolean isArcType();

    /**
     * Checks if this type is a node type.
     *
     * @return true if this type represents a node type, false otherwise
     */
    boolean isNodeType();

    /**
     * Checks if this type has no attribute type defined.
     *
     * @return true if this type has no attribute type, false otherwise
     */
    boolean isAttrTypeEmpty();

    /**
     * Checks if the parent type of this type has no attribute type defined.
     *
     * @return true if the parent type has no attribute type, false otherwise
     */
    boolean isParentAttrTypeEmpty();

    /**
     * Checks if this type or any of its parents has any attribute member.
     *
     * @return true if there is at least one attribute member in the hierarchy, false otherwise
     */
    boolean hasAnyAttrMember();

    /**
     * Removes the attribute type from this type.
     */
    void removeAttributeType();

    /**
     * Sets whether this type is abstract.
     *
     * @param b true to make this type abstract, false otherwise
     */
    void setAbstract(boolean b);

    /**
     * Checks if this type is abstract.
     *
     * @return true if this type is abstract, false otherwise
     */
    boolean isAbstract();

    /**
     * Returns the clan of this type, which includes all types in the inheritance hierarchy.
     *
     * @return a list of all types in the clan
     */
    List<Type> getClan();

    /**
     * Checks if this type is in the inheritance clan of the specified type.
     * The type t can be a parent or a child of the clan.
     *
     * @param t the type to check against
     * @return true if this type is in the clan of t, false otherwise
     */
    boolean isInClanOf(final Type t);

    /**
     * Checks if this type has a common parent with the specified type.
     *
     * @param t the type to check against
     * @return true if there is a common parent, false otherwise
     */
    boolean hasCommonParentWith(final Type t);

    /**
     * Checks if this type is a child of the specified type by comparing
     * this object with the given type and its ancestors.
     *
     * @param t the potential parent type to check
     * @return true if the given type or one of its ancestors has the same name,
     *         attributes and additional string as this type
     */
    boolean isChildOf(Type t);

    /**
     * Checks if this type is a parent of the specified type by comparing
     * the given type and its ancestors with this object.
     *
     * @param t the potential child type to check
     * @return true if the given type has the same name, attributes and additional
     *         string as this type or one of its ancestors
     */
    boolean isParentOf(Type t);

    /**
     * Checks if there is any relation between this type and the given one.
     * Two types are related if they have at least one common ancestor.
     *
     * @param t the type to check for relation
     * @return true if the types are related, false otherwise
     */
    boolean isRelatedTo(Type t);

    /**
     * Returns a list with all the parents of the current type and itself as first element.
     *
     * @return a list containing this type and all its parents
     */
    List<Type> getAllParents();

    /**
     * Adds a child type to this type.
     *
     * @param t the child type to add
     */
    void addChild(final Type t);

    /**
     * Returns a list of common parent types between this type and the specified type.
     *
     * @param t the type to find common parents with
     * @return a list of common parent types
     */
    List<Type> getCommonParentWith(final Type t);

    /**
     * Returns a list with all the children of the current type and itself as first element.
     *
     * @return a list containing this type and all its children
     */
    List<Type> getAllChildren();

    /**
     * Returns a new string containing the name, all attributes and the additional
     * string separated by ":". If the key string was null, returns the old key.
     *
     * @return a key string representation of this type
     */
    String convertToKey();

    /**
     * Returns a new string containing the name, all attributes and the additional
     * string separated by ":".
     *
     * @return a reset key string representation of this type
     */
    String resetKey();

    /**
     * Checks if the given graph object is validly typed as defined in the type graph.
     * Before this can be checked, all edges and nodes of the type graph must be added
     * to their types. The given object will not be tested if this is its type.
     *
     * @param graphObject the graph object to test
     * @param level a type graph check level, as defined in {@link TypeSet#setLevelOfTypeGraphCheck}
     * @return null if the graph object is validly typed, otherwise a {@link TypeError}
     *         describing the mismatch
     */
    TypeError check(GraphObject graphObject, int level);

    /**
     * Checks if the given node can be removed. This check only makes sense if the
     * minimum multiplicity check is activated.
     *
     * @param node the node which will be removed
     * @param level the actual level. If not set to {@link TypeSet#ENABLED_MAX_MIN},
     *        this method will do nothing.
     * @return null if the node will remain validly typed even after removing,
     *         otherwise a {@link TypeError} containing the possible fault
     */
    TypeError checkIfRemovable(Node node, int level);

    /**
     * Checks if the given arc can be removed from the given node so the node would
     * remain validly typed. This check only makes sense if the minimum multiplicity
     * check is activated.
     *
     * @param node the node which will be modified. This node has to be the source
     *        of the arc and has to have this type.
     * @param arc the arc which will be removed
     * @param level the actual level. If not set to {@link TypeSet#ENABLED_MAX_MIN},
     *        this method will do nothing.
     * @return null if the node will remain validly typed even after removing the arc,
     *         otherwise a {@link TypeError} containing the possible fault
     */
    TypeError checkIfRemovableFromSource(GraphObject node, Arc arc, int level);

    /**
     * Checks if the given arc can be removed from the given node so the node would
     * remain validly typed. This check only makes sense if the minimum multiplicity
     * check is activated.
     *
     * @param node the node which will be modified. This node has to be the source
     *        of the arc and has to have this type.
     * @param arc the arc which will be removed
     * @param deleteSrc whether to delete the source
     * @param deleteTar whether to delete the target
     * @param level the actual level. If not set to {@link TypeSet#ENABLED_MAX_MIN},
     *        this method will do nothing.
     * @return null if the node will remain validly typed even after removing the arc,
     *         otherwise a {@link TypeError} containing the possible fault
     */
    TypeError checkIfRemovableFromSource(GraphObject node, Arc arc, boolean deleteSrc,
            boolean deleteTar, int level);

    /**
     * Checks if the given arc can be removed from the given node so the node would
     * remain validly typed. This check only makes sense if the minimum multiplicity
     * check is activated.
     *
     * @param node the node which will be modified. This node has to be the target
     *        of the arc and has to have this type.
     * @param arc the arc which will be removed
     * @param level the actual level. If not set to {@link TypeSet#ENABLED_MAX_MIN},
     *        this method will do nothing.
     * @return null if the node will remain validly typed even after removing the arc,
     *         otherwise a {@link TypeError} containing the possible fault
     */
    TypeError checkIfRemovableFromTarget(final GraphObject node, final Arc arc, int level);

    /**
     * Checks if the given arc can be removed from the given node so the node would
     * remain validly typed. This check only makes sense if the minimum multiplicity
     * check is activated.
     *
     * @param node the node which will be modified. This node has to be the target
     *        of the arc and has to have this type.
     * @param arc the arc which will be removed
     * @param deleteSrc whether to delete the source
     * @param deleteTar whether to delete the target
     * @param level the actual level. If not set to {@link TypeSet#ENABLED_MAX_MIN},
     *        this method will do nothing.
     * @return null if the node will remain validly typed even after removing the arc,
     *         otherwise a {@link TypeError} containing the possible fault
     */
    TypeError checkIfRemovableFromTarget(final GraphObject node, final Arc arc,
            boolean deleteSrc, boolean deleteTar, int level);

    /**
     * Adds the given graph object to this type. The graph object is a node or an
     * arc of a TypeGraph. Only one graph object of each type is allowed.
     *
     * @param nodeOrArc the graph object (node or arc) to add
     * @return true if the graph object could be added, false otherwise
     */
    boolean addTypeGraphObject(GraphObject nodeOrArc);

    /**
     * Removes the given graph object of the type graph from this type.
     * The graph object must be added before. Returns true if this type graph object
     * is removed, otherwise false (especially if the type is in use and the type
     * graph check is activated).
     *
     * @param nodeOrArc the graph object to remove
     * @return true if the graph object was removed, false otherwise
     */
    boolean removeTypeGraphObject(GraphObject nodeOrArc);

    /**
     * Removes the given graph object of the type graph from this type.
     *
     * @param nodeOrArc the graph object to remove
     * @param forceToRemove if true, forces the removal even if type is in use
     * @return true if the graph object was removed, false otherwise
     */
    boolean removeTypeGraphObject(GraphObject nodeOrArc, boolean forceToRemove);

    /**
     * Checks if there is at least one object in the type graph for this type.
     *
     * @return true if a type graph object is defined, false otherwise
     */
    boolean isTypeGraphObjectDefined();

    /**
     * Returns a type graph node, if it is defined.
     *
     * @return the type graph node, or null if not defined
     */
    Node getTypeGraphNodeObject();

    /**
     * Checks if this type has a type graph node.
     *
     * @return true if this type has a type graph node, false otherwise
     */
    boolean hasTypeGraphNode();

    /**
     * Sets the visibility of objects of the type graph node.
     *
     * @param vis true to make objects visible, false to hide them
     */
    void setVisibilityOfObjectsOfTypeGraphNode(boolean vis);

    /**
     * Sets the visibility of objects of the type graph arc between source and target types.
     *
     * @param sourceType the source type of the arc
     * @param targetType the target type of the arc
     * @param vis true to make objects visible, false to hide them
     */
    void setVisibityOfObjectsOfTypeGraphArc(final Type sourceType, final Type targetType,
            boolean vis);

    /**
     * Checks if objects of the type graph node are visible.
     *
     * @return true if objects are visible, false otherwise
     */
    boolean isObjectOfTypeGraphNodeVisible();

    /**
     * Checks if objects of the type graph arc between source and target types are visible.
     *
     * @param sourceType the source type of the arc
     * @param targetType the target type of the arc
     * @return true if objects are visible, false otherwise
     */
    boolean isObjectOfTypeGraphArcVisible(final Type sourceType, final Type targetType);

    /**
     * Returns a type graph arc, if it is defined.
     *
     * @param sourceType the source type of the arc
     * @param targetType the target type of the arc
     * @return the type graph arc, or null if not defined
     */
    Arc getTypeGraphArcObject(Type sourceType, Type targetType);

    /**
     * Checks if this type has any type graph arc.
     *
     * @return true if this type has at least one type graph arc, false otherwise
     */
    boolean hasTypeGraphArc();

    /**
     * Disables the type graph object of this type.
     */
    void disableTypeGraphObject();

    /**
     * Sets the minimum multiplicity for arcs from the source type to the target type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @param value the minimum multiplicity value
     */
    void setSourceMin(Type sourceType, Type targetType, int value);

    /**
     * Sets the maximum multiplicity for arcs from the source type to the target type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @param value the maximum multiplicity value
     */
    void setSourceMax(Type sourceType, Type targetType, int value);

    /**
     * Sets the minimum multiplicity for arcs to the target type from the source type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @param value the minimum multiplicity value
     */
    void setTargetMin(Type sourceType, Type targetType, int value);

    /**
     * Sets the maximum multiplicity for arcs to the target type from the source type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @param value the maximum multiplicity value
     */
    void setTargetMax(Type sourceType, Type targetType, int value);

    /**
     * Returns the minimum multiplicity for arcs from the source type to the target type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @return the minimum multiplicity value
     */
    int getSourceMin(Type sourceType, Type targetType);

    /**
     * Returns the maximum multiplicity for arcs from the source type to the target type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @return the maximum multiplicity value
     */
    int getSourceMax(Type sourceType, Type targetType);

    /**
     * Returns the minimum multiplicity for arcs to the target type from the source type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @return the minimum multiplicity value
     */
    int getTargetMin(Type sourceType, Type targetType);

    /**
     * Returns the maximum multiplicity for arcs to the target type from the source type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @return the maximum multiplicity value
     */
    int getTargetMax(Type sourceType, Type targetType);

    /**
     * Sets the minimum multiplicity for arcs from this type.
     *
     * @param value the minimum multiplicity value
     */
    void setSourceMin(int value);

    /**
     * Sets the maximum multiplicity for arcs from this type.
     *
     * @param value the maximum multiplicity value
     */
    void setSourceMax(int value);

    /**
     * Returns the minimum multiplicity for arcs from this type.
     *
     * @return the minimum multiplicity value
     */
    int getSourceMin();

    /**
     * Returns the maximum multiplicity for arcs from this type.
     *
     * @return the maximum multiplicity value
     */
    int getSourceMax();

    /**
     * Checks if the given node can be created in the basis graph.
     *
     * @param basisGraph the graph to check
     * @param levelOfTypeGraphCheck the level of type graph check to apply
     * @return a TypeError if the node cannot be created, null otherwise
     */
    TypeError checkIfNodeCreatable(Graph basisGraph, int levelOfTypeGraphCheck);

    /**
     * Checks if this type has inherited attributes from its parent types.
     *
     * @return true if this type has inherited attributes, false otherwise
     */
    boolean hasInheritedAttribute();

    /**
     * Returns the type graph node for this type.
     *
     * @return the type graph node, or null if not defined
     */
    TypeGraphNode getTypeGraphNode();

    /**
     * Creates an attribute type for this type.
     */
    void createAttributeType();

    /**
     * Checks if this type has a type graph arc from the specified source type.
     *
     * @param sourceType the source type to check
     * @return true if there is a type graph arc, false otherwise
     */
    boolean hasTypeGraphArc(final Type sourceType);

    /**
     * Checks if this type has a type graph arc from the specified source type to
     * the specified target type.
     *
     * @param sourceType the source type to check
     * @param targetType the target type to check
     * @return true if there is a type graph arc, false otherwise
     */
    boolean hasTypeGraphArc(final Type sourceType, final Type targetType);

    /**
     * Checks if this type has a type graph arc from the specified source graph object
     * to the specified target graph object.
     *
     * @param sourceType the source graph object to check
     * @param targetType the target graph object to check
     * @return true if there is a type graph arc, false otherwise
     */
    boolean hasTypeGraphArc(final GraphObject sourceType, final GraphObject targetType);

    /**
     * Checks if an edge can be created between the specified source and target types
     * at the given level.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @param level the level to check
     * @return true if the edge can be created, false otherwise
     */
    boolean isEdgeCreatable(final Type sourceType, final Type targetType, final int level);

    /**
     * Returns all target types of arcs from the specified source type.
     *
     * @param sourceType the source type
     * @return a list of target types
     */
    List<Type> getTargetsOfArc(final Type sourceType);

    /**
     * Returns the maximum multiplicity of all children of this type.
     *
     * @return the maximum multiplicity value
     */
    int getMaxMultiplicityOfAllChildren();

    /**
     * Returns the minimum multiplicity of all children of this type.
     *
     * @return the minimum multiplicity value
     */
    int getMinMultiplicityOfAllChildren();

    /**
     * Returns a map of all arc type graph objects for this type.
     *
     * @return a map from source types to maps of target types to type graph arcs
     */
    HashMap<Type, HashMap<Type, TypeGraphArc>> getArcTypeGraphObjects();

    /**
     * Returns the type graph arc from the specified source type to the specified target type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @return the type graph arc, or null if not found
     */
    TypeGraphArc getTypeGraphArc(final Type sourceType, final Type targetType);

    /**
     * Returns a similar type graph arc from the specified source type to the specified target type.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @return a similar type graph arc, or null if not found
     */
    TypeGraphArc getSimilarTypeGraphArc(final Type sourceType, final Type targetType);

    /**
     * Returns all incoming arcs that are directly defined on this type (not inherited).
     *
     * @return a list of incoming arcs
     */
    List<Arc> getOwnIncomingArcs();

    /**
     * Returns all types of incoming arcs that are directly defined on this type (not inherited).
     *
     * @return a list of incoming arc types
     */
    List<Type> getOwnIncomingArcTypes();

    /**
     * Returns all outgoing arcs that are directly defined on this type (not inherited).
     *
     * @return a list of outgoing arcs
     */
    List<Arc> getOwnOutgoingArcs();

    /**
     * Returns all types of outgoing arcs that are directly defined on this type (not inherited).
     *
     * @return a list of outgoing arc types
     */
    List<Type> getOwnOutgoingArcTypes();

    /**
     * Checks for double attribute types in this type.
     */
    void checkDoubleAttributeType();

    /**
     * Adapts this type's attribute type to match the specified type.
     *
     * @param type the type to adapt to
     */
    void adaptTypeAttribute(final Type type);

    /**
     * Checks if an edge can be created between the specified source and target nodes.
     *
     * @param src the source node
     * @param tar the target node
     * @param level the level of type checking to apply
     * @return a TypeError if the edge cannot be created, null otherwise
     */
    TypeError checkIfEdgeCreatable(final Node src, final Node tar, final int level);

    /**
     * Checks if an edge can be created between the specified source and target nodes
     * in the specified graph.
     *
     * @param g the graph to check
     * @param src the source node
     * @param tar the target node
     * @param level the level of type checking to apply
     * @return a TypeError if the edge cannot be created, null otherwise
     */
    TypeError checkIfEdgeCreatable(final Graph g, final Node src, final Node tar, final int level);

    /**
     * Checks if the source maximum multiplicity is exceeded for an edge between the
     * specified nodes.
     *
     * @param g the graph to check
     * @param src the source node
     * @param tar the target node
     * @return a TypeError if the maximum is exceeded, null otherwise
     */
    TypeError checkSourceMax(final Graph g, final Node src, final Node tar);

    /**
     * Checks if the target maximum multiplicity is exceeded for an edge between the
     * specified nodes.
     *
     * @param g the graph to check
     * @param src the source node
     * @param tar the target node
     * @return a TypeError if the maximum is exceeded, null otherwise
     */
    TypeError checkTargetMax(final Graph g, final Node src, final Node tar);

    /**
     * Compares the type graph arcs of this type with the specified type.
     *
     * @param t the type to compare with
     * @return true if the type graph arcs match, false otherwise
     */
    boolean compareTypeGraphArcs(final Type t);

    /**
     * Compares the type graph arcs multiplicity of this type with the specified type.
     *
     * @param t the type to compare with
     * @return true if the type graph arcs multiplicity matches, false otherwise
     */
    boolean compareTypeGraphArcsMultiplicity(final Type t);

    /**
     * Disposes this type and releases any resources.
     */
    void dispose();
}
