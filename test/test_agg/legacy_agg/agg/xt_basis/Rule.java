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
import agg.attribute.AttrMapping;
import agg.attribute.AttrVariableTuple;
import agg.attribute.impl.AttrTupleManager;
import agg.attribute.impl.CondMember;
import agg.attribute.impl.CondTuple;
import agg.attribute.impl.DeclMember;
import agg.attribute.impl.DeclTuple;
import agg.attribute.impl.ValueMember;
import agg.attribute.impl.ValueTuple;
import agg.attribute.impl.VarMember;
import agg.attribute.impl.VarTuple;
import agg.cons.AtomApplCond;
import agg.cons.AtomConstraint;
import agg.cons.Convert;
import agg.cons.EvalSet;
import agg.cons.Evaluable;
import agg.cons.Formula;
import agg.util.Pair;
import agg.util.XMLHelper;
import agg.util.XMLObject;
import agg.xt_basis.agt.RuleScheme;
import agg.xt_basis.csp.CompletionPropertyBits;
import de.jare.ndimcol.ref.ArrayMovie;
import de.jare.ndimcol.primint.ArrayMovieInt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a graph transformation rule in the AGG system. A rule consists of a left-hand side (LHS) graph and a
 * right-hand side (RHS) graph, and defines how a subgraph matching the LHS can be transformed into a subgraph matching
 * the RHS.
 *
 * <p>
 * AGG implements the DPO (Double Pushout) approach by enabling the dangling-edge condition by default. Disabling the
 * dangling condition allows AGG to simulate the SPO (Single Pushout) approach.
 *
 * <p>
 * A rule can have application conditions (ACs), negative application conditions (NACs), and positive application
 * conditions (PACs) that constrain when and how the rule can be applied.
 *
 * @see OrdinaryMorphism
 * @see Graph
 * @see Match
 */
public class Rule extends OrdinaryMorphism implements XMLObject {

    /**
     * Accepts a visitor for this rule as part of the Visitor design pattern.
     *
     * @param <T> the return type of the visitor
     * @param visitor the visitor to accept
     * @return the result of visiting this rule
     */
    public <T> T accept(RuleVisitor<T> visitor) {
        return visitor.visit(this);
    }

    protected Formula itsFormula = new Formula(true);
    protected String formStr = "true";
    protected String formReadStr = "true";
    protected RuleAcStrategie acStrategie;
    protected RuleNacStrategie nacStrategie;
    protected RulePacStrategie pacStrategie;
    // containers for PostApplicationConditions
    transient protected boolean generatePostConstraints;
    protected List<AtomConstraint> itsUsedAtomics;
    protected List<Formula> itsUsedFormulas;
    transient protected List<String> constraintNameSet;
    transient protected List<Formula> constraints;
    transient protected List<EvalSet> atom_conditions;
    transient protected boolean applicable;
    protected boolean parallelMatching;
    protected boolean randomCSPDomain;
    protected boolean startParallelMatchByFirstCSPVar;
    protected int layer;
    protected int priority;
    protected boolean triggerOfLayer;
    transient protected boolean isReady;
    transient protected boolean isDeleting, isNodeDeleting, isCreating, isChanging,
            hasEnabledGACs;
    transient protected List<GraphObject> preserved;
    transient protected List<GraphObject> created;
    transient protected List<GraphObject> deleted;
    transient protected List<GraphObject> forbiden;
    transient protected Map<GraphObject, GraphObject> changedPreserved;
    transient protected List<String> typesWhichNeedMultiplicityCheck;
    protected Map<Node, Type> abstractType2childType;
    protected Match itsMatch;
    protected boolean notApplicable, waitBeforeApply;
    private InverseRuleConstructData invConstruct;

    /**
     * Creates a new rule with default left and right graphs. Initializes the rule with default names for the graphs and
     * sets up the attribute context.
     */
    protected Rule() {
        super();
        this.itsName = "Rule";
        this.itsOrig.setName("Left");
        this.itsOrig.setKind(GraphKind.LHS);
        this.itsImag.setName("Right");
        this.itsImag.setKind(GraphKind.RHS);
        this.itsAttrContext = this.itsAttrManager.newContext(AttrMapping.PLAIN_MAP);
        this.itsOrig.setAttrContext(this.itsAttrManager.newLeftContext(this.itsAttrContext));
        this.itsImag.setAttrContext(this.itsAttrManager.newRightContext(this.itsAttrContext));
        this.generatePostConstraints = true;
        this.applicable = true;
        this.acStrategie = new RuleAcStrategie(this);
        this.nacStrategie = new RuleNacStrategie(this);
        this.pacStrategie = new RulePacStrategie(this);
    }

    /**
     * Creates a new rule with the specified type set. Creates new left and right graphs using the given type set and
     * initializes the rule.
     *
     * @param types the type set to use for the left and right graphs
     */
    protected Rule(TypeSet types) {
        super(BaseFactory.theFactory().createGraph(types),
                BaseFactory.theFactory().createGraph(types));
        this.itsName = "Rule";
        this.itsOrig.setName("Left");
        this.itsOrig.setKind(GraphKind.LHS);
        this.itsImag.setName("Right");
        this.itsImag.setKind(GraphKind.RHS);
        this.itsAttrContext = this.itsAttrManager.newContext(AttrMapping.PLAIN_MAP);
        this.itsOrig.setAttrContext(this.itsAttrManager.newLeftContext(this.itsAttrContext));
        this.itsImag.setAttrContext(this.itsAttrManager.newRightContext(this.itsAttrContext));
        this.generatePostConstraints = true;
        this.applicable = true;
        this.acStrategie = new RuleAcStrategie(this);
        this.nacStrategie = new RuleNacStrategie(this);
        this.pacStrategie = new RulePacStrategie(this);
    }

    /**
     * Creates a new rule with the specified left and right graphs.
     *
     * @param left the left-hand side graph of this rule
     * @param right the right-hand side graph of this rule
     */
    protected Rule(Graph left, Graph right) {
        super(left, right);
        this.itsName = "Rule";
        this.itsOrig.setName("Left");
        this.itsOrig.setKind(GraphKind.LHS);
        this.itsImag.setName("Right");
        this.itsImag.setKind(GraphKind.RHS);
        this.itsAttrContext = this.itsAttrManager.newContext(AttrMapping.PLAIN_MAP);
        this.itsOrig.setAttrContext(this.itsAttrManager.newLeftContext(this.itsAttrContext));
        this.itsImag.setAttrContext(this.itsAttrManager.newRightContext(this.itsAttrContext));
        this.generatePostConstraints = true;
        this.applicable = true;
        this.acStrategie = new RuleAcStrategie(this);
        this.nacStrategie = new RuleNacStrategie(this);
        this.pacStrategie = new RulePacStrategie(this);
    }

    /**
     * Creates a new rule with the specified left graph, right graph, and attribute context.
     *
     * @param left the left-hand side graph of this rule
     * @param right the right-hand side graph of this rule
     * @param cont the attribute context to use for this rule
     */
    protected Rule(Graph left, Graph right, AttrContext cont) {
        super(left, right, cont);
        this.itsName = "Rule";
        this.itsOrig.setName("Left");
        this.itsOrig.setKind(GraphKind.LHS);
        this.itsImag.setName("Right");
        this.itsImag.setKind(GraphKind.RHS);
        this.itsAttrContext = cont;
        this.itsOrig.setAttrContext(this.itsAttrManager.newLeftContext(cont));
        this.itsImag.setAttrContext(this.itsAttrManager.newRightContext(cont));
        this.generatePostConstraints = true;
        this.applicable = true;
        this.acStrategie = new RuleAcStrategie(this);
        this.nacStrategie = new RuleNacStrategie(this);
        this.pacStrategie = new RulePacStrategie(this);
    }

    /**
     * Disposes the superclass resources and cleans up rule-specific references. This method should be called when the
     * rule is no longer needed to free resources. It releases the current match and multiplicity check references, and
     * resets the changed flag.
     */
    public void disposeSuper() {
        super.dispose();
        this.itsMatch = null;
        this.typesWhichNeedMultiplicityCheck = null;
        this.changed = false;
    }

    /**
     * Disposes this rule and all its associated resources. This includes:
     * <ul>
     * <li>Disposing all NACs (Negative Application Conditions)</li>
     * <li>Disposing all PACs (Positive Application Conditions)</li>
     * <li>Disposing all ACs (Nested Application Conditions)</li>
     * <li>Disposing the inverse rule construction data</li>
     * <li>Disposing the left-hand side and right-hand side graphs</li>
     * </ul>
     * After disposal, the rule can no longer be used.
     */
    public void dispose() {
        super.dispose();
        this.nacStrategie.disposeAllAcs();
        this.pacStrategie.disposeAllAcs();
        this.acStrategie.disposeAllAcs();
        this.disposeInverseConstruct();
        this.itsOrig.dispose();
        this.itsImag.dispose();
        this.itsMatch = null;
        this.typesWhichNeedMultiplicityCheck = null;
        this.changed = false;
    }

    /**
     * Sets the name of this rule and updates the associated formula name.
     *
     * @param n the new name for this rule
     */
    public void setName(String n) {
        this.itsName = n;
        this.itsFormula.setName("Formula.".concat(n));
    }

    /**
     * Returns the qualified name of this rule.
     *
     * @return the name of this rule
     */
    public String getQualifiedName() {
        return super.getName();
    }

    /**
     * Checks if this rule or any of its components (LHS, RHS) has been modified.
     *
     * @return true if this rule or any of its graphs has changed, false otherwise
     */
    public boolean hasChanged() {
        return this.changed
                || this.itsOrig.hasChanged()
                || this.itsImag.hasChanged();
    }

    /**
     * Disposes the current match associated with this rule.
     */
    private void disposeMatch() {
        if (this.itsMatch != null) {
            this.itsMatch.dispose();
            this.itsMatch = null;
        }
    }

    /**
     * Clears this rule by removing all application conditions and resetting the graphs. This method disposes all NACs,
     * PACs, and nested ACs, clears the superclass morphism, and resets the left-hand side and right-hand side graphs to
     * their initial empty state. The change flag is also reset.
     */
    public void clearRule() {
        disposeMatch();
        this.nacStrategie.disposeAllAcs();
        this.pacStrategie.disposeAllAcs();
        this.acStrategie.disposeAllAcs();
        super.clear();
        this.changed = false;
        this.itsOrig.clear();
        this.itsImag.clear();
        if (this.typesWhichNeedMultiplicityCheck != null) {
            this.typesWhichNeedMultiplicityCheck.clear();
            this.typesWhichNeedMultiplicityCheck = null;
        }
    }

    /**
     * Disposes the results of all nested application conditions. This is useful for freeing memory when the results are
     * no longer needed.
     */
    public void disposeResultsOfNestedACs() {
        this.acStrategie.disposeResultsOfNestedAcs();
    }

