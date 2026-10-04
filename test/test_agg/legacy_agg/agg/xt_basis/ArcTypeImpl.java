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
package agg.xt_basis;

import agg.attribute.AttrType;
import agg.attribute.impl.DeclMember;
import agg.attribute.impl.DeclTuple;
import agg.util.XMLHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the Type interface for edge graph objects. This class
 * represents the type information for arcs/edges in graphs.
 *
 * @see Type
 * @see TypeImpl
 * @see NodeTypeImpl
 */
public class ArcTypeImpl implements Type {

    String comment = "";
    /**
     * the name of the type
     */
    String itsStringRepr;
    /**
     * the attributes of the type
     */
    AttrType itsAttrType;
    /**
     * an additional String for special informations. It will be saved together
     * with {@link #itsStringRepr} as the name and can contain any information
     * as string. It is used for example for the layout information of
     * {@link agg.editor.EdType}.
     */
    String additionalRepr;
    /**
     * a map to a list of types. This map will only created for edge types. If
     * there is an edge between sourceType and targetType
     * edgeTypeGraphObjects.get(sourceType).contains(targetType) will return
     * true.
     */
    HashMap<Type, HashMap<Type, TypeGraphArc>> edgeTypeGraphObjects;
    /**
     * this value will be true, if a graph object inside of a type graph was
     * defined.
     */
    boolean typeGraphObjectDefined;
    String keyStr = null;

    /**
     * Creates a new arc type with default settings. This creates a
     * non-attributable arc type with empty name and default visual
     * representation.
     */
    protected ArcTypeImpl() {
        this.itsAttrType = null;
        this.itsStringRepr = "";
        this.additionalRepr = ":SOLID_LINE:java.awt.Color[r=0,g=0,b=0]::[EDGE]:";
        this.resetKey();
    }

    /**
     * Creates a new arc type with the given name. This creates a
     * non-attributable arc type with default visual representation.
     *
     * @param name the name of the arc type
     */
    protected ArcTypeImpl(String name) {
        this.itsAttrType = null;
        this.itsStringRepr = name;
        this.additionalRepr = ":SOLID_LINE:java.awt.Color[r=0,g=0,b=0]::[EDGE]:";
        this.resetKey();
    }

    /**
     * Creates a new arc type with the given attributes and the given name.
     *
     * @param at the attribute type declaration
     * @param name the name of the arc type
     */
    protected ArcTypeImpl(AttrType at, String name) {
        this(name);
        this.itsAttrType = at;
    }

    /**
     * Creates a new arc type with the given attributes and an empty name.
     *
     * @param at the attribute type declaration
     */
    protected ArcTypeImpl(AttrType at) {
        this();
        this.itsAttrType = at;
    }

    @Override
    public void dispose() {
        this.itsAttrType = null;
        if (this.edgeTypeGraphObjects != null) {
            Iterator<HashMap<Type, TypeGraphArc>> iter = this.edgeTypeGraphObjects.values().iterator();
            while (iter.hasNext()) {
                HashMap<Type, TypeGraphArc> map = iter.next();
                Iterator<TypeGraphArc> iter1 = map.values().iterator();
                while (iter1.hasNext()) {
                    iter1.next().dispose();
                }
            }
            this.edgeTypeGraphObjects.clear();
            this.edgeTypeGraphObjects = null;
        }
        this.typeGraphObjectDefined = false;
    }

    @Override
    public void createAttributeType() {
        this.itsAttrType = agg.attribute.impl.AttrTupleManager.getDefaultManager()
                .newType();
    }

    public void setAttributeType(final AttrType at) {
        this.itsAttrType = at;
    }

    @Override
    public void removeAttributeType() {
        if (this.itsAttrType != null) {
            ((DeclTuple) this.itsAttrType).dispose();
            this.itsAttrType = null;
        }
    }

    @Override
    public boolean isAttrTypeEmpty() {
        return (this.getAttrType() == null //					|| this.getAttrType().getNumberOfEntries() == 0
                );
    }

    @Override
    public boolean hasAnyAttrMember() {
        return (this.getAttrType() != null
                && this.getAttrType().getNumberOfEntries() != 0);
    }

    @Override
    public boolean isNodeType() {
        return false;
    }

    @Override
    public boolean isArcType() {
        return true;
    }

    /**
     * Returns result string of
     * <code>this.getStringRepr()+this.getAdditionalRepr()</code>
     *
     * @see <code>getStringRepr()</code> and <code>getAdditionalRepr()</code>
     */
    @Override
    public String convertToKey() {
        if (this.keyStr == null) {
            this.keyStr = this.itsStringRepr.concat("%").concat(this.additionalRepr);
//			this.keyStr = String.valueOf(this.hashCode());
        }
        return this.keyStr;
    }

    @Override
    public String resetKey() {
        this.keyStr = this.itsStringRepr.concat("%").concat(this.additionalRepr);
//		this.keyStr = String.valueOf(this.hashCode());
        return this.keyStr;
    }

    /**
     * Adds those attribute members of the specified Type type which are not
     * found in this type. A conflict can arise when a new member and an
     * existing member have equal names but different types. In this case the
     * name of the existing attribute member will be extended by "?" and then
     * the new attribute member will be added.
     */
    @Override
    public void adaptTypeAttribute(final Type type) {
        if (type.getAttrType() == null) {
            return;
        }
        if (this.itsAttrType == null) {
            this.itsAttrType = agg.attribute.impl.AttrTupleManager
                    .getDefaultManager().newType();
        }
        DeclTuple declTuple = (DeclTuple) this.itsAttrType;
        DeclTuple otherTuple = (DeclTuple) type.getAttrType();
        for (int i = 0; i < otherTuple.getSize(); i++) {
            DeclMember otherMem = (DeclMember) otherTuple.getMemberAt(i);
            if (otherMem.getHoldingTuple() != otherTuple) {
                continue;
            }
            String otherName = otherMem.getName();
            String otherType = otherMem.getTypeName();
            boolean nameFound = false;
            boolean conflict = false;
            DeclMember mem = null;
            for (int j = 0; j < declTuple.getSize(); j++) {
                mem = (DeclMember) declTuple.getMemberAt(j);
                if (mem.getHoldingTuple() != declTuple) {
                    continue;
                }
                if (mem.getName().equals(otherName)) {
                    nameFound = true;
                    if (!mem.getTypeName().equals(otherType)) {
                        conflict = true;
                    } else {
                        mem = null;
                    }
                    break;
                }
                mem = null;
            }
            if (nameFound && conflict && mem != null
                    && (mem.getHoldingTuple() == declTuple)) {
                mem.setName(mem.getName() + "?");
                declTuple.addMember(
                        agg.attribute.facade.impl.DefaultInformationFacade
                                .self().getJavaHandler(), otherType, otherName);
            } else if (!nameFound) {
                declTuple.addMember(
                        agg.attribute.facade.impl.DefaultInformationFacade
                                .self().getJavaHandler(), otherType, otherName);
            }
        }
    }

