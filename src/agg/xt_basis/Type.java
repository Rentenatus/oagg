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
package agg.xt_basis;

import agg.attribute.AttrType;
import agg.util.XMLObject;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Instances of this class are used for dynamic typing of graph objects. Each type is associated with a name (also
 * called "string representation"). Note that two types with the same name need not be equal.
 *
 * @version $Id: Type.java,v 1.27 2010/10/07 20:04:26 olga Exp $
 */
public interface Type extends XMLObject {

    public static final int UNDEFINED = -1;

    /**
     * Returns the string representation. Mostly used as the name of the type.
     */
    public String getStringRepr();

    public boolean hasParent();

    /**
     * Returns the last direct parent.
     */
    public Type getParent();

    public List<Type> getParents();

    public void removeChild(final Type t);

    /**
     * Returns the name of the type.
     */
    public String getName();

    /**
     * Sets the string representation. Mostly used as the name of the type
     */
    public void setStringRepr(String n);

    /**
     * Sets the parent.
     */
    public void setParent(Type t);

    public void addParent(Type t);

    public void removeParent(Type t);

    public boolean hasChild();

    /**
     * Returns its direct children only.
     */
    public List<Type> getChildren();

    /**
     * Returns the associated attribute type.
     */
    public AttrType getAttrType();

    /**
     * compares the given type with this object.
     *
     * @return true, if the given type has the same name, attributes and additional string
     */
    public boolean compareTo(Type t);

    /**
     * set an additional graphical string, which is saved together with the name string representation. Here you can
     * save additional information used in another layer. Predefined additional string: if the specified String repr is
     * "NODE" or "[NODE]", then additionalRepr = ":RECT:java.awt.Color[r=0,g=0,b=0]::[NODE]:" , if the specified String
     * repr is "EDGE" or "[EDGE]", then additionalRepr = ":SOLID_LINE:java.awt.Color[r=0,g=0,b=0]::[EDGE]:" . This
     * format of additional type information is used for the graphical layout information of nodes and edges.
     */
    public void setAdditionalRepr(String repr);

    /**
     * returns the additional string
     *
     * @see #setAdditionalRepr
     */
    public String getAdditionalRepr();

    public void setImageFilename(String imageFilename);

    public String getImageFilename();

    public void setTextualComment(String comment);

    public String getTextualComment();

    public boolean isArcType();

    public boolean isNodeType();

    public boolean isAttrTypeEmpty();

    public boolean isParentAttrTypeEmpty();

    public boolean hasAnyAttrMember();

    public void removeAttributeType();

    public void setAbstract(boolean b);

    public boolean isAbstract();

    public List<Type> getClan();

    /**
     * Returns true when this node type is in inheritance clan of the defined node type t. The type t can be a parent or
     * a child of the clan.
     */
    public boolean isInClanOf(final Type t);

    public boolean hasCommonParentWith(final Type t);

    /**
     * compares the given type with this object and its ancestors
     *
     * @return true, if the given type or one of its ancestors has the same name, attributes and additional string
     */
    public boolean isChildOf(Type t);

    /**
     * compares the given type and its ancestors with this object
     *
     * @return true, if the given type has the same name, attributes and additional string as t or one of its ancestors
     */
    public boolean isParentOf(Type t);

    /**
     * Finds out if there is any relation between this type and the given one. Two types are related if they have one
     * common ancestor.
     */
    public boolean isRelatedTo(Type t);

    /**
     * returns a list with all the parents of the current type and itself as first element
     *
     * @return list of all parents
     */
    public Vector<Type> getAllParents();

    public void addChild(final Type t);

    public List<Type> getCommonParentWith(final Type t);

    /**
     * returns a list with all the children of the current type and itself as first element
     *
     * @return list of all children
     */
    public Vector<Type> getAllChildren();

    /**
     * returns a new string containing the name, all attributes and the additional string separated by ":" if the key
     * string was null otherwise returns the old key.
     */
    public String convertToKey();

