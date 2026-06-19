/**
 **
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 * *****************************************************************************
 */
package agg.xt_basis.colim;
//-------------------------------------------------------------------
//                 dynamic object array          
//-------------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class COLIM_VECTOR {

    final List<Object> v;

    public COLIM_VECTOR() {
        v = new ArrayList<Object>();
    }

    public COLIM_VECTOR(int size) {
        v = new ArrayList<Object>(size);
    }

    public COLIM_VECTOR(COLIM_VECTOR buf) {
        v = new ArrayList<Object>(buf.v);
    }

    public void push_back(Object obj) {
        v.add(obj);
    }

    public Object item(int index) {
        return v.get(index);
    }

    public void put(Object obj, int index) {
        v.add(index, obj);
    }

    public int indexOf(Object obj) {
        return v.indexOf(obj);
    }

    @SuppressWarnings("rawtypes")
    public Enumeration elements() {
        return Collections.enumeration(v);
    }

    public int size() {
        return v.size();
    }

    public void setSize(int size) {
        // ArrayList does not support setSize directly
        // This method is kept for compatibility but does nothing
    }

    public void ensureCapacity(int size) {
        // ArrayList handles capacity automatically, no action needed
    }

    public void clear() {
        v.clear();
    }

    public void trimToSize() {
        // ArrayList does not require trimToSize, no action needed
    }

    public String toString() {
        return v.toString();
    }
}
