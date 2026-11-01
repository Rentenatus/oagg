/**
 **
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright> *****************************************************************************
 */
/**
 *
 */
package agg.gui.cpa;

import agg.editor.impl.EdGraGra;
import agg.editor.impl.EdGraph;
import agg.editor.impl.EdType;
import agg.parser.ConflictsDependenciesContainer;
import agg.parser.PairContainer;
import agg.util.XMLHelper;
import agg.xt_basis.Graph;
import agg.xt_basis.Type;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

/**
 * @author olga
 *
 */
public class ConflictsDependenciesContainerSaveLoad extends
        ConflictsDependenciesContainer {

    private EdGraGra grammar;

    protected EdGraph cpaGraph;

    public ConflictsDependenciesContainerSaveLoad() {
        super();
    }

    public ConflictsDependenciesContainerSaveLoad(
            final PairContainer conflict,
            final PairContainer dependency,
            final EdGraph graph,
            final EdGraGra gragra) {
        super(conflict, dependency, graph.getBasisGraph());
        this.grammar = gragra;
        this.cpaGraph = graph;
    }

    public EdGraGra getPairsGraGra() {
        return this.grammar;
    }

    @Override
    public EdGraph getCPAGraph() {
        return cpaGraph;
    }

    @Override
    protected boolean writeLayoutGrammar(XMLHelper h) {
        if (this.grammar != null) {
            h.addObject("GraphTransformationSystem", this.grammar, false);
            return true;
        }
        return false;
    }

    protected void readLayoutGrammar(XMLHelper h) {
        this.grammar = new EdGraGra(this.pairsGrammar);
//		this.grammar.getBasisGraGra().prepareRuleInfo();
        h.enrichObject(this.grammar);
    }

    protected void readCPAGraph(XMLHelper h) {
        this.cpaBasisGraph = new Graph();
        if (h.readSubTag("ConflictDependencyGraph")) {
            if (h.readSubTag("Types")) {
                Iterator<Element> en = h.getEnumeration("", null, true,
                        "NodeType");
                while (en.hasNext()) {
                    h.peekElement(en.next());
                    Type t = this.cpaBasisGraph.getTypeSet().createNodeType(false);
                    h.loadObject(t);
                    h.close();
                    if (t.getAdditionalRepr().equals("")) {
                        t.setAdditionalRepr("[NODE]");
                    }
                }
                en = h.getEnumeration("", null, true, "EdgeType");
                while (en.hasNext()) {
                    h.peekElement(en.next());
                    Type t = this.cpaBasisGraph.getTypeSet().createArcType(false);
                    h.loadObject(t);
                    h.close();
                    if (t.getAdditionalRepr().equals("")) {
                        t.setAdditionalRepr("[EDGE]");
                    }
                }
                h.close();
            }
            h.getObject("", this.cpaBasisGraph, true);
            // improve old CPA Graph name
            String gn = this.cpaBasisGraph.getName();
            if (gn.contains("ofRules")) {
                this.cpaBasisGraph.setName("CPA_RuleGraph:Conflicts_(red)-Dependencies_(blue)");
            }
            this.cpaGraph = new EdGraph(this.cpaBasisGraph);
            this.cpaGraph.setCPAgraph(true);
            h.enrichObject(this.cpaGraph);
            h.close();
            List<EdType> cpaEdgeTypes = this.cpaGraph.getTypeSet().getArcTypes();
            for (int i = 0; i < cpaEdgeTypes.size(); i++) {
                EdType t = cpaEdgeTypes.get(i);
                if (t.getBasisType().getName().equals("c")) {
                    t.setColor(Color.RED);
                } else if (t.getBasisType().getName().equals("d")) {
                    t.setColor(Color.BLUE);
                }
                t.setAdditionalReprOfBasisType();
            }
        }
    }

    @Override
    protected void writeCPAGraph(XMLHelper h) {
        if (this.cpaBasisGraph != null) {
            h.openSubTag("ConflictDependencyGraph");
            h.openSubTag("Types");
            h.addEnumeration("", this.cpaBasisGraph.getTypeSet().getTypeWalker(), true);
            h.close();
            h.addObject("", this.cpaBasisGraph, true);
            h.close();
        }
        if (this.cpaGraph != null) {
            //this.cpaGraph.XwriteObject(h);
            h.addObject("Graph", this.cpaGraph, false);
        }
    }
}
