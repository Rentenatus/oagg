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
package agg.parser;

import agg.util.Pair;
import agg.util.XMLHelper;
import agg.xt_basis.BaseFactory;
import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xt_basis.GraphObject;
import agg.xt_basis.OrdinaryMorphism;
import agg.xt_basis.Rule;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.w3c.dom.Element;

/**
 * This class provides a container for critical pairs. The critical pairs uses
 * the exclude algorithm. Further on the used graph grammar is layered.
 *
 * @author $Author: olga $
 * @version $Id: LayeredDependencyPairContainer.java,v 1.7 2006/12/13 13:33:00
 * enrico Exp $
 */
public class LayeredDependencyPairContainer extends DependencyPairContainer
        implements LayeredPairContainer {

    @SuppressWarnings("deprecation")
    private LayerFunction layerFunc;
    private int layer = -1; // compute all Layer

    /**
     * Creates a container for critical pairs for a layered graph grammar.
     *
     * @param gragra The graph grammar.
     * @param layerFunc The layer function.
     * @deprecated
     */
    public LayeredDependencyPairContainer(GraGra gragra, LayerFunction layerFunc) {
        super(gragra);
        // this.layerFunc = layerFunc;
    }

    /**
     * Creates a new container for critical pairs. An invalid layer function is
     * used. It is necessary to set a valid layer function.
     *
     * @param gragra The graph grammar.
     */
    public LayeredDependencyPairContainer(GraGra gragra) {
        super(gragra);
    }
    protected LayeredDependencyPair layeredPair;
    protected LayeredSimpleDependencyPair layeredSimplePair;

    /**
     * Computes if the first rule exclude the second rule. The result is added
     * to the container.
     *
     * @param r1 The first rule.
     * @param r2 The second rule.
     */
    protected synchronized void computeCritical(Rule r1, Rule r2) {
        Report.trace("LayeredDependencyPairContainer: starte computeCritical",
                2);
        // System.out.println("LayeredDependencyPairContainer.computeCritical(r1,
        // r2): "+r1.getName()+" "+r2.getName()+" "+this.getEntry(r1,
        // r2).state);
        // mark Entry
        if (!r1.isEnabled() || !r2.isEnabled()) { // test disabled rule
            this.getEntry(r1, r2).state = Entry.DISABLED;
            addEntry(r1, r2, false, null);
            addQuadruple(this.excludeContainer, r1, r2, false, null);
            addQuadruple(this.conflictFreeContainer, r1, r2, false, null);
            firePairEvent(new CriticalPairEvent(this, r1, r2, "<"
                    + r1.getName() + ">  and  <" + r2.getName()
                    + ">  should not be computed."));
            return;
        }
        if (r1.getLayer() != r2.getLayer()) {// test rule layer
            this.getEntry(r1, r2).state = Entry.NOT_RELATED;
            addEntry(r1, r2, false, null);
            addQuadruple(this.excludeContainer, r1, r2, false, null);
            addQuadruple(this.conflictFreeContainer, r1, r2, false, null);
            firePairEvent(new CriticalPairEvent(this, r1, r2, ""));
            return;
        }
        if ((this.layer > -1) && (r1.getLayer() != this.layer)) {
            return;
        }
        if ((this.getEntry(r1, r2).state == Entry.SCHEDULED_FOR_COMPUTING)
                || (this.getEntry(r1, r2).state == Entry.NOT_SET)) {
            getEntry(r1, r2).setState(Entry.COMPUTING_IS_RUNNING);
            firePairEvent(new CriticalPairEvent(this, r1, r2,
                    "Computing critical rule pair  [  " + r1.getName()
                    + "  ,  " + r2.getName() + "  ]"));
            if (!this.complete) {
                this.layeredPair = null;
                this.layeredSimplePair = new LayeredSimpleDependencyPair();
                this.excludePair = this.layeredSimplePair;
            } else {
                this.layeredSimplePair = null;
                this.layeredPair = new LayeredDependencyPair();
                this.excludePair = this.layeredPair;
            }
            setOptionsOfExcludePair();
            List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> overlapping = null;
            try {
                if (this.layeredPair != null) {
                    overlapping = this.layeredPair.isCritical(CriticalPair.EXCLUDE, r1, r2);
                } else if (this.layeredSimplePair != null) {
                    overlapping = this.layeredSimplePair.isCritical(CriticalPair.EXCLUDE, r1, r2);
                }
            } catch (InvalidAlgorithmException iae) {
                System.out.println(iae.getLocalizedMessage());
            }
            if (this.excludePair != null) {
                this.excludePair.dispose();
            }
            this.excludePair = null;
            this.layeredPair = null;
            this.layeredSimplePair = null;
//			System.gc();
            boolean critic = (overlapping != null);
            // new container
            addEntry(r1, r2, critic, overlapping);
            /*
			 * Wenn overlapping Elemente enthaelt sind r1/r2 kritisch critic
			 * wird daher true. Alle wichtigen Informationen werden eingetragen.
			 * Enthaelt r1/r2 keine Elementen, so wird critic auf false gesetzt.
			 * Wenn excludeContainer nach r1/r2 gefragt wird, liefert die
			 * Antwort auch false. overlapping kann daher null sein.
             */
 /*
			 * Achtung, wenn r1 r2 nicht kritisch ist gibt es keine
			 * Ueberlappungen
             */
            addQuadruple(this.excludeContainer, r1, r2, critic, overlapping);
            /*
			 * conflictfree braucht keine ueberlappungsgraphen daher ist das
			 * letzte Argument null
             */
            addQuadruple(this.conflictFreeContainer, r1, r2, !critic, null);
            if (overlapping != null) {
                firePairEvent(new CriticalPairEvent(this, r1, r2, "<"
                        + r1.getName() + ">  and  <" + r2.getName()
                        + ">  have critical pairs"));
            } else {
                firePairEvent(new CriticalPairEvent(this, r1, r2, "<"
                        + r1.getName() + ">  and  <" + r2.getName()
                        + ">  have no critical pairs"));
            }
        }
        Report.trace("LayeredDependencyPairContainer: beende computeCritical",
                -2);
    }

    protected void fillContainers() {
        super.fillContainers();
        if (this.layer > -1) {
            this.isComputed = false;
        }
    }

    /**
     * Sets a layer function to layer a certain object.
     *
     * @param lf A specific layer function.
     * @deprecated not more used
     */
    public void setLayer(LayerFunction lf) {
        this.layerFunc = lf;
    }

    /**
     * Returns a layer function from a certain object.
     *
     * @return A specific layer function.
     * @deprecated not more used
     */
    public LayerFunction getLayer() {
        return this.layerFunc;
    }

    /**
     * Sets a layer to compute. If l = -1 then all layers should be computed.
     *
     * @param l
     */
    public void setLayer(int l) {
        this.layer = l;
        // System.out.println("LayeredExcludePairContainer::: compute layer:
        // "+layer);
    }
}
