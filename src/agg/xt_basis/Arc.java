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
 * @version $Id: Arc.java,v 1.40 2010/11/06 18:34:59 olga Exp $
 * @author $Author: olga $
 */
@SuppressWarnings("serial")
public class Arc extends GraphObject implements XMLObject {

    protected boolean inheritance = false;
    protected GraphObject itsSource;
    protected GraphObject itsTarget;
    protected String keyStr = null;

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
     * @param attr An attribute instance of a new arc if it should have
     * attributes. May be <code>null</code>.
     * @param type An arc type of a new arc.
     * @param source A source node of a new arc.
     * @param target A target node of a new arc.
     * @param context A graph in which to consider a new arc with its source and
     * target nodes.
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
        // object name is not jet used in AGG GUI
        if (!"".equals(orig.getObjectName())) {
            this.setObjectName(orig.getObjectName());
        }
    }

    /**
     * Adds this arc to the source and target nodes according to the
     * graph's orientation strategy.
     *
     * @param sourceNode the source node
     * @param targetNode the target node
     */
    protected void addToSrcTar(final GraphObject sourceNode, final GraphObject targetNode) {
        if ((sourceNode != null) && (targetNode != null) && this.itsContext != null) {
            ((Graph) this.itsContext).getOrientation().addArcToNodes(this, (Node) sourceNode, (Node) targetNode);
        }
    }

    /**
     * Disposes this arc by removing it from its source and target nodes
     * according to the graph's orientation strategy and cleaning up resources.
     */
    public void dispose() {
//		long t = System.nanoTime();
        if (this.itsContext != null && this.itsSource != null && this.itsTarget != null) {
            ((Graph) this.itsContext).getOrientation().removeArcFromNodes(this, (Node) this.itsSource, (Node) this.itsTarget);
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
//		System.out.println("Arc disposed  in: "+(System.nanoTime()-t)+"nano");
    }

    /**
     * If the specified parameter is <code>true</code> set this edge to be an
     * inheritance edge of a type graph.
     *
     * @param inherit
     */
    protected void setInheritance(boolean inherit) {
        this.inheritance = inherit;
    }

    /**
     * Returns true if this edge is an inheritance edge of a type graph.
     *
     * @return
     */
    public boolean isInheritance() {
        return this.inheritance;
    }

    @Override
    public final boolean isArc() {
        return true;
    }

    @Override
    public final boolean isNode() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    public final GraphObject getSource() {
        return this.itsSource;
    }

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
            g.getOrientation().removeArcFromNodes(this, (Node) this.itsSource, (Node) this.itsTarget);
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
            g.getOrientation().removeArcFromNodes(this, (Node) this.itsSource, (Node) this.itsTarget);
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

    public Type getSourceType() {
        return this.itsSource.getType();
    }

    public Type getTargetType() {
        return this.itsTarget.getType();
    }

    /**
     * Converts my type to the type key string that can be used for search
     * operations:      <code> ((Arc) this).getSource().getType().convertToKey()
     * + ((Arc) this).getType().convertToKey()
     * + ((Arc) this).getTarget().getType().convertToKey()
     * </code>
     *
     * @return
     */
    @Override
    public String convertToKey() {
        this.keyStr = this.itsSource.getType().convertToKey()
                .concat(this.itsType.convertToKey())
                .concat(this.itsTarget.getType().convertToKey());
        return this.keyStr;
    }

    /**
     *
     * @return
     */
    @Override
    public String resetTypeKey() {
        this.keyStr = this.itsSource.getType().resetKey()
                .concat(this.itsType.resetKey())
                .concat(this.itsTarget.getType().resetKey());
        return this.keyStr;
    }

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
     * The edge type map key is the string:
     * getSource().getType().convertToKey()+getType().convertToKey()+getTarget().getType().convertToKey()
     * and is used to fill the type to objects map of a graph.
     *
     * @return String key
     */
    public String getTypeMapKey() {
        return this.convertToKey();
    }

    /**
     * Returns whether this arc is directed. The direction is determined by
     * the graph's orientation, not by an internal flag.
     *
     * @return true if the graph is directed, false otherwise
     */
    public boolean isDirected() {
        return this.itsContext != null && this.itsContext.isDirected();
    }

    /**
     * Sets the directed flag. This method is deprecated as the direction
     * is now determined by the graph's orientation. This method does nothing.
     *
     * @param directed the directed flag (ignored)
     */
    @Deprecated
    public void setDirected(boolean directed) {
        // Direction is now determined by the graph's orientation, not by an internal flag
        // This method is kept for backward compatibility but does nothing
    }

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
//		if (!this.getObjectName().equals(otherArc.getObjectName())) {
//			return false;
//		}
        if (!this.itsType.isParentOf(otherArc.getType())) {
            return false;
        }
        if ((this.itsAttr == null && otherArc.getAttribute() == null)
                || (this.attrExists() && otherArc.attrExists()
                && this.itsAttr.compareTo(otherArc.getAttribute()))) {
            ;
        } else {
            return false;
        }
        if (!this.compareSrcTarTo(otherArc)) {
            return false;
        }
        if (!this.compareMultiplicityTo(otherArc)) {
            return false;
        }
        return true;
    }

    protected boolean compareSrcTarTo(Arc otherArc) {
        return ((Node) getSource()).compareTo(otherArc.getSource())
                && ((Node) getTarget()).compareTo(otherArc.getTarget());
    }

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
        // save multiplicity, if part of type graph
        if (this.itsContext != null && this.itsContext.isTypeGraph()) {
            // System.out.println("Arc.Xwrite... is elem of type graph");
            Type sourceType = getSource().getType();
            Type targetType = getTarget().getType();
            int minmax = this.itsType.getSourceMin(sourceType, targetType);
            if (minmax != Type.UNDEFINED) {
                xmlHelper.addAttr("sourcemin", Integer.toString(minmax));
            }
            minmax = this.itsType.getTargetMin(sourceType, targetType);
            // System.out.println("targetmin " +minmax);
            if (minmax != Type.UNDEFINED) {
                xmlHelper.addAttr("targetmin", Integer.toString(minmax));
            }
            minmax = this.itsType.getSourceMax(sourceType, targetType);
            if (minmax != Type.UNDEFINED) {
                xmlHelper.addAttr("sourcemax", Integer.toString(minmax));
            }
            minmax = this.itsType.getTargetMax(sourceType, targetType);
            // System.out.println("targetmax " +minmax);
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
                // if(Debug.HASHCODE){
                // agg.attribute.AttrType attrType = itsType.getAttrType();
                // if(!((agg.attribute.impl.DeclTuple)
                // attrType).containsName("HASHCODE"))
                // attrType.addMember(agg.attribute.facade.impl.DefaultInformationFacade.self().getJavaHandler(),"String",
                // "HASHCODE" );
                //
                // agg.attribute.impl.ValueMember mem =
                // ((agg.attribute.impl.ValueTuple)
                // attri).getValueMemberAt("HASHCODE");
                // String hc = String.valueOf(hashCode());
                // mem.setExprAsObject(hc);
                // mem.checkValidity();
                // }
                xmlHelper.enrichObject(attri);
            }
            xmlHelper.close();
            // if this node uses variable
            // in its attribute so the variable will be marked
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
//						System.out.println(this.itsContext.getName()+"    "+var.getName()+"   "+var.getMark());
                    }
                }
            }
        }
    }

    /**
     *
     * @return
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
