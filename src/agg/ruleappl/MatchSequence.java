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
package agg.ruleappl;

import agg.xt_basis.Arc;
import agg.xt_basis.BadMappingException;
import agg.xt_basis.BaseFactory;
import agg.xt_basis.ConcurrentRule;
import agg.xt_basis.Graph;
import agg.xt_basis.GraphObject;
import agg.xt_basis.Match;
import agg.xt_basis.Morphism;
import agg.xt_basis.Node;
import agg.xt_basis.OrdinaryMorphism;
import agg.xt_basis.Rule;
import agg.xt_basis.agt.AmalgamatedRule;
import agg.xt_basis.agt.RuleScheme;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class is used inside of the <code>RuleSequence</code> class to save
 * information about matches computed during applicability check of rule
 * sequences. The class <code>ApplRuleSequencesGraTraImpl</code> makes use of
 * this information to compute transformation matches of the rules of an
 * applicable rule sequences.
 *
 * @author olga
 *
 */
public class MatchSequence {

    RuleSequence ruleSequence;
    Graph graph;
    final List<Rule> rules;
    final List<Map<GraphObject, GraphObject>> matches;
    final List<Map<GraphObject, GraphObject>> comatches;
    Map<String, ObjectFlow> objectFlow;
    final Map<Integer, Rule> imgObj2Rule;
    final Map<Integer, ConcurrentRule> imgObj2ConcurrentRule;
    int trafoIndx = -1;

    /**
     * Initiates new instance and all needed containers.
     *
     * @param ruleSeq corresponding rule sequence
     */
    public MatchSequence(final RuleSequence ruleSeq) {
        this.ruleSequence = ruleSeq;
        this.graph = this.ruleSequence.getGraph();
        this.rules = new Vector<Rule>();
        this.matches = new Vector<Map<GraphObject, GraphObject>>();
        this.comatches = new Vector<Map<GraphObject, GraphObject>>();
        this.imgObj2Rule = new HashMap<Integer, Rule>();
        this.imgObj2ConcurrentRule = new HashMap<Integer, ConcurrentRule>();
    }

    public boolean objectFlowDefined() {
        return this.ruleSequence.isObjFlowDefined();
    }

    /**
     * Initiates newly this instance and all used containers.
     *
     * @param ruleSeq
     */
    public void reinit(final RuleSequence ruleSeq) {
        clear();
        this.ruleSequence = ruleSeq;
        this.graph = this.ruleSequence.getGraph();
        this.objectFlow = ruleSeq.getObjectFlow();
    }

    public void dispose() {
        this.clear();
    }

    public void clear() {
        this.rules.clear();
        this.matches.clear();
        this.comatches.clear();
        this.imgObj2Rule.clear();
        this.imgObj2ConcurrentRule.clear();
        this.trafoIndx = -1;
    }

    public void clearComatches() {
        this.comatches.clear();
        this.trafoIndx = -1;
    }

    public void setObjectFlow(final Map<String, ObjectFlow> objectFlow) {
        this.objectFlow = objectFlow;
    }

    /**
     * Saves information about current rule and match on which this rule is
     * applicable at a host graph.
     *
     * @param currentRule an applicable rule
     * @param m	a valid match
     */
    public void addDirectMatch(final Rule currentRule, final OrdinaryMorphism m) {
        final Map<GraphObject, GraphObject> match = new HashMap<GraphObject, GraphObject>();
        final Iterator<GraphObject> objs = m.getDomain();
        while (objs.hasNext()) {
            GraphObject obj = objs.next();
            GraphObject img = m.getImage(obj);
            match.put(obj, img);
        }
        this.matches.add(match);
        this.rules.add(currentRule);
    }

    /**
     * Saves information about current rule and its pure enabling predecessor
     * rule.
     *
     * @param currentRule the current rule
     * @param totalPureRule	a pure enabling predecessor rule
     * @param m	embedding of the LHS of the current rule into the RHS of the
     * pure enabling predecessor rule
     * @param indx_currentRule
     * @param indx_totalPureRule
     */
    public void addTotalPureEnablingSourceMatch(
            final Rule currentRule,
            final Rule totalPureRule,
            final OrdinaryMorphism m,
            int indx_currentRule,
            int indx_totalPureRule) {
        final Map<GraphObject, GraphObject> match = new HashMap<GraphObject, GraphObject>();
        final Iterator<GraphObject> objs = m.getDomain();
        while (objs.hasNext()) {
            GraphObject obj = objs.next();
            GraphObject img = m.getImage(obj);
            match.put(obj, img);
        }
        this.matches.add(match);
        this.rules.add(currentRule);
        this.imgObj2Rule.put(Integer.valueOf(indx_totalPureRule), totalPureRule);
    }

