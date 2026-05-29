/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package test_agg.xt_basis;

import agg.util.OrderedSet;
import agg.util.csp.BinaryPredicate;
import de.jare.ndimcol.ref.IteratorWalker;
import de.jare.ndimcol.ref.SortedSeasonSet;
import java.util.Iterator;
import java.util.function.BiPredicate;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Janusch Rentenatus
 */
public class SortedSeasonSetNGTest {

    class CoInteger {

        Integer i;

        public CoInteger(int i) {
            this.i = i;
        }

        @Override
        public boolean equals(Object ob) {
            return i.equals(ob);
        }

        @Override
        public String toString() {
            return i.toString();
        }

        private int intValue() {
            return i.intValue();
        }

    }

    class BinaryPredicateCoInteger implements BinaryPredicate {

        @Override
        public boolean execute(Object q1, Object q2) {
            CoInteger o1 = (CoInteger) q1;
            CoInteger o2 = (CoInteger) q2;
            return o2.intValue() > o1.intValue();
        }

    }

    class BiPredicateCoInteger implements BiPredicate<CoInteger, CoInteger> {

        /**
         * Evaluates this predicate e2 greater as e1.
         *
         *
         * @param o1 the first input argument
         * @param o2 the second input argument
         * @return {@code true} if the input arguments match the predicate, otherwise {@code false}
         *
         */
        @Override
        public boolean test(CoInteger o1, CoInteger o2) {
            return o2.intValue() > o1.intValue();
        }

    }

    public SortedSeasonSetNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start SortedSeasonSetNGTest.");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End SortedSeasonSetNGTest.");
        System.out.println("===============================================");
    }

    @Test
    public void testSomeMethod() {
        SortedSeasonSet<CoInteger> set = new SortedSeasonSet(new BiPredicateCoInteger());
        OrderedSet<CoInteger> ord = new OrderedSet<>(new BinaryPredicateCoInteger());
        CoInteger[] src = {new CoInteger(2),
            new CoInteger(7),
            new CoInteger(0),
            new CoInteger(2),
            new CoInteger(3),
            new CoInteger(7)
        };
        for (int i = 0; i < src.length; i++) {
            set.add(src[i]);
            ord.add(src[i]);
            System.out.println(src[i] + "  -->  " + src[i].hashCode());
        }
        for (int i = 0; i < src.length; i++) {
            set.add(src[i]);
            ord.add(src[i]);
            System.out.println(src[i] + "  -->  " + src[i].hashCode());
        }
        System.out.println("--- SortedSeasonSet");
        IteratorWalker<CoInteger> walker = set.softWalker();
        while (walker.hasNext()) {
            CoInteger ob = walker.next();
            System.out.println(ob + "  -->  " + ob.hashCode());
        }
        System.out.println("--- OrderedSet");
        Iterator<CoInteger> itereator = ord.iterator();
        while (itereator.hasNext()) {
            CoInteger ob = itereator.next();
            System.out.println(ob + "  -->  " + ob.hashCode());
        }
        assertEquals(set.get(0).hashCode(), src[2].hashCode());
        assertEquals(set.get(1).hashCode(), src[0].hashCode());
        assertEquals(set.get(2).hashCode(), src[4].hashCode());
        assertEquals(set.get(3).hashCode(), src[1].hashCode());
    }

}
