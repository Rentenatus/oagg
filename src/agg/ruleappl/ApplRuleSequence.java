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
package agg.ruleappl;

import agg.parser.CriticalPairOption;
import agg.util.Pair;
import agg.xt_basis.BaseFactory;
import agg.xt_basis.GraGra;
import agg.xt_basis.Graph;
import agg.xt_basis.Rule;
import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author olga
 *
 */
public class ApplRuleSequence {

    private String dirName;
    private GraGra gragra;
    private CriticalPairOption cpOption;
    private final List<RuleSequence> ruleSequences = new Vector<RuleSequence>();
//	private final List<Rule> concurrentRules = new Vector<Rule>();

    /**
     *
     */
    public ApplRuleSequence(final CriticalPairOption cpOption) {
        this.cpOption = cpOption;
    }

    public void dispose() {
        clear();
        this.gragra = null;
    }

    public void clear() {
        this.ruleSequences.clear();
    }

    public void setGraGra(final GraGra gra) {
        this.gragra = gra;
        this.ruleSequences.clear();
    }

    /**
     * Returns the critical pair option this applicability container uses
     * when it creates new rule sequences during a load.
     */
    public CriticalPairOption getCPAOption() {
        return this.cpOption;
    }

    public GraGra getGraGra() {
        return this.gragra;
    }

    public boolean isEmpty() {
        return this.ruleSequences.isEmpty();
    }

    public List<RuleSequence> getRuleSequences() {
        return this.ruleSequences;
    }

    public RuleSequence getRuleSequence(final int indx) {
        if (indx >= 0 && indx < this.ruleSequences.size()) {
            return this.ruleSequences.get(indx);
        }
        return null;
    }

    public Pair<Boolean, String> getApplicabilityResult(final int indx) {
        Pair<Boolean, String> result = null;
        if (indx >= 0 && indx < this.ruleSequences.size()) {
            result = this.ruleSequences.get(indx).getApplicabilityResult();
        }
        return result;
    }

    public Pair<Boolean, String> getNonApplicabilityResult(final int indx) {
        Pair<Boolean, String> result = null;
        if (indx >= 0 && indx < this.ruleSequences.size()) {
            result = this.ruleSequences.get(indx).getNonApplicabilityResult();
        }
        return result;
    }

    public boolean removeResult(int indx) {
        if (indx >= 0 && indx < this.ruleSequences.size()) {
            this.ruleSequences.get(indx).uncheck();
            return true;
        }
        return false;
    }
//	private List<Rule> makeRuleList(final List<String> sequence) {
//		final List<Rule> ruleList = new Vector<Rule>();
//		for (int j=0; j<sequence.size(); j++) {
//			Rule rule = gragra.getRule(sequence.get(j));
//			ruleList.add(rule);
//		}
//		return ruleList;
//	}

    public void setRuleSequences(final List<RuleSequence> list) {
        this.ruleSequences.clear();
        for (int i = 0; i < list.size(); i++) {
            this.ruleSequences.add(list.get(i));
        }
    }

    public void setRuleSequence(final RuleSequence sequence) {
        this.ruleSequences.clear();
        this.ruleSequences.add(sequence);
    }

    public RuleSequence addRuleSequence(final List<String> listOfRuleNames, final Graph graph) {
        if (this.gragra == null) {
            return null;
        }
        final RuleSequence rseq = new RuleSequence(
                this.gragra,
                "RuleSequence",
                this.cpOption);
        rseq.setGraph(graph);
        for (int i = 0; i < listOfRuleNames.size(); i++) {
            String rname = listOfRuleNames.get(i);
            Rule rule = this.gragra.getRule(rname);
            if (rule != null) {
                rseq.addRule(rule);
            } else {
                rseq.dispose();
                return null;
            }
        }
        return rseq;
    }

    public void addRuleSequence(final RuleSequence sequence) {
        this.ruleSequences.add(sequence);
    }

    public void addRuleSequenceAt(int indx, final RuleSequence sequence) {
        this.ruleSequences.add(indx, sequence);
    }

    public void setGraphOfRuleSequence(final int indx, final Graph g) {
        if (indx >= 0 && indx < this.ruleSequences.size()) {
            RuleSequence seq = this.ruleSequences.get(indx);
            seq.setGraph(g);
        }
    }

    public boolean removeRuleSequence(final int indx) {
        if (indx >= 0 && indx < this.ruleSequences.size()) {
            RuleSequence seq = this.ruleSequences.get(indx);
            this.ruleSequences.remove(indx);
            seq.dispose();
            return true;
        }
        return false;
    }