    /**
     * Saves information about current rule and corresponding concurrent rule
     * and match which are computed during applicability check of rule sequence.
     *
     * @param currentRule a rule
     * @param concurrentRule	computed concurrent rule
     * @param m	a valid match of the concurrent rule
     */
    public void addConcurrentSourceMatch(
            final Rule currentRule,
            final ConcurrentRule concurrentRule,
            final OrdinaryMorphism m) {
        Map<GraphObject, GraphObject> match = new HashMap<GraphObject, GraphObject>();
        final Iterator<GraphObject> objs = m.getDomain();
        while (objs.hasNext()) {
            GraphObject obj = objs.next();
            GraphObject img = m.getImage(obj);
            match.put(obj, img);
        }
        this.matches.add(match);
        this.rules.add(currentRule);
        int indx = this.matches.size() - 1;
        this.imgObj2ConcurrentRule.put(Integer.valueOf(indx), concurrentRule);
    }

    /**
     * Saves information about the comatch after the given applicable rule was
     * applied at a host graph.
     *
     * @param r
     * @param com
     */
    public void addComatch(final Rule r, final Morphism com) {
        final Map<GraphObject, GraphObject> comatch = new HashMap<GraphObject, GraphObject>();
        final Iterator<GraphObject> objs = com.getDomain();
        while (objs.hasNext()) {
            GraphObject obj = objs.next();
            GraphObject img = com.getImage(obj);
            if (r instanceof AmalgamatedRule) {
                RuleScheme rs = r.getRuleScheme();
                GraphObject kernelRObj = rs.getRHSKernelOfAmalgamRuleObject(obj);
                if (kernelRObj != null) {
                    comatch.put(kernelRObj, img);
                }
            } else {
                comatch.put(obj, img);
            }
        }
        if (this.trafoIndx < 0) {
            this.comatches.add(comatch);
        } else {
            while (this.trafoIndx > (this.comatches.size())) {
                this.comatches.add(null);
            }
            this.comatches.add(this.trafoIndx, comatch);
        }
    }

    public Map<GraphObject, GraphObject> getDirectMatch(
            int indx,
            final Rule rule) {
        // predefined matches exist
        if (indx >= 0 && indx < this.matches.size()) {
            return this.matches.get(indx);
        }
        return null;
    }

    /**
     * Returns GraphObject pairs of partial match of the specified rule at the
     * specified index in the rule sequence if and only if an object flow is
     * defined.
     *
     * @param indx
     * @param rule
     * @return table with GraphObject pairs
     */
    public Map<GraphObject, GraphObject> getMatch(
            int indx,
            final Rule rule) {
        // no predefined matches exist
        if (this.matches.size() == 0) {
            final Map<GraphObject, GraphObject> match = makeMatchMapByObjectFlow(rule, indx);
            return match;
        }
        return null;
    }

