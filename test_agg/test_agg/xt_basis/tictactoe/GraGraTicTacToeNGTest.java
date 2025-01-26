/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package test_agg.xt_basis.tictactoe;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test_agg.rulesystem.AggRuleSystem;

/**
 *
 * @author Janusch Rentenatus
 */
public class GraGraTicTacToeNGTest {

    public GraGraTicTacToeNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testSomeMethod() {
        AggRuleSystem ars = new AggRuleSystem();
        boolean okay = ars.loadSemantics("test_agg/xt_basis/tictactoe/TicTacToeSem.ggx");
        if (!okay) {
            fail("Grama not loaded.");
        }
    }

}
