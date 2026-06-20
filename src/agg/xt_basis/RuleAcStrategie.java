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
import agg.attribute.AttrVariableTuple;
import agg.attribute.impl.CondMember;
import agg.attribute.impl.VarMember;
import agg.cons.Evaluable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Strategy class for managing Nested Application Conditions (AC/GAC) in a Rule.
 */
public class RuleAcStrategie {

    final protected List<OrdinaryMorphism> itsACs = new ArrayList<>();
    transient protected Map<GraphObject, GraphObject> changedPreserved;
    transient protected List<String> typesWhichNeedMultiplicityCheck;

    private Rule itsRule;

    /**
     * Creates a new AC strategy for the specified rule.
     *
     * @param rule the rule this strategy belongs to
     */
    public RuleAcStrategie(Rule rule) {
        this.itsRule = rule;
    }

    /**
     * Creates and adds a new nested application condition (GAC) to this rule. Note: Because the new morphism is
     * initially empty and the LHS graph is not, it is not a morphism in theoretical terms, which demands an application
     * condition to be a total morphism.
     *
     * @return an empty nested application condition with the original set to this rule's left-hand side graph
     */
    public NestedApplCond createNestedAc() {
        final NestedApplCond applicationCondition = new NestedApplCond(
                getRule().getLeft(),
                BaseFactory.theFactory().createGraph(getRule().getRight().getTypeSet()),
                getRule().getRight().getAttrContext());
        this.itsACs.add(applicationCondition);
        AttrContext acContext = applicationCondition.getAttrContext();
        applicationCondition.getImage().setAttrContext(acContext);
        applicationCondition.getImage().setKind(GraphKind.AC);
        return applicationCondition;
    }

    /**
     * Creates and adds a new nested application condition (GAC) to this rule. The target graph of the new GAC is
     * constructed based on the RHS of this rule.
     *
     * @return a new nested application condition with target graph constructed from the RHS
     */
    public NestedApplCond createNestedAcDuetoRHS() {
        final NestedApplCond nac = createNestedAc();
        getRule().makeACDuetoRHS(nac);
        return nac;
    }

    /**
     * Adds the specified morphism representing a nested application condition.
     * <p>
     * <b>Precondition:</b> The AC's original graph must be this rule's left-hand side graph.
     *
     * @param ac the nested application condition morphism to add
     * @return true if the AC was added successfully, false if it was already present
     */
    public boolean addNestedAc(final OrdinaryMorphism ac) {
        return this.addNestedAc(-1, ac);
    }

    /**
     * Adds the specified morphism representing a nested application condition at the specified index in the list.
     * <p>
     * <b>Precondition:</b> The AC's original graph must be this rule's left-hand side graph.
     *
     * @param indx the index at which to insert the AC, or -1 to append to the end
     * @param ac the nested application condition morphism to add
     * @return true if the AC was added successfully, false if it was already present
     */
    public boolean addNestedAc(int indx, final OrdinaryMorphism ac) {
        if (!this.itsACs.contains(ac)) {
            ac.getTarget().setKind(GraphKind.AC);
            if (indx >= 0 && indx < this.itsACs.size()) {
                this.itsACs.add(indx, ac);
            } else {
                this.itsACs.add(ac);
            }
            getRule().changed = true;
            return true;
        }
        return false;
    }

    /**
     * Enables or disables all nested application conditions (ACs) of this rule.
     *
     * @param enable true to enable all ACs, false to disable them
     */
    public void enableNestedAc(boolean enable) {
        for (int index = 0; index < this.itsACs.size(); index++) {
            this.itsACs.get(index).setEnabled(enable);
        }
    }

    /**
     * Destroys the specified nested application condition and removes it from this rule. The target graph of the AC
     * morphism is also disposed.
     *
     * @param ac the nested application condition morphism to destroy
     */
    public void destroyNestedAc(final OrdinaryMorphism ac) {
        this.itsACs.remove(ac);
        ac.getImage().dispose();
    }

    /**
     * Checks if this rule contains any nested application conditions.
     *
     * @return true if the rule has at least one nested AC, false otherwise
     */
    public boolean hasNestedAcs() {
        return !this.itsACs.isEmpty();
    }

    /**
     * Returns an iterator over all nested application conditions of this rule.
     *
     * @return an iterator of all nested AC morphisms
     */
    public Iterator<OrdinaryMorphism> getNestedAcs() {
        return this.itsACs.iterator();
    }

    /**
     * Returns a list of all enabled nested application conditions of this rule.
     *
     * @return a list of all enabled nested AC morphisms
     */
    public List<NestedApplCond> getEnabledAcs() {
        List<NestedApplCond> nestedApplCondList = new ArrayList<>(this.itsACs.size());
        for (int index = 0; index < this.itsACs.size(); index++) {
            NestedApplCond nestedApplCond = (NestedApplCond) this.itsACs.get(index);
            if (nestedApplCond.isEnabled()) {
                nestedApplCondList.add(nestedApplCond);
            }
        }
        return nestedApplCondList;
    }

    /**
     * Returns the list of all nested application condition morphisms of this rule.
     *
     * @return the list of nested AC morphisms
     */
    public List<OrdinaryMorphism> getNestedAcsList() {
        return this.itsACs;
    }

