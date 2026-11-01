/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.ui;

import agg.editor.impl.EdGraph;
import agg.editor.impl.EdGraGra;
import agg.gui.cpa.ConflictsDependenciesContainerSaveLoad;
import agg.parser.ConflictsDependenciesContainer;
import agg.parser.PairContainer;
import agg.ruleappl.ApplRuleSequence;
import agg.util.XMLHelper;
import agg.xt_basis.BaseFactory;
import agg.xt_basis.GraGra;
import agg.xml.XMLSerialization;
import agg.xml.migration.GraGraMigration;

/**
 * Feature flag facade for the GUI save/load entry points of all three file
 * formats: with the migration flag enabled it uses the DOM-based
 * serialization ({@link UISerialization} for the editor layer,
 * {@link XMLSerialization} for the core objects), otherwise the legacy
 * {@link XMLHelper} path exactly as the GUI classes run it today.
 *
 * <p>The GUI layer (agg-ui) calls these methods through reflection
 * ({@code agg.gui.saveload.XmlMigrationDispatch}), because agg-ui must not
 * compile-depend on agg-ui-xml: the frozen legacy clone keeps a clean
 * classpath that way, and a deployment without the agg-ui-xml jar simply
 * stays on the legacy path.</p>
 */
public final class UIMigration {

    private UIMigration() {
    }

    /**
     * Returns whether the DOM path is active for core and GUI.
     *
     * @return true if the DOM path is enabled
     */
    public static boolean isUseNewXml() {
        return GraGraMigration.isUseNewXml();
    }

    /**
     * Enables or disables the DOM path for core and GUI together.
     *
     * @param enabled the new flag state
     */
    public static void setUseNewXml(boolean enabled) {
        GraGraMigration.setUseNewXml(enabled);
    }

    // ---- .ggx with the UI layer (EdGraGra top object, like GraGraSave/GraGraLoad) ----

    /**
     * Saves an EdGraGra like {@code GraGraSave.save}: DOM path with the
     * flag on, legacy XMLHelper path otherwise.
     *
     * @param edGraGra The editor grammar to save
     * @param filename The output filename (.ggx)
     * @return true if saving succeeded
     */
    public static boolean save(EdGraGra edGraGra, String filename) {
        if (edGraGra == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            return UISerialization.saveWithDom(edGraGra, filename);
        }
        String outfileName = filename;
        if (outfileName.indexOf(".ggx") == -1) {
            outfileName = outfileName.concat(".ggx");
        }
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(edGraGra);
        return helper.save_to_xml(outfileName);
    }

    /**
     * Loads an EdGraGra like {@code GraGraLoad.reload}: DOM path with the
     * flag on, legacy XMLHelper path otherwise. Returns null on failure.
     *
     * @param filename The input filename (.ggx)
     * @return the loaded editor grammar, or null if loading failed
     */
    public static EdGraGra load(String filename) {
        if (filename == null) {
            return null;
        }
        if (isUseNewXml()) {
            try {
                return UISerialization.loadWithDom(filename);
            } catch (Exception e) {
                return null;
            }
        }
        XMLHelper helper = new XMLHelper();
        if (helper.read_from_xml(filename)) {
            GraGra bgra = BaseFactory.theFactory().createGraGra(false);
            helper.getTopObject(bgra);
            EdGraGra ed = new EdGraGra(bgra);
            helper.enrichObject(ed);
            return ed;
        }
        return null;
    }

    // ---- .ggx core only (GraGra top object, like GraGraSave.saveBase/GraGraLoad.reloadBase) ----

    /**
     * Saves a GraGra like {@code GraGraSave.saveBase}: flag-dispatched
     * through the core migration.
     *
     * @param graGra The grammar to save
     * @param filename The output filename (.ggx)
     * @return true if saving succeeded
     */
    public static boolean save(GraGra graGra, String filename) {
        if (graGra == null || filename == null) {
            return false;
        }
        return XMLSerialization.save(graGra, filename);
    }

    /**
     * Loads a GraGra like {@code GraGraLoad.reloadBase}: flag-dispatched
     * through the core migration.
     *
     * @param graGra The grammar instance to populate
     * @param filename The input filename (.ggx)
     * @return true if loading succeeded
     */
    public static boolean load(GraGra graGra, String filename) {
        if (graGra == null || filename == null) {
            return false;
        }
        try {
            return XMLSerialization.load(graGra, filename);
        } catch (Exception e) {
            return false;
        }
    }

