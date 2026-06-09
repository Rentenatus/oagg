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
import java.util.List;

/**
 * Represents an arc (edge) in a graph. Arcs connect two graph objects
 * (typically nodes) and have a type, a direction (determined by the graph's
 * orientation), and optionally attributes. Arcs are the connections between
 * nodes in a graph.
 */
@SuppressWarnings("serial")
public class Arc extends GraphObject implements XMLObject {

    protected boolean inheritance = false;
    protected GraphObject itsSource;
    protected GraphObject itsTarget;
    protected String keyStr = null;

    /**
     * Creates a new arc with the specified type, source, target, and graph
     * context. This constructor also adds the arc to the source and target
     * nodes' arc lists according to the graph's orientation strategy.
     *
     * @param type the type of this arc
     * @param source the source graph object of this arc
     * @param target the target graph object of this arc
     * @param context the graph this arc belongs to
     */
    protected Arc(final Type type,
            final GraphObject source,
            final GraphObject target,
            final Graph context) {
        this.itsContext = context;
        this.itsType = type;
        this.itsSource = source;
        this.itsTarget = target;
        addToSrcTar(this.itsSource, this.itsTarget);
        this.itsContextUsage = hashCode();
        if (!this.itsType.isAttrTypeEmpty()) {
            this.itsAttr = AttrTupleManager.getDefaultManager().newInstance(
                    this.itsType.getAttrType(), context.getAttrContext());
        }
        if (this.itsAttr != null) {
            this.itsAttr.addObserver(this);
        }
        this.keyStr = this.itsSource.getType().convertToKey()
                .concat(this.itsType.convertToKey())
                .concat(this.itsTarget.getType().convertToKey());
    }

    /**
     * Creates a new arc with the specified attribute instance, type, source,
     * target, and graph context. This constructor also adds the arc to the
     * source and target nodes' arc lists according to the graph's orientation
     * strategy.
     *
     * @param attr an attribute instance for this arc, may be null
     * @param type the type of this arc
     * @param source the source graph object of this arc
     * @param target the target graph object of this arc
     * @param context the graph this arc belongs to
     */
    public Arc(final AttrInstance attr,
            final Type type,
            final GraphObject source,
            final GraphObject target,
            final Graph context) {
        this.itsContext = context;
        this.itsType = type;
        this.itsSource = source;
        this.itsTarget = target;
        addToSrcTar(this.itsSource, this.itsTarget);
        this.itsContextUsage = hashCode();
        this.itsAttr = attr;
        if (this.itsAttr != null) {
            this.itsAttr.addObserver(this);
        }
        this.keyStr = this.itsSource.getType().convertToKey()
                .concat(this.itsType.convertToKey())
                .concat(this.itsTarget.getType().convertToKey());
    }

