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

import agg.attribute.AttrManager;
import agg.attribute.impl.AttrTupleManager;
import agg.attribute.impl.DeclMember;
import agg.attribute.impl.DeclTuple;
import agg.attribute.impl.ValueTuple;
import agg.cons.AtomConstraint;
import agg.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Manages the node/edge types of graphs. Especially the rules and host graphs of a graph transformation system (gratra)
 * should use the same type set.
 *
 * Implements methods for creation, deletion and manipulation of node/edge types, a type graph, inheritance relations
 * between node types, checking type validity.
 *
 * @version $Id: TypeSet.java,v 1.87 2010/12/02 19:37:59 olga Exp $
 * @author $Author: olga $
 */
public class TypeSet {

    /**
     * Value for unknown level of type graph check
     */
    public static final int UNDEFINED = -1;

    /**
     * Level of type graph check to disable type graph
     */
    public static final int DISABLED = 0;

    /**
     * Level of type graph check to enable inheritance relation only
     */
    public static final int ENABLED_INHERITANCE = 5;

    /**
     * Level of type graph check to enable type graph and no multiplicity
     */
    public static final int ENABLED = 10;

    /**
     * Level of type graph check to enable type graph and max multiplicity
     */
    public static final int ENABLED_MAX = 20;

    /**
     * Level of type graph check to enable type graph and max and min multiplicity
     */
    public static final int ENABLED_MAX_MIN = 30;

    /**
     * Level of type graph check to enable type graph and min multiplicity
     */
    public static final int ENABLED_MIN = 40;

    /**
     * returned instead of a list of {@link TypeError}s, if there were none.
     */
    private static final Collection<TypeError> SUCCESS = new ArrayList<>(0);

    /**
     * the types of the edges and nodes will be hold in this list
     */
    private final Vector<Type> types = new Vector<Type>();

    private boolean directed = true;
    private boolean parallel = true;
    private boolean emptyAttr = true;

    /**
     * dummy type for inheritance arcs, which are always without attributes
     */
    private final Type inheritanceType = new ArcTypeImpl();

    /**
     * inheritance edges within the type graph
     */
    private final Vector<Arc> inheritanceArcs = new Vector<Arc>();

    /**
     * manager for attributes
     */
    private final AttrManager attrManager = AttrTupleManager.getDefaultManager();

    /**
     * is true, if a type graph exists and it was successfully checked Only if true, a TypeSet is able to check the
     * types of graphs.
     */
    private boolean typeGraphIsProved = false;

    /**
     * holds the level of type graph check Possible values: null null null null null null null null null null null null
     * null null null null null null null null     {@link #DISABLED}, {@link #ENABLED},
	 * {@link #ENABLED_MAX}, {@link #ENABLED_MAX_MIN}
     */
    private int typeGraphLevel = DISABLED;

    private boolean newTypeGraphObjectImported;

    protected String info = "";

    /**
     * creates a new type manager. with no types defined and no type graph given.
     */
    public TypeSet() {
    }

    public TypeSet(boolean directedArcs, boolean parallelArcs) {
        this.directed = directedArcs;
        this.parallel = parallelArcs;
    }

    public void dispose() {

        for (int j = 0; j < this.types.size(); j++) {
            Type type = this.types.get(j);
            if (type.isArcType()) {
                type.dispose();
                this.types.remove(j);
                j--;
            }
        }
        for (int j = 0; j < this.types.size(); j++) {
            Type type = this.types.get(j);

            this.types.remove(j);
            type.dispose();
            j--;
        }
        // at this point the types should be already empty! 
        for (int j = 0; j < this.types.size(); j++) {
            Type type = this.types.get(j);
            if (!type.hasChild()) {
                type.dispose();
                this.types.remove(j);
                j--;
            }
        }

    }

    public void finalize() {
    }

    /**
     * Creates a new Type instance without attribute type. It is useful for non-attributed graphs.
     *
     * @deprecated replaced by <code>Type createNodeType(boolean withAttributes)</code> for node type and
     * <code>Type createArcType(boolean withAttributes)</code> for edge type
     */
    public final Type createType() {
        Type aType = createType(false);
        return aType;
    }

    /*
	 * Creates a new Type object. If the specified parameter is
	 * TRUE, the attribute type is created, too, otherwise the attribute type is
	 * null.
	 * 
	 * @deprecated  replaced by 
	 * 		<code>Type createNodeType(boolean withAttributes)</code> for node type
	 * and  
	 * 		<code>Type createArcType(boolean withAttributes)</code> for edge type
     */
    @SuppressWarnings("deprecation")
    public final Type createType(final boolean withAttributes) {
        if (withAttributes) {
            Type aType = new TypeImpl(this.attrManager.newType());
            this.types.add(aType);
            return aType;
        }
        Type aType = new TypeImpl();
        this.types.add(aType);
        return aType;
    }

    public final Type createNodeType(final boolean withAttributes) {
        if (withAttributes) {
            Type aType = new NodeTypeImpl(this.attrManager.newType());
            this.types.add(aType);
            return aType;
        }
        Type aType = new NodeTypeImpl();
        this.types.add(aType);
        return aType;
    }

    public final Type createArcType(final boolean withAttributes) {
        if (withAttributes) {
            Type aType = new ArcTypeImpl(this.attrManager.newType());
            this.types.add(aType);
            return aType;
        }
        Type aType = new ArcTypeImpl();
        this.types.add(aType);
        return aType;
    }

    public boolean isEmpty() {
        return (this.types.size() == 0) ? true : false;
    }

    /**
     * Iterate through all the valid types that may be given to a * GraphObject. Enumeration elements are of type
     * <code>Type</code>. *
     *
     * @see agg.xt_basis.Type
     */
    public final Iterator<Type> getTypes() {
        return this.types.iterator();
    }

    public final int getTypesCount() {
        return this.types.size();
    }

    /**
     *
     * @return a list with node/edge types
     */
    public final List<Type> getTypeList() {
        return this.types;
    }

    /**
     * @deprecated replaced by List<Type> getTypeList()
     * @return a list with node/edge types
     */
    public final Vector<Type> getTypesVec() {
        return this.types;
    }

    /**
     * Returns a set of inheritance edges. An inheritance edge is especial edge kind. It is not in the edge set of a
     * type graph and it cannot be get by typeSet.getTypeGraph().getArcs().
     */
    public final Vector<Arc> getInheritanceArcs() {
        return this.inheritanceArcs;
    }

    public final Type getInheritanceType() {
        return this.inheritanceType;
    }

    /**
     * Returns true, if a type graph exists and it is a type graph with node type inheritance, otherwise returns false.
     */
    public boolean hasInheritance() {
        return !this.inheritanceArcs.isEmpty();
    }

    /**
     * Returns true, if a type graph exists and it is a type graph with node type inheritance, otherwise returns false.
     */
    public boolean usesInheritance() {
        return !this.inheritanceArcs.isEmpty();
    }