    public RuleSequence copyRuleSequence(int seqIndx) {
        if (seqIndx >= 0 && seqIndx < this.ruleSequences.size()) {
            RuleSequence seq = this.ruleSequences.get(seqIndx);
            RuleSequence seqCopy = seq.getCopy();
            seqIndx++;
            if (seqIndx < this.ruleSequences.size()) {
                this.ruleSequences.add(seqIndx, seqCopy);
            } else {
                this.ruleSequences.add(seqCopy);
            }
            return seqCopy;
        }
        return null;
    }

    public void moveRuleSequence(int from, int to) {
        if (from >= 0 && from < this.ruleSequences.size()
                && to >= 0 && to < this.ruleSequences.size()) {
            moveSequence(from, to);
//			System.out.println(ruleSequences.get(to).getRuleNames());
        }
    }

    private void moveSequence(int from, int to) {
        RuleSequence seq = this.ruleSequences.get(from);
        this.ruleSequences.remove(from);
        this.ruleSequences.add(to, seq);
    }

    public void moveRuleInsideSequence(int seqIndx, int from, int to) {
        if (seqIndx >= 0 && seqIndx < this.ruleSequences.size()) {
            RuleSequence seq = this.ruleSequences.get(seqIndx);
            seq.moveRule(from, to);
        }
    }

    public boolean hasChecked(int seqIndx) {
        return this.ruleSequences.get(seqIndx).isChecked();
    }

    public Pair<Boolean, List<String>> getRuleResult(
            final int seqIndx,
            final int indx,
            final String ruleName,
            final String criterion) {
        RuleSequence seq = this.ruleSequences.get(seqIndx);
        return seq.getRuleResult(indx, ruleName, criterion);
    }

    public boolean check(final int seqIndx) {
        if (seqIndx >= 0 && seqIndx < this.ruleSequences.size()) {
            RuleSequence seq = this.ruleSequences.get(seqIndx);
            return this.check(seq);
        }
        return false;
    }

    public boolean check(final RuleSequence sequence) {
        boolean result = sequence.check();
//		sequence.saveConcurrentRules();
        return result;
    }

    public int getIndexOf(final RuleSequence sequence) {
        return this.ruleSequences.indexOf(sequence);
    }

    public void save(final String filename) {
        String rsx = ".rsx";
        String outfileName = "";
        if ("".equals(filename)) {
            outfileName = this.gragra.getFileName().concat("_out.rsx");
        } else {
            outfileName = filename;
            if (outfileName.indexOf(rsx) == -1) {
                outfileName = outfileName.concat(rsx);
            }
        }
        dispatchSave(this, outfileName);
    }

    /**
     * Saves this rule sequence through the DOM path of the agg-xml module
     * (reflective: agg-core must not compile-depend on agg-xml).
     */
    private static void dispatchSave(ApplRuleSequence applSeq, String filename) {
        try {
            Class<?> cls = Class.forName("agg.xml.XMLSerialization");
            java.lang.reflect.Method m = cls.getMethod("saveWithDom",
                ApplRuleSequence.class, String.class);
            m.invoke(null, applSeq, filename);
        } catch (Exception e) {
            System.out.println("Save of rule sequence failed: " + e.getMessage());
        }
    }

    public GraGra load(final String filename) throws Exception {
        File f = new File(filename);
        if (f.exists()) {
            if (filename.endsWith(".rsx")) {
                this.gragra = BaseFactory.theFactory().createGraGra(false);
                if (!dispatchLoad(this, filename)) {
                    throw new Exception("File  \"" + filename
                            + "\"  is not a  \".rsx\"  file!");
                }
                if (f.getParent() == null) {
                    this.dirName = "." + File.separator;
                } else {
                    this.dirName = f.getParent() + File.separator;
                }
            } else {
                throw new Exception("File  \"" + filename
                        + "\"  is not a  \".rsx\"  file!");
            }
        } else {
            throw new Exception("File  \"" + filename
                    + "\"  doesn't exist!");
        }
        return this.gragra;
    }

    private RuleSequence getSequence(String name) {
        for (int i = 0; i < this.ruleSequences.size(); i++) {
            if (this.ruleSequences.get(i).getName().equals(name)) {
                return this.ruleSequences.get(i);
            }
        }
        return null;
    }

    /**
     * Loads this rule sequence through the DOM path of the agg-xml module
     * (reflective: agg-core must not compile-depend on agg-xml).
     */
    private static boolean dispatchLoad(ApplRuleSequence applSeq, String filename) {
        try {
            Class<?> cls = Class.forName("agg.xml.XMLSerialization");
            java.lang.reflect.Method m = cls.getMethod("loadWithDom",
                ApplRuleSequence.class, String.class);
            Object result = m.invoke(null, applSeq, filename);
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }
}