    /**
     * Returns a list of all enabled nested application conditions as evaluable objects.
     *
     * @return a list of all enabled nested AC morphisms as evaluable objects
     */
    public List<Evaluable> getEnabledGeneralAcsAsEvaluable() {
        List<Evaluable> evaluableList = new ArrayList<>(this.itsACs.size());
        for (int index = 0; index < this.itsACs.size(); index++) {
            NestedApplCond nestedApplCond = (NestedApplCond) this.itsACs.get(index);
            if (nestedApplCond.isEnabled()) {
                evaluableList.add(nestedApplCond);
            }
        }
        return evaluableList;
    }

    /**
     * Returns the nested application condition morphism with the specified name.
     *
     * @param name the name of the nested AC to find
     * @return the nested AC morphism with the specified name, or null if not found
     */
    public OrdinaryMorphism getNestedAc(String name) {
        for (int index = 0; index < this.itsACs.size(); index++) {
            OrdinaryMorphism nestedApplCond = this.itsACs.get(index);
            if (nestedApplCond.getName().equals(name)) {
                return nestedApplCond;
            }
        }
        return null;
    }

    /**
     * Returns the nested application condition morphism at the specified index.
     *
     * @param index the index of the nested AC to retrieve
     * @return the nested AC morphism at the specified index, or null if index is out of bounds
     */
    public OrdinaryMorphism getNestedAc(int index) {
        if (index >= 0 && index < this.itsACs.size()) {
            return this.itsACs.get(index);
        } else {
            return null;
        }
    }

    /**
     * Removes the specified nested application condition from this rule. If the AC was enabled, this method also
     * updates the formula by patching out the evaluable and refreshing the formula.
     *
     * @param applicationCondition the nested application condition morphism to remove
     * @return false if the AC was not found, true if it was removed successfully
     */
    public final boolean removeNestedAc(OrdinaryMorphism applicationCondition) {
        boolean enAC = applicationCondition.isEnabled();
        if (this.itsACs.remove(applicationCondition)) {
            if (enAC) {
                getRule().itsFormula.patchOutEvaluable((NestedApplCond) applicationCondition, true);
                getRule().refreshFormula(new ArrayList<>(this.getEnabledAcs()));
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if any nested application condition in this rule is using the specified variable in the context of the
     * specified attribute condition tuple.
     *
     * @param var the variable member to check for usage
     * @param act the attribute condition tuple providing context
     * @return true if any nested AC uses the variable in the given context, false otherwise
     */
    public boolean nestedAcIsUsingVariable(
            final VarMember var,
            final AttrConditionTuple act) {
        for (int i = 0; i < this.itsACs.size(); i++) {
            final OrdinaryMorphism applicationCondition = this.itsACs.get(i);
            if (applicationCondition.getTarget().isUsingVariable(var)) {
                return true;
            }
            List<String> acVars = applicationCondition.getTarget()
                    .getVariableNamesOfAttributes();
            for (int j = 0; j < acVars.size(); j++) {
                String varName = acVars.get(j);
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
     * Disposes the results of all nested application conditions. This is useful for freeing memory when the results are
     * no longer needed.
     */
    public void disposeResultsOfNestedAcs() {
        for (int index = 0; index < this.itsACs.size(); index++) {
            NestedApplCond nestedApplCond = (NestedApplCond) this.itsACs.get(index);
            nestedApplCond.disposeResults();
        }
    }

    /**
     * Checks dangling edges of the given nested application condition. Returns true if no dangling edge exists,
     * otherwise false.
     *
     * @param ac the nested application condition to validate
     * @return true if the GAC has no dangling edges, false otherwise
     */
    public boolean isAcValid(NestedApplCond ac) {
        if (ac.isEnabled()) {
            return ac.isValid();
        }
        return true;
    }

    /**
     * Checks dangling edges of all nested application conditions in this rule. Returns true if no dangling edge exists,
     * otherwise false.
     *
     * @return true if all GACs have no dangling edges, false otherwise
     */
    public boolean areAcsValid() {
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond applicationCondition = (NestedApplCond) this.itsACs.get(i);
            if (!this.isAcValid(applicationCondition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Mark used variables of nested ACs.
     *
     * @param nestedACs the list of nested ACs
     * @param avt the attribute variable tuple
     */
    public void markUsedVarsOfNestedAcs(List<?> nestedACs, AttrVariableTuple avt) {
        for (int i = 0; i < nestedACs.size(); i++) {
            OrdinaryMorphism nestAC = (OrdinaryMorphism) nestedACs.get(i);
            Graph g = nestAC.getImage();
            markUsedVars(g.getNodesSet().iterator(),
                    g.getArcsSet().iterator(),
                    avt, VarMember.PAC);
            markUsedVarsOfNestedAcs(((NestedApplCond) nestAC).getNestedACs(), avt);
        }
    }

    private void markUsedVars(
            final Iterator<Node> nodes,
            final Iterator<Arc> arcs,
            final AttrVariableTuple avt,
            final int mark) {
        getRule().markUsedVars(nodes, arcs, avt, mark);
    }

    /**
     * Clears all nested application conditions from this strategy.
     */
    public void clearNestedAcs() {
        this.itsACs.clear();
    }

    /**
     * Disposes all nested application conditions in this strategy.
     */
    public void disposeAllNestedAcs() {
        while (!this.itsACs.isEmpty()) {
            this.itsACs.get(0).dispose(false, true);
            this.itsACs.remove(0);
        }
        this.itsACs.clear();
    }

    /**
     * Returns the list of all nested application condition morphisms. Package-private for internal use by Rule class.
     *
     * @return the list of nested AC morphisms
     */
    public List<OrdinaryMorphism> getAcsListInternal() {
        return this.itsACs;
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
