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
package agg.termination;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import agg.xt_basis.Type;

/**
 * Type layer is a set of type layer of a given layered graph grammar. The set
 * is backed by a hash table.
 *
 * @author $Author: olga $
 * @version $ID
 */
public class TypeLayer {

    private Map<Type, Integer> typeLayer;
    private Map<Type, Integer> types;

    /**
     * Creates a new set of type layers for a given layered graph grammar.
     *
     * @param types The types of a graph grammar.
     */
    public TypeLayer(Map<Type, Integer> types) {
        this.types = types;
        initTypeLayer();
    }

    /**
     * Sets the layer of the specified type
     */
    public void setLayer(Type type, int layer) {
        this.typeLayer.put(type, Integer.valueOf(layer));
        // System.out.println("type layer: "+((Integer)
        // this.typeLayer.get(type)).toString());
    }

    private void initTypeLayer() {
        this.typeLayer = new HashMap<Type, Integer>();
        for (Type t : this.types.keySet()) { 
            this.typeLayer.put(t, this.types.get(t));
            // System.out.println("type layer: "+((Integer)
            // this.typeLayer.get(t)).toString());
        }
    }

    /**
     * Returns the type layer. A type is a key, a layer is a value.
     *
     * @return The type layer.
     */
    public Map<Type, Integer> getTypeLayer() {
        return this.typeLayer;
    }

    /**
     * Returns the smallest layer of the type layer.
     *
     * @return The smallest layer.
     */
    public Integer getStartLayer() {
        int startLayer = Integer.MAX_VALUE;
        Integer result = null;
        for (Type key : getTypeLayer().keySet()) {
            Integer layer = getTypeLayer().get(key);
            if (layer.intValue() < startLayer) {
                startLayer = layer.intValue();
                result = layer;
            }
        }
        return result;
    }

    /**
     * Inverts a type layer so that the layer is the key and the value is a set.
     *
     * @return The inverted layer function.
     */
    public Map<Integer, HashSet<Object>> invertLayer() {
        Map<Integer, HashSet<Object>> inverted = new HashMap<Integer, HashSet<Object>>();
        for (Type key : this.typeLayer.keySet()) {
            // System.out.println("TypeLayer:: "+key);
            Integer value = this.typeLayer.get(key);
            // System.out.println("TypeLayer:: "+value);
            HashSet<Object> invertedValue = inverted.get(value);
            if (invertedValue == null) {
                invertedValue = new HashSet<Object>();
                invertedValue.add(key);
                inverted.put(value, invertedValue);
            } else {
                invertedValue.add(key);
            }
        }
        return inverted;
    }

    /**
     * Returns the type layer in a human readable way.
     *
     * @return The text.
     */
    public String toString() {
        String resultString = "Type:\t\tLayer:\n";
        for (Type key : this.typeLayer.keySet()) {
            Integer value = this.typeLayer.get(key);
            resultString += key.getStringRepr() + "\t\t" + value.toString()
                    + "\n";
        }
        return resultString;
    }
}
