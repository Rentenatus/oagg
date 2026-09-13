/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.adapter;

import agg.attribute.impl.VarTuple;
import agg.attribute.impl.CondTuple;
import agg.attribute.impl.DeclTuple;
import agg.attribute.impl.ValueMember;
import agg.attribute.impl.CondMember;
import agg.attribute.impl.DeclMember;

/**
 * Adapters for attribute implementation classes.
 * These adapters wrap attribute tuple and member objects and delegate
 * serialization to their XMLObject methods.
 */
public final class AttributeAdapters {

    private AttributeAdapters() {
    }

    public static final class VarTupleAdapter extends DomainObjectAdapter<VarTuple> implements agg.xml.core.XMLSerializable {
        public VarTupleAdapter(VarTuple varTuple) { super(varTuple); }
        public VarTuple getVarTuple() { return getDomainObject(); }
    }

    public static final class CondTupleAdapter extends DomainObjectAdapter<CondTuple> implements agg.xml.core.XMLSerializable {
        public CondTupleAdapter(CondTuple condTuple) { super(condTuple); }
        public CondTuple getCondTuple() { return getDomainObject(); }
    }

    public static final class DeclTupleAdapter extends DomainObjectAdapter<DeclTuple> implements agg.xml.core.XMLSerializable {
        public DeclTupleAdapter(DeclTuple declTuple) { super(declTuple); }
        public DeclTuple getDeclTuple() { return getDomainObject(); }
    }

    public static final class ValueMemberAdapter extends DomainObjectAdapter<ValueMember> implements agg.xml.core.XMLSerializable {
        public ValueMemberAdapter(ValueMember valueMember) { super(valueMember); }
        public ValueMember getValueMember() { return getDomainObject(); }
    }

    public static final class CondMemberAdapter extends DomainObjectAdapter<CondMember> implements agg.xml.core.XMLSerializable {
        public CondMemberAdapter(CondMember condMember) { super(condMember); }
        public CondMember getCondMember() { return getDomainObject(); }
    }

    public static final class DeclMemberAdapter extends DomainObjectAdapter<DeclMember> implements agg.xml.core.XMLSerializable {
        public DeclMemberAdapter(DeclMember declMember) { super(declMember); }
        public DeclMember getDeclMember() { return getDomainObject(); }
    }
}