    /**
     * Returns GraphObject pairs of partial match of the specified rule at the
     * specified index in the rule sequence.
     *
     * @param indx	index of the current rule
     * @param rule pure enabling predecessor rule
     * @param preRuleIndx	index of the predecessor rule
     * @param preRule	the predecessor rule
     * @param g	a host graph
     *
     * @return	a table with pairs of match objects
     */
    public Map<GraphObject, GraphObject> getMatch(
            int indx,
            final Rule rule,
            final int preRuleIndx,
            final Rule preRule,
            final Graph g) {
        // no predefined matches exist
        if (this.matches.size() == 0) {
            final Map<GraphObject, GraphObject> match = makeMatchMapByObjectFlow(rule, indx);
            return match;
        } else if (indx >= 0 && indx < this.matches.size() && preRuleIndx < indx) {
            final Map<GraphObject, GraphObject> srcMatch = this.matches.get(indx);
            // rule is the first rule
            if (preRule == null) {
                if (this.ruleSequence.getGraph() != null
                        && this.objectFlow.get("0:1") != null
                        && this.objectFlow.get("0:1").isSourceOfOutput(this.ruleSequence.getGraph())
                        && this.objectFlow.get("0:1").isSourceOfInput(rule)) {
                    final Map<GraphObject, GraphObject> match = makeMatchMapByObjectFlow(
                            rule,
                            indx,
                            this.objectFlow.get("0:1"));
                    return match;
                }
                // initial match of the first rule
                return srcMatch;
            }
            Map<GraphObject, GraphObject> match = makeMatchMapByObjectFlow(
                    rule,
                    indx);
            if (!match.isEmpty()) {
                return match;
            }
            if (this.imgObj2Rule.get(Integer.valueOf(indx)) != null) {
                match = makeMatchMapByTotalPureRule(
                        indx, preRule, this.comatches.get(preRuleIndx));
                return match;
            } else if (this.imgObj2ConcurrentRule.get(Integer.valueOf(indx)) != null
                    && this.imgObj2ConcurrentRule.get(Integer.valueOf(indx)).getSecondSourceRule() == rule) {
                match = makeMatchMapByConcurrentRuleBackward(
                        indx, rule, g);
                return match;
            } else {
                // direct match
                return srcMatch;
            }
        }
        return null;
    }

    public ObjectFlow getObjectFlowForRules(final Rule r1, int indx_r1, final Rule r2, int indx_r2) {
        return this.ruleSequence.getObjFlowForRules(r1, indx_r1, r2, indx_r2);
    }

    public List<ObjectFlow> getObjectFlowForRule(final Rule r, int indx) {
        return this.ruleSequence.getObjFlowForRule(r, indx);
    }

    private Map<GraphObject, GraphObject> makeMatchMapByTotalPureRule(
            int indx,
            final Rule preRule,
            final Map<GraphObject, GraphObject> preRuleComatch) {
//		System.out.println("## MatchSequence.makeMatchByTotalPureRule  :: ");
        final Map<GraphObject, GraphObject> match = new HashMap<GraphObject, GraphObject>();
        final Map<GraphObject, GraphObject> srcMatch = this.matches.get(indx);
        final Rule totalPureRule = this.imgObj2Rule.get(Integer.valueOf(indx));
        final Enumeration<GraphObject> objs = Collections.enumeration(srcMatch.keySet());
        while (objs.hasMoreElements()) {
            GraphObject obj = objs.nextElement();
            GraphObject img = srcMatch.get(obj);
            if (totalPureRule == preRule) {
                GraphObject img2 = preRuleComatch.get(img);
                if (img2 != null && img2.getContext() != null) {
                    match.put(obj, img2);
                }
            } else {
                final Map<GraphObject, GraphObject> srcComatch
                        = this.comatches.get(this.ruleSequence.getIndexOf(totalPureRule));
                if (srcComatch != null) {
                    GraphObject img2 = srcComatch.get(img);
                    if (img2 != null && img2.getContext() != null) {
                        match.put(obj, img2);
                    }
                }
            }
        }
        return match;
    }

