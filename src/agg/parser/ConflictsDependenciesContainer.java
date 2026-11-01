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
package agg.parser;

import agg.parser.ExcludePairContainer.Entry;
import agg.util.Pair;
import agg.xt_basis.Arc;
import agg.xt_basis.BadMappingException;
import agg.xt_basis.BaseFactory;
import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xt_basis.GraphObject;
import agg.xt_basis.Node;
import agg.xt_basis.OrdinaryMorphism;
import agg.xt_basis.Rule;
import agg.xt_basis.Type;
import agg.xt_basis.TypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ConflictsDependenciesContainer {

    protected ExcludePairContainer epc;
    protected DependencyPairContainer dpc;
    protected LayeredExcludePairContainer lepc;
    protected LayeredDependencyPairContainer ldpc;
    protected boolean layered;
    protected PriorityExcludePairContainer pepc;
    protected PriorityDependencyPairContainer pdpc;
    protected boolean priority;
    protected GraGra pairsGrammar;
    protected Graph cpaBasisGraph;
    protected int count;
    protected final List<Pair<String, String>> cpaOptions = new Vector<Pair<String, String>>();

    public ConflictsDependenciesContainer() {
        this.cpaBasisGraph = null;
    }

    public ConflictsDependenciesContainer(
            final PairContainer conflict,
            final PairContainer dependency) {
        this.layered = false;
        this.priority = false;
        this.count = 0;
        if (dependency instanceof LayeredDependencyPairContainer) {
            this.ldpc = (LayeredDependencyPairContainer) dependency;
            this.count++;
            this.layered = true;
            this.pairsGrammar = this.ldpc.getGrammar();
        } else if (dependency instanceof PriorityDependencyPairContainer) {
            this.pdpc = (PriorityDependencyPairContainer) dependency;
            this.count++;
            this.priority = true;
            this.pairsGrammar = this.pdpc.getGrammar();
        } else if (dependency instanceof DependencyPairContainer) {
            this.dpc = (DependencyPairContainer) dependency;
            this.count++;
            this.pairsGrammar = this.dpc.getGrammar();
        }
        this.layered = false;
        this.priority = false;
        if (conflict instanceof LayeredExcludePairContainer) {
            this.lepc = (LayeredExcludePairContainer) conflict;
            this.count++;
            this.layered = true;
            this.pairsGrammar = this.lepc.getGrammar();
        } else if (conflict instanceof PriorityExcludePairContainer) {
            this.pepc = (PriorityExcludePairContainer) conflict;
            this.count++;
            this.priority = true;
            this.pairsGrammar = this.pepc.getGrammar();
        } else if (conflict instanceof ExcludePairContainer) {
            this.epc = (ExcludePairContainer) conflict;
            this.count++;
            this.pairsGrammar = this.epc.getGrammar();
        }
    }

    public ConflictsDependenciesContainer(
            final PairContainer conflict,
            final PairContainer dependency,
            final Graph conflictDependencyGraph) {
        this(conflict, dependency);
        this.cpaBasisGraph = conflictDependencyGraph;
    }

    public ExcludePairContainer getExcludePairContainer() {
        return this.epc;
    }

    public DependencyPairContainer getDependencyPairContainer() {
        return this.dpc;
    }

    public LayeredExcludePairContainer getLayeredExcludePairContainer() {
        return this.lepc;
    }

    public LayeredDependencyPairContainer getLayeredDependencyPairContainer() {
        return this.ldpc;
    }

    public PriorityExcludePairContainer getPriorityExcludePairContainer() {
        return this.pepc;
    }

    public PriorityDependencyPairContainer getPriorityDependencyPairContainer() {
        return this.pdpc;
    }

    public Graph getCPABasisGraph() {
        return this.cpaBasisGraph;
    }

    public Object getCPAGraph() {
        return null;
    }

    public boolean isPriority() {
        return this.priority;
    }

    public boolean isLayered() {
        return this.layered;
    }

    public GraGra getGrammar() {
        return this.pairsGrammar;
    }

    public int getContainerCount() {
        return this.count;
    }

    public List<Pair<String, String>> getLoadedCPAOptions() {
        return this.cpaOptions;
    }

    /**
     * Adds a CPA option loaded by an external reader (the DOM load path of
     * the agg-xml module mirrors the legacy readCPAoptions behaviour).
     */
    public void addLoadedCPAOption(final String name, final String value) {
        this.cpaOptions.add(new Pair<String, String>(name, value));
    }

    /**
     * Installs the CPA basis graph restored by an external reader (the DOM
     * load path of the agg-xml module). The graph is only set when the file
     * actually contains a ConflictDependencyGraph section, unlike the
     * legacy reader, which always creates an empty graph.
     */
    public void setCPABasisGraph(final Graph cpaBasisGraph) {
        this.cpaBasisGraph = cpaBasisGraph;
    }

    /**
     * Installs the containers restored by an external reader (the DOM load
     * path of the agg-xml module). Mirrors the container creation of the
     * legacy XreadObject, which writes the private container fields
     * directly.
     */
    public void setLoadedContainers(final ExcludePairContainer epc,
            final DependencyPairContainer dpc, final boolean layered,
            final boolean priority) {
        this.epc = epc;
        this.dpc = dpc;
        this.layered = layered;
        this.priority = priority;
        this.count = 0;
        if (epc != null) {
            this.count++;
            this.pairsGrammar = epc.getGrammar();
        }
        if (dpc != null) {
            this.count++;
            if (this.pairsGrammar == null) {
                this.pairsGrammar = dpc.getGrammar();
            }
        }
    }
    /**
     * Reads the contents of a xml file to restore this object.
     *
     * @param h A helper object for loading.
     */
    @SuppressWarnings("unused")
    private OrdinaryMorphism getPAC(
            final Rule r,
            final Graph pacGraph) {
        final List<OrdinaryMorphism> pacs = r.getPACsList();
        for (int l = 0; l < pacs.size(); l++) {
            final OrdinaryMorphism pac = pacs.get(l);
            if (pac.getTarget() == pacGraph) {
                return pac;
            }
        }
        return null;
    }
    /*
	private OrdinaryMorphism extendLeftGraph(OrdinaryMorphism isoLeft,
			OrdinaryMorphism nac) {
		if (isoLeft == null || nac == null)
			return null;
		Graph extLeft = isoLeft.getTarget();
		OrdinaryMorphism isoNAC = BaseFactory.theFactory().createMorphism(
				nac.getTarget(), extLeft);
		Map<Node, Node> tmp = new HashMap<Node, Node>(5);
		Iterator<?> e = nac.getTarget().getNodesSet().iterator();
		while (e.hasNext()) {
			GraphObject o = (GraphObject) e.next();
			if (!nac.getInverseImage(o).hasMoreElements()) {
				try {
					Node n = extLeft.copyNode((Node) o);
					tmp.put((Node) o, n);
					isoNAC.addMapping(o, n);
				} catch (TypeException ex) {
				}
			} else {
				GraphObject oi = isoLeft.getImage(nac
						.getInverseImage(o).nextElement());
//				if (oi != null) {
					if(o.getType().isParentOf(oi.getType())) 
						isoNAC.addMapping(o, isoLeft.getImage(nac
								.getInverseImage(o).nextElement()));
					else if(o.getType().isChildOf(oi.getType())) {
//						System.out.println(o.getType().getName()+"  !=  "+oi.getType().getName());
						isoNAC.addChild2ParentMapping(o, isoLeft.getImage(nac
								.getInverseImage(o).nextElement()));
					}
//				}
			}
		}
		e = nac.getTarget().getArcsSet().iterator();
		while (e.hasNext()) {
			GraphObject o = (GraphObject) e.next();
			if (!nac.getInverseImage(o).hasMoreElements()) {
				Node src = tmp.get(((Arc) o).getSource());
				if (src == null) {
					src = (Node) isoLeft.getImage(nac.getInverseImage(
							((Arc) o).getSource()).nextElement());
				}
				Node tar = tmp.get(((Arc) o).getTarget());
				if (tar == null) {
					tar = (Node) isoLeft.getImage(nac.getInverseImage(
							((Arc) o).getTarget()).nextElement());
				}
				if (src != null && tar != null) {
					try {					
						Arc a = extLeft.copyArc((Arc) o, src, tar);
						isoNAC.addMapping(o, a);
					} catch (TypeException ex) {}
				}
			} else {
				GraphObject oi = isoLeft.getImage(nac
						.getInverseImage(o).nextElement());
				if (oi != null) {
					isoNAC.addMapping(o, isoLeft.getImage(nac
							.getInverseImage(o).nextElement()));
				}
			}
		}
		return isoNAC;
	}
	
	
	private OrdinaryMorphism extendRightGraph(OrdinaryMorphism isoRight,
			OrdinaryMorphism nac) {
		if (isoRight == null || nac == null)
			return null;
		Graph extRight = isoRight.getTarget();
		OrdinaryMorphism isoNAC = BaseFactory.theFactory().createMorphism(
				nac.getTarget(), extRight);
		Map<Node, Node> tmp = new HashMap<Node, Node>(5);
		Iterator<?> e = nac.getTarget().getNodesSet().iterator();
		while (e.hasNext()) {
			GraphObject o = (GraphObject) e.next();
			if (!nac.getInverseImage(o).hasMoreElements()) {
				try {
					Node n = extRight.copyNode((Node) o);
					tmp.put((Node) o, n);
					isoNAC.addMapping(o, n);
				} catch (TypeException ex) {}
			} else {
				GraphObject oi = isoRight.getImage(nac
						.getInverseImage(o).nextElement());
//				if (oi != null) {
					if(o.getType().isParentOf(oi.getType())) 
						isoNAC.addMapping(o, isoRight.getImage(nac
								.getInverseImage(o).nextElement()));
					else if(o.getType().isChildOf(oi.getType())) {
//						System.out.println(o.getType().getName()+"  !=  "+oi.getType().getName());
						isoNAC.addChild2ParentMapping(o, isoRight.getImage(nac
								.getInverseImage(o).nextElement()));
					}
//				}
			}
		}
		e = nac.getTarget().getArcsSet().iterator();
		while (e.hasNext()) {
			GraphObject o = (GraphObject) e.next();
			if (!nac.getInverseImage(o).hasMoreElements()) {
				Node src = tmp.get(((Arc) o).getSource());
				if (src == null) {
					src = (Node) isoRight.getImage(nac.getInverseImage(
							((Arc) o).getSource()).nextElement());
				}
				Node tar = tmp.get(((Arc) o).getTarget());
				if (tar == null) {
					tar = (Node) isoRight.getImage(nac.getInverseImage(
							((Arc) o).getTarget()).nextElement());
				}
				if (src != null && tar != null) {
					try {					
						Arc a = extRight.copyArc((Arc) o, src, tar);
						isoNAC.addMapping(o, a);
					} catch (TypeException ex) {}
				}
			} else {
				GraphObject oi = isoRight.getImage(nac
						.getInverseImage(o).nextElement());
				if (oi != null) {
					isoNAC.addMapping(o, isoRight.getImage(nac
							.getInverseImage(o).nextElement()));
				}
			}
		}
		return isoNAC;
	}
     */
}
