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
 * Represents a graph transformation rule in the AGG system. A rule consists of
 * a left-hand side (LHS) graph and a right-hand side (RHS) graph, and defines
 * how a subgraph matching the LHS can be transformed into a subgraph matching
 * the RHS.
 *
 * <p>
 * AGG implements the DPO (Double Pushout) approach by enabling the
 * dangling-edge condition by default. Disabling the dangling condition allows
 * AGG to simulate the SPO (Single Pushout) approach.
 *
 * <p>
 * A rule can have application conditions (ACs), negative application conditions
 * (NACs), and positive application conditions (PACs) that constrain when and
 * how the rule can be applied.
 *
 * @see OrdinaryMorphism
 * @see Graph
 * @see Match
 */
public class Rule extends OrdinaryMorphism implements XMLObject {

    /**
     * Accepts a visitor for this rule.
     *
     * @param visitor the visitor to accept
     * @param <T> the return type of the visitor
     * @return the result of visiting this rule
     */
    public <T> T accept(RuleVisitor<T> visitor) {
        return visitor.visit(this);
    }

    protected Formula itsFormula = new Formula(true);
    protected String formStr = "true";
    protected String formReadStr = "true";
    final protected List<OrdinaryMorphism> itsACs = new ArrayList<>();
    final protected List<OrdinaryMorphism> itsNACs = new ArrayList<>();
    final protected List<OrdinaryMorphism> itsPACs = new ArrayList<>();
    // containers for PostApplicationConditions
    transient protected boolean generatePostConstraints;
    protected List<AtomConstraint> itsUsedAtomics;
    protected List<Formula> itsUsedFormulas;
    transient protected List<String> constraintNameSet;
    transient protected List<Formula> constraints;
    transient protected List<EvalSet> atom_conditions;
    protected List<ShiftedPAC> itsShiftedPACs;
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
     * Creates a new rule with default left and right graphs. Initializes the
     * rule with default names for the graphs and sets up the attribute context.
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
    }

    /**
     * Creates a new rule with the specified type set. Creates new left and
     * right graphs using the given type set and initializes the rule.
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
    }

    /**
     * Creates a new rule with the specified left graph, right graph, and
     * attribute context.
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
    }

    /**
     * Disposes the superclass resources and cleans up rule-specific references.
     * This method should be called when the rule is no longer needed to free
     * resources.
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
     * <li>Disposing all ACs (Application Conditions)</li>
     * <li>Disposing the inverse rule construction data</li>
     * <li>Disposing the left and right graphs</li>
     * </ul>
     */
    public void dispose() {
        super.dispose();
        while (!this.itsNACs.isEmpty()) {
            this.itsNACs.get(0).dispose(false, true);
            this.itsNACs.remove(0);
        }
        this.itsNACs.clear();
        while (!this.itsPACs.isEmpty()) {
            this.itsPACs.get(0).dispose(false, true);
            this.itsPACs.remove(0);
        }
        this.itsPACs.clear();
        while (!this.itsACs.isEmpty()) {
            this.itsACs.get(0).dispose(false, true);
            this.itsACs.remove(0);
        }
        this.itsACs.clear();
        this.disposeInverseConstruct();
        this.itsOrig.dispose();
        this.itsImag.dispose();
        this.itsMatch = null;
        this.typesWhichNeedMultiplicityCheck = null;
        this.changed = false;
    }

    /**
     * Sets the name of this rule.
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
     * Checks if this rule or any of its components (LHS, RHS) has changed.
     *
     * @return true if this rule or any of its graphs has changed, false
     * otherwise
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
     * Clears this rule by removing all application conditions and resetting the
     * graphs. This method disposes all NACs, PACs, and ACs, clears the
     * superclass, and resets the left and right graphs.
     */
    public void clearRule() {
        disposeMatch();
        while (!this.itsNACs.isEmpty()) {
            this.itsNACs.get(0).dispose(false, true);
            this.itsNACs.remove(0);
        }
        while (!this.itsPACs.isEmpty()) {
            this.itsPACs.get(0).dispose(false, true);
            this.itsPACs.remove(0);
        }
        while (!this.itsACs.isEmpty()) {
            this.itsACs.get(0).dispose(false, true);
            this.itsACs.remove(0);
        }
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
     * Disposes the results of all nested application conditions. This is useful
     * for freeing memory when the results are no longer needed.
     */
    public void disposeResultsOfNestedACs() {
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            ac.disposeResults();
        }
    }

    /**
     * Checks if the specified graph is part of this rule. This includes
     * checking if the graph is the LHS, RHS, or the target of any NAC, PAC, or
     * AC morphism.
     *
     * @param g the graph to check
     * @return true if the graph is part of this rule, false otherwise
     */
    public boolean isElement(Graph g) {
        if (this.itsOrig == g || this.itsImag == g) {
            return true;
        }
        for (int i = 0; i < this.itsNACs.size(); i++) {
            OrdinaryMorphism om = this.itsNACs.get(i);
            if (om.getTarget() == g) {
                return true;
            }
        }
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism om = this.itsPACs.get(i);
            if (om.getTarget() == g) {
                return true;
            }
        }
        for (int i = 0; i < this.itsACs.size(); i++) {
            OrdinaryMorphism om = this.itsACs.get(i);
            if (om.getTarget() == g) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the left-hand side graph of this rule.
     *
     * @return the left graph (LHS)
     */
    public final Graph getLeft() {
        return this.itsOrig;
    }

    /**
     * Returns the right-hand side graph of this rule.
     *
     * @return the right graph (RHS)
     */
    public final Graph getRight() {
        return this.itsImag;
    }

    /**
     * Checks if this rule is marked as not applicable.
     *
     * @return true if this rule is not applicable, false otherwise
     */
    public boolean isNotApplicable() {
        return this.notApplicable;
    }

    /**
     * Checks if this rule is applicable. A rule is applicable if it is not
     * explicitly marked as not applicable and the applicable flag is set to
     * true.
     *
     * @return true if this rule is applicable, false otherwise
     */
    public boolean isApplicable() {
        return !this.notApplicable && this.applicable;
    }

    /**
     * Checks whether this rule is applicable at the specified graph using the
     * specified matching strategy.
     *
     * <p>
     * <b>Precondition:</b> {@link #isReadyToTransform()} should be called
     * before invoking this method to ensure the rule is ready for
     * transformation.
     *
     * @param g the graph to check for applicability
     * @param strategy the matching completion strategy to use
     * @return true if this rule can be applied to the graph, false otherwise
     *
     * @see #isApplicable(Graph, MorphCompletionStrategy, boolean)
     * @see #isReadyToTransform()
     */
    public boolean isApplicable(Graph g, MorphCompletionStrategy strategy) {
        return isApplicable(g, strategy, false);
    }

    /**
     * Checks whether this rule is applicable at the specified graph using the
     * specified matching strategy. This method optionally checks if the rule is
     * ready to transform before checking applicability.
     *
     * @param g the graph to check for applicability
     * @param strategy the matching completion strategy to use
     * @param doCheckIfReadyToTransform if true, checks
     * {@link #isReadyToTransform()} before checking applicability
     * @return true if this rule can be applied to the graph, false otherwise
     *
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
     * Enables or disables all input parameters in this rule's attribute
     * context. Input parameters are variables that can be set from outside the
     * rule to influence the rule's behavior during application.
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
     * Enables or disables all attribute conditions that reference the specified
     * input parameter.
     *
     * @param ipName the name of the input parameter
     * @param enable true to enable conditions, false to disable them
     */
    private void enableAttrConditionWithInputParameter(final String ipName, final boolean enable) {
        CondTuple conds = (CondTuple) this.getAttrContext().getConditions();
        for (int i = 0; i < conds.getNumberOfEntries(); i++) {
            CondMember cond = conds.getCondMemberAt(i);
            if (cond.getAllVariables().contains(ipName)) {
                cond.setEnabled(enable);
            }
        }
    }

    /**
     * Checks whether the left-hand side of this rule can be matched in the
     * specified graph using the given matching completion strategy. This method
     * temporarily disables all NACs, PACs, and ACs to check only the basic
     * applicability of the LHS pattern.
     *
     * @param g the graph to check for LHS applicability
     * @param strategy the matching completion strategy to use
     * @param doCheckIfReadyToTransform if true, checks whether the rule is
     * ready to transform first
     * @return true if the LHS pattern can be found in the graph, false
     * otherwise
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
                    this.itsNACs.size() + this.itsPACs.size() + this.itsACs.size());
            // store nac.isEnabled() setting and disable nac 
            for (int i = 0; i < this.itsNACs.size(); i++) {
                OrdinaryMorphism nac = this.itsNACs.get(i);
                applcond2enable.put(nac, Boolean.valueOf(nac.isEnabled()));
                nac.setEnabled(false);
            }
            // store pac.isEnabled() setting and disable nac 
            for (int i = 0; i < this.itsPACs.size(); i++) {
                OrdinaryMorphism pac = this.itsPACs.get(i);
                applcond2enable.put(pac, Boolean.valueOf(pac.isEnabled()));
                pac.setEnabled(false);
            }
            // store ac.isEnabled() setting and disable ac 
            for (int i = 0; i < this.itsACs.size(); i++) {
                OrdinaryMorphism ac = this.itsACs.get(i);
                applcond2enable.put(ac, Boolean.valueOf(ac.isEnabled()));
                ac.setEnabled(false);
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
            for (int i = 0; i < this.itsNACs.size(); i++) {
                this.itsNACs.get(i).setEnabled(applcond2enable.get(this.itsNACs.get(i)).booleanValue());
            }
            for (int i = 0; i < this.itsPACs.size(); i++) {
                this.itsPACs.get(i).setEnabled(applcond2enable.get(this.itsPACs.get(i)).booleanValue());
            }
            for (int i = 0; i < this.itsACs.size(); i++) {
                this.itsACs.get(i).setEnabled(applcond2enable.get(this.itsACs.get(i)).booleanValue());
            }
        }
        return result;
    }

    /**
     * Enables or disables all negative application conditions (NACs) of this
     * rule.
     *
     * @param enable true to enable all NACs, false to disable them
     */
    public void enableNACs(boolean enable) {
        for (int i = 0; i < this.itsNACs.size(); i++) {
            this.itsNACs.get(i).setEnabled(enable);
        }
    }

    /**
     * Enables or disables all positive application conditions (PACs) of this
     * rule.
     *
     * @param enable true to enable all PACs, false to disable them
     */
    public void enablePACs(boolean enable) {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            this.itsPACs.get(i).setEnabled(enable);
        }
    }

    /**
     * Sets the applicability flag for this rule. This flag indicates whether
     * the rule can currently be applied during graph transformation.
     *
     * @param appl true if the rule should be considered applicable, false
     * otherwise
     */
    public void setApplicable(boolean appl) {
        this.applicable = appl;
    }

    /**
     * Returns the type set associated with this rule. The type set is derived
     * from the left-hand side graph of this rule.
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
     * Creates and adds a new nested application condition (GAC) to this rule.
     * Note: Because the new morphism is initially empty and the LHS graph is
     * not, it is not a morphism in theoretical terms, which demands an
     * application condition to be a total morphism.
     *
     * @return an empty nested application condition with the original set to
     * this rule's left-hand side graph
     */
    public NestedApplCond createNestedAC() {
        final NestedApplCond ac = new NestedApplCond(
                getLeft(),
                BaseFactory.theFactory().createGraph(getRight().getTypeSet()),
                getRight().getAttrContext());
        this.itsACs.add(ac);
        AttrContext acContext = ac.getAttrContext(); //getLeft().getAttrContext();
        ac.getImage().setAttrContext(acContext);
        ac.getImage().setKind(GraphKind.AC);
        return ac;
    }

    /**
     * Creates and adds a new nested application condition (GAC) to this rule.
     * The target graph of the new GAC is constructed based on the RHS of this
     * rule.
     *
     * @return a new nested application condition with target graph constructed
     * from the RHS
     */
    public NestedApplCond createNestedACDuetoRHS() {
        final NestedApplCond nac = createNestedAC();
        makeACDuetoRHS(nac);
        return nac;
    }

    /**
     * Adds the specified morphism representing a nested application condition.
     * <p>
     * <b>Precondition:</b> The AC's original graph must be this rule's
     * left-hand side graph.
     *
     * @param ac the nested application condition morphism to add
     * @return true if the AC was added successfully, false if it was already
     * present
     */
    public boolean addNestedAC(final OrdinaryMorphism ac) {
        return this.addNestedAC(-1, ac);
    }

    /**
     * Adds the specified morphism representing a nested application condition
     * at the specified index in the list.
     * <p>
     * <b>Precondition:</b> The AC's original graph must be this rule's
     * left-hand side graph.
     *
     * @param indx the index at which to insert the AC, or -1 to append to the
     * end
     * @param ac the nested application condition morphism to add
     * @return true if the AC was added successfully, false if it was already
     * present
     */
    public boolean addNestedAC(int indx, final OrdinaryMorphism ac) {
        if (!this.itsACs.contains(ac)) {
            ac.getTarget().setKind(GraphKind.AC);
            if (indx >= 0 && indx < this.itsACs.size()) {
                this.itsACs.add(indx, ac);
            } else {
                this.itsACs.add(ac);
            }
            this.changed = true;
            return true;
        }
        return false;
    }

    /**
     * Enables or disables all nested application conditions (ACs) of this rule.
     *
     * @param enable true to enable all ACs, false to disable them
     */
    public void enableNestedAC(boolean enable) {
        for (int i = 0; i < this.itsACs.size(); i++) {
            this.itsACs.get(i).setEnabled(enable);
        }
    }

    /**
     * Destroys the specified nested application condition and removes it from
     * this rule. The target graph of the AC morphism is also disposed.
     *
     * @param ac the nested application condition morphism to destroy
     */
    public void destroyNestedAC(final OrdinaryMorphism ac) {
        this.itsACs.remove(ac);
        ac.getImage().dispose();
    }

    /**
     * Checks if this rule contains any nested application conditions.
     *
     * @return true if the rule has at least one AC, false otherwise
     */
    public boolean hasNestedACs() {
        return !this.itsACs.isEmpty();
    }

    /**
     * Returns an iterator over all nested application conditions of this rule.
     *
     * @return an iterator of all AC morphisms
     */
    public Iterator<OrdinaryMorphism> getNestedACs() {
        return this.itsACs.iterator();
    }

    /**
     * Returns a list of all enabled nested application conditions of this rule.
     *
     * @return a list of all enabled AC morphisms
     */
    public List<NestedApplCond> getEnabledACs() {
        List<NestedApplCond> list = new ArrayList<>(this.itsACs.size());
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            if (ac.isEnabled()) {
                list.add(ac);
            }
        }
        return list;
    }

    /**
     * Returns the list of all nested application condition morphisms of this
     * rule.
     *
     * @return the list of AC morphisms
     */
    public List<OrdinaryMorphism> getNestedACsList() {
        return this.itsACs;
    }

    /**
     * Returns a list of all enabled nested application conditions as evaluable
     * objects.
     *
     * @return a list of all enabled AC morphisms as evaluable objects
     */
    public List<Evaluable> getEnabledGeneralACsAsEvaluable() {
        List<Evaluable> list = new ArrayList<>(this.itsACs.size());
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            if (ac.isEnabled()) {
                list.add(ac);
            }
        }
        return list;
    }

    /**
     * Returns the nested application condition morphism with the specified
     * name.
     *
     * @param name the name of the AC to find
     * @return the AC morphism with the specified name, or null if not found
     */
    public OrdinaryMorphism getNestedAC(String name) {
        for (int i = 0; i < this.itsACs.size(); i++) {
            OrdinaryMorphism ac = this.itsACs.get(i);
            if (ac.getName().equals(name)) {
                return ac;
            }
        }
        return null;
    }

    /**
     * Returns the nested application condition morphism at the specified index.
     *
     * @param indx the index of the AC to retrieve
     * @return the AC morphism at the specified index, or null if index is out
     * of bounds
     */
    public OrdinaryMorphism getNestedAC(int indx) {
        if (indx >= 0 && indx < this.itsACs.size()) {
            return this.itsACs.get(indx);
        } else {
            return null;
        }
    }

    /**
     * Removes the specified nested application condition from this rule. If the
     * AC was enabled, this method also updates the formula by patching out the
     * evaluable and refreshing the formula.
     *
     * @param ac the nested application condition morphism to remove
     * @return false if the AC was not found, true if it was removed
     * successfully
     */
    public final boolean removeNestedAC(OrdinaryMorphism ac) {
        boolean enAC = ac.isEnabled();
        if (this.itsACs.remove(ac)) {
            if (enAC) {
                this.itsFormula.patchOutEvaluable((NestedApplCond) ac, true);
                this.refreshFormula(new ArrayList<>(this.getEnabledACs()));
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if any nested application condition in this rule is using the
     * specified variable in the context of the specified attribute condition
     * tuple.
     *
     * @param var the variable member to check for usage
     * @param act the attribute condition tuple providing context
     * @return true if any AC uses the variable in the given context, false
     * otherwise
     */
    public boolean nestedACIsUsingVariable(
            final VarMember var,
            final AttrConditionTuple act) {
        for (int i = 0; i < this.itsACs.size(); i++) {
            final OrdinaryMorphism ac = this.itsACs.get(i);
            if (ac.getTarget().isUsingVariable(var)) {
                return true;
            }
            List<String> acVars = ac.getTarget()
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
     * Creates a new negative application condition (NAC) and adds it to this
     * rule. Note: Because the new morphism is initially empty and the LHS graph
     * is not, it is not a morphism in theoretical terms, which demands a NAC to
     * be a total morphism.
     *
     * @return an empty morphism with the original set to this rule's left-hand
     * side graph
     */
    public OrdinaryMorphism createNAC() {
        final OrdinaryMorphism nac = new OrdinaryMorphism(
                getLeft(),
                BaseFactory.theFactory().createGraph(getRight().getTypeSet()),
                getRight().getAttrContext());
        this.itsNACs.add(nac);
        AttrContext nacContext = nac.getAttrContext(); //getLeft().getAttrContext();
        nac.getImage().setAttrContext(nacContext);
        nac.getImage().setKind(GraphKind.NAC);
        return nac;
    }

    /**
     * Creates a new negative application condition (NAC) and adds it to this
     * rule. The target graph of the new NAC is constructed based on the RHS of
     * this rule.
     *
     * @return a new NAC with target graph constructed from the RHS
     */
    public OrdinaryMorphism createNACDuetoRHS() {
        final OrdinaryMorphism nac = createNAC();
        makeACDuetoRHS(nac);
        return nac;
    }

    /**
     * Constructs the target graph of the specified morphism based on the RHS of
     * this rule. This method copies nodes and arcs from the RHS to create a
     * target graph for application conditions (NACs, PACs, or ACs).
     *
     * @param morph the morphism whose target graph should be constructed
     */
    public void makeACDuetoRHS(final OrdinaryMorphism morph) {
        HashMap<Node, Node> map = new HashMap<>();
        Iterator<Node> nodes = this.itsImag.getNodesSet().iterator();
        while (nodes.hasNext()) {
            Node nr = nodes.next();
            Iterator<GraphObject> l = this.getInverseImage(nr);
            if (l.hasNext()) {
                Node nl = (Node) l.next();
                try {
                    Node n = morph.getTarget().copyNode(nl);
                    try {
                        morph.addMapping(nl, n);
                        while (l.hasNext()) {
                            morph.addMapping((Node) l.next(), n);
                        }
                        map.put(nr, n);
                    } catch (BadMappingException ex) {
                    }
                } catch (TypeException e) {
                }
            } else {
                try {
                    Node n = morph.getTarget().copyNode(nr);
                    if (n.getAttribute() != null) {
                        ((agg.attribute.impl.ValueTuple) n.getAttribute()).unsetValueAsExpr();
                    }
                    map.put(nr, n);
                } catch (TypeException e) {
                }
            }
        }
        Iterator<Arc> arcs = this.itsImag.getArcsSet().iterator();
        while (arcs.hasNext()) {
            Arc ar = arcs.next();
            Iterator<GraphObject> l = this.getInverseImage(ar);
            if (l.hasNext()) {
                Arc al = (Arc) l.next();
                try {
                    Arc a = morph.getTarget().copyArc(al, (Node) morph.getImage(al.getSource()), (Node) morph.getImage(al.getTarget()));
                    try {
                        morph.addMapping(al, a);
                        while (l.hasNext()) {
                            morph.addMapping(l.next(), a);
                        }
                    } catch (BadMappingException ex) {
                    }
                } catch (TypeException e) {
                }
            } else {
                try {
                    Node s = (Node) map.get(ar.getSource());
                    Node t = (Node) map.get(ar.getTarget());
                    Arc a = morph.getTarget().copyArc(ar, s, t);
                    if (a.getAttribute() != null) {
                        ((agg.attribute.impl.ValueTuple) a.getAttribute()).unsetValueAsExpr();
                    }
                } catch (TypeException e) {
                }
            }
        }
        map.clear();
        map = null;
    }

    /**
     * Adds the specified morphism representing a negative application condition
     * (NAC).
     * <p>
     * <b>Precondition:</b> The NAC's original graph must be this rule's
     * left-hand side graph.
     *
     * @param nac the negative application condition morphism to add
     * @return true if the NAC was added successfully, false if it was already
     * present
     */
    public boolean addNAC(final OrdinaryMorphism nac) {
        return this.addNAC(-1, nac);
    }

    /**
     * Adds the specified morphism representing a negative application condition
     * (NAC) at the specified index in the list.
     * <p>
     * <b>Precondition:</b> The NAC's original graph must be this rule's
     * left-hand side graph.
     *
     * @param indx the index at which to insert the NAC, or -1 to append to the
     * end
     * @param nac the negative application condition morphism to add
     * @return true if the NAC was added successfully, false if it was already
     * present
     */
    public boolean addNAC(int indx, final OrdinaryMorphism nac) {
        if (!this.itsNACs.contains(nac)) {
            nac.getTarget().setKind(GraphKind.NAC);
            if (indx >= 0 && indx < this.itsNACs.size()) {
                this.itsNACs.add(indx, nac);
            } else {
                this.itsNACs.add(nac);
            }
            this.changed = true;
            return true;
        }
        return false;
    }

    /**
     * Destroys the specified negative application condition and removes it from
     * this rule. The target graph of the NAC morphism is also disposed.
     *
     * @param nac the negative application condition morphism to destroy
     */
    public void destroyNAC(OrdinaryMorphism nac) {
        this.itsNACs.remove(nac);
        nac.getImage().dispose();
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
     * Checks if this rule has at least one enabled negative application
     * condition.
     *
     * @return true if the rule has at least one enabled NAC, false otherwise
     */
    public boolean hasEnabledNACs() {
        for (OrdinaryMorphism n : this.itsNACs) {
            if (n.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator over all negative application conditions of this
     * rule.
     *
     * @return an iterator of all NAC morphisms
     */
    public Iterator<OrdinaryMorphism> getNACs() {
        return this.itsNACs.iterator();
    }

    /**
     * Returns the list of all negative application condition morphisms of this
     * rule.
     *
     * @return the list of NAC morphisms
     */
    public List<OrdinaryMorphism> getNACsList() {
        return this.itsNACs;
    }

    /**
     * Returns the negative application condition morphism with the specified
     * name.
     *
     * @param name the name of the NAC to find
     * @return the NAC morphism with the specified name, or null if not found
     */
    public OrdinaryMorphism getNAC(String name) {
        for (int i = 0; i < this.itsNACs.size(); i++) {
            OrdinaryMorphism nac = this.itsNACs.get(i);
            if (nac.getName().equals(name)) {
                return nac;
            }
        }
        return null;
    }

    /**
     * Returns the negative application condition morphism at the specified
     * index.
     *
     * @param indx the index of the NAC to retrieve
     * @return the NAC morphism at the specified index, or null if index is out
     * of bounds
     */
    public OrdinaryMorphism getNAC(int indx) {
        if (indx >= 0 && indx < this.itsNACs.size()) {
            return this.itsNACs.get(indx);
        } else {
            return null;
        }
    }

    /**
     * Returns the negative application condition morphism with the specified
     * target graph.
     *
     * @param g the target graph to search for
     * @return the NAC morphism with the specified target graph, or null if not
     * found
     */
    public OrdinaryMorphism getNAC(final Graph g) {
        for (int i = 0; i < this.itsNACs.size(); i++) {
            OrdinaryMorphism ac = this.itsNACs.get(i);
            if (ac.getTarget() == g) {
                return ac;
            }
        }
        return null;
    }

    /**
     * Checks if the specified graph is the target graph of any negative
     * application condition.
     *
     * @param g the graph to check
     * @return true if the graph is a target of any NAC, false otherwise
     */
    public boolean hasNAC(final Graph g) {
        for (int i = 0; i < this.itsNACs.size(); i++) {
            OrdinaryMorphism ac = this.itsNACs.get(i);
            if (ac.getTarget() == g) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified negative application condition from this rule.
     *
     * @param nac the negative application condition morphism to remove
     * @return false if the NAC was not found, true if it was removed
     * successfully
     */
    public final boolean removeNAC(OrdinaryMorphism nac) {
        return this.itsNACs.remove(nac);
    }

    /**
     * Creates a new positive application condition (PAC) and adds it to this
     * rule. Note: Because the new morphism is initially empty and the LHS graph
     * is not, it is not a morphism in theoretical terms, which demands a PAC to
     * be a total morphism.
     *
     * @return an empty morphism with the original set to this rule's left-hand
     * side graph
     */
    public OrdinaryMorphism createPAC() {
        final OrdinaryMorphism pac = new OrdinaryMorphism(
                getLeft(),
                BaseFactory.theFactory().createGraph(getRight().getTypeSet()),
                getRight().getAttrContext());
        this.itsPACs.add(pac);
        AttrContext pacContext = pac.getAttrContext(); //getLeft().getAttrContext();
        pac.getImage().setAttrContext(pacContext);
        pac.getImage().setKind(GraphKind.PAC);
        return pac;
    }

    /**
     * Adds the specified morphism representing a positive application condition
     * (PAC).
     * <p>
     * <b>Precondition:</b> The PAC's original graph must be this rule's
     * left-hand side graph.
     *
     * @param pac the positive application condition morphism to add
     * @return true if the PAC was added successfully, false if it was already
     * present
     */
    public boolean addPAC(final OrdinaryMorphism pac) {
        return this.addPAC(-1, pac);
    }

    /**
     * Adds the specified morphism representing a positive application condition
     * (PAC) at the specified index in the list.
     * <p>
     * <b>Precondition:</b> The PAC's original graph must be this rule's
     * left-hand side graph.
     *
     * @param indx the index at which to insert the PAC, or -1 to append to the
     * end
     * @param pac the positive application condition morphism to add
     * @return true if the PAC was added successfully, false if it was already
     * present
     */
    public boolean addPAC(int indx, final OrdinaryMorphism pac) {
        if (!this.itsPACs.contains(pac)) {
            pac.getTarget().setKind(GraphKind.PAC);
            if (indx >= 0 && indx < this.itsPACs.size()) {
                this.itsPACs.add(indx, pac);
            } else {
                this.itsPACs.add(pac);
            }
            this.changed = true;
            return true;
        }
        return false;
    }

    /**
     * Adds a new shifted positive application condition composed of the
     * specified list of morphisms.
     *
     * @param list the list of morphisms that form the shifted PAC
     */
    public void addShiftedPAC(final List<OrdinaryMorphism> list) {
        final ShiftedPAC shiftedPAC = new ShiftedPAC(list);
        if (this.itsShiftedPACs == null) {
            itsShiftedPACs = new ArrayList<>();
        }
        this.itsShiftedPACs.add(shiftedPAC);
    }

    /**
     * Returns the list of all shifted positive application conditions.
     *
     * @return the list of shifted PACs, may be null if none exist
     */
    public List<ShiftedPAC> getShiftedPACs() {
        return this.itsShiftedPACs;
    }

    /**
     * Checks if the specified morphism is part of any shifted positive
     * application condition.
     *
     * @param pac the morphism to check
     * @return true if the morphism is part of a shifted PAC, false otherwise
     */
    public boolean isShiftedPAC(final OrdinaryMorphism pac) {
        if (this.itsShiftedPACs == null) {
            return false;
        }
        for (int i = 0; i < this.itsShiftedPACs.size(); i++) {
            if (this.itsShiftedPACs.get(i).contains(pac)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Destroys the specified positive application condition and removes it from
     * this rule. The target graph of the PAC morphism is also disposed.
     *
     * @param pac the positive application condition morphism to destroy
     */
    public void destroyPAC(final OrdinaryMorphism pac) {
        this.itsPACs.remove(pac);
        pac.getImage().dispose();
    }

    /**
     * Checks if this rule contains any positive application conditions.
     *
     * @return true if the rule has at least one PAC, false otherwise
     */
    public boolean hasPACs() {
        return !this.itsPACs.isEmpty();
    }

    /**
     * Checks if this rule has at least one enabled positive application
     * condition.
     *
     * @return true if the rule has at least one enabled PAC, false otherwise
     */
    public boolean hasEnabledPACs() {
        for (OrdinaryMorphism p : this.itsPACs) {
            if (p.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator over all positive application conditions of this
     * rule.
     *
     * @return an iterator of all PAC morphisms
     */
    public Iterator<OrdinaryMorphism> getPACs() {
        return this.itsPACs.iterator();
    }

    /**
     * Returns an iterator over all enabled positive application conditions of
     * this rule.
     *
     * @return an iterator of all enabled PAC morphisms
     */
    public Iterator<OrdinaryMorphism> getEnabledPACs() {
        List<OrdinaryMorphism> v = new ArrayList<>(2);
        for (OrdinaryMorphism p : this.itsPACs) {
            if (p.isEnabled()) {
                v.add(p);
            }
        }
        return v.iterator();
    }

    /**
     * Returns the list of all positive application condition morphisms of this
     * rule.
     *
     * @return the list of PAC morphisms
     */
    public List<OrdinaryMorphism> getPACsList() {
        return this.itsPACs;
    }

    /**
     * Returns the positive application condition morphism with the specified
     * name.
     *
     * @param name the name of the PAC to find
     * @return the PAC morphism with the specified name, or null if not found
     */
    public OrdinaryMorphism getPAC(String name) {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism pac = this.itsPACs.get(i);
            if (pac.getName().equals(name)) {
                return pac;
            }
        }
        return null;
    }

    /**
     * Returns the positive application condition morphism at the specified
     * index.
     *
     * @param indx the index of the PAC to retrieve
     * @return the PAC morphism at the specified index, or null if index is out
     * of bounds
     */
    public OrdinaryMorphism getPAC(int indx) {
        if (indx >= 0 && indx < this.itsPACs.size()) {
            return this.itsPACs.get(indx);
        } else {
            return null;
        }
    }

    /**
     * Returns the positive application condition morphism with the specified
     * target graph.
     *
     * @param g the target graph to search for
     * @return the PAC morphism with the specified target graph, or null if not
     * found
     */
    public OrdinaryMorphism getPAC(final Graph g) {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism ac = this.itsPACs.get(i);
            if (ac.getTarget() == g) {
                return ac;
            }
        }
        return null;
    }

    /**
     * Checks if the specified graph is the target graph of any positive
     * application condition.
     *
     * @param g the graph to check
     * @return true if the graph is a target of any PAC, false otherwise
     */
    public boolean hasPAC(final Graph g) {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism ac = this.itsPACs.get(i);
            if (ac.getTarget() == g) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified positive application condition from this rule.
     *
     * @param pac the positive application condition morphism to remove
     * @return false if the PAC was not found, true if it was removed
     * successfully
     */
    public final boolean removePAC(OrdinaryMorphism pac) {
        return this.itsPACs.remove(pac);
    }

    // /////////////////////////////////////
    /**
     * Checks if the specified node type can be used to create a node in the
     * RHS. Returns false if the node type is abstract and used in the RHS to
     * create a node, otherwise returns true.
     *
     * @param nodeType the node type to check
     * @return false if the node type is abstract and used in RHS, true
     * otherwise
     */
    public boolean checkCreateAbstractNode(Type nodeType) {
        Iterator<Node> en = getTarget().getNodesSet().iterator();
        while (en.hasNext()) {
            Node n = en.next();
            if (n.getType().equals(nodeType)) {
                if (!this.getInverseImage(n).hasNext()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if any node in the RHS that has no preimage in the LHS violates
     * type constraints regarding required arcs. Returns a TypeError if such a
     * violation is found, otherwise null.
     *
     * @return a TypeError describing the constraint violation, or null if all
     * nodes satisfy constraints
     */
    public TypeError checkNewNodeRequiresArc() {
        final Iterator<Node> elems = this.getRight().getNodesSet().iterator();
        while (elems.hasNext()) {
            final GraphObject obj = elems.next();
            if (!this.getInverseImage(obj).hasNext()) {
                List<String> list = this.getRight().getTypeSet().nodeRequiresArc((Node) obj);
                if (list != null && !list.isEmpty()) {
                    TypeError actError = new TypeError(TypeError.TO_LESS_ARCS,
                            "Node type  "
                            + "\"" + obj.getType().getName() + "\" \n"
                            + "requires edge(s) of type: \n"
                            + list.toString(), obj.getType());
                    actError.setContainingGraph(this.getRight());
                    return actError;
                }
            }
        }
        return null;
    }

    /**
     * Attempts to destroy all graph objects of the specified type from all
     * graphs associated with this rule (LHS, RHS, NACs, PACs, nested ACs).
     *
     * @param t the type of graph objects to destroy
     * @return true if all objects of the specified type were successfully
     * destroyed, false otherwise
     */
    public boolean destroyObjectsOfType(Type t) {
        if (getLeft().destroyObjectsOfType(t)) {
            if (getRight().destroyObjectsOfType(t)) {
                for (int j = 0; j < this.itsNACs.size(); j++) {
                    OrdinaryMorphism nac = this.itsNACs.get(j);
                    if (!nac.getTarget().destroyObjectsOfType(t)) {
                        return false;
                    }
                }
                for (int j = 0; j < this.itsPACs.size(); j++) {
                    OrdinaryMorphism pac = this.itsPACs.get(j);
                    if (!pac.getTarget().destroyObjectsOfType(t)) {
                        return false;
                    }
                }
                for (int j = 0; j < this.itsACs.size(); j++) {
                    OrdinaryMorphism ac = this.itsACs.get(j);
                    if (!ac.getTarget().destroyObjectsOfType(t)) {
                        return false;
                    }
                }
                // delete from rule application conditions
                List<EvalSet> atom_conds = getAtomApplConds();
                for (int i = 0; i < atom_conds.size(); i++) {
                    List<?> v = atom_conds.get(i).getSet();
                    for (int j = 0; j < v.size(); j++) {
                        List<?> v1 = ((EvalSet) v.get(j)).getSet();
                        for (int k = 0; k < v1.size(); k++) {
                            agg.cons.AtomApplCond aac = (agg.cons.AtomApplCond) v1
                                    .get(k);
                            OrdinaryMorphism cond = aac.getPreCondition();
                            OrdinaryMorphism tm = aac.getT();
                            OrdinaryMorphism qm = aac.getQ();
                            cond.getSource().destroyObjectsOfType(t);
                            cond.getTarget().destroyObjectsOfType(t);
                            tm.getTarget().destroyObjectsOfType(t);
                            qm.getSource().destroyObjectsOfType(t);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Attempts to destroy all graph objects of the specified types from all
     * graphs associated with this rule (LHS, RHS, NACs, PACs, nested ACs).
     *
     * @param types the list of types whose objects should be destroyed
     * @return a list of names of types that could not be destroyed completely
     */
    public List<String> destroyObjectsOfTypes(List<Type> types) {
        List<String> failed = new ArrayList<>(5);
        for (int i = 0; i < types.size(); i++) {
            Type t = types.get(i);
            if (!destroyObjectsOfType(t)) {
                String s = "Rule:  " + getName() + "   Type:  " + t.getName();
                failed.add(s);
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
     * @return a new Rule instance that is a copy of this rule with the
     * specified types
     */
    public Rule getClone(TypeSet types) {
        return BaseFactory.theFactory().cloneRule(this, types, true);
    }

    /**
     * Returns the morphism between the left and right graphs of this rule. This
     * is the rule itself, as Rule extends OrdinaryMorphism.
     *
     * @return this rule as an OrdinaryMorphism
     */
    public final OrdinaryMorphism getMorphism() {
        return this;
    }

    /**
     * Returns the list of graph constraints that can be converted to
     * post-application constraints.
     *
     * @return the list of formula constraints, or an empty list if none exist
     */
    public List<Formula> getConstraints() {
        return (this.constraints != null) ? this.constraints : new ArrayList<>(0);
    }

    /**
     * Checks the type compatibility of two graph objects. The first object
     * should belong to the LHS, the second to the RHS, to be used for a mapping
     * of the rule morphism.
     *
     * @param orig the original type from the LHS
     * @param image the image type from the RHS
     * @return true if the types are compatible, false otherwise
     */
    protected boolean checkType(Type orig, Type image) {
        return orig.compareTo(image);
    }

    /**
     * Creates attribute instances where needed for all graphs in this rule
     * (LHS, RHS, NACs, PACs, nested ACs).
     */
    public void createAttrInstanceWhereNeeded() {
        this.itsOrig.createAttrInstanceWhereNeeded();
        this.itsImag.createAttrInstanceWhereNeeded();
        for (int i = 0; i < this.itsNACs.size(); i++) {
            this.itsNACs.get(i).getTarget().createAttrInstanceWhereNeeded();
        }
        for (int i = 0; i < this.itsPACs.size(); i++) {
            this.itsPACs.get(i).getTarget().createAttrInstanceWhereNeeded();
        }
        for (int i = 0; i < this.itsACs.size(); i++) {
            this.itsACs.get(i).getTarget().createAttrInstanceWhereNeeded();
        }
    }

    /**
     * Creates attribute instances of the specified type where needed for all
     * graphs in this rule (LHS, RHS, NACs, PACs, nested ACs).
     *
     * @param t the type for which to create attribute instances
     */
    public void createAttrInstanceOfTypeWhereNeeded(final Type t) {
        this.itsOrig.createAttrInstanceOfTypeWhereNeeded(t);
        this.itsImag.createAttrInstanceOfTypeWhereNeeded(t);
        for (int i = 0; i < this.itsNACs.size(); i++) {
            this.itsNACs.get(i).getTarget().createAttrInstanceOfTypeWhereNeeded(t);
        }
        for (int i = 0; i < this.itsPACs.size(); i++) {
            this.itsPACs.get(i).getTarget().createAttrInstanceOfTypeWhereNeeded(t);
        }
        for (int i = 0; i < this.itsACs.size(); i++) {
            this.itsACs.get(i).getTarget().createAttrInstanceOfTypeWhereNeeded(t);
        }
    }

    /**
     * Generates rule post application conditions from its constraints
     * (formulas). Returns an error message if something went wrong, otherwise
     * an empty string.
     *
     * @return an error message if conversion failed, or empty string if
     * successful
     */
    public String convertUsedFormulas() {
        if (this.itsUsedAtomics != null && this.itsUsedAtomics.size() > 0
                && this.itsUsedFormulas != null && this.itsUsedFormulas.size() > 0) {
            String msg = "";
            List<EvalSet> fin = new ArrayList<>();
            List<String> names = new ArrayList<>();
            // clear Post Appl. Conditions
            if (this.constraints == null) {
                constraints = new ArrayList<>();
            } else {
                this.constraints.clear();
            }
            setAtomApplConds(null, null);
            final Map<AtomConstraint, EvalSet> atomic2set = new HashMap<>();
            final Map<String, String> failedAtomic2error = new HashMap<>();
            int tgLevel = this.getTypeSet().getLevelOfTypeGraphCheck();
            if (tgLevel > TypeSet.ENABLED_MAX) {
                this.getTypeSet().setLevelOfTypeGraph(TypeSet.ENABLED_MAX);
            }
            for (int j = 0; j < this.itsUsedAtomics.size(); j++) {
                AtomConstraint a = this.itsUsedAtomics.get(j);
                if (!a.isValid()) {
                    msg = "Atomic  \"" + a.getAtomicName() + "\"  is not valid.";
                    this.itsUsedAtomics.clear();
                    this.itsUsedFormulas.clear();
                    return msg;
                }
                ((AttrTupleManager) AttrTupleManager.getDefaultManager())
                        .setVariableContext(true);
                Convert conv = new Convert(this, a);
                List<Object> v = conv.convert();
                ((AttrTupleManager) AttrTupleManager.getDefaultManager())
                        .setVariableContext(false);
                final EvalSet set = new EvalSet(v);
                fin.add(set);
                names.add(a.getAtomicName());
                if (!v.isEmpty()) {
                    atomic2set.put(a, set);
                }
                if (!"".equals(conv.getErrorMsg())) {
                    failedAtomic2error.put(a.getAtomicName(), conv.getErrorMsg());
                }
            }
            this.getTypeSet().setLevelOfTypeGraph(tgLevel);
            if (!failedAtomic2error.isEmpty()) {
                msg = "Error(s) during creating Post Application Condition:";
            }
            for (int j = 0; j < this.itsUsedFormulas.size(); j++) {
                Formula f = this.itsUsedFormulas.get(j);
                if (!f.isEnabled()) {
                    continue;
                }
                List<Evaluable> v = new ArrayList<>();
                String s = f.getAsString(v);
                //			System.out.println(s);
                //			System.out.println(v);
                // In v the atomics used in f are noted.
                // In fin the set of _all_new atomics are noted
                // (though they are real formulas now) in the original order.
                // This means, we need a translation.
                // I.e. we build a new List as the source of a new formula
                // only containing the base formulas
                // corresponding to the atomic at that index.
                boolean formulaOK = true;
                List<Evaluable> v2 = new ArrayList<>();
                for (int k = 0; k < v.size(); k++) {
                    Object e = v.get(k);
                    boolean convertOK = false;
                    int k2;
                    for (k2 = 0; k2 < this.itsUsedAtomics.size(); k2++) {
                        if (this.itsUsedAtomics.get(k2) == e) {
                            final String atomicName = this.itsUsedAtomics.get(k2).getAtomicName();
                            //						System.out.println(atomicName));
                            Evaluable set = atomic2set.get(e);
                            if (set != null) {
                                v2.add(set);
                                convertOK = true;
                                break;
                            }
                            int indx = names.indexOf(atomicName);
                            fin.remove(indx);
                            names.remove(indx);
                        }
                    }
                    if (!convertOK) {
                        formulaOK = false;
                        break;
                    }
                }
                if (formulaOK) {
                    Formula f2 = new Formula(v2, s);
                    this.constraints.add(f2);
                }
            }
            if (fin.isEmpty()) {
                this.itsUsedAtomics.clear();
                this.itsUsedFormulas.clear();
            } else {
                this.setAtomApplConds(fin, names);
            }
            deleteTransientContextVariables(getSource());
            deleteTransientContextVariables(getTarget());
            this.removeUnusedVariableOfAttrContext();
            String msg1 = "Cannot convert atomic(s) :\n";
            String msg2 = "";
            final Iterator<String> failedAtomic = failedAtomic2error.keySet().iterator();
            while (failedAtomic.hasNext()) {
                String name = failedAtomic.next();
                String error = failedAtomic2error.get(name);
                msg2 = msg2.concat(" - ").concat(name).concat(" - ").concat("\n");
                msg2 = msg2.concat(error).concat("\n");
            }
            if (!"".equals(msg2)) {
                msg1 = msg1.concat(msg2);
                msg = msg.concat(msg1);
            }
            return msg;
        } else {
            return "Cannot create post application conditions. There isn't any formula selected.";
        }
    }

    /**
     * Sets the constraints (formulas) which will be used for generating post
     * application conditions. This method also extracts any atomic constraints
     * from the formulas.
     *
     * @param formulasToUse the list of formulas to use for generating post
     * application conditions
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
            for (int i = 0; i < this.itsUsedFormulas.size(); i++) {
                Formula f = this.itsUsedFormulas.get(i);
                List<Evaluable> vec = new ArrayList<>();
                String form = f.getAsString(vec);
                for (int j = 0; j < vec.size(); j++) {
                    if (vec.get(j) instanceof AtomConstraint) {
                        AtomConstraint ac = (AtomConstraint) vec.get(j);
                        this.itsUsedAtomics.add(ac);
                    } else {
                        System.out
                                .println("Rule.setUsedFormulas(List<Formula> usedFormulas):  formula: "
                                        + form + "   FAILED!");
                    }
                }
            }
        }
    }

    /**
     * Returns a list of atomic graph constraints used for generating post
     * application conditions. The elements are of type agg.cons.AtomConstraint.
     *
     * @return a list of atomic constraints, or an empty list if none exist
     */
    public List<AtomConstraint> getUsedAtomics() {
        return (this.itsUsedAtomics != null) ? this.itsUsedAtomics : new ArrayList<>(0);
    }

    /**
     * Returns the list of constraints (formulas) used for generating post
     * application conditions. The elements are of type agg.cons.Formula.
     *
     * @return a list of formulas, or an empty list if none exist
     */
    public List<Formula> getUsedFormulas() {
        return (this.itsUsedFormulas != null) ? this.itsUsedFormulas : new ArrayList<>(0);
    }

    /**
     * Clears all lists of graph constraints if the specified atomic graph
     * constraint belongs to this rule's constraints.
     *
     * @param ac the atomic constraint to check for presence before clearing
     */
    public void clearConstraints(AtomConstraint ac) {
        if (this.itsUsedAtomics != null && this.itsUsedAtomics.contains(ac)) {
            this.clearConstraints();
        }
    }

    /**
     * Clears all lists of graph constraints if the specified formula constraint
     * belongs to this rule's constraints.
     *
     * @param f the formula to check for presence before clearing
     */
    public void clearConstraints(Formula f) {
        if (this.itsUsedFormulas != null && this.itsUsedFormulas.contains(f)) {
            this.clearConstraints();
        }
    }

    /**
     * Clears all lists of graph constraints (used atomics, used formulas,
     * constraints, and atom application conditions).
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
     * @param v the list of evaluation sets representing post application
     * conditions
     * @param names the list of names corresponding to the evaluation sets
     */
    public void setAtomApplConds(List<EvalSet> v, List<String> names) {
        if (this.atom_conditions == null) {
            atom_conditions = new ArrayList<>();
        } else {
            this.atom_conditions.clear();
        }
        if (this.constraintNameSet == null) {
            constraintNameSet = new ArrayList<>();
        } else {
            this.constraintNameSet.clear();
        }
        if (v != null) {
            this.atom_conditions.addAll(v);
        }
        if (names != null) {
            this.constraintNameSet.addAll(names);
        }
        if (this.constraintNameSet.size() < this.atom_conditions.size()) {
            for (int i = this.constraintNameSet.size(); i < this.atom_conditions.size(); i++) {
                this.constraintNameSet.add("Unknown Name " + i);
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
     * Removes the specified evaluation set constraint from the post application
     * conditions.
     *
     * @param constraint the evaluation set constraint to remove
     */
    public void removeConstraint(EvalSet constraint) {
        if (this.atom_conditions != null && this.atom_conditions.contains(constraint)) {
            int i = this.atom_conditions.indexOf(constraint);
            this.atom_conditions.remove(constraint);
            this.constraintNameSet.remove(i);
        }
    }

    /**
     * Removes the specified atomic application condition from the post
     * application conditions. This method recursively searches through the
     * nested structure of evaluation sets to find and remove the specified
     * atomic condition.
     *
     * @param atom the atomic application condition to remove
     */
    public void removeAtomApplCond(AtomApplCond atom) {
        if (this.atom_conditions != null) {
            int i = 0;
            while (i < this.atom_conditions.size()) {
                List<?> v = this.atom_conditions.get(i).getSet();
                int j = 0;
                while (j < v.size()) {
                    List<?> v1 = ((EvalSet) v.get(j)).getSet();
                    int k = 0;
                    while (k < v1.size()) {
                        AtomApplCond aac = (AtomApplCond) v1.get(k);
                        if (atom.equals(aac)) {
                            v1.remove(atom);
                            // System.out.println("AtomApplCond: DONE");
                            k = v1.size();
                        } else {
                            k++;
                        }
                    }
                    if (v1.isEmpty()) {
                        v.remove(j);
                        j = v.size();
                    } else {
                        j++;
                    }
                }
                if (v.isEmpty()) {
                    this.atom_conditions.remove(i);
                    this.constraintNameSet.remove(i);
                    i = this.atom_conditions.size();
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Clears all application constraints (formulas, atomics, etc.) from this
     * rule.
     */
    public void removeApplConditions() {
        clearConstraints();
    }

    /**
     * Set this rule to be a trigger rule of its layer. That means: This rule
     * will be the first rule to apply on its layer. It can be applyed one time
     * only. All other rules on this layer can be applyed so long as possible.
     *
     * @param trigger
     */
    public void setTriggerForLayer(boolean trigger) {
        this.triggerOfLayer = trigger;
    }

    /**
     * Checks if this rule is a trigger rule of its layer.
     *
     * @return
     */
    public boolean isTriggerOfLayer() {
        return this.triggerOfLayer;
    }

    /**
     * Returns the layer associated with this rule. The layer is used by layered
     * grammars to organize rules in layers.
     *
     * @return the layer number of this rule
     */
    public int getLayer() {
        return this.layer;
    }

    /**
     * Sets the layer associated with this rule. The layer is used by layered
     * grammars to organize rules in layers.
     *
     * @param l the layer number to assign to this rule
     */
    public void setLayer(int l) {
        this.layer = l;
    }

    /**
     * Returns the priority of this rule. The priority can be used during graph
     * transformation to determine rule application order.
     *
     * @return the priority number of this rule
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * Sets the priority of this rule. The priority can be used during graph
     * transformation to determine rule application order.
     *
     * @param p the priority number to assign to this rule
     */
    public void setPriority(int p) {
        this.priority = p;
    }

    /**
     * Trims the capacity of used lists to match their current size. This method
     * optimizes memory usage by reducing the internal capacity of the list of
     * used atomic constraints.
     */
    public void trimToSize() {
        if (this.itsUsedAtomics != null) {
            for (int i = 0; i < this.itsUsedAtomics.size(); i++) {
                this.itsUsedAtomics.get(i).trimToSize();
            }
        }
    }

    /**
     * Refreshes the attributed state for all graphs in this rule (LHS, RHS,
     * NACs, PACs, nested ACs).
     */
    public void refreshAttributed() {
        getLeft().refreshAttributed();
        getRight().refreshAttributed();
        for (int i = 0; i < this.itsNACs.size(); i++) {
            this.itsNACs.get(i).getTarget().refreshAttributed();
        }
        for (int i = 0; i < this.itsPACs.size(); i++) {
            this.itsPACs.get(i).getTarget().refreshAttributed();
        }
        for (int i = 0; i < this.itsACs.size(); i++) {
            this.itsACs.get(i).getTarget().refreshAttributed();
        }
    }

    /**
     * Checks if this rule or any of its application conditions (NACs, PACs,
     * nested ACs) are using the specified type graph object (node or edge).
     *
     * @param typeObj the type graph object to check for usage
     * @return true if the type object is used anywhere in this rule, false
     * otherwise
     */
    public boolean isUsingType(GraphObject typeObj) {
        if (getLeft().isUsingType(typeObj)) {
            return true;
        }
        if (getRight().isUsingType(typeObj)) {
            return true;
        }
        for (int i = 0; i < this.itsNACs.size(); i++) {
            if (this.itsNACs.get(i).getTarget().isUsingType(typeObj)) {
                return true;
            }
        }
        for (int i = 0; i < this.itsPACs.size(); i++) {
            if (this.itsPACs.get(i).getTarget().isUsingType(typeObj)) {
                return true;
            }
        }
        for (int i = 0; i < this.itsACs.size(); i++) {
            this.itsACs.get(i).getTarget().refreshAttributed();
        }
        return false;
    }

    @Override
    public void removeUnusedVariableOfAttrContext() {
        DeclTuple vars = ((VarTuple) this.getAttrContext().getVariables()).getTupleType();
        for (int i = 0; i < vars.getNumberOfEntries(); i++) {
            DeclMember vm = (DeclMember) vars.getMemberAt(i);
            String var = vm.getName();
            if (!this.getSource().getVariableNamesOfAttributes().contains(var)) {
                if (!this.getRight().getVariableNamesOfAttributes().contains(var)) {
                    if (!isUsedInTargetGraph(this.getNACs(), var)) {
                        if (!isUsedInTargetGraph(this.getPACs(), var)) {
                            if (!isUsedInNestedGraphs(this.getNestedACs(), var)) {
                                vars.getTupleType().deleteMemberAt(var);
                                //							System.out.println("Rule.removeVariableOfAttrContext::  removed: "+var);
                                i--;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isUsedInTargetGraph(Iterator<OrdinaryMorphism> iter, String varName) {
        while (iter.hasNext()) {
            Graph g = iter.next().getTarget();
            if (g.getVariableNamesOfAttributes().contains(varName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsedInNestedGraphs(Iterator<OrdinaryMorphism> iter, String varName) {
        while (iter.hasNext()) {
            OrdinaryMorphism m = iter.next();
            if (m.getTarget().getVariableNamesOfAttributes().contains(varName)) {
                return true;
            }
            if (m instanceof NestedApplCond nestedApplCond) {
                List<OrdinaryMorphism> nl = new ArrayList<>();
                for (int i = 0; i < nestedApplCond.getNestedACs().size(); i++) {
                    nl.add(nestedApplCond.getNestedACs().get(i));
                }
                if (isUsedInNestedGraphs(nl.iterator(), varName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setWaitBeforeApplyEnabled(boolean b) {
        this.waitBeforeApply = b;
    }

    public boolean isWaitBeforeApplyEnabled() {
        return this.waitBeforeApply;
    }

    /**
     * Implements the interface of XMLObject
     *
     * @param h
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
        AttrContext context = getAttrContext();
        h.addObject("", context.getVariables(), true);
        getSource().setKind(GraphKind.LHS);
        h.addObject("", getSource(), true);
        getSource().setKind(GraphKind.RHS);
        h.addObject("", getTarget(), true);
//		String namestr = this.getName();
        writeMorphism(h);
        // NACs
        Iterator<OrdinaryMorphism> nacs = getNACs();
        // PACs
        Iterator<OrdinaryMorphism> pacs = getPACs();
        // nested ACs
        Iterator<OrdinaryMorphism> nested = getNestedACs();
        // Attr context conditions
        AttrConditionTuple condt = context.getConditions();
        int num = condt.getNumberOfEntries();
        if (nested.hasNext()
                || nacs.hasNext()
                || pacs.hasNext()
                || (num > 0)
                || (this.itsUsedAtomics != null && !this.itsUsedAtomics.isEmpty())) {
            h.openSubTag("ApplCondition");
            // NACs
            while (nacs.hasNext()) {
                OrdinaryMorphism m = nacs.next();
                m.getTarget().setKind(GraphKind.NAC);
                h.openSubTag("NAC");
                if (!m.isEnabled()) {
                    h.addAttr("enabled", "false");
                }
                h.addObject("", m.getTarget(), true);
                m.writeMorphism(h);
                h.close();
            }
            // PACs
            while (pacs.hasNext()) {
                OrdinaryMorphism m = pacs.next();
                m.getTarget().setKind(GraphKind.PAC);
                h.openSubTag("PAC");
                if (!m.isEnabled()) {
                    h.addAttr("enabled", "false");
                }
                h.addObject("", m.getTarget(), true);
                m.writeMorphism(h);
                h.close();
            }
            // nested ACs
            while (nested.hasNext()) {
                OrdinaryMorphism m = nested.next();
                m.getTarget().setKind(GraphKind.AC);
                h.openSubTag("NestedAC");
                if (!m.isEnabled()) {
                    h.addAttr("enabled", "false");
                }
                h.addObject("", m.getTarget(), true);
                m.writeMorphism(h);
                ((NestedApplCond) m).writeNestedApplConds(h);
                h.close();
            }
            // Attr context conditions
            if (num > 0) {
                h.openSubTag("AttrCondition");
                h.addObject("", condt, true);
                h.close();
            }
            // Post Application Constraints
            if ((this.itsUsedAtomics != null && !this.itsUsedAtomics.isEmpty())
                    && (this.itsUsedFormulas != null && !this.itsUsedFormulas.isEmpty())) {
                h.openSubTag("PostApplicationCondition");
                // save formulas
                for (int i = 0; i < this.itsUsedFormulas.size(); i++) {
                    Formula f = this.itsUsedFormulas.get(i);
                    h.openSubTag("FormulaRef");
                    h.addObject("f", f, false);
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
     * Implements the interface of XMLObject
     *
     * @param h
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
            List<Formula> tmpFormulas = new ArrayList<>(); // of PostApplicationCondition
//			List<NestedApplCond> nacs = new ArrayList< >();
//			List<NestedApplCond> pacs = new ArrayList< >();
//			boolean needConvertToFormula = false;
            if (h.readSubTag("ApplCondition")) {
                while (h.readSubTag("NAC")) {
                    boolean nacEnabled = true;
                    Object nacattr_enabled = h.readAttr("enabled");
                    if ((nacattr_enabled != null)
                            && ((String) nacattr_enabled).equals("false")) {
                        nacEnabled = false;
                    }
                    OrdinaryMorphism nac = createNAC();
//					NestedApplCond nac = createNestedAC();
//					nacs.add(nac);
//					needConvertToFormula = true;
                    nac.getTarget().setHelpInfo(this.getName());
                    nac.getTarget().xyAttr = this.getLeft().xyAttr;
                    h.getObject("", nac.getTarget(), true);
                    nac.readMorphism(h);
                    h.close();
                    nac.setEnabled(nacEnabled);
                    nac.getTarget().setHelpInfo("");
                    if (nac.getName().isEmpty()) {
                        nac.setName("nac".concat(String.valueOf(this.itsNACs.size())));
                    }
                }
                while (h.readSubTag("PAC")) {
                    boolean pacEnabled = true;
                    Object pacattr_enabled = h.readAttr("enabled");
                    if ((pacattr_enabled != null)
                            && ((String) pacattr_enabled).equals("false")) {
                        pacEnabled = false;
                    }
                    OrdinaryMorphism pac = createPAC();
//					NestedApplCond pac = createNestedAC();					
//					pacs.add(pac);
//					needConvertToFormula = true;
                    pac.getTarget().setHelpInfo(this.getName());
                    pac.getTarget().xyAttr = this.getLeft().xyAttr;
                    h.getObject("", pac.getTarget(), true);
                    pac.readMorphism(h);
                    h.close();
                    pac.setEnabled(pacEnabled);
                    pac.getTarget().setHelpInfo("");
                    if (pac.getName().isEmpty()) {
                        pac.setName("pac".concat(String.valueOf(this.itsPACs.size())));
                    }
                }
                while (h.readSubTag("NestedAC")) {
//					needConvertToFormula = false;
                    boolean acEnabled = true;
                    Object acattr_enabled = h.readAttr("enabled");
                    if ((acattr_enabled != null)
                            && ((String) acattr_enabled).equals("false")) {
                        acEnabled = false;
                    }
                    NestedApplCond ac = createNestedAC();
                    ac.getTarget().setHelpInfo(this.getName());
                    ac.getTarget().xyAttr = this.getLeft().xyAttr;
                    h.getObject("", ac.getTarget(), true);
                    ac.readMorphism(h);
                    ac.readNestedApplConds(h);
                    h.close();
                    ac.setEnabled(acEnabled);
                    ac.getTarget().setHelpInfo("");
                    if (ac.getName().isEmpty()) {
                        ac.setName("gac".concat(String.valueOf(this.itsACs.size())));
                    }
                }
                if (h.readSubTag("AttrCondition")) {
                    AttrConditionTuple condt = getAttrContext().getConditions();
                    if (condt != null) {
                        h.enrichObject(condt);
                    }
                    h.close();
                }
                // read Post Application Constraints
                if (h.readSubTag("PostApplicationCondition")) {
                    // System.out.println("PostApplicationCondition");
                    // read formulas
                    while (h.readSubTag("FormulaRef")) {
                        Formula f = new Formula(true);
                        f.setName("");
                        Formula f1 = (Formula) h.getObject("f", null, false);
                        // System.out.println("Formula: "+f1);
                        if (f1 != null) {
                            tmpFormulas.add(f1);
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
            /*
			 * alte Variante : NOT MORE USED! / read Post Application
			 * Constraints generatePostConstraints = false;
			 * if(h.readSubTag("TaggedValue")) { String t = h.readAttr("Tag");
			 * int v = h.readIAttr("Value"); if(t.equals("post_constraints")) {
			 * if(v != 0) generatePostConstraints = true; } h.close(); }
             */
            h.close();
            this.applicable = true;
            setUsedFormulas(tmpFormulas);
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
        final List<Evaluable> vars = new ArrayList<>(this.itsACs.size());
        if (this.itsACs.isEmpty()) {
            this.formStr = "true";
            this.formReadStr = this.formStr;
            return true;
        }
        String tmp = "";
        int indx = -1;
        for (int i = 0; i < pacs.size(); i++) {
            NestedApplCond ac = pacs.get(i);
            if (ac.isEnabled()) {
                indx++;
                vars.add(ac);
                if (vars.size() == 1) {
                    tmp = tmp.concat(String.valueOf(indx + 1));
                } else {
                    tmp = tmp.concat("&").concat(String.valueOf(indx + 1));
                }
            }
        }
        for (int i = 0; i < nacs.size(); i++) {
            NestedApplCond ac = nacs.get(i);
            if (ac.isEnabled()) {
                indx++;
                vars.add(ac);
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
     * Returns an inverted rule.This rule has to be injective, otherwise -
     * returns null. The attribute mappings are NOT inverted, thus: the
     * resulting left and right-hand side graphs are not attributed anymore.
     *
     * @return
     */
    public Rule invertSimplex() {
        if (!this.isInjective()) {
            return (null);
        }
        Rule inverse = new Rule();
        Graph lgraph = this.getLeft();
        Graph rgraph = this.getRight();
        Graph linverse = inverse.getLeft();
        Graph rinverse = inverse.getRight();
        OrdinaryMorphism lmorph = new OrdinaryMorphism(lgraph, rinverse);
        OrdinaryMorphism rmorph = new OrdinaryMorphism(rgraph, linverse);
        Iterator<Node> rnodes = rgraph.getNodesSet().iterator();
        while (rnodes.hasNext()) {
            Node rNode = rnodes.next();
            Node linverseNode = null;
            try {
                linverseNode = linverse.createNode(rNode.getType());
            } catch (TypeException e) {
                // if the old rule was well typed, the new
                // rule should be also well typed
                e.printStackTrace();
            }
            rmorph.addMapping(rNode, linverseNode);
        }
        Iterator<Node> lnodes = lgraph.getNodesSet().iterator();
        while (lnodes.hasNext()) {
            Node lNode = lnodes.next();
            Node rinverseNode = null;
            try {
                rinverseNode = rinverse.createNode(lNode.getType());
            } catch (TypeException e) {
                // if the old rule was well typed, the new
                // rule should be also well typed
                e.printStackTrace();
            }
            lmorph.addMapping(lNode, rinverseNode);
            GraphObject rn = this.getImage(lNode);
            if (rn != null) {
                inverse.addMapping(rmorph.getImage(rn), rinverseNode);
            }
        }
        Iterator<Arc> rarcs = rgraph.getArcsSet().iterator();
        while (rarcs.hasNext()) {
            Arc rArc = rarcs.next();
            Node linverseSource = (Node) rmorph.getImage(rArc.getSource());
            Node linverseTarget = (Node) rmorph.getImage(rArc.getTarget());
            Arc linverseArc = null;
            try {
                linverseArc = linverse.createArc(rArc.getType(),
                        linverseSource, linverseTarget);
                rmorph.addMapping(rArc, linverseArc);
            } catch (TypeException ex) {
            }
        }
        Iterator<Arc> larcs = lgraph.getArcsSet().iterator();
        while (larcs.hasNext()) {
            Arc lArc = larcs.next();
            Node rinverseSource = (Node) lmorph.getImage(lArc.getSource());
            Node rinverseTarget = (Node) lmorph.getImage(lArc.getTarget());
            Arc rinverseArc = null;
            try {
                rinverseArc = rinverse.createArc(lArc.getType(),
                        rinverseSource, rinverseTarget);
                lmorph.addMapping(lArc, rinverseArc);
            } catch (TypeException ex) {
            }
            GraphObject ra = this.getImage(lArc);
            if (ra != null) {
                inverse.addMapping(rmorph.getImage(ra), rinverseArc);
            }
        }
        return (inverse);
    }

    /**
     * Tries to invert this rule.The rule has to be injective. The attribute
     * mappings are NOT inverted, thus the resulting left and right-hand side
     * graphs are not attributed anymore.
     *
     * Returns the pair with an inverted rule as the first element and a help
     * pair of two graph morphisms as the second element. The first morphism is
     * between the LHS of this and the RHS of the inverted rule, the second
     * morphism is between the RHS of this and the LHS of the inverted rule. If
     * this rule is not injective - returns null.
     *
     * @return
     */
    public Pair<Rule, Pair<OrdinaryMorphism, OrdinaryMorphism>> invertComplex() {
        if (!this.isInjective()) {
            return (null);
        }
        Rule inverse = new Rule();
        Graph lgraph = this.getLeft();
        Graph rgraph = this.getRight();
        Graph linverse = inverse.getLeft();
        Graph rinverse = inverse.getRight();
        OrdinaryMorphism lmorph = new OrdinaryMorphism(lgraph, rinverse);
        OrdinaryMorphism rmorph = new OrdinaryMorphism(rgraph, linverse);
        Iterator<Node> rnodes = rgraph.getNodesSet().iterator();
        while (rnodes.hasNext()) {
            Node rNode = rnodes.next();
            Node linverseNode = null;
            try {
                linverseNode = linverse.createNode(rNode.getType());
            } catch (TypeException e) {
                // if the old rule was well typed, the new
                // rule should be also well typed
                e.printStackTrace();
            }
            rmorph.addMapping(rNode, linverseNode);
        }
        Iterator<Node> lnodes = lgraph.getNodesSet().iterator();
        while (lnodes.hasNext()) {
            Node lNode = lnodes.next();
            Node rinverseNode = null;
            try {
                rinverseNode = rinverse.createNode(lNode.getType());
            } catch (TypeException e) {
                // if the old rule was well typed, the new
                // rule should be also well typed
                e.printStackTrace();
            }
            lmorph.addMapping(lNode, rinverseNode);
            GraphObject rn = this.getImage(lNode);
            if (rn != null) {
                inverse.addMapping(rmorph.getImage(rn), rinverseNode);
            }
        }
        Iterator<Arc> rarcs = rgraph.getArcsSet().iterator();
        while (rarcs.hasNext()) {
            Arc rArc = rarcs.next();
            Node linverseSource = (Node) rmorph.getImage(rArc.getSource());
            Node linverseTarget = (Node) rmorph.getImage(rArc.getTarget());
            Arc linverseArc = null;
            try {
                linverseArc = linverse.createArc(rArc.getType(),
                        linverseSource, linverseTarget);
                rmorph.addMapping(rArc, linverseArc);
            } catch (TypeException ex) {
            }
        }
        Iterator<Arc> larcs = lgraph.getArcsSet().iterator();
        while (larcs.hasNext()) {
            Arc lArc = larcs.next();
            Node rinverseSource = (Node) lmorph.getImage(lArc.getSource());
            Node rinverseTarget = (Node) lmorph.getImage(lArc.getTarget());
            Arc rinverseArc = null;
            try {
                rinverseArc = rinverse.createArc(lArc.getType(),
                        rinverseSource, rinverseTarget);
                lmorph.addMapping(lArc, rinverseArc);
            } catch (TypeException ex) {
            }
            GraphObject ra = this.getImage(lArc);
            if (ra != null) {
                inverse.addMapping(rmorph.getImage(ra), rinverseArc);
            }
        }
        Pair<OrdinaryMorphism, OrdinaryMorphism> information = new Pair<OrdinaryMorphism, OrdinaryMorphism>(
                lmorph, rmorph);
        return (new Pair<Rule, Pair<OrdinaryMorphism, OrdinaryMorphism>>(
                inverse, information));
    }

    /**
     * A plain rule returns null.Its subclasses <code>KernelRule</code>,
     * <code>MultiRule</code>, <code>RuleScheme</code>,
     * <code>AmalgamatedRule</code> override this method to return its
     * <code>RuleScheme</code>.
     *
     * @return
     */
    public RuleScheme getRuleScheme() {
        return null;
    }

    /**
     * Returns my current match
     *
     * @return
     */
    public Match getMatch() {
        return this.itsMatch;
    }

    /**
     * Compares attribute value of the specified objects due to constant value
     * of the first object. Failed attribute value of the second object will be
     * unset. Checks all members of the attribute tuple.
     *
     * @param src first object (an object of the LHS of a rule)
     * @param tgt second object (an object of a NAC, PAC of a rule)
     * @return	true if attribute value is equal, otherwise false
     */
    public boolean compareConstantAttributeValue(
            final GraphObject src,
            final GraphObject tgt) {
        boolean result = true;
        if (src.getAttribute() != null
                && tgt.getAttribute() != null) {
            final ValueTuple tgtValue = (ValueTuple) tgt.getAttribute();
            final ValueTuple srcValue = (ValueTuple) src.getAttribute();
            for (int i = 0; i < srcValue.getNumberOfEntries(); i++) {
                final ValueMember lhsvm = srcValue.getValueMemberAt(i);
                final ValueMember tgtvm = tgtValue.getValueMemberAt(lhsvm.getName());
                if (lhsvm.isSet()
                        && lhsvm.getExpr().isConstant()
                        && tgtvm != null && tgtvm.isSet()
                        && !lhsvm.getExprAsText().equals(tgtvm.getExprAsText())) {
                    result = false;
                    tgtvm.setExpr(null);
                }
            }
        }
        return result;
    }

    /**
     * Compares attribute value of the specified objects due to constant value
     * of the first object. Failed attribute value of the second object will be
     * unset. The check broken after at least one attribute failed.
     *
     * @param src first object (an object of the LHS of a rule)
     * @param tgt second object (an object of a NAC, PAC of a rule)
     * @return	true if attribute value is equal, otherwise false
     */
    public boolean compareConstAttrValueOfMapObjs(
            final GraphObject src, final GraphObject tgt) {
        if (src.getAttribute() != null
                && tgt.getAttribute() != null) {
            final ValueTuple tgtValue = (ValueTuple) tgt.getAttribute();
            final ValueTuple srcValue = (ValueTuple) src.getAttribute();
            for (int i = 0; i < srcValue.getNumberOfEntries(); i++) {
                final ValueMember srcvm = srcValue.getValueMemberAt(i);
                final ValueMember tgtvm = tgtValue.getValueMemberAt(srcvm.getName());
                if (srcvm.isSet()
                        && srcvm.getExpr().isConstant()
                        && tgtvm.isSet()
                        && !srcvm.getExprAsText().equals(tgtvm.getExprAsText())) {
                    tgtvm.setExpr(null);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compares its LHS, RHS, morphism, NACs, PACs and attribute context to the
     * appropriate elements of the specified rule.Returns true if all elements
     * are equal.
     *
     * @param r
     * @return
     */
    public boolean compareTo(Rule r) {
        // System.out.println("Rule.compareTo");
        Pair<Boolean, String> errMsgHolder = null;
        // compare rule morphism
        if (!((OrdinaryMorphism) this).compareTo(r)) {
            // System.out.println("Rule: "+getName()+" :: Mapping failed!");
            errMsgHolder = new Pair<>(true,
                    "Rule content is different.");
            return false;
        }
        // compare NACs
        errMsgHolder = compareApplConds(this.getNACsList(), r.getNACsList(), "NAC");
        if (errMsgHolder != null) {
            return false;
        }
        // compare PACs
        errMsgHolder = compareApplConds(this.getPACsList(), r.getPACsList(), "PAC");
        if (errMsgHolder != null) {
            return false;
        }
        // compare nested ACs
        errMsgHolder = compareApplConds(this.getNestedACsList(), r.getNestedACsList(), "nested AC");
        if (errMsgHolder != null) {
            return false;
        }
        // compare rule context
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        VarTuple varOther = (VarTuple) r.getAttrContext().getVariables();
        if (!var.compareTo(varOther)) {
            errMsgHolder = new Pair<>(true,
                    "Variable rule context is different.");
            return false;
        }
        CondTuple cond = (CondTuple) getAttrContext().getConditions();
        CondTuple condOther = (CondTuple) r.getAttrContext().getConditions();
        if (!cond.compareTo(condOther)) {
            errMsgHolder = new Pair<>(true,
                    "Conditional rule context is different.");
            return false;
        }
        return true;
    }

    private Pair<Boolean, String> compareApplConds(
            final List<OrdinaryMorphism> applConds,
            final List<OrdinaryMorphism> otherApplConds,
            String what) {
        // compare ACs
        List<OrdinaryMorphism> another = new ArrayList<>();
        another.addAll(otherApplConds);
        if (applConds.size() != another.size()) {
            // System.out.println("Rule: "+getName()+" NACs discrepancy!");
            Pair<Boolean, String> errMsgHolder = new Pair<>(
                    true,
                    "Number of " + what + "s is different.");
            return errMsgHolder;
        }
        OrdinaryMorphism ac = null;
        for (int i = 0; i < applConds.size(); i++) {
            ac = applConds.get(i);
            for (int j = another.size() - 1; j >= 0; j--) {
                OrdinaryMorphism ac1 = another.get(j);
                if (ac.compareTo(ac1)) {
                    another.remove(ac1);
                    break;
                }
            }
        }
        if (!another.isEmpty() && ac != null) {
            Pair<Boolean, String> errMsgHolder = new Pair<>(
                    true,
                    what + ":  " + ac.getName() + "  is different.");
            return errMsgHolder;
        }
        return null;
    }

    public List<GraphObject> getInputParameterObjectsLeft(final List<String> inputParams) {
        return getInputParameterObjects(this.getLeft(), inputParams);
    }

    public List<GraphObject> getInputParameterObjectsRight(final List<String> inputParams) {
        return getInputParameterObjects(this.getRight(), inputParams);
    }

    private List<GraphObject> getInputParameterObjects(final Graph g, final List<String> inputParams) {
        List<GraphObject> goIP = new ArrayList<>();
        Iterator<GraphObject> elems = g.iteratorOfElems();
        while (elems.hasNext()) {
            GraphObject go = elems.next();
            if (go.getAttribute() != null) {
                ValueTuple val = (ValueTuple) go.getAttribute();
                for (int i = 0; i < val.getNumberOfEntries(); i++) {
                    ValueMember mem = val.getEntryAt(i);
                    if (mem.isSet() && mem.getExpr().isVariable()) {
                        if (inputParams.contains(mem.getExprAsText())) {
                            goIP.add(go);
                        }
                    }
                }
            }
        }
        return goIP;
    }

    public List<GraphObject> getLeftInputParameterObjects() {
        List<GraphObject> list = new ArrayList<>();
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        Iterator<GraphObject> elems = this.itsOrig.iteratorOfElems();
        while (elems.hasNext()) {
            GraphObject go = elems.next();
            if (go.getAttribute() != null) {
                ValueTuple val = (ValueTuple) go.getAttribute();
                for (int i = 0; i < val.getNumberOfEntries(); i++) {
                    ValueMember mem = val.getValueMemberAt(i);
                    if (mem.isSet() && mem.getExpr().isVariable()) {
                        if (var.getVarMemberAt(mem.getExprAsText()) != null
                                && var.getVarMemberAt(mem.getExprAsText()).isInputParameter()) {
                            list.add(go);
                        }
                    }
                }
            }
        }
//		System.out.println(list);
        return list;
    }

    public List<GraphObject> getRightInputParameterObjects() {
        List<GraphObject> list = new ArrayList<>();
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        Iterator<GraphObject> elems = this.itsImag.iteratorOfElems();
        while (elems.hasNext()) {
            GraphObject go = elems.next();
            if (go.getAttribute() != null) {
                ValueTuple val = (ValueTuple) go.getAttribute();
                for (int i = 0; i < val.getNumberOfEntries(); i++) {
                    ValueMember mem = val.getValueMemberAt(i);
                    if (mem.isSet() && mem.getExpr().isVariable()) {
                        if (var.getVarMemberAt(mem.getExprAsText()) != null
                                && var.getVarMemberAt(mem.getExprAsText()).isInputParameter()) {
                            list.add(go);
                        }
                    }
                }
            }
        }
        return list;
    }

    public List<String> getInputParameterNames() {
        List<String> inputParams = new ArrayList<>(1);
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        for (int i = 0; i < var.getNumberOfEntries(); i++) {
            VarMember varm = var.getVarMemberAt(i);
            if (varm.isInputParameter()) {
                inputParams.add(varm.getName());
            }
        }
        return inputParams;
    }

    /**
     * Returns variables of the attribute context of this rule which are used as
     * input parameter for the rule application.
     *
     * @return
     */
    public List<VarMember> getInputParameters() {
        List<VarMember> inputParams = new ArrayList<>(1);
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        for (int i = 0; i < var.getNumberOfEntries(); i++) {
            VarMember varm = var.getVarMemberAt(i);
            if (varm.isInputParameter()) {
                inputParams.add(varm);
            }
        }
        return inputParams;
    }

    public List<VarMember> getInputParametersLeft() {
        List<VarMember> inputParams = new ArrayList<>(1);
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        for (int i = 0; i < var.getNumberOfEntries(); i++) {
            VarMember vm = var.getVarMemberAt(i);
            if (vm.isInputParameter()
                    && vm.getMark() == VarMember.LHS) {
                inputParams.add(vm);
            }
        }
        return inputParams;
    }

    public List<VarMember> getInputParametersRight() {
        List<VarMember> inputParams = new ArrayList<>(1);
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        for (int i = 0; i < var.getNumberOfEntries(); i++) {
            VarMember vm = var.getVarMemberAt(i);
            if (vm.isInputParameter()
                    && (vm.getMark() == VarMember.RHS
                    || vm.getMark() == VarMember.NAC)) {
                inputParams.add(vm);
            }
        }
        return inputParams;
    }

    /**
     * Returns variables of the attribute context of this rule which are used by
     * attributes of the specified graph object as an input parameter for the
     * rule application.
     *
     * @param go
     * @param var
     * @return
     */
    public List<VarMember> getInputParametersOfGraphObject(final GraphObject go, final VarTuple var) {
        if (go.getAttribute() == null) {
            return new ArrayList<>();
        }
        List<VarMember> inputParams = new ArrayList<>(1);
        ValueTuple attrVal = (ValueTuple) go.getAttribute();
        for (int i = 0; i < attrVal.getNumberOfEntries(); i++) {
            ValueMember vm = attrVal.getValueMemberAt(i);
            if (vm.isSet() && vm.getExpr().isVariable()) {
                VarMember varm = var.getVarMemberAt(vm.getExprAsText());
                if (varm != null && varm.isInputParameter()) {
                    inputParams.add(varm);
                }
            }
        }
        return inputParams;
    }

    public List<VarMember> getNonInputParametersOfNewGraphObjects() {
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        List<VarMember> params = new ArrayList<>(1);
        final Iterator<GraphObject> objs = this.itsImag.iteratorOfElems();
        while (objs.hasNext()) {
            GraphObject go = objs.next();
            if (go.getAttribute() == null
                    || this.itsCodomObjects.contains(go)) {
                continue;
            }
            ValueTuple attrVal = (ValueTuple) go.getAttribute();
            for (int i = 0; i < attrVal.getNumberOfEntries(); i++) {
                ValueMember vm = attrVal.getValueMemberAt(i);
                if (vm.isSet() && vm.getExpr().isVariable()) {
                    VarMember varm = var.getVarMemberAt(vm.getExprAsText());
                    if (varm != null && !varm.isInputParameter()) {
                        params.add(varm);
                    }
                }
            }
        }
        return params;
    }

    public List<VarMember> getNonInputParameters() {
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        List<VarMember> params = new ArrayList<>(1);
        for (int i = 0; i < var.getNumberOfEntries(); i++) {
            VarMember v = var.getVarMemberAt(i);
            if (!v.isInputParameter()) {
                params.add(v);
            }
        }
        return params;
    }

    /**
     * always returns TRUE. It is not yet implemented!
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
     * always returns TRUE. It is not yet implemented!
     */
    public boolean isNACValid(OrdinaryMorphism nac) {
        return true;
    }

    /**
     * Shift of an application condition is not possible when it may cause a
     * dangling edge.
     *
     * @param ac
     * @return
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
     * Checks dangling edges of the given pac. Returns true if no dangling edge
     * exists, otherwise false.
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
     * Checks dangling edges of the its pacs. Returns true if no dangling edge
     * exists, otherwise false.
     */
    public boolean arePACsValid() {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism ac = this.itsPACs.get(i);
            if (!this.isPACValid(ac)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks dangling edges of the given general application condition ac.
     * Returns true if no dangling edge exists, otherwise false.
     */
    public boolean isGACValid(NestedApplCond ac) {
        if (ac.isEnabled()) {
            return ac.isValid();
        }
        return true;
    }

    /**
     * Checks dangling edges of the its general application conditions. Returns
     * true if no dangling edge exists, otherwise false.
     */
    public boolean areGACsValid() {
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            if (!this.isGACValid(ac)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks dangling edges of the its pacs and general acs. Returns true if no
     * dangling edge exists, otherwise false.
     */
    public boolean areApplCondsValid() {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism ac = this.itsPACs.get(i);
            if (!this.isPACValid(ac)) {
                return false;
            }
        }
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            if (!ac.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies nodes and edges of its PACs in the LHS resp. RHS and extends the
     * rule mapping. The PACs will be disabled.
     */
    public boolean extendByPacs() {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism pac = this.itsPACs.get(i);
            if (pac.isEnabled()) {
                extendByPac(pac);
                pac.setEnabled(false);
            }
        }
        return true;
    }

    private boolean extendByPac(OrdinaryMorphism pac) {
        Map<Node, Node> n2n = new HashMap<>();
        Iterator<Node> nodes = pac.getTarget().getNodesCollection().iterator();
        while (nodes.hasNext()) {
            Node pn = nodes.next();
            Iterator<GraphObject> en = pac.getInverseImage(pn);
            if (!en.hasNext()) {
                try {
                    Node nln = this.itsOrig.copyNode(pn);
                    n2n.put(pn, nln);
                    Node nrn = this.itsImag.copyNode(pn);
                    n2n.put(nln, nrn);
                    try {
                        this.addMapping(nln, nrn);
                        nln.setContextUsage(pac.hashCode());
                        nrn.setContextUsage(pac.hashCode());
                    } catch (BadMappingException ex1) {
                        return false;
                    }
                } catch (TypeException ex) {
                    return false;
                }
            } else {
                while (en.hasNext()) {
                    Node ln = (Node) en.next();
                    n2n.put(pn, ln);
                    Node rn = (Node) this.getImage(ln);
                    if (rn != null) {
                        n2n.put(ln, rn);
                    }
                    break;
                }
            }
        }
        Iterator<Arc> arcs = pac.getTarget().getArcsCollection().iterator();
        while (arcs.hasNext()) {
            Arc pa = arcs.next();
            Iterator<GraphObject> en = pac.getInverseImage(pa);
            if (!en.hasNext()) {
                try {
                    Node srcl = n2n.get(pa.getSource());
                    Node tarl = n2n.get(pa.getTarget());
                    if (srcl != null && tarl != null) {
                        Arc nla = this.getOriginal().copyArc(pa, srcl, tarl);
                        Node srcr = n2n.get(srcl);
                        Node tarr = n2n.get(tarl);
                        if (srcr != null && tarr != null) {
                            Arc nra = this.getImage().copyArc(pa, srcr, tarr);
                            if (nla != null && nra != null) {
                                try {
                                    this.addMapping(nla, nra);
                                    nla.setContextUsage(pac.hashCode());
                                    nra.setContextUsage(pac.hashCode());
                                } catch (BadMappingException ex1) {
                                    return false;
                                }
                            }
                        }
                    }
                } catch (TypeException ex) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Undo the copy of its PACs done by <code>extendByPacs</code>. The PACs
     * will be enabled.
     */
    public boolean extendByPacsUndo() {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism pac = this.itsPACs.get(i);
            if (extendByPacUndo(pac)) {
                pac.setEnabled(true);
            }
        }
        return true;
    }

    private boolean extendByPacUndo(OrdinaryMorphism pac) {
        boolean res = false;
        Iterator<Arc> arcsL = this.itsOrig.getArcsCollection().iterator();
        while (arcsL.hasNext()) {
            Arc aL = arcsL.next();
            if (aL.getContextUsage() == pac.hashCode()) {
                Arc aR = (Arc) this.getImage(aL);
                this.removeMapping(aL, aR);
                try {
                    this.itsImag.destroyArc(aR);
                    this.itsOrig.destroyArc(aL);
                    res = true;
                } catch (TypeException ex) {
                    String exstr = ex.getLocalizedMessage();
                }
            }
        }
        Iterator<Node> nodes = this.itsOrig.getNodesCollection().iterator();
        while (nodes.hasNext()) {
            Node nL = nodes.next();
            if (nL.getContextUsage() == pac.hashCode()) {
                Node nR = (Node) this.getImage(nL);
                this.removeMapping(nL, nR);
                try {
                    this.itsImag.destroyNode(nR);
                    this.itsOrig.destroyNode(nL);
                    res = true;
                } catch (TypeException ex) {
                    String exstr = ex.getLocalizedMessage();
                }
            }
        }
        return res;
    }

    /**
     * Checks existing variables of the attribute context against the attribute
     * context of its current match and adjusts the attribute context of its
     * match, if needed.
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
        VarTuple vt = (VarTuple) getAttrContext().getVariables();
        for (int i = 0; i < vt.getNumberOfEntries(); i++) {
            VarMember vm = vt.getVarMemberAt(i);
            if (inputParameterOnly) {
                if (vm.isInputParameter()) {
                    vm.setExpr(null);
                }
            } else {
                vm.setExpr(null);
            }
        }
    }

    /**
     * Attribute context variable which is an input parameter is no more input
     * parameter after this method applied.
     */
    public void unsetInputParameter() {
        AttrVariableTuple avt = getAttrContext().getVariables();
        for (int i = 0; i < avt.getNumberOfEntries(); i++) {
            VarMember vm = (VarMember) avt.getMemberAt(i);
            if (vm.isInputParameter()) {
                vm.setInputParameter(false);
            }
        }
    }

    /**
     * Returns the name of an input parameter whithout value, otherwise - null.
     */
    public String getInputParameterWithoutValue() {
        AttrVariableTuple avt = getAttrContext().getVariables();
        for (int i = 0; i < avt.getNumberOfEntries(); i++) {
            VarMember vm = (VarMember) avt.getMemberAt(i);
            if (vm.isInputParameter() && !vm.isSet()) {
                return vm.getName();
            }
        }
        return null;
    }

    /**
     * Returns name(s) of the variables of the attribute context which are used
     * as input parameter(s) of this rule and they are not set. If the specified
     * parameter is TRUE then the LHS (NACs, PACs) with an input parameter for
     * matching are taken in account. If the specified parameter is FALSE then
     * the RHS (NACs, PACs) with an input parameter for matching are taken in
     * acount.
     *
     * Returns null if all input parameter are set.
     */
    public String getInputParameterWithoutValue(boolean left) {
        AttrVariableTuple avt = getAttrContext().getVariables();
        for (int i = 0; i < avt.getNumberOfEntries(); i++) {
            VarMember vm = (VarMember) avt.getMemberAt(i);
            if (vm.isInputParameter() && !vm.isSet()) {
                if (left) {
                    List<String> vars = getLeft().getVariableNamesOfAttributes();
                    for (int j = 0; j < vars.size(); j++) {
                        if (vars.get(j).equals(vm.getName())) {
                            return vm.getName();
                        }
                    }
                } else {
                    List<String> vars = getRight().getVariableNamesOfAttributes();
                    for (int j = 0; j < vars.size(); j++) {
                        if (vars.get(j).equals(vm.getName())) {
                            return vm.getName();
                        }
                    }
                }
                for (int j = 0; j < this.itsNACs.size(); j++) {
                    OrdinaryMorphism nac = this.itsNACs.get(j);
                    List<String> vars = nac.getTarget()
                            .getVariableNamesOfAttributes();
                    for (int k = 0; k < vars.size(); k++) {
                        if (vars.get(k).equals(vm.getName())) {
                            return vm.getName();
                        }
                    }
                }
                for (int j = 0; j < this.itsPACs.size(); j++) {
                    OrdinaryMorphism pac = this.itsPACs.get(j);
                    List<String> vars = pac.getTarget()
                            .getVariableNamesOfAttributes();
                    for (int k = 0; k < vars.size(); k++) {
                        if (vars.get(k).equals(vm.getName())) {
                            return vm.getName();
                        }
                    }
                }
                for (int j = 0; j < this.itsACs.size(); j++) {
                    OrdinaryMorphism ac = this.itsACs.get(j);
                    List<String> vars = ac.getTarget()
                            .getVariableNamesOfAttributes();
                    for (int k = 0; k < vars.size(); k++) {
                        if (vars.get(k).equals(vm.getName())) {
                            return vm.getName();
                        }
                    }
                }
            }
        }
        return null;
    }

    private void deleteUnusedVars(List<VarMember> used) {
        VarTuple vars = (VarTuple) this.getAttrContext().getVariables();
        for (int i = 0; i < vars.getNumberOfEntries(); i++) {
            VarMember vm = vars.getVarMemberAt(i);
            if (!used.contains(vm)) {
                vars.getTupleType().deleteMemberAt(vm.getName());
//				vars.showVariables();
            }
        }
    }

    /**
     * Checks attribute setting of RHS, variable declarations and attribute
     * conditions. If all checks successful, it prepares infos about this rule.
     * The method getErrorMessage() gives more information about fails.
     */
    public boolean isReadyToTransform() {
        this.isReady = true;
        if (!this.enabled) {
            return true;
        }
        // check usage of abstract types of the RHS
        final List<String> abstractTypesOfRHS = new ArrayList<>(1);
        Iterator<Node> enumer = this.itsImag.getNodesSet().iterator();
        while (enumer.hasNext()) {
            GraphObject o = enumer.next();
            Iterator<GraphObject> inverse = getInverseImage(o);
            if (!inverse.hasNext() && o.getType().isAbstract()) {
                abstractTypesOfRHS.add(o.getType().getName());
            }
        }
        this.isReady = abstractTypesOfRHS.isEmpty();
        if (!this.isReady) {
            this.errorMsg = this.errorMsg.concat("RHS: creating abstract nodes not allowed!  ").concat(abstractTypesOfRHS.toString());
            return false;
        }
        // check  PAC is valid: check dangling edge of nodes to delete which are used in a PAC
        for (int l = 0; l < this.itsPACs.size(); l++) {
            this.isReady = this.isPACValid(this.itsPACs.get(l));
            if (!this.isReady) {
                return false;
            }
        }
        //   check attributes
        if (!isAttributed()) {
            return true;
        }
        this.applyDefaultAttrValuesOfTypeGraph(this.itsImag);
        AttrVariableTuple avt = this.itsAttrContext.getVariables();
        AttrConditionTuple act = this.itsAttrContext.getConditions();
        this.errorMsg = "";
        // get used variable and its declaration: (type, name)
        List<Pair<String, String>> varDecls = getVariableDeclarations();
        // add vars of NACs to varDecls
        for (int l = 0; l < this.itsNACs.size(); l++) {
            addVarDecl(this.itsNACs.get(l).getImage(), varDecls);
        }
        // add vars of PACs to varDecls
        for (int l = 0; l < this.itsPACs.size(); l++) {
            addVarDecl(this.itsPACs.get(l).getImage(), varDecls);
        }
        // add vars of nested ACs to varDecls
        for (int l = 0; l < this.itsACs.size(); l++) {
            addVarDecl(this.itsACs.get(l).getImage(), varDecls);
        }
        // check: same variable name , different type :: should not happen!
        this.isReady = checkDoubleVarDecl(varDecls);
        if (!this.isReady) {
            return false;
        }
        // check used variables
        this.isReady = checkUsedVariables(avt, varDecls);
        if (!this.isReady) {
            return false;
        }
        // mark used variables: RHS, NAC, PAC, LHS
        markUsedVariables(avt);
        // check and mark the attr. conditions
        this.isReady = markAttrConditions(avt, act);
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
        this.isReady = this.checkAttributesOfNewObjects(avt);
        if (!this.isReady) {
            return false;
        }
        return this.isReady;
    }

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

    public boolean pacIsUsingVariable(
            final VarMember var,
            final AttrConditionTuple act) {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            final OrdinaryMorphism pac = this.itsPACs.get(i);
            if (pac.getTarget().isUsingVariable(var)) {
                return true;
            }
            List<String> pacVars = pac.getTarget()
                    .getVariableNamesOfAttributes();
            for (int j = 0; j < pacVars.size(); j++) {
                String varName = pacVars.get(j);
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

    protected void applyDefaultAttrValuesOfTypeGraph(
            final Graph g,
            final Iterator<?> iter) {
        boolean right = g == this.getRight();
        while (iter.hasNext()) {
            GraphObject o = (GraphObject) iter.next();
            if (o.getAttribute() == null) {
                if ((o.getType().getAttrType() != null)
                        && (o.getType().getAttrType().getNumberOfEntries() != 0)) {
                    o.createAttributeInstance();
                } else {
                    continue;
                }
            }
            if (right && !this.getInverseImage(o).hasNext()) {
                if (o.isNode()) {
                    g.applyDefaultAttrValuesOfTypeGraph((Node) o, null);
                } else {
                    g.applyDefaultAttrValuesOfTypeGraph((Arc) o, null);
                }
            }
        }
    }

    /*
	 * Use the attribute values of the nodes and edges of the Type Graph as default values
	 * for the attributes of the specified graph.
     */
    public void applyDefaultAttrValuesOfTypeGraph(final Graph g) {
        this.applyDefaultAttrValuesOfTypeGraph(g, g.getNodesSet().iterator());
        this.applyDefaultAttrValuesOfTypeGraph(g, g.getArcsSet().iterator());
    }

    protected boolean isAttributed() {
        boolean attributed = this.itsOrig.isAttributed()
                || this.itsImag.isAttributed();
        for (int l = 0; !attributed && l < this.itsNACs.size(); l++) {
            attributed = this.itsNACs.get(l).getImage().isAttributed();
        }
        for (int l = 0; !attributed && l < this.itsPACs.size(); l++) {
            attributed = this.itsPACs.get(l).getImage().isAttributed();
        }
        for (int l = 0; !attributed && l < this.itsACs.size(); l++) {
            attributed = this.itsACs.get(l).getImage().isAttributed();
        }
        return attributed;
    }

    private void addVarDecl(final Graph g, final List<Pair<String, String>> varDecls) {
        addVarDecl(g.getNodesSet().iterator(), varDecls);
        addVarDecl(g.getArcsSet().iterator(), varDecls);
    }

    private void addVarDecl(final Iterator<?> elems, final List<Pair<String, String>> varDecls) {
        while (elems.hasNext()) {
            GraphObject o = (GraphObject) elems.next();
            if (o.getAttribute() != null) {
                AttrInstance attr = o.getAttribute();
                ValueTuple vt = (ValueTuple) attr;
                for (int k = 0; k < vt.getSize(); k++) {
                    ValueMember vm = vt.getValueMemberAt(k);
                    if (vm.isSet() && vm.getExpr().isVariable()) {
                        String n = vm.getExprAsText();
                        String t = vm.getDeclaration().getTypeName();
//						System.out.println(o.getContext().getName()+"   "+n+"    "+t);
                        Pair<String, String> p = new Pair<String, String>(t, n);
                        boolean found = false;
                        for (int j = 0; j < varDecls.size(); j++) {
                            Pair<String, String> pj = varDecls.get(j);
                            if (t.equals(pj.first) && n.equals(pj.second)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            varDecls.add(p);
                        }
                    }
                }
            }
        }
    }

    private boolean checkDoubleVarDecl(final List<Pair<String, String>> varDecls) {
        boolean result = true;
        // check: same variable name , different type :: should not happen!
        for (int j = 0; result && j < varDecls.size(); j++) {
            Pair<String, String> pj = varDecls.get(j);
            for (int jj = j + 1; result && jj < varDecls.size(); jj++) {
                Pair<String, String> pjj = varDecls.get(jj);
                if (pj.second.equals(pjj.second) && !pj.first.equals(pjj.first)) {
                    if (!("Object".equals(pj.first)
                            || "java.lang.Object".equals(pj.first))
                            && !("Object".equals(pjj.first)
                            || "java.lang.Object".equals(pjj.first))) {
                        this.errorMsg = "Variable has multiple declaration : ".concat(pj.second);
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
            final AttrVariableTuple avt,
            final List<Pair<String, String>> varDecls) {
        List<VarMember> used = new ArrayList<>(5);
        boolean result = true;
        for (int i = 0; i < varDecls.size(); i++) {
            final Pair<String, String> p = varDecls.get(i);
            String typeName1 = p.first;
            boolean isClass1 = false;
            final String className1 = isClassName(typeName1);
            if (className1 != null) {
                isClass1 = true;
            }
            boolean isClass2 = false;
            String className2 = null;
            String typeName2 = "";
            final String varName = p.second;
            VarMember varm = ((VarTuple) avt).getVarMemberAt(varName);
            if (varm == null) {
                className2 = isClassName(varName);
                if (className2 != null) {
                    typeName2 = className2;
                }
            } else if (varm.getDeclaration() == null) {
                this.errorMsg = "Variable: ".concat(varName).concat("  isn't declared!");
                return false;
            } else {
                typeName2 = varm.getDeclaration().getTypeName();
                className2 = isClassName(typeName2);
            }
            if (className2 != null) {
                isClass2 = true;
            }
            if (className1 != null && className2 != null) {
                if (!className1.equals(className2)) {
                    if (!className1.equals("java.lang.Object")
                            && !className2.equals("java.lang.Object")) {
                        this.errorMsg = "Variable: " + varName
                                + "  has wrong type." + "\nIt should be :  "
                                + className1 + " .";
                        return false;
                    }
                } else if (!typeName1.equals(typeName2)) {
                    final String packageName = className1.substring(0, className1.lastIndexOf("."));
                    if (packageName.equals("java.lang") && varm != null) {
                        varm.getDeclaration().setType(typeName1);
                    } else {
                        this.errorMsg = "Variable: " + varName
                                + "  has wrong type." + "\nIt should be :  "
                                + typeName1 + " .";
                        return false;
                    }
                }
            } else if (!isClass1 && !isClass2 && !typeName1.equals(typeName2)) {
                this.errorMsg = "Variable: " + varName + "  has wrong type."
                        + "\nIt should be :  " + typeName1 + " .";
                return false;
            }
            used.add(varm);
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
        for (int l = 0; l < this.itsNACs.size(); l++) {
            Graph g = this.itsNACs.get(l).getImage();
            markUsedVars(g.getNodesSet().iterator(),
                    g.getArcsSet().iterator(),
                    avt, VarMember.NAC);
        }
        // inside PACs	
        for (int l = 0; l < this.itsPACs.size(); l++) {
            Graph g = this.itsPACs.get(l).getImage();
            markUsedVars(g.getNodesSet().iterator(),
                    g.getArcsSet().iterator(),
                    avt, VarMember.PAC);
        }
        // inside nested AC	
        markUsedVarsOfNestedACs(this.itsACs, avt);
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

    private void markUsedVarsOfNestedACs(List<?> nestedACs, AttrVariableTuple avt) {
        for (int i = 0; i < nestedACs.size(); i++) {
            OrdinaryMorphism nestAC = (OrdinaryMorphism) nestedACs.get(i);
            Graph g = nestAC.getImage();
            markUsedVars(g.getNodesSet().iterator(),
                    g.getArcsSet().iterator(),
                    avt, VarMember.PAC);
            markUsedVarsOfNestedACs(((NestedApplCond) nestAC).getNestedACs(), avt);
        }
    }

    private void markUsedVars(
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
     * Prepares info about this rule: node, edges to preserve, change, delete,
     * create; types which should be checked due to node resp. edge type
     * multiplicity. These infos can be called by methods:
     * getElementsToPreserve(), getElementsToChange(), getElementsToDelete(),
     * getElementsToCreate(), getTypesWhichNeedMultiplicityCheck.
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
     * Return true if its left and right graphs are empty and there aren't any
     * application conditions, otherwise - false.
     */
    public boolean isEmptyRule() {
        return (this.itsOrig.isEmpty()
                && this.itsImag.isEmpty()
                && this.itsNACs.isEmpty()
                && this.itsPACs.isEmpty()
                && this.itsACs.isEmpty());
    }

    public List<Type> getTypesOfLeftGraph() {
        final List<Type> list = new ArrayList<>();
        for (final Iterator<Node> en = getLeft().getNodesSet().iterator(); en.hasNext();) {
            final Node o = en.next();
            if (!list.contains(o.getType())) {
                list.add(o.getType());
            }
        }
        for (final Iterator<Arc> en = getLeft().getArcsSet().iterator(); en.hasNext();) {
            final Arc o = en.next();
            if (!list.contains(o.getType())) {
                list.add(o.getType());
            }
        }
        return list;
    }

    public List<Type> getTypeOfObjectToDelete() {
        final List<Type> list = new ArrayList<>();
        for (final Iterator<Node> en = getLeft().getNodesSet().iterator(); en.hasNext();) {
            final Node o = en.next();
            if (getImage(o) == null
                    && !list.contains(o.getType())) {
                list.add(o.getType());
            }
        }
        for (final Iterator<Arc> en = getLeft().getArcsSet().iterator(); en.hasNext();) {
            final Arc o = en.next();
            if (getImage(o) == null
                    && !list.contains(o.getType())) {
                list.add(o.getType());
            }
        }
        return list;
    }

    public List<Type> getTypeOfObjectToCreate() {
        final List<Type> list = new ArrayList<>();
        for (Iterator<Node> en = getRight().getNodesSet().iterator(); en.hasNext();) {
            GraphObject o = en.next();
            if (!getInverseImage(o).hasNext()
                    && !list.contains(o.getType())) {
                list.add(o.getType());
            }
        }
        for (Iterator<Arc> en = getRight().getArcsSet().iterator(); en.hasNext();) {
            GraphObject o = en.next();
            if (!getInverseImage(o).hasNext()
                    && !list.contains(o.getType())) {
                list.add(o.getType());
            }
        }
        return list;
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
        final List<String> list = new ArrayList<>();
        final List<GraphObject> list1 = new ArrayList<>();
        list1.addAll(this.getElementsToCreate());
        list1.addAll(this.getElementsToDelete());
        for (int i = 0; i < list1.size(); i++) {
            final GraphObject go = list1.get(i);
            final String typekey = go.convertToKey();
            if (!list.contains(typekey)) {
                if (go.isNode()) {
                    int min = go.getType().getSourceMin();
                    int max = go.getType().getSourceMax();
                    if (min > 0 || max > 0) {
                        list.add(typekey);
                        final List<Type> children = go.getType().getChildren();
                        for (int ch = 0; ch < children.size(); ch++) {
                            list.add(children.get(ch).convertToKey());
                        }
                    }
                } else {
                    int srcMin = go.getType().getSourceMin(((Arc) go).getSource().getType(),
                            ((Arc) go).getTarget().getType());
                    int srcMax = go.getType().getSourceMax(((Arc) go).getSource().getType(),
                            ((Arc) go).getTarget().getType());
                    int tarMin = go.getType().getTargetMin(((Arc) go).getSource().getType(),
                            ((Arc) go).getTarget().getType());
                    int tarMax = go.getType().getTargetMax(((Arc) go).getSource().getType(),
                            ((Arc) go).getTarget().getType());
                    if (srcMin > 0 || tarMin > 0 || srcMax > 0 || tarMax > 0) {
                        list.add(typekey);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Returns true if this rule will create new graph elements, otherwise -
     * false.
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
     * Returns true if this rule will delete some graph elements, otherwise -
     * false.
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
     * Returns true if rule is deleting on nodes and after deleting a
     * dangling-edge problem may occurred.
     *
     * Otherwise returns false.
     */
    public boolean mayCauseDanglingEdge() {
        final List<Node> delNodes = this.findNodesToDelete();
        if (delNodes.isEmpty()) {
            return false;
        }
        boolean result = false;
        for (int i = 0; i < delNodes.size() && !result; i++) {
            final Node n = delNodes.get(i);
            final List<Arc> inheritedArcs = this.getTypeSet().getInheritedArcs(n.getType());
            if (inheritedArcs.size() > 0) {
                // TypeGraph exists and arcs at Node of type n.getType()
                for (int j = 0; j < inheritedArcs.size() && !result; j++) {
                    final Arc a = inheritedArcs.get(j);
                    if (a.getSourceType().isParentOf(n.getType())) {
                        int number = n.getNumberOfOutgoingArcsOfTypeToTargetType(a.getType(), a.getTarget().getType());
                        if (number > 0) {
                            int tarMax = a.getType().getTargetMax(a.getSource().getType(),
                                    a.getTarget().getType());
                            if (tarMax != TypeSet.UNDEFINED
                                    && number != tarMax) {
                                result = true;
                            }
                        } else if (!this.hasNacWhichForbidsArc(a, n)) {
                            result = true;
                        }
//						else
//							result = true;
                    } else if (a.getTargetType().isParentOf(n.getType())) {
                        int number = n.getNumberOfIncomingArcsOfTypeFromSourceType(a.getType(), a.getSource().getType());
                        if (number > 0) {
                            int srcMax = a.getType().getSourceMax(a.getSource().getType(),
                                    a.getTarget().getType());
                            if (srcMax != TypeSet.UNDEFINED
                                    && number != srcMax) {
                                result = true;
                            }
                        } else if (!this.hasNacWhichForbidsArc(a, n)) {
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
        for (int i = 0; i < this.itsNACs.size(); i++) {
            OrdinaryMorphism nac = this.itsNACs.get(i);
            if (!nac.isEnabled()) {
                continue;
            }
            Iterator<Arc> arcs = nac.getTarget().getArcsCollection().iterator();
            while (arcs.hasNext()) {
                Arc a = arcs.next();
                if (!nac.getInverseImage(a).hasNext()
                        && a.getType() == typeArc.getType()) {
                    Node n = (Node) nac.getImage(lhsNode);
                    if (n == a.getSource()) {
                        return true;
                    } else if (n == a.getTarget()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isArcDeleting() {
        if (this.isDeleting) {
            for (final Iterator<Arc> en = getLeft().getArcsSet().iterator(); en.hasNext();) {
                if (getImage(en.next()) == null) {
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
    public boolean isArcDeleting(final Node src, final Type arct, final Node tar) {
        if (this.itsOrig.getNodesSet().contains(src)
                && this.itsOrig.getNodesSet().contains(tar)) {
            for (final Iterator<Arc> en = src.getOutgoingArcs(arct, tar.getType()).iterator(); en.hasNext();) {
                if (getImage(en.next()) == null) {
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
    public boolean isArcDeleting(final Arc a) {
        if (this.itsOrig.getArcsSet().contains(a)
                && this.getImage(a) == null) {
            return true;
        }
        return false;
    }

    /*
	 * Checks whether this rule creates an edge of the given type 
	 * and the specified source and target nodes.
	 * The nodes must be contained in the LHS of this rule. 
     */
    public boolean isArcCreating(final Node src, final Type arct, final Node tar) {
        if (this.isCreating
                && this.itsOrig.getNodesSet().contains(src)
                && this.itsOrig.getNodesSet().contains(tar)) {
            for (final Iterator<Arc> en = this.itsImag.getArcsSet().iterator(); en.hasNext();) {
                Arc a = en.next();
                if (a.getType().compareTo(arct)
                        && !this.getInverseImage(a).hasNext()) {
                    List<GraphObject> inv1 = this.getInverseImageList(a.getSource());
                    if (inv1.contains(src)) {
                        List<GraphObject> inv2 = this.getInverseImageList(a.getTarget());
                        if (inv2.contains(tar)) {
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
    public boolean isArcCreating(final Arc a) {
        if (//this.itsImag.getArcsSet().contains(a) &&
                !this.getInverseImage(a).hasNext()
                && this.getInverseImage(a.getSource()).hasNext()
                && this.getInverseImage(a.getTarget()).hasNext()) {
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
     * Returns preserved elements which attributes should be changed. The key is
     * an object of the LHS, the value - its image of the RHS.
     */
    public Map<GraphObject, GraphObject> getElementsToChange() {
        if (this.changedPreserved == null
                || this.itsOrig.changed) {
            this.changedPreserved = findElementsToChange();
        }
        return this.changedPreserved;
    }

    private List<GraphObject> findElementsToPreserve() {
        List<GraphObject> vec = new ArrayList<>();
        vec.addAll(this.itsDomObjects);
        return vec;
    }

    private List<GraphObject> findElementsToCreate() {
        List<GraphObject> vec = new ArrayList<>();
        vec.addAll(this.findNodesToCreate());
        vec.addAll(this.findArcsToCreate());
        this.isCreating = !vec.isEmpty();
        return vec;
    }

    private List<Node> findNodesToCreate() {
        List<Node> vec = new ArrayList<>();
        for (Iterator<Node> en = getRight().getNodesSet().iterator(); en.hasNext();) {
            Node o = en.next();
            if (!getInverseImage(o).hasNext()) {
                vec.add(o);
            }
        }
        return vec;
    }

    private List<Arc> findArcsToCreate() {
        List<Arc> vec = new ArrayList<>();
        for (Iterator<Arc> en = getRight().getArcsSet().iterator(); en.hasNext();) {
            Arc o = en.next();
            if (!getInverseImage(o).hasNext()) {
                vec.add(o);
            }
        }
        return vec;
    }

    private List<GraphObject> findElementsToDelete() {
        final List<GraphObject> vec = new ArrayList<>();
        vec.addAll(findNodesToDelete());
        vec.addAll(findArcsToDelete());
        return vec;
    }

    private List<Node> findNodesToDelete() {
        final List<Node> vec = new ArrayList<>();
        for (final Iterator<Node> en = getLeft().getNodesSet().iterator(); en.hasNext();) {
            final Node o = en.next();
            if (getImage(o) == null) {
                vec.add(o);
            }
        }
        this.isDeleting = !vec.isEmpty();
        this.isNodeDeleting = this.isDeleting;
        return vec;
    }

    private List<Arc> findArcsToDelete() {
        final List<Arc> vec = new ArrayList<>();
        for (final Iterator<Arc> en = getLeft().getArcsSet().iterator(); en.hasNext();) {
            final Arc o = en.next();
            if (getImage(o) == null) {
                vec.add(o);
            }
        }
        this.isDeleting = this.isDeleting || !vec.isEmpty();
        return vec;
    }

    /**
     * Returns true if this rule will change some attributes of the graph
     * elements, otherwise - false.
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
        Map<GraphObject, GraphObject> set = new HashMap<>();
        for (int i = 0; i < this.itsDomObjects.size(); i++) {
            GraphObject go = this.itsDomObjects.get(i);
            if (isChangingAttribute(go, getImage(go))) {
                set.put(go, getImage(go));
            }
        }
        this.isChanging = !set.isEmpty();
        return set;
    }

    private boolean isChangingAttribute(GraphObject obj,
            GraphObject img) {
        if (img.getAttribute() == null
                || img.getAttribute().getNumberOfEntries() == 0) {
            return false;
        }
        ValueTuple vtObj = (ValueTuple) obj.getAttribute();
        ValueTuple vtImg = (ValueTuple) img.getAttribute();
        for (int i = 0; i < vtObj.getNumberOfEntries(); i++) {
            ValueMember vmObj = vtObj.getValueMemberAt(i);
            ValueMember vmImg = vtImg.getValueMemberAt(vmObj.getName());
            if (vmImg != null && vmImg.isSet()) {
                if (!vmObj.isSet()) {
                    return true;
                } else if (!vmImg.getExprAsText().equals(vmObj.getExprAsText())) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
	 * protected boolean differentVariablesUsed(Graph g, Enumeration nacs) {
	 * AttrContext ac = getAttrContext(); VarTuple avt = (VarTuple)
	 * ac.getVariables(); Map<VarMember,Boolean> used = new Map<VarMember,Boolean>(avt.getSize());
	 * for (int i = 0; i < avt.getSize(); i++) { VarMember var =
	 * avt.getVarMemberAt(i); used.put(var, Boolean.valueOf(false)); } Enumeration e =
	 * g.getElements(); while (nacs.hasNext()) { used = new Map<VarMember,Boolean>(avt.getSize());
	 * for (int i = 0; i < avt.getSize(); i++) { VarMember var =
	 * avt.getVarMemberAt(i); used.put(var, Boolean.valueOf(false)); }
	 * OrdinaryMorphism m = (OrdinaryMorphism) nacs.next(); e =
	 * m.getTarget().getElements(); while (e.hasNext()) { GraphObject o =
	 * (GraphObject) e.next(); if(o.getAttribute() == null) continue;
	 * AttrInstance attr = o.getAttribute(); ValueTuple vt = (ValueTuple) attr;
	 * if (m.getInverseImage(o).hasNext()) { GraphObject orig =
	 * (GraphObject) m.getInverseImage(o).next(); ValueTuple vtOrig =
	 * (ValueTuple) orig.getAttribute(); for (int k = 0; k < vt.getSize(); k++) {
	 * ValueMember vm = vt.getValueMemberAt(k); ValueMember vmOrig =
	 * vtOrig.getValueMemberAt(k); if (vmOrig.isSet() && vm.isSet()) { if
	 * (vmOrig.getExpr().isVariable() && vm.getExpr().isVariable()) { //
	 * System.out.println(vm.getExpr()); if
	 * (!vmOrig.getExprAsText().equals(vm.getExprAsText())) return false; } } } } } }
	 * return true; }
	 * 
	 * 
	 * protected boolean areSameVariablesUsed() { AttrContext ac =
	 * getAttrContext(); VarTuple avt = (VarTuple) ac.getVariables(); Map<VarMember,Boolean>
	 * used = new Map<VarMember,Boolean>(avt.getSize()); for (int i = 0;
	 * i < avt.getSize(); i++) { VarMember var = avt.getVarMemberAt(i);
	 * used.put(var, Boolean.valueOf(false)); } List<VarMember> result = new
	 * List<VarMember>(); Enumeration e = getLeft().getElements(); while
	 * (e.hasNext()) { GraphObject o = (GraphObject) e.next();
	 * if(o.getAttribute() == null) continue; AttrInstance attr =
	 * o.getAttribute(); ValueTuple vt = (ValueTuple) attr; for (int k = 0; k <
	 * vt.getSize(); k++) { ValueMember vm = vt.getValueMemberAt(k); if
	 * (vm.isSet()) { if (vm.getExpr().isVariable()) { //
	 * System.out.println(vm.getExpr()); VarMember var =
	 * avt.getVarMemberAt(vm.getExprAsText()); if (((Boolean)
	 * used.get(var)).booleanValue() == false) used.put(var, Boolean.valueOf(true));
	 * else { if (!result.contains(var)) result.add(var); } } } } } if
	 * (result.size() != 0) return true; else return false; }
     */
    /**
     * Restores variable declarations of the RHS, NACs and PACs. The reason is:
     * the variables declarations can be lost after a step. Before the next
     * application of this rule can be done the lost variable declarations have
     * to be restored. This method is called during Critical Pair Analysis.
     */
    protected void restoreVariableDeclaration() {
        VarTuple vart = (VarTuple) getAttrContext().getVariables();
        if (this.itsImag.isAttributed()) {
            // check vars of RHS
            this.restoreVarDecl(this.itsImag, vart);
        }
        // check vars of NACs
        for (int l = 0; l < this.itsNACs.size(); l++) {
            OrdinaryMorphism nac = this.itsNACs.get(l);
            if (nac.getImage().isAttributed()) {
                this.restoreVarDecl(nac.getImage(), vart);
            }
        }
        // check vars of PACs
        for (int l = 0; l < this.itsPACs.size(); l++) {
            OrdinaryMorphism pac = this.itsPACs.get(l);
            if (pac.getImage().isAttributed()) {
                this.restoreVarDecl(pac.getImage(), vart);
            }
        }
        // check vars of nested ACs
        for (int l = 0; l < this.itsACs.size(); l++) {
            OrdinaryMorphism ac = this.itsACs.get(l);
            if (ac.getImage().isAttributed()) {
                this.restoreVarDecl(ac.getImage(), vart);
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
        final ArrayMovie<Type> vec = super.getUsedTypes();
        // add types of NACs
        for (int i = 0; i < this.itsNACs.size(); i++) {
            OrdinaryMorphism om = this.itsNACs.get(i);
            addUsedTypes(om.getTarget(), vec);
        }
        // add types of PACs
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism om = this.itsPACs.get(i);
            addUsedTypes(om.getTarget(), vec);
        }
        // add types of nested ACs
        for (int i = 0; i < this.itsACs.size(); i++) {
            OrdinaryMorphism om = this.itsACs.get(i);
            addUsedTypes(om.getTarget(), vec);
        }
        return vec;
    }

    private void addUsedTypes(final Graph g, final ArrayMovie<Type> vec) {
        Iterator<Node> nodes = g.getNodesSet().iterator();
        while (nodes.hasNext()) {
            GraphObject o = nodes.next();
            if (!vec.contains(o.getType())) {
                vec.add(o.getType());
            }
        }
        Iterator<Arc> arcs = g.getArcsSet().iterator();
        while (arcs.hasNext()) {
            GraphObject o = arcs.next();
            if (!vec.contains(o.getType())) {
                vec.add(o.getType());
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
     * Returns true if this rule can make a match basically. It works for
     * INJECTIVE matching, only.
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
     * Allows to define the CSP solver has to do next match completion starting
     * always by first CSP variable. This works for parallel match only. The
     * method <code>setParallelMatchingEnabled(true)</code> should be called
     * before.
     */
    public void setStartParallelMatchingByFirst(boolean b) {
        this.startParallelMatchByFirstCSPVar = b;
    }

    /**
     * Set value of the input parameter of its attribute context. The specified
     * parameters contain: String - is a name of an input parameter, first
     * Object of a List - is the value, second Object of a List - is the type of
     * this input parameter.
     */
    public void setInputParameters(HashMap<String, List<Object>> parameters) {
        VarTuple var = (VarTuple) getAttrContext().getVariables();
        int j = 0;
        for (int i = 0; i < var.getNumberOfEntries(); i++) {
            VarMember varm = var.getVarMemberAt(i);
            if (varm.isInputParameter()) {
                List<Object> valuePair = parameters.get(varm.getName());
                Object value = valuePair.get(0);
                String type = (String) valuePair.get(1);
                if (type.equals("int") || type.equals("boolean")
                        || type.equals("float") || type.equals("double")
                        || type.equals("short") || type.equals("long")) {
                    varm.setExprAsEvaluatedText(value.toString());
                } else {
                    varm.setExprAsObject(value);
                }
                j++;
            }
            if (j > parameters.size()) {
                break;
            }
        }
    }

    protected boolean evalDefaultFormula() {
        if (this.itsMatch == null) {
            return false;
        }
        if (this.itsACs.size() == 0) {
            return true;
        }
        int n = this.itsACs.size();
        final List<Evaluable> vars = new ArrayList<>(n);
        String tmp = "";
        int indx = -1;
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            if (ac.isEnabled()) {
                indx++;
                ac.setRelatedMorphism(this.itsMatch);
                vars.add(ac);
                if (indx == 0) {
                    if (this.formStr.equals("false")) {
                        tmp = tmp.concat("!".concat(String.valueOf(vars.size())));
                    } else {
                        tmp = tmp.concat(String.valueOf(vars.size()));
                    }
                } else {
                    if (this.formStr.equals("false")) {
                        tmp = tmp.concat("&!").concat(String.valueOf(vars.size()));
                    } else {
                        tmp = tmp.concat("&").concat(String.valueOf(vars.size()));
                    }
                }
            }
        }
//		System.out.println("Test formula of (nested) appl conds:  " + tmp);
        boolean res = this.itsFormula.setFormula(vars, tmp)
                && this.itsFormula.eval(this.itsMatch.getImage());
        if (!res) {
            this.itsMatch.setErrorMsg("Formula:  " + tmp + "  is violated!");
        }
        return res;
    }

    public boolean setDefaultFormulaTrue() {
        if (this.itsACs.size() == 0) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        }
        final List<Evaluable> vars = new ArrayList<>(this.itsACs.size());
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            if (ac.isEnabled()) {
                vars.add(ac);
            }
        }
        String tmp = "";
        for (int i = 0; i < vars.size(); i++) {
            String tmp1 = (i == 0) ? tmp.concat(String.valueOf(i + 1))
                    : tmp.concat("&").concat(String.valueOf(i + 1));
            tmp = tmp1;
        }
        if ("".equals(tmp)) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        }
        if (this.itsFormula.setFormula(vars, tmp)) {
            this.formStr = this.itsFormula.getAsString(vars);
            this.formReadStr = this.itsFormula.getAsString(vars, this.getNameOfEnabledACs());
//			System.out.println(this.formReadStr);
//			this.setTextualComment("Formula: ".concat(this.formReadStr));
            return true;
        }
        return false;
    }

    public boolean setDefaultFormulaFalse() {
        if (this.itsACs.size() == 0) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        }
        final List<Evaluable> vars = new ArrayList<>(this.itsACs.size());
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            if (ac.isEnabled()) {
                vars.add(ac);
            }
        }
        String tmp = "";
        for (int i = 0; i < vars.size(); i++) {
            String tmp1 = (i == 0) ? tmp.concat(String.valueOf(i + 1))
                    : tmp.concat("&").concat(String.valueOf(i + 1));
            tmp = tmp1;
        }
        if ("".equals(tmp)) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        } else {
            tmp = "!(".concat(tmp).concat(")");
        }
        if (this.itsFormula.setFormula(vars, tmp)) {
            this.formStr = this.itsFormula.getAsString(vars);
            this.formReadStr = this.itsFormula.getAsString(vars, this.getNameOfEnabledACs());
//			System.out.println(this.formReadStr);
//			this.setTextualComment("Formula: ".concat(this.formReadStr));
            return true;
        }
        return false;
    }

    public boolean evalFormula() {
        boolean result = true;
        if (this.itsMatch != null && this.itsACs.size() != 0) {
            for (int i = 0; i < this.itsACs.size(); i++) {
                NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
                if (ac.isEnabled()) {
                    ac.setRelatedMorphism(this.itsMatch);
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

    public void setFormula(Formula f) {
//		this.itsFormula = f;
//		this.formulaStr = this.itsFormula.getAsString(this.getEnabledGeneralACsAsEvaluable());
//		this.setTextualComment("Formula: ".concat(this.formulaStr));
        this.setFormula(f.getAsString(this.getEnabledGeneralACsAsEvaluable()), this.getEnabledACs());
    }

    /**
     * Set a boolean formula represented by the specified bnf string above
     * nested application conditions.
     *
     * @param bnf
     */
    public boolean setFormula(String bnf) {
//		final List<NestedApplCond> vars = new List<NestedApplCond>(this.itsACs.size());
//		for (int i=0; i<this.itsACs.size(); i++) {	
//			vars.add((NestedApplCond) this.itsACs.get(i));
//		}		
        return this.setFormula(bnf, this.getEnabledACs());
    }

    /**
     * Set a boolean formula represented by the specified bnf string above
     * nested application conditions.
     *
     * @param bnf
     */
    public boolean setFormula(String bnf, final List<NestedApplCond> list) {
        if (bnf.equals("true")) {
//			this.formStr = bnf;
//			this.formReadStr = bnf;
//			return true;
            return this.setDefaultFormulaTrue();
        } else if (bnf.equals("false")) {
            return this.setDefaultFormulaFalse();
        }
        final List<Evaluable> vars = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NestedApplCond ac = list.get(i);
            if (ac.isEnabled()) {
                vars.add(ac);
            }
        }
        if (vars.isEmpty()) {
            this.formStr = "true";
            this.formReadStr = "true";
            return true;
        }
        if (this.itsFormula.setFormula(vars, bnf)) {
            this.formStr = this.itsFormula.getAsString(vars);
            this.formReadStr = this.itsFormula.getAsString(vars, this.getNameOfEnabledACs());
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
     * Returns the formula string as internal represantation like this:
     * (1&2).>br> This method shoud be used for all actions relationg to Formula
     * objects.
     */
    public String getFormulaStr() {
        return this.formStr;
    }

    /**
     * Returns the formula string as readable representation like this: (nameOf1
     * & nameOf2). This method should be used for messages.
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
            for (int i = 0; i < this.itsACs.size(); i++) {
                NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
                if (ac.isEnabled()) {
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
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            if (ac.isEnabled()) {
                vars.add(ac.getName());
            }
        }
        return vars;
    }

    /**
     * Returns a list with names of enabled general application conditions and
     * its nested ACs inclusively.
     */
    public List<String> getNameOfEnabledNestedACs() {
        final List<String> vars = new ArrayList<>();
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            if (ac.isEnabled()) {
                vars.add(ac.getName());
            }
            vars.addAll(ac.getNameOfEnabledNestedACs());
        }
        return vars;
    }

    /**
     * Returns a list with names of all general application conditions and its
     * nested ACs inclusively.
     */
    public List<String> getNameOfNestedACs() {
        final List<String> vars = new ArrayList<>();
        for (int i = 0; i < this.itsACs.size(); i++) {
            NestedApplCond ac = (NestedApplCond) this.itsACs.get(i);
            vars.add(ac.getName());
            vars.addAll(ac.getNameOfEnabledNestedACs());
        }
        return vars;
    }

    /**
     * Makes the minimal rule from the given rule. A minimal rule comprises the
     * effects of a given rule in a minimal context.
     */
    public Rule getMinimalRule() {
        return BaseFactory.theBaseFactory.makeMinimalOfRule(this);
    }

    /**
     * Returns an inverse construction of this rule. This rule has to be
     * injective, otherwise returns null.
     *
     * Note: This method is mainly used during critical pair analysis.
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
     * This method does not destroy the Rule and OrdinaryMorphism instances of
     * the inverse construction. They must be disposed by the user object
     * explicitly. The local pair references set to null, only.
     */
    public void disposeInverseConstruct() {
        if (this.invConstruct != null) {
            this.invConstruct.dispose();
            this.invConstruct = null;
        }
    }

    /**
     * Destroys the Rule and OrdinaryMorphism instances of the inverse
     * construction. The local pair references set to null.
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
        VarTuple vars = (VarTuple) this.getAttrContext().getVariables();
        String s = this.getName().concat("(");
        String s1 = "";
        ArrayMovieInt order = vars.getSignaturOrder();
        for (int i = 0; i < order.size(); i++) {
            VarMember m = (VarMember) vars.getMemberAt(order.get(i));
            String nt = m.getName().concat(":").concat(m.getDeclaration().getTypeName());
            s1 = s1.concat(nt);
            if (i < (order.size() - 1)) {
                s1 = s1.concat(", ");
            }
        }
        String s2 = "";
        for (int i = 0; i < vars.getSize(); i++) {
            VarMember m = (VarMember) vars.getMemberAt(i);
            if (m.isOutputParameter()) {
                if (!s1.isEmpty()) {
                    s2 = s2.concat(", ");
                }
                s2 = s2.concat("out ");
                String nt = m.getName().concat(":").concat(m.getDeclaration().getTypeName());
                s2 = s2.concat(nt);
                break;
            }
        }
        s = s.concat(s1).concat(s2);
        s = s.concat(")");
        return s;
    }

    public void addInToSignatur(int indxOfVar) {
        ((VarTuple) this.getAttrContext().getVariables()).addToSignaturOrder(indxOfVar);
    }

    public void removeInFromSignatur(int indxOfVar) {
        ((VarTuple) this.getAttrContext().getVariables()).removeFromSignaturOrder(indxOfVar);
    }

    public void addOutToSignatur(int indxOfVar) {
        VarTuple vars = (VarTuple) this.getAttrContext().getVariables();
        for (int i = 0; i < vars.getSize(); i++) {
            VarMember m = (VarMember) vars.getMemberAt(i);
            if (i == indxOfVar) {
                m.setOutputParameter(true);
            } else {
                m.setOutputParameter(false);
            }
        }
    }

    public void removeOutFromSignatur(int indxOfVar) {
        VarTuple vars = (VarTuple) this.getAttrContext().getVariables();
        VarMember m = (VarMember) vars.getMemberAt(indxOfVar);
        if (m != null) {
            m.setOutputParameter(false);
        }
    }
}
