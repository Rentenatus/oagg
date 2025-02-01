/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package test_agg.rulesystem.param;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Janusch Rentenatus
 * @param <T> Die Klasse für den Wert.
 */
public abstract class Parameter<T> {

    private String paramName;
    private T value;
    private List<WeakReference<ParamChangeListener>> listeners;

    public Parameter(String paramName) {
        this.paramName = paramName;
        if (paramName == null) {
            throw new NullPointerException("paramName is null.");
        }
    }

    public Parameter(String paramName, T value) {
        this.paramName = paramName;
        this.value = value;
        this.listeners = new ArrayList<>();
        if (paramName == null) {
            throw new NullPointerException("paramName is null.");
        }
        if (value == null) {
            throw new NullPointerException("value is null.");
        }
    }

    public boolean isCosFilter() {
        return false;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * Der Name der Klasse, so wie RB Teil es versteht (z.B. AGG, so wird
     * "Integer" mit "int" angegeben).
     *
     * @return Name der Klasse.
     */
    public abstract Object getParamTypeName();

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        boolean warwas = this.value != value;
        this.value = value;
        if (warwas) {
            sendValueChanged();
        }
    }

    public void addChangeListener(ParamChangeListener listener) {
        listeners.add(new WeakReference(listener));
    }

    public void removeChangeListener(ParamChangeListener listener) {
        for (Iterator<WeakReference<ParamChangeListener>> it = listeners.iterator(); it.hasNext();) {
            ParamChangeListener next = it.next().get();
            if (next == null || next == listener) {
                it.remove();
            }
        }
    }

    private void sendValueChanged() {
        for (Iterator<WeakReference<ParamChangeListener>> it = listeners.iterator(); it.hasNext();) {
            ParamChangeListener next = it.next().get();
            if (next == null) {
                it.remove();
            } else {
                next.paramChanged(this);
            }
        }
    }

    @Override
    public boolean equals(Object o2) {
        if (!getClass().isInstance(o2)) {
            return false;
        }
        Parameter<T> p2 = (Parameter<T>) o2;
        if ((getValue() == null) ^ (p2.getValue() == null)) {
            return false;
        }
        return getParamName().equals(p2.getParamName()) && getValue().equals(p2.getValue());
    }

    @Override
    public int hashCode() {
        int hash = 23333;
        hash = hash + Objects.hashCode(this.paramName);
        hash = 47 * hash + Objects.hashCode(this.value);
        return hash;
    }

}