    /**
     * Creates a copy of the specified arc with new source, target, and graph
     * context.
     *
     * @param orig the original arc to copy
     * @param source the new source graph object
     * @param target the new target graph object
     * @param context the new graph context
     */
    protected Arc(final Arc orig,
            final GraphObject source,
            final GraphObject target,
            final Graph context) {
        this(orig.getType(), source, target, context);
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
     * Adds this arc to the source and target nodes according to the graph's
     * orientation strategy.
     *
     * @param sourceNode the source node
     * @param targetNode the target node
     */
    protected void addToSrcTar(final GraphObject sourceNode, final GraphObject targetNode) {
        if ((sourceNode != null) && (targetNode != null) && this.itsContext != null) {
            ((Graph) this.itsContext).getOrientation().addArcToNodes(this, (Node) sourceNode,
                    (Node) targetNode);
        }
    }

    /**
     * Disposes this arc by removing it from its source and target nodes
     * according to the graph's orientation strategy and cleaning up resources.
     */
    public void dispose() {
        if (this.itsContext != null && this.itsSource != null && this.itsTarget != null) {
            ((Graph) this.itsContext).getOrientation().removeArcFromNodes(this,
                    (Node) this.itsSource, (Node) this.itsTarget);
        }
        if (this.itsAttr != null) {
            this.itsAttr.removeObserver(this);
            ((ValueTuple) this.itsAttr).dispose();
            this.itsAttr = null;
        }
        this.itsType = null;
        this.itsContext = null;
        this.itsContextUsage = -1;
        this.itsTarget = null;
        this.itsSource = null;
    }

    /**
     * Sets whether this arc is an inheritance edge in a type graph.
     *
     * @param inherit true if this arc represents inheritance, false otherwise
     */
    protected void setInheritance(boolean inherit) {
        this.inheritance = inherit;
    }

    /**
     * Checks if this arc is an inheritance edge in a type graph.
     *
     * @return true if this arc represents inheritance, false otherwise
     */
    public boolean isInheritance() {
        return this.inheritance;
    }

    /**
     * Always returns true since this is an arc, not a node.
     *
     * @return true
     */
    @Override
    public final boolean isArc() {
        return true;
    }

    /**
     * Always returns false since this is an arc, not a node.
     *
     * @return false
     */
    @Override
    public final boolean isNode() {
        return false;
    }

    /**
     * Checks if this arc is abstract. Always returns false for arcs.
     *
     * @return false
     */
    public boolean isAbstract() {
        return false;
    }

    /**
     * Returns the source graph object of this arc.
     *
     * @return the source graph object
     */
    public final GraphObject getSource() {
        return this.itsSource;
    }

    /**
     * Returns the target graph object of this arc.
     *
     * @return the target graph object
     */
    public final GraphObject getTarget() {
        return this.itsTarget;
    }

    /**
     * Sets the source node of this arc and updates the node's arc lists
     * according to the graph's orientation.
     *
     * @param newSource the new source node
     */
    public void setSource(Node newSource) {
        if (this.itsSource != null && this.itsContext != null) {
            Graph g = (Graph) this.itsContext;
            g.getOrientation().removeArcFromNodes(this, (Node) this.itsSource,
                    (Node) this.itsTarget);
        }
        this.itsSource = newSource;
        if (newSource != null && this.itsContext != null) {
            Graph g = (Graph) this.itsContext;
            g.getOrientation().addArcToNodes(this, newSource, (Node) this.itsTarget);
        }
        this.keyStr = this.itsSource.getType().convertToKey()
                .concat(this.itsType.convertToKey())
                .concat(this.itsTarget.getType().convertToKey());
    }

    /**
     * Sets the target node of this arc and updates the node's arc lists
     * according to the graph's orientation.
     *
     * @param newTarget the new target node
     */
    public void setTarget(Node newTarget) {
        if (this.itsTarget != null && this.itsContext != null) {
            Graph g = (Graph) this.itsContext;
            g.getOrientation().removeArcFromNodes(this, (Node) this.itsSource,
                    (Node) this.itsTarget);
        }
        this.itsTarget = newTarget;
        if (newTarget != null && this.itsContext != null) {
            Graph g = (Graph) this.itsContext;
            g.getOrientation().addArcToNodes(this, (Node) this.itsSource, newTarget);
        }
        this.keyStr = this.itsSource.getType().convertToKey()
                .concat(this.itsType.convertToKey())
                .concat(this.itsTarget.getType().convertToKey());
    }

    /**
     * Returns the type of the source graph object.
     *
     * @return the source type
     */
    public Type getSourceType() {
        return this.itsSource.getType();
    }

    /**
     * Returns the type of the target graph object.
     *
     * @return the target type
     */
    public Type getTargetType() {
        return this.itsTarget.getType();
    }

    /**
     * Converts this arc's type to a type key string that can be used for search
     * operations: sourceType.convertToKey() + this.itsType.convertToKey() +
     * targetType.convertToKey()
     *
     * @return the type key string
     */
    @Override
    public String convertToKey() {
        this.keyStr = this.itsSource.getType().convertToKey()
                .concat(this.itsType.convertToKey())
                .concat(this.itsTarget.getType().convertToKey());
        return this.keyStr;
    }

    /**
     * Resets and returns the type key for this arc.
     *
     * @return the reset type key
     */
    @Override
    public String resetTypeKey() {
        this.keyStr = this.itsSource.getType().resetKey()
                .concat(this.itsType.resetKey())
                .concat(this.itsTarget.getType().resetKey());
        return this.keyStr;
    }

    /**
     * Converts this arc's type to a key string extended with all parent types.
     * This generates keys for all combinations of parent types of source and
     * target.
     *
     * @return a list of extended key strings
     */
    public List<String> convertToKeyParentExtended() {
        final List<String> list = new ArrayList<>();
        List<Type> mySrcParents = this.getSource().getType().getAllParents();
        List<Type> myTrgParents = this.getTarget().getType().getAllParents();
        for (Type srcParent : mySrcParents) {
            for (Type trgParent : myTrgParents) {
                String keystr = srcParent.convertToKey()
                        + this.getType().convertToKey()
                        + trgParent.convertToKey();
                list.add(keystr);
            }
        }
        return list;
    }

    /**
     * Returns the type map key for this arc. The key is the string:
     * getSource().getType().convertToKey() + getType().convertToKey() +
     * getTarget().getType().convertToKey() This is used to fill the type to
     * objects map of a graph.
     *
     * @return the type map key string
     */
    public String getTypeMapKey() {
        return this.convertToKey();
    }

    /**
     * Checks if this arc is directed. The direction is determined by the
     * graph's orientation, not by an internal flag.
     *
     * @return true if the graph is directed, false otherwise
     */
    public boolean isDirected() {
        return this.itsContext != null && this.itsContext.isDirected();
    }

    /**
     * Sets the directed flag. This method is deprecated as the direction is now
     * determined by the graph's orientation. This method does nothing and is
     * kept for backward compatibility.
     *
     * @param directed the directed flag (ignored)
     * @deprecated Direction is now determined by the graph's orientation
     */
    @Deprecated
    public void setDirected(boolean directed) {
        // Direction is now determined by the graph's orientation, not by an internal flag
        // This method is kept for backward compatibility but does nothing
    }

    /**
     * Checks if this arc is a loop (connects a node to itself).
     *
     * @return true if source and target are the same, false otherwise
     */
    public boolean isLoop() {
        return (this.itsSource == this.itsTarget);
    }

    /**
     * Compares this arc to another graph object for equality.
     *
     * @param otherObject the graph object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean compareTo(GraphObject otherObject) {
        if (otherObject == null || !otherObject.isArc()) {
            return false;
        }
        Arc otherArc = (Arc) otherObject;
        if (!this.itsType.isParentOf(otherArc.getType())) {
            return false;
        }
        final boolean okay = (this.itsAttr == null && otherArc.getAttribute() == null)
                || (this.attrExists() && otherArc.attrExists()
                && this.itsAttr.compareTo(otherArc.getAttribute()));
        if (!okay) {
            return false;
        }
        if (!this.compareSrcTarTo(otherArc)) {
            return false;
        }
        return this.compareMultiplicityTo(otherArc);
    }

    /**
     * Compares the source and target of this arc with another arc.
     *
     * @param otherArc the other arc to compare with
     * @return true if source and target match, false otherwise
     */
    protected boolean compareSrcTarTo(Arc otherArc) {
        return ((Node) getSource()).compareTo(otherArc.getSource())
                && ((Node) getTarget()).compareTo(otherArc.getTarget());
    }

    /**
     * Compares multiplicity information of this arc with another arc. This is
     * only relevant for arcs in type graphs.
     *
     * @param otherArc the other arc to compare with
     * @return true if multiplicity information matches, false otherwise
     */
    protected boolean compareMultiplicityTo(Arc otherArc) {
        if (this.itsContext.isTypeGraph()) {
            Type sourceType = getSource().getType();
            Type targetType = getTarget().getType();
            Type otherSourceType = otherArc.getSource().getType();
            Type otherTargetType = otherArc.getTarget().getType();
            int minmax = this.itsType.getSourceMin(sourceType, targetType);
            int otherMinmax = otherArc.getType().getSourceMin(otherSourceType, otherTargetType);
            if (minmax != otherMinmax) {
                return false;
            } else {
                minmax = this.itsType.getTargetMin(sourceType, targetType);
                otherMinmax = otherArc.getType().getTargetMin(otherSourceType, otherTargetType);
                if (minmax != otherMinmax) {
                    return false;
                } else {
                    minmax = this.itsType.getSourceMax(sourceType, targetType);
                    otherMinmax = otherArc.getType().getSourceMax(otherSourceType,
                            otherTargetType);
                    if (minmax != otherMinmax) {
                        return false;
                    } else {
                        minmax = this.itsType.getTargetMax(sourceType, targetType);
                        otherMinmax = otherArc.getType().getTargetMax(otherSourceType,
                                otherTargetType);
                        if (minmax != otherMinmax) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Writes this arc to XML.
     *
     * @param xmlHelper the XML helper to write with
     */
    @Override
    public void XwriteObject(XMLHelper xmlHelper) {
        xmlHelper.openNewElem("Edge", this);
        if (!this.visible) {
            xmlHelper.addAttr("visible", "false");
        }
        if (!this.getObjectName().equals("")) {
            xmlHelper.addAttr("name", this.getObjectName());
        }
        xmlHelper.addObject("type", this.itsType, false);
        xmlHelper.addObject("source", getSource(), false);
        xmlHelper.addObject("target", getTarget(), false);
        if (this.itsContext != null && this.itsContext.isTypeGraph()) {
            Type sourceType = getSource().getType();
            Type targetType = getTarget().getType();
            int minmax = this.itsType.getSourceMin(sourceType, targetType);
            if (minmax != Type.UNDEFINED) {
                xmlHelper.addAttr("sourcemin", Integer.toString(minmax));
            }
            minmax = this.itsType.getTargetMin(sourceType, targetType);
            if (minmax != Type.UNDEFINED) {
                xmlHelper.addAttr("targetmin", Integer.toString(minmax));
            }
            minmax = this.itsType.getSourceMax(sourceType, targetType);
            if (minmax != Type.UNDEFINED) {
                xmlHelper.addAttr("sourcemax", Integer.toString(minmax));
            }
            minmax = this.itsType.getTargetMax(sourceType, targetType);
            if (minmax != Type.UNDEFINED) {
                xmlHelper.addAttr("targetmax", Integer.toString(minmax));
            }
        }
        xmlHelper.addObject("", this.itsAttr, true);
        xmlHelper.close();
    }

    /**
     * Reads this arc from XML.
     *
     * @param xmlHelper the XML helper to read from
     */
    @Override
    public void XreadObject(XMLHelper xmlHelper) {
        if (xmlHelper.isTag("Edge", this)) {
            String str = xmlHelper.readAttr("visible");
            this.visible = !str.equals("false");
            str = xmlHelper.readAttr("name");
            this.setObjectName(str);
            if (this.itsType.getAttrType() != null
                    || this.itsType.hasInheritedAttribute()) {
                this.createAttributeInstance();
            }
            AttrInstance attri = this.itsAttr;
            if (attri != null) {
                xmlHelper.enrichObject(attri);
            }
            xmlHelper.close();
            // If this arc uses variables in its attribute, mark the variable
            if (this.itsContext != null
                    && this.itsContext.getAttrContext() != null
                    && this.itsAttr != null) {
                ValueTuple value = (ValueTuple) this.itsAttr;
                for (int i = 0; i < value.getSize(); i++) {
                    ValueMember val = value.getValueMemberAt(i);
                    if (val.isSet() && val.getExpr().isVariable()) {
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

    /**
     * Returns a string representation of this arc.
     *
     * @return a string containing the arc's hash code, type, source type,
     * target type, and attributes
     */
    @Override
    public String toString() {
        String result;
        String t = this.itsType.getStringRepr();
        if (t.equals("")) {
            t = "[unnamed]";
        }
        String tSrc = getSource().getType().getStringRepr();
        if (tSrc.equals("")) {
            tSrc = "[unnamed]";
        }
        String tTrg = getTarget().getType().getStringRepr();
        if (tTrg.equals("")) {
            tTrg = "[unnamed]";
        }
        result = " (" + "[" + hashCode() + "] " + "Arc: " + tSrc + "---" + t
                + "---" + tTrg + ") ";
        if (this.itsAttr != null) {
            result = result + this.itsAttr.toString();
        }
        return result;
    }

    /**
     * Implements the AttrObserver. Propagates the change
     * <code>agg.util.Change.OBJECT_MODIFIED</code> and object Pair (this,
     * ev.getID()) to its Graph if the attributes are changed.
     *
     * @param ev the attribute event that occurred
     */
    @Override
    public void attributeChanged(AttrEvent ev) {
        super.attributeChanged(ev);
        if (this.itsContext != null) {
            Pair<Object, AttrEvent> p = new Pair<>(this, ev);
            this.itsContext.propagateChange(new Change(Change.OBJECT_MODIFIED, p));
        }
    }
}
