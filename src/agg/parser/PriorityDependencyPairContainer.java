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
import agg.xt_basis.BaseFactory;
import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xt_basis.GraphObject;
import agg.xt_basis.OrdinaryMorphism;
import agg.xt_basis.Rule;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

/**
 * This class provides a container for critical pairs. The critical pairs uses
 * the exclude algorithm. Further on the used graph grammar is layered.
 *
 * @author $Author: olga $
 * @version $Id: PriorityDependencyPairContainer.java,v 1.1 2006/12/13 13:33:00
 * enrico Exp $
 */
public class PriorityDependencyPairContainer extends DependencyPairContainer {

    /**
     * Creates a new container for critical pairs.
     *
     * @param gragra The graph grammar.
     */
    public PriorityDependencyPairContainer(GraGra gragra) {
        super(gragra);
    }

    /**
     * Computes if the first rule exclude the second rule. The result is added
     * to the container.
     *
     * @param r1 The first rule.
     * @param r2 The second rule.
     */
    protected synchronized void computeCritical(Rule r1, Rule r2) {
        System.out
                .println("PriorityDependencyPairContainer.computeCritical(r1, r2): "
                        + r1.getName()
                        + "   "
                        + r2.getName()
                        + "   "
                        + this.getEntry(r1, r2).state);
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
        if (r1.getPriority() != r2.getPriority()) {
            this.getEntry(r1, r2).state = Entry.NOT_RELATED;
            addEntry(r1, r2, false, null);
            addQuadruple(this.excludeContainer, r1, r2, false, null);
            addQuadruple(this.conflictFreeContainer, r1, r2, false, null);
            firePairEvent(new CriticalPairEvent(this, r1, r2, ""));
            return;
        }
        if ((this.getEntry(r1, r2).state == Entry.SCHEDULED_FOR_COMPUTING)
                || (this.getEntry(r1, r2).state == Entry.NOT_SET)) {
            getEntry(r1, r2).setState(Entry.COMPUTING_IS_RUNNING);
            firePairEvent(new CriticalPairEvent(this, r1, r2,
                    "Computing critical rule pair  [  " + r1.getName()
                    + "  ,  " + r2.getName() + "  ]"));
            if (!this.complete) {
                PrioritySimpleDependencyPair pair = new PrioritySimpleDependencyPair();
                this.excludePair = pair;
            } else {
                PriorityDependencyPair pair = new PriorityDependencyPair();
                this.excludePair = pair;
            }
            setOptionsOfExcludePair();
            List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> overlapping = null;
            // if(stop) {
            // this.getEntry(r1, r2).setState(Entry.NOT_SET);
            // return;
            // }
            try {
                if (this.excludePair != null) {
                    List<Pair<Pair<OrdinaryMorphism, OrdinaryMorphism>, Pair<OrdinaryMorphism, OrdinaryMorphism>>> obj = this.excludePair.isCritical(CriticalPair.EXCLUDE, r1, r2);
//					if (obj instanceof List)
//						overlapping = (List) obj;
                    overlapping = obj;
                    // if(stop){
                    // this.getEntry(r1, r2).state = Entry.NOT_SET;
                    // if(overlapping != null && !overlapping.isEmpty()){
                    // overlapping.clear();
                    // overlapping = null;
                    // }
                    // }
                }
            } catch (InvalidAlgorithmException iae) {
//				System.out.println(iae.getLocalizedMessage());
//				if (iae.getKindOfInvalidAlgorithm() == CriticalPairEvent.NOT_COMPUTABLE) {
//					Entry entry = this.getEntry(r1, r2);
//					entry.state = Entry.NOT_COMPUTABLE;
//				}
            }
            if (this.excludePair != null) {
                this.excludePair.dispose();
            }
            this.excludePair = null;
            boolean critic = (overlapping != null);
            // if(stop && this.getEntry(r1, r2).state == Entry.NOT_SET){
            // firePairEvent(new CriticalPairEvent(this, r1, r2, "Computing
            // critical stopped."));
            // return;
            // }
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
    }
}
