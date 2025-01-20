/**
 **
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universität Berlin. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 * *****************************************************************************
 */
package agg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import agg.util.csp.BinaryPredicate;
import agg.util.csp.Variable;


/*
 * This ordered set does not allow any duplications.
 */
@SuppressWarnings("serial")
public class OrderedSet<E> implements SortedSet<E> {
    
    private ArrayList<E> list;
    
    @SuppressWarnings("rawtypes")
    Comparator comp;
    BinaryPredicate predicate;
    Iterator<E> iter;
    E obj;
    
    public OrderedSet() {
        super();
        list = new ArrayList<>();
    }
    
    @SuppressWarnings("rawtypes")
    public OrderedSet(Comparator comparator) {
        this();
        this.comp = comparator;
    }
    
    public OrderedSet(BinaryPredicate bp) {
        this();
        this.predicate = bp;
    }
    
    public OrderedSet(Collection<E> col) {
        this();
        
        Iterator<E> iter = col.iterator();
        while (iter.hasNext()) {
            this.add(iter.next());
        }
    }
    
    @SuppressWarnings("rawtypes")
    public OrderedSet(Collection<E> col, Comparator comparator) {
        this(comparator);
        
        Iterator<E> iter = col.iterator();
        while (iter.hasNext()) {
            this.add(iter.next());
        }
    }
    
    public OrderedSet(SortedSet<E> ss) {
        super();
        
        this.comp = ss.comparator();
        Iterator<E> iter = ss.iterator();
        while (iter.hasNext()) {
            this.add(iter.next());
        }
    }
    
    public synchronized boolean add(E e) {
        boolean res = false;
        if (this.isEmpty()) {
            res = list.add(e);
        } else if (this.comp != null) {
            res = addByComparator(e);
        } else if (this.predicate != null) {
            res = addByPredicate(e);
        } else if (!list.contains(e)) {
            res = list.add(e);
        }
        return res;
    }
    
    public boolean remove(Object o) {
        int i = (this.iter != null && this.obj != null) ? list.indexOf(this.obj) : -1;
        int i1 = list.indexOf(o);
        
        boolean res = list.remove(o);
        if (res) {
            if (i == -1 && this.iter != null) {
                this.start();
            } else if (i == 0 && i1 == 0) {
                this.start();
            } else if (i > 0) {
                if (i >= i1) {
                    this.start();
                    for (int j = 0; j <= i - 1; j++) {
                        this.get();
                    }
                } else if (i < i1) {
                    this.start();
                    for (int j = 0; j <= i; j++) {
                        this.get();
                    }
                }
            }
        }
        return res;
    }
    
    private boolean addByPredicate(E e) {
        for (int i = this.size() - 1; i >= 0; i--) {
            if (this.predicate.execute(e, list.get(i))) {
                if (i == this.size() - 1) {
                    return list.add(e);
                } else {
                    list.add(i + 1, e);
                    return true;
                }
            } else if (i > 0 && this.predicate.execute(e, list.get(i - 1))) {
                list.add(i, e);
                return true;
            } else if (i == 0) {
                list.add(i, e);
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private boolean addByComparator(E e) {
        for (int i = this.size() - 1; i >= 0; i--) {
            int c = this.comp.compare(e, list.get(i));
            if (c == 0) {
                return false;
            }
            if (c > 0) {
                if (i == this.size() - 1) {
                    return list.add(e);
                } else {
                    list.add(i + 1, e);
                    return true;
                }
            } else if (i > 0 && this.comp.compare(e, list.get(i - 1)) > 0) {
                list.add(i, e);
                return true;
            } else if (i == 0) {
                list.add(i, e);
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public Comparator<? super E> comparator() {
        return this.comp;
    }
    
    public BinaryPredicate binaryPredicate() {
        return this.predicate;
    }
    
    public SortedSet<E> subSet(E fromElement, E toElement) {
        OrderedSet<E> set = new OrderedSet<E>();
        int start = list.indexOf(fromElement);
        int end = list.indexOf(toElement);
        for (int i = start; i <= end; i++) {
            set.add(list.get(i));
        }
        set.comp = this.comp;
        set.predicate = this.predicate;
        return set;
    }
    
    public SortedSet<E> headSet(E toElement) {
        OrderedSet<E> set = new OrderedSet<E>(this.comp);
        int end = list.indexOf(toElement);
        for (int i = 0; i <= end; i++) {
            set.add(list.get(i));
        }
        set.comp = this.comp;
        set.predicate = this.predicate;
        return set;
    }
    
    public SortedSet<E> tailSet(E fromElement) {
        OrderedSet<E> set = new OrderedSet<E>();
        int start = list.indexOf(fromElement);
        for (int i = start; i < this.size(); i++) {
            set.add(list.get(i));
        }
        set.comp = this.comp;
        set.predicate = this.predicate;
        return set;
    }
    
    public E first() {
        return !this.isEmpty() ? list.get(0) : null;
    }
    
    public E last() {
        return !this.isEmpty() ? list.get(this.size() - 1) : null;
    }
    
    public void start() {
        this.iter = this.iterator();
        this.obj = null;
    }
    
    public E get() {
        this.obj = this.iter.hasNext() ? this.iter.next() : null;
        return this.obj;
    }
    
    public boolean hasNext() {
        if (this.iter == null) {
            this.start();
        }
        return this.iter.hasNext() ? true : false;
    }
    
    public OrderedSet<E> union(OrderedSet<E> os) {
        OrderedSet<E> set = new OrderedSet<E>();
        for (int i = 0; i < this.size(); i++) {
            set.add(list.get(i));
        }
        for (int i = 0; i < os.size(); i++) {
            set.add(os.get(i));
        }
        set.comp = this.comp;
        set.predicate = this.predicate;
        return set;
    }
    
    public E get(int i) {
        return list.get(i);
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }
    
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }
    
    @Override
    public Object[] toArray() {
        return list.toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E element : c) {
            if (this.add(element)) {
                modified = true;
            }
        }
        return modified;
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<E> iterator = this.iterator();
        while (iterator.hasNext()) {
            E element = iterator.next();
            if (!c.contains(element)) {
                iterator.remove();
                modified = true;
            }
        }
        return modified;
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }
    
    @Override
    public void clear() {
        list = new ArrayList<>();
    }
    
    public int indexOf(Variable aVar, int i) {
        return list.indexOf(i);
    }
    
}
