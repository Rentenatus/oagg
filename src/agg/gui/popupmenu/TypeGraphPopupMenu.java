/**
 **
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universität Berlin. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright> *****************************************************************************
 */
// $Id: TypeGraphPopupMenu.java,v 1.2 2010/09/23 08:21:34 olga Exp $
package agg.gui.popupmenu;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import agg.editor.impl.EdGraGra;
import agg.editor.impl.EdGraph;
import agg.gui.treeview.GraGraTreeView;
import agg.gui.treeview.nodedata.GraGraTextualComment;
import agg.xt_basis.TypeSet;

/**
 * This context menu displayed on top of the type graph. Within this menu one can select which level of type graph check
 * should be applied to all other graphs. The menu actions will be processed in {@link GraGraTreeView}.
 *
 * @author $Author: olga $
 * @version $Id: TypeGraphPopupMenu.java,v 1.2 2010/09/23 08:21:34 olga Exp $
 */
@SuppressWarnings("serial")
public class TypeGraphPopupMenu extends JPopupMenu {

    public TypeGraphPopupMenu(GraGraTreeView tree) {
        super("Type Graph");

        this.treeView = tree;

        ButtonGroup states = new ButtonGroup();

        this.disabled = new JRadioButtonMenuItem("disabled");
        this.disabled.setActionCommand("checkTypeGraph.DISABLED");
//		this.disabled.addActionListener(this.treeView);
        this.disabled.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //setTypeGraphLevel(TypeSet.DISABLED);
            }
        });
        states.add(this.disabled);
        this.add(this.disabled);

        this.addSeparator();

        JMenuItem mi = add(new JMenuItem("Delete                  Delete"));
        mi.setActionCommand("deleteTypeGraph");
//		mi.addActionListener(this.treeView);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (treeView.hasMultipleSelection()) {
                    treeView.delete("selected");
                
                }
            }
        });

        addSeparator();

        mi = add(new JMenuItem("Textual Comments"));
        mi.setActionCommand("commentTypeGraph");
//		mi.addActionListener(this.treeView);
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editComments();
            }
        });
        this.pack();
        this.setBorderPainted(true);
    }

    public boolean invoked(int x, int y) {
        // no Tree?
        if (this.treeView == null) {
            return false;
        }

        // not an object in the Tree
        if (this.treeView.getTree().getRowForLocation(x, y) != -1) {
            // is it a level 3 object;
            if (this.treeView.getTree().getPathForLocation(x, y).getPath().length == 3) {
                // get the type graph there, if there is one
                this.path = this.treeView.getTree().getPathForLocation(x, y);
                this.node = (DefaultMutableTreeNode) this.path.getLastPathComponent();
                this.graph = this.treeView.getGraph(this.node);
                // get the gragra for the graph
                EdGraGra gragra = this.treeView
                        .getGraGra((DefaultMutableTreeNode) this.path.getPathComponent(1));

                // is this the type graph?
                if ((graph != null) && (graph.isTypeGraph())
                        && (gragra != null)) {
                    // select the actual level
                    int level = gragra.getLevelOfTypeGraphCheck();
                    if (level == TypeSet.DISABLED) {
                        this.disabled.setSelected(true);

                    } else {
                        // unknown level?
                        this.disabled.setSelected(false);

                    }
                    // this.treeView.selectPath(x,y);
                    return true;
                }// if graph!=null
            }// if length!=3
        }// if row != -1
        return false;
    }// invoked

    void editComments() {
        if (graph != null) {
            this.treeView.cancelCommentsEdit();
            Point p = this.treeView.getPopupMenuLocation();
            if (p == null) {
                p = new Point(200, 200);
            }
            GraGraTextualComment comments = new GraGraTextualComment(this.treeView.getFrame(), p.x,
                    p.y, graph.getBasisGraph());

            if (comments != null) {
                comments.setVisible(true);
            }
        }
    }

    private JMenuItem disabled;

    GraGraTreeView treeView;
    TreePath path;
    DefaultMutableTreeNode node;
    EdGraph graph;

}
