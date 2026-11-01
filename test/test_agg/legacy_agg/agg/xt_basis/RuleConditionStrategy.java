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
import agg.attribute.impl.CondMember;
import agg.attribute.impl.VarMember;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract base class for strategy classes managing application conditions (AC, NAC, PAC) in a Rule. Provides common
 * functionality for creating, managing, and validating application conditions.
 *
 */
public abstract class RuleConditionStrategy {

    final protected List<OrdinaryMorphism> itsACs = new ArrayList<>();
    private Rule itsRule;

    /**
     * Creates a new condition strategy for the specified rule.
     *
     * @param rule the rule this strategy belongs to
     */
    protected RuleConditionStrategy(Rule rule) {
        this.itsRule = rule;
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

    /**
     * Creates a new application condition morphism. Subclasses must implement this to create the appropriate type of
     * morphism with the correct GraphKind set.
     *
     * @return a new empty morphism with the original set to this rule's left-hand side graph
     */
    public abstract OrdinaryMorphism createAc();

    /**
     * Adds the specified application condition morphism.
     *
     * @param ac the application condition morphism to add
     * @return true if the AC was added successfully, false if it was already present
     */
    public boolean addAc(final OrdinaryMorphism ac) {
        return this.addAc(-1, ac);
    }

    /**
     * Adds the specified application condition morphism at the specified index in the list.
     *
     * @param index the index at which to insert the AC, or -1 to append to the end
     * @param ac the application condition morphism to add
     * @return true if the AC was added successfully, false if it was already present
     */
    public abstract boolean addAc(int index, OrdinaryMorphism ac);

    /**
     * Enables or disables all application conditions of this rule.
     *
     * @param enable true to enable all ACs, false to disable them
     */
    public void enableAcs(boolean enable) {
        for (int index = 0; index < this.itsACs.size(); index++) {
            this.itsACs.get(index).setEnabled(enable);
        }
    }

    /**
     * Destroys the specified application condition and removes it from this rule. The target graph of the AC morphism
     * is also disposed.
     *
     * @param ac the application condition morphism to destroy
     */
    public void destroyAc(OrdinaryMorphism ac) {
        this.itsACs.remove(ac);
        ac.getImage().dispose();
    }

    /**
     * Checks if this rule contains any application conditions.
     *
     * @return true if the rule has at least one AC, false otherwise
     */
    public boolean hasAcs() {
        return !this.itsACs.isEmpty();
    }

    /**
     * Checks if this rule has at least one enabled application condition.
     *
     * @return true if the rule has at least one enabled AC, false otherwise
     */
    public boolean hasEnabledAcs() {
        for (OrdinaryMorphism ac : this.itsACs) {
            if (ac.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator over all application conditions of this rule.
     *
     * @return an iterator of all AC morphisms
     */
    public Iterator<OrdinaryMorphism> getAcs() {
        return this.itsACs.iterator();
    }

    /**
     * Returns the list of all application condition morphisms of this rule.
     *
     * @return the list of AC morphisms
     */
    public List<OrdinaryMorphism> getAcsList() {
        return this.itsACs;
    }

    /**
     * Returns the application condition morphism with the specified name.
     *
     * @param name the name of the AC to find
     * @return the AC morphism with the specified name, or null if not found
     */
    public OrdinaryMorphism getAc(String name) {
        for (int index = 0; index < this.itsACs.size(); index++) {
            OrdinaryMorphism ac = this.itsACs.get(index);
            if (ac.getName().equals(name)) {
                return ac;
            }
        }
        return null;
    }

    /**
     * Returns the application condition morphism at the specified index.
     *
     * @param index the index of the AC to retrieve
     * @return the AC morphism at the specified index, or null if index is out of bounds
     */
    public OrdinaryMorphism getAc(int index) {
        if (index >= 0 && index < this.itsACs.size()) {
            return this.itsACs.get(index);
        } else {
            return null;
        }
    }

    /**
     * Returns the application condition morphism with the specified target graph.
     *
     * @param graph the target graph to search for
     * @return the AC morphism with the specified target graph, or null if not found
     */
    public OrdinaryMorphism getAc(final Graph graph) {
        for (int index = 0; index < this.itsACs.size(); index++) {
            OrdinaryMorphism ac = this.itsACs.get(index);
            if (ac.getTarget() == graph) {
                return ac;
            }
        }
        return null;
    }

    /**
     * Checks if the specified graph is the target graph of any application condition.
     *
     * @param graph the graph to check
     * @return true if the graph is a target of any AC, false otherwise
     */
    public boolean hasAc(final Graph graph) {
        for (int index = 0; index < this.itsACs.size(); index++) {
            OrdinaryMorphism ac = this.itsACs.get(index);
            if (ac.getTarget() == graph) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified application condition from this rule.
     *
     * @param ac the application condition morphism to remove
     * @return false if the AC was not found, true if it was removed successfully
     */
    public boolean removeAc(OrdinaryMorphism ac) {
        return this.itsACs.remove(ac);
    }

    /**
     * Checks if the specified application condition is valid. Subclasses must implement this according to their
     * specific validation rules.
     *
     * @param ac the application condition to validate
     * @return true if the AC is valid, false otherwise
     */
    public abstract boolean isAcValid(OrdinaryMorphism ac);

    /**
     * Checks if all application conditions in this rule are valid.
     *
     * @return true if all ACs are valid, false otherwise
     */
    public boolean areAcsValid() {
        for (int i = 0; i < this.itsACs.size(); i++) {
            OrdinaryMorphism ac = this.itsACs.get(i);
            if (!this.isAcValid(ac)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any application condition in this rule is using the specified variable in the context of the specified
     * attribute condition tuple.
     *
     * @param var the variable member to check for usage
     * @param act the attribute condition tuple providing context
     * @return true if any AC uses the variable in the given context, false otherwise
     */
    public boolean acIsUsingVariable(final VarMember var, final AttrConditionTuple act) {
        for (int i = 0; i < this.itsACs.size(); i++) {
            final OrdinaryMorphism ac = this.itsACs.get(i);
            if (ac.getTarget().isUsingVariable(var)) {
                return true;
            }
            List<String> acVars = ac.getTarget().getVariableNamesOfAttributes();
            for (int j = 0; j < acVars.size(); j++) {
                String varName = acVars.get(j);
                for (int k = 0; k < act.getNumberOfEntries(); k++) {
                    CondMember cond = (CondMember) act.getMemberAt(k);
                    List<String> condVars = cond.getAllVariables();
                    if (condVars.contains(varName) && condVars.contains(var.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Clears all application conditions from this strategy.
     */
    public void clearAcs() {
        this.itsACs.clear();
    }

    /**
     * Disposes all application conditions in this strategy.
     */
    public void disposeAllAcs() {
        while (!this.itsACs.isEmpty()) {
            this.itsACs.get(0).dispose(false, true);
            this.itsACs.remove(0);
        }
        this.itsACs.clear();
    }

    /**
     * Returns the list of all application condition morphisms. Package-private for internal use by Rule class.
     *
     * @return the list of AC morphisms
     */
    public List<OrdinaryMorphism> getAcsListInternal() {
        return this.itsACs;
    }

    /**
     * Creates a helper method to create a morphism with proper attribute context setup.
     *
     * @param kind the GraphKind to set on the target graph
     * @return a new OrdinaryMorphism configured for this rule
     */
    protected OrdinaryMorphism createMorphismWithContext(String kind) {
        final OrdinaryMorphism morphism = new OrdinaryMorphism(
                getRule().getLeft(),
                BaseFactory.theFactory().createGraph(getRule().getRight().getTypeSet()),
                getRule().getRight().getAttrContext());
        AttrContext context = morphism.getAttrContext();
        morphism.getImage().setAttrContext(context);
        morphism.getImage().setKind(kind);
        return morphism;
    }
}
