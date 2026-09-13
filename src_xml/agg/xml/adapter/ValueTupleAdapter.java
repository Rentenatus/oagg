/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.attribute.impl.ValueTuple;

/**
 * Adapter for ValueTuple objects that implements the new XMLSerializable interface.
 */
public class ValueTupleAdapter extends DomainObjectAdapter<ValueTuple> implements agg.xml.core.XMLSerializable {

    public ValueTupleAdapter(ValueTuple valueTuple) {
        super(valueTuple);
    }

    public ValueTuple getValueTuple() {
        return getDomainObject();
    }
}
