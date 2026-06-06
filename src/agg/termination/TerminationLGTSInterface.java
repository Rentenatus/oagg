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
package agg.termination;

import agg.util.Pair;
import agg.xt_basis.GraGra;
import agg.xt_basis.GraphObject;
import agg.xt_basis.Rule;
import agg.xt_basis.Type;
import de.jare.ndimcol.primint.ArrayMovieInt;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class implements termination conditions of Layered Graph Grammar.
 *
 * @author $Author: olga $
 * @version $Id: TerminationLGTSInterface.java,v 1.4 2009/02/04 10:11:29 olga
 * Exp $
 */
public interface TerminationLGTSInterface {

    /**
     * Initialize a termination layers of the grammar. Initially the termination
     * conditions are invalid.
     *
     * @param gra The graph grammar.
     */
    public void setGrammar(GraGra gra);

    public void resetGrammar();

    public GraGra getGrammar();

    public List<Rule> getListOfEnabledRules();

    public boolean hasGrammarChanged();

    public List<Rule> getListOfRules();

    public Map<Integer, HashSet<Rule>> getInvertedRuleLayer();

    public ArrayMovieInt getOrderedRuleLayer();

    public Map<Integer, HashSet<Object>> getInvertedTypeDeletionLayer();

    public Map<Integer, HashSet<Object>> getInvertedTypeCreationLayer();

    public Map<Integer, List<Type>> getDeletionType();

    public Map<Integer, List<GraphObject>> getDeletionTypeObject();

    public Map<Integer, Pair<Boolean, List<Rule>>> getResultTypeDeletion();

    public Map<Integer, Pair<Boolean, List<Rule>>> getResultDeletion();

    public Map<Integer, Pair<Boolean, List<Rule>>> getResultNondeletion();

    public void resetLayer();

    public void initRuleLayer(Map<?, Integer> init);

    public void initAll(boolean generate);

    public List<Object> getCreatedTypesOnDeletionLayer(Integer layer);

    /**
     * Checks layer conditions .
     *
     * @return true if conditions are valid.
     */
    public boolean checkTermination();

    /**
     * A fast check on validity.
     *
     * @return true if the layer function is valid.
     */
    public boolean isValid();

    /**
     * Returns an error message if the layer function is not valid.
     *
     * @return The error message.
     */
    public String getErrorMessage();

    /**
     * Returns the rule layer of the layer function.
     *
     * @return The rule layer.
     */
    public Map<Rule, Integer> getRuleLayer();

    public int getRuleLayer(Rule r);

    /**
     * Returns the creation layer of the layer function.
     *
     * @return The creation layer.
     */
    public Map<Object, Integer> getCreationLayer();

    public int getCreationLayer(Type t);

    public int getCreationLayer(GraphObject t);

    /**
     * Returns the deletion layer of the layer function.
     *
     * @return The deletion layer.
     */
    public Map<Object, Integer> getDeletionLayer();

    public int getDeletionLayer(Type t);

    public int getDeletionLayer(GraphObject t);

    /**
     * Returns the smallest layer of the rule layer.
     *
     * @return The smallest layer.
     */
    public Integer getStartLayer();

    /**
     * Inverts a layer function so that the layer is the key and the value is a
     * set.
     *
     * @param layer The layer function will be inverted.
     * @return The inverted layer function.
     */
    public Map<Integer, HashSet<Rule>> invertLayer(
            Map<Rule, Integer> layer);

    public void saveRuleLayer();

    public void setGenerateRuleLayer(boolean b);

    public void showLayer();

    /**
     * Returns the layer function in a human readable way.
     *
     * @return The text.
     */
    public String toString();
}
