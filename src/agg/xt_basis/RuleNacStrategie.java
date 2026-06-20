/**
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

import agg.attribute.AttrConditionTuple;
import agg.attribute.AttrContext;
import agg.attribute.AttrInstance;
import agg.attribute.AttrVariableTuple;
import agg.attribute.impl.CondMember;
import agg.attribute.impl.ValueMember;
import agg.attribute.impl.ValueTuple;
import agg.attribute.impl.VarMember;
import agg.cons.Evaluable;
import agg.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Strategy class for managing Negative Application Conditions (NAC) in a Rule.
 */
public class RuleNacStrategie {

    final protected List<OrdinaryMorphism> itsNACs = new ArrayList<>();

    private Rule itsRule;

    /**
     * Creates a new NAC strategy for the specified rule.
     *
     * @param rule the rule this strategy belongs to
     */
    public RuleNacStrategie(Rule rule) {
        this.itsRule = rule;
    }

    /**
     * Creates a new negative application condition (NAC) and adds it to this rule. Note: Because the new morphism is
     * initially empty and the LHS graph is not, it is not a morphism in theoretical terms, which demands a NAC to be a
     * total morphism.
     *
     * @return an empty morphism with the original set to this rule's left-hand side graph
     */
    public OrdinaryMorphism createNAC() {
        final OrdinaryMorphism negativeApplCond = new OrdinaryMorphism(
                getRule().getLeft(),
                BaseFactory.theFactory().createGraph(getRule().getRight().getTypeSet()),
                getRule().getRight().getAttrContext());
        this.itsNACs.add(negativeApplCond);
        AttrContext negativeApplCondContext = negativeApplCond.getAttrContext();
        negativeApplCond.getImage().setAttrContext(negativeApplCondContext);
        negativeApplCond.getImage().setKind(GraphKind.NAC);
        return negativeApplCond;
    }

    /**
     * Creates a new negative application condition (NAC) and adds it to this rule. The target graph of the new NAC is
     * constructed based on the RHS of this rule.
     *
     * @return a new NAC with target graph constructed from the RHS
     */
    public OrdinaryMorphism createNACDuetoRHS() {
        final OrdinaryMorphism negativeApplCond = createNAC();
        getRule().makeACDuetoRHS(negativeApplCond);
        return negativeApplCond;
    }

    /**
     * Adds the specified morphism representing a negative application condition (NAC).
     * <p>
     * <b>Precondition:</b> The NAC's original graph must be this rule's left-hand side graph.
     *
     * @param negativeApplCond the negative application condition morphism to add
     * @return true if the NAC was added successfully, false if it was already present
     */
    public boolean addNAC(final OrdinaryMorphism negativeApplCond) {
        return this.addNAC(-1, negativeApplCond);
    }

    /**
     * Adds the specified morphism representing a negative application condition (NAC) at the specified index in the
     * list.
     * <p>
     * <b>Precondition:</b> The NAC's original graph must be this rule's left-hand side graph.
     *
     * @param index the index at which to insert the NAC, or -1 to append to the end
     * @param negativeApplCond the negative application condition morphism to add
     * @return true if the NAC was added successfully, false if it was already present
     */
    public boolean addNAC(int index, final OrdinaryMorphism negativeApplCond) {
        if (!this.itsNACs.contains(negativeApplCond)) {
            negativeApplCond.getTarget().setKind(GraphKind.NAC);
            if (index >= 0 && index < this.itsNACs.size()) {
                this.itsNACs.add(index, negativeApplCond);
            } else {
                this.itsNACs.add(negativeApplCond);
            }
            getRule().changed = true;
            return true;
        }
        return false;
    }

    /**
     * Enables or disables all negative application conditions (NACs) of this rule.
     *
     * @param enable true to enable all NACs, false to disable them
     */
    public void enableNACs(boolean enable) {
        for (int index = 0; index < this.itsNACs.size(); index++) {
            this.itsNACs.get(index).setEnabled(enable);
        }
    }

    /**
     * Destroys the specified negative application condition and removes it from this rule. The target graph of the NAC
     * morphism is also disposed.
     *
     * @param negativeApplCond the negative application condition morphism to destroy
     */
    public void destroyNAC(OrdinaryMorphism negativeApplCond) {
        this.itsNACs.remove(negativeApplCond);
        negativeApplCond.getImage().dispose();
    }

    /**
     * Checks if this rule contains any negative application conditions.
     *
     * @return true if the rule has at least one NAC, false otherwise
     */
    public boolean hasNACs() {
        return !this.itsNACs.isEmpty();
    }

