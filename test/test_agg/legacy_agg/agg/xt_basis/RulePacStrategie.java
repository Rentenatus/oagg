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

import agg.attribute.AttrContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Strategy class for managing Positive Application Conditions (PAC) in a Rule.
 */
public class RulePacStrategie extends RuleConditionStrategy {

    protected List<ShiftedPAC> itsShiftedPACs;

    /**
     * Creates a new PAC strategy for the specified rule.
     *
     * @param rule the rule this strategy belongs to
     */
    public RulePacStrategie(Rule rule) {
        super(rule);
    }

    @Override
    public OrdinaryMorphism createAc() {
        final OrdinaryMorphism positiveApplCond = new OrdinaryMorphism(
                getRule().getLeft(),
                BaseFactory.theFactory().createGraph(getRule().getRight().getTypeSet()),
                getRule().getRight().getAttrContext());
        this.itsACs.add(positiveApplCond);
        AttrContext positiveApplCondContext = positiveApplCond.getAttrContext();
        positiveApplCond.getImage().setAttrContext(positiveApplCondContext);
        positiveApplCond.getImage().setKind(GraphKind.PAC);
        return positiveApplCond;
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

    @Override
    public boolean addAc(int index, final OrdinaryMorphism positiveApplCond) {
        if (!this.itsACs.contains(positiveApplCond)) {
            positiveApplCond.getTarget().setKind(GraphKind.PAC);
            if (index >= 0 && index < this.itsACs.size()) {
                this.itsACs.add(index, positiveApplCond);
            } else {
                this.itsACs.add(positiveApplCond);
            }
            getRule().changed = true;
            return true;
        }
        return false;
    }

    /**
     * Returns an iterator over all enabled positive application conditions of this rule.
     *
     * @return an iterator of all enabled PAC morphisms
     */
    public Iterator<OrdinaryMorphism> getEnabledAcs() {
        var enabledPacsList = new ArrayList<OrdinaryMorphism>(2);
        for (OrdinaryMorphism positiveApplCond : this.itsACs) {
            if (positiveApplCond.isEnabled()) {
                enabledPacsList.add(positiveApplCond);
            }
        }
        return enabledPacsList.iterator();
    }

    @Override
    public boolean isAcValid(OrdinaryMorphism ac) {
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

    @Override
    public boolean areAcsValid() {
        for (int i = 0; i < this.itsACs.size(); i++) {
            OrdinaryMorphism applicationCondition = this.itsACs.get(i);
            if (!this.isAcValid(applicationCondition)) {
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
    public boolean extendByAcs() {
        for (int i = 0; i < this.itsACs.size(); i++) {
            OrdinaryMorphism pac = this.itsACs.get(i);
            if (pac.isEnabled()) {
                if (extendByAc(pac)) {
                    pac.setEnabled(false);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    boolean extendByAc(OrdinaryMorphism pac) {
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
     * Undo the copy of its PACs done by <code>extendByAcs</code>.The PACs will be enabled.
     * @return 
     */
    public boolean extendByAcsUndo() {
        for (int i = 0; i < this.itsACs.size(); i++) {
            OrdinaryMorphism pac = this.itsACs.get(i);
            if (extendByAcUndo(pac)) {
                pac.setEnabled(true);
            }
        }
        return true;
    }

    boolean extendByAcUndo(OrdinaryMorphism pac) {
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
     * Returns the list of shifted PACs. Package-private for internal use by Rule class.
     *
     * @return the list of shifted PACs
     */
    List<ShiftedPAC> getShiftedPACsInternal() {
        return this.itsShiftedPACs;
    }

    /**
     * Sets the list of shifted PACs. Package-private for internal use by Rule class.
     *
     * @param shiftedPACs the list of shifted PACs
     */
    void setShiftedPACsInternal(List<ShiftedPAC> shiftedPACs) {
        this.itsShiftedPACs = shiftedPACs;
    }
}
