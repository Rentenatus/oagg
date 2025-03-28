/**
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universität Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.gui.treeview.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class GraphImportDialog extends JDialog implements ActionListener {

    private final JPanel contentPane;

    private final JPanel panel;

    private final JPanel itemPanel;

    private final JPanel buttonPanel;

    private final JScrollPane scrollPane;

    private final List<String> itemNames;

    private final List<String> result;

    private final List<JCheckBox> checkBox;

    private final JButton allItemsButton;

    private final JButton closeButton;

    private final JButton cancelButton;

//	private boolean isCancelled;
    private boolean singleSelection;

    public GraphImportDialog(JFrame parent, String title,
            List<String> itemNames, boolean singleSelection) {
        super(parent, true);
        this.itemNames = itemNames;
        this.singleSelection = singleSelection;

        this.result = new Vector<String>();

        setTitle(title);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                setVisible(false);
                dispose();
            }
        });
        if (parent != null) {
            setLocationRelativeTo(parent);
        } else {
            setLocation(300, 100);
        }

        this.checkBox = new Vector<JCheckBox>();

        this.contentPane = new JPanel(new BorderLayout());
        this.contentPane.setBackground(Color.lightGray);

        this.panel = new JPanel(new BorderLayout());
        this.panel.setBorder(new TitledBorder(" Please select an item "));

        this.itemPanel = new JPanel(new GridLayout(itemNames.size(), 1));
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < itemNames.size(); i++) {
            String name = itemNames.get(i);
            JCheckBox cb = new JCheckBox(name, null, false);
            cb.addActionListener(this);

            if (this.singleSelection) {
                group.add(cb);
            }

            this.checkBox.add(cb);
            this.itemPanel.add(cb);
        }

        this.scrollPane = new JScrollPane(this.itemPanel);
        this.scrollPane.setPreferredSize(new Dimension(100, 150));
        this.panel.add(this.scrollPane, BorderLayout.CENTER);

        this.allItemsButton = new JButton();
        this.allItemsButton.setActionCommand("allItems");
        this.allItemsButton.setText("Select All");
        this.allItemsButton.addActionListener(this);
        if (!this.singleSelection) {
            this.panel.add(this.allItemsButton, BorderLayout.SOUTH);
        }

        this.buttonPanel = new JPanel(new GridBagLayout());
        this.closeButton = new JButton();
        this.closeButton.setActionCommand("close");
        this.closeButton.setText("Import");
        this.closeButton.addActionListener(this);

        this.cancelButton = new JButton();
//		isCancelled = false;
        this.cancelButton.setActionCommand("cancel");
        this.cancelButton.setText("Cancel");
        this.cancelButton.addActionListener(this);

        constrainBuild(this.buttonPanel, this.closeButton, 0, 0, 1, 1,
                GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1.0, 0.0,
                5, 10, 10, 5);
        constrainBuild(this.buttonPanel, this.cancelButton, 1, 0, 1, 1,
                GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1.0, 0.0,
                5, 5, 10, 10);

        this.contentPane.add(this.panel, BorderLayout.CENTER);
        this.contentPane.add(this.buttonPanel, BorderLayout.SOUTH);
        this.contentPane.revalidate();

        setContentPane(this.contentPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        validate();
        pack();
    }

    /**
     * This handles the clicks on the different buttons.
     *
     * @param e The event from the buttons.
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this.allItemsButton) {
            for (int j = 0; j < this.checkBox.size(); j++) {
                JCheckBox cb = this.checkBox.get(j);
                cb.setSelected(true);
            }
        } else if (source == this.closeButton) {
            for (int j = 0; j < this.checkBox.size(); j++) {
                JCheckBox cb = this.checkBox.get(j);
                if (cb.isSelected()) {
                    this.result.add(this.itemNames.get(j));
                }
            }
            setVisible(false);
            dispose();
        } else if (source == this.cancelButton) {
//			isCancelled = true;
            setVisible(false);
            dispose();
        }
    }

    public List<String> getSelectedItemNames() {
        return this.result;
    }

    // constrainBuild() method
    private void constrainBuild(Container container, Component component,
            int grid_x, int grid_y, int grid_width, int grid_height, int fill,
            int anchor, double weight_x, double weight_y, int top, int left,
            int bottom, int right) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = grid_x;
        c.gridy = grid_y;
        c.gridwidth = grid_width;
        c.gridheight = grid_height;
        c.fill = fill;
        c.anchor = anchor;
        c.weightx = weight_x;
        c.weighty = weight_y;
        c.insets = new Insets(top, left, bottom, right);
        ((GridBagLayout) container.getLayout()).setConstraints(component, c);
        container.add(component);
    }
}
