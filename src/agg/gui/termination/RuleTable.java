/**
 **
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universität Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 ******************************************************************************
 */
package agg.gui.termination;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class RuleTable extends JPanel {

    private int hght;

    public RuleTable(Vector<String> rules, String title) {
        super(new BorderLayout());
        setBackground(Color.orange);
        setBorder(new TitledBorder(title));

        JPanel rulePanel = new JPanel(new BorderLayout());
        JTable ruleTable = new JTable(rules.size(), 1);
        ruleTable.setEnabled(false);
        for (int i = 0; i < rules.size(); i++) {
            ruleTable.setValueAt(rules.elementAt(i), i, 0);
        }

        this.hght = getHeight(ruleTable.getRowCount(), ruleTable.getRowHeight());
        ruleTable.doLayout();
        JScrollPane ruleScrollPane = new JScrollPane(ruleTable);
        ruleScrollPane.setPreferredSize(new Dimension(200, this.hght));
        rulePanel.add(ruleScrollPane, BorderLayout.CENTER);
        add(rulePanel, BorderLayout.CENTER);
        validate();
    }

    public Dimension getPreferredSize() {
        return new Dimension(200, this.hght);
    }

    public int getTableHeight() {
        return this.hght;
    }

    private int getHeight(int rowCount, int rowHeight) {
        int n = 15;
        int h = (rowCount + 3) * rowHeight;
        if (rowCount > n) {
            h = (n + 2) * rowHeight;
        } // else if(rowCount < 5)
        // h = 6*rowHeight;
        else if (rowCount == 0) {
            h = 2 * rowHeight;
        }
        return h;
    }
}