    @Override
    public void checkDoubleAttributeType() {
        if (this.itsAttrType == null) {
            return;
        }
        DeclTuple declTuple = (DeclTuple) this.itsAttrType;
        for (int i = 0; i < declTuple.getSize(); i++) {
            DeclMember memi = (DeclMember) declTuple.getMemberAt(i);
            String n = memi.getName();
//			String t = memi.getTypeName();
            boolean nameFound = false;
            boolean conflict = false;
            DeclMember memj = null;
            for (int j = i + 1; j < declTuple.getSize(); j++) {
                memj = (DeclMember) declTuple.getMemberAt(j);
                if (memj.getName().equals(n)) {
                    nameFound = true;
                    conflict = true;
                }
                if (nameFound && conflict) {
                    memj.setName(memj.getName() + "?");
                }
                nameFound = false;
                conflict = false;
            }
        }
    }

    /**
     * Returns TRUE if this type is equal to the type t.
     */
    @Override
    public boolean compareTo(final Type t) {
        if (!getStringRepr().equals(t.getStringRepr())) {
            return false;
        }
        if (!getAdditionalRepr().equals(t.getAdditionalRepr())) {
            return false;
        }
        if (this.itsAttrType != null) {
            return !(t.getAttrType() == null
                    || ((DeclTuple) this.itsAttrType).getSize()
                    != ((DeclTuple) t.getAttrType()).getSize()
                    || !((DeclTuple) this.itsAttrType).weakcompareTo(t.getAttrType()));
        }
        return t.getAttrType() == null;
    }

    /**
     * Returns TRUE if this type is different to the type t.The list difference
     * will contain all found differences between the types, otherwise it is
     * empty.This method should be used sooner for information about differences
     * of types.
     *
     * @param t
     * @param difference
     * @return
     */
    public boolean differentTo(final Type t, final List<String> difference) {
        String diff = "";
        if (!getStringRepr().equals(t.getStringRepr())) {
            diff = "Type name# " + getStringRepr() + " != " + t.getStringRepr();
            difference.add(diff);
        }
        if (!getAdditionalRepr().equals(t.getAdditionalRepr())) {
            diff = "Type graphical repr# " + getAdditionalRepr() + " != "
                    + t.getAdditionalRepr();
            difference.add(diff);
        }
        if (this.itsAttrType != null) {
            if (t.getAttrType() == null) {
                diff = "Attribute Type# " + "defined (is not null)" + " != "
                        + "not defined (is null)";
                difference.add(diff);
            } else if (((DeclTuple) this.itsAttrType).getSize() != ((DeclTuple) t
                    .getAttrType()).getSize()) {
                diff = "Attr member count# "
                        + ((DeclTuple) this.itsAttrType).getSize() + " != "
                        + ((DeclTuple) t.getAttrType()).getSize();
                difference.add(diff);
            } else if (!this.itsAttrType.compareTo(t.getAttrType())) {
                DeclTuple dt1 = (DeclTuple) this.itsAttrType;
                DeclTuple dt2 = (DeclTuple) t.getAttrType();
                for (int i = 0; i < dt1.getSize(); i++) {
                    DeclMember dm1 = (DeclMember) dt1.getMemberAt(i);
                    DeclMember dm2 = (DeclMember) dt2.getMemberAt(i);
                    if (!dm1.compareTo(dm2)) {
                        diff = i + ". " + "Member decl(type:name)# "
                                + dm1.getTypeName() + ":" + dm1.getName()
                                + " != " + dm2.getTypeName() + ":"
                                + dm2.getName();
                        difference.add(diff);
                    }
                }
            }
        } else if (t.getAttrType() != null) {
            diff = "Attribute Type# " + "not defined (is null)" + " != "
                    + "defined (is not null)";
            difference.add(diff);
        }
        return !difference.isEmpty();
    }

    /**
     * Returns the string representation. Mostly used as the name of a type.
     */
    @Override
    public final String getStringRepr() {
        return this.itsStringRepr;
    }

    /**
     * Sets the string representation. Mostly used as the name of the type
     */
    @Override
    public final void setStringRepr(final String n) {
        this.itsStringRepr = n;
        this.resetKey();
    }

    /**
     * Set textual comments for this type.
     *
     * @param text
     */
    @Override
    public void setTextualComment(final String text) {
        this.comment = text;
    }

    /**
     * Return textual comments of this type.
     */
    @Override
    public String getTextualComment() {
        return this.comment;
    }

    public List<String> checkDoubleAttributeName(final Type otherType) {
        List<String> v = new ArrayList<>(5);
        if (this.itsAttrType == null || otherType.getAttrType() == null) {
            return v;
        }
        DeclTuple myDecl = (DeclTuple) this.itsAttrType;
        DeclTuple otherDecl = (DeclTuple) otherType.getAttrType();
        for (int i = 0; i < otherDecl.getNumberOfEntries(); i++) {
            DeclMember mem = (DeclMember) otherDecl.getMemberAt(i);
            if (myDecl.isLegalName(mem.getName()) > 0) {
                if (mem.getHoldingTuple() != myDecl.getMemberAt(mem.getName()).getHoldingTuple()) {
                    v.add(otherDecl.getNameAsString(i));
                }
            }
        }
        return v;
    }

    /**
     * Returns the associated attribute type.
     */
    @Override
    public final AttrType getAttrType() {
        return this.itsAttrType;
    }

    /**
     * Returns the additional representation string
     *
     * @see #setAdditionalRepr
     */
    @Override
    public String getAdditionalRepr() {
        return this.additionalRepr;
    }

    /**
     * Set its additional graphical representation, which is always saved
     * together with its name. Predefined minimal additional representation
     * string of an Arc - ":SOLID_LINE:java.awt.Color[r=0,g=0,b=0]:[EDGE]:".
     */
    @Override
    public void setAdditionalRepr(final String repr) {
        if (repr.equals("EDGE") || repr.equals("[EDGE]")) {
            this.additionalRepr = ":SOLID_LINE:java.awt.Color[r=0,g=0,b=0]:[EDGE]:";
        } else {
            this.additionalRepr = repr;
        }
        this.resetKey();
    }

