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

import agg.attribute.AttrEvent;
import agg.attribute.AttrInstance;
import agg.attribute.impl.AttrTupleManager;
import agg.attribute.impl.ContextView;
import agg.attribute.impl.ValueMember;
import agg.attribute.impl.ValueTuple;
import agg.attribute.impl.VarMember;
import agg.attribute.impl.VarTuple;
import agg.util.Change;
import agg.util.Pair;
import agg.util.XMLHelper;
import agg.util.XMLObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Represents a node in a graph. Nodes are the fundamental building blocks of
 * graphs and can have incoming and outgoing arcs (edges) connecting them to
 * other nodes. Each node has a type, a context (the graph it belongs to), and
 * optionally attributes.
 */
@SuppressWarnings("serial")
public class Node extends GraphObject implements XMLObject {

    // Test: node XY-position as attribute
    public boolean xyAttr = false;

    final protected LinkedHashSet<Arc> itsOutgoingArcs = new LinkedHashSet<>();
    final protected LinkedHashSet<Arc> itsIncomingArcs = new LinkedHashSet<>();

    /**
     * Creates a new node with the specified type and graph context.
     *
     * @param type the type of this node
     * @param context the graph this node belongs to
     */
    protected Node(Type type, Graph context) {
        this.itsContext = context;
        this.itsType = type;
        this.itsContextUsage = hashCode();
        // Test: XY Position as attributes
        addXYPosAttrs(this.itsContext != null && this.itsContext.xyAttr);
        if (!this.itsType.isAttrTypeEmpty() && this.itsAttr == null) {
            this.itsAttr = AttrTupleManager.getDefaultManager().newInstance(
                    this.itsType.getAttrType(), context.getAttrContext());
        }
        if (this.itsAttr != null) {
            this.itsAttr.addObserver(this);
        }
    }

    /**
     * Creates a new node with the specified attribute instance, type, and graph
     * context.
     *
     * @param attr the attribute instance for this node
     * @param type the type of this node
     * @param context the graph this node belongs to
     */
    public Node(AttrInstance attr, Type type, Graph context) {
        this.itsContext = context;
        this.itsType = type;
        this.itsContextUsage = hashCode();
        this.itsAttr = attr;
        // Test: XY Position as attributes
        addXYPosAttrs(this.itsContext != null && this.itsContext.xyAttr);
        if (this.itsAttr != null) {
            this.itsAttr.addObserver(this);
        }
    }

    /**
     * Adds XY position attributes to this node if the context graph has XY
     * attributes enabled.
     *
     * @param xyPosAttrs true if XY position attributes should be added
     */
    private void addXYPosAttrs(boolean xyPosAttrs) {
        // Test: XY Position as attributes
        if (xyPosAttrs) {
            xyAttr = true;
            if (this.itsAttr == null) {
                if (this.itsType.getAttrType() == null) {
                    ((NodeTypeImpl) this.itsType).setAttributeType(
                            AttrTupleManager.getDefaultManager().newType());
                }
                this.itsAttr = AttrTupleManager.getDefaultManager().newInstance(
                        this.itsType.getAttrType(), this.itsContext.getAttrContext());
            }
            agg.attribute.AttrType attrType = itsType.getAttrType();
            if (!((agg.attribute.impl.DeclTuple) attrType).containsName("thisX")) {
                attrType.addMember(
                        agg.attribute.facade.impl.DefaultInformationFacade.self().getJavaHandler(),
                        "int", "thisX");
            }
            if (!((agg.attribute.impl.DeclTuple) attrType).containsName("thisY")) {
                attrType.addMember(
                        agg.attribute.facade.impl.DefaultInformationFacade.self().getJavaHandler(),
                        "int", "thisY");
            }
        }
    }

    /**
     * Creates a copy of the specified node in the given graph context.
     *
     * @param orig the original node to copy
     * @param context the graph context for the new node
     */
    protected Node(Node orig, Graph context) {
        this(orig.getType(), context);
        if (orig.getAttribute() != null) {
            if (this.itsAttr == null) {
                this.createAttributeInstance();
            }
            ((ValueTuple) this.itsAttr).copyEntries(orig.getAttribute());
        }
        // Object name is not yet used in AGG GUI
        if (!"".equals(orig.getObjectName())) {
            this.setObjectName(orig.getObjectName());
        }
    }