    public void fillMatchMapByObjectFlow(
            final Rule rule,
            final ObjectFlow objFlow,
            final Object srcOfOutput,
            final Map<GraphObject, GraphObject> matchmap) {
        if (srcOfOutput instanceof Rule) {
            final Rule preRule = (Rule) srcOfOutput;
            int indx_preRule = objFlow.getIndexOfOutput();
            if (this.ruleSequence.getGraph() != null) {
                indx_preRule--;
            }
            if (indx_preRule >= 0 && indx_preRule < this.comatches.size()) {
                final Map<GraphObject, GraphObject> srcComatch = this.comatches.get(indx_preRule);
                if (srcComatch != null) {
                    Iterator<?> lhs = rule.getLeft().getNodesSet().iterator();
                    while (lhs.hasNext()) {
                        GraphObject lhs_obj = (GraphObject) lhs.next();
                        GraphObject rhs_obj = (GraphObject) objFlow.getOutput(lhs_obj);
                        if (rhs_obj != null) {
                            GraphObject g_obj = srcComatch.get(rhs_obj);
                            if (g_obj != null && g_obj.getContext() != null) {
                                matchmap.put(lhs_obj, g_obj);
                            }
                        }
                    }
                    lhs = rule.getLeft().getArcsSet().iterator();
                    while (lhs.hasNext()) {
                        GraphObject lhs_obj = (GraphObject) lhs.next();
                        GraphObject rhs_obj = (GraphObject) objFlow.getOutput(lhs_obj);
                        if (rhs_obj != null) {
                            GraphObject g_obj = srcComatch.get(rhs_obj);
                            if (g_obj != null && g_obj.getContext() != null) {
                                matchmap.put(lhs_obj, g_obj);
                            }
                        }
                    }
                }
            } else if (indx_preRule >= 0 && indx_preRule < this.matches.size()) {
                final Map<GraphObject, GraphObject> preMatch = this.matches.get(indx_preRule);
                Iterator<?> lhs = rule.getLeft().getNodesSet().iterator();
                while (lhs.hasNext()) {
                    GraphObject lhs_obj = (GraphObject) lhs.next();
                    GraphObject rhs_obj = (GraphObject) objFlow.getOutput(lhs_obj);
                    if (rhs_obj != null) {
                        if (preRule.hasInverseImage(rhs_obj)) {
                            GraphObject pre_lhs_obj = preRule.firstOfInverseImage(rhs_obj);
                            GraphObject g_obj = preMatch.get(pre_lhs_obj);
                            if (g_obj != null) {
                                matchmap.put(lhs_obj, g_obj);
                            }
                        }
                    }
                }
                lhs = rule.getLeft().getArcsSet().iterator();
                while (lhs.hasNext()) {
                    GraphObject lhs_obj = (GraphObject) lhs.next();
                    GraphObject rhs_obj = (GraphObject) objFlow.getOutput(lhs_obj);
                    if (rhs_obj != null) {
                        if (preRule.hasInverseImage(rhs_obj)) {
                            GraphObject pre_lhs_obj = preRule.firstOfInverseImage(rhs_obj);
                            GraphObject g_obj = preMatch.get(pre_lhs_obj);
                            if (g_obj != null) {
                                matchmap.put(lhs_obj, g_obj);
                            }
                        }
                    }
                }
            }
        } else if (srcOfOutput instanceof Graph) {
            Iterator<?> lhs = rule.getLeft().getNodesSet().iterator();
            while (lhs.hasNext()) {
                GraphObject lhs_obj = (GraphObject) lhs.next();
                GraphObject g_obj = (GraphObject) objFlow.getOutput(lhs_obj);
                if (g_obj != null && g_obj.getContext() != null) {
                    matchmap.put(lhs_obj, g_obj);
                }
            }
            lhs = rule.getLeft().getArcsSet().iterator();
            while (lhs.hasNext()) {
                GraphObject lhs_obj = (GraphObject) lhs.next();
                GraphObject g_obj = (GraphObject) objFlow.getOutput(lhs_obj);
                if (g_obj != null && g_obj.getContext() != null) {
                    matchmap.put(lhs_obj, g_obj);
                }
            }
        }
    }

    private Map<GraphObject, GraphObject> makeMatchMapByObjectFlow(
            final Rule rule,
            int indx,
            final ObjectFlow objFlow) {
//		System.out.println("### MatchSequence.makeMatchByObjectFlow  of rule:  "+rule.getName());
        final Map<GraphObject, GraphObject> matchmap = new HashMap<GraphObject, GraphObject>();
        final Object srcOfOutput = objFlow.getSourceOfOutput();
        fillMatchMapByObjectFlow(rule, objFlow, srcOfOutput, matchmap);
        return matchmap;
    }

