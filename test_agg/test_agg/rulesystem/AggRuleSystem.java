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
import agg.xt_basis.CompletionStrategySelector;
import agg.xt_basis.DefaultGraTraImpl;
import agg.xt_basis.GraGra;
import agg.xt_basis.GraTra;
import agg.xt_basis.Graph;
import agg.xt_basis.GraphObject;
import agg.xt_basis.Match;
import agg.xt_basis.MorphCompletionStrategy;
import agg.xt_basis.Rule;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import test_agg.rulesystem.param.Parameter;

/**
 *
 * @author Janusch Rentenatus
 */
public class AggRuleSystem {

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

    public boolean useGraph(String graphName) {
        Graph g = graphGrammar.getGraph(graphName);
        if (g == null) {
            return false;
        }
        dataGraph = g.graphcopy();
        return dataGraph != null;
    }

    public String getSemanticsName() {
        return semanticsName;
    }

    public Graph getDataGraph() {
        return dataGraph;
    }

    public Rule getRule(String ruleName) {
        return graphGrammar.getRule(ruleName);
    }

    public boolean execute(String ruleName, Parameter<?>... values) {
        return execute(getRule(ruleName), values);
    }

    public boolean execute(Rule rule, Parameter<?>... values) {
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

    public void reduce(String ruleName, Map<String, Object> returnMapOrNull) {
        reduce(getRule(ruleName), returnMapOrNull);
    }

    public void reduce(Rule rule, Map<String, Object> returnMapOrNull) {
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

    protected void reduce(Vector<GraphObject> codomainObjects, String matchNameIndex, Map<String, Object> returnMapOrNull) {
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