    /**
     * Checks if the specified graph is part of this rule. A graph is considered part of this rule if it is the
     * left-hand side, right-hand side, or the target of any NAC, PAC, or nested AC morphism.
     *
     * @param graph the graph to check
     * @return true if the graph is part of this rule, false otherwise
     */
    public boolean isElement(Graph graph) {
        if (this.itsOrig == graph || this.itsImag == graph) {
            return true;
        }
        for (OrdinaryMorphism ordMorph : this.nacStrategie.getAcsListInternal()) {
            if (ordMorph.getTarget() == graph) {
                return true;
            }
        }
        for (OrdinaryMorphism ordMorph : this.pacStrategie.getAcsListInternal()) {
            if (ordMorph.getTarget() == graph) {
                return true;
            }
        }
        for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
            if (ordMorph.getTarget() == graph) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the left-hand side graph of this rule.
     *
     * @return the left-hand side graph (LHS)
     */
    public final Graph getLeft() {
        return this.itsOrig;
    }

    /**
     * Returns the right-hand side graph of this rule.
     *
     * @return the right-hand side graph (RHS)
     */
    public final Graph getRight() {
        return this.itsImag;
    }

    /**
     * Checks if this rule is explicitly marked as not applicable.
     *
     * @return true if this rule is marked as not applicable, false otherwise
     */
    public boolean isNotApplicable() {
        return this.notApplicable;
    }

    /**
     * Checks if this rule is currently applicable. A rule is applicable if it is not explicitly marked as not
     * applicable and the internal applicable flag is set to true.
     *
     * @return true if this rule is applicable, false otherwise
     */
    public boolean isApplicable() {
        return !this.notApplicable && this.applicable;
    }

    /**
     * Checks whether this rule is applicable at the specified graph using the specified matching strategy.
     *
     * <p>
     * <b>Precondition:</b> {@link #isReadyToTransform()} should be called before invoking this method to ensure the
     * rule is ready for transformation.
     *
     * @param g the graph to check for applicability
     * @param strategy the matching completion strategy to use
     * @return true if this rule can be applied to the graph, false otherwise
     * @see #isApplicable(Graph, MorphCompletionStrategy, boolean)
     * @see #isReadyToTransform()
     */
    public boolean isApplicable(Graph g, MorphCompletionStrategy strategy) {
        return isApplicable(g, strategy, false);
    }

    /**
     * Checks whether this rule is applicable at the specified graph using the specified matching strategy. This method
     * optionally checks if the rule is ready to transform before checking applicability.
     *
     * @param g the graph to check for applicability
     * @param strategy the matching completion strategy to use
     * @param doCheckIfReadyToTransform if true, checks {@link #isReadyToTransform()} before checking applicability
     * @return true if this rule can be applied to the graph, false otherwise
     * @see #isApplicable(Graph, MorphCompletionStrategy)
     * @see #isReadyToTransform()
     */
    public boolean isApplicable(
            final Graph g,
            final MorphCompletionStrategy strategy,
            final boolean doCheckIfReadyToTransform) {
        boolean result = this.enabled;
        if (result && doCheckIfReadyToTransform) {
            result = this.isReadyToTransform();
        }
        if (result) {
            result = false;
            Match m = BaseFactory.theFactory().createMatch(this, g);
            if (m != null) {
                m.setCompletionStrategy(strategy, true);
                m.enableInputParameter(false);
                if (m.nextCompletion()) {
                    result = true;
                }
                m.dispose();
            }
        }
        return result;
    }

    /**
     * Enables or disables all input parameters in this rule's attribute context. Input parameters are variables that
     * can be set from outside the rule to influence the rule's behavior during application.
     *
     * @param enable true to enable input parameters, false to disable them
     */
    public void enableInputParameter(final boolean enable) {
        VarTuple vars = (VarTuple) this.getAttrContext().getVariables();
        for (int i = 0; i < vars.getNumberOfEntries(); i++) {
            VarMember vm = vars.getVarMemberAt(i);
            if (vm.isInputParameter()) {
                vm.setEnabled(enable);
                enableAttrConditionWithInputParameter(vm.getName(), enable);
            }
        }
    }

    /**
     * Enables or disables all attribute conditions that reference the specified input parameter.
     *
     * @param ipName the name of the input parameter
     * @param enable true to enable conditions, false to disable them
     */
    private void enableAttrConditionWithInputParameter(final String inputParamName, final boolean enable) {
        CondTuple condTuple = (CondTuple) this.getAttrContext().getConditions();
        for (int index = 0; index < condTuple.getNumberOfEntries(); index++) {
            CondMember condition = condTuple.getCondMemberAt(index);
            if (condition.getAllVariables().contains(inputParamName)) {
                condition.setEnabled(enable);
            }
        }
    }

    /**
     * Checks whether the left-hand side of this rule can be matched in the specified graph using the given matching
     * completion strategy. This method temporarily disables all NACs, PACs, and nested ACs to check only the basic
     * applicability of the LHS pattern without considering application conditions.
     *
     * @param g the graph to check for LHS applicability
     * @param strategy the matching completion strategy to use
     * @param doCheckIfReadyToTransform if true, checks whether the rule is ready to transform first
     * @return true if the LHS pattern can be found in the graph, false otherwise
     * @see #isApplicable(Graph, MorphCompletionStrategy)
     */
    public boolean isLeftApplicable(
            final Graph g,
            final MorphCompletionStrategy strategy,
            final boolean doCheckIfReadyToTransform) {
        boolean result = true;
        if (doCheckIfReadyToTransform) {
            result = this.isReadyToTransform();
        }
        if (result) {
            result = false;
            Map<OrdinaryMorphism, Boolean> applcond2enable = new HashMap<>(
                    this.nacStrategie.getAcsListInternal().size() + this.pacStrategie.getAcsListInternal().size() + this.acStrategie.getAcsListInternal().size());
            // store nac.isEnabled() setting and disable nac 
            for (OrdinaryMorphism negativeApplCond : this.nacStrategie.getAcsListInternal()) {
                applcond2enable.put(negativeApplCond, Boolean.valueOf(negativeApplCond.isEnabled()));
                negativeApplCond.setEnabled(false);
            }
            // store pac.isEnabled() setting and disable pac 
            for (OrdinaryMorphism positiveApplCond : this.pacStrategie.getAcsListInternal()) {
                applcond2enable.put(positiveApplCond, Boolean.valueOf(positiveApplCond.isEnabled()));
                positiveApplCond.setEnabled(false);
            }
            // store ac.isEnabled() setting and disable ac 
            for (OrdinaryMorphism nestedApplCond : this.acStrategie.getAcsListInternal()) {
                applcond2enable.put(nestedApplCond, Boolean.valueOf(nestedApplCond.isEnabled()));
                nestedApplCond.setEnabled(false);
            }
            Match m = BaseFactory.theFactory().createMatch(this, g);
            if (m != null) {
                m.setCompletionStrategy(strategy);
                while (m.nextCompletion()) {
                    result = true;
                    break;
                }
            }
            BaseFactory.theFactory().destroyMatch(m);
            // restore enable setting
            for (OrdinaryMorphism negativeApplCond : this.nacStrategie.getAcsListInternal()) {
                negativeApplCond.setEnabled(applcond2enable.get(negativeApplCond).booleanValue());
            }
            for (OrdinaryMorphism positiveApplCond : this.pacStrategie.getAcsListInternal()) {
                positiveApplCond.setEnabled(applcond2enable.get(positiveApplCond).booleanValue());
            }
            for (OrdinaryMorphism nestedApplCond : this.acStrategie.getAcsListInternal()) {
                nestedApplCond.setEnabled(applcond2enable.get(nestedApplCond).booleanValue());
            }
        }
        return result;
    }

    /**
     * Enables or disables all negative application conditions (NACs) of this rule.
     *
     * @param enable true to enable all NACs, false to disable them
     */
    public void enableNACs(boolean enable) {
        this.nacStrategie.enableAcs(enable);
    }

    /**
     * Enables or disables all positive application conditions (PACs) of this rule.
     *
     * @param enable true to enable all PACs, false to disable them
     */
    public void enablePACs(boolean enable) {
        this.pacStrategie.enableAcs(enable);
    }

    /**
     * Sets the applicability flag for this rule. This flag indicates whether the rule can currently be applied during
     * graph transformation.
     *
     * @param appl true if the rule should be considered applicable, false otherwise
     */
    public void setApplicable(boolean appl) {
        this.applicable = appl;
    }

    /**
     * Returns the type set associated with this rule. The type set is derived from the left-hand side graph of this
     * rule.
     *
     * @return the type set of this rule
     */
    public TypeSet getTypeSet() {
        return getLeft().getTypeSet();
    }
//	private Graph createCondGraph(final TypeSet types) {
//		return types.isArcDirected()? new Graph(types): new UndirectedGraph(types);
//	}

    /**
     * Creates and adds a new nested application condition (GAC) to this rule. Note: Because the new morphism is
     * initially empty and the LHS graph is not, it is not a morphism in theoretical terms, which demands an application
     * condition to be a total morphism.
     *
     * @return an empty nested application condition with the original set to this rule's left-hand side graph
     */
    public NestedApplCond createNestedAC() {
        return this.acStrategie.createAc();
    }

    /**
     * Creates and adds a new nested application condition (GAC) to this rule. The target graph of the new GAC is
     * constructed based on the RHS of this rule.
     *
     * @return a new nested application condition with target graph constructed from the RHS
     */
    public NestedApplCond createNestedACDuetoRHS() {
        return this.acStrategie.createNestedAcDuetoRHS();
    }

    /**
     * Adds the specified morphism representing a nested application condition.
     * <p>
     * <b>Precondition:</b> The AC's original graph must be this rule's left-hand side graph.
     *
     * @param ac the nested application condition morphism to add
     * @return true if the AC was added successfully, false if it was already present
     */
    public boolean addNestedAC(final OrdinaryMorphism ac) {
        return this.acStrategie.addNestedAc(ac);
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
    public boolean addNestedAC(int indx, final OrdinaryMorphism ac) {
        return this.acStrategie.addAc(indx, ac);
    }

    /**
     * Enables or disables all nested application conditions (ACs) of this rule.
     *
     * @param enable true to enable all ACs, false to disable them
     */
    public void enableNestedAC(boolean enable) {
        this.acStrategie.enableAcs(enable);
    }

    /**
     * Destroys the specified nested application condition and removes it from this rule. The target graph of the AC
     * morphism is also disposed.
     *
     * @param ac the nested application condition morphism to destroy
     */
    public void destroyNestedAC(final OrdinaryMorphism ac) {
        this.acStrategie.destroyAc(ac);
    }

    /**
     * Checks if this rule contains any nested application conditions.
     *
     * @return true if the rule has at least one nested AC, false otherwise
     */
    public boolean hasNestedACs() {
        return this.acStrategie.hasAcs();
    }

    /**
     * Returns an iterator over all nested application conditions of this rule.
     *
     * @return an iterator of all nested AC morphisms
     */
    public Iterator<OrdinaryMorphism> getNestedACs() {
        return this.acStrategie.getAcs();
    }

    /**
     * Returns a list of all enabled nested application conditions of this rule.
     *
     * @return a list of all enabled nested AC morphisms
     */
    public List<NestedApplCond> getEnabledACs() {
        return this.acStrategie.getEnabledAcs();
    }

    /**
     * Returns the list of all nested application condition morphisms of this rule.
     *
     * @return the list of nested AC morphisms
     */
    public List<OrdinaryMorphism> getNestedACsList() {
        return this.acStrategie.getAcsList();
    }

    /**
     * Returns a list of all enabled nested application conditions as evaluable objects.
     *
     * @return a list of all enabled nested AC morphisms as evaluable objects
     */
    public List<Evaluable> getEnabledGeneralACsAsEvaluable() {
        return this.acStrategie.getEnabledGeneralAcsAsEvaluable();
    }

    /**
     * Returns the nested application condition morphism with the specified name.
     *
     * @param name the name of the nested AC to find
     * @return the nested AC morphism with the specified name, or null if not found
     */
    public OrdinaryMorphism getNestedAC(String name) {
        return this.acStrategie.getAc(name);
    }

    /**
     * Returns the nested application condition morphism at the specified index.
     *
     * @param index the index of the nested AC to retrieve
     * @return the nested AC morphism at the specified index, or null if index is out of bounds
     */
    public OrdinaryMorphism getNestedAC(int index) {
        return this.acStrategie.getAc(index);
    }

    /**
     * Removes the specified nested application condition from this rule.If the AC was enabled, this method also updates
     * the formula by patching out the evaluable and refreshing the formula.
     *
     * @param applicationCondition the nested application condition morphism to remove
     * @return false if the AC was not found, true if it was removed successfully
     */
    public final boolean removeNestedAC(OrdinaryMorphism applicationCondition) {
        return this.acStrategie.removeNestedAc(applicationCondition);
    }

    /**
     * Checks if any nested application condition in this rule is using the specified variable in the context of the
     * specified attribute condition tuple.
     *
     * @param var the variable member to check for usage
     * @param act the attribute condition tuple providing context
     * @return true if any nested AC uses the variable in the given context, false otherwise
     */
    public boolean nestedACIsUsingVariable(
            final VarMember var,
            final AttrConditionTuple act) {
        return this.acStrategie.nestedAcIsUsingVariable(var, act);
    }

    /**
     * Creates a new negative application condition (NAC) and adds it to this rule. Note: Because the new morphism is
     * initially empty and the LHS graph is not, it is not a morphism in theoretical terms, which demands a NAC to be a
     * total morphism.
     *
     * @return an empty morphism with the original set to this rule's left-hand side graph
     */
    public OrdinaryMorphism createNAC() {
        return this.nacStrategie.createAc();
    }

    /**
     * Creates a new negative application condition (NAC) and adds it to this rule. The target graph of the new NAC is
     * constructed based on the RHS of this rule.
     *
     * @return a new NAC with target graph constructed from the RHS
     */
    public OrdinaryMorphism createNACDuetoRHS() {
        return this.nacStrategie.createAcDuetoRHS();
    }

    /**
     * Constructs the target graph of the specified morphism based on the RHS of this rule. This method copies nodes and
     * arcs from the RHS to create a target graph for application conditions (NACs, PACs, or nested ACs).
     *
     * @param morph the morphism whose target graph should be constructed
     */
    public void makeACDuetoRHS(final OrdinaryMorphism morph) {
        var nodeMap = new HashMap<Node, Node>();
        Iterator<Node> rightNodes = this.itsImag.getNodesSet().iterator();
        while (rightNodes.hasNext()) {
            Node rightNode = rightNodes.next();
            Iterator<GraphObject> inverseImageIter = this.getInverseImage(rightNode);
            if (inverseImageIter.hasNext()) {
                Node leftNode = (Node) inverseImageIter.next();
                try {
                    Node targetNode = morph.getTarget().copyNode(leftNode);
                    try {
                        morph.addMapping(leftNode, targetNode);
                        while (inverseImageIter.hasNext()) {
                            morph.addMapping((Node) inverseImageIter.next(), targetNode);
                        }
                        nodeMap.put(rightNode, targetNode);
                    } catch (BadMappingException ex) {
                        // Intentionally empty
                    }
                } catch (TypeException e) {
                    // Intentionally empty
                }
            } else {
                try {
                    Node targetNode = morph.getTarget().copyNode(rightNode);
                    if (targetNode.getAttribute() != null) {
                        ((agg.attribute.impl.ValueTuple) targetNode.getAttribute()).unsetValueAsExpr();
                    }
                    nodeMap.put(rightNode, targetNode);
                } catch (TypeException e) {
                    // Intentionally empty
                }
            }
        }
        Iterator<Arc> arcIterator = this.itsImag.getArcsSet().iterator();
        while (arcIterator.hasNext()) {
            Arc rightArc = arcIterator.next();
            Iterator<GraphObject> inverseImageIter = this.getInverseImage(rightArc);
            if (inverseImageIter.hasNext()) {
                Arc leftArc = (Arc) inverseImageIter.next();
                try {
                    Arc targetArc = morph.getTarget().copyArc(leftArc, (Node) morph.getImage(leftArc.getSource()), (Node) morph.getImage(leftArc.getTarget()));
                    try {
                        morph.addMapping(leftArc, targetArc);
                        while (inverseImageIter.hasNext()) {
                            morph.addMapping(inverseImageIter.next(), targetArc);
                        }
                    } catch (BadMappingException ex) {
                        // Intentionally empty
                    }
                } catch (TypeException e) {
                    // Intentionally empty
                }
            } else {
                try {
                    Node sourceNode = nodeMap.get(rightArc.getSource());
                    Node targetNode = nodeMap.get(rightArc.getTarget());
                    Arc targetArc = morph.getTarget().copyArc(rightArc, sourceNode, targetNode);
                    if (targetArc.getAttribute() != null) {
                        ((agg.attribute.impl.ValueTuple) targetArc.getAttribute()).unsetValueAsExpr();
                    }
                } catch (TypeException e) {
                    // Intentionally empty
                }
            }
        }
        nodeMap.clear();
        nodeMap = null;
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
        return this.nacStrategie.addAc(negativeApplCond);
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
        return this.nacStrategie.addAc(index, negativeApplCond);
    }

    /**
     * Destroys the specified negative application condition and removes it from this rule. The target graph of the NAC
     * morphism is also disposed.
     *
     * @param negativeApplCond the negative application condition morphism to destroy
     */
    public void destroyNAC(OrdinaryMorphism negativeApplCond) {
        this.nacStrategie.destroyAc(negativeApplCond);
    }

    /**
     * Checks if this rule contains any negative application conditions.
     *
     * @return true if the rule has at least one NAC, false otherwise
     */
    public boolean hasNACs() {
        return this.nacStrategie.hasAcs();
    }

    /**
     * Checks if this rule has at least one enabled negative application condition.
     *
     * @return true if the rule has at least one enabled NAC, false otherwise
     */
    public boolean hasEnabledNACs() {
        return this.nacStrategie.hasEnabledAcs();
    }

    /**
     * Returns an iterator over all negative application conditions of this rule.
     *
     * @return an iterator of all NAC morphisms
     */
    public Iterator<OrdinaryMorphism> getNACs() {
        return this.nacStrategie.getAcs();
    }

    /**
     * Returns the list of all negative application condition morphisms of this rule.
     *
     * @return the list of NAC morphisms
     */
    public List<OrdinaryMorphism> getNACsList() {
        return this.nacStrategie.getAcsList();
    }

    /**
     * Returns the negative application condition morphism with the specified name.
     *
     * @param name the name of the NAC to find
     * @return the NAC morphism with the specified name, or {@code null} if not found
     */
    public OrdinaryMorphism getNAC(String name) {
        return this.nacStrategie.getAc(name);
    }

    /**
     * Returns the negative application condition morphism at the specified index.
     *
     * @param index the index of the NAC to retrieve
     * @return the NAC morphism at the specified index, or {@code null} if index is out of bounds
     */
    public OrdinaryMorphism getNAC(int index) {
        return this.nacStrategie.getAc(index);
    }

    /**
     * Returns the negative application condition morphism with the specified target graph.
     *
     * @param graph the target graph to search for
     * @return the NAC morphism with the specified target graph, or {@code null} if not found
     */
    public OrdinaryMorphism getNAC(final Graph graph) {
        return this.nacStrategie.getAc(graph);
    }

    /**
     * Checks if the specified graph is the target graph of any negative application condition.
     *
     * @param graph the graph to check
     * @return true if the graph is a target of any NAC, false otherwise
     */
    public boolean hasNAC(final Graph graph) {
        return this.nacStrategie.hasAc(graph);
    }

    /**
     * Removes the specified negative application condition from this rule.
     *
     * @param negativeApplCond the negative application condition morphism to remove
     * @return false if the NAC was not found, true if it was removed successfully
     */
    public final boolean removeNAC(OrdinaryMorphism negativeApplCond) {
        return this.nacStrategie.removeAc(negativeApplCond);
    }

    /**
     * Creates a new positive application condition (PAC) and adds it to this rule. Note: Because the new morphism is
     * initially empty and the LHS graph is not, it is not a morphism in theoretical terms, which demands a PAC to be a
     * total morphism.
     *
     * @return an empty morphism with the original set to this rule's left-hand side graph
     */
    public OrdinaryMorphism createPAC() {
        return this.pacStrategie.createAc();
    }

    /**
     * Adds the specified morphism representing a positive application condition (PAC).
     * <p>
     * <b>Precondition:</b> The PAC's original graph must be this rule's left-hand side graph.
     *
     * @param positiveApplCond the positive application condition morphism to add
     * @return true if the PAC was added successfully, false if it was already present
     */
    public boolean addPAC(final OrdinaryMorphism positiveApplCond) {
        return this.pacStrategie.addAc(positiveApplCond);
    }

    /**
     * Adds the specified morphism representing a positive application condition (PAC) at the specified index in the
     * list.
     * <p>
     * <b>Precondition:</b> The PAC's original graph must be this rule's left-hand side graph.
     *
     * @param index the index at which to insert the PAC, or -1 to append to the end
     * @param positiveApplCond the positive application condition morphism to add
     * @return true if the PAC was added successfully, false if it was already present
     */
    public boolean addPAC(int index, final OrdinaryMorphism positiveApplCond) {
        return this.pacStrategie.addAc(index, positiveApplCond);
    }

    /**
     * Adds a new shifted positive application condition composed of the specified list of morphisms.
     *
     * @param morphismList the list of morphisms that form the shifted PAC
     */
    public void addShiftedPAC(final List<OrdinaryMorphism> morphismList) {
        this.pacStrategie.addShiftedPAC(morphismList);
    }

    /**
     * Returns the list of all shifted positive application conditions.
     *
     * @return the list of shifted PACs, may be {@code null} if none exist
     */
    public List<ShiftedPAC> getShiftedPACs() {
        return this.pacStrategie.getShiftedPACs();
    }

    /**
     * Checks if the specified morphism is part of any shifted positive application condition.
     *
     * @param positiveApplCond the morphism to check
     * @return true if the morphism is part of a shifted PAC, false otherwise
     */
    public boolean isShiftedPAC(final OrdinaryMorphism positiveApplCond) {
        return this.pacStrategie.isShiftedPAC(positiveApplCond);
    }

    /**
     * Destroys the specified positive application condition and removes it from this rule. The target graph of the PAC
     * morphism is also disposed.
     *
     * @param positiveApplCond the positive application condition morphism to destroy
     */
    public void destroyPAC(final OrdinaryMorphism positiveApplCond) {
        this.pacStrategie.destroyAc(positiveApplCond);
    }

    /**
     * Checks if this rule contains any positive application conditions.
     *
     * @return true if the rule has at least one PAC, false otherwise
     */
    public boolean hasPACs() {
        return this.pacStrategie.hasAcs();
    }

    /**
     * Checks if this rule has at least one enabled positive application condition.
     *
     * @return true if the rule has at least one enabled PAC, false otherwise
     */
    public boolean hasEnabledPACs() {
        return this.pacStrategie.hasEnabledAcs();
    }

    /**
     * Returns an iterator over all positive application conditions of this rule.
     *
     * @return an iterator of all PAC morphisms
     */
    public Iterator<OrdinaryMorphism> getPACs() {
        return this.pacStrategie.getAcs();
    }

    /**
     * Returns an iterator over all enabled positive application conditions of this rule.
     *
     * @return an iterator of all enabled PAC morphisms
     */
    public Iterator<OrdinaryMorphism> getEnabledPACs() {
        return this.pacStrategie.getEnabledAcs();
    }

    /**
     * Returns the list of all positive application condition morphisms of this rule.
     *
     * @return the list of PAC morphisms
     */
    public List<OrdinaryMorphism> getPACsList() {
        return this.pacStrategie.getAcsList();
    }

    /**
     * Returns the positive application condition morphism with the specified name.
     *
     * @param name the name of the PAC to find
     * @return the PAC morphism with the specified name, or {@code null} if not found
     */
    public OrdinaryMorphism getPAC(String name) {
        return this.pacStrategie.getAc(name);
    }

    /**
     * Returns the positive application condition morphism at the specified index.
     *
     * @param index the index of the PAC to retrieve
     * @return the PAC morphism at the specified index, or {@code null} if index is out of bounds
     */
    public OrdinaryMorphism getPAC(int index) {
        return this.pacStrategie.getAc(index);
    }

    /**
     * Returns the positive application condition morphism with the specified target graph.
     *
     * @param graph the target graph to search for
     * @return the PAC morphism with the specified target graph, or {@code null} if not found
     */
    public OrdinaryMorphism getPAC(final Graph graph) {
        return this.pacStrategie.getAc(graph);
    }

    /**
     * Checks if the specified graph is the target graph of any positive application condition.
     *
     * @param graph the graph to check
     * @return true if the graph is a target of any PAC, false otherwise
     */
    public boolean hasPAC(final Graph graph) {
        return this.pacStrategie.hasAc(graph);
    }

    /**
     * Removes the specified positive application condition from this rule.
     *
     * @param positiveApplCond the positive application condition morphism to remove
     * @return false if the PAC was not found, true if it was removed successfully
     */
    public final boolean removePAC(OrdinaryMorphism positiveApplCond) {
        return this.pacStrategie.removeAc(positiveApplCond);
    }

    // /////////////////////////////////////
    /**
     * Checks if the specified node type can be used to create a node in the RHS. Returns false if the node type is
     * abstract and used in the RHS to create a node, otherwise returns true.
     *
     * @param nodeType the node type to check
     * @return false if the node type is abstract and used in RHS, true otherwise
     */
    public boolean checkCreateAbstractNode(Type nodeType) {
        Iterator<Node> nodeIter = getTarget().getNodesSet().iterator();
        while (nodeIter.hasNext()) {
            Node currentNode = nodeIter.next();
            if (currentNode.getType().equals(nodeType)) {
                if (!this.getInverseImage(currentNode).hasNext()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if any node in the RHS that has no preimage in the LHS violates type constraints regarding required arcs.
     * Returns a TypeError if such a violation is found, otherwise null.
     *
     * @return a TypeError describing the constraint violation, or {@code null} if all nodes satisfy constraints
     */
    public TypeError checkNewNodeRequiresArc() {
        final Iterator<Node> nodeIter = this.getRight().getNodesSet().iterator();
        while (nodeIter.hasNext()) {
            final GraphObject currentObj = nodeIter.next();
            if (!this.getInverseImage(currentObj).hasNext()) {
                List<String> requiredArcTypes = this.getRight().getTypeSet().nodeRequiresArc((Node) currentObj);
                if (requiredArcTypes != null && !requiredArcTypes.isEmpty()) {
                    TypeError typeError = new TypeError(TypeError.TO_LESS_ARCS,
                            "Node type  "
                            + "\"" + currentObj.getType().getName() + "\" \n"
                            + "requires edge(s) of type: \n"
                            + requiredArcTypes.toString(), currentObj.getType());
                    typeError.setContainingGraph(this.getRight());
                    return typeError;
                }
            }
        }
        return null;
    }

    /**
     * Attempts to destroy all graph objects of the specified type from all graphs associated with this rule (LHS, RHS,
     * NACs, PACs, nested ACs).
     *
     * @param t the type of graph objects to destroy
     * @return true if all objects of the specified type were successfully destroyed, false otherwise
     */
    public boolean destroyObjectsOfType(Type type) {
        if (getLeft().destroyObjectsOfType(type)) {
            if (getRight().destroyObjectsOfType(type)) {
                for (OrdinaryMorphism negativeApplCond : this.nacStrategie.getAcsListInternal()) {
                    if (!negativeApplCond.getTarget().destroyObjectsOfType(type)) {
                        return false;
                    }
                }
                for (OrdinaryMorphism positiveApplCond : this.pacStrategie.getAcsListInternal()) {
                    if (!positiveApplCond.getTarget().destroyObjectsOfType(type)) {
                        return false;
                    }
                }
                for (OrdinaryMorphism nestedApplCond : this.acStrategie.getAcsListInternal()) {
                    if (!nestedApplCond.getTarget().destroyObjectsOfType(type)) {
                        return false;
                    }
                }
                // delete from rule application conditions
                List<EvalSet> atomConds = getAtomApplConds();
                for (int atomCondIndex = 0; atomCondIndex < atomConds.size(); atomCondIndex++) {
                    List<?> evalSetList = atomConds.get(atomCondIndex).getSet();
                    for (int nestedEvalSetIndex = 0; nestedEvalSetIndex < evalSetList.size(); nestedEvalSetIndex++) {
                        List<?> nestedEvalSet = ((EvalSet) evalSetList.get(nestedEvalSetIndex)).getSet();
                        for (int atomApplCondIndex = 0; atomApplCondIndex < nestedEvalSet.size(); atomApplCondIndex++) {
                            agg.cons.AtomApplCond atomApplCond = (agg.cons.AtomApplCond) nestedEvalSet
                                    .get(atomApplCondIndex);
                            OrdinaryMorphism preCondition = atomApplCond.getPreCondition();
                            OrdinaryMorphism tMorphism = atomApplCond.getT();
                            OrdinaryMorphism qMorphism = atomApplCond.getQ();
                            preCondition.getSource().destroyObjectsOfType(type);
                            preCondition.getTarget().destroyObjectsOfType(type);
                            tMorphism.getTarget().destroyObjectsOfType(type);
                            qMorphism.getSource().destroyObjectsOfType(type);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Attempts to destroy all graph objects of the specified types from all graphs associated with this rule (LHS, RHS,
     * NACs, PACs, nested ACs).
     *
     * @param types the list of types whose objects should be destroyed
     * @return a list of names of types that could not be destroyed completely
     */
    public List<String> destroyObjectsOfTypes(List<Type> typeList) {
        List<String> failed = new ArrayList<>(5);
        for (int index = 0; index < typeList.size(); index++) {
            Type currentType = typeList.get(index);
            if (!destroyObjectsOfType(currentType)) {
                String errorMsg = "Rule:  " + getName() + "   Type:  " + currentType.getName();
                failed.add(errorMsg);
            }
        }
        return failed;
    }

    /**
     * Returns a copy of this rule using its current types.
     *
     * @return a new Rule instance that is a copy of this rule
     */
    public Rule getClone() {
        return BaseFactory.theFactory().cloneRule(this);
    }

    /**
     * Returns a copy of this rule using the specified types.
     *
     * @param types the type set to use for the cloned rule
     * @return a new Rule instance that is a copy of this rule with the specified types
     */
    public Rule getClone(TypeSet types) {
        return BaseFactory.theFactory().cloneRule(this, types, true);
    }

    /**
     * Returns the morphism between the left and right graphs of this rule. This is the rule itself, as Rule extends
     * OrdinaryMorphism.
     *
     * @return this rule as an OrdinaryMorphism
     */
    public final OrdinaryMorphism getMorphism() {
        return this;
    }

    /**
     * Returns the list of graph constraints that can be converted to post-application constraints.
     *
     * @return the list of formula constraints, or an empty list if none exist
     */
    public List<Formula> getConstraints() {
        return (this.constraints != null) ? this.constraints : new ArrayList<>(0);
    }

    /**
     * Checks the type compatibility of two graph objects. The first object should belong to the LHS, the second to the
     * RHS, to be used for a mapping of the rule morphism.
     *
     * @param orig the original type from the LHS
     * @param image the image type from the RHS
     * @return true if the types are compatible, false otherwise
     */
    protected boolean checkType(Type originType, Type imageType) {
        return originType.compareTo(imageType);
    }

    /**
     * Creates attribute instances where needed for all graphs in this rule (LHS, RHS, NACs, PACs, nested ACs).
     */
    public void createAttrInstanceWhereNeeded() {
        this.itsOrig.createAttrInstanceWhereNeeded();
        this.itsImag.createAttrInstanceWhereNeeded();
        for (OrdinaryMorphism ordMorph : this.nacStrategie.getAcsListInternal()) {
            ordMorph.getTarget().createAttrInstanceWhereNeeded();
        }
        for (OrdinaryMorphism ordMorph : this.pacStrategie.getAcsListInternal()) {
            ordMorph.getTarget().createAttrInstanceWhereNeeded();
        }
        for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
            ordMorph.getTarget().createAttrInstanceWhereNeeded();
        }
    }

    /**
     * Creates attribute instances of the specified type where needed for all graphs in this rule (LHS, RHS, NACs, PACs,
     * nested ACs).
     *
     * @param t the type for which to create attribute instances
     */
    public void createAttrInstanceOfTypeWhereNeeded(final Type type) {
        this.itsOrig.createAttrInstanceOfTypeWhereNeeded(type);
        this.itsImag.createAttrInstanceOfTypeWhereNeeded(type);
        for (OrdinaryMorphism ordMorph : this.nacStrategie.getAcsListInternal()) {
            ordMorph.getTarget().createAttrInstanceOfTypeWhereNeeded(type);
        }
        for (OrdinaryMorphism ordMorph : this.pacStrategie.getAcsListInternal()) {
            ordMorph.getTarget().createAttrInstanceOfTypeWhereNeeded(type);
        }
        for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
            ordMorph.getTarget().createAttrInstanceOfTypeWhereNeeded(type);
        }
    }

    /**
     * Generates rule post application conditions from its constraints (formulas). Returns an error message if something
     * went wrong, otherwise an empty string.
     *
     * @return an error message if conversion failed, or empty string if successful
     */
    public String convertUsedFormulas() {
        if (this.itsUsedAtomics != null && this.itsUsedAtomics.size() > 0
                && this.itsUsedFormulas != null && this.itsUsedFormulas.size() > 0) {
            String errorMsg = "";
            var finalEvalSets = new ArrayList<EvalSet>();
            var atomicNames = new ArrayList<String>();
            // clear Post Appl. Conditions
            if (this.constraints == null) {
                constraints = new ArrayList<>();
            } else {
                this.constraints.clear();
            }
            setAtomApplConds(null, null);
            final Map<AtomConstraint, EvalSet> atomicToEvalSet = new HashMap<>();
            final Map<String, String> failedAtomicToError = new HashMap<>();
            int typeGraphLevel = this.getTypeSet().getLevelOfTypeGraphCheck();
            if (typeGraphLevel > TypeSet.ENABLED_MAX) {
                this.getTypeSet().setLevelOfTypeGraph(TypeSet.ENABLED_MAX);
            }
            for (int index = 0; index < this.itsUsedAtomics.size(); index++) {
                AtomConstraint atomicConstraint = this.itsUsedAtomics.get(index);
                if (!atomicConstraint.isValid()) {
                    errorMsg = "Atomic  \"" + atomicConstraint.getAtomicName() + "\"  is not valid.";
                    this.itsUsedAtomics.clear();
                    this.itsUsedFormulas.clear();
                    return errorMsg;
                }
                ((AttrTupleManager) AttrTupleManager.getDefaultManager())
                        .setVariableContext(true);
                Convert converter = new Convert(this, atomicConstraint);
                List<Object> evalList = converter.convert();
                ((AttrTupleManager) AttrTupleManager.getDefaultManager())
                        .setVariableContext(false);
                final EvalSet evalSet = new EvalSet(evalList);
                finalEvalSets.add(evalSet);
                atomicNames.add(atomicConstraint.getAtomicName());
                if (!evalList.isEmpty()) {
                    atomicToEvalSet.put(atomicConstraint, evalSet);
                }
                if (!"".equals(converter.getErrorMsg())) {
                    failedAtomicToError.put(atomicConstraint.getAtomicName(), converter.getErrorMsg());
                }
            }
            this.getTypeSet().setLevelOfTypeGraph(typeGraphLevel);
            if (!failedAtomicToError.isEmpty()) {
                errorMsg = "Error(s) during creating Post Application Condition:";
            }
            for (int formulaIndex = 0; formulaIndex < this.itsUsedFormulas.size(); formulaIndex++) {
                Formula formula = this.itsUsedFormulas.get(formulaIndex);
                if (!formula.isEnabled()) {
                    continue;
                }
                List<Evaluable> evalList = new ArrayList<>();
                String formulaStr = formula.getAsString(evalList);
                // In evalList the atomics used in formula are noted.
                // In finalEvalSets the set of _all_new atomics are noted
                // (though they are real formulas now) in the original order.
                // This means, we need a translation.
                // I.e. we build a new List as the source of a new formula
                // only containing the base formulas
                // corresponding to the atomic at that index.
                boolean formulaOk = true;
                List<Evaluable> newEvalList = new ArrayList<>();
                for (int elementIndex = 0; elementIndex < evalList.size(); elementIndex++) {
                    Object element = evalList.get(elementIndex);
                    boolean convertOk = false;
                    int atomicIndex;
                    for (atomicIndex = 0; atomicIndex < this.itsUsedAtomics.size(); atomicIndex++) {
                        if (this.itsUsedAtomics.get(atomicIndex) == element) {
                            final String atomicName = this.itsUsedAtomics.get(atomicIndex).getAtomicName();
                            //						System.out.println(atomicName));
                            Evaluable evalSet = atomicToEvalSet.get(element);
                            if (evalSet != null) {
                                newEvalList.add(evalSet);
                                convertOk = true;
                                break;
                            }
                            int nameIndex = atomicNames.indexOf(atomicName);
                            finalEvalSets.remove(nameIndex);
                            atomicNames.remove(nameIndex);
                        }
                    }
                    if (!convertOk) {
                        formulaOk = false;
                        break;
                    }
                }
                if (formulaOk) {
                    Formula newFormula = new Formula(newEvalList, formulaStr);
                    this.constraints.add(newFormula);
                }
            }
            if (finalEvalSets.isEmpty()) {
                this.itsUsedAtomics.clear();
                this.itsUsedFormulas.clear();
            } else {
                this.setAtomApplConds(finalEvalSets, atomicNames);
            }
            deleteTransientContextVariables(getSource());
            deleteTransientContextVariables(getTarget());
            this.removeUnusedVariableOfAttrContext();
            String errorMsgPart1 = "Cannot convert atomic(s) :\n";
            String errorMsgPart2 = "";
            final Iterator<String> failedAtomicIter = failedAtomicToError.keySet().iterator();
            while (failedAtomicIter.hasNext()) {
                String atomicName = failedAtomicIter.next();
                String atomicError = failedAtomicToError.get(atomicName);
                errorMsgPart2 = errorMsgPart2.concat(" - ").concat(atomicName).concat(" - ").concat("\n");
                errorMsgPart2 = errorMsgPart2.concat(atomicError).concat("\n");
            }
            if (!"".equals(errorMsgPart2)) {
                errorMsgPart1 = errorMsgPart1.concat(errorMsgPart2);
                errorMsg = errorMsg.concat(errorMsgPart1);
            }
            return errorMsg;
        } else {
            return "Cannot create post application conditions. There isn't any formula selected.";
        }
    }

    /**
     * Sets the constraints (formulas) which will be used for generating post application conditions. This method also
     * extracts any atomic constraints from the formulas.
     *
     * @param formulasToUse the list of formulas to use for generating post application conditions
     */
    public void setUsedFormulas(List<Formula> formulasToUse) {
        if (!formulasToUse.isEmpty()) {
            if (this.itsUsedFormulas == null) {
                itsUsedFormulas = new ArrayList<>();
            } else {
                this.itsUsedFormulas.clear();
            }
            if (this.itsUsedAtomics == null) {
                itsUsedAtomics = new ArrayList<>();
            } else {
                this.itsUsedAtomics.clear();
            }
            this.itsUsedFormulas.addAll(formulasToUse);
            for (int index = 0; index < this.itsUsedFormulas.size(); index++) {
                Formula formula = this.itsUsedFormulas.get(index);
                List<Evaluable> evalVec = new ArrayList<>();
                String formulaStr = formula.getAsString(evalVec);
                for (int elementIndex = 0; elementIndex < evalVec.size(); elementIndex++) {
                    if (evalVec.get(elementIndex) instanceof AtomConstraint) {
                        AtomConstraint atomConstraint = (AtomConstraint) evalVec.get(elementIndex);
                        this.itsUsedAtomics.add(atomConstraint);
                    } else {
                        System.out
                                .println("Rule.setUsedFormulas(List<Formula> usedFormulas):  formula: "
                                        + formulaStr + "   FAILED!");
                    }
                }
            }
        }
    }

    /**
     * Returns a list of atomic graph constraints used for generating post application conditions. The elements are of
     * type agg.cons.AtomConstraint.
     *
     * @return a list of atomic constraints, or an empty list if none exist
     */
    public List<AtomConstraint> getUsedAtomics() {
        return (this.itsUsedAtomics != null) ? this.itsUsedAtomics : new ArrayList<>(0);
    }

    /**
     * Returns the list of constraints (formulas) used for generating post application conditions. The elements are of
     * type agg.cons.Formula.
     *
     * @return a list of formulas, or an empty list if none exist
     */
    public List<Formula> getUsedFormulas() {
        return (this.itsUsedFormulas != null) ? this.itsUsedFormulas : new ArrayList<>(0);
    }

    /**
     * Clears all lists of graph constraints if the specified atomic graph constraint belongs to this rule's
     * constraints.
     *
     * @param atomConstraint the atomic constraint to check for presence before clearing
     */
    public void clearConstraints(AtomConstraint atomConstraint) {
        if (this.itsUsedAtomics != null && this.itsUsedAtomics.contains(atomConstraint)) {
            this.clearConstraints();
        }
    }

    /**
     * Clears all lists of graph constraints if the specified formula constraint belongs to this rule's constraints.
     *
     * @param formula the formula to check for presence before clearing
     */
    public void clearConstraints(Formula formula) {
        if (this.itsUsedFormulas != null && this.itsUsedFormulas.contains(formula)) {
            this.clearConstraints();
        }
    }

    /**
     * Clears all lists of graph constraints (used atomics, used formulas, constraints, and atom application
     * conditions).
     */
    public void clearConstraints() {
        if (this.itsUsedAtomics != null) {
            this.itsUsedAtomics.clear();
        }
        if (this.itsUsedFormulas != null) {
            this.itsUsedFormulas.clear();
        }
        if (this.constraints != null) {
            this.constraints.clear();
        }
        setAtomApplConds(null, null);
    }

    /**
     * Sets the specified post application conditions to this rule's conditions.
     *
     * @param evalSetList the list of evaluation sets representing post application conditions
     * @param constraintNames the list of names corresponding to the evaluation sets
     */
    public void setAtomApplConds(List<EvalSet> evalSetList, List<String> constraintNames) {
        if (this.atom_conditions == null) {
            this.atom_conditions = new ArrayList<>();
        } else {
            this.atom_conditions.clear();
        }
        if (this.constraintNameSet == null) {
            this.constraintNameSet = new ArrayList<>();
        } else {
            this.constraintNameSet.clear();
        }
        if (evalSetList != null) {
            this.atom_conditions.addAll(evalSetList);
        }
        if (constraintNames != null) {
            this.constraintNameSet.addAll(constraintNames);
        }
        if (this.constraintNameSet.size() < this.atom_conditions.size()) {
            for (int index = this.constraintNameSet.size(); index < this.atom_conditions.size(); index++) {
                this.constraintNameSet.add("Unknown Name " + index);
            }
        }
    }

    /**
     * Returns the list of atomic application conditions.
     *
     * @return the list of evaluation sets, or an empty list if none exist
     */
    public List<EvalSet> getAtomApplConds() {
        return (this.atom_conditions != null) ? this.atom_conditions : new ArrayList<>(0);
    }

    /**
     * Returns the list of names for the constraint evaluation sets.
     *
     * @return the list of constraint names, or an empty list if none exist
     */
    public List<String> getConstraintNames() {
        return (this.constraintNameSet != null) ? this.constraintNameSet : new ArrayList<>(0);
    }

    /**
     * Removes the specified evaluation set constraint from the post application conditions.
     *
     * @param evalSetConstraint the evaluation set constraint to remove
     */
    public void removeConstraint(EvalSet evalSetConstraint) {
        if (this.atom_conditions != null && this.atom_conditions.contains(evalSetConstraint)) {
            int constraintIndex = this.atom_conditions.indexOf(evalSetConstraint);
            this.atom_conditions.remove(evalSetConstraint);
            this.constraintNameSet.remove(constraintIndex);
        }
    }

    /**
     * Removes the specified atomic application condition from the post application conditions. This method recursively
     * searches through the nested structure of evaluation sets to find and remove the specified atomic condition.
     *
     * @param atomApplCond the atomic application condition to remove
     */
    public void removeAtomApplCond(AtomApplCond atomApplCond) {
        if (this.atom_conditions != null) {
            int index = 0;
            while (index < this.atom_conditions.size()) {
                List<?> evalSet = this.atom_conditions.get(index).getSet();
                int elementIndex = 0;
                while (elementIndex < evalSet.size()) {
                    List<?> nestedEvalSet = ((EvalSet) evalSet.get(elementIndex)).getSet();
                    int nestedIndex = 0;
                    while (nestedIndex < nestedEvalSet.size()) {
                        AtomApplCond currentApplCond = (AtomApplCond) nestedEvalSet.get(nestedIndex);
                        if (atomApplCond.equals(currentApplCond)) {
                            nestedEvalSet.remove(atomApplCond);
                            // System.out.println("AtomApplCond: DONE");
                            nestedIndex = nestedEvalSet.size();
                        } else {
                            nestedIndex++;
                        }
                    }
                    if (nestedEvalSet.isEmpty()) {
                        evalSet.remove(elementIndex);
                        elementIndex = evalSet.size();
                    } else {
                        elementIndex++;
                    }
                }
                if (evalSet.isEmpty()) {
                    this.atom_conditions.remove(index);
                    this.constraintNameSet.remove(index);
                    index = this.atom_conditions.size();
                } else {
                    index++;
                }
            }
        }
    }

    /**
     * Clears all application constraints (formulas, atomics, etc.) from this rule.
     */
    public void removeApplConditions() {
        clearConstraints();
    }

    /**
     * Sets this rule to be a trigger rule of its layer. A trigger rule will be the first rule to apply on its layer. It
     * can be applied one time only. All other rules on this layer can be applied as long as possible.
     *
     * @param trigger true to set this rule as a trigger rule, false otherwise
     */
    public void setTriggerForLayer(boolean trigger) {
        this.triggerOfLayer = trigger;
    }

    /**
     * Checks if this rule is a trigger rule of its layer.
     *
     * @return true if this rule is a trigger rule, false otherwise
     */
    public boolean isTriggerOfLayer() {
        return this.triggerOfLayer;
    }

    /**
     * Returns the layer associated with this rule. The layer is used by layered grammars to organize rules in layers.
     *
     * @return the layer number of this rule
     */
    public int getLayer() {
        return this.layer;
    }

    /**
     * Sets the layer associated with this rule. The layer is used by layered grammars to organize rules in layers.
     *
     * @param l the layer number to assign to this rule
     */
    public void setLayer(int l) {
        this.layer = l;
    }

    /**
     * Returns the priority of this rule. The priority can be used during graph transformation to determine rule
     * application order.
     *
     * @return the priority number of this rule
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * Sets the priority of this rule. The priority can be used during graph transformation to determine rule
     * application order.
     *
     * @param p the priority number to assign to this rule
     */
    public void setPriority(int p) {
        this.priority = p;
    }

    /**
     * Trims the capacity of used lists to match their current size. This method optimizes memory usage by reducing the
     * internal capacity of the list of used atomic constraints.
     */
    public void trimToSize() {
        if (this.itsUsedAtomics != null) {
            for (int i = 0; i < this.itsUsedAtomics.size(); i++) {
                this.itsUsedAtomics.get(i).trimToSize();
            }
        }
    }

    /**
     * Refreshes the attributed state for all graphs in this rule (LHS, RHS, NACs, PACs, nested ACs).
     */
    public void refreshAttributed() {
        getLeft().refreshAttributed();
        getRight().refreshAttributed();
        for (OrdinaryMorphism ordMorph : this.nacStrategie.getAcsListInternal()) {
            ordMorph.getTarget().refreshAttributed();
        }
        for (OrdinaryMorphism ordMorph : this.pacStrategie.getAcsListInternal()) {
            ordMorph.getTarget().refreshAttributed();
        }
        for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
            ordMorph.getTarget().refreshAttributed();
        }
    }

    /**
     * Checks if this rule or any of its application conditions (NACs, PACs, nested ACs) are using the specified type
     * graph object (node or edge).
     *
     * @param typeObj the type graph object to check for usage
     * @return true if the type object is used anywhere in this rule, false otherwise
     */
    public boolean isUsingType(GraphObject typeObj) {
        if (getLeft().isUsingType(typeObj)) {
            return true;
        }
        if (getRight().isUsingType(typeObj)) {
            return true;
        }
        for (OrdinaryMorphism ordMorph : this.nacStrategie.getAcsListInternal()) {
            if (ordMorph.getTarget().isUsingType(typeObj)) {
                return true;
            }
        }
        for (OrdinaryMorphism ordMorph : this.pacStrategie.getAcsListInternal()) {
            if (ordMorph.getTarget().isUsingType(typeObj)) {
                return true;
            }
        }
        for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
            if (ordMorph.getTarget().isUsingType(typeObj)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeUnusedVariableOfAttrContext() {
        DeclTuple varTuple = ((VarTuple) this.getAttrContext().getVariables()).getTupleType();
        for (int index = 0; index < varTuple.getNumberOfEntries(); index++) {
            DeclMember varMember = (DeclMember) varTuple.getMemberAt(index);
            String varName = varMember.getName();
            if (!this.getSource().getVariableNamesOfAttributes().contains(varName)) {
                if (!this.getRight().getVariableNamesOfAttributes().contains(varName)) {
                    if (!isUsedInTargetGraph(this.getNACs(), varName)) {
                        if (!isUsedInTargetGraph(this.getPACs(), varName)) {
                            if (!isUsedInNestedGraphs(this.getNestedACs(), varName)) {
                                varTuple.getTupleType().deleteMemberAt(varName);
                                //							System.out.println("Rule.removeVariableOfAttrContext::  removed: "+var);
                                index--;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isUsedInTargetGraph(Iterator<OrdinaryMorphism> morphIterator, String variableName) {
        while (morphIterator.hasNext()) {
            Graph graph = morphIterator.next().getTarget();
            if (graph.getVariableNamesOfAttributes().contains(variableName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsedInNestedGraphs(Iterator<OrdinaryMorphism> morphIterator, String variableName) {
        while (morphIterator.hasNext()) {
            OrdinaryMorphism morphism = morphIterator.next();
            if (morphism.getTarget().getVariableNamesOfAttributes().contains(variableName)) {
                return true;
            }
            if (morphism instanceof NestedApplCond currentNestedApplCond) {
                var nestedAcList = new ArrayList<OrdinaryMorphism>();
                for (int index = 0; index < currentNestedApplCond.getNestedACs().size(); index++) {
                    nestedAcList.add(currentNestedApplCond.getNestedACs().get(index));
                }
                if (isUsedInNestedGraphs(nestedAcList.iterator(), variableName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets whether to wait before applying this rule.
     *
     * @param b true to enable waiting before apply, false to disable it
     */
    public void setWaitBeforeApplyEnabled(boolean b) {
        this.waitBeforeApply = b;
    }

    /**
     * Checks if waiting before apply is enabled for this rule.
     *
     * @return true if waiting before apply is enabled, false otherwise
     */
    public boolean isWaitBeforeApplyEnabled() {
        return this.waitBeforeApply;
    }

    /**
     * Implements the interface of XMLObject. Writes this rule to the specified XML helper.
     *
     * @param h the XML helper to write to
     */
    @Override
    public void XwriteObject(XMLHelper h) {
        this.changed = false;
        h.openNewElem("Rule", this);
        h.addAttr("name", this.itsName);
        if (!"".equals(this.formStr)) {
            h.addAttr("formula", this.formStr);
        }
        if (!this.enabled) {
            h.addAttr("enabled", "false");
        }
        if (this.triggerOfLayer) {
            h.addAttr("trigger", "true");
        }
        if (this.parallelMatching) {
            h.addAttr("parallel", "true");
        }
        if (this.startParallelMatchByFirstCSPVar) {
            h.addAttr("parallelByFirst", "true");
        }
        if (this.waitBeforeApply) {
            h.addAttr("waitBeforeApply", "true");
        }
        AttrContext attrContext = getAttrContext();
        h.addObject("", attrContext.getVariables(), true);
        getSource().setKind(GraphKind.LHS);
        h.addObject("", getSource(), true);
        getSource().setKind(GraphKind.RHS);
        h.addObject("", getTarget(), true);
//		String nameStr = this.getName();
        writeMorphism(h);
        // NACs
        Iterator<OrdinaryMorphism> nacIter = getNACs();
        // PACs
        Iterator<OrdinaryMorphism> pacIter = getPACs();
        // nested ACs
        Iterator<OrdinaryMorphism> acIter = getNestedACs();
        // Attr context conditions
        AttrConditionTuple condTuple = attrContext.getConditions();
        int condCount = condTuple.getNumberOfEntries();
        if (acIter.hasNext()
                || nacIter.hasNext()
                || pacIter.hasNext()
                || (condCount > 0)
                || (this.itsUsedAtomics != null && !this.itsUsedAtomics.isEmpty())) {
            h.openSubTag("ApplCondition");
            // NACs
            while (nacIter.hasNext()) {
                OrdinaryMorphism negativeApplCond = nacIter.next();
                negativeApplCond.getTarget().setKind(GraphKind.NAC);
                h.openSubTag("NAC");
                if (!negativeApplCond.isEnabled()) {
                    h.addAttr("enabled", "false");
                }
                h.addObject("", negativeApplCond.getTarget(), true);
                negativeApplCond.writeMorphism(h);
                h.close();
            }
            // PACs
            while (pacIter.hasNext()) {
                OrdinaryMorphism positiveApplCond = pacIter.next();
                positiveApplCond.getTarget().setKind(GraphKind.PAC);
                h.openSubTag("PAC");
                if (!positiveApplCond.isEnabled()) {
                    h.addAttr("enabled", "false");
                }
                h.addObject("", positiveApplCond.getTarget(), true);
                positiveApplCond.writeMorphism(h);
                h.close();
            }
            // nested ACs
            while (acIter.hasNext()) {
                OrdinaryMorphism nestedApplCond = acIter.next();
                nestedApplCond.getTarget().setKind(GraphKind.AC);
                h.openSubTag("NestedAC");
                if (!nestedApplCond.isEnabled()) {
                    h.addAttr("enabled", "false");
                }
                h.addObject("", nestedApplCond.getTarget(), true);
                nestedApplCond.writeMorphism(h);
                ((NestedApplCond) nestedApplCond).writeNestedApplConds(h);
                h.close();
            }
            // Attr context conditions
            if (condCount > 0) {
                h.openSubTag("AttrCondition");
                h.addObject("", condTuple, true);
                h.close();
            }
            // Post Application Constraints
            if ((this.itsUsedAtomics != null && !this.itsUsedAtomics.isEmpty())
                    && (this.itsUsedFormulas != null && !this.itsUsedFormulas.isEmpty())) {
                h.openSubTag("PostApplicationCondition");
                // save formulas
                for (int index = 0; index < this.itsUsedFormulas.size(); index++) {
                    Formula formula = this.itsUsedFormulas.get(index);
                    h.openSubTag("FormulaRef");
                    h.addObject("f", formula, false);
                    h.close();
                }
                h.close();
            }
            h.close(); // ApplCondition
        }
        // TaggedValue layer
        h.openSubTag("TaggedValue");
        h.addAttr("Tag", "layer");
        h.addAttr("TagValue", this.layer);
        h.close();
        // TaggedValue priority
        h.openSubTag("TaggedValue");
        h.addAttr("Tag", "priority");
        h.addAttr("TagValue", this.priority);
        h.close();
        h.close();
    }

    /**
     * Implements the interface of XMLObject. Reads this rule from the specified XML helper.
     *
     * @param h the XML helper to read from
     */
    @Override
    public void XreadObject(XMLHelper h) {
        if (h.isTag("Rule", this)) {
            String attrStr = h.readAttr("formula");
            if (!"".equals(attrStr)) {
                this.formStr = attrStr;
            } else {
                this.formStr = "true";
            }
            this.setTextualComment("Formula: ".concat(this.formStr));
            attrStr = h.readAttr("enabled");
            if (attrStr != null && attrStr.equals("false")) {
                this.enabled = false;
            } else {
                this.enabled = true;
            }
            attrStr = h.readAttr("trigger");
            if (attrStr != null && attrStr.equals("true")) {
                this.triggerOfLayer = true;
            } else {
                this.triggerOfLayer = false;
            }
            attrStr = h.readAttr("parallel");
            if (attrStr != null && attrStr.equals("true")) {
                this.parallelMatching = true;
            } else {
                this.parallelMatching = false;
            }
            attrStr = h.readAttr("parallelByFirst");
            if (attrStr != null && attrStr.equals("true")) {
                this.startParallelMatchByFirstCSPVar = true;
            } else {
                this.startParallelMatchByFirstCSPVar = false;
            }
            attrStr = h.readAttr("waitBeforeApply");
            if (attrStr != null && attrStr.equals("true")) {
                this.waitBeforeApply = true;
            } else {
                this.waitBeforeApply = false;
            }
            h.enrichObject(getAttrContext().getVariables());
            h.getObject("", getSource(), true);
            h.getObject("", getTarget(), true);
            readMorphism(h);
            this.itsOrig.setName("LeftOf_" + getName());
            this.itsOrig.setKind(GraphKind.LHS);
            this.itsOrig.setHelpInfo(this.getName());
            this.itsImag.setName("RightOf_" + getName());
            this.itsImag.setKind(GraphKind.RHS);
            this.itsImag.setHelpInfo(this.getName());
            var tempFormulas = new ArrayList<Formula>(); // of PostApplicationCondition
//			List<NestedApplCond> nacs = new ArrayList< >();
//			List<NestedApplCond> pacs = new ArrayList< >();
//			boolean needConvertToFormula = false;
            if (h.readSubTag("ApplCondition")) {
                while (h.readSubTag("NAC")) {
                    boolean negativeApplCondEnabled = true;
                    Object nacAttrEnabled = h.readAttr("enabled");
                    if ((nacAttrEnabled != null)
                            && ((String) nacAttrEnabled).equals("false")) {
                        negativeApplCondEnabled = false;
                    }
                    OrdinaryMorphism negativeApplCond = createNAC();
//					NestedApplCond nac = createNestedAC();
//					nacs.add(nac);
//					needConvertToFormula = true;
                    negativeApplCond.getTarget().setHelpInfo(this.getName());
                    negativeApplCond.getTarget().xyAttr = this.getLeft().xyAttr;
                    h.getObject("", negativeApplCond.getTarget(), true);
                    negativeApplCond.readMorphism(h);
                    h.close();
                    negativeApplCond.setEnabled(negativeApplCondEnabled);
                    negativeApplCond.getTarget().setHelpInfo("");
                    if (negativeApplCond.getName().isEmpty()) {
                        negativeApplCond.setName("nac".concat(String.valueOf(this.nacStrategie.getAcsListInternal().size())));
                    }
                }
                while (h.readSubTag("PAC")) {
                    boolean positiveApplCondEnabled = true;
                    Object pacAttrEnabled = h.readAttr("enabled");
                    if ((pacAttrEnabled != null)
                            && ((String) pacAttrEnabled).equals("false")) {
                        positiveApplCondEnabled = false;
                    }
                    OrdinaryMorphism positiveApplCond = createPAC();
//					NestedApplCond pac = createNestedAC();					
//					pacs.add(pac);
//					needConvertToFormula = true;
                    positiveApplCond.getTarget().setHelpInfo(this.getName());
                    positiveApplCond.getTarget().xyAttr = this.getLeft().xyAttr;
                    h.getObject("", positiveApplCond.getTarget(), true);
                    positiveApplCond.readMorphism(h);
                    h.close();
                    positiveApplCond.setEnabled(positiveApplCondEnabled);
                    positiveApplCond.getTarget().setHelpInfo("");
                    if (positiveApplCond.getName().isEmpty()) {
                        positiveApplCond.setName("pac".concat(String.valueOf(this.pacStrategie.getAcsListInternal().size())));
                    }
                }
                while (h.readSubTag("NestedAC")) {
//					needConvertToFormula = false;
                    boolean nestedApplCondEnabled = true;
                    Object acAttrEnabled = h.readAttr("enabled");
                    if ((acAttrEnabled != null)
                            && ((String) acAttrEnabled).equals("false")) {
                        nestedApplCondEnabled = false;
                    }
                    NestedApplCond nestedApplCond = createNestedAC();
                    nestedApplCond.getTarget().setHelpInfo(this.getName());
                    nestedApplCond.getTarget().xyAttr = this.getLeft().xyAttr;
                    h.getObject("", nestedApplCond.getTarget(), true);
                    nestedApplCond.readMorphism(h);
                    nestedApplCond.readNestedApplConds(h);
                    h.close();
                    nestedApplCond.setEnabled(nestedApplCondEnabled);
                    nestedApplCond.getTarget().setHelpInfo("");
                    if (nestedApplCond.getName().isEmpty()) {
                        nestedApplCond.setName("gac".concat(String.valueOf(this.acStrategie.getAcsListInternal().size())));
                    }
                }
                if (h.readSubTag("AttrCondition")) {
                    AttrConditionTuple condTuple = getAttrContext().getConditions();
                    if (condTuple != null) {
                        h.enrichObject(condTuple);
                    }
                    h.close();
                }
                // read Post Application Constraints
                if (h.readSubTag("PostApplicationCondition")) {
                    // System.out.println("PostApplicationCondition");
                    // read formulas
                    while (h.readSubTag("FormulaRef")) {
                        Formula formula = new Formula(true);
                        formula.setName("");
                        Formula readFormula = (Formula) h.getObject("f", null, false);
                        // System.out.println("Formula: "+readFormula);
                        if (readFormula != null) {
                            tempFormulas.add(readFormula);
                        }
                        h.close();
                    }
                    h.close();
                    // generatePostConstraints = true;
//					setUsedFormulas(tmpFormulas);
//					convertUsedFormulas();
                }
                h.close();
            }
            // read layer
            if (h.readSubTag("TaggedValue")) {
                int v = 0;
                String t = h.readAttr("Tag");
                // read old attribute
                int v1 = h.readIAttr("Value");
                // read new attribute
                int v2 = h.readIAttr("TagValue");
                if (v1 > 0) {
                    v = v1;
                } else if (v2 > 0) {
                    v = v2;
                }
                if (t.equals("layer")) {
                    this.layer = v;
                }
                h.close();
            }
            // read priority
            if (h.readSubTag("TaggedValue")) {
                int v = 0;
                String t = h.readAttr("Tag");
                // read old attribute
                int v1 = h.readIAttr("Value");
                // read new attribute
                int v2 = h.readIAttr("TagValue");
                if (v1 > 0) {
                    v = v1;
                } else if (v2 > 0) {
                    v = v2;
                }
                if (t.equals("priority")) {
                    this.priority = v;
                }
                h.close();
            }

            h.close();
            this.applicable = true;
            setUsedFormulas(tempFormulas);
            this.itsOrig.setHelpInfo("");
            this.itsImag.setHelpInfo("");
//			if ( needConvertToFormula && "true".equals(this.formStr)) {
//				convertToFormula(pacs, nacs);
//			} 
//			else 
            this.setFormula(this.formStr);
        }
    }

    @SuppressWarnings("unused")
    private boolean convertToFormula(
            final List<NestedApplCond> pacs,
            final List<NestedApplCond> nacs) {
        final List<Evaluable> vars = new ArrayList<>(this.acStrategie.getAcsListInternal().size());
        if (this.acStrategie.getAcsListInternal().isEmpty()) {
            this.formStr = "true";
            this.formReadStr = this.formStr;
            return true;
        }
        String tmp = "";
        int indx = -1;
        for (int i = 0; i < pacs.size(); i++) {
            NestedApplCond applicationCondition = pacs.get(i);
            if (applicationCondition.isEnabled()) {
                indx++;
                vars.add(applicationCondition);
                if (vars.size() == 1) {
                    tmp = tmp.concat(String.valueOf(indx + 1));
                } else {
                    tmp = tmp.concat("&").concat(String.valueOf(indx + 1));
                }
            }
        }
        for (int i = 0; i < nacs.size(); i++) {
            NestedApplCond applicationCondition = nacs.get(i);
            if (applicationCondition.isEnabled()) {
                indx++;
                vars.add(applicationCondition);
                if (vars.size() == 1) {
                    tmp = tmp.concat("!".concat(String.valueOf(indx + 1)));
                } else {
                    tmp = tmp.concat("&!").concat(String.valueOf(indx + 1));
                }
            }
        }
        if ("".equals(tmp)) {
            this.formStr = "true";
            this.formReadStr = this.formStr;
            return true;
        }
        if (this.itsFormula.setFormula(vars, tmp)) {
            this.formStr = this.itsFormula.getAsString(vars);
            this.formReadStr = this.formStr;
//			System.out.println("Rule: "+this.getName()+"   formula: "+this.formStr);
            return true;
        }
        return false;
    }

    // ------ additional methods according to Gabi's new AGG design ---------
    // ----------- attention: yet untested! (SG, Aug.1999) ------------------
    /**
     * Returns an inverted rule. This rule has to be injective, otherwise returns null. The attribute mappings are NOT
     * inverted, thus the resulting left and right-hand side graphs are not attributed anymore.
     *
     * @return the inverted rule, or null if this rule is not injective
     */
    public Rule invertSimplex() {
        if (!this.isInjective()) {
            return null;
        }
        Rule inverseRule = new Rule();
        Graph leftGraph = this.getLeft();
        Graph rightGraph = this.getRight();
        Graph leftInverse = inverseRule.getLeft();
        Graph rightInverse = inverseRule.getRight();
        OrdinaryMorphism leftMorphism = new OrdinaryMorphism(leftGraph, rightInverse);
        OrdinaryMorphism rightMorphism = new OrdinaryMorphism(rightGraph, leftInverse);
        Iterator<Node> rightNodes = rightGraph.getNodesSet().iterator();
        while (rightNodes.hasNext()) {
            Node rightNode = rightNodes.next();
            Node leftInverseNode = null;
            try {
                leftInverseNode = leftInverse.createNode(rightNode.getType());
            } catch (TypeException e) {
                // if the old rule was well typed, the new
                // rule should be also well typed
                e.printStackTrace();
            }
            rightMorphism.addMapping(rightNode, leftInverseNode);
        }
        Iterator<Node> leftNodeIter = leftGraph.getNodesSet().iterator();
        while (leftNodeIter.hasNext()) {
            Node leftNode = leftNodeIter.next();
            Node rightInverseNode = null;
            try {
                rightInverseNode = rightInverse.createNode(leftNode.getType());
            } catch (TypeException e) {
                // if the old rule was well typed, the new
                // rule should be also well typed
                e.printStackTrace();
            }
            leftMorphism.addMapping(leftNode, rightInverseNode);
            GraphObject rightNodeObj = this.getImage(leftNode);
            if (rightNodeObj != null) {
                inverseRule.addMapping(rightMorphism.getImage(rightNodeObj), rightInverseNode);
            }
        }
        Iterator<Arc> rightArcIter = rightGraph.getArcsSet().iterator();
        while (rightArcIter.hasNext()) {
            Arc rightArc = rightArcIter.next();
            Node leftInverseSource = (Node) rightMorphism.getImage(rightArc.getSource());
            Node leftInverseTarget = (Node) rightMorphism.getImage(rightArc.getTarget());
            Arc leftInverseArc = null;
            try {
                leftInverseArc = leftInverse.createArc(rightArc.getType(),
                        leftInverseSource, leftInverseTarget);
                rightMorphism.addMapping(rightArc, leftInverseArc);
            } catch (TypeException ex) {
                // Intentionally empty
            }
        }
        Iterator<Arc> leftArcIter = leftGraph.getArcsSet().iterator();
        while (leftArcIter.hasNext()) {
            Arc leftArc = leftArcIter.next();
            Node rightInverseSource = (Node) leftMorphism.getImage(leftArc.getSource());
            Node rightInverseTarget = (Node) leftMorphism.getImage(leftArc.getTarget());
            Arc rightInverseArc = null;
            try {
                rightInverseArc = rightInverse.createArc(leftArc.getType(),
                        rightInverseSource, rightInverseTarget);
                leftMorphism.addMapping(leftArc, rightInverseArc);
            } catch (TypeException ex) {
                // Intentionally empty
            }
            GraphObject rightArcObj = this.getImage(leftArc);
            if (rightArcObj != null) {
                inverseRule.addMapping(rightMorphism.getImage(rightArcObj), rightInverseArc);
            }
        }
        return inverseRule;
    }

    /**
     * Tries to invert this rule. The rule has to be injective. The attribute mappings are NOT inverted, thus the
     * resulting left and right-hand side graphs are not attributed anymore.
     * <p>
     * Returns the pair with an inverted rule as the first element and a help pair of two graph morphisms as the second
     * element. The first morphism is between the LHS of this and the RHS of the inverted rule, the second morphism is
     * between the RHS of this and the LHS of the inverted rule. If this rule is not injective, returns null.
     *
     * @return a pair containing the inverted rule and morphism information, or null if not injective
     */
    public Pair<Rule, Pair<OrdinaryMorphism, OrdinaryMorphism>> invertComplex() {
        if (!this.isInjective()) {
            return null;
        }
        Rule inverseRule = new Rule();
        Graph leftGraph = this.getLeft();
        Graph rightGraph = this.getRight();
        Graph leftInverse = inverseRule.getLeft();
        Graph rightInverse = inverseRule.getRight();
        OrdinaryMorphism leftMorphism = new OrdinaryMorphism(leftGraph, rightInverse);
        OrdinaryMorphism rightMorphism = new OrdinaryMorphism(rightGraph, leftInverse);
        Iterator<Node> rightNodes = rightGraph.getNodesSet().iterator();
        while (rightNodes.hasNext()) {
            Node rightNode = rightNodes.next();
            Node leftInverseNode = null;
            try {
                leftInverseNode = leftInverse.createNode(rightNode.getType());
            } catch (TypeException e) {
                // if the old rule was well typed, the new
                // rule should be also well typed
                e.printStackTrace();
            }
            rightMorphism.addMapping(rightNode, leftInverseNode);
        }
        Iterator<Node> leftNodeIter = leftGraph.getNodesSet().iterator();
        while (leftNodeIter.hasNext()) {
            Node leftNode = leftNodeIter.next();
            Node rightInverseNode = null;
            try {
                rightInverseNode = rightInverse.createNode(leftNode.getType());
            } catch (TypeException e) {
                // if the old rule was well typed, the new
                // rule should be also well typed
                e.printStackTrace();
            }
            leftMorphism.addMapping(leftNode, rightInverseNode);
            GraphObject rightNodeObj = this.getImage(leftNode);
            if (rightNodeObj != null) {
                inverseRule.addMapping(rightMorphism.getImage(rightNodeObj), rightInverseNode);
            }
        }
        Iterator<Arc> rightArcIter = rightGraph.getArcsSet().iterator();
        while (rightArcIter.hasNext()) {
            Arc rightArc = rightArcIter.next();
            Node leftInverseSource = (Node) rightMorphism.getImage(rightArc.getSource());
            Node leftInverseTarget = (Node) rightMorphism.getImage(rightArc.getTarget());
            Arc leftInverseArc = null;
            try {
                leftInverseArc = leftInverse.createArc(rightArc.getType(),
                        leftInverseSource, leftInverseTarget);
                rightMorphism.addMapping(rightArc, leftInverseArc);
            } catch (TypeException ex) {
                // Intentionally empty
            }
        }
        Iterator<Arc> leftArcIter = leftGraph.getArcsSet().iterator();
        while (leftArcIter.hasNext()) {
            Arc leftArc = leftArcIter.next();
            Node rightInverseSource = (Node) leftMorphism.getImage(leftArc.getSource());
            Node rightInverseTarget = (Node) leftMorphism.getImage(leftArc.getTarget());
            Arc rightInverseArc = null;
            try {
                rightInverseArc = rightInverse.createArc(leftArc.getType(),
                        rightInverseSource, rightInverseTarget);
                leftMorphism.addMapping(leftArc, rightInverseArc);
            } catch (TypeException ex) {
                // Intentionally empty
            }
            GraphObject rightArcObj = this.getImage(leftArc);
            if (rightArcObj != null) {
                inverseRule.addMapping(rightMorphism.getImage(rightArcObj), rightInverseArc);
            }
        }
        Pair<OrdinaryMorphism, OrdinaryMorphism> infoPair = new Pair<OrdinaryMorphism, OrdinaryMorphism>(
                leftMorphism, rightMorphism);
        return new Pair<Rule, Pair<OrdinaryMorphism, OrdinaryMorphism>>(
                inverseRule, infoPair);
    }

    /**
     * Returns the rule scheme for this rule. A plain rule returns null. Its subclasses <code>KernelRule</code>,
     * <code>MultiRule</code>, <code>RuleScheme</code>, <code>AmalgamatedRule</code> override this method to return its
     * <code>RuleScheme</code>.
     *
     * @return the rule scheme, or null for plain rules
     */
    public RuleScheme getRuleScheme() {
        return null;
    }

    /**
     * Returns the current match for this rule.
     *
     * @return the current match, or null if no match exists
     */
    public Match getMatch() {
        return this.itsMatch;
    }

    /**
     * Compares attribute value of the specified objects due to constant value of the first object. Failed attribute
     * value of the second object will be unset. Checks all members of the attribute tuple.
     *
     * @param src first object (an object of the LHS of a rule)
     * @param tgt second object (an object of a NAC, PAC of a rule)
     * @return true if attribute value is equal, otherwise false
     */
    public boolean compareConstantAttributeValue(
            final GraphObject sourceObj,
            final GraphObject targetObj) {
        boolean result = true;
        if (sourceObj.getAttribute() != null
                && targetObj.getAttribute() != null) {
            final ValueTuple targetValueTuple = (ValueTuple) targetObj.getAttribute();
            final ValueTuple sourceValueTuple = (ValueTuple) sourceObj.getAttribute();
            for (int index = 0; index < sourceValueTuple.getNumberOfEntries(); index++) {
                final ValueMember leftHandSideValueMem = sourceValueTuple.getValueMemberAt(index);
                final ValueMember targetValueMem = targetValueTuple.getValueMemberAt(leftHandSideValueMem.getName());
                if (leftHandSideValueMem.isSet()
                        && leftHandSideValueMem.getExpr().isConstant()
                        && targetValueMem != null && targetValueMem.isSet()
                        && !leftHandSideValueMem.getExprAsText().equals(targetValueMem.getExprAsText())) {
                    result = false;
                    targetValueMem.setExpr(null);
                }
            }
        }
        return result;
    }

    /**
     * Compares attribute value of the specified objects due to constant value of the first object. Failed attribute
     * value of the second object will be unset. The check breaks after at least one attribute failed.
     *
     * @param sourceObj first object (an object of the LHS of a rule)
     * @param targetObj second object (an object of a NAC, PAC of a rule)
     * @return true if attribute value is equal, otherwise false
     */
    public boolean compareConstAttrValueOfMapObjs(
            final GraphObject sourceObj, final GraphObject targetObj) {
        if (sourceObj.getAttribute() != null
                && targetObj.getAttribute() != null) {
            final ValueTuple targetValueTuple = (ValueTuple) targetObj.getAttribute();
            final ValueTuple sourceValueTuple = (ValueTuple) sourceObj.getAttribute();
            for (int index = 0; index < sourceValueTuple.getNumberOfEntries(); index++) {
                final ValueMember sourceValueMem = sourceValueTuple.getValueMemberAt(index);
                final ValueMember targetValueMem = targetValueTuple.getValueMemberAt(sourceValueMem.getName());
                if (sourceValueMem.isSet()
                        && sourceValueMem.getExpr().isConstant()
                        && targetValueMem.isSet()
                        && !sourceValueMem.getExprAsText().equals(targetValueMem.getExprAsText())) {
                    targetValueMem.setExpr(null);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compares its LHS, RHS, morphism, NACs, PACs and attribute context to the appropriate elements of the specified
     * rule. Returns true if all elements are equal.
     *
     * @param otherRule the rule to compare to
     * @return true if all elements are equal, false otherwise
     */
    public boolean compareTo(Rule otherRule) {
        // System.out.println("Rule.compareTo");
        Pair<Boolean, String> errorMsgHolder = null;
        // compare rule morphism
        if (!((OrdinaryMorphism) this).compareTo(otherRule)) {
            // System.out.println("Rule: "+getName()+" :: Mapping failed!");
            errorMsgHolder = new Pair<>(true,
                    "Rule content is different.");
            return false;
        }
        // compare NACs
        errorMsgHolder = compareApplConds(this.getNACsList(), otherRule.getNACsList(), "NAC");
        if (errorMsgHolder != null) {
            return false;
        }
        // compare PACs
        errorMsgHolder = compareApplConds(this.getPACsList(), otherRule.getPACsList(), "PAC");
        if (errorMsgHolder != null) {
            return false;
        }
        // compare nested ACs
        errorMsgHolder = compareApplConds(this.getNestedACsList(), otherRule.getNestedACsList(), "nested AC");
        if (errorMsgHolder != null) {
            return false;
        }
        // compare rule context
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        VarTuple otherVarTuple = (VarTuple) otherRule.getAttrContext().getVariables();
        if (!varTuple.compareTo(otherVarTuple)) {
            errorMsgHolder = new Pair<>(true,
                    "Variable rule context is different.");
            return false;
        }
        CondTuple condTuple = (CondTuple) getAttrContext().getConditions();
        CondTuple otherCondTuple = (CondTuple) otherRule.getAttrContext().getConditions();
        if (!condTuple.compareTo(otherCondTuple)) {
            errorMsgHolder = new Pair<>(true,
                    "Conditional rule context is different.");
            return false;
        }
        return true;
    }

    private Pair<Boolean, String> compareApplConds(
            final List<OrdinaryMorphism> applConds,
            final List<OrdinaryMorphism> otherApplConds,
            String applCondType) {
        // compare ACs
        List<OrdinaryMorphism> otherList = new ArrayList<>();
        otherList.addAll(otherApplConds);
        if (applConds.size() != otherList.size()) {
            // System.out.println("Rule: "+getName()+" NACs discrepancy!");
            Pair<Boolean, String> errorMsgHolder = new Pair<>(
                    true,
                    "Number of " + applCondType + "s is different.");
            return errorMsgHolder;
        }
        OrdinaryMorphism currentApplCond = null;
        for (int index = 0; index < applConds.size(); index++) {
            currentApplCond = applConds.get(index);
            for (int otherIndex = otherList.size() - 1; otherIndex >= 0; otherIndex--) {
                OrdinaryMorphism otherApplCond = otherList.get(otherIndex);
                if (currentApplCond.compareTo(otherApplCond)) {
                    otherList.remove(otherApplCond);
                    break;
                }
            }
        }
        if (!otherList.isEmpty() && currentApplCond != null) {
            Pair<Boolean, String> errorMsgHolder = new Pair<>(
                    true,
                    applCondType + ":  " + currentApplCond.getName() + "  is different.");
            return errorMsgHolder;
        }
        return null;
    }

    /**
     * Returns all graph objects from the left-hand side graph that use the specified input parameters.
     *
     * @param inputParams the list of input parameter names to filter by
     * @return a list of graph objects from the LHS that use the specified input parameters
     */
    public List<GraphObject> getInputParameterObjectsLeft(final List<String> inputParams) {
        return getInputParameterObjects(this.getLeft(), inputParams);
    }

    /**
     * Returns all graph objects from the right-hand side graph that use the specified input parameters.
     *
     * @param inputParams the list of input parameter names to filter by
     * @return a list of graph objects from the RHS that use the specified input parameters
     */
    public List<GraphObject> getInputParameterObjectsRight(final List<String> inputParams) {
        return getInputParameterObjects(this.getRight(), inputParams);
    }

    private List<GraphObject> getInputParameterObjects(final Graph graph, final List<String> inputParamNames) {
        var graphObjWithInputParam = new ArrayList<GraphObject>();
        Iterator<GraphObject> elemIter = graph.iteratorOfElems();
        while (elemIter.hasNext()) {
            GraphObject currentObj = elemIter.next();
            if (currentObj.getAttribute() != null) {
                ValueTuple valueTuple = (ValueTuple) currentObj.getAttribute();
                for (int index = 0; index < valueTuple.getNumberOfEntries(); index++) {
                    ValueMember valueMem = valueTuple.getEntryAt(index);
                    if (valueMem.isSet() && valueMem.getExpr().isVariable()) {
                        if (inputParamNames.contains(valueMem.getExprAsText())) {
                            graphObjWithInputParam.add(currentObj);
                        }
                    }
                }
            }
        }
        return graphObjWithInputParam;
    }

    /**
     * Returns all graph objects from the left-hand side graph that use any input parameter.
     *
     * @return a list of graph objects from the LHS that use input parameters
     */
    public List<GraphObject> getLeftInputParameterObjects() {
        var resultList = new ArrayList<GraphObject>();
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        Iterator<GraphObject> elemIter = this.itsOrig.iteratorOfElems();
        while (elemIter.hasNext()) {
            GraphObject currentObj = elemIter.next();
            if (currentObj.getAttribute() != null) {
                ValueTuple valueTuple = (ValueTuple) currentObj.getAttribute();
                for (int index = 0; index < valueTuple.getNumberOfEntries(); index++) {
                    ValueMember valueMem = valueTuple.getValueMemberAt(index);
                    if (valueMem.isSet() && valueMem.getExpr().isVariable()) {
                        if (varTuple.getVarMemberAt(valueMem.getExprAsText()) != null
                                && varTuple.getVarMemberAt(valueMem.getExprAsText()).isInputParameter()) {
                            resultList.add(currentObj);
                        }
                    }
                }
            }
        }
//		System.out.println(resultList);
        return resultList;
    }

    /**
     * Returns all graph objects from the right-hand side graph that use any input parameter.
     *
     * @return a list of graph objects from the RHS that use input parameters
     */
    public List<GraphObject> getRightInputParameterObjects() {
        var resultList = new ArrayList<GraphObject>();
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        Iterator<GraphObject> elemIter = this.itsImag.iteratorOfElems();
        while (elemIter.hasNext()) {
            GraphObject currentObj = elemIter.next();
            if (currentObj.getAttribute() != null) {
                ValueTuple valueTuple = (ValueTuple) currentObj.getAttribute();
                for (int index = 0; index < valueTuple.getNumberOfEntries(); index++) {
                    ValueMember valueMem = valueTuple.getValueMemberAt(index);
                    if (valueMem.isSet() && valueMem.getExpr().isVariable()) {
                        if (varTuple.getVarMemberAt(valueMem.getExprAsText()) != null
                                && varTuple.getVarMemberAt(valueMem.getExprAsText()).isInputParameter()) {
                            resultList.add(currentObj);
                        }
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * Returns the names of all input parameters in this rule's attribute context.
     *
     * @return a list of input parameter names
     */
    public List<String> getInputParameterNames() {
        var inputParamNames = new ArrayList<String>(1);
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        for (int index = 0; index < varTuple.getNumberOfEntries(); index++) {
            VarMember varMem = varTuple.getVarMemberAt(index);
            if (varMem.isInputParameter()) {
                inputParamNames.add(varMem.getName());
            }
        }
        return inputParamNames;
    }

    /**
     * Returns variables of the attribute context of this rule which are used as input parameter for the rule
     * application.
     *
     * @return a list of input parameter variable members
     */
    public List<VarMember> getInputParameters() {
        var inputParams = new ArrayList<VarMember>(1);
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        for (int index = 0; index < varTuple.getNumberOfEntries(); index++) {
            VarMember varMem = varTuple.getVarMemberAt(index);
            if (varMem.isInputParameter()) {
                inputParams.add(varMem);
            }
        }
        return inputParams;
    }

    /**
     * Returns input parameters from the attribute context that are marked for the left-hand side.
     *
     * @return a list of input parameter variable members marked for LHS
     */
    public List<VarMember> getInputParametersLeft() {
        var inputParams = new ArrayList<VarMember>(1);
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        for (int index = 0; index < varTuple.getNumberOfEntries(); index++) {
            VarMember varMem = varTuple.getVarMemberAt(index);
            if (varMem.isInputParameter()
                    && varMem.getMark() == VarMember.LHS) {
                inputParams.add(varMem);
            }
        }
        return inputParams;
    }

    /**
     * Returns input parameters from the attribute context that are marked for the right-hand side or NACs.
     *
     * @return a list of input parameter variable members marked for RHS or NAC
     */
    public List<VarMember> getInputParametersRight() {
        var inputParams = new ArrayList<VarMember>(1);
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        for (int index = 0; index < varTuple.getNumberOfEntries(); index++) {
            VarMember varMem = varTuple.getVarMemberAt(index);
            if (varMem.isInputParameter()
                    && (varMem.getMark() == VarMember.RHS
                    || varMem.getMark() == VarMember.NAC)) {
                inputParams.add(varMem);
            }
        }
        return inputParams;
    }

    /**
     * Returns variables of the attribute context of this rule which are used by attributes of the specified graph
     * object as an input parameter for the rule application.
     *
     * @param go the graph object to check
     * @param var the variable tuple containing the variables to check
     * @return a list of input parameter variable members used by the graph object's attributes
     */
    public List<VarMember> getInputParametersOfGraphObject(final GraphObject graphObj, final VarTuple varTuple) {
        if (graphObj.getAttribute() == null) {
            return new ArrayList<>();
        }
        var inputParams = new ArrayList<VarMember>(1);
        ValueTuple attrVal = (ValueTuple) graphObj.getAttribute();
        for (int index = 0; index < attrVal.getNumberOfEntries(); index++) {
            ValueMember valueMem = attrVal.getValueMemberAt(index);
            if (valueMem.isSet() && valueMem.getExpr().isVariable()) {
                VarMember varMem = varTuple.getVarMemberAt(valueMem.getExprAsText());
                if (varMem != null && varMem.isInputParameter()) {
                    inputParams.add(varMem);
                }
            }
        }
        return inputParams;
    }

    /**
     * Returns all non-input parameters used by newly created graph objects in the RHS. Newly created objects are those
     * that have no preimage in the LHS.
     *
     * @return a list of non-input parameter variable members used by new graph objects
     */
    public List<VarMember> getNonInputParametersOfNewGraphObjects() {
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        var params = new ArrayList<VarMember>(1);
        final Iterator<GraphObject> objIter = this.itsImag.iteratorOfElems();
        while (objIter.hasNext()) {
            GraphObject currentObj = objIter.next();
            if (currentObj.getAttribute() == null
                    || this.itsCodomObjects.contains(currentObj)) {
                continue;
            }
            ValueTuple attrVal = (ValueTuple) currentObj.getAttribute();
            for (int index = 0; index < attrVal.getNumberOfEntries(); index++) {
                ValueMember valueMem = attrVal.getValueMemberAt(index);
                if (valueMem.isSet() && valueMem.getExpr().isVariable()) {
                    VarMember varMem = varTuple.getVarMemberAt(valueMem.getExprAsText());
                    if (varMem != null && !varMem.isInputParameter()) {
                        params.add(varMem);
                    }
                }
            }
        }
        return params;
    }

    /**
     * Returns all non-input parameters from this rule's attribute context.
     *
     * @return a list of all variable members that are not input parameters
     */
    public List<VarMember> getNonInputParameters() {
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        var params = new ArrayList<VarMember>(1);
        for (int index = 0; index < varTuple.getNumberOfEntries(); index++) {
            VarMember varMem = varTuple.getVarMemberAt(index);
            if (!varMem.isInputParameter()) {
                params.add(varMem);
            }
        }
        return params;
    }

    /**
     * Checks if all NACs in this rule are valid. Currently always returns true as it is not yet fully implemented.
     *
     * @return true if all NACs are valid, false otherwise
     */
    public boolean areNACsValid() {
//		for (int i = 0; i < itsNACs.size(); i++) {
//			OrdinaryMorphism nac = itsNACs.get(i);
//			if (!isNACValid(nac) {
//				return false;
//			}
//		}
        return true;
    }
//	public boolean isGlobalNAC(OrdinaryMorphism nac) {
//		return nac.isEmpty();		
//	}
//	
//	public boolean isGlobalPAC(OrdinaryMorphism pac) {
//		return pac.isEmpty();		
//	}

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
     * Checks if shifting of an application condition is possible. Shift is not possible when it may cause a dangling
     * edge.
     *
     * @param ac the application condition to check
     * @return true if the AC can be shifted, false otherwise
     */
    public boolean isACShiftPossible(OrdinaryMorphism ac) {
        Iterator<Arc> arcs = ac.getTarget().getArcsCollection().iterator();
        while (arcs.hasNext()) {
            Arc a = arcs.next();
            if (!ac.getInverseImage(a).hasNext()) {
                if (ac.getInverseImage(a.getSource()).hasNext()) {
                    if (this.getImage(ac.getInverseImage(a.getSource()).next()) == null) {
                        return false;
                    }
                }
                if (ac.getInverseImage(a.getTarget()).hasNext()) {
                    if (this.getImage(ac.getInverseImage(a.getTarget()).next()) == null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks dangling edges of the given PAC. Returns true if no dangling edge exists, otherwise false.
     *
     * @param ac the positive application condition to validate
     * @return true if the PAC has no dangling edges, false otherwise
     */
    public boolean isPACValid(OrdinaryMorphism ac) {
        if (ac.isEnabled()) {
            final Iterator<Node> objects = this.itsOrig.getNodesSet().iterator();
            while (objects.hasNext()) {
                final Node x = objects.next();
                if (this.getImage(x) == null) {
                    final Node y = (Node) ac.getImage(x);
                    if (y != null
                            && x.getNumberOfArcs() != y.getNumberOfArcs()) {
                        this.setErrorMsg(ac.getName() + "  -  PAC failed (dangling edge)");
//						this.setErrorMsg(this.getName()+":    "+ac.getName()+"  -  PAC failed (dangling edge)");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks dangling edges of all PACs in this rule. Returns true if no dangling edge exists, otherwise false.
     *
     * @return true if all PACs have no dangling edges, false otherwise
     */
    public boolean arePACsValid() {
        return this.pacStrategie.areAcsValid();
    }

    /**
     * Checks dangling edges of the given nested application condition. Returns true if no dangling edge exists,
     * otherwise false.
     *
     * @param ac the nested application condition to validate
     * @return true if the GAC has no dangling edges, false otherwise
     */
    public boolean isGACValid(NestedApplCond ac) {
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
    public boolean areGACsValid() {
        return this.acStrategie.areAcsValid();
    }

    /**
     * Checks dangling edges of all PACs and nested ACs in this rule. Returns true if no dangling edge exists, otherwise
     * false.
     *
     * @return true if all application conditions have no dangling edges, false otherwise
     */
    public boolean areApplCondsValid() {
        if (!this.pacStrategie.areAcsValid()) {
            return false;
        }
        return this.acStrategie.areAcsValid();
    }

    /**
     * Copies nodes and edges of its PACs in the LHS resp. RHS and extends the rule mapping. The PACs will be disabled.
     *
     * @return true if the extension was successful, false otherwise
     */
    public boolean extendByPacs() {
        return this.pacStrategie.extendByAcs();
    }

    /**
     * Undo the copy of its PACs done by <code>extendByPacs</code>. The PACs will be enabled.
     */
    public boolean extendByPacsUndo() {
        return this.pacStrategie.extendByAcsUndo();
    }

    /**
     * Checks existing variables of the attribute context against the attribute context of its current match and adjusts
     * the attribute context of its match, if needed.
     */
    public void adjustAttrContextOfMatch(boolean inputParameterOnly) {
        if (this.itsMatch != null) {
            this.itsMatch.adjustAttrInputParameter(inputParameterOnly);
            this.itsMatch.adjustAttrCondition();
        }
    }

    /**
     * Set the value of variables of rule attribute context to null.
     */
    public void unsetValueOfContextVariable(boolean inputParameterOnly) {
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        for (int index = 0; index < varTuple.getNumberOfEntries(); index++) {
            VarMember varMem = varTuple.getVarMemberAt(index);
            if (inputParameterOnly) {
                if (varMem.isInputParameter()) {
                    varMem.setExpr(null);
                }
            } else {
                varMem.setExpr(null);
            }
        }
    }

    /**
     * Attribute context variable which is an input parameter is no more input parameter after this method applied.
     */
    public void unsetInputParameter() {
        AttrVariableTuple attrVariableTuple = getAttrContext().getVariables();
        for (int index = 0; index < attrVariableTuple.getNumberOfEntries(); index++) {
            VarMember varMem = (VarMember) attrVariableTuple.getMemberAt(index);
            if (varMem.isInputParameter()) {
                varMem.setInputParameter(false);
            }
        }
    }

    /**
     * Returns the name of an input parameter without value, otherwise null.
     *
     * @return the name of the first unset input parameter, or null if all are set
     */
    public String getInputParameterWithoutValue() {
        AttrVariableTuple attrVariableTuple = getAttrContext().getVariables();
        for (int index = 0; index < attrVariableTuple.getNumberOfEntries(); index++) {
            VarMember varMem = (VarMember) attrVariableTuple.getMemberAt(index);
            if (varMem.isInputParameter() && !varMem.isSet()) {
                return varMem.getName();
            }
        }
        return null;
    }

    /**
     * Returns the name of an unset input parameter variable from the attribute context. If the specified parameter is
     * true, the LHS (NACs, PACs) with an input parameter for matching are taken into account. If false, the RHS (NACs,
     * PACs) with an input parameter for matching are taken into account.
     *
     * @param left true to check LHS, false to check RHS
     * @return the name of the first unset input parameter in the specified context, or null if all are set
     */
    public String getInputParameterWithoutValue(boolean left) {
        AttrVariableTuple attrVariableTuple = getAttrContext().getVariables();
        for (int index = 0; index < attrVariableTuple.getNumberOfEntries(); index++) {
            VarMember varMem = (VarMember) attrVariableTuple.getMemberAt(index);
            if (varMem.isInputParameter() && !varMem.isSet()) {
                if (left) {
                    List<String> varNames = getLeft().getVariableNamesOfAttributes();
                    for (int nameIndex = 0; nameIndex < varNames.size(); nameIndex++) {
                        if (varNames.get(nameIndex).equals(varMem.getName())) {
                            return varMem.getName();
                        }
                    }
                } else {
                    List<String> varNames = getRight().getVariableNamesOfAttributes();
                    for (int nameIndex = 0; nameIndex < varNames.size(); nameIndex++) {
                        if (varNames.get(nameIndex).equals(varMem.getName())) {
                            return varMem.getName();
                        }
                    }
                }
                for (OrdinaryMorphism negativeApplCond : this.nacStrategie.getAcsListInternal()) {
                    List<String> varNames = negativeApplCond.getTarget()
                            .getVariableNamesOfAttributes();
                    for (int nameIndex = 0; nameIndex < varNames.size(); nameIndex++) {
                        if (varNames.get(nameIndex).equals(varMem.getName())) {
                            return varMem.getName();
                        }
                    }
                }
                for (OrdinaryMorphism positiveApplCond : this.pacStrategie.getAcsListInternal()) {
                    List<String> varNames = positiveApplCond.getTarget()
                            .getVariableNamesOfAttributes();
                    for (int nameIndex = 0; nameIndex < varNames.size(); nameIndex++) {
                        if (varNames.get(nameIndex).equals(varMem.getName())) {
                            return varMem.getName();
                        }
                    }
                }
                for (OrdinaryMorphism nestedApplCond : this.acStrategie.getAcsListInternal()) {
                    List<String> varNames = nestedApplCond.getTarget()
                            .getVariableNamesOfAttributes();
                    for (int nameIndex = 0; nameIndex < varNames.size(); nameIndex++) {
                        if (varNames.get(nameIndex).equals(varMem.getName())) {
                            return varMem.getName();
                        }
                    }
                }
            }
        }
        return null;
    }

    private void deleteUnusedVars(List<VarMember> used) {
        VarTuple varTuple = (VarTuple) this.getAttrContext().getVariables();
        for (int index = 0; index < varTuple.getNumberOfEntries(); index++) {
            VarMember varMem = varTuple.getVarMemberAt(index);
            if (!used.contains(varMem)) {
                varTuple.getTupleType().deleteMemberAt(varMem.getName());
//				varTuple.showVariables();
            }
        }
    }

    /**
     * Checks attribute setting of RHS, variable declarations and attribute conditions. If all checks successful, it
     * prepares infos about this rule. The method getErrorMessage() gives more information about fails.
     */
    public boolean isReadyToTransform() {
        this.isReady = true;
        if (!this.enabled) {
            return true;
        }
        // check usage of abstract types of the RHS
        final var abstractTypesOfRHS = new ArrayList<String>(1);
        Iterator<Node> nodeIter = this.itsImag.getNodesSet().iterator();
        while (nodeIter.hasNext()) {
            GraphObject graphObj = nodeIter.next();
            Iterator<GraphObject> inverseIter = getInverseImage(graphObj);
            if (!inverseIter.hasNext() && graphObj.getType().isAbstract()) {
                abstractTypesOfRHS.add(graphObj.getType().getName());
            }
        }
        this.isReady = abstractTypesOfRHS.isEmpty();
        if (!this.isReady) {
            this.errorMsg = this.errorMsg.concat("RHS: creating abstract nodes not allowed!  ").concat(abstractTypesOfRHS.toString());
            return false;
        }
        // check  PAC is valid: check dangling edge of nodes to delete which are used in a PAC
        for (OrdinaryMorphism pac : this.pacStrategie.getAcsListInternal()) {
            this.isReady = this.isPACValid(pac);
            if (!this.isReady) {
                return false;
            }
        }
        //   check attributes
        if (!isAttributed()) {
            return true;
        }
        this.applyDefaultAttrValuesOfTypeGraph(this.itsImag);
        AttrVariableTuple attrVariableTuple = this.itsAttrContext.getVariables();
        AttrConditionTuple attrConditionTuple = this.itsAttrContext.getConditions();
        this.errorMsg = "";
        // get used variable and its declaration: (type, name)
        List<Pair<String, String>> varDeclPairs = getVariableDeclarations();
        // add vars of NACs to varDeclPairs
        for (OrdinaryMorphism nac : this.nacStrategie.getAcsListInternal()) {
            addVarDecl(nac.getImage(), varDeclPairs);
        }
        // add vars of PACs to varDeclPairs
        for (OrdinaryMorphism pac : this.pacStrategie.getAcsListInternal()) {
            addVarDecl(pac.getImage(), varDeclPairs);
        }
        // add vars of nested ACs to varDeclPairs
        for (OrdinaryMorphism ac : this.acStrategie.getAcsListInternal()) {
            addVarDecl(ac.getImage(), varDeclPairs);
        }
        // check: same variable name , different type :: should not happen!
        this.isReady = checkDoubleVarDecl(varDeclPairs);
        if (!this.isReady) {
            return false;
        }
        // check used variables
        this.isReady = checkUsedVariables(attrVariableTuple, varDeclPairs);
        if (!this.isReady) {
            return false;
        }
        // mark used variables: RHS, NAC, PAC, LHS
        markUsedVariables(attrVariableTuple);
        // check and mark the attr. conditions
        this.isReady = markAttrConditions(attrVariableTuple, attrConditionTuple);
        if (!this.isReady) {
            return false;
        }
        // find objects: to preserve, to delete, to create, to change,
        // also types which need to be checked due to multiplicity
        this.prepareRuleInfo();
        // adjust the attribute conditions (mark, enabled)
        // of my match, if it exists
        if (this.itsMatch != null) {
            if (this.itsMatch.getRule() == this) {
                this.itsMatch.adjustAttrCondition();
            } else {
                this.itsMatch.dispose();
                this.itsMatch = null;
            }
        }
        // check attribute settings of the new objects
        this.isReady = this.checkAttributesOfNewObjects(attrVariableTuple);
        if (!this.isReady) {
            return false;
        }
        return this.isReady;
    }

    public boolean nacIsUsingVariable(
            final VarMember var,
            final AttrConditionTuple act) {
        return this.nacStrategie.acIsUsingVariable(var, act);
    }

    public boolean pacIsUsingVariable(
            final VarMember var,
            final AttrConditionTuple act) {
        return this.pacStrategie.acIsUsingVariable(var, act);
    }

    protected void applyDefaultAttrValuesOfTypeGraph(
            final Graph graph,
            final Iterator<?> iter) {
        boolean isRight = graph == this.getRight();
        while (iter.hasNext()) {
            GraphObject currentObj = (GraphObject) iter.next();
            if (currentObj.getAttribute() == null) {
                if ((currentObj.getType().getAttrType() != null)
                        && (currentObj.getType().getAttrType().getNumberOfEntries() != 0)) {
                    currentObj.createAttributeInstance();
                } else {
                    continue;
                }
            }
            if (isRight && !this.getInverseImage(currentObj).hasNext()) {
                if (currentObj.isNode()) {
                    graph.applyDefaultAttrValuesOfTypeGraph((Node) currentObj, null);
                } else {
                    graph.applyDefaultAttrValuesOfTypeGraph((Arc) currentObj, null);
                }
            }
        }
    }

    /*
	 * Use the attribute values of the nodes and edges of the Type Graph as default values
	 * for the attributes of the specified graph.
     */
    public void applyDefaultAttrValuesOfTypeGraph(final Graph graph) {
        this.applyDefaultAttrValuesOfTypeGraph(graph, graph.getNodesSet().iterator());
        this.applyDefaultAttrValuesOfTypeGraph(graph, graph.getArcsSet().iterator());
    }

    protected boolean isAttributed() {
        boolean attributed = this.itsOrig.isAttributed()
                || this.itsImag.isAttributed();
        for (OrdinaryMorphism ordMorph : this.nacStrategie.getAcsListInternal()) {
            attributed = ordMorph.getImage().isAttributed();
            if (attributed) break;
        }
        if (!attributed) {
            for (OrdinaryMorphism ordMorph : this.pacStrategie.getAcsListInternal()) {
                attributed = ordMorph.getImage().isAttributed();
                if (attributed) break;
            }
        }
        if (!attributed) {
            for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
                attributed = ordMorph.getImage().isAttributed();
                if (attributed) break;
            }
        }
        return attributed;
    }

    private void addVarDecl(final Graph graph, final List<Pair<String, String>> varDeclPairs) {
        addVarDecl(graph.getNodesSet().iterator(), varDeclPairs);
        addVarDecl(graph.getArcsSet().iterator(), varDeclPairs);
    }

    private void addVarDecl(final Iterator<?> elementsIter, final List<Pair<String, String>> varDeclPairs) {
        while (elementsIter.hasNext()) {
            GraphObject currentObj = (GraphObject) elementsIter.next();
            if (currentObj.getAttribute() != null) {
                AttrInstance attr = currentObj.getAttribute();
                ValueTuple vt = (ValueTuple) attr;
                for (int index = 0; index < vt.getSize(); index++) {
                    ValueMember valueMem = vt.getValueMemberAt(index);
                    if (valueMem.isSet() && valueMem.getExpr().isVariable()) {
                        String varName = valueMem.getExprAsText();
                        String typeName = valueMem.getDeclaration().getTypeName();
//						System.out.println(currentObj.getContext().getName()+"   "+varName+"    "+typeName);
                        Pair<String, String> varDeclPair = new Pair<String, String>(typeName, varName);
                        boolean found = false;
                        for (int pairIndex = 0; pairIndex < varDeclPairs.size(); pairIndex++) {
                            Pair<String, String> existingPair = varDeclPairs.get(pairIndex);
                            if (typeName.equals(existingPair.first) && varName.equals(existingPair.second)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            varDeclPairs.add(varDeclPair);
                        }
                    }
                }
            }
        }
    }

    private boolean checkDoubleVarDecl(final List<Pair<String, String>> varDeclPairs) {
        boolean result = true;
        // check: same variable name , different type :: should not happen!
        for (int outerIndex = 0; result && outerIndex < varDeclPairs.size(); outerIndex++) {
            Pair<String, String> outerPair = varDeclPairs.get(outerIndex);
            for (int innerIndex = outerIndex + 1; result && innerIndex < varDeclPairs.size(); innerIndex++) {
                Pair<String, String> innerPair = varDeclPairs.get(innerIndex);
                if (outerPair.second.equals(innerPair.second) && !outerPair.first.equals(innerPair.first)) {
                    if (!("Object".equals(outerPair.first)
                            || "java.lang.Object".equals(outerPair.first))
                            && !("Object".equals(innerPair.first)
                            || "java.lang.Object".equals(innerPair.first))) {
                        this.errorMsg = "Variable has multiple declaration : ".concat(outerPair.second);
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    /**
     * All attributes of the elements of the RHS to create have to be set.
     */
    private boolean checkAttributesOfNewObjects(final AttrVariableTuple avt) {
        return checkAttrsOfNewObjs(avt, this.itsImag.getNodesSet().iterator())
                && checkAttrsOfNewObjs(avt, this.itsImag.getArcsSet().iterator());
    }

    private boolean checkAttrsOfNewObjs(
            final AttrVariableTuple avt,
            final Iterator<?> elems) {
        final boolean result = true;
        while (elems.hasNext()) {
            final GraphObject o = (GraphObject) elems.next();
            if (this.itsCodomObjects.contains(o)) {
                continue;
            }
            if (o.getAttribute() == null) {
                if ((o.getType().getAttrType() != null)
                        && (o.getType().getAttrType().getNumberOfEntries() != 0)) {
                    this.errorMsg = "Type: <".concat(o.getType().getName()).concat(">  -  attribute not set.");
                    return false;
                }
                continue;
            }
            final AttrInstance attr = o.getAttribute();
            ValueTuple typeObjectAttr = null;
            if (o instanceof Node) {
                final Node typeNode = o.getType().getTypeGraphNodeObject();
                if (typeNode != null) {
                    typeObjectAttr = (ValueTuple) typeNode.getAttribute();
                }
            } else {
                final Arc typeEdge = o.getType().getTypeGraphArcObject(((Arc) o).getSourceType(), ((Arc) o).getTargetType());
                if (typeEdge != null) {
                    typeObjectAttr = (ValueTuple) typeEdge.getAttribute();
                }
            }
            final ValueTuple vt = (ValueTuple) attr;
            for (int k = 0; k < vt.getSize(); k++) {
                final ValueMember vm = vt.getValueMemberAt(k);
                if (vm.isSet()) {
                    if (vm.getExpr().isVariable()) {
                        final VarMember var = avt.getVarMemberAt(vm.getExprAsText());
                        if (var == null) {
                            this.errorMsg = "Variable :  ";
                            this.errorMsg = this.errorMsg.concat(vm.getExprAsText());
                            this.errorMsg = this.errorMsg.concat("   is not declared.");
                            return false;
                        }
                        if (!var.isInputParameter() && vm.getExpr() == null) {
                            final List<String> leftVars = this.itsOrig.getVariableNamesOfAttributes();
                            if (!leftVars.contains(var.getName())) {
                                this.errorMsg = "Variable :  ";
                                this.errorMsg = this.errorMsg.concat(var.getName());
                                this.errorMsg = this.errorMsg.concat("   in the RHS of the rule should be an input parameter");
                                this.errorMsg = this.errorMsg.concat("\nor already declared in the LHS.");
                                return false;
                            }
                        }
                    } else if (vm.getExpr().isComplex()) {
                        if (!vm.isValid()) {
                            this.errorMsg = "Not all attributes of the RHS are correct.\nPlease check expression :  ";
                            this.errorMsg = this.errorMsg.concat(vm.getExprAsText());
                            return false;
                        }
                        final List<String> vec = vm.getAllVariableNamesOfExpression();
                        final List<String> vecLeft = this.itsOrig.getVariableNamesOfAttributes();
                        for (int l = 0; l < vec.size(); l++) {
                            final String n = vec.get(l);
                            boolean found = false;
                            for (int ll = 0; ll < vecLeft.size(); ll++) {
                                String nn = vecLeft.get(ll);
                                if (n.equals(nn)) {
                                    found = true;
                                }
                            }
                            if (!found) {
                                final VarMember m = avt.getVarMemberAt(n);
                                if (m != null && !m.isSet() && !m.isInputParameter()) {
                                    this.errorMsg = "Not all attributes in the RHS of the rule are correct.";
                                    this.errorMsg = this.errorMsg.concat("\nPlease check variable :  ");
                                    this.errorMsg = this.errorMsg.concat(n);
                                    return false;
                                }
                            }
                        }
                    }
                } else //if (!getInverseImage(o).hasNext()) 
                {
                    // look for default attr value in the type graph
                    boolean failed = true;
                    if (typeObjectAttr != null) {
                        final ValueMember tnvm = typeObjectAttr.getValueMemberAt(vm.getName());
                        if (tnvm != null && tnvm.isSet()) {
                            vm.setExprAsText(tnvm.getExprAsText());
                            if (vm.isSet()) {
                                failed = false;
                            }
                        }
                    }
                    if (failed) {
                        if (vm.getDeclaration().getType() == null) {
                            vm.setExprAsText("null");
                        } else if (!this.getTypeSet().isEmptyAttrAllowed()) {
                            this.errorMsg = "Not all attributes in the RHS of the rule are set.";
                            return false;
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean checkUsedVariables(
            final AttrVariableTuple attrVariableTuple,
            final List<Pair<String, String>> varDeclPairs) {
        List<VarMember> used = new ArrayList<>(5);
        boolean result = true;
        for (int index = 0; index < varDeclPairs.size(); index++) {
            final Pair<String, String> varDeclPair = varDeclPairs.get(index);
            String declaredTypeName = varDeclPair.first;
            boolean isClass1 = false;
            final String declaredClassName = isClassName(declaredTypeName);
            if (declaredClassName != null) {
                isClass1 = true;
            }
            boolean isClass2 = false;
            String actualClassName = null;
            String actualTypeName = "";
            final String variableName = varDeclPair.second;
            VarMember varMember = ((VarTuple) attrVariableTuple).getVarMemberAt(variableName);
            if (varMember == null) {
                actualClassName = isClassName(variableName);
                if (actualClassName != null) {
                    actualTypeName = actualClassName;
                }
            } else if (varMember.getDeclaration() == null) {
                this.errorMsg = "Variable: ".concat(variableName).concat("  isn't declared!");
                return false;
            } else {
                actualTypeName = varMember.getDeclaration().getTypeName();
                actualClassName = isClassName(actualTypeName);
            }
            if (actualClassName != null) {
                isClass2 = true;
            }
            if (declaredClassName != null && actualClassName != null) {
                if (!declaredClassName.equals(actualClassName)) {
                    if (!declaredClassName.equals("java.lang.Object")
                            && !actualClassName.equals("java.lang.Object")) {
                        this.errorMsg = "Variable: " + variableName
                                + "  has wrong type." + "\nIt should be :  "
                                + declaredClassName + " .";
                        return false;
                    }
                } else if (!declaredTypeName.equals(actualTypeName)) {
                    final String packageName = declaredClassName.substring(0, declaredClassName.lastIndexOf("."));
                    if (packageName.equals("java.lang") && varMember != null) {
                        varMember.getDeclaration().setType(declaredTypeName);
                    } else {
                        this.errorMsg = "Variable: " + variableName
                                + "  has wrong type." + "\nIt should be :  "
                                + declaredTypeName + " .";
                        return false;
                    }
                }
            } else if (!isClass1 && !isClass2 && !declaredTypeName.equals(actualTypeName)) {
                this.errorMsg = "Variable: " + variableName + "  has wrong type."
                        + "\nIt should be :  " + declaredTypeName + " .";
                return false;
            }
            used.add(varMember);
        }
        this.deleteUnusedVars(used);
        return result;
    }

    private boolean markAttrConditions(
            final AttrVariableTuple avt,
            final AttrConditionTuple act) {
        boolean result = true;
        // check and mark the attr. conditions
        for (int k = 0; k < ((CondTuple) act).getSize(); k++) {
            final CondMember cm = ((CondTuple) act).getCondMemberAt(k);
            if (cm.getExpr() == null) {
                this.errorMsg = "Condition:  " + cm + "  is not defined.";
                return false;
            } else if (!cm.isValid()) {
                this.errorMsg = "Condition:  " + cm
                        + "  is not valid.\nPlease check variables of it.";
                return false;
            }
            final List<String> vars = cm.getAllVariables();
            if (!vars.isEmpty()) {
                boolean mixedNAC = false;
                boolean mixedPAC = false;
                boolean mixed = false;
                String name0 = vars.get(0);
                if (((VarTuple) avt).isDeclared(name0)) {
                    final VarMember var0 = avt.getVarMemberAt(vars.get(0));
                    int mark = var0.getMark();
                    for (int j = 1; j < vars.size(); j++) {
                        final String name = vars.get(j);
                        if (((VarTuple) avt).isDeclared(name)) {
                            final VarMember var = avt.getVarMemberAt(name);
                            if (mark == VarMember.LHS) {
                                if (var.getMark() == VarMember.NAC) {
                                    mixedNAC = true;
                                } else if (var.getMark() == VarMember.PAC) {
                                    mixedPAC = true;
                                }
                            } else if (mark == VarMember.NAC) {
                                if (var.getMark() == VarMember.LHS) {
                                    mixedNAC = true;
                                } else if (var.getMark() == VarMember.PAC) {
                                    mixed = true;
                                }
                            } else if (mark == VarMember.PAC) {
                                if (var.getMark() == VarMember.LHS) {
                                    mixedPAC = true;
                                } else if (var.getMark() == VarMember.NAC) {
                                    mixed = true;
                                }
                            }
                        } else {
                            if (isClassName(name) == null) {
                                this.errorMsg = "Variable: " + name
                                        + "\nof condition: " + cm.getExprAsText()
                                        + "\nis not declared.";
                                return false;
                            }
                        }
                    }
                    if (mixedNAC && mixedPAC) {
                        cm.setMark(CondMember.NAC_PAC_LHS);
                    } else if (mixedNAC) {
                        cm.setMark(CondMember.NAC_LHS);
                    } else if (mixedPAC) {
                        cm.setMark(CondMember.PAC_LHS);
                    } else if (mixed) {
                        cm.setMark(CondMember.NAC_PAC);
                    } else if (mark == VarMember.NAC) {
                        cm.setMark(CondMember.NAC);
                    } else if (mark == VarMember.PAC) {
                        cm.setMark(CondMember.PAC);
                    } else if (mark == VarMember.RHS) {
                        cm.setMark(CondMember.RHS);
                    } else {
                        cm.setMark(CondMember.LHS);
                    }
                } else {
                    if (isClassName(name0) == null) {
                        this.errorMsg = "Variable: " + name0 + "\nof condition: "
                                + cm.getExprAsText() + "\nis not declared.";
                        return false;
                    }
                }
            }
        }
        return result;
    }

    private void markUsedVariables(final AttrVariableTuple avt) {
        // mark used variables: 
        // inside RHS
        markUsedVars(this.itsImag.getNodesSet().iterator(),
                this.itsImag.getArcsSet().iterator(),
                avt, VarMember.RHS);
        // inside NACs	
        for (OrdinaryMorphism ordMorph : this.nacStrategie.getAcsListInternal()) {
            Graph g = ordMorph.getImage();
            markUsedVars(g.getNodesSet().iterator(),
                    g.getArcsSet().iterator(),
                    avt, VarMember.NAC);
        }
        // inside PACs	
        for (OrdinaryMorphism ordMorph : this.pacStrategie.getAcsListInternal()) {
            Graph g = ordMorph.getImage();
            markUsedVars(g.getNodesSet().iterator(),
                    g.getArcsSet().iterator(),
                    avt, VarMember.PAC);
        }
        // inside nested AC	
        this.acStrategie.markUsedVarsOfNestedAcs(this.acStrategie.getAcsListInternal(), avt);
//		for (int l=0; l<this.itsACs.size(); l++) {
//			Graph g = this.itsACs.get(l).getImage();
//			markUsedVars(g.getNodesSet().iterator(), 
//					g.getArcsSet().iterator(),
//					avt, VarMember.PAC); 
//			
//		}
        // finally inside LHS
        markUsedVars(this.itsOrig.getNodesSet().iterator(),
                this.itsOrig.getArcsSet().iterator(),
                avt, VarMember.LHS);
    }

     void markUsedVarsOfNestedACs(List<?> nestedACs, AttrVariableTuple avt) {
        for (int i = 0; i < nestedACs.size(); i++) {
            OrdinaryMorphism nestAC = (OrdinaryMorphism) nestedACs.get(i);
            Graph g = nestAC.getImage();
            markUsedVars(g.getNodesSet().iterator(),
                    g.getArcsSet().iterator(),
                    avt, VarMember.PAC);
            markUsedVarsOfNestedACs(((NestedApplCond) nestAC).getNestedACs(), avt);
        }
    }

     void markUsedVars(
            final Iterator<Node> nodes,
            final Iterator<Arc> arcs,
            final AttrVariableTuple avt,
            int mark) {
        while (nodes.hasNext()) {
            final GraphObject o = nodes.next();
            if (o.getAttribute() != null) {
                final ValueTuple vt = (ValueTuple) o.getAttribute();
                for (int k = 0; k < vt.getSize(); k++) {
                    final ValueMember vm = vt.getValueMemberAt(k);
                    if (vm.isSet()) {
                        if (vm.getExpr().isVariable()) {
                            final VarMember var = avt.getVarMemberAt(vm.getExprAsText());
                            if (var != null) {
                                var.setMark(mark);
                            }
                        } else if (vm.getExpr().isComplex()) {
                            List<String> vec = new ArrayList<>(3);
                            vm.getExpr().getAllVariables(vec);
                            for (String s : vec) {
                                VarMember var = avt.getVarMemberAt(s);
                                if (var != null) {
                                    var.setMark(mark);
                                }
                            }
                        }
                    }
                }
            }
        }
        while (arcs.hasNext()) {
            final GraphObject o = arcs.next();
            if (o.getAttribute() != null) {
                final ValueTuple vt = (ValueTuple) o.getAttribute();
                for (int k = 0; k < vt.getSize(); k++) {
                    final ValueMember vm = vt.getValueMemberAt(k);
                    if (vm.isSet()) {
                        if (vm.getExpr().isVariable()) {
                            final VarMember var = avt.getVarMemberAt(vm.getExprAsText());
                            if (var != null) {
                                var.setMark(mark);
                            }
                        } else if (vm.getExpr().isComplex()) {
                            List<String> vec = new ArrayList<>(3);
                            vm.getExpr().getAllVariables(vec);
                            for (String s : vec) {
                                VarMember var = avt.getVarMemberAt(s);
                                if (var != null) {
                                    var.setMark(mark);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Prepares info about this rule: node, edges to preserve, change, delete, create; types which should be checked due
     * to node resp. edge type multiplicity. These infos can be called by methods: getElementsToPreserve(),
     * getElementsToChange(), getElementsToDelete(), getElementsToCreate(), getTypesWhichNeedMultiplicityCheck.
     */
    public void prepareRuleInfo() {
        this.preserved = this.findElementsToPreserve();
        this.created = this.findElementsToCreate();
        this.deleted = this.findElementsToDelete();
        this.changedPreserved = this.findElementsToChange();
        this.typesWhichNeedMultiplicityCheck = this.findTypesWhichNeedMultiplicityCheck();
        this.hasEnabledGACs = this.hasEnabledACs(true);
        if ("true".equals(this.formStr)) {
            this.setDefaultFormulaTrue();
        } else if ("false".equals(this.formStr)) {
            this.setDefaultFormulaFalse();
        }
    }

    /*
	 * Update infos about this rule (creating, deleting, changing) after
	 * the specified graph object of the LHS or RHS is removed.
	 * @param obj   removed object 
	 * @deprecated the update of rule infos is always done in the method
	 * 				this.isReadyToTransform()
     */
 /*
	private void updateInfosAfterRemove(final GraphObject obj) {
		if (preserved != null && preserved.contains(obj)) {
			preserved.remove(obj);
		}
		else if (created != null && created.contains(obj)) {
			created.remove(obj);
		}
		else if (deleted != null && deleted.contains(obj)) {
			deleted.remove(obj);
		}
		if (changedPreserved != null && changedPreserved.contains(obj)) {
				changedPreserved.remove(obj);
		}
	}
     */
 /*
	private Class<?> getStaticClass(String name) {
		Class<?> klass = null;
		try {
			// check static class
			klass = Class.forName(name);
			System.out.println("Rule.getStaticClass:: for "+name+"  =>  Class "+klass);
			return klass;
		} catch (ClassNotFoundException cnfe) {
			System.out.println("Rule.getStaticClass:: ClassNotFoundException: "+cnfe.getMessage());	
			return null;
		}
	}
     */
    /**
     * Return true if its left and right graphs are empty and there aren't any application conditions, otherwise -
     * false.
     */
    public boolean isEmptyRule() {
        return (this.itsOrig.isEmpty()
                && this.itsImag.isEmpty()
                && this.nacStrategie.getAcsListInternal().isEmpty()
                && this.pacStrategie.getAcsListInternal().isEmpty()
                && this.acStrategie.getAcsListInternal().isEmpty());
    }

    public List<Type> getTypesOfLeftGraph() {
        final List<Type> typeList = new ArrayList<>();
        for (final Iterator<Node> nodeIter = getLeft().getNodesSet().iterator(); nodeIter.hasNext();) {
            final Node currentNode = nodeIter.next();
            if (!typeList.contains(currentNode.getType())) {
                typeList.add(currentNode.getType());
            }
        }
        for (final Iterator<Arc> arcIter = getLeft().getArcsSet().iterator(); arcIter.hasNext();) {
            final Arc currentArc = arcIter.next();
            if (!typeList.contains(currentArc.getType())) {
                typeList.add(currentArc.getType());
            }
        }
        return typeList;
    }

    public List<Type> getTypeOfObjectToDelete() {
        final List<Type> typeList = new ArrayList<>();
        for (final Iterator<Node> nodeIter = getLeft().getNodesSet().iterator(); nodeIter.hasNext();) {
            final Node currentNode = nodeIter.next();
            if (getImage(currentNode) == null
                    && !typeList.contains(currentNode.getType())) {
                typeList.add(currentNode.getType());
            }
        }
        for (final Iterator<Arc> arcIter = getLeft().getArcsSet().iterator(); arcIter.hasNext();) {
            final Arc currentArc = arcIter.next();
            if (getImage(currentArc) == null
                    && !typeList.contains(currentArc.getType())) {
                typeList.add(currentArc.getType());
            }
        }
        return typeList;
    }

    public List<Type> getTypeOfObjectToCreate() {
        final List<Type> typeList = new ArrayList<>();
        for (Iterator<Node> nodeIter = getRight().getNodesSet().iterator(); nodeIter.hasNext();) {
            GraphObject currentObj = nodeIter.next();
            if (!getInverseImage(currentObj).hasNext()
                    && !typeList.contains(currentObj.getType())) {
                typeList.add(currentObj.getType());
            }
        }
        for (Iterator<Arc> arcIter = getRight().getArcsSet().iterator(); arcIter.hasNext();) {
            GraphObject currentObj = arcIter.next();
            if (!getInverseImage(currentObj).hasNext()
                    && !typeList.contains(currentObj.getType())) {
                typeList.add(currentObj.getType());
            }
        }
        return typeList;
    }

    /*
	 *
	 * @return
     */
    public List<String> getTypesWhichNeedMultiplicityCheck() {
        if (this.typesWhichNeedMultiplicityCheck == null) {
            this.typesWhichNeedMultiplicityCheck = findTypesWhichNeedMultiplicityCheck();
        }
        this.itsOrig.changed = false;
        this.itsImag.changed = false;
        return this.typesWhichNeedMultiplicityCheck;
    }

    private List<String> findTypesWhichNeedMultiplicityCheck() {
        final var typeKeyList = new ArrayList<String>();
        final var graphObjList = new ArrayList<GraphObject>();
        graphObjList.addAll(this.getElementsToCreate());
        graphObjList.addAll(this.getElementsToDelete());
        for (int index = 0; index < graphObjList.size(); index++) {
            final GraphObject graphObj = graphObjList.get(index);
            final String typeKey = graphObj.convertToKey();
            if (!typeKeyList.contains(typeKey)) {
                if (graphObj.isNode()) {
                    int min = graphObj.getType().getSourceMin();
                    int max = graphObj.getType().getSourceMax();
                    if (min > 0 || max > 0) {
                        typeKeyList.add(typeKey);
                        final List<Type> children = graphObj.getType().getChildren();
                        for (int childIndex = 0; childIndex < children.size(); childIndex++) {
                            typeKeyList.add(children.get(childIndex).convertToKey());
                        }
                    }
                } else {
                    Arc arc = (Arc) graphObj;
                    int srcMin = graphObj.getType().getSourceMin(arc.getSource().getType(),
                            arc.getTarget().getType());
                    int srcMax = graphObj.getType().getSourceMax(arc.getSource().getType(),
                            arc.getTarget().getType());
                    int tarMin = graphObj.getType().getTargetMin(arc.getSource().getType(),
                            arc.getTarget().getType());
                    int tarMax = graphObj.getType().getTargetMax(arc.getSource().getType(),
                            arc.getTarget().getType());
                    if (srcMin > 0 || tarMin > 0 || srcMax > 0 || tarMax > 0) {
                        typeKeyList.add(typeKey);
                    }
                }
            }
        }
        return typeKeyList;
    }

    /**
     * Returns true if this rule will create new graph elements, otherwise - false.
     */
    public boolean isCreating() {
        // LHS graph size > rule mapping size
        this.isCreating = this.itsImag.getSize() > this.getCodomainSize();
        if (this.isCreating) {
            this.created = findElementsToCreate();
        }
        return this.isCreating;
    }

    /**
     * Returns true if this rule will delete some graph elements, otherwise - false.
     */
    public boolean isDeleting() {
        // LHS graph size > rule mapping size
        this.isDeleting = this.itsOrig.getSize() > this.getDomainSize();
        if (this.isDeleting) {
            this.deleted = findElementsToDelete();
        }
        return this.isDeleting;
    }

    public boolean isNodeDeleting() {
        this.isNodeDeleting = false;
        if (this.isDeleting) {
            for (final Iterator<Node> en = this.itsOrig.getNodesSet().iterator(); en.hasNext();) {
                if (getImage(en.next()) == null) {
                    this.isNodeDeleting = true;
                    break;
                }
            }
        }
        return this.isNodeDeleting;
    }

    /**
     * Checks if this rule may cause a dangling edge problem. Returns true if the rule is deleting nodes and after
     * deleting a dangling-edge problem may occur.
     *
     * @return true if the rule may cause dangling edges, false otherwise
     */
    public boolean mayCauseDanglingEdge() {
        final List<Node> deletedNodes = this.findNodesToDelete();
        if (deletedNodes.isEmpty()) {
            return false;
        }
        boolean result = false;
        for (int nodeIndex = 0; nodeIndex < deletedNodes.size() && !result; nodeIndex++) {
            final Node currentNode = deletedNodes.get(nodeIndex);
            final List<Arc> inheritedArcs = this.getTypeSet().getInheritedArcs(currentNode.getType());
            if (!inheritedArcs.isEmpty()) {
                // TypeGraph exists and arcs at Node of type currentNode.getType()
                for (int arcIndex = 0; arcIndex < inheritedArcs.size() && !result; arcIndex++) {
                    final Arc currentArc = inheritedArcs.get(arcIndex);
                    if (currentArc.getSourceType().isParentOf(currentNode.getType())) {
                        int number = currentNode.getNumberOfOutgoingArcsOfTypeToTargetType(currentArc.getType(), currentArc.getTarget().getType());
                        if (number > 0) {
                            int tarMax = currentArc.getType().getTargetMax(currentArc.getSource().getType(),
                                    currentArc.getTarget().getType());
                            if (tarMax != TypeSet.UNDEFINED
                                    && number != tarMax) {
                                result = true;
                            }
                        } else if (!this.hasNacWhichForbidsArc(currentArc, currentNode)) {
                            result = true;
                        }
//						else
//							result = true;
                    } else if (currentArc.getTargetType().isParentOf(currentNode.getType())) {
                        int number = currentNode.getNumberOfIncomingArcsOfTypeFromSourceType(currentArc.getType(), currentArc.getSource().getType());
                        if (number > 0) {
                            int srcMax = currentArc.getType().getSourceMax(currentArc.getSource().getType(),
                                    currentArc.getTarget().getType());
                            if (srcMax != TypeSet.UNDEFINED
                                    && number != srcMax) {
                                result = true;
                            }
                        } else if (!this.hasNacWhichForbidsArc(currentArc, currentNode)) {
                            result = true;
                        }
//						else
//							result = true;
                    }
                }
            }
        }
        return result;
    }

    private boolean hasNacWhichForbidsArc(Arc typeArc, Node lhsNode) {
        for (OrdinaryMorphism negativeApplCond : this.nacStrategie.getAcsListInternal()) {
            if (!negativeApplCond.isEnabled()) {
                continue;
            }
            Iterator<Arc> arcIterator = negativeApplCond.getTarget().getArcsCollection().iterator();
            while (arcIterator.hasNext()) {
                Arc currentArc = arcIterator.next();
                if (!negativeApplCond.getInverseImage(currentArc).hasNext()
                        && currentArc.getType() == typeArc.getType()) {
                    Node currentNode = (Node) negativeApplCond.getImage(lhsNode);
                    if (currentNode == currentArc.getSource()) {
                        return true;
                    } else if (currentNode == currentArc.getTarget()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isArcDeleting() {
        if (this.isDeleting) {
            for (final Iterator<Arc> arcIter = getLeft().getArcsSet().iterator(); arcIter.hasNext();) {
                if (getImage(arcIter.next()) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
	 * Checks whether this rule deletes an edge of the given type 
	 * and the specified source and target nodes.
	 * The nodes must be contained in the LHS of this rule. 
     */
    public boolean isArcDeleting(final Node source, final Type arcType, final Node target) {
        if (this.itsOrig.getNodesSet().contains(source)
                && this.itsOrig.getNodesSet().contains(target)) {
            for (final Iterator<Arc> arcIter = source.getOutgoingArcs(arcType, target.getType()).iterator(); arcIter.hasNext();) {
                if (getImage(arcIter.next()) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
	 * Checks whether this rule deletes the specified edge.
	 * The edge must be contained in the LHS of this rule. 
     */
    public boolean isArcDeleting(final Arc arc) {
        if (this.itsOrig.getArcsSet().contains(arc)
                && this.getImage(arc) == null) {
            return true;
        }
        return false;
    }

    /*
	 * Checks whether this rule creates an edge of the given type 
	 * and the specified source and target nodes.
	 * The nodes must be contained in the LHS of this rule. 
     */
    public boolean isArcCreating(final Node source, final Type arcType, final Node target) {
        if (this.isCreating
                && this.itsOrig.getNodesSet().contains(source)
                && this.itsOrig.getNodesSet().contains(target)) {
            for (final Iterator<Arc> arcIter = this.itsImag.getArcsSet().iterator(); arcIter.hasNext();) {
                Arc currentArc = arcIter.next();
                if (currentArc.getType().compareTo(arcType)
                        && !this.getInverseImage(currentArc).hasNext()) {
                    List<GraphObject> inv1 = this.getInverseImageList(currentArc.getSource());
                    if (inv1.contains(source)) {
                        List<GraphObject> inv2 = this.getInverseImageList(currentArc.getTarget());
                        if (inv2.contains(target)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
	 * Checks whether this rule creates the specified edge.
	 * The edge must be contained in the RHS of this rule. 
	 * The source and target nodes must be preserved by this rule. 
     */
    public boolean isArcCreating(final Arc currentArc) {
        if (//this.itsImag.getArcsSet().contains(currentArc) &&
                !this.getInverseImage(currentArc).hasNext()
                && this.getInverseImage(currentArc.getSource()).hasNext()
                && this.getInverseImage(currentArc.getTarget()).hasNext()) {
            return true;
        }
        return false;
    }

    /**
     * Returns elements of the LHS which should be preserved.
     */
    public List<GraphObject> getElementsToPreserve() {
        if (this.preserved == null
                || this.itsImag.changed) {
            this.preserved = findElementsToPreserve();
        }
        return this.preserved;
    }

    /**
     * Returns elements of the RHS to create.
     */
    public List<GraphObject> getElementsToCreate() {
        if (this.created == null
                || this.itsImag.changed) {
            this.created = findElementsToCreate();
        }
        return this.created;
    }

    /**
     * Returns elements of the LHS to delete.
     */
    public List<GraphObject> getElementsToDelete() {
        if (this.deleted == null
                || this.itsOrig.changed) {
            this.deleted = findElementsToDelete();
        }
        return this.deleted;
    }

    /**
     * Returns preserved elements which attributes should be changed. The key is an object of the LHS, the value - its
     * image of the RHS.
     */
    public Map<GraphObject, GraphObject> getElementsToChange() {
        if (this.changedPreserved == null
                || this.itsOrig.changed) {
            this.changedPreserved = findElementsToChange();
        }
        return this.changedPreserved;
    }

    private List<GraphObject> findElementsToPreserve() {
        var resultList = new ArrayList<GraphObject>();
        resultList.addAll(this.itsDomObjects);
        return resultList;
    }

    private List<GraphObject> findElementsToCreate() {
        var resultList = new ArrayList<GraphObject>();
        resultList.addAll(this.findNodesToCreate());
        resultList.addAll(this.findArcsToCreate());
        this.isCreating = !resultList.isEmpty();
        return resultList;
    }

    private List<Node> findNodesToCreate() {
        var resultList = new ArrayList<Node>();
        for (Iterator<Node> nodeIter = getRight().getNodesSet().iterator(); nodeIter.hasNext();) {
            Node currentNode = nodeIter.next();
            if (!getInverseImage(currentNode).hasNext()) {
                resultList.add(currentNode);
            }
        }
        return resultList;
    }

    private List<Arc> findArcsToCreate() {
        var resultList = new ArrayList<Arc>();
        for (Iterator<Arc> arcIter = getRight().getArcsSet().iterator(); arcIter.hasNext();) {
            Arc currentArc = arcIter.next();
            if (!getInverseImage(currentArc).hasNext()) {
                resultList.add(currentArc);
            }
        }
        return resultList;
    }

    private List<GraphObject> findElementsToDelete() {
        final var resultList = new ArrayList<GraphObject>();
        resultList.addAll(findNodesToDelete());
        resultList.addAll(findArcsToDelete());
        return resultList;
    }

    private List<Node> findNodesToDelete() {
        final var resultList = new ArrayList<Node>();
        for (final Iterator<Node> nodeIter = getLeft().getNodesSet().iterator(); nodeIter.hasNext();) {
            final Node currentNode = nodeIter.next();
            if (getImage(currentNode) == null) {
                resultList.add(currentNode);
            }
        }
        this.isDeleting = !resultList.isEmpty();
        this.isNodeDeleting = this.isDeleting;
        return resultList;
    }

    private List<Arc> findArcsToDelete() {
        final var resultList = new ArrayList<Arc>();
        for (final Iterator<Arc> arcIter = getLeft().getArcsSet().iterator(); arcIter.hasNext();) {
            final Arc currentArc = arcIter.next();
            if (getImage(currentArc) == null) {
                resultList.add(currentArc);
            }
        }
        this.isDeleting = this.isDeleting || !resultList.isEmpty();
        return resultList;
    }

    /**
     * Returns true if this rule will change some attributes of the graph elements, otherwise - false.
     */
    public boolean isChanging() {
        for (int i = 0; i < this.itsDomObjects.size(); i++) {
            GraphObject go = this.itsDomObjects.get(i);
            if (isChangingAttribute(go, getImage(go))) {
                this.isChanging = true;
                break;
            }
        }
        return this.isChanging;
    }

    /*
	 * Returns preserved graph objects to be changed. The key is a graph object
	 * of the LHS, the value is its image object of the RHS
     */
    private Map<GraphObject, GraphObject> findElementsToChange() {
        var resultMap = new HashMap<GraphObject, GraphObject>();
        for (int index = 0; index < this.itsDomObjects.size(); index++) {
            GraphObject graphObj = this.itsDomObjects.get(index);
            if (isChangingAttribute(graphObj, getImage(graphObj))) {
                resultMap.put(graphObj, getImage(graphObj));
            }
        }
        this.isChanging = !resultMap.isEmpty();
        return resultMap;
    }

    private boolean isChangingAttribute(GraphObject sourceObj,
            GraphObject targetObj) {
        if (targetObj.getAttribute() == null
                || targetObj.getAttribute().getNumberOfEntries() == 0) {
            return false;
        }
        ValueTuple sourceValueTuple = (ValueTuple) sourceObj.getAttribute();
        ValueTuple targetValueTuple = (ValueTuple) targetObj.getAttribute();
        for (int index = 0; index < sourceValueTuple.getNumberOfEntries(); index++) {
            ValueMember sourceValueMem = sourceValueTuple.getValueMemberAt(index);
            ValueMember targetValueMem = targetValueTuple.getValueMemberAt(sourceValueMem.getName());
            if (targetValueMem != null && targetValueMem.isSet()) {
                if (!sourceValueMem.isSet()) {
                    return true;
                } else if (!targetValueMem.getExprAsText().equals(sourceValueMem.getExprAsText())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Restores variable declarations of the RHS, NACs and PACs. The reason is: the variables declarations can be lost
     * after a step. Before the next application of this rule can be done the lost variable declarations have to be
     * restored. This method is called during Critical Pair Analysis.
     */
    protected void restoreVariableDeclaration() {
        VarTuple vart = (VarTuple) getAttrContext().getVariables();
        if (this.itsImag.isAttributed()) {
            // check vars of RHS
            this.restoreVarDecl(this.itsImag, vart);
        }
        // check vars of NACs
        for (OrdinaryMorphism nac : this.nacStrategie.getAcsListInternal()) {
            if (nac.getImage().isAttributed()) {
                this.restoreVarDecl(nac.getImage(), vart);
            }
        }
        // check vars of PACs
        for (OrdinaryMorphism pac : this.pacStrategie.getAcsListInternal()) {
            if (pac.getImage().isAttributed()) {
                this.restoreVarDecl(pac.getImage(), vart);
            }
        }
        // check vars of nested ACs
        for (OrdinaryMorphism applicationCondition : this.acStrategie.getAcsListInternal()) {
            if (applicationCondition.getImage().isAttributed()) {
                this.restoreVarDecl(applicationCondition.getImage(), vart);
            }
        }
    }

    private void restoreVarDecl(final Graph g, final VarTuple vart) {
        Iterator<GraphObject> en1 = g.iteratorOfElems();
        while (en1.hasNext()) {
            GraphObject o = en1.next();
            if (o.getAttribute() == null) {
                continue;
            }
            AttrInstance attr = o.getAttribute();
            ValueTuple vt = (ValueTuple) attr;
            for (int k = 0; k < vt.getSize(); k++) {
                ValueMember vm = vt.getValueMemberAt(k);
                if (vm.isSet() && vm.getExpr().isVariable()) {
                    String n = vm.getExprAsText();
                    String t = vm.getDeclaration().getTypeName();
                    VarMember varm = vart.getVarMemberAt(n);
                    String t_varm = "";
                    if (varm != null) {
                        t_varm = varm.getDeclaration().getTypeName();
                    }
                    if (varm == null || !t.equals(t_varm)) {
                        vart.declare(vm.getDeclaration().getHandler(),
                                t, n);
                        ((VarMember) vart.getMemberAt(n))
                                .setTransient(true);
                    }
                }
            }
        }
    }

    @Override
    public ArrayMovie<Type> getUsedTypes() {
        // get types of LHS and RHS
        final ArrayMovie<Type> typeVec = super.getUsedTypes();
        // add types of NACs
        for (OrdinaryMorphism ordMorph : this.nacStrategie.getAcsListInternal()) {
            addUsedTypes(ordMorph.getTarget(), typeVec);
        }
        // add types of PACs
        for (OrdinaryMorphism ordMorph : this.pacStrategie.getAcsListInternal()) {
            addUsedTypes(ordMorph.getTarget(), typeVec);
        }
        // add types of nested ACs
        for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
            addUsedTypes(ordMorph.getTarget(), typeVec);
        }
        return typeVec;
    }

    private void addUsedTypes(final Graph graph, final ArrayMovie<Type> typeVec) {
        Iterator<Node> nodes = graph.getNodesSet().iterator();
        while (nodes.hasNext()) {
            GraphObject currentObj = nodes.next();
            if (!typeVec.contains(currentObj.getType())) {
                typeVec.add(currentObj.getType());
            }
        }
        Iterator<Arc> arcs = graph.getArcsSet().iterator();
        while (arcs.hasNext()) {
            GraphObject currentObj = arcs.next();
            if (!typeVec.contains(currentObj.getType())) {
                typeVec.add(currentObj.getType());
            }
        }
    }

    /**
     * Returns error message if this rule is not ready to transform.
     *
     * @see agg.xt_basis.Rule#isReadyToTransform().
     */
    public String getErrorMsg() {
        return this.errorMsg;
    }

    /**
     * Returns true if this rule can make a match basically. It works for INJECTIVE matching, only.
     */
    public boolean canMatch(Graph g, MorphCompletionStrategy strategy) {
        // check graph size if injective morphism
        if (strategy.getProperties().get(CompletionPropertyBits.INJECTIVE)) {
            if ((getLeft().getNodesCount() > g.getNodesCount())
                    || (getLeft().getArcsCount() > g.getArcsCount())) {
                return false;
            }
        }
        // check types: all types of the orig. graph should be in image, too
        ArrayMovie<Type> origTypes = getLeft().getUsedTypes();
        // TODO::mit PACs  origTypes erweitern
        List<Type> otherTypes = g.getUsedAndInheritedTypes();
        for (int i = 0; i < origTypes.size(); i++) {
            if (!otherTypes.contains(origTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set its match to the specified parameter.
     */
    public void setMatch(Match m) {
        this.itsMatch = m;
    }

    /**
     * Reset target graph of its match, if it exists.
     */
    public void resetTargetOfMatch(Graph g) {
        if (this.itsMatch != null) {
            this.itsMatch.setTarget(g);
        }
    }

    public void setParallelMatchingEnabled(boolean b) {
        this.parallelMatching = b;
    }

    public boolean isParallelApplyEnabled() {
        return this.parallelMatching;
    }

    public void setRandomizedCSPDomain(boolean b) {
        this.randomCSPDomain = b;
    }

    public boolean isRandomizedCSPDomain() {
        return this.randomCSPDomain;
    }

    /**
     * Allows to define the CSP solver has to do next match completion starting always by first CSP variable. This works
     * for parallel match only. The method <code>setParallelMatchingEnabled(true)</code> should be called before.
     */
    public void setStartParallelMatchingByFirst(boolean b) {
        this.startParallelMatchByFirstCSPVar = b;
    }

    /**
     * Set value of the input parameter of its attribute context. The specified parameters contain: String - is a name
     * of an input parameter, first Object of a List - is the value, second Object of a List - is the type of this input
     * parameter.
     */
    public void setInputParameters(HashMap<String, List<Object>> parameters) {
        VarTuple varTuple = (VarTuple) getAttrContext().getVariables();
        int paramCount = 0;
        for (int index = 0; index < varTuple.getNumberOfEntries(); index++) {
            VarMember varMem = varTuple.getVarMemberAt(index);
            if (varMem.isInputParameter()) {
                List<Object> valuePair = parameters.get(varMem.getName());
                Object value = valuePair.get(0);
                String type = (String) valuePair.get(1);
                if (type.equals("int") || type.equals("boolean")
                        || type.equals("float") || type.equals("double")
                        || type.equals("short") || type.equals("long")) {
                    varMem.setExprAsEvaluatedText(value.toString());
                } else {
                    varMem.setExprAsObject(value);
                }
                paramCount++;
            }
            if (paramCount > parameters.size()) {
                break;
            }
        }
    }

    protected boolean evalDefaultFormula() {
        if (this.itsMatch == null) {
            return false;
        }
        if (this.acStrategie.getAcsListInternal().isEmpty()) {
            return true;
        }
        int acCount = this.acStrategie.getAcsListInternal().size();
        final List<Evaluable> evalList = new ArrayList<>(acCount);
        String formulaStr = "";
        int enabledIndex = -1;
        for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
            NestedApplCond nestedApplCond = (NestedApplCond) ordMorph;
            if (nestedApplCond.isEnabled()) {
                enabledIndex++;
                nestedApplCond.setRelatedMorphism(this.itsMatch);
                evalList.add(nestedApplCond);
                if (enabledIndex == 0) {
                    if (this.formStr.equals("false")) {
                        formulaStr = formulaStr.concat("!".concat(String.valueOf(evalList.size())));
                    } else {
                        formulaStr = formulaStr.concat(String.valueOf(evalList.size()));
                    }
                } else {
                    if (this.formStr.equals("false")) {
                        formulaStr = formulaStr.concat("&!").concat(String.valueOf(evalList.size()));
                    } else {
                        formulaStr = formulaStr.concat("&").concat(String.valueOf(evalList.size()));
                    }
                }
            }
        }
//		System.out.println("Test formula of (nested) appl conds:  " + formulaStr);
        boolean result = this.itsFormula.setFormula(evalList, formulaStr)
                && this.itsFormula.eval(this.itsMatch.getImage());
        if (!result) {
            this.itsMatch.setErrorMsg("Formula:  " + formulaStr + "  is violated!");
        }
        return result;
    }

    public boolean setDefaultFormulaTrue() {
        if (this.acStrategie.getAcsListInternal().isEmpty()) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        }
        final List<Evaluable> evalList = new ArrayList<>(this.acStrategie.getAcsListInternal().size());
        for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
            NestedApplCond nestedApplCond = (NestedApplCond) ordMorph;
            if (nestedApplCond.isEnabled()) {
                evalList.add(nestedApplCond);
            }
        }
        String formulaStr = "";
        for (int index = 0; index < evalList.size(); index++) {
            String tempStr = (index == 0) ? formulaStr.concat(String.valueOf(index + 1))
                    : formulaStr.concat("&").concat(String.valueOf(index + 1));
            formulaStr = tempStr;
        }
        if ("".equals(formulaStr)) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        }
        if (this.itsFormula.setFormula(evalList, formulaStr)) {
            this.formStr = this.itsFormula.getAsString(evalList);
            this.formReadStr = this.itsFormula.getAsString(evalList, this.getNameOfEnabledACs());
//			System.out.println(this.formReadStr);
//			this.setTextualComment("Formula: ".concat(this.formReadStr));
            return true;
        }
        return false;
    }

    public boolean setDefaultFormulaFalse() {
        if (this.acStrategie.getAcsListInternal().isEmpty()) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        }
        final List<Evaluable> evalList = new ArrayList<>(this.acStrategie.getAcsListInternal().size());
        for (OrdinaryMorphism ordMorph : this.acStrategie.getAcsListInternal()) {
            NestedApplCond nestedApplCond = (NestedApplCond) ordMorph;
            if (nestedApplCond.isEnabled()) {
                evalList.add(nestedApplCond);
            }
        }
        String formulaStr = "";
        for (int index = 0; index < evalList.size(); index++) {
            String tempStr = (index == 0) ? formulaStr.concat(String.valueOf(index + 1))
                    : formulaStr.concat("&").concat(String.valueOf(index + 1));
            formulaStr = tempStr;
        }
        if ("".equals(formulaStr)) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        } else {
            formulaStr = "!(".concat(formulaStr).concat(")");
        }
        if (this.itsFormula.setFormula(evalList, formulaStr)) {
            this.formStr = this.itsFormula.getAsString(evalList);
            this.formReadStr = this.itsFormula.getAsString(evalList, this.getNameOfEnabledACs());
//			System.out.println(this.formReadStr);
//			this.setTextualComment("Formula: ".concat(this.formReadStr));
            return true;
        }
        return false;
    }

    public boolean evalFormula() {
        boolean result = true;
        if (this.itsMatch != null && this.acStrategie.getAcsListInternal().size() != 0) {
            for (OrdinaryMorphism om : this.acStrategie.getAcsListInternal()) {
                NestedApplCond nestedApplCond = (NestedApplCond) om;
                if (nestedApplCond.isEnabled()) {
                    nestedApplCond.setRelatedMorphism(this.itsMatch);
                }
            }
            if (this.formStr.equals("true")) {
                this.setDefaultFormulaTrue();
            } else if (this.formStr.equals("false")) {
                this.setDefaultFormulaFalse();
            }
            result = this.itsFormula.eval(this.itsMatch.getImage());
            if (!result) {
                this.itsMatch.setErrorMsg("Formula:  " + this.formReadStr + "  is violated!");
            }
            this.disposeResultsOfNestedACs();
            return result;
        } else {
            return true;
        }
    }

    public void setFormula(Formula formula) {
//		this.itsFormula = formula;
//		this.formulaStr = this.itsFormula.getAsString(this.getEnabledGeneralACsAsEvaluable());
//		this.setTextualComment("Formula: ".concat(this.formulaStr));
        this.setFormula(formula.getAsString(this.getEnabledGeneralACsAsEvaluable()), this.getEnabledACs());
    }

    /**
     * Set a boolean formula represented by the specified bnf string above nested application conditions.
     *
     * @param bnfFormula the boolean formula string to set
     */
    public boolean setFormula(String bnfFormula) {
//		final List<NestedApplCond> vars = new List<NestedApplCond>(this.itsACs.size());
//		for (int i=0; i<this.itsACs.size(); i++) {	
//			vars.add((NestedApplCond) this.itsACs.get(i));
//		}		
        return this.setFormula(bnfFormula, this.getEnabledACs());
    }

    /**
     * Set a boolean formula represented by the specified bnf string above nested application conditions.
     *
     * @param bnfFormula the boolean formula string to set
     * @param nestedApplCondList the list of nested application conditions
     */
    public boolean setFormula(String bnfFormula, final List<NestedApplCond> nestedApplCondList) {
        if (bnfFormula.equals("true")) {
//			this.formStr = bnfFormula;
//			this.formReadStr = bnfFormula;
//			return true;
            return this.setDefaultFormulaTrue();
        } else if (bnfFormula.equals("false")) {
            return this.setDefaultFormulaFalse();
        }
        final List<Evaluable> evalList = new ArrayList<>();
        for (int index = 0; index < nestedApplCondList.size(); index++) {
            NestedApplCond nestedApplCond = nestedApplCondList.get(index);
            if (nestedApplCond.isEnabled()) {
                evalList.add(nestedApplCond);
            }
        }
        if (evalList.isEmpty()) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        }
        if (this.itsFormula.setFormula(evalList, bnfFormula)) {
            this.formStr = this.itsFormula.getAsString(evalList);
            this.formReadStr = this.itsFormula.getAsString(evalList, this.getNameOfEnabledACs());
//			System.out.println(this.formReadStr);
            this.setTextualComment("Formula: ".concat(this.formReadStr));
            return true;
        } else {
            return false;
        }
    }

    public boolean refreshFormula(final List<Evaluable> vars) {
        String bnf = this.formStr;
        if (this.itsFormula.setFormula(vars, bnf)) {
            this.formStr = this.itsFormula.getAsString(vars);
            this.formReadStr = this.itsFormula.getAsString(vars, this.getNameOfEnabledACs());
            this.setTextualComment("Formula: ".concat(this.formReadStr));
            return true;
        } else {
            this.formStr = "true";
            this.formReadStr = "true";
        }
        return false;
    }

    /**
     * Returns the formula string as internal represantation like this: (1&2).>br> This method shoud be used for all
     * actions relationg to Formula objects.
     */
    public String getFormulaStr() {
        return this.formStr;
    }

    /**
     * Returns the formula string as readable representation like this: (nameOf1 & nameOf2). This method should be used
     * for messages.
     */
    public String getFormulaText() {
        return this.formReadStr;
    }

    public Formula getFormula() {
        return this.itsFormula;
    }

    /**
     * Returns true, if it contains enabled nested application conditions.
     */
    public boolean hasEnabledACs(boolean checkBefore) {
        if (checkBefore) {
            this.hasEnabledGACs = false;
            for (OrdinaryMorphism om : this.acStrategie.getAcsListInternal()) {
                NestedApplCond applicationCondition = (NestedApplCond) om;
                if (applicationCondition.isEnabled()) {
                    this.hasEnabledGACs = true;
                    break;
                }
            }
        }
        return this.hasEnabledGACs;
    }

    /**
     * Returns a list with names of enabled general application conditions.
     */
    public List<String> getNameOfEnabledACs() {
        final List<String> vars = new ArrayList<>();
        for (OrdinaryMorphism om : this.acStrategie.getAcsListInternal()) {
            NestedApplCond applicationCondition = (NestedApplCond) om;
            if (applicationCondition.isEnabled()) {
                vars.add(applicationCondition.getName());
            }
        }
        return vars;
    }

    /**
     * Returns a list with names of enabled general application conditions and its nested ACs inclusively.
     */
    public List<String> getNameOfEnabledNestedACs() {
        final List<String> vars = new ArrayList<>();
        for (OrdinaryMorphism om : this.acStrategie.getAcsListInternal()) {
            NestedApplCond applicationCondition = (NestedApplCond) om;
            if (applicationCondition.isEnabled()) {
                vars.add(applicationCondition.getName());
            }
            vars.addAll(applicationCondition.getNameOfEnabledNestedACs());
        }
        return vars;
    }

    /**
     * Returns a list with names of all general application conditions and its nested ACs inclusively.
     */
    public List<String> getNameOfNestedACs() {
        final List<String> vars = new ArrayList<>();
        for (OrdinaryMorphism om : this.acStrategie.getAcsListInternal()) {
            NestedApplCond applicationCondition = (NestedApplCond) om;
            vars.add(applicationCondition.getName());
            vars.addAll(applicationCondition.getNameOfEnabledNestedACs());
        }
        return vars;
    }

    /**
     * Makes the minimal rule from the given rule.A minimal rule comprises the effects of a given rule in a minimal
     * context.
     *
     * @return
     */
    public Rule getMinimalRule() {
        return BaseFactory.theBaseFactory.makeMinimalOfRule(this);
    }

    /**
     * Returns an inverse construction of this rule.This rule has to be injective, otherwise returns null. Note: This
     * method is mainly used during critical pair analysis.
     *
     * @return
     */
    public InverseRuleConstructData getInverseConstructData() {
        if (this.isInjective()) {
            if (this.invConstruct == null) {
                this.invConstruct = new InverseRuleConstructData(this);
            }
            return this.invConstruct;
        }
        return null;
    }

    /**
     * This method does not destroy the Rule and OrdinaryMorphism instances of the inverse construction. They must be
     * disposed by the user object explicitly. The local pair references set to null, only.
     */
    public void disposeInverseConstruct() {
        if (this.invConstruct != null) {
            this.invConstruct.dispose();
            this.invConstruct = null;
        }
    }

    /**
     * Destroys the Rule and OrdinaryMorphism instances of the inverse construction. The local pair references set to
     * null.
     */
    public void destroyInverseConstruct() {
        if (this.invConstruct != null) {
            this.invConstruct.destroy();
            this.invConstruct = null;
        }
    }

    public void initSignatur() {
        ((VarTuple) this.getAttrContext().getVariables()).initSignaturOrder();
    }

    public void disposeSignatur() {
        ((VarTuple) this.getAttrContext().getVariables()).disposeSignaturOrder();
    }

    public ArrayMovieInt getSignaturOrder() {
        return ((VarTuple) this.getAttrContext().getVariables()).getSignaturOrder();
    }

    public String getSignatur() {
        VarTuple varTuple = (VarTuple) this.getAttrContext().getVariables();
        String signatureStr = this.getName().concat("(");
        String inParamsStr = "";
        ArrayMovieInt order = varTuple.getSignaturOrder();
        for (int index = 0; index < order.size(); index++) {
            VarMember varMem = (VarMember) varTuple.getMemberAt(order.get(index));
            String nameType = varMem.getName().concat(":").concat(varMem.getDeclaration().getTypeName());
            inParamsStr = inParamsStr.concat(nameType);
            if (index < (order.size() - 1)) {
                inParamsStr = inParamsStr.concat(", ");
            }
        }
        String outParamsStr = "";
        for (int index = 0; index < varTuple.getSize(); index++) {
            VarMember varMem = (VarMember) varTuple.getMemberAt(index);
            if (varMem.isOutputParameter()) {
                if (!inParamsStr.isEmpty()) {
                    outParamsStr = outParamsStr.concat(", ");
                }
                outParamsStr = outParamsStr.concat("out ");
                String nameType = varMem.getName().concat(":").concat(varMem.getDeclaration().getTypeName());
                outParamsStr = outParamsStr.concat(nameType);
                break;
            }
        }
        signatureStr = signatureStr.concat(inParamsStr).concat(outParamsStr);
        signatureStr = signatureStr.concat(")");
        return signatureStr;
    }

    public void addInToSignatur(int indexOfVar) {
        ((VarTuple) this.getAttrContext().getVariables()).addToSignaturOrder(indexOfVar);
    }

    public void removeInFromSignatur(int indexOfVar) {
        ((VarTuple) this.getAttrContext().getVariables()).removeFromSignaturOrder(indexOfVar);
    }

    public void addOutToSignatur(int indexOfVar) {
        VarTuple varTuple = (VarTuple) this.getAttrContext().getVariables();
        for (int index = 0; index < varTuple.getSize(); index++) {
            VarMember varMem = (VarMember) varTuple.getMemberAt(index);
            if (index == indexOfVar) {
                varMem.setOutputParameter(true);
            } else {
                varMem.setOutputParameter(false);
            }
        }
    }

    public void removeOutFromSignatur(int indexOfVar) {
        VarTuple varTuple = (VarTuple) this.getAttrContext().getVariables();
        VarMember varMem = (VarMember) varTuple.getMemberAt(indexOfVar);
        if (varMem != null) {
            varMem.setOutputParameter(false);
        }
    }
}