    /**
     * returns a new string containing the name, all attributes and the additional string separated by ":".
     */
    public String resetKey();

    /**
     * Add the given GraphObject to this type. The GraphObject is a node or an arc of a TypeGraph. Only one GraphObject
     * of each type is allowed.
     *
     * @return true, if the graph object could be added.
     */
    public boolean addTypeGraphObject(GraphObject nodeOrArc);

    /**
     * Remove the given GraphObject of the type graph from this type. The GraphObject must be added before. Returns true
     * if this type graph object is removed, otherwise false (esp. if the type is in use and the type graph check is
     * activated).
     */
    public boolean removeTypeGraphObject(GraphObject nodeOrArc);

    public boolean removeTypeGraphObject(GraphObject nodeOrArc, boolean forceToRemove);

    /**
     * returns true, if there is at least one object in the type graph for this type.
     */
    public boolean isTypeGraphObjectDefined();

    /**
     * returns a type graph node, if it is defined.
     */
    public Node getTypeGraphNodeObject();

    public boolean hasTypeGraphNode();

    public void setVisibilityOfObjectsOfTypeGraphNode(boolean vis);

    public void setVisibityOfObjectsOfTypeGraphArc(final Type sourceType, final Type targetType, boolean vis);

    public boolean isObjectOfTypeGraphNodeVisible();

    public boolean isObjectOfTypeGraphArcVisible(final Type sourceType, final Type targetType);

    /**
     * returns a type graph edge, if it is defined.
     */
    public Arc getTypeGraphArcObject(Type sourceType, Type targetType);

    public boolean hasTypeGraphArc();

    /**
     * returns a collection of defined graph type edges.
     *
     * public HashMap getTypeGraphEdgeObjects();
     */
    /**
     * disable type graph object of this type.
     */
    public void disableTypeGraphObject();

    public void setSourceMin(Type sourceType, Type targetType, int value);

    public void setSourceMax(Type sourceType, Type targetType, int value);

    public void setTargetMin(Type sourceType, Type targetType, int value);

    public void setTargetMax(Type sourceType, Type targetType, int value);

    public int getSourceMin(Type sourceType, Type targetType);

    public int getSourceMax(Type sourceType, Type targetType);

    public int getTargetMin(Type sourceType, Type targetType);

    public int getTargetMax(Type sourceType, Type targetType);

    public void setSourceMin(int value);

    public void setSourceMax(int value);

    public int getSourceMin();

    public int getSourceMax();

    public boolean hasInheritedAttribute();

    public TypeGraphNode getTypeGraphNode();

    public void createAttributeType();

    public boolean hasTypeGraphArc(final Type sourceType);

    public boolean hasTypeGraphArc(final Type sourceType, final Type targetType);

    public boolean hasTypeGraphArc(final GraphObject sourceType, final GraphObject targetType);

    public boolean isEdgeCreatable(final Type sourceType, final Type targetType, final int level);

    public Vector<Type> getTargetsOfArc(final Type sourceType);

    public int getMaxMultiplicityOfAllChildren();

    public int getMinMultiplicityOfAllChildren();

    public HashMap<Type, HashMap<Type, TypeGraphArc>> getArcTypeGraphObjects();

    public TypeGraphArc getTypeGraphArc(final Type sourceType, final Type targetType);

    public TypeGraphArc getSimilarTypeGraphArc(final Type sourceType, final Type targetType);

    public List<Arc> getOwnIncomingArcs();

    public Vector<Type> getOwnIncomingArcTypes();

    public List<Arc> getOwnOutgoingArcs();

    public Vector<Type> getOwnOutgoingArcTypes();

    public void checkDoubleAttributeType();

    public void adaptTypeAttribute(final Type type);

    public boolean compareTypeGraphArcs(final Type t);

    public boolean compareTypeGraphArcsMultiplicity(final Type t);

    public void dispose();

}
