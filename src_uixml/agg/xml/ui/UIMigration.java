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
import agg.xt_basis.GraGra;
import agg.xml.XMLSerialization;

/**
 * Facade for the GUI save/load entry points of all file formats.
 *
 * <p>Every method goes through the DOM-based serialization
 * ({@link UISerialization} for the editor layer, {@link XMLSerialization}
 * for the core objects). The former migration feature flag and the legacy
 * XMLHelper branches have been removed with the legacy XML cleanup.</p>
 *
 * <p>The GUI layer (agg-ui) calls these methods through reflection
 * ({@code agg.gui.saveload.XmlMigrationDispatch}), because agg-ui must not
 * compile-depend on agg-ui-xml.</p>
 */
public final class UIMigration {

    private UIMigration() {
    }

    // ---- .ggx with the UI layer (EdGraGra top object, like GraGraSave/GraGraLoad) ----

    /**
     * Saves an EdGraGra like {@code GraGraSave.save}.
     *
     * @param edGraGra The editor grammar to save
     * @param filename The output filename (.ggx)
     * @return true if saving succeeded
     */
    public static boolean save(EdGraGra edGraGra, String filename) {
        if (edGraGra == null || filename == null) {
            return false;
        }
        return UISerialization.saveWithDom(edGraGra, filename);
    }

    /**
     * Loads an EdGraGra like {@code GraGraLoad.reload}. Returns null on
     * failure.
     *
     * @param filename The input filename (.ggx)
     * @return the loaded editor grammar, or null if loading failed
     */
    public static EdGraGra load(String filename) {
        if (filename == null) {
            return null;
        }
        try {
            return UISerialization.loadWithDom(filename);
        } catch (Exception e) {
            return null;
        }
    }

    // ---- .ggx core only (GraGra top object, like GraGraSave.saveBase/GraGraLoad.reloadBase) ----

    /**
     * Saves a GraGra like {@code GraGraSave.saveBase}.
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
     * Loads a GraGra like {@code GraGraLoad.reloadBase}.
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
     * Saves a ConflictsDependenciesContainer.
     *
     * @param container The container to save
     * @param filename The output filename (.cpx)
     * @return true if saving succeeded
     */
    public static boolean save(ConflictsDependenciesContainer container, String filename) {
        if (container == null || filename == null) {
            return false;
        }
        return XMLSerialization.saveWithDom(container, filename);
    }

    /**
     * Loads a ConflictsDependenciesContainer.
     *
     * @param container The container instance to populate
     * @param filename The input filename (.cpx)
     * @return true if loading succeeded
     */
    public static boolean load(ConflictsDependenciesContainer container, String filename) {
        if (container == null || filename == null) {
            return false;
        }
        try {
            return XMLSerialization.loadWithDom(container, filename);
        } catch (Exception e) {
            return false;
        }
    }

    // ---- UI .cpx (ConflictsDependenciesContainerSaveLoad like PairIOGUI) ----

    /**
     * Saves a critical pairs container with the editor layer like
     * {@code PairIOGUI.save} does with the SaveLoad wrapper.
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
        return UISerialization.saveConflictsDependenciesWithDom(
            saveLoad, saveLoad.getPairsGraGra(), saveLoad.getCPAGraph(),
            filename);
    }

    /**
     * Loads a critical pairs container with the editor layer like
     * {@code PairIOGUI.load} does with the SaveLoad wrapper.
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

    // ---- standalone PairContainer (.cpx/.dpx like PairIOGUI) ----

    /**
     * Saves a standalone pair container like {@code PairIOGUI.save} does
     * with a PairContainer.
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
        return XMLSerialization.saveWithDom(container, filename);
    }

    /**
     * Loads a standalone pair container like {@code PairIOGUI.load} does
     * with a PairContainer.
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
        try {
            return XMLSerialization.loadWithDom(container, filename);
        } catch (Exception e) {
            return false;
        }
    }

    // ---- .rsx (ApplRuleSequence, like ApplRuleSequenceSaveLoad) ----

    /**
     * Saves an ApplRuleSequence with the editor layer of its grammar
     * (like {@code ApplRuleSequenceSaveLoad.save}).
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
        return UISerialization.saveRuleSequenceWithDom(applRuleSeq, edGraGra,
            filename);
    }

    /**
     * Loads an ApplRuleSequence with the editor layer of its grammar
     * (like {@code ApplRuleSequenceSaveLoad.load}). Returns the loaded
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
        try {
            return UISerialization.loadRuleSequenceWithDom(applRuleSeq,
                filename);
        } catch (Exception e) {
            return null;
        }
    }

    // ---- .rsx (ApplRuleSequence, core only) ----

    /**
     * Saves an ApplRuleSequence.
     *
     * @param applRuleSeq The rule sequence to save
     * @param filename The output filename (.rsx)
     * @return true if saving succeeded
     */
    public static boolean save(ApplRuleSequence applRuleSeq, String filename) {
        if (applRuleSeq == null || filename == null) {
            return false;
        }
        return XMLSerialization.saveWithDom(applRuleSeq, filename);
    }

    /**
     * Loads an ApplRuleSequence.
     *
     * @param applRuleSeq The rule sequence instance to populate
     * @param filename The input filename (.rsx)
     * @return true if loading succeeded
     */
    public static boolean load(ApplRuleSequence applRuleSeq, String filename) {
        if (applRuleSeq == null || filename == null) {
            return false;
        }
        try {
            return XMLSerialization.loadWithDom(applRuleSeq, filename) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