    /**
     * Disposes this node and releases all resources. Clears all arcs and
     * removes observers from attributes.
     */
    public void dispose() {
        this.itsOutgoingArcs.clear();
        this.itsIncomingArcs.clear();
        if (this.itsAttr != null) {
            this.itsAttr.removeObserver(this);
            ((ValueTuple) this.itsAttr).dispose();
            this.itsAttr = null;
        }
        this.itsType = null;
        this.itsContext = null;
        this.itsContextUsage = -1;
    }

    /**
     * Adds an outgoing arc to this node.
     *
     * @param anArc the arc to add as outgoing
     */
    protected synchronized void addOut(Arc anArc) {
        this.itsOutgoingArcs.add((Arc) anArc);
    }

    /**
     * Adds an incoming arc to this node.
     *
     * @param anArc the arc to add as incoming
     */
    protected synchronized void addIn(Arc anArc) {
        this.itsIncomingArcs.add((Arc) anArc);
    }

    /**
     * Removes an outgoing arc from this node.
     *
     * @param anArc the arc to remove from outgoing arcs
     */
    protected synchronized void removeOut(Arc anArc) {
        this.itsOutgoingArcs.remove(anArc);
    }

    /**
     * Removes an incoming arc from this node.
     *
     * @param anArc the arc to remove from incoming arcs
     */
    protected synchronized void removeIn(Arc anArc) {
        this.itsIncomingArcs.remove(anArc);
    }

    /**
     * Returns the total number of arcs connected to this node (both incoming
     * and outgoing).
     *
     * @return the total number of arcs
     */
    public final int getNumberOfArcs() {
        return this.itsOutgoingArcs.size() + this.itsIncomingArcs.size();
    }

    /**
     * Returns an iterator through all incoming arcs to this node. The iterator
     * returns arcs in the order they were created.
     *
     * @return an iterator of incoming arcs
     * @see Arc
     */
    public final Iterator<Arc> getIncomingArcs() {
        return ((LinkedHashSet<Arc>) this.itsIncomingArcs).iterator();
    }

    /**
     * Returns an iterator through all incoming arcs to this node. The order of
     * arcs may differ from the creation order.
     *
     * @return an iterator of incoming arcs
     */
    public final Iterator<Arc> getIncomingArcsIterator() {
        return this.itsIncomingArcs.iterator();
    }

    /**
     * Returns a set of all incoming arcs to this node. The order of arcs may
     * differ from the creation order.
     *
     * @return a set of incoming arcs
     */
    public final HashSet<Arc> getIncomingArcsSet() {
        return this.itsIncomingArcs;
    }

    /**
     * Returns the number of incoming arcs to this node.
     *
     * @return the number of incoming arcs
     */
    @Override
    public final int getNumberOfIncomingArcs() {
        return this.itsIncomingArcs.size();
    }

    /**
     * Returns the number of incoming arcs of the specified type.
     *
     * @param aType the arc type to count
     * @return the number of incoming arcs of the given type
     */
    public final int getNumberOfIncomingArcs(Type aType) {
        int n = 0;
        for (Arc go : this.itsIncomingArcs) {
            if (go.getType().compareTo(aType)) {
                n++;
            }
        }
        return n;
    }

