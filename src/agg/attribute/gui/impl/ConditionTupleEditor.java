/*******************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universität Berlin. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 *******************************************************************************/
package agg.attribute.gui.impl;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JToolBar;

import agg.attribute.AttrManager;
import agg.attribute.gui.AttrEditorManager;

/**
 * Editor for a condition tuple.
 * 
 * @version $Id: ConditionTupleEditor.java,v 1.3 2010/09/20 14:27:59 olga Exp $
 * @author $Author: olga $
 */
public class ConditionTupleEditor extends TabMesTool_TupleEditor {

	public ConditionTupleEditor(AttrManager m, AttrEditorManager em) {
		super(m, em);
	}

	//
	// Overriding...

	/**
	 * The heart of the matter. Columns are: [ EXPR, CORRECTNESS ]. Extendable:
	 * true. Titles: default. Editable: Only EXPR.
	 */
	protected TupleTableModel createTableModel() {
		int columns[] = { EXPR, CORRECTNESS };
		TupleTableModel tm = new TupleTableModel(this);
		tm.setColumnArray(columns);
		tm.setExtensible(true);
		return tm;
	}

	/** Short tool bar because of less space. */
	protected void createToolBar() {
		JToolBar toolBar1 = new JToolBar();
		toolBar1.setFloatable(false);
		toolBar1.add(getResetAction());
		toolBar1.addSeparator();
		toolBar1.add(getDeleteAction());
		this.toolBarPanel = new JPanel(new BorderLayout());
		this.toolBarPanel.add(toolBar1, BorderLayout.CENTER);
	}
}
/*
 * $Log: ConditionTupleEditor.java,v $
 * Revision 1.3  2010/09/20 14:27:59  olga
 * tuning
 *
 * Revision 1.2  2007/09/10 13:05:30  olga
 * In this update:
 * - package xerces2.5.0 is not used anymore;
 * - class com.objectspace.jgl.Pair is replaced by the agg own generic class agg.util.Pair;
 * - bugs fixed in:  usage of PACs in rules;  match completion;
 * 	usage of static method calls in attr. conditions
 * - graph editing: added some new features
 * Revision 1.1 2005/08/25 11:56:58 enrico
 * *** empty log message ***
 * 
 * Revision 1.1 2005/05/30 12:58:04 olga Version with Eclipse
 * 
 * Revision 1.3 2003/03/05 18:24:11 komm sorted/optimized import statements
 * 
 * Revision 1.2 2002/09/23 12:23:49 komm added type graph in xt_basis, editor
 * and GUI
 * 
 * Revision 1.1.1.1 2002/07/11 12:16:57 olga Imported sources
 * 
 * Revision 1.5 2000/04/05 12:07:42 shultzke serialVersionUID aus V1.0.0
 * generiert
 * 
 */
