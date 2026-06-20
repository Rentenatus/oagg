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
 * Strategy class for managing Positive Application Conditions (PAC) in a Rule.
 */
public class RulePacStrategie {

    final protected List<OrdinaryMorphism> itsPACs = new ArrayList<>();
    protected List<ShiftedPAC> itsShiftedPACs;

    private Rule itsRule;

    /**
     * Creates a new PAC strategy for the specified rule.
     *
     * @param rule the rule this strategy belongs to
     */
    public RulePacStrategie(Rule rule) {
        this.itsRule = rule;
    }

    /**
     * Creates a new positive application condition (PAC) and adds it to this rule. Note: Because the new morphism is
     * initially empty and the LHS graph is not, it is not a morphism in theoretical terms, which demands a PAC to be a
     * total morphism.
     *
     * @return an empty morphism with the original set to this rule's left-hand side graph
     */
    public OrdinaryMorphism createPAC() {
        final OrdinaryMorphism positiveApplCond = new OrdinaryMorphism(
                getRule().getLeft(),
                BaseFactory.theFactory().createGraph(getRule().getRight().getTypeSet()),
                getRule().getRight().getAttrContext());
        this.itsPACs.add(positiveApplCond);
        AttrContext positiveApplCondContext = positiveApplCond.getAttrContext();
        positiveApplCond.getImage().setAttrContext(positiveApplCondContext);
        positiveApplCond.getImage().setKind(GraphKind.PAC);
        return positiveApplCond;
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
        return this.addPAC(-1, positiveApplCond);
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
        if (!this.itsPACs.contains(positiveApplCond)) {
            positiveApplCond.getTarget().setKind(GraphKind.PAC);
            if (index >= 0 && index < this.itsPACs.size()) {
                this.itsPACs.add(index, positiveApplCond);
            } else {
                this.itsPACs.add(positiveApplCond);
            }
            getRule().changed = true;
            return true;
        }
        return false;
    }

    /**
     * Adds a new shifted positive application condition composed of the specified list of morphisms.
     *
     * @param morphismList the list of morphisms that form the shifted PAC
     */
    public void addShiftedPAC(final List<OrdinaryMorphism> morphismList) {
        final ShiftedPAC newShiftedPac = new ShiftedPAC(morphismList);
        if (this.itsShiftedPACs == null) {
            this.itsShiftedPACs = new ArrayList<>();
        }
        this.itsShiftedPACs.add(newShiftedPac);
    }

    /**
     * Returns the list of all shifted positive application conditions.
     *
     * @return the list of shifted PACs, may be {@code null} if none exist
     */
    public List<ShiftedPAC> getShiftedPACs() {
        return this.itsShiftedPACs;
    }

