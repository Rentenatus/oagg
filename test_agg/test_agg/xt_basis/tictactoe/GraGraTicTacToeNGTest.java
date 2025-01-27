/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package test_agg.xt_basis.tictactoe;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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
public class GraGraTicTacToeNGTest {

    public final static String START_GRAPH = "startGraph";

    public GraGraTicTacToeNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start GraGraTicTacToeNGTest.");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End GraGraTicTacToeNGTest.");
        System.out.println("===============================================");
    }

    @Test
    public void testSomeMethod() {
        AggRuleSystem ars = new AggRuleSystem();
        boolean okay = ars.loadSemantics("test_agg/xt_basis/tictactoe/TicTacToeSem.ggx");
        if (!okay) {
            fail("Grama not loaded.");
        }
        okay = ars.useGraph(START_GRAPH);
        if (!okay) {
            fail("Start graph not found.");
        }
        okay = ars.execute("prodSteinB",
                // Auf RB wird von 1 bis 3 (menschnelesbar) nummeriert:
                new ParameterInteger("paramX", 1),
                new ParameterInteger("paramY", 1)
        );
        if (!okay) {
            fail("Can naot set stone.");
        }
        okay = ars.execute("prodSteinA",
                // Auf RB wird von 1 bis 3 (menschnelesbar) nummeriert:
                new ParameterInteger("paramX", 1),
                new ParameterInteger("paramY", 2)
        );
        if (!okay) {
            fail("Can naot set stone.");
        }
        okay = ars.execute("prodSteinB",
                // Auf RB wird von 1 bis 3 (menschnelesbar) nummeriert:
                new ParameterInteger("paramX", 2),
                new ParameterInteger("paramY", 3)
        );
        if (!okay) {
            fail("Can naot set stone.");
        }
        okay = ars.execute("prodSteinA",
                // Auf RB wird von 1 bis 3 (menschnelesbar) nummeriert:
                new ParameterInteger("paramX", 1),
                new ParameterInteger("paramY", 3)
        );
        if (!okay) {
            fail("Can naot set stone.");
        }
        okay = ars.execute("prodSteinB",
                // Auf RB wird von 1 bis 3 (menschnelesbar) nummeriert:
                new ParameterInteger("paramX", 3),
                new ParameterInteger("paramY", 3)
        );
        if (!okay) {
            fail("Can naot set stone.");
        }
        // X..
        // O..
        // OXX

        ars.execute("linieH");
        ars.execute("linieH");
        okay = ars.execute("linieH");
        if (!okay) {
            fail("Line H failed.");
        }
        okay = ars.execute("linieH");
        if (okay) {
            fail("NAC of line H failed.");
        }
        ars.execute("linieV");
        ars.execute("linieV");
        okay = ars.execute("linieV");
        if (!okay) {
            fail("Line V failed.");
        }
        okay = ars.execute("linieV");
        if (okay) {
            fail("NAC of line V failed.");
        }
        ars.execute("linieD1");
        ars.execute("linieD2");

        okay = ars.execute("drohung");
        if (!okay) {
            fail("Search for the threat failed..");
        }
        okay = ars.execute("setzeABlock");
        if (!okay) {
            fail("Block failed..");
        }
        ars.execute("chance");
        okay = ars.execute("chance");
        if (!okay) {
            fail("Search for the chance failed..");
        }
        okay = ars.execute("chance");
        if (okay) {
            fail("NAC of chance failed.");
        }

        Map<String, Object> returnMap = new TreeMap<String, Object>();
        ars.reduce("reduNaechsterZug", returnMap);

        returnMap.forEach((s, ob) -> System.out.println("    > " + s + " :    " + ob));

        int check = 0;
        for (Map.Entry<String, Object> entry : returnMap.entrySet()) {
            if ("drohung".equals(entry.getValue())) {
                String it = entry.getKey().substring(0, 20);
                assertEquals(returnMap.get(it + "Feld.x"), 2);
                assertEquals(returnMap.get(it + "Feld.y"), 2);
                assertEquals(returnMap.get(it + "msg.param"), "F");
                check++;
            }
            if ("setzeABlock".equals(entry.getValue())) {
                String it = entry.getKey().substring(0, 20);
                assertEquals(returnMap.get(it + "Feld.x"), 2);
                assertEquals(returnMap.get(it + "Feld.y"), 2);
                assertEquals(returnMap.get(it + "Workrecord.indexR"), 11);
                check++;
            }
        }
        assertEquals(check, 2);

    }

}