    public Map<GraphObject, GraphObject> makeMatchMapByObjectFlow(
            final Rule rule,
            int indx) {
//		System.out.println("## MatchSequence.makeMatchByObjectFlow  of rule:  "+rule.getName());
        final Map<GraphObject, GraphObject> matchmap = new HashMap<GraphObject, GraphObject>();
        if (indx == -1) {
            return matchmap;
        }
        int index = indx;
        if (this.ruleSequence.getGraph() != null) {
            index++;
        }
        Enumeration<String> keys = Collections.enumeration(this.objectFlow.keySet());
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String[] keyItems = key.split(":");
            ObjectFlow objFlow = this.objectFlow.get(key);
            if (Integer.valueOf(keyItems[1]).intValue() == index
                    && rule.getName().equals(((Rule) objFlow.getSourceOfInput()).getName())) {
                Object srcOfOutput = objFlow.getSourceOfOutput();
                fillMatchMapByObjectFlow(rule, objFlow, srcOfOutput, matchmap);
            }
        }
        return matchmap;
    }

    public Map<GraphObject, GraphObject> makeMatchMapByObjectFlow(
            final Rule rule,
            final List<ObjectFlow> objFlowList) {
//		System.out.println("##### MatchSequence.makeMatchByObjectFlow  of rule:  "+rule.getName());
        final Map<GraphObject, GraphObject> matchmap = new HashMap<GraphObject, GraphObject>();
        for (int i = 0; i < objFlowList.size(); i++) {
            ObjectFlow objFlow = objFlowList.get(i);
            Object srcOfOutput = objFlow.getSourceOfOutput();
            fillMatchMapByObjectFlow(rule, objFlow, srcOfOutput, matchmap);
        }
        return matchmap;
    }

    private Map<GraphObject, GraphObject> makeMatchMapByConcurrentRuleBackward(
            int indx,
            final Rule ruleAtIndx,
            final Graph g) {
        final Map<GraphObject, GraphObject> match = new HashMap<GraphObject, GraphObject>();
//		final Map<GraphObject, GraphObject> srcMatch = this.matches.get(indx);
        final ConcurrentRule concurRule = this.imgObj2ConcurrentRule.get(Integer.valueOf(indx));
        boolean secondRuleIsRuleAtIndx = false;
        ConcurrentRule testCR = concurRule;
        while (!secondRuleIsRuleAtIndx) {
            if (testCR.getSecondSourceRule() == ruleAtIndx) {
                secondRuleIsRuleAtIndx = true;
            } else if (testCR.getSecondSourceConcurrentRule() != null) {
                testCR = testCR.getSecondSourceConcurrentRule();
            } else {
                break;
            }
        }
        if (secondRuleIsRuleAtIndx) {
            if (concurRule.getSecondSourceConcurrentRule() != null) {
                final Vector<GraphObject> domList = new Vector<GraphObject>();
                ConcurrentRule tmpCR = concurRule;
                ConcurrentRule tmpPreCR = tmpCR.getSecondSourceConcurrentRule();
                // find recursively all done comatches of rule before
                while (tmpPreCR != null) {
                    int i = tmpPreCR.getIndexOfFirstSourceRule();
                    if (i >= 0 && i < this.comatches.size()
                            && this.comatches.get(i) != null) {
                        final Enumeration<GraphObject> elems = Collections.enumeration(this.comatches.get(i).values());
                        while (elems.hasMoreElements()) {
                            final GraphObject obj = elems.nextElement();
                            if (obj.getContext() != null && !domList.contains(obj)) {
                                domList.add(obj);
                            }
                        }
                    }
                    tmpCR = tmpPreCR;
                    tmpPreCR = tmpCR.getSecondSourceConcurrentRule();
                }
                int i = tmpCR.getIndexOfFirstSourceRule();
                if (i >= 0 && i < this.comatches.size()
                        && this.comatches.get(i) != null) {
                    final Enumeration<GraphObject> elems = Collections.enumeration(this.comatches.get(i).values());
                    while (elems.hasMoreElements()) {
                        final GraphObject obj = elems.nextElement();
                        if (obj.getContext() != null && !domList.contains(obj)) {
                            domList.add(obj);
                        }
                    }
                }
                makeGraphObjectMap(ruleAtIndx, domList, match, g);
            }
        }
        return match;
    }

    /*
	private Map<GraphObject, GraphObject> makeMatchMapByConcurrentRuleForward(
			int indx,
			final Rule ruleAtIndx,
			final Graph g) {
		
		final Map<GraphObject, GraphObject> match = new HashMap<GraphObject, GraphObject>();
		final Map<GraphObject, GraphObject> srcMatch = this.matches.get(indx);
		final ConcurrentRule concurRule = this.imgObj2ConcurrentRule.get(Integer.valueOf(indx));
				
		if (concurRule.getSecondSourceRule() == ruleAtIndx) {
			
			if (concurRule.getFirstSourceConcurrentRule() != null) {				
				
				final Vector<GraphObject> domList = new Vector<GraphObject>();
				
				ConcurrentRule tmpCR = concurRule;
				ConcurrentRule tmpPreCR = tmpCR.getFirstSourceConcurrentRule();
				// find recursively all done comatches of rule before
				while (tmpPreCR != null) {			
					int i = this.rules.indexOf(tmpPreCR.getSecondSourceRule());					
					if (i >= 0 && i < this.comatches.size()) {
						final Enumeration<GraphObject> elems = this.comatches.get(i).elements();
						while (elems.hasMoreElements()) {
							final GraphObject obj = elems.nextElement();
							if (obj.getContext() != null && !domList.contains(obj)) {
								domList.add(obj);
							}						
						}
					} 
					tmpCR = tmpPreCR;
					tmpPreCR = tmpCR.getFirstSourceConcurrentRule();
				}
				
				int i = this.rules.indexOf(tmpCR.getFirstSourceRule());				
				if (i >= 0 && i < this.comatches.size()) {
					final Enumeration<GraphObject> elems = this.comatches.get(i).elements();
					while (elems.hasMoreElements()) {
						final GraphObject obj = elems.nextElement();
						if (obj.getContext() != null && !domList.contains(obj)) {
							domList.add(obj);
						}						
					}
				} 					
				
				makeGraphObjectMap(ruleAtIndx, domList, match, g);
			}					
		}
		
		return match;
	}
     */
    private void makeGraphObjectMap(
            final Rule r,
            final Vector<GraphObject> goSet,
            final Map<GraphObject, GraphObject> map,
            final Graph g) {
        if (!goSet.isEmpty()) {
            // create test match
            Match m = BaseFactory.theFactory().createMatch(r, g);
            // first try to map edges 
            final Iterator<Arc> arcs = m.getSource().getArcsSet().iterator();
            while (arcs.hasNext()) {
                final Arc a = arcs.next();
                for (int i = 0; i < goSet.size(); i++) {
                    final GraphObject obj = goSet.get(i);
                    if (obj.getContext() != null
                            && obj.isArc()
                            && a.getType().compareTo(obj.getType())) {
                        boolean srcOK = false;
                        if (m.getImage(a.getSource()) == null) {
                            try {
                                m.addMapping(a.getSource(), ((Arc) obj).getSource());
                                map.put(a.getSource(), ((Arc) obj).getSource());
                                srcOK = true;
                            } catch (BadMappingException ex) {
                                System.out.println("MatchSequence.makeGraphObjectMap (Node)  of match based on concurrent rule: " + ex.getLocalizedMessage());
                            }
                        } else {
                            srcOK = true;
                        }
                        boolean tarOK = false;
                        if (m.getImage(a.getTarget()) == null) {
                            try {
                                m.addMapping(a.getTarget(), ((Arc) obj).getTarget());
                                map.put(a.getTarget(), ((Arc) obj).getTarget());
                                tarOK = true;
                            } catch (BadMappingException ex) {
                                System.out.println("MatchSequence.makeGraphObjectMap (Node)  of match based on concurrent rule: " + ex.getLocalizedMessage());
                            }
                        } else {
                            tarOK = true;
                        }
                        if (srcOK && tarOK) {
                            try {
                                m.addMapping(a, obj);
                                map.put(a, obj);
                                goSet.remove(obj);
                                goSet.remove(((Arc) obj).getSource());
                                goSet.remove(((Arc) obj).getTarget());
                                break;
                            } catch (BadMappingException ex) {
                                System.out.println("MatchSequence.makeGraphObjectMap (Arc)  of match based on concurrent rule: " + ex.getLocalizedMessage());
                            }
                        }
                    }
                }
            }
            // now try to map single nodes
            final Iterator<Node> nodes = m.getSource().getNodesSet().iterator();
            while (nodes.hasNext()) {
                final Node n = nodes.next();
                if (m.getImage(n) == null) {
                    for (int i = 0; i < goSet.size(); i++) {
                        final GraphObject obj = goSet.get(i);
                        if (obj.isNode() && n.getType().isParentOf(obj.getType())) {
                            if (!m.hasInverseImage(obj)) {
                                try {
                                    m.addMapping(n, obj);
                                    goSet.remove(obj);
                                    map.put(n, obj);
                                    break;
                                } catch (BadMappingException ex) {
                                    System.out.println("MatchSequence.makeGraphObjectMap (Node)  of match based on concurrent rule: " + ex.getLocalizedMessage());
                                }
                            }
                        }
                    }
                }
            }
            // dispose test match
            m.dispose();
            r.setMatch(null);
            m = null;
        }
    }

    public void setTrafoIndex(int i) {
        this.trafoIndx = i;
    }
}
