/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package test_agg.rulesystem;

import agg.attribute.AttrInstance;
import agg.attribute.AttrMember;
import agg.attribute.AttrVariableTuple;
import agg.attribute.impl.ValueMember;
import agg.attribute.impl.VarMember;
import agg.xt_basis.Arc;
import agg.xt_basis.CompletionStrategySelector;
import agg.xt_basis.DefaultGraTraImpl;
import agg.xt_basis.GraGra;
import agg.xt_basis.GraTra;
import agg.xt_basis.Graph;
import agg.xt_basis.GraphObject;
import agg.xt_basis.Match;
import agg.xt_basis.MorphCompletionStrategy;
import agg.xt_basis.Morphism;
import agg.xt_basis.Rule;
import agg.xt_basis.StaticStep;
import agg.xt_basis.TypeException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import test_agg.rulesystem.param.Parameter;

/**
 *
 * @author Janusch Rentenatus
 */
public class AggRuleSystem {

    public final static String START_GRAPH = "startGraph";

    private GraGra graphGrammar;
    private String semanticsName;
    private Graph dataGraph;

    public boolean loadSemantics(String semanticsName) {
        this.semanticsName = semanticsName;
        graphGrammar = new GraGra(false);
        boolean warwas = false;
        try {
            graphGrammar.load(semanticsName);
            warwas = true;
        } catch (Exception ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        if (!warwas) {
            graphGrammar = null;
            return false;
        }
        return true;
    }

    public Rule getRule(String ruleName) {
        return graphGrammar.getRule(ruleName);
    }

    public boolean execute(Rule rule, Parameter<?>[] values) {
        GraTra graTra = new DefaultGraTraImpl();
        graTra.setGraGra(graphGrammar);
        graTra.setHostGraph(dataGraph);
        graTra.setCompletionStrategy((MorphCompletionStrategy) CompletionStrategySelector.getDefault().clone());
        graTra.setGraTraOptions(graphGrammar.getGraTraOptions());

        AttrVariableTuple variables = rule.getAttrContext().getVariables();
        if (variables == null) {
            System.err.println(rule.getName() + ": Variables not found.");
            return false;
        }
        for (var param : values) {
            VarMember variable = variables.getVarMemberAt(param.getParamName());
            if (variable == null) {
                System.err.println(rule.getName() + ": Variable '" + param.getParamName() + "' not found.");
                return false;
            }
            variable.setExprAsObject(param.getValue());
        }
        graphGrammar.createMatchIndependent(rule, dataGraph);
        return graTra.apply(rule);
    }

    public List<String> scriptExecute(Rule rule) {
        Match match = graphGrammar.createMatchIndependent(rule, dataGraph);
        List<String> out = null;
        try {
            match.setCompletionStrategy((MorphCompletionStrategy) CompletionStrategySelector.getDefault().clone(), true);
            //match.getDomainObjects().addAll(aMatch.getLinkerRegelgraph());
            //match.getDomainObjects().addAll(aMatch.getDataMatchgraph());

            //System.out.println("------------------------");
            if (match.isValid()) {
                try {

                    Morphism co = StaticStep.execute(match);
                    out = calculateOutput(co);
                    co.dispose();
                } catch (TypeException ex) {
                    Logger.getGlobal().log(Level.SEVERE, "Rule " + rule.getName() + " : match failed.", ex);
                }
            } else {
                Logger.getGlobal().log(Level.SEVERE, "Rule {0} : match is not valid.", rule.getName());
            }
        } finally {
            graphGrammar.destroyMatch(match);
        }
        return out;
    }

    protected List<String> calculateOutput(Morphism co) {
        List<String> ret = new ArrayList<String>();
        Enumeration<GraphObject> codomainObjects = co.getCodomain();
        // Zuerst schreiben wir alles auf:
        while (codomainObjects.hasMoreElements()) {
            GraphObject graphObject = codomainObjects.nextElement();
            AttrInstance attr = graphObject.getAttribute();
            if (attr != null) {
                AttrMember memParam = attr.getMemberAt("param");
                if (graphObject.isArc() && memParam != null && memParam instanceof ValueMember && "msg".equals(graphObject.getType().getName())) {
                    String param1 = String.valueOf(((ValueMember) memParam).getExprAsObject());
                    Arc a = (Arc) graphObject;
                    GraphObject aTarget = a.getTarget();
                    AttrInstance attrTarget = aTarget.getAttribute();
                    int number = attrTarget.getNumberOfEntries();
                    for (int i = 0; i < number; i++) {
                        AttrMember memN = attrTarget.getMemberAt(i);
                        if (memN != null && memN instanceof ValueMember) {
                            ValueMember memVal = (ValueMember) memN;
                            Object value1 = memVal.getExprAsObject();
                            ret.add(param1 + "." + memN.getName() + "=" + value1);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public void reduce(String ruleName, HashMap<String, Object> returnMapOrNull) {
        reduce(getRule(ruleName), returnMapOrNull);
    }

    public void reduce(Rule rule, HashMap<String, Object> returnMapOrNull) {
        Match match = graphGrammar.createMatchIndependent(rule, dataGraph);
        try {
            int matchIndex = 0;
            match.setCompletionStrategy((MorphCompletionStrategy) CompletionStrategySelector.getDefault().clone(), true);
            while (match.nextCompletion()) {
                if (match.isValid()) {

                    final String matchNameIndex = rule.getName() + "(" + matchIndex + ").";
                    reduce(match.getCodomainObjects(), matchNameIndex, returnMapOrNull);
                    matchIndex++;
                }
            }
        } finally {
            graphGrammar.destroyMatch(match);
        }
    }

    protected void reduce(Vector<GraphObject> codomainObjects, String matchNameIndex, HashMap<String, Object> returnMapOrNull) {
        for (GraphObject graphObject : codomainObjects) {
            AttrInstance attr = graphObject.getAttribute();
            if (attr != null) {
                String goName = matchNameIndex + graphObject.getType().getName() + ".";
                for (int i = 0; i < attr.getNumberOfEntries(); i++) {
                    AttrMember mem = attr.getMemberAt(i);
                    if (mem instanceof ValueMember) {
                        ValueMember memVal = (ValueMember) mem;
                        if (returnMapOrNull != null) {
                            returnMapOrNull.put(goName + memVal.getName(),
                                    memVal.getExprAsObject());
                        }
                    }
                }
            }
        }
    }
}