    /**
     * Checks if the specified morphism is part of any shifted positive application condition.
     *
     * @param positiveApplCond the morphism to check
     * @return true if the morphism is part of a shifted PAC, false otherwise
     */
    public boolean isShiftedPAC(final OrdinaryMorphism positiveApplCond) {
        if (this.itsShiftedPACs == null) {
            return false;
        }
        for (int index = 0; index < this.itsShiftedPACs.size(); index++) {
            if (this.itsShiftedPACs.get(index).contains(positiveApplCond)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enables or disables all positive application conditions (PACs) of this rule.
     *
     * @param enable true to enable all PACs, false to disable them
     */
    public void enablePACs(boolean enable) {
        for (int index = 0; index < this.itsPACs.size(); index++) {
            this.itsPACs.get(index).setEnabled(enable);
        }
    }

    /**
     * Destroys the specified positive application condition and removes it from this rule. The target graph of the PAC
     * morphism is also disposed.
     *
     * @param positiveApplCond the positive application condition morphism to destroy
     */
    public void destroyPAC(final OrdinaryMorphism positiveApplCond) {
        this.itsPACs.remove(positiveApplCond);
        positiveApplCond.getImage().dispose();
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
     * Checks if this rule has at least one enabled positive application condition.
     *
     * @return true if the rule has at least one enabled PAC, false otherwise
     */
    public boolean hasEnabledPACs() {
        for (OrdinaryMorphism positiveApplCond : this.itsPACs) {
            if (positiveApplCond.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator over all positive application conditions of this rule.
     *
     * @return an iterator of all PAC morphisms
     */
    public Iterator<OrdinaryMorphism> getPACs() {
        return this.itsPACs.iterator();
    }

    /**
     * Returns an iterator over all enabled positive application conditions of this rule.
     *
     * @return an iterator of all enabled PAC morphisms
     */
    public Iterator<OrdinaryMorphism> getEnabledPACs() {
        var enabledPacsList = new ArrayList<OrdinaryMorphism>(2);
        for (OrdinaryMorphism positiveApplCond : this.itsPACs) {
            if (positiveApplCond.isEnabled()) {
                enabledPacsList.add(positiveApplCond);
            }
        }
        return enabledPacsList.iterator();
    }

    /**
     * Returns the list of all positive application condition morphisms of this rule.
     *
     * @return the list of PAC morphisms
     */
    public List<OrdinaryMorphism> getPACsList() {
        return this.itsPACs;
    }

    /**
     * Returns the positive application condition morphism with the specified name.
     *
     * @param name the name of the PAC to find
     * @return the PAC morphism with the specified name, or {@code null} if not found
     */
    public OrdinaryMorphism getPAC(String name) {
        for (int index = 0; index < this.itsPACs.size(); index++) {
            OrdinaryMorphism positiveApplCond = this.itsPACs.get(index);
            if (positiveApplCond.getName().equals(name)) {
                return positiveApplCond;
            }
        }
        return null;
    }

    /**
     * Returns the positive application condition morphism at the specified index.
     *
     * @param index the index of the PAC to retrieve
     * @return the PAC morphism at the specified index, or {@code null} if index is out of bounds
     */
    public OrdinaryMorphism getPAC(int index) {
        if (index >= 0 && index < this.itsPACs.size()) {
            return this.itsPACs.get(index);
        } else {
            return null;
        }
    }

    /**
     * Returns the positive application condition morphism with the specified target graph.
     *
     * @param graph the target graph to search for
     * @return the PAC morphism with the specified target graph, or {@code null} if not found
     */
    public OrdinaryMorphism getPAC(final Graph graph) {
        for (int index = 0; index < this.itsPACs.size(); index++) {
            OrdinaryMorphism applCond = this.itsPACs.get(index);
            if (applCond.getTarget() == graph) {
                return applCond;
            }
        }
        return null;
    }

    /**
     * Checks if the specified graph is the target graph of any positive application condition.
     *
     * @param graph the graph to check
     * @return true if the graph is a target of any PAC, false otherwise
     */
    public boolean hasPAC(final Graph graph) {
        for (int index = 0; index < this.itsPACs.size(); index++) {
            OrdinaryMorphism applCond = this.itsPACs.get(index);
            if (applCond.getTarget() == graph) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified positive application condition from this rule.
     *
     * @param positiveApplCond the positive application condition morphism to remove
     * @return false if the PAC was not found, true if it was removed successfully
     */
    public final boolean removePAC(OrdinaryMorphism positiveApplCond) {
        return this.itsPACs.remove(positiveApplCond);
    }

    /**
     * Checks dangling edges of the given PAC. Returns true if no dangling edge exists, otherwise false.
     *
     * @param ac the positive application condition to validate
     * @return true if the PAC has no dangling edges, false otherwise
     */
    public boolean isPACValid(OrdinaryMorphism ac) {
        if (ac.isEnabled()) {
            final Iterator<Node> objects = getRule().getLeft().getNodesSet().iterator();
            while (objects.hasNext()) {
                final Node x = objects.next();
                if (getRule().getImage(x) == null) {
                    final Node y = (Node) ac.getImage(x);
                    if (y != null
                            && x.getNumberOfArcs() != y.getNumberOfArcs()) {
                        getRule().setErrorMsg(ac.getName() + "  -  PAC failed (dangling edge)");
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
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism applicationCondition = this.itsPACs.get(i);
            if (!this.isPACValid(applicationCondition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies nodes and edges of its PACs in the LHS resp. RHS and extends the rule mapping. The PACs will be disabled.
     *
     * @return true if the extension was successful, false otherwise
     */
    public boolean extendByPacs() {
        for (int i = 0; i < this.itsPACs.size(); i++) {
            OrdinaryMorphism pac = this.itsPACs.get(i);
            if (pac.isEnabled()) {
                if (extendByPac(pac)) {
                    pac.setEnabled(false);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    boolean extendByPac(OrdinaryMorphism pac) {
        Map<Node, Node> n2n = new HashMap<>();
        Iterator<Node> nodes = pac.getTarget().getNodesCollection().iterator();
        while (nodes.hasNext()) {
            Node pn = nodes.next();
            Iterator<GraphObject> en = pac.getInverseImage(pn);
            if (!en.hasNext()) {
                try {
                    Node nln = getRule().getLeft().copyNode(pn);
                    n2n.put(pn, nln);
                    Node nrn = getRule().getRight().copyNode(pn);
                    n2n.put(nln, nrn);
                    try {
                        getRule().addMapping(nln, nrn);
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
                    Node rn = (Node) getRule().getImage(ln);
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
                        Arc nla = getRule().getLeft().copyArc(pa, srcl, tarl);
                        Node srcr = n2n.get(srcl);
                        Node tarr = n2n.get(tarl);
                        if (srcr != null && tarr != null) {
                            Arc nra = getRule().getRight().copyArc(pa, srcr, tarr);
                            if (nla != null && nra != null) {
                                try {
                                    getRule().addMapping(nla, nra);
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
     * Undo the copy of its PACs done by <code>extendByPacs</code>. The PACs will be enabled.
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

    boolean extendByPacUndo(OrdinaryMorphism pac) {
        boolean res = false;
        Iterator<Arc> arcsL = getRule().getLeft().getArcsCollection().iterator();
        while (arcsL.hasNext()) {
            Arc aL = arcsL.next();
            if (aL.getContextUsage() == pac.hashCode()) {
                Arc aR = (Arc) getRule().getImage(aL);
                getRule().removeMapping(aL, aR);
                try {
                    getRule().getRight().destroyArc(aR);
                    getRule().getLeft().destroyArc(aL);
                    res = true;
                } catch (TypeException ex) {
                    String exstr = ex.getLocalizedMessage();
                }
            }
        }
        Iterator<Node> nodes = getRule().getLeft().getNodesCollection().iterator();
        while (nodes.hasNext()) {
            Node nL = nodes.next();
            if (nL.getContextUsage() == pac.hashCode()) {
                Node nR = (Node) getRule().getImage(nL);
                getRule().removeMapping(nL, nR);
                try {
                    getRule().getRight().destroyNode(nR);
                    getRule().getLeft().destroyNode(nL);
                    res = true;
                } catch (TypeException ex) {
                    String exstr = ex.getLocalizedMessage();
                }
            }
        }
        return res;
    }

    /**
     * Checks if the specified PAC is using the specified variable in the context of the
     * specified attribute condition tuple.
     *
     * @param var the variable member to check for usage
     * @param act the attribute condition tuple providing context
     * @return true if the PAC uses the variable in the given context, false otherwise
     */
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

    /**
     * Clears all positive application conditions from this strategy.
     */
    public void clearPACs() {
        this.itsPACs.clear();
    }

    /**
     * Disposes all positive application conditions in this strategy.
     */
    public void disposeAllPACs() {
        while (!this.itsPACs.isEmpty()) {
            this.itsPACs.get(0).dispose(false, true);
            this.itsPACs.remove(0);
        }
        this.itsPACs.clear();
    }

    /**
     * Returns the list of all positive application condition morphisms.
     * Package-private for internal use by Rule class.
     *
     * @return the list of PAC morphisms
     */
    public List<OrdinaryMorphism> getPACsListInternal() {
        return this.itsPACs;
    }

    /**
     * Returns the list of shifted PACs.
     * Package-private for internal use by Rule class.
     *
     * @return the list of shifted PACs
     */
    List<ShiftedPAC> getShiftedPACsInternal() {
        return this.itsShiftedPACs;
    }

    /**
     * Sets the list of shifted PACs.
     * Package-private for internal use by Rule class.
     *
     * @param shiftedPACs the list of shifted PACs
     */
    void setShiftedPACsInternal(List<ShiftedPAC> shiftedPACs) {
        this.itsShiftedPACs = shiftedPACs;
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
