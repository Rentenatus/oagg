/**
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 * *****************************************************************************
 */
package agg.xt_basis;

import agg.attribute.AttrEvent;
import agg.attribute.AttrInstance;
import agg.attribute.AttrObserver;
import agg.attribute.AttrTuple;
import agg.attribute.impl.AttrTupleManager;
import agg.attribute.impl.ValueMember;
import agg.attribute.impl.ValueTuple;
import agg.util.XMLHelper;
import agg.util.XMLObject;
import java.util.ArrayList;
import java.util.List;

/**
 * GraphObject defines the common interface and implementations for Nodes and
 * Arcs.
 *
 * @version $Id: GraphObject.java,v 1.51 2010/11/14 23:51:48 olga Exp $
 * @author $Author: olga $
 * @author Jansuch Rentenatus
 */
@SuppressWarnings("serial")
public abstract class GraphObject implements GraphElement, XMLObject, AttrObserver {

    protected String name = "";
    protected Graph itsContext = null;
    protected Type itsType = null;
    protected AttrInstance itsAttr = null;
    protected int itsContextUsage;
    protected boolean critical = false;
    protected boolean visible = true;
    protected NNVector inputVector;

    public NNVector getInputVector() {
        return inputVector;
    }

    public void setInputVector(NNVector inputVector) {
        this.inputVector = inputVector;
    }

    public NNVector copyInputVector() {
        return inputVector == null ? null : inputVector.copyNNVector();
    }

    public final void createAttributeInstance() {
        if (this.itsType.getAttrType() == null) {
            this.itsType.createAttributeType();
        }
        if (this.itsType.getAttrType() != null) {
            if (this.itsAttr == null) {
                this.itsAttr = AttrTupleManager.getDefaultManager().newInstance(
                        this.itsType.getAttrType(), this.itsContext.getAttrContext());
                this.itsAttr.addObserver(this);
                this.itsContext.attributed = true;
            } else if (this.itsAttr.getType() != this.itsType.getAttrType()) {
                this.itsAttr.removeObserver(this);
                ((ValueTuple) this.itsAttr).dispose();
                this.itsAttr = AttrTupleManager.getDefaultManager().newInstance(
                        this.itsType.getAttrType(), this.itsContext.getAttrContext());
                this.itsAttr.addObserver(this);
            }
        }
    }

    public void disposeAttributeInstance() {
        if (this.itsAttr != null) {
            this.itsAttr.removeObserver(this);
            ((ValueTuple) this.itsAttr).dispose();
            this.itsAttr = null;
        }
    }

    /**
     * Sets the object name.
     * @param name the name to set (can be null)
     */
    public final void setObjectName(final String name) {
        this.name = (name != null) ? name : "";
    }

    public final String getObjectName() {
        return this.name;
    }

    public final int getContextUsage() {
        return this.itsContextUsage;
    }

    /**
     * Sets the context usage identifier.
     * @param contextUsage the context usage to set
     */
    public final void setContextUsage(int contextUsage) {
        this.itsContextUsage = contextUsage;
    }

    /**
     * Returns the graph context.
     * @return the graph context
     */
    public final Graph getContext() {
        return this.itsContext;
    }

    /**
     * Sets whether this object is critical.
     * @param critical true if critical, false otherwise
     */
    public final void setCritical(boolean critical) {
        this.critical = critical;
    }

    public final boolean isCritical() {
        return this.critical;
    }

    /**
     * Sets whether this object is visible.
     * @param visible true if visible, false otherwise
     */
    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    public final boolean isVisible() {
        return this.visible;
    }

    public final Type getType() {
        return this.itsType;
    }

    public final void setType(Type type) {
        this.itsType = type;
    }

    /**
     * Converts my type to a type key string that can be used for search
     * operations. For a node it is similar to
	 * <code> ((Node) this).getType().convertToKey() </code>, for an edge to      <code> ((Arc) this).getSource().getType().convertToKey()
     * + ((Arc) this).getType().convertToKey()
     * + ((Arc) this).getTarget().getType().convertToKey()
     * </code>
     */
    public abstract String convertToKey();

    public abstract String resetTypeKey();

    /**
     * Return my attribute value.
     */
    public final AttrInstance getAttribute() {
        return this.itsAttr;
    }

    /**
     * True when <code>this.itsAttr != null && this.itsAttr.getNumberOfEntries() > 0
     * </code>
     */
    public final boolean attrExists() {
        return this.itsAttr != null && this.itsAttr.getNumberOfEntries() > 0;
    }

    public final int getNumberOfAttributes() {
        return this.itsAttr == null ? 0 : this.itsAttr.getNumberOfEntries();
    }

    public final List<String> getVariableNamesOfAttribute() {
        return this.itsAttr == null ? new ArrayList<String>(0) : ((ValueTuple) this.itsAttr).getAllVariableNames();
    }

    /**
     * Copies attributes from another graph object.
     * @param original the graph object to copy attributes from
     */
    public synchronized void copyAttributes(GraphObject original) {
        if (original.getAttribute() != null) {
            if (this.itsAttr == null) {
                this.createAttributeInstance();
            }
            this.itsAttr.copyEntries(original.getAttribute());
        }
    }

