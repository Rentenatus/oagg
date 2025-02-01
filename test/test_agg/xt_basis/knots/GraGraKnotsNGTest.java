/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package test_agg.xt_basis.knots;

import java.util.Map;
import java.util.TreeMap;
import test_agg.xt_basis.tictactoe.*;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test_agg.rulesystem.AggRuleSystem;
import test_agg.rulesystem.param.ParameterInteger;

/**
 *
 * @author Janusch Rentenatus
 */
public class GraGraKnotsNGTest {

    public final static String START_GRAPH = "startGraph";

    public GraGraKnotsNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start GraGraKnotsNGTest.");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End GraGraKnotsNGTest.");
        System.out.println("===============================================");
    }

    @Test
    public void testSomeMethod() {
        AggRuleSystem ars = new AggRuleSystem();
        boolean okay = ars.loadSemantics("test_agg/xt_basis/knots/KnotsSem.ggx");
        if (!okay) {
            fail("Grama not loaded.");
        }
        okay = ars.useGraph(START_GRAPH);
        if (!okay) {
            fail("Start graph not found.");
        }
        okay = ars.execute("prodRope");
        if (!okay) {
            fail("Can naot set rope.");
        }

        okay = ars.execute("prodBand");
        if (!okay) {
            fail("Can naot set band.");
        }

        okay = ars.execute("thinkReidermeister1p");
        if (!okay) {
            fail("thinkReidermeister1p failed.");
        }
        okay = ars.execute("thinkReidermeister2m");
        if (okay) {
            fail("Unexpected thinkReidermeister2m.");
        }
        okay = ars.execute("thinkReidermeister2p");
        if (!okay) {
            fail("thinkReidermeister2p failed.");
        }

    }

}
