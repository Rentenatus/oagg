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
 * GraphObject defines the common interface and implementations for Nodes and Arcs.
 * This abstract class provides the fundamental operations and attributes that are
 * shared across all graph elements.
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

    /**
     * Returns the input vector associated with this graph object.
     *
     * @return the input vector, or null if not set
     */
    public NNVector getInputVector() {
        return inputVector;
    }

    /**
     * Sets the input vector for this graph object.
     *
     * @param inputVector the input vector to set
     */
    public void setInputVector(NNVector inputVector) {
        this.inputVector = inputVector;
    }

    /**
     * Creates and returns a copy of the input vector.
     *
     * @return a copy of the input vector, or null if the input vector is null
     */
    public NNVector copyInputVector() {
        return inputVector == null ? null : inputVector.copyNNVector();
    }

    /**
     * Creates an attribute instance for this graph object if the type has an attribute type defined.
     * This method also registers this object as an observer of the attribute instance.
     */
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

    /**
     * Disposes the attribute instance of this graph object.
     * Removes this object as an observer and disposes the attribute tuple.
     */
    public void disposeAttributeInstance() {
        if (this.itsAttr != null) {
            this.itsAttr.removeObserver(this);
            ((ValueTuple) this.itsAttr).dispose();
            this.itsAttr = null;
        }
    }

    /**
     * Sets the name of this graph object.
     *
     * @param name the name to set, can be null (will be converted to empty string)
     */
    public final void setObjectName(final String name) {
        this.name = (name != null) ? name : "";
    }

    /**
     * Returns the name of this graph object.
     *
     * @return the name of this object
     */
    public final String getObjectName() {
        return this.name;
    }

    /**
     * Returns the context usage identifier for this graph object.
     *
     * @return the context usage identifier
     */
    public final int getContextUsage() {
        return this.itsContextUsage;
    }

    /**
     * Sets the context usage identifier for this graph object.
     *
     * @param contextUsage the context usage identifier to set
     */
    public final void setContextUsage(int contextUsage) {
        this.itsContextUsage = contextUsage;
    }

    /**
     * Returns the graph context this object belongs to.
     *
     * @return the graph context
     */
    public final Graph getContext() {
        return this.itsContext;
    }

    /**
     * Sets whether this graph object is critical.
     *
     * @param critical true if this object is critical, false otherwise
     */
    public final void setCritical(boolean critical) {
        this.critical = critical;
    }

    /**
     * Checks if this graph object is critical.
     *
     * @return true if this object is critical, false otherwise
     */
    public final boolean isCritical() {
        return this.critical;
    }

    /**
     * Sets whether this graph object is visible.
     *
     * @param visible true if this object is visible, false otherwise
     */
    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Checks if this graph object is visible.
     *
     * @return true if this object is visible, false otherwise
     */
    public final boolean isVisible() {
        return this.visible;
    }

    /**
     * Returns the type of this graph object.
     *
     * @return the type of this object
     */
    public final Type getType() {
        return this.itsType;
    }

    /**
     * Sets the type of this graph object.
     *
     * @param type the type to set
     */
    public final void setType(Type type) {
        this.itsType = type;
    }

    /**
     * Converts this object's type to a type key string that can be used for search
     * operations. For a node it is similar to
     * <code>((Node) this).getType().convertToKey()</code>, for an edge to
     * <code>((Arc) this).getSource().getType().convertToKey()
     * + ((Arc) this).getType().convertToKey()
     * + ((Arc) this).getTarget().getType().convertToKey()</code>
     *
     * @return a key string representation of this object's type
     */
    public abstract String convertToKey();

    /**
     * Resets and returns the type key for this graph object.
     *
     * @return the reset type key
     */
    public abstract String resetTypeKey();

    /**
     * Returns the attribute instance of this graph object.
     *
     * @return the attribute instance, or null if not set
     */
    public final AttrInstance getAttribute() {
        return this.itsAttr;
    }

    /**
     * Checks if this graph object has attributes.
     *
     * @return true if attributes exist and have entries, false otherwise
     */
    public final boolean attrExists() {
        return this.itsAttr != null && this.itsAttr.getNumberOfEntries() > 0;
    }

    /**
     * Returns the number of attributes for this graph object.
     *
     * @return the number of attribute entries, or 0 if no attributes exist
     */
    public final int getNumberOfAttributes() {
        return this.itsAttr == null ? 0 : this.itsAttr.getNumberOfEntries();
    }

    /**
     * Returns the names of all variable attributes for this graph object.
     *
     * @return a list of variable names, or an empty list if no attributes exist
     */
    public final List<String> getVariableNamesOfAttribute() {
        return this.itsAttr == null ? new ArrayList<String>(0)
                : ((ValueTuple) this.itsAttr).getAllVariableNames();
    }

    /**
     * Copies attributes from another graph object to this object.
     *
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
     * Called when an attribute changes. Implements the AttrObserver interface
     * and propagates attribute changes.
     *
     * @param ev the attribute event that occurred
     */
    @Override
    public void attributeChanged(AttrEvent ev) {
        if (inputVector != null && inputVector.nullOutAtAttrChange()) {
            inputVector = null;
        }
    }

    /**
     * Returns a string representation of the attributes of this graph object.
     *
     * @return a string containing the attribute names and values
     */
    public String attributeToString() {
        String result = "\nAttributes of : " + getType().getName() + " {\n";
        for (int i = 0; i < this.itsAttr.getNumberOfEntries(); i++) {
            ValueMember mem = (ValueMember) this.itsAttr.getMemberAt(i);
            result += "name: " + mem.getName() + "   value: " + mem.getExpr() + "\n";
        }
        result += " }\n";
        return result;
    }

    /**
     * Returns the number of incoming arcs for this graph object.
     * Default implementation returns 0, subclasses should override.
     *
     * @return the number of incoming arcs
     */
    public int getNumberOfIncomingArcs() {
        return 0;
    }

    /**
     * Returns the number of outgoing arcs for this graph object.
     * Default implementation returns 0, subclasses should override.
     *
     * @return the number of outgoing arcs
     */
    public int getNumberOfOutgoingArcs() {
        return 0;
    }

    /**
     * Returns the total number of incoming and outgoing arcs for this graph object.
     * Default implementation returns 0, subclasses should override.
     *
     * @return the total number of arcs
     */
    public int getNumberOfInOutArcs() {
        return 0;
    }

    /**
     * Checks if attributes have changed between this object and another.
     *
     * @param otherObject the other graph object to compare with
     * @return true if attributes have changed, false otherwise
     */
    public boolean doesChangeAttr(GraphObject otherObject) {
        if (this.attrExists() && otherObject.attrExists()) {
            for (int i = 0; i < this.itsAttr.getNumberOfEntries(); i++) {
                ValueMember vm = (ValueMember) this.itsAttr.getMemberAt(i);
                ValueMember vm2 = ((ValueTuple) otherObject.getAttribute()).getEntryAt(
                        vm.getName());
                if (vm2 != null
                        && vm.getDeclaration().getTypeName().equals(
                                vm2.getDeclaration().getTypeName())
                        && vm.isSet()
                        && (!vm2.isSet() || !vm.getExprAsText().equals(vm2.getExprAsText()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if attribute member values are different between this object and another.
     *
     * @param otherObject the other graph object to compare with
     * @return true if attribute member values are different, false otherwise
     */
    public boolean isAttrMemValDifferent(GraphObject otherObject) {
        if (this.attrExists() && otherObject.attrExists()) {
            for (int i = 0; i < this.itsAttr.getNumberOfEntries(); i++) {
                ValueMember vm = (ValueMember) this.itsAttr.getMemberAt(i);
                ValueMember vm2 = ((ValueTuple) otherObject.getAttribute()).getEntryAt(
                        vm.getName());
                if (vm2 != null
                        && vm.getDeclaration().getTypeName().equals(
                                vm2.getDeclaration().getTypeName())
                        && vm.isSet() && vm2.isSet()
                        && !vm.getExprAsText().equals(vm2.getExprAsText())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if attribute member constant values are different between this object and another.
     *
     * @param otherObject the other graph object to compare with
     * @return true if attribute member constant values are different, false otherwise
     */
    public boolean isAttrMemConstantValDifferent(GraphObject otherObject) {
        if (this.attrExists() && otherObject.attrExists()) {
            for (int i = 0; i < this.itsAttr.getNumberOfEntries(); i++) {
                ValueMember vm = (ValueMember) this.itsAttr.getMemberAt(i);
                ValueMember vm2 = ((ValueTuple) otherObject.getAttribute()).getEntryAt(
                        vm.getName());
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
     * Checks if attribute member constant values are different among three graph objects.
     *
     * @param first the first graph object to compare
     * @param second the second graph object to compare
     * @return true if attribute member constant values are different, false otherwise
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

    /**
     * Checks if this graph object is an arc.
     *
     * @return true if this is an arc, false otherwise
     */
    public abstract boolean isArc();

    /**
     * Checks if this graph object is a node.
     *
     * @return true if this is a node, false otherwise
     */
    public abstract boolean isNode();

    /**
     * Compares this graph object to another for equality.
     *
     * @param otherObject the graph object to compare with
     * @return true if the objects are equal, false otherwise
     */
    public abstract boolean compareTo(GraphObject otherObject);

    /**
     * Writes this graph object to XML.
     *
     * @param xmlHelper the XML helper to write with
     */
    public abstract void XwriteObject(XMLHelper xmlHelper);

    /**
     * Reads this graph object from XML.
     *
     * @param xmlHelper the XML helper to read from
     */
    public abstract void XreadObject(XMLHelper xmlHelper);

    /**
     * Checks whether the attribute observer wants a persistent connection to
     * the given attribute tuple.
     *
     * @param attrTuple the attribute tuple to check
     * @return false (default implementation always returns false)
     */
    public boolean isPersistentFor(AttrTuple attrTuple) {
        return false;
    }
}