    /**
     * Returns a type with the specified name and additional graphical representation if it is found, otherwise
     * <code>null</code>.
     */
    public Type getTypeByNameAndAdditionalRepr(final String name, final String addRepr) {
        for (int i = 0; i < this.types.size(); i++) {
            Type t = this.types.elementAt(i);
            if ((t.getStringRepr().equals(name)
                    || ("unnamed".equals(name) && "".equals(t.getStringRepr())))
                    && t.getAdditionalRepr().equals(addRepr)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns a type with the specified name if it is found, otherwise <code>null</code> Here the graphical
     * represantation of a node or edge type is not taken in account. That can be a problem, when there exists a node
     * type and an edge type with equal names. The method <c>Type getTypeByNameAndAdditionalRepr(String name, String
     * addRepr)</c> should be used.
     */
    public Type getTypeByName(final String name) {
        for (int i = 0; i < this.types.size(); i++) {
            Type t = this.types.elementAt(i);
            if (t.getStringRepr().equals(name)
                    || ("unnamed".equals(name) && "".equals(t.getStringRepr()))) {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns a type with the specified name or <code>null</code>
     *
     * @deprecated <code>Type getTypeByName(String name)</code> should be used
     */
    public Type getTypeForName(final String name) {
        for (int i = 0; i < this.types.size(); i++) {
            Type t = this.types.elementAt(i);
            if (t.getStringRepr().equals(name)
                    || ("unnamed".equals(name) && "".equals(t.getStringRepr()))) {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns a type which is similar to the specified type: the type names, the graphical representation such as
     * color, node shape of node type or edge style of edge type, the number of attributes, the name and type of
     * attribute member should be equal, otherwise returns <code>null</code>.
     */
    public Type getSimilarType(final Type t) {
        for (int i = 0; i < this.types.size(); i++) {
            Type ti = this.types.elementAt(i);
            if (ti.compareTo(t)) {
                return ti;
            }
        }
        return null;
    }

    /**
     * Returns the type graph node of the specified type or <code>null</code>.
     */
    public Node getTypeGraphNode(final Type t) {
        return t.getTypeGraphNodeObject();
    }

    public void setArcDirected(boolean b) {
        this.directed = b;
    }

    public boolean isArcDirected() {
        return this.directed;
    }

    public void setArcParallel(boolean b) {
        this.parallel = b;
    }

    public boolean isArcParallel() {
        return this.parallel;
    }

    /*
	 * Allows or prohibits empty attribute values of new objects
	 * of the RHS of a rule and also of the host graphs.
     */
    public void setAllowEmptyAttr(boolean b) {
        this.emptyAttr = b;
    }

    public boolean isEmptyAttrAllowed() {
        return this.emptyAttr;
    }

    /**
     * If the parameter <code>all</code> is true, then a copy of each type of the <code>otherTypes</code> will be
     * created, otherwise only of the not found types.
     */
    public void adaptTypes(final TypeSet otherTypes, final boolean all) {
        doAdaptTypes(otherTypes.getTypes(), all);
    }

    /**
     * If the parameter <code>all</code> is true, then a copy will be created of each type of the
     * <code>otherTypes</code>, otherwise only of not found type.
     */
    public void adaptTypes(final Iterator<Type> otherTypes, final boolean all) {
        doAdaptTypes(otherTypes, all);
    }

    private void doAdaptTypes(final Iterator<Type> otherTypes, final boolean all) {
        Vector<Pair<Type, Type>> v = new Vector<Pair<Type, Type>>(5);
        while (otherTypes.hasNext()) {
            Type t = otherTypes.next();
            Type similar = getTypeByNameAndAdditionalRepr(t.getStringRepr(), t
                    .getAdditionalRepr());

            if (similar == null) {
                Type type = adoptType(t);
                for (int i = 0; i < t.getParents().size(); i++) {
                    Type parType = t.getParents().get(i);
                    if (parType != null) {
                        v.add(new Pair<Type, Type>(type, parType));
                    }
                }
            } else {
                for (int i = 0; i < t.getParents().size(); i++) {
                    Type parType = t.getParents().get(i);
                    if (parType != null) {
                        v.add(new Pair<Type, Type>(similar, parType));
                    }
                }

                if (all) {
                    if (similar instanceof NodeTypeImpl) {
                        ((NodeTypeImpl) similar).adaptTypeAttribute(t);
                    } else if (similar instanceof ArcTypeImpl) {
                        ((ArcTypeImpl) similar).adaptTypeAttribute(t);
                    }
                }
            }
        }

        for (int i = 0; i < v.size(); i++) {
            Pair<Type, Type> pair = v.get(i);
            Type type = pair.first;
            Type parent = pair.second;
            Type itsParent = getTypeByNameAndAdditionalRepr(parent
                    .getName(), parent.getAdditionalRepr());
            if (itsParent != null) {
//				final TypeError error = 
                addInheritanceRelation(type, itsParent);
            }
        }

        while (otherTypes.hasNext()) {
            Type t = otherTypes.next();
            Type similar = getTypeByNameAndAdditionalRepr(t.getStringRepr(), t
                    .getAdditionalRepr());
            if (similar != null) {
                similar.adaptTypeAttribute(t);
            } else {
                System.out.println(this.getClass().getName() + ".doAdaptTypes::  FAILED type:  " + t.getStringRepr());
            }
        }
    }

    /**
     * Adds the given type to this type manager. This should only called with independent types, types can't be part of
     * two manager.
     */
    public Type addType(final Type aType) {
        if (aType.isNodeType()) {
            if (//!aType.isTypeUsed() &&
                    !aType.hasTypeGraphNode()) {
                this.types.add(aType);
                return aType;
            }
            return adoptType(aType);
        } else if (aType.isArcType()) {
            if (//!aType.isTypeUsed() &&
                    !aType.isTypeGraphObjectDefined()) {
                this.types.add(aType);
                return aType;
            }
            return adoptType(aType);
        } else {
            return null;
        }
    }

    public Type adoptClan(final Type aType) {
        if (this.containsType(aType)) {
            return aType;
        }
        Type t = this.getSimilarType(aType);
        if (t == null) {
            t = addType(aType);
        }
        if (t.getAdditionalRepr().indexOf("[NODE]") == -1) {
            t.setAdditionalRepr("[NODE]");
        }
        if (!aType.getParents().isEmpty()) {
            for (int i = 0; i < aType.getParents().size(); i++) {
                Type p = aType.getParents().get(i);
                Type type = adoptClan(p);
                t.addParent(type);
            }
        }
        return t;
    }

    /**
     * Creates a new type that is similar to the specified Type t.
     */
    public Type adoptType(final Type t) {
//		 System.out.println("TypeSet.adoptType: "+t.getName());
        Type type = null;
        boolean failed = false;

        DeclTuple otherTuple = (DeclTuple) t.getAttrType();
        // create a new type
        if (otherTuple != null) {
            if (t.isNodeType()) {
                type = new NodeTypeImpl(this.attrManager.newType());
            } else if (t.isArcType()) {
                type = new ArcTypeImpl(this.attrManager.newType());
            }

            if (type != null) {
                // create attribute members
                DeclTuple declTuple = (DeclTuple) type.getAttrType();
                for (int i = 0; i < otherTuple.getSize(); i++) {
                    DeclMember dm = (DeclMember) otherTuple.getMemberAt(i);
                    if (dm.getHoldingTuple() == otherTuple) {
                        Object dmnew = declTuple.addMember(
                                agg.attribute.facade.impl.DefaultInformationFacade
                                        .self().getJavaHandler(), otherTuple
                                        .getTypeAsString(i), otherTuple
                                .getNameAsString(i));
                        if (dmnew == null) {
                            failed = true;
                            break;
                        }
                    }
                }
            }
        } else if (t.isNodeType()) {
            type = new NodeTypeImpl();
        } else if (t.isArcType()) {
            type = new ArcTypeImpl();
        }

        if (type != null && !failed) {
            type.setAbstract(t.isAbstract());
            type.setStringRepr(t.getStringRepr());
            type.setAdditionalRepr(t.getAdditionalRepr());

            this.types.add(type);
        }

        return type;
    }

    /**
     * Returns true if the attribute members of the specified Type type could be added without a conflict to an existing
     * type which has the same name. A conflict could arise from the members which have an equal name but a different
     * type. In this case the name of an existing attribute member is extended by "?", the new attribute member is added
     * and the method returns false.
     */
    private boolean adaptTypeAttribute(final Type type) {
        Type myType = getTypeByName(type.getStringRepr()); // getName());
        if (myType != null) {
            if ((type.isNodeType() && myType.isNodeType())
                    || (type.isArcType() && myType.isArcType())) {
                myType.adaptTypeAttribute(type);
            }
        }
        return true;
    }

    /**
     * The {@link #adaptTypeAttribute(Type type)} method will be aplied to each element of the specified Vector
     * typesToAdapt.
     */
    private void adaptTypeAttribute(final Vector<Type> typesToAdapt) {
        for (int i = 0; i < typesToAdapt.size(); i++) {
            Type other_t = typesToAdapt.get(i);
            adaptTypeAttribute(other_t);
        }
        return;
    }

    /**
     * Returns true if the given type is managed by this object.
     */
    public boolean containsType(final Type type) {
        if (this.types.contains(type)) {
            return true;
        }
        return false;
    }

    /**
     * Dispose the specified type.
     */
    public void destroyType(final Type type) throws TypeException {
//		if (this.typeGraphLevel != DISABLED) {
//			if (type.isTypeGraphObjectDefined()) {
//				throw new TypeException(new TypeError(TypeError.TYPE_IS_IN_USE,
//						"There is at least one type graph object defined for \""
//								+ type.getName() + "\" type."
//								+ "\nPlease remove it before you remove the type.",
//						type, this.getTypeGraph()));
//			} 
//		}

        this.types.remove(type);

        type.dispose();
    }

    /**
     * Returns the AttrManager used to create attributed types
     */
    public AttrManager getAttrManager() {
        return this.attrManager;
    }

    TypeError checkTypeInTypeGraph(final Graph g, final Type edgeType, final GraphObject src,
            final GraphObject tar, final int tgl) {
        return edgeType.checkIfEdgeCreatable(g, (Node) src,
                (Node) tar, tgl);
    }

    /**
     * Checks, if the specified Arc is valid typed as defined in the type graph: existence and multiplicity constraints
     * of its type edge. The type graph must be proofed before.
     *
     * @param arc an arc in a graph
     * @param tgl the level to use. {@see #setTypegraphLevel()}
     * @return null if the arc is valid typed, otherwise an error object
     */
    TypeError checkTypeInTypeGraph(final Arc arc, final int tgl) {
        if (!arc.getType().hasTypeGraphArc()) {
            return new TypeError(TypeError.NO_SUCH_TYPE,
                    "The type graph does not contain an edge type with name \""
                    + arc.getType().getName() + "\" \nbetween node type \""
                    + arc.getSourceType().getName() + "\" and \""
                    + arc.getTargetType().getName() + "\""
                    + "\n ( see graph:  " + arc.getContext().getName() + " ).",
                    arc,
                    arc.getType());
        }
        // delegate multiplicity check to arc type
        return arc.getType().check(arc, tgl);
    }

    /**
     * Checks, if the specified Node is valid typed as defined in the type graph: existence and multiplicity constraints
     * of its type node. The type graph must be proofed before.
     *
     * @param node a node in a graph
     * @param tgl the level to use. {@see #setTypegraphLevel()}
     * @return null if the node is valid typed, otherwise an error object
     */
    TypeError checkTypeInTypeGraph(final Node node, final int tgl) {
        if (node.getType().getTypeGraphNodeObject() == null) {
            return new TypeError(TypeError.NO_SUCH_TYPE,
                    "The type graph does not contain a node type with name \""
                    + node.getType().getName() + "\""
                    + "\n ( see graph:  " + node.getContext().getName() + " ).",
                    node,
                    node.getType());
        }
        return checkMultiplicity(node, tgl);
    }

    public boolean isInheritanceArc(final Arc a) {
        if (a.getType() == this.inheritanceType && this.inheritanceArcs.contains(a)) {
            return true;
        }

        return false;
    }

    /**
     * To use this method, a type graph must to be created before.
     */
    public TypeError addInheritanceRelation(final Type child, final Type parent) {

        String childName = (child == null) ? "NULL" : child.getName();
        String parentName = (parent == null) ? "NULL" : parent.getName();
        return new TypeError(TypeError.UNKNOWN_ERROR,
                "The inharitance relation from type:  " + childName
                + "  to  " + parentName + "  "
                + "is not possible.");

    }

    /**
     * Returns a set with all edges to inherit from the specified parent type, or empty set.
     */
    public Vector<Arc> getInheritedArcs(final Type parentType) {
        Vector<Arc> inheritedArcs = new Vector<Arc>();
        Vector<Type> allparents = parentType.getAllParents();
        for (int i = 0; i < allparents.size(); i++) {
            Type p = allparents.get(i);
            Node go = p.getTypeGraphNodeObject();
            if (go != null) {
                Iterator<Arc> enOut = go.getOutgoingArcsSet().iterator();
                while (enOut.hasNext()) {
                    Arc a = enOut.next();
                    if (!a.isInheritance()) {
                        inheritedArcs.add(a);
                    }
                }
                Iterator<Arc> enIn = go.getIncomingArcsSet().iterator();
                while (enIn.hasNext()) {
                    Arc a = enIn.next();
                    if (!a.isInheritance()) {
                        inheritedArcs.add(a);
                    }
                }
            }
        }
        return inheritedArcs;
    }

    /**
     * Returns all child types of the given node type.
     *
     * @param t The type which children should be found.
     * @return a list of child types.
     */
    public List<Type> getClan(final Type t) {
        return t.getClan();
    }

    /**
     * checks the given graph, if it is valid typed. If the TypeSet of the graph is not this object, the types of the
     * nodes and edges must be also defined here. If in this TypeSet a proofed type graph is used, there must be a
     * morphism from the given graph into the type graph (But a different algorithmus will be used).
     *
     * @param graph the graph to check
     * @return an empty {@link Collection} if the given graph is valid typed. If there were type errors in the graph a
     * Collection with objects of class {@link TypeError} will returned. For each mismatch an error object will
     * delivered. You can check if there were some errors {@link Collection#isEmpty}.
     *
     * @see #checkTypeGraph
     */
    public Collection<TypeError> checkType(final Graph graph) {
        // count the type mismatches
        Vector<TypeError> errors = new Vector<TypeError>();
        // the given graph has another TypeSet
        checkTypeSet(graph, errors);

        // no type graph is defined/used
        // or the type graph is not proofed
        return errors;

    }

    // the given graph has another TypeSet
    private Vector<TypeError> checkTypeSet(final Graph graph, final Vector<TypeError> errors) {
        if (!this.equals(graph.getTypeSet())) {
            // checks all edges/arcs
            Iterator<?> en = graph.getArcsSet().iterator();
            GraphObject act;
            while (en.hasNext()) {
                act = (GraphObject) en.next();
                if (!this.types.contains(act.getType())) {
                    // if the type of this object is
                    // not defined here, increment the error counter
                    errors.add(new TypeError(
                            TypeError.TYPE_UNKNOWN_HERE,
                            "The edge type \""
                            + act.getType().getName()
                            + "\" used is not part of the grammars type set ( graph \""
                            + graph.getName() + "\" ).", act
                            .getType()));
                }
            }
            // check all nodes
            en = graph.getNodesSet().iterator();
            while (en.hasNext()) {
                act = (GraphObject) en.next();
                if (!this.types.contains(act.getType())) {
                    // if the type of this object is
                    // not defined here, increment the error counter
                    errors.add(new TypeError(
                            TypeError.TYPE_UNKNOWN_HERE,
                            "The node type \""
                            + act.getType().getName()
                            + "\" used is not part of the grammars type set ( graph \""
                            + graph.getName() + "\" ).", act
                            .getType()));
                }
            }
        }
        return errors;
    }

    /**
     * Returns an error object if the type multiplicity check failed after a node of the specified type created,
     * otherwise - null.
     */
    public TypeError canCreateNode(
            final Graph g,
            final Type nodeType,
            final int currentTypeGraphLevel) {

        if (currentTypeGraphLevel >= ENABLED_MAX) {
            List<Type> parents = nodeType.getAllParents();
            for (int i = 0; i < parents.size(); i++) {
                Type t = parents.get(i);
                int count = 0;
                int maxValue = t.getSourceMax();

                HashSet<GraphObject> set = g.getTypeObjectsMap().get(nodeType.convertToKey());
                if (set != null && !set.isEmpty()) {
                    count = g.getTypeObjectsMap().get(nodeType.convertToKey()).size();
                }

                if ((maxValue > 0) && (count + 1 > maxValue)) {
                    TypeError actError = new TypeError(TypeError.TO_MUCH_NODES,
                            "Too many nodes of type \"" + t.getName()
                            + "\".\nThere are only " + maxValue
                            + " allowed.", t);
                    actError.setContainingGraph(g);
                    return actError;
                }
            }
        }
        return null;
    }

    /**
     * Returns an error object if the type multiplicity check failed after an edge of the specified type created,
     * otherwise - null.
     */
    public TypeError canCreateArc(
            final Graph g,
            final Type edgeType,
            final Node source,
            final Node target,
            final int currentTypeGraphLevel) {

        return checkTypeInTypeGraph(g, edgeType, source, target,
                currentTypeGraphLevel);
    }

    /**
     * Checks edges of the specified graph due to type arc multiplicity of the specified type arc.
     *
     * @param typearc type arc of the current type graph
     * @param graph a graph to check (not a type graph)
     * @param currentTypeGraphLevel
     * @return null if multiplicity is satisfied, otherwise an error object
     */
    public TypeError checkEdgeTypeMultiplicity(
            final Arc typearc,
            final Graph graph,
            final int currentTypeGraphLevel) {

        final Iterator<GraphObject> list = graph.getElementsOfType(
                typearc.getType(),
                typearc.getSourceType(),
                typearc.getTargetType());
        while (list.hasNext()) {
            final Arc arc = (Arc) list.next();
            // delegate multiplicity check to arc type
            final TypeError error = arc.getType().check(arc, currentTypeGraphLevel);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    /**
     * Checks node type multiplicity of the specified node Type of the nodes of the specified Graph.
     *
     * @param nodeType node type of the current type graph
     * @param graph a graph to check
     * @param currentTypeGraphLevel
     * @return null if multiplicity is satisfied, otherwise an error object
     */
    public TypeError checkNodeTypeMultiplicity(
            final Type nodeType,
            final Graph graph,
            final int currentTypeGraphLevel) {

        TypeError actError = null;
        if (currentTypeGraphLevel > ENABLED) {
            HashSet<GraphObject> set = graph.getTypeObjectsMap().get(nodeType.convertToKey());
            if (set != null && !set.isEmpty()) {
                int nc = graph.getTypeObjectsMap().get(nodeType.convertToKey()).size();

                int maxValue = nodeType.getSourceMax();
                if ((maxValue != UNDEFINED) && (nc > maxValue)) {
                    actError = new TypeError(TypeError.TO_MUCH_NODES,
                            "Too many (" + nc + ") nodes of type \"" + nodeType.getName()
                            + "\".\nThere are only " + maxValue
                            + " allowed ( graph \""
                            + graph.getName() + "\" ).", graph);
                    return actError;
                }

                if (currentTypeGraphLevel == ENABLED_MAX_MIN
                        && graph.isCompleteGraph()) {
                    int minValue = nodeType.getSourceMin();
                    if (minValue > 0 && nc < minValue) {
                        actError = new TypeError(TypeError.TO_LESS_NODES,
                                "Not enough (" + nc + ") nodes of type \"" + nodeType.getName()
                                + "\".\nThere are at least " + minValue
                                + " needed ( graph \""
                                + graph.getName() + "\" ).", graph);
                        return actError;
                    }
                }
            }
        }
        /*
		for (int i = 0; i < nodeType.getAllParents().size(); i++) {
			Type t = nodeType.getAllParents().get(i);
			int nc = 0;
			int minValue = t.getSourceMin();
			int maxValue = t.getSourceMax();

			List<Type> clan = getClan(t);
			for (Iterator<Type> it = clan.iterator(); it.hasNext();) {
				Type clanMember = it.next();
				if (clanMember.getTypeGraphNode() != null) {
					List<Node> list = graph.getNodes(clanMember);
					if (list != null)
						nc += list.size();
				}
			}
			if (currentTypeGraphLevel >= ENABLED_MAX) {
				if ((maxValue != UNDEFINED) && (nc > maxValue)) {
					actError = new TypeError(TypeError.TO_MUCH_NODES,
							"Too many ("+nc+") nodes of type \"" + t.getName()
									+ "\".\nThere are only " + maxValue
									+ " allowed ( graph \""
									+ graph.getName() + "\" ).", graph);
					return actError;
				}
			}
			if (currentTypeGraphLevel == ENABLED_MAX_MIN
					&& graph.isCompleteGraph()) {
				if (minValue > 0 && nc < minValue) {
					actError = new TypeError(TypeError.TO_LESS_NODES,
							"Not enough ("+nc+") nodes of type \"" + t.getName()
									+ "\".\nThere are at least " + minValue
									+ " needed ( graph \""
									+ graph.getName() + "\" ).", graph);
					return actError;
				}
			}
		}
         */
        return null;
    }

    /**
     * Check node type multiplicity for the specified Node. The specified Node can be a new created node.
     *
     * @param n
     * @param currentTypeGraphLevel
     * @return null if node type multiplicity satisfied otherwise an error object
     */
    private TypeError checkMultiplicity(final Node n, final int currentTypeGraphLevel) {
        TypeError actError = null;
        if (currentTypeGraphLevel >= ENABLED_MAX) {
            Type nodeType = n.getType();
            List<Type> parents = nodeType.getAllParents();
            for (int i = 0; i < parents.size(); i++) {
                Type t = parents.get(i);
                int count = 0;
                int minValue = t.getSourceMin();
                int maxValue = t.getSourceMax();

                HashSet<GraphObject> set = n.getContext().getTypeObjectsMap().get(t.convertToKey());
                if (set != null && !set.isEmpty()) {
                    count = n.getContext().getTypeObjectsMap().get(t.convertToKey()).size();
                }

//				List<Type> clan = getClan(t);
//				for (Iterator<Type> it = clan.iterator(); it.hasNext();) {
//					Type member = it.next();
//					if (member.getTypeGraphNode() != null) {
//						List<Node> list = n.getContext().getNodes(member);
//						if (list != null)
//							count += list.size();
//					}
//				}
                if (!n.getContext().isNode(n)) {
                    count++; // a node is created and should be added to nodes of a graph
                }
                if ((maxValue != UNDEFINED) && (count > maxValue)) {
                    actError = new TypeError(TypeError.TO_MUCH_NODES,
                            "Too many (" + count + ") nodes of type \"" + t.getName()
                            + "\".\nThere are only " + maxValue
                            + " allowed ( graph \""
                            + n.getContext().getName() + "\" ).", n, t);
                    actError.setContainingGraph(n.getContext());
                    return actError;
                }
                if (currentTypeGraphLevel == ENABLED_MAX_MIN
                        && n.getContext().isCompleteGraph()) {
                    if (minValue > 0 && count < minValue) {
                        actError = new TypeError(TypeError.TO_LESS_NODES,
                                "Not enough (" + count + ") nodes of type \"" + t.getName()
                                + "\".\nThere are at least " + minValue
                                + " needed ( graph \""
                                + n.getContext().getName() + "\" ).", n, t);
                        actError.setContainingGraph(n.getContext());
                        return actError;
                    }
                }
            }
        }
        return null;
    }

    // now checks the nodes about min/max multiplicity of the type graph nodes
/*
	private Vector<TypeError> checkTypeGraph(final Graph graph,
			final int actTypeGraphLevel, final Vector<TypeError> errors) {
		TypeError actError = null;
		if ((actTypeGraphLevel == ENABLED_MAX)
				|| (actTypeGraphLevel == ENABLED_MAX_MIN)) {
			Iterator<Node> en = this.typeGraph.getNodesSet().iterator();
			Node actNode = null;
			while (en.hasNext()) {
				actNode = en.next();
				actError = checkMultiplicity(actNode, actTypeGraphLevel);
				if (actError != null)
					errors.add(actError);
			}
			return errors;
		}
		return errors;
	}
     */
    // not used currently
    /*
	private Vector<TypeError> checkNodes(final Graph graph, final int actTypeGraphLevel,
			final Vector<TypeError> errors) {
		TypeError actError = null;
		final List<Type> checkedTypes = new Vector<Type>();
		
		Iterator<Node> en = graph.getNodesSet().iterator();
		Node actNode;
		while (en.hasNext()) {
			actNode = en.next();
			if (!checkedTypes.contains(actNode.getType())) {
				checkedTypes.add(actNode.getType());
				
				// check the actual node
				actError = this.checkTypeInTypeGraph(actNode, actTypeGraphLevel);
				if (actError != null) {
					actError.setContainingGraph(graph);
					errors.add(actError);
				}
			}
		}
		checkedTypes.clear();
		
		return errors;
	}
     */
    /**
     * Checks edges of the specified graph over the type edges (multiplicity constraint) of the type graph.
     *
     * @param graph
     * @param actTypeGraphLevel
     * @param errors
     * @return	errors
     */
    private Vector<TypeError> checkArcsOverTypeGraph(
            final Graph graph,
            final int actTypeGraphLevel,
            Vector<TypeError> errors) {

        TypeError actError = null;
        final Iterator<Arc> en = graph.getArcsSet().iterator();
        Arc actArc;
        while (en.hasNext()) {
            actArc = en.next();
            actError = this.checkTypeInTypeGraph(actArc, actTypeGraphLevel);

            if (actError != null) {
                actError.setContainingGraph(graph);
                errors.add(actError);
            }
        }
        return errors;
    }

    /**
     * checks the given rule, if it is valid typed. If the TypeSet of the contained graphs is not this object, the types
     * of the nodes and edges must be also defined here. If in this TypeSet a proofed type graph is used, there must be
     * a morphism from the given graphs into the type graph.
     *
     * @param rule the rule to check. The original and image graphs of the rule and of all NACs will be checked.
     * @return An empty {@link Collection} if the given rule is valid typed. If there were type errors in the rule a
     * Collection with objects of class {@link TypeError} will returned. For each mismatch an object will delivered. You
     * can check if there were errors with {@link Collection#isEmpty}.
     *
     * @see #checkTypeGraph
     */
    public Collection<TypeError> checkType(final Rule rule) {
        // count the type mismatches
        Vector<TypeError> errors = new Vector<TypeError>();

        // check LHS
        errors.addAll(checkType(rule.getOriginal()));
        // check RHS
        errors.addAll(checkType(rule.getImage()));

        // check all NACs
        final List<OrdinaryMorphism> nacs = rule.getNACsList();
        for (int l = 0; l < nacs.size(); l++) {
            final OrdinaryMorphism nac = nacs.get(l);
            // check original (LHS)
//			errors.addAll(checkType(nac.getOriginal()));
            // check image
            errors.addAll(checkType(nac.getImage()));
        }

//		 check all PACs
        final List<OrdinaryMorphism> pacs = rule.getPACsList();
        for (int l = 0; l < pacs.size(); l++) {
            final OrdinaryMorphism pac = pacs.get(l);
            // check  original (LHS)
//			errors.addAll(checkType(pac.getOriginal()));
            // check image
            errors.addAll(checkType(pac.getImage()));
        }

        return errors;
    }

    /**
     * checks the given atomic, if it is valid typed. If the TypeSet of the contained graphs is not this object, the
     * types of the nodes and edges must be also defined here. If in this TypeSet a proofed type graph is used, there
     * must be a morphism from the given graphs into the type graph.
     *
     * @param atomic the atomic to check.
     * @return An empty {@link Collection} if the given atomic is valid typed. If there were type errors in the atomic a
     * Collection with objects of class {@link TypeError} will returned. For each mismatch an object will delivered. You
     * can check if there were errors with {@link Collection#isEmpty}.
     *
     * @see #checkTypeGraph
     */
    public Collection<TypeError> checkType(final AtomConstraint atomic) {
        // count the type mismatches
        Vector<TypeError> errors = new Vector<TypeError>();
        // check left side / original
        errors.addAll(checkType(atomic.getOriginal()));
        // check all right sides / conclusions
        Enumeration<AtomConstraint> cons = atomic.getConclusions();
        OrdinaryMorphism actCon;
        while (cons.hasMoreElements()) {
            actCon = cons.nextElement();
            // the left side is always the same
            // check image
            errors.addAll(checkType(actCon.getImage()));
        }
        return errors;
    }// checkType(Rule)

    /**
     * checks the given morphism, if it is valid typed. If the TypeSet of the contained graph is not this object, the
     * types of the nodes and edges must be also defined here. If in this TypeSet a proofed type graph is used, there
     * must be a morphism from the given graphs into the type graph.
     *
     * @param morphism the morphism to check. The image and the original grpah of the morphism will be checked.
     * @return An empty {@link Collection} if the given morphism is valid typed. If there were type errors in the
     * morphism a Collection with objects of class {@link TypeError} will returned. For each mismatch an object will
     * delivered. You can check if there were errors with {@link Collection#isEmpty}.
     *
     * @see #checkTypeGraph
     */
    public Collection<TypeError> checkType(final OrdinaryMorphism morphism) {
        // count the type mismatches
        Vector<TypeError> errors = new Vector<TypeError>();
        // check left side / original
        errors.addAll(checkType(morphism.getOriginal()));
        // check right side / image
        errors.addAll(checkType(morphism.getImage()));
        // return the errors
        return errors;
    }// checkType(OrinaryMorphism)

    /**
     * checks the given arc, if it is valid typed. The TypeSet of the graph which contains the arc must be this object.
     * If in this TypeSet a proofed type graph is used, this arc must be represented there.
     *
     * @param arc the arc to check.
     * @return An empty {@link Collection} if the given arc is valid typed. If there were type errors in the arc a
     * Collection with objects of class {@link TypeError} will returned. For each mismatch an object will delivered. You
     * can check if there were errors with {@link Collection#isEmpty}.
     *
     * @see #checkTypeGraph
     */
    public TypeError checkType(final Arc arc, final boolean isComplete) {
        // check if the type graph is validated
        if (this.typeGraphLevel <= DISABLED) {
            return null;
        }

        if (isComplete && this.typeGraphLevel >= ENABLED_MAX) {
            return checkTypeInTypeGraph(arc, this.typeGraphLevel);
        } else if (!isComplete && this.typeGraphLevel >= ENABLED_MAX) {
            return checkTypeInTypeGraph(arc, ENABLED_MAX);
        } else {
            return null;
        }
    }

    /**
     * checks the given node, if it is valid typed. The TypeSet of the graph which contains the node must be this
     * object. If in this TypeSet a proofed type graph is used, this node must be represented there.
     *
     * @param node the node to check.
     * @param isComplete true, if the containing graph is not a subgraph so we will also check for minimum multiplicity
     * if activated.
     * @return An empty {@link Collection} if the given node is valid typed. If there were type errors in the node a
     * Collection with objects of class {@link TypeError} will returned. For each mismatch an object will delivered. You
     * can check if there were errors with {@link Collection#isEmpty}.
     *
     * @see #checkTypeGraph
     */
    public TypeError checkType(final Node node, final boolean isComplete) {
        // check if the type graph is validated
        if (this.typeGraphLevel == DISABLED) {
            return null;
        }

        if (this.typeGraphLevel >= ENABLED_MAX) {
            if (isComplete) {
                return checkTypeInTypeGraph(node, this.typeGraphLevel);
            }

            return checkTypeInTypeGraph(node, ENABLED_MAX);
        }

        return null;
    }

    /**
     * checks the given graph object, if it is valid typed. The TypeSet of the graph which contains the object must be
     * this object. If in this TypeSet a proofed type graph is used, this object must be represented there.
     *
     * @param object the object to check.
     * @return An empty {@link Collection} if the given object is valid typed. If there were type errors in the arc a
     * Collection with objects of class {@link TypeError} will returned. For each mismatch an object will delivered. You
     * can check if there were errors with {@link Collection#isEmpty}.
     *
     * @see #checkTypeGraph
     */
    public TypeError checkType(final GraphObject object) {
        if (object instanceof Node) {
            return checkType((Node) object, false);
        } else if (object instanceof Arc) {
            return checkType((Arc) object, false);
        } else {
            return null;
        }
    }

    /**
     * changes the behavior of the type graph check and defines, how the type graph is used.
     *
     * @param level
     * <table>
     * <tr>
     * <td>{@link #DISABLED}</td>
     * <td>The type graph will be ignored, so all graphs can contain objects with types undefined in the type graph.
     * Multiplicity will be also ignored.</td>
     * </tr>
     * <tr>
     * <td>{@link #ENABLED}</td>
     * <td>The type graph will be basicaly used, so all graphs can only contain objects with types defined in the type
     * graph. But the multiplicity will not be checked.</td>
     * </tr>
     * <tr>
     * <td>{@link #ENABLED_MAX}</td>
     * <td>The type graph will be basically used, so all graphs can only contain objects with types defined in the type
     * graph. Multiplicity in all graphs should satisfy the defined maximum constraints.</td>
     * </tr>
     * <tr>
     * <td>{@link #ENABLED_MAX_MIN}</td>
     * <td>The type graph will be used, so all graphs can only contain objects with types defined in the type graph.
     * Multiplicity in all graphs must satisfy the defined maximum constraints and the working graph must</td>
     * </tr>
     * </table>
     *
     * @return {@link #SUCCESS} (an empty Collection), if the type graph is defined and is usable as type graph.
     * Otherwise you get a Collection of {@link TypeError}.
     */
    public Collection<TypeError> setLevelOfTypeGraphCheck(final int level) {
        // if the type graph is not proofed, check it

        this.typeGraphLevel = level;
        return SUCCESS;
    }

    /**
     * Set the level of the type graph without checking whether the type multiplicity constraints are satisfied.
     *
     * @param level
     */
    public void setLevelOfTypeGraph(final int level) {
        this.typeGraphLevel = level;
    }

    /**
     * Returns the level of the type graph.
     *
     * @return <table>
     * <tr>
     * <td>{@link #DISABLED}</td>
     * <td>The type graph will be ignored, so all graphs can contain objects with types undefined in the type
     * graph.</td>
     * </tr>
     * <tr>
     * <td>{@link #ENABLED}</td>
     * <td>The type graph is used, so all graphs can only contain objects with types defined in the type graph.
     * Multiplicity constraints will not be checked.</td>
     * </tr>
     * <tr>
     * <td>{@link #ENABLED_MAX}</td>
     * <td>The type graph is used, so all graphs can only contain objects with types defined in the type graph.
     * Multiplicity constraints max will be checked.</td>
     * </tr>
     * <tr>
     * <td>{@link #ENABLED_MAX_MIN}</td>
     * <td>The type graph will be used, so all graphs can only contain objects with types defined in the type graph.
     * Multiplicity constraints min, max will be checked.</td>
     * </tr>
     * </table>
     */
    public int getLevelOfTypeGraphCheck() {
        return this.typeGraphLevel;
    }

    /**
     * marks the type graph as unchecked, so no longer the type graph will be used for type checks. Use
     * {@link #checkTypeGraph()} to turn the type graph based checks on.
     *
     * @deprecated use {@link #setLevelOfTypeGraphCheck}
     */
    public void disableTypeGraphCheck() {
        this.setLevelOfTypeGraphCheck(DISABLED);
    }// turnTypeGraphCheckOff

    /**
     * marks the type graph as checked, so no longer the type graph will be used for type checks. Use
     * {@link #checkTypeGraph()} to turn the type graph based checks on.
     *
     * @deprecated use {@link #setLevelOfTypeGraphCheck}
     */
    public Collection<TypeError> enableTypeGraphCheck() {
        return this.setLevelOfTypeGraphCheck(ENABLED_MAX);
    }// turnTypeGraphCheckOff

    public boolean compareTo(final TypeSet ts) {
        if (ts == this) {
            return true;
        }
        // compare types size
        Iterator<Type> e = ts.getTypes();
        Vector<Type> another = new Vector<Type>();
        while (e.hasNext()) {
            another.add(e.next());
        }
        if (this.types.size() != another.size()) {
            return false;
        }
        // compare this.types
        for (int i = 0; i < this.types.size(); i++) {
            Type t = this.types.elementAt(i);
            for (int j = another.size() - 1; j >= 0; j--) {
                Type t1 = another.elementAt(j);
                if (t.compareTo(t1)) {
                    another.remove(t1);
                    break;
                }
            }
        }
        if (another.size() != 0) {
            return false;
        }

        return true;
    }

    /**
     * Compare my types with the types of the specified TypeSet ts. Returns true, if the types are compatible, otherwise
     * false.
     */
    public boolean compareTypes(final TypeSet ts) {
        if (String.valueOf(ts.hashCode()).equals(this.info)) {
            return true;
        }

        return compareTypes(ts, new Vector<Type>(), new Vector<Type>(),
                new Vector<Type>(), new Vector<Type>());
    }

    /**
     * Compare my types with the types of the specified TypeSet ts. Fill the specified container with types that have
     * equal name but different attributes, inheritance, multiplicity. Returns true, if the types are compatible,
     * otherwise false.
     */
    public boolean compareTypes(final TypeSet ts,
            final Vector<Type> differentAttribute,
            final Vector<Type> differentInheritance,
            final Vector<Type> differentMultiplicity) {
        return compareTypes(ts, differentAttribute, differentInheritance,
                differentMultiplicity, new Vector<Type>());
    }

    public boolean isNewTypeGraphObjectImported() {
        return this.newTypeGraphObjectImported;
    }

    /**
     * Compare my types with the types of the specified TypeSet ts. Fill the specified container with types that have
     * equal name but different attributes, inheritance, multiplicity, also not existing types. Returns true, if the
     * types are compatible, otherwise false.
     */
    private boolean compareTypes(final TypeSet ts,
            final Vector<Type> differentAttribute,
            final Vector<Type> differentInheritance,
            final Vector<Type> differentMultiplicity,
            final Vector<Type> typesToAdd) {

        if (ts == this
                || String.valueOf(ts.hashCode()).equals(this.info)) {
            return true;
        }
        // hier multiple inheritance einbauen!!
        differentInheritance.clear();
        differentAttribute.clear();
        differentMultiplicity.clear();
        typesToAdd.clear();
        boolean conflict = false;

        Vector<Type> another = new Vector<Type>(ts.getTypeList());
        // compare types
        for (int i = 0; i < this.types.size(); i++) {
            Type t = this.types.elementAt(i);
            for (int j = 0; j < another.size(); j++) {
                Type t1 = another.elementAt(j);
                if (t.getStringRepr().equals(t1.getStringRepr())) {
                    if ((t.isNodeType() && t1.isNodeType())
                            || (t.isArcType() && t1.isArcType())) {
                        // check attribute list
                        if (!t.compareTo(t1)) {
                            if (!differentAttribute.contains(t1)) {
                                differentAttribute.add(t1);
                            }
                            conflict = true;
                        }

                        // check node type graph object
                        if (t.getTypeGraphNodeObject() != null
                                && t1.getTypeGraphNodeObject() != null) {

                            // check inheritance
                            if (!t1.getParents().isEmpty()) {
                                if (!t.getParents().isEmpty()) {
                                    Vector<Type> tParents = t.getAllParents();
                                    Vector<Type> t1Parents = t1.getAllParents();
                                    if (!compareParents(tParents, t1Parents)) {
                                        conflict = true;
                                        if (!differentInheritance.contains(t1)) {
                                            differentInheritance.add(t1);
                                        }
                                    }
                                } else {
                                    conflict = true;
                                    if (!differentInheritance.contains(t1)) {
                                        differentInheritance.add(t1);
                                    }
                                }
                            }
                            // check multiplicity of node type							
                            if (t.getSourceMax() != t1.getSourceMax()) {
                                conflict = true;
                                if (!differentMultiplicity.contains(t1)) {
                                    differentMultiplicity.add(t1);
                                }
                            } else if (t.getSourceMin() != t1.getSourceMin()) {
                                conflict = true;
                                if (!differentMultiplicity.contains(t1)) {
                                    differentMultiplicity.add(t1);
                                }
                            }

                        } else if (t.hasTypeGraphArc()
                                && t1.hasTypeGraphArc()) {
                            // check arc embedding into the type graph
                            if (!t.compareTypeGraphArcs(t1)) {
                                if (!typesToAdd.contains(t1)) {
                                    typesToAdd.add(t1);
                                }
                            }
                            // check multiplicity of arc type graph object
                            if (!t.compareTypeGraphArcsMultiplicity(t1)) {
                                conflict = true;
                                if (!differentMultiplicity.contains(t1)) {
                                    differentMultiplicity.add(t1);
                                }
                            }
                        } else {
                            if (t1.hasTypeGraphNode()) {
                                if (!typesToAdd.contains(t1)) {
                                    typesToAdd.add(t1);
                                }
                            } else if (t1.hasTypeGraphArc()) {
                                if (!typesToAdd.contains(t1)) {
                                    typesToAdd.add(t1);
                                }
                            }
                        }
                        another.remove(t1);
                        break;
                    }
                }
            }
        }
        if (typesToAdd.isEmpty()) {
            for (int j = 0; j < another.size(); j++) {
                Type t1 = another.elementAt(j);
                if (t1.hasTypeGraphNode()) {
                    if (!typesToAdd.contains(t1)) {
                        typesToAdd.add(t1);
                    }
                } else if (t1.hasTypeGraphArc()) {
                    if (!typesToAdd.contains(t1)) {
                        typesToAdd.add(t1);
                    }
                }
            }
        }
        this.newTypeGraphObjectImported = !typesToAdd.isEmpty();
        return !conflict;
    }

    private boolean compareParents(final Vector<Type> allParents1,
            final Vector<Type> allParents2) {
        int nm = allParents1.size();
        int i = 1;
        while (i < nm) {
            Type p1 = allParents1.get(i);
            Type p2 = allParents2.get(i);
            if (!p1.compareTo(p2)) {
                return false;
            }
            i++;
        }
        return true;
    }

    public boolean contains(final TypeSet ts) {
        if (ts == this
                || String.valueOf(ts.hashCode()).equals(this.info)) {
            return true;
        }
        // compare types
        if (this.types.size() < ts.getTypesCount()) {
            return false;
        }

        int count = 0;
        for (int i = 0; i < this.types.size(); i++) {
            Type ti = this.types.elementAt(i);
            for (int j = 0; j < ts.getTypeList().size(); j++) {
                Type tj = ts.getTypeList().get(j);
                if (ti.compareTo(tj)) {
                    count++;
                    break;
                }
            }
        }
        if (count != ts.getTypeList().size()) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the given edge could be removed from its source and target.
     *
     * @param arc the edge to check
     * @return null, if edge type multiplicity has not failed, otherwise {@link TypeError}
     *
     */
    public TypeError checkIfRemovable(final Arc arc) {
        if (this.typeGraphLevel != ENABLED_MAX_MIN) {
            return null;
        }
        GraphObject node = arc.getSource();
        TypeError error = node.getType().checkIfRemovableFromSource(node, arc,
                this.typeGraphLevel);
        if (error != null) {
            return error;
        }
        node = arc.getTarget();
        error = node.getType().checkIfRemovableFromTarget(node, arc,
                this.typeGraphLevel);
        return error;
    }

    /**
     * Checks if the given edge could be removed from its source and target with respect to source and target to delete.
     *
     * @param arc	edge to delete
     * @param deleteSrc	true if the source node will be deleted
     * @param deleteTar	true if the target node will be deleted
     *
     * @return	null if edge type multiplicity has not failed, otherwise a type error {@link TypeError}
     */
    public TypeError checkIfRemovable(final Arc arc, boolean deleteSrc, boolean deleteTar) {
        if (this.typeGraphLevel != ENABLED_MAX_MIN) {
            return null;
        }
        TypeError error = null;
        if (!deleteTar) {
            GraphObject node = arc.getSource();
            error = node.getType().checkIfRemovableFromSource(node, arc,
                    deleteSrc, deleteTar,
                    this.typeGraphLevel);
        }
        if ((error == null) && !deleteSrc) {
            GraphObject node = arc.getTarget();
            error = node.getType().checkIfRemovableFromTarget(node, arc,
                    deleteSrc, deleteTar,
                    this.typeGraphLevel);

        }

        return error;
    }

    /**
     * Checks if the given arc could be removed from its source. This method ignores the multiplicity constraints of the
     * target node. It should be only used when the target node should be destroyed.
     *
     * @param arc the edge to check
     * @return null, if the removing is allowed otherwise a {@link TypeError}
     */
    public TypeError checkIfRemovableFromSource(final Arc arc) {
        if (this.typeGraphLevel != ENABLED_MAX_MIN) {
            return null;
        }
        GraphObject node = arc.getSource();
        TypeError error = node.getType().checkIfRemovableFromSource(node, arc,
                this.typeGraphLevel);
        return error;
    }

    /**
     * Checks if the given arc could be removed from its target. This method ignores the multiplicity constraints of the
     * source node. It should be only used when the source node should be destroyed.
     *
     * @param arc the arc to check
     * @return null, if the removing is allowed otherwise a {@link TypeError}
     */
    public TypeError checkIfRemovableFromTarget(final Arc arc) {
        if (this.typeGraphLevel != ENABLED_MAX_MIN) {
            return null;
        }
        GraphObject node = arc.getTarget();
        TypeError error = node.getType().checkIfRemovableFromTarget(node, arc,
                this.typeGraphLevel);
        return error;
    }

    public TypeError checkIfEdgeCreatable(final Type type, final Node src, final Node tar) {
        if ((this.typeGraphLevel == DISABLED)
                || (this.typeGraphLevel == ENABLED)) {
            return null;
        }

        return type.checkIfEdgeCreatable(src, tar,
                this.typeGraphLevel);
    }

    public int getMaxMultiplicity(final Type type) {
        return type.getSourceMax();
    }

    public int getMinMultiplicity(final Type type) {
        return type.getSourceMin();
    }

    public int getMaxSourceMultiplicity(final Type type, final Type srctype, final Type tartype) {
        return type.getSourceMax(srctype, tartype);
    }

    public int getMinSourceMultiplicity(final Type type, final Type srctype, final Type tartype) {
        return type.getSourceMin(srctype, tartype);
    }

    public int getMaxTargetMultiplicity(final Type type, final Type srctype, final Type tartype) {
        return type.getTargetMax(srctype, tartype);
    }

    public int getMinTargetMultiplicity(final Type type, final Type srctype, final Type tartype) {
        return type.getTargetMin(srctype, tartype);
    }

    public String showTypes() {
        String out = "Types:" + "\n";
        for (int i = 0; i < this.types.size(); i++) {
            Type t = this.types.get(i);
            out = out + t.getName() + t.getAdditionalRepr() + "\n";
        }
        return out;
    }

    public String showNodeTypes() {
        String out = "Types:" + "\n";
        for (int i = 0; i < this.types.size(); i++) {
            Type t = this.types.get(i);
            if (t.getAdditionalRepr().indexOf("NODE") >= 0) {
                out = out + t.getName() + t.getAdditionalRepr() + "\n";
            }
        }
        return out;
    }

    public String showArcTypes() {
        String out = "Types:" + "\n";
        for (int i = 0; i < this.types.size(); i++) {
            Type t = this.types.get(i);
            if (t.getAdditionalRepr().indexOf("EDGE") >= 0) {
                out = out + t.getName() + t.getAdditionalRepr() + "\n";
            }
        }
        return out;
    }

    /*
	 * Remove all type users which are destroyed by <code>Node.disposeFast()</ode>
	 * resp. <code>Arc.disposeFast()</code>.
	 * The type of these edges is already null. 
     */
//	public void refreshTypeUsers() {
//		for (int i=0; i<this.types.size(); i++) {
//			Type t = this.types.get(i);
//			if (t instanceof NodeTypeImpl) {
//				((NodeTypeImpl)t).refreshTypeUsers();
//			} else if (t instanceof ArcTypeImpl) {
//				((ArcTypeImpl)t).refreshTypeUsers();
//			}
//		}
//	}
    public void setHelpInfo(final String str) {
        this.info = str;
    }

    public String getHelpInfo() {
        return this.info;
    }

    /**
     * Trims the capacity of used vectors to be the vector's current size.
     */
    public void trimToSize() {
        this.types.trimToSize();
        this.inheritanceArcs.trimToSize();
    }

}

// $Log: TypeSet.java,v $
// Revision 1.87  2010/12/02 19:37:59  olga
// import type graph - bug fixed
//
// Revision 1.86  2010/10/19 19:04:51  olga
// tuning
//
// Revision 1.85  2010/10/18 15:00:51  olga
// import type graph - bug fixed
//
// Revision 1.84  2010/10/16 22:46:11  olga
// improved undo for RuleScheme graph objects
//
// Revision 1.83  2010/10/07 20:04:26  olga
// bug fixed- multiplicity bug after disable-enable the type graph
//
// Revision 1.82  2010/09/23 08:27:32  olga
// tuning
//
// Revision 1.81  2010/08/09 13:58:50  olga
// tuning
//
// Revision 1.80  2010/03/08 15:51:40  olga
// code optimizing
//
// Revision 1.79  2010/03/04 14:17:13  olga
// code optimizing
//
// Revision 1.78  2010/02/22 14:42:07  olga
// code optimizing
//
// Revision 1.77  2009/10/05 08:53:09  olga
// RSA check - bug fixed
//
// Revision 1.76  2009/09/21 13:56:40  olga
// type graph check improved
//
// Revision 1.75  2009/08/03 16:54:52  olga
// CPA , essential pairs - bug fixed
//
// Revision 1.74  2009/07/29 09:32:05  olga
// Match with PACs - bug fixed
// ARS - code tuning
//
// Revision 1.73  2009/07/14 12:16:30  olga
// Multiplicity bug fixed
//
// Revision 1.72  2009/07/13 07:26:22  olga
// ARS: further development
//
// Revision 1.71  2009/06/02 12:39:22  olga
// Min Multiplicity check - bug fixed
//
// Revision 1.70  2009/05/12 10:36:42  olga
// CPA: bug fixed
// Applicability of Rule Seq. : bug fixed
//
// Revision 1.69  2009/04/14 09:18:35  olga
// Edge Type Multiplicity check - bug fixed
//
// Revision 1.68  2009/03/30 13:50:48  olga
// some tests
//
// Revision 1.67  2009/03/12 10:57:48  olga
// some changes in CPA of managing names of the attribute variables.
//
// Revision 1.66  2009/02/26 16:31:41  olga
// Code tuning
//
// Revision 1.65  2009/01/14 10:51:41  olga
// - Import of TypeGraph with NTI in an existing TypeGraph without NTI,
// attribute member overlapping with parent attribute - bug fixed
// - Edge type Multiplicity check (target min, source min) during manual graph editing - bug fixed
// - Edge type Multiplicity check (target min, source min) during graph matching - improved
//
// Revision 1.64  2008/12/17 09:37:37  olga
// Import of TypeGraph from  grammar (.ggx) - bug fixed
//
// Revision 1.63  2008/11/13 08:26:20  olga
// some tests
//
// Revision 1.62  2008/10/15 14:58:38  olga
// - Bug fixed: import type graph with inheritance
// - Bug fixed: edge type Multiplicity check in CPA
//
// Revision 1.61  2008/09/04 08:17:01  olga
// Inheritance events added
//
// Revision 1.60  2008/08/21 13:08:13  jurack
// Benachrichtigung an Observer, wenn Inheritance hinzugef�gt wird
//
// Revision 1.59 2008/07/09 13:34:26 olga
// Applicability of RS - bug fixed
// Delete not used node/edge type - bug fixed
// AGG help - extended
//
// Revision 1.58 2008/04/21 09:32:18 olga
// Visualization of inheritance edge - bugs fixed
// Graph layout tuning
//
// Revision 1.57 2008/04/17 10:11:08 olga
// Undo, redo edit and graph layout tuning,
//
// Revision 1.56 2008/04/10 14:57:24 olga
// code tuning
//
// Revision 1.55 2008/04/09 10:29:37 olga
// Bug fixed - import graph type with abstract types
//
// Revision 1.54 2008/04/09 07:38:52 olga
// Bug fixed during loading of grammar
//
// Revision 1.53 2008/04/07 09:36:54 olga
// Code tuning: refactoring + profiling
// Extension: CPA - two new options added
//
// Revision 1.52 2008/02/18 09:37:11 olga
// - an extention of rule dependency check is implemented;
// - some bugs fixed;
// - editing of graphs improved
//
// Revision 1.51 2007/12/17 08:33:29 olga
// Editing inheritance relations - bug fixed;
// CPA: dependency of rules - bug fixed
//
// Revision 1.50 2007/12/03 08:35:12 olga
// - Some bugs fixed in visualization of morphism mappings after deleting and
// creating
// nodes, edges
// - implemented: matching with non-injective NAC and Match morphism
//
// Revision 1.49 2007/11/21 09:59:44 olga
// Update V1.6.2.1:
// new features: - default attr value can be set in a type graph and used during
// transformation (experimental phase)
// - currently selected node and edge type are shown in the bottom right corner
// of the AGG GUI
// - Critical pair analysis for grammar with node type inheritance (experimental
// phase)
//
// Revision 1.48 2007/11/19 08:48:40 olga
// Some GUI usability mistakes fixed.
// Default values in node/edge of a type graph implemented.
// Code tuning.
//
// Revision 1.47 2007/11/14 08:53:42 olga
// code tuning
//
// Revision 1.46 2007/11/12 08:48:55 olga
// Code tuning
//
// Revision 1.45 2007/11/08 12:57:00 olga
// working on CPA inconsistency for rules with pacs and inheritance
// bugs are possible
//
// Revision 1.44 2007/11/05 09:18:21 olga
// code tuning
//
// Revision 1.43 2007/11/01 09:58:16 olga
// Code refactoring: generic types- done
//
// Revision 1.42 2007/10/10 14:30:32 olga
// Enumeration typing
//
// Revision 1.41 2007/10/04 10:56:04 olga
// Code tuning
//
// Revision 1.40 2007/10/04 07:44:28 olga
// Code tuning
//
// Revision 1.39 2007/10/01 12:48:54 olga
// test: in Graph, TypeGraph, GraphObject - DList replaced by Vector
//
// Revision 1.38 2007/09/27 08:42:47 olga
// CPA: new option -ignore pairs with same rules and same matches-
//
// Revision 1.37 2007/09/24 09:42:36 olga
// AGG transformation engine tuning
//
// Revision 1.36 2007/09/10 13:05:37 olga
// In this update:
// - package xerces2.5.0 is not used anymore;
// - class com.objectspace.jgl.Pair is replaced by the agg own generic class
// agg.util.Pair;
// - bugs fixed in: usage of PACs in rules; match completion;
// usage of static method calls in attr. conditions
// - graph editing: added some new features
//
// Revision 1.35 2007/07/09 08:00:16 olga
// GUI tuning
//
// Revision 1.34 2007/06/25 08:28:26 olga
// Tuning and Docu update
//
// Revision 1.33 2007/06/18 08:15:56 olga
// New extentions by drawing edge.
//
// Revision 1.32 2007/06/13 08:33:03 olga
// Update: V161
//
// Revision 1.31 2007/04/30 10:39:46 olga
// tests
//
// Revision 1.30 2007/04/19 14:50:01 olga
// Loading grammar - tuning
//
// Revision 1.29 2007/04/19 07:52:41 olga
// Tuning of: Undo/Redo, Graph layouter, loading grammars
//
// Revision 1.28 2007/04/11 10:03:38 olga
// Undo, Redo tuning,
// Simple Parser- bug fixed
//
// Revision 1.27 2007/03/28 10:01:08 olga
// - extensive changes of Node/Edge Type Editor,
// - first Undo implementation for graphs and Node/edge Type editing and
// transformation,
// - new / reimplemented options for layered transformation, for graph layouter
// - enable / disable for NACs, attr conditions, formula
// - GUI tuning
//
// Revision 1.26 2007/02/05 12:33:46 olga
// CPA: chengeAttribute conflict/dependency : attributes with constants
// bug fixed, but the critical pairs computation has still a gap.
//
// Revision 1.25 2007/01/31 09:19:20 olga
// Bug fixed in case of transformating attributed grammar with inheritance and
// non-injective match
//
// Revision 1.24 2007/01/29 09:44:27 olga
// Bugs fiixed, that occur during the extension a non-attributed grammar by
// attributes.
//
// Revision 1.23 2007/01/22 08:28:26 olga
// GUI bugs fixed
//
// Revision 1.22 2006/11/15 09:00:32 olga
// Transform with input parameter : bug fixed
//
// Revision 1.21 2006/11/09 10:31:05 olga
// Matching error fixed
//
// Revision 1.20 2006/11/06 10:09:36 olga
// Type editor GUI tuning
//
// Revision 1.19 2006/11/01 11:17:30 olga
// Optimized agg sources of CSP algorithm, match usability,
// graph isomorphic copy,
// node/edge type multiplicity check for injective rule and match
//
// Revision 1.18 2006/08/03 17:10:28 olga
// Java docu
//
// Revision 1.17 2006/08/02 09:00:57 olga
// Preliminary version 1.5.0 with
// - multiple node type inheritance,
// - new implemented evolutionary graph layouter for
// graph transformation sequences
//
// Revision 1.16 2006/05/29 07:59:42 olga
// GUI, undo delete - tuning.
//
// Revision 1.15 2006/05/08 08:24:12 olga
// Some extentions of GUI: - Undo Delete button of tool bar to undo deletions
// if grammar elements like rule, NAC, graph constraints;
// - the possibility to add a new graph to a grammar or a copy of the current
// host graph;
// - to set one or more layer for consistency constraints.
// Also some bugs fixed of matching and some optimizations of CSP algorithmus
// done.
//
// Revision 1.14 2006/04/12 14:54:07 olga
// Restore attr. values of attr. type observers after type graph imported.
//
// Revision 1.13 2006/04/12 09:01:57 olga
// Layered graph constraints tuning
//
// Revision 1.12 2006/04/10 09:19:30 olga
// Import Type Graph, Import Graph - tuning.
// Attr. member type check: if class does not exist.
// Graph constraints for a layer of layered grammar.
//
// Revision 1.11 2006/04/06 15:27:29 olga
// Import Type Graph and Type Graph tuning
//
// Revision 1.10 2006/04/06 09:28:52 olga
// Tuning of Import Type Graph and Import Graph
//
// Revision 1.9 2006/04/03 08:57:50 olga
// New: Import Type Graph
// and some bugs fixed
//
// Revision 1.8 2006/03/01 09:55:47 olga
// - new CPA algorithm, new CPA GUI
//
// Revision 1.7 2005/10/24 09:04:49 olga
// GUI tuning
//
// Revision 1.6 2005/10/17 12:32:28 enrico
// Multiplicity check for node instances
//
// Revision 1.5 2005/09/26 08:35:15 olga
// CPA graph frames; bugs
//
// Revision 1.4 2005/09/08 16:25:02 olga
// Improved: editing attr. condition, importing graph, sorting node/edge types
//
// Revision 1.3 2005/09/05 10:06:45 olga
// Deleting type graph object - fixed.
//
// Revision 1.2 2005/09/01 08:22:14 olga
// Adaptation inheritance version to AGG standard:
// - remove type graph nodes/arcs,
// - GUI conformance.
//
// Revision 1.1 2005/08/25 11:56:54 enrico
// *** empty log message ***
//
// Revision 1.2.2.2 2005/08/16 09:56:04 enrico
// implemented method getClan
//
// Revision 1.2.2.1 2005/07/04 11:41:37 enrico
// basic support for inheritance
//
// Revision 1.2 2005/06/20 13:37:03 olga
// Up to now the version 1.2.8 will be prepared.
//
// Revision 1.1 2005/05/30 12:58:02 olga
// Version with Eclipse
//
// Revision 1.31 2005/04/21 15:48:04 olga
// Fehler in PACs korregiert. Graph constraints (formulae) mit Verneinung
// koennen nicht vollstaendig korrekt getestet werden. Nur positive
// constraints werden richtig geprueft. Attribute Kontext kann aber noch
// Probleme berreiten.
// CVSr ----------------------------------------------------------------------
//
// Revision 1.30 2005/03/16 12:02:10 olga
//
// only little changes
//
// Revision 1.29 2005/02/09 15:08:15 olga
// Interface Type erweitert; TypeImpl ist jetzt public class.
// CiVS: ----------------------------------------------------------------------
//
// Revision 1.28 2005/01/28 14:02:32 olga
// -Fehlerbehandlung beim Typgraph check
// -Erweiterung CP GUI / CP Menu
// -Fehlerbehandlung mit identification option
// -Fehlerbehandlung bei Rule PAC
//
// Revision 1.27 2005/01/05 08:56:13 olga
// Source tuning
//
// Revision 1.26 2004/12/20 14:53:49 olga
// Changes because of matching optimisation.
//
// Revision 1.25 2004/11/15 17:50:39 olga
// Jetzt das Kombinieren von Grammars moeglich.
//
// Revision 1.24 2004/05/13 17:54:10 olga
// Fehlerbehandlung
//
// Revision 1.23 2004/04/19 11:39:30 olga
// Graphname als String ohne Blanks
//
// Revision 1.22 2004/04/15 10:49:48 olga
// Kommentare
//
// Revision 1.21 2004/02/25 16:35:46 olga
// Testausgaben aus.
//
// Revision 1.20 2004/01/28 17:58:39 olga
// Errors suche
//
// Revision 1.19 2003/12/18 16:27:46 olga
// Tests.
//
// Revision 1.18 2003/06/05 15:07:55 olga
// delete type angepasst
//
// Revision 1.17 2003/06/05 07:43:26 olga
// Dokumentation
//
// Revision 1.16 2003/05/30 13:43:17 olga
// Multiplicity Max-check ist jetzt in Step.execute.
// Multiplicity Min-check ist in Match.isValid
//
// Revision 1.15 2003/05/28 11:50:41 olga
// Min/Max Multiplicity check Test und Aenderung
//
// Revision 1.14 2003/05/23 11:33:31 komm
// new check function for dangling arcs
//
// Revision 1.13 2003/05/14 17:56:47 komm
// Added minimum multiplicity and removed TODOs
//
// Revision 1.12 2003/04/10 08:51:23 olga
// Tests mit serializable Ausgabe
//
// Revision 1.11 2003/03/31 10:33:36 olga
// Min-Multiplicity check nur fuer Work Graph
//
// Revision 1.10 2003/03/20 13:36:10 olga
// Delete TypeGraph neu
//
// Revision 1.9 2003/03/05 13:28:55 komm
// method of type graph activation changed to type graph level
//
// Revision 1.8 2003/02/24 11:20:28 komm
// appereance changed
//
// Revision 1.7 2003/02/03 17:49:52 olga
// new : compareTo(..)
//
// Revision 1.6 2002/12/18 11:38:24 komm
// remove of type error marks works now
//
// Revision 1.5 2002/12/16 13:43:59 komm
// renamed methods for turning off type graph check
//
// Revision 1.4 2002/11/11 10:37:19 komm
// multiplicity check added
//
// Revision 1.3 2002/09/30 10:12:55 komm
// dynamic type check expanded
//
// Revision 1.2 2002/09/23 12:24:14 komm
// added type graph in xt_basis, editor and GUI
//
// Revision 1.1.1.1 2002/07/11 12:17:27 olga
// Imported sources
//
// Revision 1.6 2001/02/22 12:35:02 olga
// Alle ALR Objecte werden mit einem "default" Typ aus der BaseFactory kreiert.
//
// Revision 1.5 2001/02/16 14:52:04 olga
// Type Fehler korregiert.
//
// Revision 1.4 2001/02/15 16:02:37 olga
// Fehlerbehebung wegen XML;
// Aenderungen bei TypeSet.
//
// Revision 1.3 1999/06/28 16:41:46 shultzke
// Hoffentlich erzeigen wir eine uebersetzungsfaehige Version
//
// Revision 1.2 1998/09/03 13:14:42 mich
// Updated for use with JGL V3.1.
//
// Revision 1.1 1998/05/27 17:29:02 mich
// Initial revision
//
