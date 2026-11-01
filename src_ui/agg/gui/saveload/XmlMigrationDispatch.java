/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.gui.saveload;

import agg.editor.impl.EdGraGra;
import agg.xt_basis.GraGra;

/**
 * Reflection dispatch from the GUI save/load entry points to the
 * DOM-based XML serialization ({@code agg.xml.ui.UIMigration} in
 * agg-ui-xml).
 *
 * <p>agg-ui must not compile-depend on agg-ui-xml: the frozen legacy
 * clone keeps a clean classpath that way, and a deployment without the
 * agg-ui-xml jar simply stays on the legacy path. Every dispatch returns
 * the failure value ({@code false} or {@code null}) when the new XML
 * layer is unavailable or fails, so the callers fall back to their
 * existing legacy XMLHelper code.</p>
 *
 * <p>The migration flag is NOT cached, unlike the core dispatch in
 * {@code GraGra}: the flag can be toggled at runtime (tests do it), and
 * the reflection cost is negligible compared to the file I/O of a save
 * or load.</p>
 */
public final class XmlMigrationDispatch {

    private XmlMigrationDispatch() {
    }

    /**
     * Returns whether the DOM path is enabled and reachable.
     *
     * @return true if the new XML layer is active
     */
    public static boolean isUseNewXml() {
        try {
            Class<?> cls = Class.forName("agg.xml.ui.UIMigration");
            java.lang.reflect.Method m = cls.getMethod("isUseNewXml");
            Object result = m.invoke(null);
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Saves an EdGraGra through the new XML layer (like GraGraSave.save
     * does with the legacy XMLHelper).
     *
     * @param edGraGra The editor grammar to save
     * @param filename The output filename
     * @return true if the new layer saved the file
     */
    public static boolean dispatchSave(EdGraGra edGraGra, String filename) {
        try {
            Class<?> cls = Class.forName("agg.xml.ui.UIMigration");
            java.lang.reflect.Method m = cls.getMethod("save",
                EdGraGra.class, String.class);
            Object result = m.invoke(null, edGraGra, filename);
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Loads an EdGraGra through the new XML layer (like GraGraLoad.reload
     * does with the legacy XMLHelper).
     *
     * @param filename The input filename
     * @return the loaded editor grammar, or null on failure
     */
    public static EdGraGra dispatchLoad(String filename) {
        try {
            Class<?> cls = Class.forName("agg.xml.ui.UIMigration");
            java.lang.reflect.Method m = cls.getMethod("load", String.class);
            return (EdGraGra) m.invoke(null, filename);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Saves a core GraGra through the new XML layer (like
     * GraGraSave.saveBase does with the legacy XMLHelper).
     *
     * @param graGra The grammar to save
     * @param filename The output filename
     * @return true if the new layer saved the file
     */
    public static boolean dispatchSave(GraGra graGra, String filename) {
        try {
            Class<?> cls = Class.forName("agg.xml.ui.UIMigration");
            java.lang.reflect.Method m = cls.getMethod("save",
                GraGra.class, String.class);
            Object result = m.invoke(null, graGra, filename);
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Loads a core GraGra through the new XML layer (like
     * GraGraLoad.reloadBase does with the legacy XMLHelper).
     *
     * @param graGra The grammar instance to populate
     * @param filename The input filename
     * @return true if the new layer loaded the file into the grammar
     */
    public static boolean dispatchLoad(GraGra graGra, String filename) {
        try {
            Class<?> cls = Class.forName("agg.xml.ui.UIMigration");
            java.lang.reflect.Method m = cls.getMethod("load",
                GraGra.class, String.class);
            Object result = m.invoke(null, graGra, filename);
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Saves an ApplRuleSequence with the editor layer of its grammar
     * through the new XML layer (like ApplRuleSequenceSaveLoad.save does
     * with the legacy XMLHelper).
     *
     * @param applRuleSeq The rule sequence to save
     * @param edGraGra The editor grammar carrying the UI state
     * @param filename The output filename
     * @return true if the new layer saved the file
     */
    public static boolean dispatchSave(agg.ruleappl.ApplRuleSequence applRuleSeq,
            EdGraGra edGraGra, String filename) {
        try {
            Class<?> cls = Class.forName("agg.xml.ui.UIMigration");
            java.lang.reflect.Method m = cls.getMethod("saveRuleSequence",
                agg.ruleappl.ApplRuleSequence.class, EdGraGra.class, String.class);
            Object result = m.invoke(null, applRuleSeq, edGraGra, filename);
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Loads an ApplRuleSequence with the editor layer of its grammar
     * through the new XML layer (like ApplRuleSequenceSaveLoad.load does
     * with the legacy XMLHelper).
     *
     * @param applRuleSeq The rule sequence instance to populate
     * @param filename The input filename
     * @return the loaded editor grammar, or null on failure
     */
    public static EdGraGra dispatchLoad(agg.ruleappl.ApplRuleSequence applRuleSeq,
            String filename) {
        try {
            Class<?> cls = Class.forName("agg.xml.ui.UIMigration");
            java.lang.reflect.Method m = cls.getMethod("loadRuleSequence",
                agg.ruleappl.ApplRuleSequence.class, String.class);
            return (EdGraGra) m.invoke(null, applRuleSeq, filename);
        } catch (Exception e) {
            return null;
        }
    }
}