    /**
     * Returns the number of incoming arcs of the specified type from nodes of
     * the specified source type.
     *
     * @param aType the arc type to count
     * @param srcType the source node type to filter by
     * @return the number of matching incoming arcs
     */
    public final int getNumberOfIncomingArcsOfTypeFromSourceType(Type aType, Type srcType) {
        int n = 0;
        for (Arc go : this.itsIncomingArcs) {
            if (go.getType().compareTo(aType)) {
                if (srcType.isParentOf(go.getSource().getType())) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * Returns the number of incoming arcs of the specified type from nodes of
     * the specified source type.
     *
     * @param aType the arc type to count
     * @param src the (parent) type of the source node of incoming arcs
     * @return the number of matching incoming arcs
     */
    public final int getNumberOfIncomingArcs(Type aType, Type src) {
        int n = 0;
        for (Arc go : this.itsIncomingArcs) {
            if (go.getType().compareTo(aType)) {
                if (src.isParentOf(go.getSourceType())) {
                    n++;
                } else if (!this.itsContext.isCompleteGraph()
                        && go.getSourceType().isParentOf(src)) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * Returns a list of incoming arcs of the specified type from nodes of the
     * specified source type.
     *
     * @param aType the arc type to filter by
     * @param src the (parent) type of the source node of incoming arcs
     * @return a list of matching incoming arcs
     */
    public final List<Arc> getIncomingArcs(Type aType, Type src) {
        final List<Arc> result = new ArrayList<>(2);
        for (Arc go : this.itsIncomingArcs) {
            if (go.getType().compareTo(aType)) {
                if (src.isParentOf(go.getSourceType())) {
                    result.add(go);
                } else if (!this.itsContext.isCompleteGraph()
                        && go.getSourceType().isParentOf(src)) {
                    result.add(go);
                }
            }
        }
        return result;
    }

    /**
     * Checks if this node has an incoming arc from a node of the specified
     * type.
     *
     * @param aType the (parent) type of the source node of an incoming arc
     * @return true if an incoming arc from the specified type exists, false
     * otherwise
     */
    public boolean hasIncomingArcFrom(Type aType) {
        for (Arc a : this.itsIncomingArcs) {
            if (aType.isParentOf(a.getSourceType())
                    || a.getSourceType().isParentOf(aType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator through all outgoing arcs from this node. The
     * iterator returns arcs in the order they were created.
     *
     * @return an iterator of outgoing arcs
     * @see Arc
     */
    public final Iterator<Arc> getOutgoingArcs() {
        return ((LinkedHashSet<Arc>) this.itsOutgoingArcs).iterator();
    }

    /**
     * Returns the number of outgoing arcs from this node.
     *
     * @return the number of outgoing arcs
     */
    @Override
    public final int getNumberOfOutgoingArcs() {
        return this.itsOutgoingArcs.size();
    }

    /**
     * Returns a set of all outgoing arcs from this node. The order of arcs may
     * differ from the creation order.
     *
     * @return a set of outgoing arcs
     */
    public final HashSet<Arc> getOutgoingArcsSet() {
        return this.itsOutgoingArcs;
    }

    /**
     * Returns an iterator through all outgoing arcs from this node. The order
     * of arcs may differ from the creation order.
     *
     * @return an iterator of outgoing arcs
     */
    public final Iterator<Arc> getOutgoingArcsIterator() {
        return this.itsOutgoingArcs.iterator();
    }

    /**
     * Returns the number of outgoing arcs of the specified type.
     *
     * @param aType the arc type to count
     * @return the number of outgoing arcs of the given type
     */
    public final int getNumberOfOutgoingArcs(Type aType) {
        int n = 0;
        for (Arc go : this.itsOutgoingArcs) {
            if (go.getType().compareTo(aType)) {
                n++;
            }
        }
        return n;
    }

    /**
     * Returns the number of outgoing arcs of the specified type to nodes of the
     * specified target type.
     *
     * @param aType the arc type to count
     * @param tarType the target node type to filter by
     * @return the number of matching outgoing arcs
     */
    public final int getNumberOfOutgoingArcsOfTypeToTargetType(Type aType, Type tarType) {
        int n = 0;
        for (Arc go : this.itsOutgoingArcs) {
            if (go.getType().compareTo(aType)) {
                if (tarType.isParentOf(go.getTarget().getType())) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * Returns the number of outgoing arcs of the specified type to nodes of the
     * specified target type.
     *
     * @param aType the arc type to count
     * @param tar the (parent) type of the target node of outgoing arcs
     * @return the number of matching outgoing arcs
     */
    public final int getNumberOfOutgoingArcs(Type aType, Type tar) {
        int n = 0;
        for (Arc go : this.itsOutgoingArcs) {
            if (go.getType().compareTo(aType)) {
                if (tar.isParentOf(go.getTargetType())) {
                    n++;
                } else if (!this.itsContext.isCompleteGraph()
                        && go.getTargetType().isParentOf(tar)) {
                    n++;
                }
            }
        }
        return n;
    }

    /**
     * Checks if this node has an outgoing arc of the specified type to the
     * specified target node.
     *
     * @param arct the arc type to check
     * @param tar the target node to check
     * @return true if such an arc exists, false otherwise
     */
    public boolean hasArc(final Type arct, final Node tar) {
        return (this.getOutgoingArc(arct, tar) != null);
    }

    /**
     * Returns a list of outgoing arcs of the specified type to nodes of the
     * specified target type.
     *
     * @param aType the arc type to filter by
     * @param tar the (parent) type of the target node of outgoing arcs
     * @return a list of matching outgoing arcs
     */
    public final List<Arc> getOutgoingArcs(Type aType, Type tar) {
        final List<Arc> result = new ArrayList<>(2);
        for (Arc go : this.itsOutgoingArcs) {
            if (go.getType().compareTo(aType)) {
                if (tar.isParentOf(go.getTargetType())) {
                    result.add(go);
                } else if (!this.itsContext.isCompleteGraph()
                        && go.getTargetType().isParentOf(tar)) {
                    result.add(go);
                }
            }
        }
        return result;
    }

    /**
     * Returns the outgoing arc of the specified type to the specified target
     * node.
     *
     * @param t the arc type to search for
     * @param tar the target node to search for
     * @return the outgoing arc, or null if not found
     */
    public final Arc getOutgoingArc(Type t, Node tar) {
        for (Arc go : this.itsOutgoingArcs) {
            if (go.getTarget() == tar
                    && go.getType().compareTo(t)) {
                return go;
            }
        }
        return null;
    }

    /**
     * Checks if this node has an outgoing arc to a node of the specified type.
     *
     * @param targetType the (parent) type of the target node of an outgoing arc
     * @return true if an outgoing arc to the specified type exists, false
     * otherwise
     */
    public boolean hasOutgoingArcTo(Type targetType) {
        for (Arc arc : this.itsOutgoingArcs) {
            if (targetType.isParentOf(arc.getTargetType())
                    || arc.getTargetType().isParentOf(targetType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the total number of incoming and outgoing arcs connected to this
     * node.
     *
     * @return the total number of arcs
     */
    @Override
    public final int getNumberOfInOutArcs() {
        int nb = this.itsIncomingArcs.size() + this.itsOutgoingArcs.size();
        return nb;
    }

    /**
     * Converts this node's type to a type key string that is used for search
     * operations.
     *
     * @return the type key string
     */
    @Override
    public String convertToKey() {
        return this.getType().convertToKey();
    }

    /**
     * Resets and returns the type key for this node.
     *
     * @return the reset type key
     */
    @Override
    public String resetTypeKey() {
        return this.getType().resetKey();
    }

    /**
     * Compares this node to another graph object for equality.
     *
     * @param otherObject the graph object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean compareTo(GraphObject otherObject) {
        if (!otherObject.isNode()) {
            return false;
        }
        Node otherNode = (Node) otherObject;
        if (!this.itsType.isParentOf(otherNode.getType())) {
            return false;
        }
        final boolean okay = (this.itsAttr == null && otherNode.getAttribute() == null)
                || ((this.attrExists() && otherNode.attrExists())
                && this.itsAttr.compareTo(otherNode.getAttribute()));
        if (!okay) {
            return false;
        }
        return this.compareMultiplicityTo(otherNode);
    }

    /**
     * Compares multiplicity information between this node and another node.
     *
     * @param otherNode the other node to compare with
     * @return true if multiplicity information matches, false otherwise
     */
    protected boolean compareMultiplicityTo(Node otherNode) {
        if (this.itsContext.isTypeGraph()) {
            int minmax = this.itsType.getSourceMin();
            int otherMinmax = otherNode.getType().getSourceMin();
            if (minmax != otherMinmax) {
                return false;
            } else {
                minmax = this.itsType.getSourceMax();
                otherMinmax = otherNode.getType().getSourceMax();
                if (minmax != otherMinmax) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Writes this node to XML.
     *
     * @param xmlHelper the XML helper to write with
     */
    @Override
    public void XwriteObject(XMLHelper xmlHelper) {
        xmlHelper.openNewElem("Node", this);
        if (!this.visible) {
            xmlHelper.addAttr("visible", "false");
        }
        if (!this.getObjectName().equals("")) {
            xmlHelper.addAttr("name", this.getObjectName());
        }
        xmlHelper.addObject("type", this.itsType, false);
        if (this.itsContext != null && this.itsContext.isTypeGraph()) {
            int minmax = this.itsType.getSourceMin();
            if (minmax != Type.UNDEFINED) {
                xmlHelper.addAttr("sourcemin", Integer.toString(minmax));
            }
            minmax = this.itsType.getSourceMax();
            if (minmax != Type.UNDEFINED) {
                xmlHelper.addAttr("sourcemax", Integer.toString(minmax));
            }
        }
        xmlHelper.addObject("", this.itsAttr, true);
        xmlHelper.close();
    }

    /**
     * Reads this node from XML.
     *
     * @param xmlHelper the XML helper to read from
     */
    @Override
    public void XreadObject(XMLHelper xmlHelper) {
        if (xmlHelper.isTag("Node", this)) {
            String str = xmlHelper.readAttr("visible");
            this.visible = !str.equals("false");
            str = xmlHelper.readAttr("name");
            this.setObjectName(str);
            if (this.itsType.getAttrType() != null
                    || this.itsType.hasInheritedAttribute()
                    || (this.itsContext != null && this.itsContext.xyAttr)) {
                this.createAttributeInstance();
            }
            AttrInstance attri = this.itsAttr;
            if (attri != null) {
                if (this.itsContext != null && this.itsContext.xyAttr) {
                    xyAttr = true;
                    agg.attribute.AttrType attrType = itsType.getAttrType();
                    if (!((agg.attribute.impl.DeclTuple) attrType).containsName("thisX")) {
                        attrType.addMember(
                                agg.attribute.facade.impl.DefaultInformationFacade.self().getJavaHandler(),
                                "int", "thisX");
                    }
                    if (!((agg.attribute.impl.DeclTuple) attrType).containsName("thisY")) {
                        attrType.addMember(
                                agg.attribute.facade.impl.DefaultInformationFacade.self().getJavaHandler(),
                                "int", "thisY");
                    }
                }
                xmlHelper.enrichObject(attri);
            }
            xmlHelper.close();
            // If this node uses variables in its attribute, mark the variable
            if (this.itsContext != null && this.itsContext.getAttrContext() != null
                    && this.itsAttr != null) {
                ValueTuple value = (ValueTuple) this.itsAttr;
                for (int i = 0; i < value.getSize(); i++) {
                    ValueMember val = value.getValueMemberAt(i);
                    if (val.isSet()) {
                        if (val.getExpr().isVariable()) {
                            ContextView viewContext = (ContextView) ((ValueTuple) val
                                    .getHoldingTuple()).getContext();
                            VarTuple variable = (VarTuple) viewContext
                                    .getVariables();
                            VarMember var = variable.getVarMemberAt(val
                                    .getExprAsText());
                            if (getContext().isNacGraph()) {
                                var.setMark(VarMember.NAC);
                            } else if (getContext().isPacGraph()) {
                                var.setMark(VarMember.PAC);
                            } else if (viewContext.doesAllowComplexExpressions()) {
                                var.setMark(VarMember.RHS);
                            } else {
                                var.setMark(VarMember.LHS);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Always returns false since this is a node, not an arc.
     *
     * @return false
     */
    @Override
    public final boolean isArc() {
        return false;
    }

    /**
     * Always returns true since this is a node.
     *
     * @return true
     */
    @Override
    public final boolean isNode() {
        return true;
    }

    /**
     * Checks if this node is isolated (has no incoming or outgoing arcs).
     *
     * @return true if this node has no arcs, false otherwise
     */
    public final boolean isIsolated() {
        return this.itsOutgoingArcs.isEmpty()
                && this.itsIncomingArcs.isEmpty();
    }

    /**
     * Returns a string representation of this node.
     *
     * @return a string containing the node's hash code, type, and attributes
     */
    @Override
    public String toString() {
        String typeStr = this.itsType.getStringRepr();
        if (this.itsAttr != null) {
            return " (" + "[" + hashCode() + "] " + "Node: " + typeStr + ")  "
                    + this.itsAttr.toString();
        } else {
            return " (" + "[" + hashCode() + "] " + "Node: " + typeStr + ") ";
        }
    }

    /**
     * Called when an attribute changes. Implements the AttrObserver interface
     * and propagates attribute changes to the graph.
     *
     * @param ev the attribute event that occurred
     */
    @Override
    public void attributeChanged(AttrEvent ev) {
        super.attributeChanged(ev);
        if (this.itsContext != null) {
            Pair<Object, AttrEvent> p = new Pair<>(this, ev);
            if (this.itsContext.isTypeGraph()) {
                if (ev.getID() == AttrEvent.MEMBER_VALUE_MODIFIED) {
                    propagateAttrValueToChildNode();
                }
            }
            this.itsContext.propagateChange(new Change(Change.OBJECT_MODIFIED, p));
        }
    }

    /**
     * Propagates attribute value changes to child nodes in the type graph. This
     * is used for inheritance of attribute values.
     */
    private void propagateAttrValueToChildNode() {
        for (Type cht : this.getType().getChildren()) {
            List<Node> chnodes = this.itsContext.getNodesByParentType(cht);
            if (chnodes != null) {
                Node childNode = chnodes.get(0);
                if (childNode != this) {
                    setValueToChildMember(childNode);
                }
            }
        }
    }

    /**
     * Sets attribute values to child nodes when parent node attributes change.
     * Used for attribute inheritance in type graphs.
     *
     * @param childNode the child node to update
     */
    private void setValueToChildMember(Node childNode) {
        final ValueTuple valueTuple = (ValueTuple) this.itsAttr;
        for (int i = 0; i < valueTuple.getNumberOfEntries(); i++) {
            ValueMember vm = valueTuple.getValueMemberAt(i);
            if (vm.isSet()
                    && childNode.getAttribute() != null
                    && ((ValueTuple) childNode.getAttribute()).getValueAt(vm.getName()) == null) {
                ((ValueTuple) childNode.getAttribute()).setExprValueAt(vm.getExprAsText(),
                        vm.getName());
            }
        }
    }

    /**
     * Propagates attribute values from parent nodes to this node. This is used
     * for inheritance of attribute values in type graphs.
     */
    public void propagateAttrValueFromParentNode() {
        if (!this.itsContext.isTypeGraph()) {
            return;
        }
        for (Type part : this.getType().getParents()) {
            List<Node> parnodes = this.itsContext.getNodesByParentType(part);
            if (parnodes != null) {
                Node parNode = parnodes.get(0);
                if (parNode != this) {
                    setValueFromParentMember(parNode);
                }
            }
        }
    }

    /**
     * Sets attribute values from parent node to this node. Used for attribute
     * inheritance in type graphs.
     *
     * @param parentNode the parent node to copy attributes from
     */
    private void setValueFromParentMember(Node parentNode) {
        if (parentNode.getAttribute() != null && this.itsAttr == null) {
            this.createAttributeInstance();
            for (int i = 0; i < ((ValueTuple) this.itsAttr).getNumberOfEntries(); i++) {
                ValueMember vm = ((ValueTuple) this.itsAttr).getValueMemberAt(i);
                ValueMember parvm = ((ValueTuple) parentNode.getAttribute()).getValueMemberAt(
                        vm.getName());
                if (parvm != null && !vm.isSet() && parvm.isSet()) {
                    ((ValueTuple) this.itsAttr).setExprValueAt(parvm.getExprAsText(),
                            parvm.getName());
                }
            }
        }
    }
}
