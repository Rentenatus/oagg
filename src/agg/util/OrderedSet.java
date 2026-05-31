/**
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package agg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import agg.util.csp.BinaryPredicate;
import agg.util.csp.Variable;

/**
 * An ordered set implementation that maintains elements in a specific order
 * based on a BinaryPredicate. This set does not allow duplicates.
 *
 * <p>
 * <strong>Note:</strong> This implementation is not thread-safe. For
 * thread-safe operations, external synchronization must be provided.</p>
 *
 * <p>
 * The ordering is maintained through a BinaryPredicate that determines the
 * relative position of elements. If no predicate is provided, elements are
 * stored in insertion order (like a LinkedHashSet).</p>
 *
 * @param <E> the type of elements in this set
 * @author unknown
 * @author Janusch Rentenatus
 */
@SuppressWarnings("serial")
public class OrderedSet<E> implements SortedSet<E> {

    private final ArrayList<E> list;
    @SuppressWarnings("rawtypes")
    private final BinaryPredicate predicate;

    /**
     * Creates an empty OrderedSet with no ordering predicate. Elements will be
     * stored in insertion order.
     */
    public OrderedSet() {
        this.list = new ArrayList<>();
        this.predicate = null;
    }

    /**
     * Creates an empty OrderedSet with the specified ordering predicate.
     *
     * @param bp the binary predicate used to determine element ordering
     */
    @SuppressWarnings("rawtypes")
    public OrderedSet(BinaryPredicate bp) {
        this.list = new ArrayList<>();
        this.predicate = bp;
    }

    /**
     * Creates an OrderedSet containing all elements from the specified
     * collection.
     *
     * @param col the collection whose elements are to be placed into this set
     */
    public OrderedSet(Collection<E> col) {
        this();
        for (E element : col) {
            this.add(element);
        }
    }

    /**
     * Creates an OrderedSet containing all elements from the specified sorted
     * set.
     *
     * @param ss the sorted set whose elements are to be placed into this set
     */
    public OrderedSet(SortedSet<E> ss) {
        this();
        for (E element : ss) {
            this.add(element);
        }
    }

    /**
     * Adds the specified element to this set if it is not already present. The
     * element is inserted at the position determined by the predicate, or at
     * the end if no predicate is set.
     *
     * @param e the element to be added
     * @return true if this set did not already contain the specified element
     */
    @Override
    public boolean add(E e) {
        if (this.isEmpty()) {
            return list.add(e);
        } else if (this.predicate != null) {
            return addByPredicate(e);
        } else if (!list.contains(e)) {
            return list.add(e);
        }
        return false;
    }