    // ---- .cpx (ConflictsDependenciesContainer, like ConflictsDependenciesContainerSaveLoad) ----

    /**
     * Saves a ConflictsDependenciesContainer: DOM path with the flag on,
     * legacy XMLHelper path otherwise.
     *
     * @param container The container to save
     * @param filename The output filename (.cpx)
     * @return true if saving succeeded
     */
    public static boolean save(ConflictsDependenciesContainer container, String filename) {
        if (container == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            return XMLSerialization.saveWithDom(container, filename);
        }
        String outfileName = filename;
        if (outfileName.indexOf(".cpx") == -1) {
            outfileName = outfileName.concat(".cpx");
        }
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(container);
        return helper.save_to_xml(outfileName);
    }

    /**
     * Loads a ConflictsDependenciesContainer: DOM path with the flag on,
     * legacy XMLHelper path otherwise.
     *
     * @param container The container instance to populate
     * @param filename The input filename (.cpx)
     * @return true if loading succeeded
     */
    public static boolean load(ConflictsDependenciesContainer container, String filename) {
        if (container == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            try {
                return XMLSerialization.loadWithDom(container, filename);
            } catch (Exception e) {
                return false;
            }
        }
        XMLHelper helper = new XMLHelper();
        if (helper.read_from_xml(filename)) {
            helper.getTopObject(container);
            return true;
        }
        return false;
    }

    // ---- UI .cpx (ConflictsDependenciesContainerSaveLoad like PairIOGUI) ----

    /**
     * Saves a critical pairs container with the editor layer like
     * {@code PairIOGUI.save} does with the SaveLoad wrapper: DOM path
     * with the flag on, legacy XMLHelper path otherwise.
     *
     * @param saveLoad The SaveLoad wrapper to save
     * @param filename The output filename (.cpx)
     * @return true if saving succeeded
     */
    public static boolean saveCdContainerSaveLoad(
            ConflictsDependenciesContainerSaveLoad saveLoad, String filename) {
        if (saveLoad == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            return UISerialization.saveConflictsDependenciesWithDom(
                saveLoad, saveLoad.getPairsGraGra(), saveLoad.getCPAGraph(),
                filename);
        }
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(saveLoad);
        return helper.save_to_xml(filename);
    }

