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
import agg.parser.ConflictsDependenciesContainer;
import agg.parser.PairContainer;

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

    /**
     * Installs the editor grammar restored by an external reader (the DOM
     * load path of the agg-xml UI module mirrors readLayoutGrammar).
     */
    public void setPairsGraGra(EdGraGra gragra) {
        this.grammar = gragra;
    }

    /**
     * Installs the CPA editor graph restored by an external reader (the DOM
     * load path of the agg-xml UI module mirrors readCPAGraph).
     */
    public void setCpaGraph(EdGraph graph) {
        this.cpaGraph = graph;
    }

    @Override
    public EdGraph getCPAGraph() {
        return cpaGraph;
    }

}