    @Override
    public void XwriteObject(XMLHelper h) {
        String n = getStringRepr();
//		System.out.println("TypeImpl.XwriteObject: " +getAdditionalRepr());
        if ((getAdditionalRepr() != null) && (!getAdditionalRepr().equals(""))) {
            n += ("%" + getAdditionalRepr());
        }
        if (n.contains("[EDGE]")) {
            h.openNewElem("EdgeType", this);
        } else {
            h.openNewElem("Type", this);
        }
        h.addAttr("name", n);
        if (!this.comment.equals("")) {
            h.addAttr("comment", this.comment);
        }
        h.addAttr("abstract", String.valueOf(false));
        if (this.itsAttrType != null && this.itsAttrType.getNumberOfEntries() > 0) {
            h.addObject("", this.itsAttrType, true);
        }
        // multiplicity will be written in the Arc
        // object in the type graph
        h.close();
    }

    @Override
    public void XreadObject(XMLHelper h) {
        if (h.isTag("NodeType", this) || h.isTag("EdgeType", this)
                || h.isTag("Type", this)) {
            String n = h.readAttr("name");
//			n = XMLHelper.checkNameDueToSpecialCharacters(n);
//			System.out.println("TypeImpl.XreadObject: " +n);
            String str = h.readAttr("comment");
            if (!str.equals("")) {
                this.comment = str;
            }
            h.readAttr("abstract");
            int i = n.indexOf('%');
            // set type name
            if (i != -1) {
                String test = n.substring(0, i);
                test = XMLHelper.checkNameDueToSpecialCharacters(test);
                this.itsStringRepr = test;
//				itsStringRepr = n.substring(0, i);
            } else {
                String test = XMLHelper.checkNameDueToSpecialCharacters(n);
                this.itsStringRepr = test;
//				itsStringRepr = n;				
            }
            AttrType tmpAttr = agg.attribute.impl.AttrTupleManager
                    .getDefaultManager().newType();
            h.enrichObject(tmpAttr);
            if (tmpAttr.getNumberOfEntries() != 0) {
                this.itsAttrType = tmpAttr;
            } else {
                this.itsAttrType = null;
            }
            if (i != -1) {
                String a = n.substring(i + 1);
                a = a.replaceAll("::", ":");
                n = n.substring(0, i);
                setAdditionalRepr(a);
            }
            // NOTE:: multiplicity will be read in the TypeGraph
            h.close();
        }
    }

    /**
     * internal function to convert a type into a string. If the type contains
     * an empty string representation, this function will return "unnamed"
     * otherwise the string representation of the type
     * ({@link #getStringRepr()})
     */
    @Override
    public String getName() {
        String stringRepr = this.getStringRepr();
        if ("".equals(stringRepr)) {
            return "unnamed";
        }
        return stringRepr;
    }

    /**
     * returns if the given GraphObject is valid typed as defined in the type
     * graph.Before this can be checked, all edges and nodes of the type graph
     * must be added to their types. The given object is not taken in account
     * when this is its type.
     *
     * @param nodeOrArc
     * @return null, if the graph object is valid typed otherwise a
     * {@link TypeError} if there was a mismatch
     *
     */
    @Override
    public TypeError check(final GraphObject nodeOrArc, final int level) {
        if (level == TypeSet.DISABLED) {
            return null;
        }
        if (nodeOrArc instanceof Arc arc) {
            return check(arc, level);
        }
        throw new IllegalArgumentException(
                "parameter must be of Arc type.");
    }