    /**
     * Loads a critical pairs container with the editor layer like
     * {@code PairIOGUI.load} does with the SaveLoad wrapper: DOM path
     * with the flag on, legacy XMLHelper path otherwise.
     *
     * @param saveLoad The SaveLoad wrapper instance to populate
     * @param filename The input filename (.cpx)
     * @return true if loading succeeded
     */
    public static boolean loadCdContainerSaveLoad(
            ConflictsDependenciesContainerSaveLoad saveLoad, String filename) {
        if (saveLoad == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            try {
                UISerialization.ConflictsDependenciesUiResult result =
                    UISerialization.loadConflictsDependenciesWithDom(
                        saveLoad, filename);
                saveLoad.setPairsGraGra(result.edGraGra);
                saveLoad.setCpaGraph(result.cpaGraph);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        XMLHelper helper = new XMLHelper();
        if (helper.read_from_xml(filename)) {
            helper.getTopObject(saveLoad);
            return true;
        }
        return false;
    }

    // ---- standalone PairContainer (.cpx/.dpx like PairIOGUI) ----

    /**
     * Saves a standalone pair container like {@code PairIOGUI.save} does
     * with a PairContainer: DOM path with the flag on, legacy XMLHelper
     * path otherwise.
     *
     * @param container The pair container to save
     * @param filename The output filename (.cpx/.dpx)
     * @return true if saving succeeded
     */
    public static boolean saveStandalonePairContainer(PairContainer container,
            String filename) {
        if (container == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            return XMLSerialization.saveWithDom(container, filename);
        }
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(container);
        return helper.save_to_xml(filename);
    }

    /**
     * Loads a standalone pair container like {@code PairIOGUI.load} does
     * with a PairContainer: DOM path with the flag on, legacy XMLHelper
     * path otherwise.
     *
     * @param container The pair container instance to populate
     * @param filename The input filename (.cpx/.dpx)
     * @return true if loading succeeded
     */
    public static boolean loadStandalonePairContainer(PairContainer container,
            String filename) {
        if (container == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            try {
                return XMLSerialization.loadWithDom(container, filename);
            } catch (Exception e) {
                return false;
            }
        }
        XMLHelper helper = new XMLHelper();
        if (helper.read_from_xml(filename)) {
            helper.getTopObject(container);
            return true;
        }
        return false;
    }

    // ---- .rsx (ApplRuleSequence, like ApplRuleSequenceSaveLoad) ----

    /**
     * Saves an ApplRuleSequence with the editor layer of its grammar
     * (like {@code ApplRuleSequenceSaveLoad.save}): DOM path with the
     * flag on, legacy XMLHelper path otherwise.
     *
     * @param applRuleSeq The rule sequence to save
     * @param edGraGra The editor grammar carrying the UI state
     * @param filename The output filename (.rsx)
     * @return true if saving succeeded
     */
    public static boolean saveRuleSequence(ApplRuleSequence applRuleSeq,
            EdGraGra edGraGra, String filename) {
        if (applRuleSeq == null || edGraGra == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            return UISerialization.saveRuleSequenceWithDom(applRuleSeq, edGraGra,
                filename);
        }
        String outfileName = filename;
        if (outfileName.indexOf(".rsx") == -1) {
            outfileName = outfileName.concat(".rsx");
        }
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(applRuleSeq);
        helper.addObject("GraphTransformationSystem", edGraGra, false);
        return helper.save_to_xml(outfileName);
    }

    /**
     * Loads an ApplRuleSequence with the editor layer of its grammar
     * (like {@code ApplRuleSequenceSaveLoad.load}): DOM path with the
     * flag on, legacy XMLHelper path otherwise. Returns the loaded
     * editor grammar, or null on failure.
     *
     * @param applRuleSeq The rule sequence instance to populate
     * @param filename The input filename (.rsx)
     * @return the loaded editor grammar, or null if loading failed
     */
    public static EdGraGra loadRuleSequence(ApplRuleSequence applRuleSeq,
            String filename) {
        if (applRuleSeq == null || filename == null) {
            return null;
        }
        if (isUseNewXml()) {
            try {
                return UISerialization.loadRuleSequenceWithDom(applRuleSeq,
                    filename);
            } catch (Exception e) {
                return null;
            }
        }
        try {
            applRuleSeq.setGraGra(null);
            XMLHelper helper = new XMLHelper();
            applRuleSeq.load(filename, helper);
            EdGraGra ed = new EdGraGra(applRuleSeq.getGraGra());
            helper.enrichObject(ed);
            return ed;
        } catch (Exception e) {
            return null;
        }
    }

    // ---- .rsx (ApplRuleSequence, core only) ----

    /**
     * Saves an ApplRuleSequence: DOM path with the flag on, legacy
     * XMLHelper path otherwise.
     *
     * @param applRuleSeq The rule sequence to save
     * @param filename The output filename (.rsx)
     * @return true if saving succeeded
     */
    public static boolean save(ApplRuleSequence applRuleSeq, String filename) {
        if (applRuleSeq == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            return XMLSerialization.saveWithDom(applRuleSeq, filename);
        }
        String outfileName = filename;
        if (outfileName.indexOf(".rsx") == -1) {
            outfileName = outfileName.concat(".rsx");
        }
        XMLHelper helper = new XMLHelper();
        helper.addTopObject(applRuleSeq);
        return helper.save_to_xml(outfileName);
    }

    /**
     * Loads an ApplRuleSequence: DOM path with the flag on, legacy
     * XMLHelper path otherwise.
     *
     * @param applRuleSeq The rule sequence instance to populate
     * @param filename The input filename (.rsx)
     * @return true if loading succeeded
     */
    public static boolean load(ApplRuleSequence applRuleSeq, String filename) {
        if (applRuleSeq == null || filename == null) {
            return false;
        }
        if (isUseNewXml()) {
            try {
                return XMLSerialization.loadWithDom(applRuleSeq, filename) != null;
            } catch (Exception e) {
                return false;
            }
        }
        try {
            return XMLSerialization.load(applRuleSeq, filename) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
