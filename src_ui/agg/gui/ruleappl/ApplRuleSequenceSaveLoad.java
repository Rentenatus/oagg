/**
 * ***************************************************************************
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 * *****************************************************************************
 */
/**
 *
 */
package agg.gui.ruleappl;

import java.io.File;
import javax.swing.JFileChooser;
import agg.editor.impl.EdGraGra;
import agg.gui.saveload.AGGFileFilter;
import agg.ruleappl.ApplRuleSequence;
import agg.gui.saveload.XmlMigrationDispatch;

/**
 * @author olga
 *
 */
public class ApplRuleSequenceSaveLoad {

    public ApplRuleSequence ars;
    public EdGraGra layout;
    public String dirName = "";
    public String fname = "";
    protected JFileChooser chooser;

    public ApplRuleSequenceSaveLoad() {
    }

    public ApplRuleSequenceSaveLoad(ApplRuleSequence applRuleSeqence, EdGraGra layoutGrammar) {
        this.ars = applRuleSeqence;
        this.layout = layoutGrammar;
    }

    public boolean save() {
        if (this.dirName.equals("")) {
            this.chooser = new JFileChooser(System.getProperty("user.dir"));
        } else {
            this.chooser = new JFileChooser(this.dirName);
        }
        this.chooser.setFileFilter(new AGGFileFilter("rsx", "AGG Files (.rsx)"));
        int returnVal = this.chooser.showSaveDialog(null);
        this.dirName = this.chooser.getCurrentDirectory().toString();
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (this.chooser.getSelectedFile() != null
                    && !this.chooser.getSelectedFile().getName().equals("")) {
                this.fname = this.chooser.getSelectedFile().getName();
            }
        }
        if (!"".equals(this.fname)) {
            if (!this.fname.endsWith(".rsx")) {
                this.fname = this.fname.concat(".rsx");
            }
            return XmlMigrationDispatch.dispatchSave(this.ars, this.layout,
                    this.dirName + File.separator + this.fname);
        }
        return false;
    }

    public void load(ApplRuleSequence applrulesequence) throws Exception {
        if (this.dirName.equals("")) {
            this.chooser = new JFileChooser(System.getProperty("user.dir"));
        } else {
            this.chooser = new JFileChooser(this.dirName);
        }
        this.chooser.setFileFilter(new AGGFileFilter("rsx", "AGG Files (.rsx)"));
        int returnVal = this.chooser.showOpenDialog(null);
        this.dirName = this.chooser.getCurrentDirectory().toString();
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (this.chooser.getSelectedFile() != null
                    && !this.chooser.getSelectedFile().getName().equals("")) {
                this.fname = this.chooser.getSelectedFile().getName();
            }
        }
        if (!"".equals(this.fname)) {
            EdGraGra ed = XmlMigrationDispatch.dispatchLoad(applrulesequence,
                    this.dirName + File.separator + this.fname);
            if (ed != null) {
                this.ars = applrulesequence;
                this.layout = ed;
            }
        }
    }

}
