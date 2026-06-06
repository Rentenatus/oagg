/**
 **
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 * *****************************************************************************
 */
package agg.cons;

import de.jare.ndimcol.primint.ArrayMovieInt;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Vector;
//import com.objectspace.jgl.HashSet;

/**
 * Constraint layer is a set of layer of a given layered graph grammar. The set
 * is backed by a hash table.
 *
 * @author $Author: olga $
 * @version $ID
 */
public class ConstraintLayer {

    private Map<Object, Object> constraintLayer;
    private Enumeration<Formula> constraints;
    private Vector<Formula> constraintsVec;

    /**
     * Creates a new set of constraint layers for a given layered graph grammar.
     *
     * @param constraints The constraints of a graph grammar.
     */
    public ConstraintLayer(Enumeration<Formula> constraints) {
        this.constraints = constraints;
        this.constraintsVec = new Vector<Formula>(0);
        while (this.constraints.hasMoreElements()) {
            this.constraintsVec.addElement(constraints.nextElement());
        }
        initConstraintLayer();
    }

    public ConstraintLayer(List<Formula> constraints) {
        this.constraintsVec = new Vector<Formula>(0);
        for (int i = 0; i < constraints.size(); i++) {
            this.constraintsVec.add(constraints.get(i));
        }
        this.constraints = this.constraintsVec.elements();
        initConstraintLayer();
    }

    public ConstraintLayer(Vector<Formula> constraints) {
        this.constraintsVec = constraints;
        this.constraints = this.constraintsVec.elements();
        initConstraintLayer();
    }

    /**
     * Sets the layer of the specified constraint
     */
    public void addLayer(Formula constraint, int layer) {
        constraint.addLayer(layer);
        this.constraintLayer.put(constraint, constraint.getLayer());
        // System.out.println("constraint layer: "
        // +((Integer)this.constraintLayer.get(rule)).toString());
    }

    private void initConstraintLayer() {
        this.constraintLayer = new HashMap<Object, Object>();
        for (int i = 0; i < this.constraintsVec.size(); i++) {
            Object constraint = this.constraintsVec.elementAt(i);
            if (constraint instanceof Formula) {
                ArrayMovieInt layer = ((Formula) constraint).getLayer();
                if (layer != null) {
                    this.constraintLayer.put(constraint, layer);
                }
            } else if (constraint instanceof String) {
                this.constraintLayer.put(constraint, Integer.valueOf(0));
            }
        }
    }

    /**
     * Returns the constraint (formula) layer. A constraint is a key, a layer is
     * a value.
     */
    public Map<Object, Object> getConstraintLayer() {
        return this.constraintLayer;
    }

    /**
     * Returns the smallest layer of the formula layer.
     *
     * @return The smallest layer.
     */
    public Integer getStartLayer() {
        int startLayer = Integer.MAX_VALUE;
        Integer result = null;
        for (Object key : getConstraintLayer().keySet()) {
            Vector<?> layer = (Vector<?>) getConstraintLayer().get(key);
            if (layer != null) {
                if (layer.isEmpty()) {
                    startLayer = 0;
                    result = Integer.valueOf(0);
                } else {
                    Integer l = (Integer) layer.get(0);
                    if (l.intValue() < startLayer) {
                        startLayer = l.intValue();
                        result = l;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Inverts and returns constraint layers so that a layer is a key and a set
     * is a value.
     */
    public Map<Integer, HashSet<Object>> invertLayer() {
        Map<Integer, HashSet<Object>> inverted = new HashMap<Integer, HashSet<Object>>();
        for (Object key : this.constraintLayer.keySet()) {
            Vector<?> layer = (Vector<?>) this.constraintLayer.get(key);
            if (layer != null) {
                Integer l = Integer.valueOf(0);
                if (!layer.isEmpty()) {
                    l = (Integer) layer.get(0);
                }
                HashSet<Object> invertedValue = inverted.get(l);
                if (invertedValue == null) {
                    invertedValue = new HashSet<Object>();
                    invertedValue.add(key);
                    inverted.put(l, invertedValue);
                } else {
                    invertedValue.add(key);
                }
            }
        }
        return inverted;
    }

    /**
     * Returns the constraint layer in a human readable way.
     */
    public String toString() {
        String resultString = "Formula:\t\tLayer:\n";
        for (Object key : this.constraintLayer.keySet()) {
            resultString += ((Formula) key).getName() + "\t\t";
            Vector<?> valueVec = (Vector<?>) this.constraintLayer.get(key);
            for (int i = 0; i < valueVec.size(); i++) {
                Integer value = (Integer) valueVec.get(i);
                resultString += value.toString() + "  ";
            }
            resultString += "\n";
        }
        return resultString;
    }
}
