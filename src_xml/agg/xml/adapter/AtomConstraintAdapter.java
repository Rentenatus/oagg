/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.cons.AtomConstraint;

/**
 * Adapter for AtomConstraint objects that implements the new XMLSerializable interface.
 */
public class AtomConstraintAdapter extends DomainObjectAdapter<AtomConstraint> {

    public AtomConstraintAdapter(AtomConstraint atomConstraint) {
        super(atomConstraint);
    }

    public AtomConstraint getAtomConstraint() {
        return getDomainObject();
    }
}