    /**
     * Checks if this rule has at least one enabled negative application condition.
     *
     * @return true if the rule has at least one enabled NAC, false otherwise
     */
    public boolean hasEnabledNACs() {
        for (OrdinaryMorphism negativeApplCond : this.itsNACs) {
            if (negativeApplCond.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator over all negative application conditions of this rule.
     *
     * @return an iterator of all NAC morphisms
     */
    public Iterator<OrdinaryMorphism> getNACs() {
        return this.itsNACs.iterator();
    }

    /**
     * Returns the list of all negative application condition morphisms of this rule.
     *
     * @return the list of NAC morphisms
     */
    public List<OrdinaryMorphism> getNACsList() {
        return this.itsNACs;
    }

    /**
     * Returns the negative application condition morphism with the specified name.
     *
     * @param name the name of the NAC to find
     * @return the NAC morphism with the specified name, or {@code null} if not found
     */
    public OrdinaryMorphism getNAC(String name) {
        for (int index = 0; index < this.itsNACs.size(); index++) {
            OrdinaryMorphism negativeApplCond = this.itsNACs.get(index);
            if (negativeApplCond.getName().equals(name)) {
                return negativeApplCond;
            }
        }
        return null;
    }

    /**
     * Returns the negative application condition morphism at the specified index.
     *
     * @param index the index of the NAC to retrieve
     * @return the NAC morphism at the specified index, or {@code null} if index is out of bounds
     */
    public OrdinaryMorphism getNAC(int index) {
        if (index >= 0 && index < this.itsNACs.size()) {
            return this.itsNACs.get(index);
        } else {
            return null;
        }
    }

    /**
     * Returns the negative application condition morphism with the specified target graph.
     *
     * @param graph the target graph to search for
     * @return the NAC morphism with the specified target graph, or {@code null} if not found
     */
    public OrdinaryMorphism getNAC(final Graph graph) {
        for (int index = 0; index < this.itsNACs.size(); index++) {
            OrdinaryMorphism applCond = this.itsNACs.get(index);
            if (applCond.getTarget() == graph) {
                return applCond;
            }
        }
        return null;
    }

    /**
     * Checks if the specified graph is the target graph of any negative application condition.
     *
     * @param graph the graph to check
     * @return true if the graph is a target of any NAC, false otherwise
     */
    public boolean hasNAC(final Graph graph) {
        for (int index = 0; index < this.itsNACs.size(); index++) {
            OrdinaryMorphism applCond = this.itsNACs.get(index);
            if (applCond.getTarget() == graph) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified negative application condition from this rule.
     *
     * @param negativeApplCond the negative application condition morphism to remove
     * @return false if the NAC was not found, true if it was removed successfully
     */
    public final boolean removeNAC(OrdinaryMorphism negativeApplCond) {
        return this.itsNACs.remove(negativeApplCond);
    }

    /**
     * Checks if all NACs in this rule are valid. Currently always returns true as it is not yet fully implemented.
     *
     * @return true if all NACs are valid, false otherwise
     */
    public boolean areNACsValid() {
        return true;
    }

    /**
     * Checks if the specified NAC is valid. Currently always returns true as it is not yet fully implemented.
     *
     * @param nac the negative application condition to validate
     * @return true if the NAC is valid, false otherwise
     */
    public boolean isNACValid(OrdinaryMorphism nac) {
        return true;
    }

    /**
     * Checks if the specified NAC is using the specified variable in the context of the
     * specified attribute condition tuple.
     *
     * @param var the variable member to check for usage
     * @param act the attribute condition tuple providing context
     * @return true if the NAC uses the variable in the given context, false otherwise
     */
    public boolean nacIsUsingVariable(
            final VarMember var,
            final AttrConditionTuple act) {
        for (int i = 0; i < this.itsNACs.size(); i++) {
            final OrdinaryMorphism nac = this.itsNACs.get(i);
            if (nac.getTarget().isUsingVariable(var)) {
                return true;
            }
            List<String> nacVars = nac.getTarget()
                    .getVariableNamesOfAttributes();
            for (int j = 0; j < nacVars.size(); j++) {
                String varName = nacVars.get(j);
                for (int k = 0; k < act.getNumberOfEntries(); k++) {
                    CondMember cond = (CondMember) act.getMemberAt(k);
                    List<String> condVars = cond.getAllVariables();
                    if (condVars.contains(varName)
                            && condVars.contains(var.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Clears all negative application conditions from this strategy.
     */
    public void clearNACs() {
        this.itsNACs.clear();
    }

    /**
     * Disposes all negative application conditions in this strategy.
     */
    public void disposeAllNACs() {
        while (!this.itsNACs.isEmpty()) {
            this.itsNACs.get(0).dispose(false, true);
            this.itsNACs.remove(0);
        }
        this.itsNACs.clear();
    }

    /**
     * Returns the list of all negative application condition morphisms.
     * Package-private for internal use by Rule class.
     *
     * @return the list of NAC morphisms
     */
    public List<OrdinaryMorphism> getNACsListInternal() {
        return this.itsNACs;
    }

    /**
     * Returns the rule this strategy belongs to.
     *
     * @return the rule
     */
    public Rule getRule() {
        return itsRule;
    }

    /**
     * Sets the rule this strategy belongs to.
     *
     * @param rule the rule
     */
    public void setRule(Rule rule) {
        this.itsRule = rule;
    }
}