    /**
     * Implements the AttrObserver and propagates attribute changes to the
     * attribute observers.
     *
     * @param ev
     */
    @Override
    public void attributeChanged(AttrEvent ev) {
        if (inputVector != null && inputVector.nullOutAtAttrChange()) {
            inputVector = null;
        }
//		Pair<Object, Integer> p = new Pair<Object, Integer>(this, Integer.valueOf(
//				ev.getID()));
//		itsContext.propagateChange(new Change(Change.OBJECT_MODIFIED, p));
    }

    public String attributeToString() {
        String result = "\nAttributes of : " + getType().getName() + " {\n";
        for (int i = 0; i < this.itsAttr.getNumberOfEntries(); i++) {
            ValueMember mem = (ValueMember) this.itsAttr.getMemberAt(i);
            result += "name: " + mem.getName() + "   value: " + mem.getExpr()
                    + "\n";
        }
        result += " }\n";
        return result;
    }

    public int getNumberOfIncomingArcs() {
        return 0;
    }

    public int getNumberOfOutgoingArcs() {
        return 0;
    }

    public int getNumberOfInOutArcs() {
        return 0;
    }

    /**
     * Checks if attributes change between this object and another.
     * @param otherObject the other graph object to compare with
     * @return true if attributes change, false otherwise
     */
    public boolean doesChangeAttr(GraphObject otherObject) {
        if (this.attrExists() && otherObject.attrExists()) {
            for (int i = 0; i < this.itsAttr.getNumberOfEntries(); i++) {
                ValueMember vm = (ValueMember) this.itsAttr.getMemberAt(i);
                ValueMember vm2 = ((ValueTuple) otherObject.getAttribute()).getEntryAt(vm.getName());
                if (vm2 != null
                        && vm.getDeclaration().getTypeName().equals(vm2.getDeclaration().getTypeName())
                        && vm.isSet()
                        && (!vm2.isSet() || !vm.getExprAsText().equals(vm2.getExprAsText()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if attribute member values are different.
     * @param otherObject the other graph object to compare with
     * @return true if values are different, false otherwise
     */
    public boolean isAttrMemValDifferent(GraphObject otherObject) {
        if (this.attrExists() && otherObject.attrExists()) {
            for (int i = 0; i < this.itsAttr.getNumberOfEntries(); i++) {
                ValueMember vm = (ValueMember) this.itsAttr.getMemberAt(i);
                ValueMember vm2 = ((ValueTuple) otherObject.getAttribute()).getEntryAt(vm.getName());
                if (vm2 != null
                        && vm.getDeclaration().getTypeName().equals(vm2.getDeclaration().getTypeName())
                        && vm.isSet() && vm2.isSet()
                        && !vm.getExprAsText().equals(vm2.getExprAsText())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if attribute member constant values are different.
     * @param otherObject the other graph object to compare with
     * @return true if constant values are different, false otherwise
     */
    public boolean isAttrMemConstantValDifferent(GraphObject otherObject) {
        if (this.attrExists() && otherObject.attrExists()) {
            for (int i = 0; i < this.itsAttr.getNumberOfEntries(); i++) {
                ValueMember vm = (ValueMember) this.itsAttr.getMemberAt(i);
                ValueMember vm2 = ((ValueTuple) otherObject.getAttribute()).getEntryAt(vm.getName());
                if (vm.isSet() && vm.getExpr().isConstant()) {
                    if (vm2 != null && vm2.isSet() && vm2.getExpr().isConstant()
                            && !vm.getExprAsText().equals(vm2.getExprAsText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if attribute member constant values are different between three objects.
     * @param first the first graph object to compare
     * @param second the second graph object to compare
     * @return true if constant values are different, false otherwise
     */
    public boolean isAttrMemConstantValDifferent(GraphObject first, GraphObject second) {
        if (this.attrExists() && first.attrExists() && second.attrExists()) {
            for (int i = 0; i < this.itsAttr.getNumberOfEntries(); i++) {
                ValueMember vm = (ValueMember) this.itsAttr.getMemberAt(i);
                ValueMember vm2 = ((ValueTuple) first.getAttribute()).getEntryAt(vm.getName());
                if (vm.isSet() && vm.getExpr().isConstant()) {
                    if (vm2 != null && vm2.isSet() && vm2.getExpr().isConstant()
                            && !vm.getExprAsText().equals(vm2.getExprAsText())) {
                        return true;
                    } else {
                        vm2 = ((ValueTuple) second.getAttribute()).getEntryAt(vm.getName());
                        if (vm2 != null && vm2.isSet() && vm2.getExpr().isConstant()
                                && !vm.getExprAsText().equals(vm2.getExprAsText())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public abstract boolean isArc();

    public abstract boolean isNode();

    /**
     * Compares this graph object to another for equality.
     * @param otherObject the graph object to compare with
     * @return true if objects are equal, false otherwise
     */
    public abstract boolean compareTo(GraphObject otherObject);

    /**
     * Writes this graph object to XML.
     * @param xmlHelper the XML helper to write with
     */
    public abstract void XwriteObject(XMLHelper xmlHelper);

    /**
     * Reads this graph object from XML.
     * @param xmlHelper the XML helper to read from
     */
    public abstract void XreadObject(XMLHelper xmlHelper);

    /**
     * Checks whether the attribute observer wants a persistent connection to
     * the given attribute.
     * @param attrTuple the attribute tuple to check
     * @return false (default implementation)
     */
    public boolean isPersistentFor(AttrTuple attrTuple) {
        return false;
    }
}