    /**
     * Returns true if at least one edge exists from the specified source type
     * to any other type, otherwise false.
     */
    @Override
    public boolean hasTypeGraphArc(final Type sourceType) {
        if (this.edgeTypeGraphObjects != null) {
            List<Type> mySrcParents = sourceType.getAllParents();
            for (int i = 0; i < mySrcParents.size(); ++i) {
                Type mySrcType = mySrcParents.get(i);
                HashMap<Type, TypeGraphArc> targets = this.edgeTypeGraphObjects.get(mySrcType);
                if (targets != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Searches for a type that is the target type of this edge type with the
     * specified source type. Returns a list with all found target types,
     * otherwise empty list.
     *
     */
    @Override
    public List<Type> getTargetsOfArc(final Type sourceType) {
        final List<Type> v = new ArrayList<>();
        // try to find any edge between any pair of src-tar parents
        if (this.edgeTypeGraphObjects != null) {
            List<Type> mySrcParents = sourceType.getAllParents();
            for (int i = 0; i < mySrcParents.size(); ++i) {
                Type mySrcType = mySrcParents.get(i);
                HashMap<Type, TypeGraphArc> targets = this.edgeTypeGraphObjects.get(mySrcType);
                if (targets == null) {
                    continue;
                }
                Iterator<Type> iter = targets.keySet().iterator();
                while (iter.hasNext()) {
                    Type t = iter.next();
                    v.add(t);
                    v.addAll(t.getChildren());
                }
            }
        }
        return v;
    }

    @Override
    public boolean isEdgeCreatable(final Type sourceType, final Type targetType, final int level) {
        // iterator for the parents of the src and target type of the current
        // arc
        Type mySrcType = sourceType;
        Type myTarType = targetType;
        // find out all parents of source and target
        List<Type> mySrcParents = sourceType.getAllParents();
        List<Type> myTarParents = targetType.getAllParents();
        TypeGraphArc subType = null;
        // try to find any edge between any pair of src/tar parents
        if (this.edgeTypeGraphObjects != null) {
            for (int i = 0; i < mySrcParents.size(); ++i) {
                mySrcType = mySrcParents.get(i);
                HashMap<Type, TypeGraphArc> targets = this.edgeTypeGraphObjects.get(mySrcType);
                if (targets == null) {
                    continue;
                }
                for (int j = 0; j < myTarParents.size(); ++j) {
                    myTarType = myTarParents.get(j);
                    subType = targets.get(myTarType);
                    if (subType != null) {
                        break;
                    }
                }
                if (subType != null) {
                    break;
                }
            }
            //TODO  test
            if (subType == null) {
                for (int i = 0; i < myTarParents.size(); ++i) {
                    myTarType = myTarParents.get(i);
                    HashMap<Type, TypeGraphArc> targets = this.edgeTypeGraphObjects.get(myTarType);
                    if (targets == null) {
                        continue;
                    }
                    for (int j = 0; j < mySrcParents.size(); ++j) {
                        mySrcType = mySrcParents.get(j);
                        subType = targets.get(mySrcType);
                        if (subType != null) {
                            break;
                        }
                    }
                    if (subType != null) {
                        break;
                    }
                }
            }
        }
        return (subType != null);
    }

    /**
     * Returns null, if the specified arc is valid typed as defined in the type
     * graph.Before this can be checked, all edges and nodes of the type graph
     * must be added to theire types.
     *
     * @param arc
     * @param level
     * @return null, if the Arc is valid typed otherwise a {@link TypeError} if
     * there was a mismatch
     */
    public TypeError check(final Arc arc, final int level) {
        if (this.edgeTypeGraphObjects != null) {
            // the source and target type of the current arc
            final Type sourceType = arc.getSource().getType();
            final Type targetType = arc.getTarget().getType();
            // find out all parents of source and target
            final List<Type> mySrcParents = sourceType.getAllParents();
            final List<Type> myTarParents = targetType.getAllParents();
            Type mySrcType = arc.getSource().getType();
            Type myTarType = arc.getTarget().getType();
            TypeGraphArc subType = null;
            // try to find any edge between any pair of src/tar parents
            for (int i = 0; i < mySrcParents.size(); ++i) {
                mySrcType = mySrcParents.get(i);
                HashMap<Type, TypeGraphArc> targets = this.edgeTypeGraphObjects.get(mySrcType);
                if (targets == null) {
                    continue;
                }
                for (int j = 0; j < myTarParents.size(); ++j) {
                    myTarType = myTarParents.get(j);
                    subType = targets.get(myTarType);
                    if (subType != null) {
                        break;
                    }
                }
                if (subType != null) {
                    break;
                }
            }
            if (subType != null && subType.doesTypeGraphObjectExist()) {
                if (level > TypeSet.ENABLED) {
                    int count = 0;
                    int sourceMax = subType.getSourceMax();
                    int targetMax = subType.getTargetMax();
                    int sourceMin = subType.getSourceMin();
                    int targetMin = subType.getTargetMin();
                    if (targetMax != UNDEFINED) {
                        // if multipl. defined, check if arc is possible
                        count = ((Node) arc.getSource()).getNumberOfOutgoingArcsOfTypeToTargetType(this, myTarType);
                        // if there are too many outgoing arcs
                        if (count > targetMax) {
                            String isOrAre = "is";
                            if (targetMax != 1) {
                                isOrAre = "are";
                            }
                            return new TypeError(TypeError.TO_MUCH_ARCS,
                                    "- Too many edges of type \"" + getName()
                                    + "\" end at the node of type \""
                                    + myTarType.getName() + "\".\nThere " //targetType.getName() 
                                    + isOrAre + " only " + targetMax
                                    + " allowed ( graph \""
                                    + arc.getContext().getName() + "\" ).",
                                    arc, this);
                        }
                    }
                    if (sourceMax != UNDEFINED) {
                        // if multipl. defined, check if arc is possible
                        count = ((Node) arc.getTarget()).getNumberOfIncomingArcsOfTypeFromSourceType(this, mySrcType);
                        if (count > sourceMax) {
                            String isOrAre = "is";
                            if (sourceMax != 1) {
                                isOrAre = "are";
                            }
                            return new TypeError(TypeError.TO_MUCH_ARCS,
                                    "- Too many edges of type \"" + getName()
                                    + "\" start at the node of type \""
                                    + mySrcType.getName() + "\".\nThere " //sourceType
                                    + isOrAre + " only " + sourceMax
                                    + " allowed ( graph \""
                                    + arc.getContext().getName() + "\" ).",
                                    arc, this);
                        }
                    }
                    if (level >= TypeSet.ENABLED_MAX_MIN) {
                        if (targetMin > 0) {
                            // if multipl. defined, check if arc is possible
                            count = ((Node) arc.getSource()).getNumberOfOutgoingArcsOfTypeToTargetType(this, myTarType);
                            // if there are too many outgoing arcs
                            if (count < targetMin) {
                                String isOrAre = "is";
                                if (targetMin != 1) {
                                    isOrAre = "are";
                                }
                                return new TypeError(TypeError.TO_LESS_ARCS,
                                        "- Too few edges of type \"" + getName()
                                        + "\" end at the node of type \""
                                        + targetType.getName()
                                        + "\".\nThere " + isOrAre
                                        + " at least " + targetMin
                                        + " required ( graph \""
                                        + arc.getContext().getName()
                                        + "\" ).", arc, this);
                            }
                        }
                        if (sourceMin > 0) {
                            // if multipl. defined, check if arc is possible
                            count = ((Node) arc.getTarget()).getNumberOfIncomingArcsOfTypeFromSourceType(this, mySrcType);
                            if (count < sourceMin) {
                                String isOrAre = "is";
                                if (sourceMin != 1) {
                                    isOrAre = "are";
                                }
                                return new TypeError(TypeError.TO_LESS_ARCS,
                                        "- Too few edges of type \"" + getName()
                                        + "\" start at the node of type \""
                                        + sourceType.getName()
                                        + "\".\nThere " + isOrAre
                                        + " at least " + sourceMin
                                        + " required ( graph \""
                                        + arc.getContext().getName()
                                        + "\" ).", arc, this);
                            }
                        }
                    }
                }
                return null;
            } else if (level > TypeSet.ENABLED_INHERITANCE) {
                // no such source or target or there is no type graph object defined
                // for this combination
                return new TypeError(TypeError.NO_SUCH_TYPE,
                        "- The type graph does not contain an edge type with name \""
                        + getName() + "\" \nbetween node type \""
                        + sourceType.getName() + "\" and \""
                        + targetType.getName() + "\""
                        + "\n ( see graph:  " + arc.getContext().getName() + " ).", arc, this);
            }
        }
        return null;
    }

    @Override
    public TypeError checkIfEdgeCreatable(final Node src, final Node tar, final int level) {
        return checkIfEdgeCreatable(null, src, tar, level);
    }

    @Override
    public TypeError checkIfEdgeCreatable(final Graph g, final Node src, final Node tar, final int level) {
        if ((level == TypeSet.DISABLED)
                || (level == TypeSet.ENABLED_INHERITANCE)
                || (level == TypeSet.ENABLED)) {
            return null;
        }
        TypeError typeError = checkSourceMax(g, src, tar);
        if (typeError == null) {
            typeError = checkTargetMax(g, src, tar);
        }
        return typeError;
    }

    /*
	 * Source Max Multiplicity means how many ( at most ) nodes of the source node type
	 * are incoming into the target node. 
     */
    @Override
    public TypeError checkSourceMax(final Graph g, final Node src, final Node tar) {
        String graphName = "";
        if (g != null) {
            graphName = g.getName();
        }
        final Type sourceType = src.getType();
        final Type targetType = tar.getType();
        if (this.edgeTypeGraphObjects != null) {
            // check entry for source
            final HashMap<Type, TypeGraphArc> targets = this.edgeTypeGraphObjects.get(sourceType);
            if (targets != null) {
                final TypeGraphArc subType = targets.get(targetType);
                // search for the type graph object
                if ((subType != null) && (subType.doesTypeGraphObjectExist())) {
                    int sourceMax = subType.getSourceMax();
                    if (sourceMax != UNDEFINED) {
                        int count = tar.getNumberOfIncomingArcs(this, sourceType);
                        if (count + 1 > sourceMax) {
                            String isOrAre = "is";
                            if (sourceMax != 1) {
                                isOrAre = "are";
                            }
                            return new TypeError(TypeError.TO_MUCH_ARCS,
                                    "Too many edges of type \"" + getName()
                                    + "\" (would) start at the node of type \""
                                    + src.getType().getName()
                                    + "\" (green marked node).\nThere "
                                    + isOrAre + " only " + sourceMax
                                    + " allowed ( graph \""
                                    + graphName
                                    + "\" ).", tar, this);
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Target Max Multiplicity means how many ( at most ) nodes of the target
     * node type are outgoing from the source node.
     */
    @Override
    public TypeError checkTargetMax(final Graph g, final Node src, final Node tar) {
        String graphName = "";
        if (g != null) {
            graphName = g.getName();
        }
        final Type sourceType = src.getType();
        final Type targetType = tar.getType();
        if (this.edgeTypeGraphObjects != null) {
            // check entry for source
            final HashMap<Type, TypeGraphArc> targets = this.edgeTypeGraphObjects.get(sourceType);
            if (targets != null) {
                final TypeGraphArc subType = targets.get(targetType);
                // search for the type graph object
                if ((subType != null) && (subType.doesTypeGraphObjectExist())) {
                    int targetMax = subType.getTargetMax();
                    if (targetMax != UNDEFINED) {
                        int count = src.getNumberOfOutgoingArcs(this, targetType);
                        if (count + 1 > targetMax) {
                            String isOrAre = "is";
                            if (targetMax != 1) {
                                isOrAre = "are";
                            }
                            return new TypeError(TypeError.TO_MUCH_ARCS,
                                    "Too many edges of type \"" + getName()
                                    + "\" (would) end at the node of type \""
                                    + targetType.getName()
                                    + "\" (green marked node).\nThere "
                                    + isOrAre + " only " + targetMax
                                    + " allowed ( graph \""
                                    + graphName
                                    + "\" ).", src, this);
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Add the given GraphObject of a type graph to this type.The GraphObject
     * nodeOrArc must be of this type: it is a Node if this is a node type, it
     * is an Arc if this is an edge type. In case of it is a node type and a
     * node object inside of a type graph is already exist, it should to be
     * removed first.
     *
     * @param arc
     */
    @Override
    public boolean addTypeGraphObject(final GraphObject arc) {
        if (arc instanceof Arc && arc.getContext().isTypeGraph()) {
            Type sourceType = ((Arc) arc).getSource().getType();
            Type targetType = ((Arc) arc).getTarget().getType();
            TypeGraphArc subType = getTypeGraphArc(sourceType, targetType);
            if (subType.getArc() == null) {
                subType.addTypeGraphObject((Arc) arc);
                this.typeGraphObjectDefined = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the given GraphObject from the type graph and from this
     * type.Returns true if remove is done, otherwise false. To remove an
     * GraphObject is not possible when the type graph check is activated.
     *
     * @param arc
     */
    @Override
    public boolean removeTypeGraphObject(final GraphObject arc, final boolean forceToRemove) {
        if (arc == null
                || !arc.isArc()
                || arc.getContext() == null
                || !arc.getContext().isTypeGraph()) {
            return true;
        }
        boolean allowedToRemove = false;
        if (arc.getContext().getTypeSet().getLevelOfTypeGraphCheck()
                <= TypeSet.ENABLED_INHERITANCE) {
            allowedToRemove = true;
        } else {
            if (forceToRemove) {
                allowedToRemove = true;
            } else {
                allowedToRemove = false;
            }
        }
        if (allowedToRemove) {
            if (this.edgeTypeGraphObjects == null) {
                return true;
            }
            // get source and target
            Type sourceType = ((Arc) arc).getSource().getType();
            Type targetType = ((Arc) arc).getTarget().getType();
            HashMap<Type, TypeGraphArc> targets = this.edgeTypeGraphObjects.get(sourceType);
            if (targets == null) {
                return true;
            }
            TypeGraphArc subType = targets.get(targetType);
            if (subType == null) {
                return true;
            }
            if (arc.getContext().getTypeSet().getLevelOfTypeGraphCheck() <= TypeSet.ENABLED_INHERITANCE) { //TypeSet.DISABLED) 
                subType.forceRemoveTypeGraphObject();
            } else if (forceToRemove) {
                subType.forceRemoveTypeGraphObject();
            } else if (!subType.removeTypeGraphObject()) {
                return false;
            }
            // if the subtype doesn't contains a type graph object
            // or some using graph objects, we can destroy it
            targets.remove(targetType);
            // remove list, if it is empty
            if (targets.isEmpty()) {
                this.edgeTypeGraphObjects.remove(sourceType);
                // remove HashMap if it is empty
                if (this.edgeTypeGraphObjects.isEmpty()) {
                    this.edgeTypeGraphObjects = null;
                    this.typeGraphObjectDefined = false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Remove the given GraphObject from the type graph and from this
     * type.Returns true if remove is done, otherwise false. To remove an
     * GraphObject is not possible when the type graph check is activated.
     *
     * @param arc
     */
    @Override
    public boolean removeTypeGraphObject(final GraphObject arc) {
        return removeTypeGraphObject(arc, false);
    }

    /**
     * Set the min of the source multiplicity of an edge type to the value. The
     * edge type is defined by the node type sourceType and the node type
     * targetType.
     */
    @Override
    public void setSourceMin(final Type sourceType, final Type targetType, final int value) {
        this.getTypeGraphArc(sourceType, targetType).setSourceMin(value);
    }

    /**
     * Set the max of the source multiplicity of an edge type to the value. The
     * edge type is defined by the node type sourceType and the node type
     * targetType.
     */
    @Override
    public void setSourceMax(final Type sourceType, final Type targetType, final int value) {
        this.getTypeGraphArc(sourceType, targetType).setSourceMax(value);
    }

    /**
     * Set the min of the target multiplicity of an edge type to the value. The
     * edge type is defined by the node type sourceType and the node type
     * targetType.
     */
    @Override
    public void setTargetMin(final Type sourceType, final Type targetType, final int value) {
        this.getTypeGraphArc(sourceType, targetType).setTargetMin(value);
    }

    /**
     * Set the max of the target multiplicity of an edge type to the value. The
     * edge type is defined by the node type sourceType and the node type
     * targetType.
     */
    @Override
    public void setTargetMax(final Type sourceType, final Type targetType, final int value) {
        this.getTypeGraphArc(sourceType, targetType).setTargetMax(value);
    }

    /**
     * Return the min of the source multiplicity of an edge type. The edge type
     * is defined by the node type sourceType and the node type targetType.
     */
    @Override
    public int getSourceMin(final Type sourceType, final Type targetType) {
        return this.getTypeGraphArc(sourceType, targetType).getSourceMin();
    }

    /**
     * Return the max of the source multiplicity of an edge type. The edge type
     * is defined by the node type sourceType and the node type targetType.
     */
    @Override
    public int getSourceMax(final Type sourceType, final Type targetType) {
        return this.getTypeGraphArc(sourceType, targetType).getSourceMax();
    }

    /**
     * Return the min of the target multiplicity of an edge type. The edge type
     * is defined by the node type sourceType and the node type targetType.
     */
    @Override
    public int getTargetMin(final Type sourceType, final Type targetType) {
        return this.getTypeGraphArc(sourceType, targetType).getTargetMin();
    }

    /**
     * Return the max of the target multiplicity of an edge type. The edge type
     * is defined by the node type sourceType and the node type targetType.
     */
    @Override
    public int getTargetMax(final Type sourceType, final Type targetType) {
        return this.getTypeGraphArc(sourceType, targetType).getTargetMax();
    }

    @Override
    public void setVisibityOfObjectsOfTypeGraphArc(final Type sourceType, final Type targetType, boolean vis) {
        TypeGraphArc tgarc = getTypeGraphArc(sourceType, targetType);
        if (tgarc != null) {
            tgarc.setVisible(vis);
        }
    }

    @Override
    public boolean isObjectOfTypeGraphArcVisible(final Type sourceType, final Type targetType) {
        TypeGraphArc tgarc = getTypeGraphArc(sourceType, targetType);
        return (tgarc == null) || tgarc.isVisible();
    }

    @Override
    public Arc getTypeGraphArcObject(final Type sourceType, final Type targetType) {
        TypeGraphArc tgarc = getTypeGraphArc(sourceType, targetType);
        if (tgarc != null) {
            return tgarc.getArc();
        }
        return null;
    }

    /**
     * Returns the subtype object for this source and target combination. The
     * subtype will be created, if it does not exist.
     */
    @Override
    public TypeGraphArc getTypeGraphArc(final Type sourceType, final Type targetType) {
        // iterator for the parents of the src and target type of the current
        // arc
        Type mySrcType = sourceType;
        Type myTarType = targetType;
        // find out all parents of source and target
        List<Type> mySrcParents = sourceType.getAllParents();
        List<Type> myTarParents = targetType.getAllParents();
        HashMap<Type, TypeGraphArc> targets = null;
        TypeGraphArc subType = null;
        // create Map if not def.
        if (this.edgeTypeGraphObjects == null) {
            this.edgeTypeGraphObjects = new HashMap<Type, HashMap<Type, TypeGraphArc>>();
            this.typeGraphObjectDefined = true;
        }
        // create HashMap for this sourceType if not def.
        for (int i = 0; i < mySrcParents.size(); ++i) {
            mySrcType = mySrcParents.get(i);
            targets = this.edgeTypeGraphObjects.get(mySrcType);
            if (targets != null) {
                for (int j = 0; j < myTarParents.size(); ++j) {
                    myTarType = myTarParents.get(j);
                    subType = targets.get(myTarType);
                    if (subType != null) {
                        return subType;
                    }
                }
            }
        }
        targets = this.edgeTypeGraphObjects.get(sourceType);
        if (targets == null) {
            targets = new HashMap<>();
            this.edgeTypeGraphObjects.put(sourceType, targets);
            subType = new TypeGraphArc();
            targets.put(targetType, subType);
        }
        if (subType == null) {
            subType = new TypeGraphArc();
            targets.put(targetType, subType);
        }
        return subType;
    }

    @Override
    public TypeGraphArc getSimilarTypeGraphArc(final Type sourceType,
            final Type targetType) {
        Iterator<Type> sourceIter = this.edgeTypeGraphObjects.keySet().iterator();
        while (sourceIter.hasNext()) {
            Type srct = sourceIter.next();
            // if(getName().equals("s:i")){
            // System.out.println("type s:i source:: "+srct.getName()+" ::
            // "+sourceType.getName()+" "+targetType.getName());
            // }
            if (!srct.compareTo(sourceType)) {
                continue;
            }
            HashMap<Type, TypeGraphArc> targetsMap = this.edgeTypeGraphObjects
                    .get(srct);
            Iterator<Type> targetsIter = targetsMap.keySet().iterator();
            while (targetsIter.hasNext()) {
                Type tart = targetsIter.next();
                // if(getName().equals("s:i")){
                // System.out.println("type s:i target:: "+tart.getName()+" ::
                // "+sourceType.getName()+" "+targetType.getName());
                // }
                if (!tart.compareTo(targetType)) {
                    continue;
                }
                TypeGraphArc subType = targetsMap.get(tart);
                // if(getName().equals("s:i")){
                // System.out.println("type s:i TypeGraphArc: "+ subType);
                // }
                return subType;
            }
        }
        return null;
    }

    @Override
    public boolean hasTypeGraphArc() {
        return (this.edgeTypeGraphObjects != null);
    }

    @Override
    public boolean hasTypeGraphArc(final Type sourceType, final Type targetType) {
        if (this.edgeTypeGraphObjects == null) {
            return false;
        }
        Type mySrcType = sourceType;
        Type myTarType = targetType;
        List<Type> mySrcParents = sourceType.getAllParents();
        List<Type> myTarParents = targetType.getAllParents();
        TypeGraphArc subType = null;
        // try to find any edge between any pair of src/tar parents
        for (int i = 0; i < mySrcParents.size(); ++i) {
            mySrcType = mySrcParents.get(i);
            HashMap<Type, TypeGraphArc> targets = this.edgeTypeGraphObjects
                    .get(mySrcType);
            if (targets != null) {
                for (int j = 0; j < myTarParents.size(); ++j) {
                    myTarType = myTarParents.get(j);
                    subType = targets.get(myTarType);
                    if (subType != null) {
                        break;
                    }
                }
                if (subType != null) {
                    break;
                }
            }
        }
        return (subType != null);
    }

    @Override
    public boolean hasTypeGraphArc(
            final GraphObject sourceType,
            final GraphObject targetType) {
        return hasTypeGraphArc(sourceType.getType(), targetType.getType());
    }

    @Override
    public HashMap<Type, HashMap<Type, TypeGraphArc>> getArcTypeGraphObjects() {
        return this.edgeTypeGraphObjects;
    }

    @Override
    public boolean compareTypeGraphArcs(final Type t) {
        if (this.edgeTypeGraphObjects == null
                && ((ArcTypeImpl) t).getArcTypeGraphObjects() == null) {
            return true;
        } else if (this.edgeTypeGraphObjects != null
                && ((ArcTypeImpl) t).getArcTypeGraphObjects() != null) {
            if (!this.edgeTypeGraphObjects.isEmpty()
                    && !((ArcTypeImpl) t).getArcTypeGraphObjects().isEmpty()) {
                Iterator<Type> sourceIter = this.edgeTypeGraphObjects.keySet().iterator();
                while (sourceIter.hasNext()) {
                    Type srct = sourceIter.next();
                    HashMap<Type, TypeGraphArc> targetsMap = this.edgeTypeGraphObjects
                            .get(srct);
                    Iterator<Type> targetsIter = targetsMap.keySet().iterator();
                    while (targetsIter.hasNext()) {
                        Type tart = targetsIter.next();
//						TypeGraphArc subType = targetsMap.get(tart);
                        TypeGraphArc subType_t = ((ArcTypeImpl) t)
                                .getSimilarTypeGraphArc(srct, tart);
                        if (subType_t != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean compareTypeGraphArcsMultiplicity(final Type t) {
        if (this.edgeTypeGraphObjects == null
                && ((ArcTypeImpl) t).getArcTypeGraphObjects() == null) {
            return true;
        } else if (this.edgeTypeGraphObjects != null
                && ((ArcTypeImpl) t).getArcTypeGraphObjects() != null) {
            if (!this.edgeTypeGraphObjects.isEmpty()
                    && !((ArcTypeImpl) t).getArcTypeGraphObjects().isEmpty()) {
                // System.out.println("Typeimpl.compareTypeGraphArcs ...");
                Iterator<Type> sourceIter = this.edgeTypeGraphObjects.keySet().iterator();
                while (sourceIter.hasNext()) {
                    Type srct = sourceIter.next();
                    HashMap<Type, TypeGraphArc> targetsMap = this.edgeTypeGraphObjects
                            .get(srct);
                    Iterator<Type> targetsIter = targetsMap.keySet().iterator();
                    while (targetsIter.hasNext()) {
                        Type tart = targetsIter.next();
                        TypeGraphArc subType = targetsMap.get(tart);
                        TypeGraphArc subType_t = ((ArcTypeImpl) t)
                                .getSimilarTypeGraphArc(srct, tart);
                        if (subType_t != null) {
                            // System.out.println("Typeimpl.compareTypeGraphArcs
                            // ... "+subType+" with "+subType_t);
                            if (subType.getSourceMax() != subType_t
                                    .getSourceMax()) {
                                return false;
                            } else if (subType.getSourceMin() != subType_t
                                    .getSourceMin()) {
                                return false;
                            } else if (subType.getTargetMin() != subType_t
                                    .getTargetMin()) {
                                return false;
                            } else if (subType.getTargetMax() != subType_t
                                    .getTargetMax()) {
                                return false;
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * Disable type graph object of this type.
     */
    @Override
    public void disableTypeGraphObject() {
        if (this.edgeTypeGraphObjects != null) {
            Iterator<HashMap<Type, TypeGraphArc>> iter = this.edgeTypeGraphObjects.values().iterator();
            while (iter.hasNext()) {
                HashMap<Type, TypeGraphArc> actMap = iter.next();
                Iterator<TypeGraphArc> subIter = actMap.values().iterator();
                while (subIter.hasNext()) {
                    TypeGraphArc subType = subIter.next();
                    // remove the type graph object
                    subType.forceRemoveTypeGraphObject();
                }
                if (actMap.isEmpty()) {
                    // if there is no type graph arc with this source, we can
                    // remove the Hash Map
                    iter.remove();
                }
            }
            if (this.edgeTypeGraphObjects.isEmpty()) {
                this.edgeTypeGraphObjects = null;
                this.typeGraphObjectDefined = false;
            }
        }
    }

    /**
     * returns true, if there is at least one object in the type graph for this
     * type.
     */
    @Override
    public boolean isTypeGraphObjectDefined() {
        return this.typeGraphObjectDefined;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see agg.xt_basis.Type#checkIfRemovableFromSource(agg.xt_basis.Node,
	 *      agg.xt_basis.Arc, int)
     */
    @Override
    public TypeError checkIfRemovableFromSource(final GraphObject node, final Arc arc,
            final int level) {
        if (arc.getContext().isCompleteGraph()
                && level == TypeSet.ENABLED_MAX_MIN) {
            return checkSourceMin(node, arc, false, false);
        }
        return null;
    }

    @Override
    public TypeError checkIfRemovableFromSource(
            final GraphObject node, final Arc arc,
            boolean deleteSrc, boolean deleteTar,
            final int level) {
        if (arc.getContext().isCompleteGraph()
                && level == TypeSet.ENABLED_MAX_MIN) {
            return checkSourceMin(node, arc, deleteSrc, deleteTar);
        }
        return null;
    }

    /*
	 * Source Max Multiplicity means how many ( at least ) nodes of the source node type
	 * are incoming into the target node. 
     */
    private TypeError checkSourceMin(final GraphObject srcnode, final Arc arc,
            boolean deleteSrc, boolean deleteTar) {
//		System.out.println("TypeImpl.checkSourceMin(final GraphObject srcnode, final Arc arc)");
        int sourceMin = arc.getType().getSourceMin(this, arc.getTarget().getType());
        if (sourceMin != UNDEFINED) {
            int count = ((Node) arc.getTarget()).getNumberOfIncomingArcs(arc.getType(), srcnode.getType());
            if (count - 1 < sourceMin
                    && !deleteTar) {
                return new TypeError(TypeError.TO_LESS_ARCS,
                        "Too few edges of type \"" + arc.getType().getName()
                        + "\"" + " (would) start at the source node  " + "\""
                        + arc.getSource().getType().getName()
                        + "\"." + "\nThere are at least " + sourceMin
                        + " needed ( graph \""
                        + srcnode.getContext().getName() + "\" ).",
                        arc,
                        this);
            }
        }
        return null;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see agg.xt_basis.Type#checkIfRemovableFromTarget(agg.xt_basis.Node,
	 *      agg.xt_basis.Arc, int)
     */
    @Override
    public TypeError checkIfRemovableFromTarget(final GraphObject node, final Arc arc,
            final int level) {
        if (arc.getContext().isCompleteGraph()
                && level == TypeSet.ENABLED_MAX_MIN) {
            return checkTargetMin(node, arc, false, false);
        }
        return null;
    }

    @Override
    public TypeError checkIfRemovableFromTarget(final GraphObject node, final Arc arc,
            boolean deleteSrc, boolean deleteTar,
            final int level) {
        if (arc.getContext().isCompleteGraph()
                && level == TypeSet.ENABLED_MAX_MIN) {
            return checkTargetMin(node, arc, deleteSrc, deleteTar);
        }
        return null;
    }

    /*
	 * Target Min Multiplicity means how many (at least ) nodes of the target node type
	 * are outgoing from the source node. 
     */
    private TypeError checkTargetMin(final GraphObject tarnode, final Arc arc,
            boolean deleteSrc, boolean deleteTar) {
//		System.out.println("TypeImpl.checkTargetMin(final GraphObject tarnode, final Arc arc)");
        int targetMin = arc.getType().getTargetMin(arc.getSource().getType(), this);
        if (targetMin != UNDEFINED) {
            int count = ((Node) arc.getSource()).getNumberOfOutgoingArcs(arc.getType(), tarnode.getType());
            if (count - 1 < targetMin
                    && !deleteSrc) {
                return new TypeError(TypeError.TO_LESS_ARCS,
                        "Too few edges of type \"" + arc.getType().getName()
                        + "\"" + " (would) end at the target node  " + "\""
                        + arc.getTarget().getType().getName()
                        + "\"." + "\nThere are at least " + targetMin
                        + " needed ( graph \""
                        + tarnode.getContext().getName() + "\" ).",
                        arc,
                        this);
            }
        }
        return null;
    }

    /**
     * @see agg.xt_basis.Type#isParentOf(agg.xt_basis.Type)
     */
    @Override
    public boolean isParentOf(Type t) {
        return t.compareTo(this);
    }

    /**
     * @see agg.xt_basis.Type#isRelatedTo(agg.xt_basis.Type)
     */
    @Override
    public boolean isRelatedTo(Type t) {
        return t.compareTo(this);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#addParent(agg.xt_basis.Type)
     */
    @Override
    public void addParent(Type t) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#checkIfNodeCreatable(agg.xt_basis.Graph, int)
     */
    @Override
    public TypeError checkIfNodeCreatable(Graph basisGraph,
            int levelOfTypeGraphCheck) {
        return null;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#checkIfRemovable(agg.xt_basis.Node, int)
     */
    @Override
    public TypeError checkIfRemovable(Node node, int level) {
        return null;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getAllChildren()
     */
    @Override
    public List<Type> getAllChildren() {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getAllParents()
     */
    @Override
    public List<Type> getAllParents() {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getChildren()
     */
    @Override
    public List<Type> getChildren() {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getCommonParentWith(agg.xt_basis.Type)
     */
    @Override
    public List<Type> getCommonParentWith(Type t) {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getImageFilename()
     */
    @Override
    public String getImageFilename() {
        return "";
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getParent()
     */
    @Override
    public Type getParent() {
        return null;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getParents()
     */
    @Override
    public List<Type> getParents() {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getSourceMax()
     */
    @Override
    public int getSourceMax() {
        return 0;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getSourceMin()
     */
    @Override
    public int getSourceMin() {
        return 0;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getTypeGraphNodeObject()
     */
    @Override
    public Node getTypeGraphNodeObject() {
        return null;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#hasTypeGraphNode()
     */
    @Override
    public boolean hasTypeGraphNode() {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#isAbstract()
     */
    @Override
    public boolean isAbstract() {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#isChildOf(agg.xt_basis.Type)
     */
    @Override
    public boolean isChildOf(Type t) {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#isObjectOfTypeGraphNodeVisible()
     */
    @Override
    public boolean isObjectOfTypeGraphNodeVisible() {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#removeParent(agg.xt_basis.Type)
     */
    @Override
    public void removeParent(Type t) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#setAbstract(boolean)
     */
    @Override
    public void setAbstract(boolean b) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#setImageFilename(java.lang.String)
     */
    @Override
    public void setImageFilename(String imageFilename) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#setParent(agg.xt_basis.Type)
     */
    @Override
    public void setParent(Type t) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#setSourceMax(int)
     */
    @Override
    public void setSourceMax(int value) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#setSourceMin(int)
     */
    @Override
    public void setSourceMin(int value) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#setVisibilityOfObjectsOfTypeGraphNode(boolean)
     */
    @Override
    public void setVisibilityOfObjectsOfTypeGraphNode(boolean vis) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#hasInheritedAttribute()
     */
    @Override
    public boolean hasInheritedAttribute() {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getMaxMultiplicityOfAllChildren()
     */
    @Override
    public int getMaxMultiplicityOfAllChildren() {
        return 0;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getMinMultiplicityOfAllChildren()
     */
    @Override
    public int getMinMultiplicityOfAllChildren() {
        return 0;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#isChildTypeGraphNodeUsed()
     */
    public boolean isChildTypeGraphNodeUsed() {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#isTypeGraphNodeUsed()
     */
    public boolean isTypeGraphNodeUsed() {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#addChild(agg.xt_basis.Type)
     */
    @Override
    public void addChild(Type t) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getOwnIncomingArcs()
     */
    @Override
    public List<Arc> getOwnIncomingArcs() {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getOwnIncomingArcTypes()
     */
    @Override
    public List<Type> getOwnIncomingArcTypes() {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getOwnOutgoingArcTypes()
     */
    @Override
    public List<Type> getOwnOutgoingArcTypes() {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getOwnOutgoingArcs()
     */
    @Override
    public List<Arc> getOwnOutgoingArcs() {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#removeChild(agg.xt_basis.Type)
     */
    @Override
    public void removeChild(Type t) {
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getTypeGraphNode()
     */
    @Override
    public TypeGraphNode getTypeGraphNode() {
        return null;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#getClan()
     */
    @Override
    public List<Type> getClan() {
        return new ArrayList<>(0);
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#hasCommonParentWith(agg.xt_basis.Type)
     */
    @Override
    public boolean hasCommonParentWith(Type t) {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#isInClanOf(agg.xt_basis.Type)
     */
    @Override
    public boolean isInClanOf(Type t) {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#hasChild()
     */
    @Override
    public boolean hasChild() {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#hasParent()
     */
    @Override
    public boolean hasParent() {
        return false;
    }

    /* (non-Javadoc)
	 * @see agg.xt_basis.Type#isParentAttrTypeEmpty()
     */
    @Override
    public boolean isParentAttrTypeEmpty() {
        return true;
    }
}