    /**
     * Inserts an element at the correct position based on the predicate. The
     * predicate.execute(e, existingElement) should return true if e should come
     * after existingElement.
     *
     * @param e the element to insert
     * @return true if the element was added
     */
    private boolean addByPredicate(E e) {
        // Check for duplicates first
        if (list.contains(e)) {
            return false;
        }
        // Find the correct insertion point
        for (int i = this.size() - 1; i >= 0; i--) {
            if (this.predicate.execute(e, list.get(i))) {
                // e should come after list.get(i)
                if (i == this.size() - 1) {
                    return list.add(e);
                } else {
                    list.add(i + 1, e);
                    return true;
                }
            } else if (i > 0 && this.predicate.execute(e, list.get(i - 1))) {
                // e should come after list.get(i-1) but before list.get(i)
                list.add(i, e);
                return true;
            } else if (i == 0) {
                // e should come before all existing elements
                list.add(i, e);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o the element to be removed
     * @return true if this set contained the specified element
     */
    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public Comparator<? super E> comparator() {
        // This implementation uses BinaryPredicate for ordering, not Comparator
        // Return null to indicate natural ordering (though we use predicate internally)
        return null;
    }

    /**
     * Returns the binary predicate used for ordering, or null if using
     * insertion order.
     *
     * @return the binary predicate, or null
     */
    public BinaryPredicate binaryPredicate() {
        return this.predicate;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        OrderedSet<E> set = new OrderedSet<>();
        int start = list.indexOf(fromElement);
        int end = list.indexOf(toElement);
        if (start >= 0 && end >= 0 && start <= end) {
            for (int i = start; i <= end; i++) {
                set.add(list.get(i));
            }
        }
        return set;
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        OrderedSet<E> set = new OrderedSet<>(this.predicate);
        int end = list.indexOf(toElement);
        if (end >= 0) {
            for (int i = 0; i <= end; i++) {
                set.add(list.get(i));
            }
        }
        return set;
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        OrderedSet<E> set = new OrderedSet<>();
        int start = list.indexOf(fromElement);
        if (start >= 0) {
            for (int i = start; i < this.size(); i++) {
                set.add(list.get(i));
            }
        }
        return set;
    }

    @Override
    public E first() {
        return !this.isEmpty() ? list.get(0) : null;
    }

    @Override
    public E last() {
        return !this.isEmpty() ? list.get(this.size() - 1) : null;
    }

    /**
     * Returns the element at the specified position in this set. This is an
     * extension method not part of the SortedSet interface.
     *
     * @param i the index of the element to return
     * @return the element at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public E get(int i) {
        return list.get(i);
    }
    // ==================== Legacy Iterator Methods ====================
    // These methods maintain backward compatibility with the old stateful iterator approach.
    // They are deprecated in favor of using standard iterator() method.
    private Iterator<E> legacyIterator;
    private E legacyCurrent;

    /**
     * <b>Deprecated:</b> Use {@link #iterator()} instead. Starts iteration from
     * the beginning. Each call creates a new independent iterator.
     */
    @Deprecated
    public void start() {
        this.legacyIterator = this.iterator();
        this.legacyCurrent = null;
    }

    /**
     * <b>Deprecated:</b> Use {@link #iterator()} instead. Returns the next
     * element in the iteration. Call {@link #start()} before using this method.
     *
     * @return the next element, or null if no more elements
     */
    @Deprecated
    public E get() {
        this.legacyCurrent = this.legacyIterator.hasNext() ? this.legacyIterator.next() : null;
        return this.legacyCurrent;
    }

    /**
     * <b>Deprecated:</b> Use {@link #iterator()} instead. Checks if there are
     * more elements in the iteration. Call {@link #start()} before using this
     * method.
     *
     * @return true if there are more elements
     */
    @Deprecated
    public boolean hasNext() {
        if (this.legacyIterator == null) {
            this.start();
        }
        return this.legacyIterator.hasNext();
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
        // Return a new independent iterator each time
        return new ArrayList<>(list).iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
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
        list.clear();
    }

    /**
     * Returns the index of the specified element in this set.
     *
     * @param o the element to search for
     * @return the index of the element, or -1 if not found
     */
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    /**
     * Creates a union of this set with the specified OrderedSet. Elements from
     * both sets are combined, maintaining the ordering predicate.
     *
     * @param os the other OrderedSet to union with
     * @return a new OrderedSet containing all elements from both sets
     */
    public OrderedSet<E> union(OrderedSet<E> os) {
        OrderedSet<E> set = new OrderedSet<>();
        for (int i = 0; i < this.size(); i++) {
            set.add(list.get(i));
        }
        for (int i = 0; i < os.size(); i++) {
            set.add(os.get(i));
        }
        return set;
    }

    /**
     * <b>Legacy method:</b> Returns the index of the specified element in this
     * set. The second parameter is ignored for backward compatibility.
     *
     * @param aVar the element to search for
     * @param i ignored parameter (kept for backward compatibility)
     * @return the index of the element, or -1 if not found
     */
    public int indexOf(Variable aVar, int i) {
        return list.indexOf(aVar);
    }
}
