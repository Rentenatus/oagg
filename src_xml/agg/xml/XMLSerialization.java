/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml;

import agg.xt_basis.GraGra;
import agg.xml.adapter.ApplRuleSequenceAdapter;
import agg.xml.adapter.ConflictsDependenciesContainerAdapter;
import agg.xml.adapter.GraGraAdapter;
import agg.xml.adapter.PairContainerAdapter;
import agg.xml.core.DOMXMLDeserializerContext;
import agg.xml.core.DOMXMLSerializer;
import agg.xml.core.XMLSerializationException;
import agg.xml.migration.GraGraMigration;

import java.io.File;

/**
 * Central utility class providing static methods for saving and loading
 * AGG domain objects via the new DOM-based XML serialization infrastructure.
 *
 * <p>This class is the primary entry point for all XML save/load operations.
 * It replaces direct calls to {@code XMLHelper.save_to_xml()} and
 * {@code XMLHelper.read_from_xml()} in the core domain classes.</p>
 *
 * <p>The GraGra save/load methods delegate to {@link GraGraMigration}; the
 * former migration feature flag has been removed with the legacy XML
 * cleanup - every save/load goes through the DOM-based adapters.</p>
 *
 * <p><b>Supported object types:</b></p>
 * <ul>
 *   <li>{@link GraGra} -- .ggx files (grammar with types, graphs, rules, constraints)</li>
 *   <li>{@link agg.ruleappl.ApplRuleSequence} -- .rsx files (rule sequence applicability)</li>
 *   <li>{@link agg.parser.ConflictsDependenciesContainer} -- .cpx files (critical pairs)</li>
 *   <li>{@link agg.xt_basis.AGGBasicAppl} -- static GraGra load/save helpers</li>
 *   <li>{@link agg.parser.PairContainer} -- standalone .cpx files</li>
 * </ul>
 *
 * @see GraGraMigration
 * @see GraGraAdapter
 */
public final class XMLSerialization {

    private XMLSerialization() {
    }

    // ---- GraGra save/load ----

    /**
     * Saves a GraGra to a .ggx file.
     *
     * <p>If the filename does not end with {@code .ggx}, the extension
     * is appended automatically. The file name and directory are updated
     * on the GraGra instance after saving.</p>
     *
     * @param graGra   The GraGra to save
     * @param filename The output filename (full path)
     * @return true if saving succeeded, false if it failed
     */
    public static boolean save(GraGra graGra, String filename) {
        return GraGraMigration.save(graGra, filename);
    }

    /**
     * Loads a GraGra from a .ggx file.
     *
     * <p>The GraGra instance is populated with the data from the file.
     * The file name and directory are updated on the GraGra instance
     * after loading.</p>
     *
     * @param graGra   The GraGra instance to populate (must not be null)
     * @param filename The input filename (full path)
     * @return true if loading succeeded
     * @throws Exception if loading fails (file not found, parse error, etc.)
     */
    public static boolean load(GraGra graGra, String filename) throws Exception {
        return GraGraMigration.load(graGra, filename);
    }

    // ---- Validation ----

    /**
     * Validates a .ggx file against the AGG XML schema.
     *
     * @param filename The file to validate
     * @return true if the file is a valid .ggx file
     */
    public static boolean validate(String filename) {
        return GraGraMigration.validate(filename);
    }

    /**
     * Checks if a file can be loaded as a .ggx file.
     *
     * <p>This performs a quick check: the file must exist, end with
     * {@code .ggx} (case-insensitive), and be parseable as XML.</p>
     *
     * @param filename The file to check
     * @return true if the file can be loaded
     */
    public static boolean canLoad(String filename) {
        if (filename == null) {
            return false;
        }
        File f = new File(filename);
        if (!f.exists() || !f.isFile()) {
            return false;
        }
        if (!filename.toLowerCase().endsWith(".ggx")) {
            return false;
        }
        try {
            DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(f);
            return context.getDocument() != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ---- Direct DOM serialization ----

    /**
     * Saves a GraGra directly using the new DOM-based serialization,
     * bypassing the feature flag. This always uses GraGraAdapter
     * with DOMXMLSerializer.
     *
     * @param graGra   The GraGra to save
     * @param filename The output filename
     * @return true if saving succeeded
     */
    public static boolean saveWithDom(GraGra graGra, String filename) {
        if (graGra == null || filename == null) {
            return false;
        }
        try {
            String outfileName = filename;
            if (!outfileName.toLowerCase().endsWith(".ggx")) {
                outfileName = outfileName + ".ggx";
            }
            File outputFile = new File(outfileName);
            GraGraAdapter adapter = new GraGraAdapter(graGra);
            DOMXMLSerializer serializer = new DOMXMLSerializer();
            serializer.serializeToFile(adapter, outputFile);
            return true;
        } catch (XMLSerializationException e) {
            return false;
        }
    }

    /**
     * Loads a GraGra directly using the new DOM-based deserialization,
     * bypassing the feature flag. This always uses GraGraAdapter
     * with DOMXMLDeserializerContext.
     *
     * @param graGra   The GraGra instance to populate
     * @param filename The input filename
     * @return true if loading succeeded
     * @throws XMLSerializationException if deserialization fails
     */
    public static boolean loadWithDom(GraGra graGra, String filename)
            throws XMLSerializationException {
        if (graGra == null || filename == null) {
            return false;
        }
        File f = new File(filename);
        if (!f.exists()) {
            throw new XMLSerializationException("File not found: " + filename);
        }
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(f);
        GraGraAdapter adapter = new GraGraAdapter(graGra);
        adapter.deserialize(context);
        return true;
    }

    // ---- Serialize to string ----

    /**
     * Serializes a GraGra to an XML string using the new DOM path.
     *
     * @param graGra The GraGra to serialize
     * @return The XML string representation
     * @throws XMLSerializationException if serialization fails
     */
    public static String saveToString(GraGra graGra) throws XMLSerializationException {
        if (graGra == null) {
            throw new XMLSerializationException("GraGra is null");
        }
        GraGraAdapter adapter = new GraGraAdapter(graGra);
        DOMXMLSerializer serializer = new DOMXMLSerializer();
        return serializer.serializeToString(adapter);
    }

    // ---- ApplRuleSequence save/load (.rsx files) ----

    /**
     * Saves an ApplRuleSequence to a .rsx file.
     *
     * <p>If the filename does not end with {@code .rsx}, the extension
     * is appended automatically.</p>
     *
     * @param applRuleSeq The ApplRuleSequence to save
     * @param filename    The output filename
     * @return true if saving succeeded
     */
    public static boolean save(agg.ruleappl.ApplRuleSequence applRuleSeq, String filename) {
        if (applRuleSeq == null || filename == null) {
            return false;
        }
        return saveWithDom(applRuleSeq, filename);
    }

    /**
     * Loads an ApplRuleSequence from a .rsx file.
     *
     * @param applRuleSeq The ApplRuleSequence instance to populate
     * @param filename    The input filename (.rsx)
     * @return the loaded GraGra, or null if loading failed
     * @throws Exception if loading fails
     */
    public static GraGra load(agg.ruleappl.ApplRuleSequence applRuleSeq, String filename)
            throws Exception {
        if (applRuleSeq == null || filename == null) {
            return null;
        }
        return loadWithDom(applRuleSeq, filename);
    }

    /**
     * Saves an ApplRuleSequence directly using the new DOM-based
     * serialization, bypassing the feature flag.
     *
     * @param applRuleSeq The ApplRuleSequence to save
     * @param filename    The output filename
     * @return true if saving succeeded
     */
    public static boolean saveWithDom(agg.ruleappl.ApplRuleSequence applRuleSeq,
            String filename) {
        if (applRuleSeq == null || filename == null) {
            return false;
        }
        try {
            String outfileName = filename;
            if (!outfileName.toLowerCase().endsWith(".rsx")) {
                outfileName = outfileName + ".rsx";
            }
            ApplRuleSequenceAdapter adapter = new ApplRuleSequenceAdapter(applRuleSeq);
            DOMXMLSerializer serializer = new DOMXMLSerializer();
            serializer.serializeToFile(adapter, new File(outfileName));
            return true;
        } catch (XMLSerializationException e) {
            return false;
        }
    }

    /**
     * Loads an ApplRuleSequence directly using the new DOM-based
     * deserialization, bypassing the feature flag.
     *
     * @param applRuleSeq The ApplRuleSequence instance to populate
     * @param filename    The input filename (.rsx)
     * @return the loaded GraGra, or null if loading failed
     * @throws XMLSerializationException if deserialization fails
     */
    public static GraGra loadWithDom(agg.ruleappl.ApplRuleSequence applRuleSeq,
            String filename) throws XMLSerializationException {
        if (applRuleSeq == null || filename == null) {
            return null;
        }
        File f = new File(filename);
        if (!f.exists()) {
            throw new XMLSerializationException("File not found: " + filename);
        }
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(f);
        ApplRuleSequenceAdapter adapter = new ApplRuleSequenceAdapter(applRuleSeq);
        adapter.deserialize(context);
        return applRuleSeq.getGraGra();
    }

    // ---- ConflictsDependenciesContainer DOM save/load (.cpx files) ----

    /**
     * Saves a ConflictsDependenciesContainer directly using the new DOM-based
     * serialization, bypassing the feature flag.
     *
     * <p>Computed critical pair entries are not yet supported by the DOM
     * path; containers with computed pairs throw
     * {@link XMLSerializationException}.</p>
     *
     * @param container The ConflictsDependenciesContainer to save
     * @param filename  The output filename
     * @return true if saving succeeded
     */
    public static boolean saveWithDom(agg.parser.ConflictsDependenciesContainer container,
            String filename) {
        if (container == null || filename == null) {
            return false;
        }
        try {
            String outfileName = filename;
            if (!outfileName.toLowerCase().endsWith(".cpx")) {
                outfileName = outfileName + ".cpx";
            }
            ConflictsDependenciesContainerAdapter adapter =
                new ConflictsDependenciesContainerAdapter(container);
            DOMXMLSerializer serializer = new DOMXMLSerializer();
            serializer.serializeToFile(adapter, new File(outfileName));
            return true;
        } catch (XMLSerializationException e) {
            return false;
        }
    }

    /**
     * Loads a ConflictsDependenciesContainer directly using the new DOM-based
     * deserialization, bypassing the feature flag.
     *
     * @param container The ConflictsDependenciesContainer instance to populate
     * @param filename  The input filename (.cpx)
     * @return true if loading succeeded
     * @throws XMLSerializationException if deserialization fails
     */
    public static boolean loadWithDom(agg.parser.ConflictsDependenciesContainer container,
            String filename) throws XMLSerializationException {
        if (container == null || filename == null) {
            return false;
        }
        File f = new File(filename);
        if (!f.exists()) {
            throw new XMLSerializationException("File not found: " + filename);
        }
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(f);
        ConflictsDependenciesContainerAdapter adapter =
            new ConflictsDependenciesContainerAdapter(container);
        adapter.deserialize(context);
        return true;
    }

    // ---- standalone PairContainer DOM save/load (.cpx files) ----

    /**
     * Saves a standalone pair container (ExcludePairContainer or
     * DependencyPairContainer, including the layered/priority
     * variants) directly using the new DOM-based serialization,
     * bypassing the feature flag.
     *
     * @param container The PairContainer to save
     * @param filename  The output filename (.cpx)
     * @return true if saving succeeded
     */
    public static boolean saveWithDom(agg.parser.PairContainer container,
            String filename) {
        if (container == null || filename == null) {
            return false;
        }
        try {
            String outfileName = filename;
            if (!outfileName.toLowerCase().endsWith(".cpx")) {
                outfileName = outfileName + ".cpx";
            }
            PairContainerAdapter adapter = new PairContainerAdapter(container);
            DOMXMLSerializer serializer = new DOMXMLSerializer();
            serializer.serializeToFile(adapter, new File(outfileName));
            return true;
        } catch (XMLSerializationException e) {
            return false;
        }
    }

    /**
     * Loads a standalone pair container directly using the new
     * DOM-based deserialization, bypassing the feature flag.
     *
     * @param container The PairContainer instance to populate
     * @param filename  The input filename (.cpx)
     * @return true if loading succeeded
     * @throws XMLSerializationException if deserialization fails
     */
    public static boolean loadWithDom(agg.parser.PairContainer container,
            String filename) throws XMLSerializationException {
        if (container == null || filename == null) {
            return false;
        }
        File f = new File(filename);
        if (!f.exists()) {
            throw new XMLSerializationException("File not found: " + filename);
        }
        DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(f);
        PairContainerAdapter adapter = new PairContainerAdapter(container);
        adapter.deserialize(context);
        return true;
    }

    // ---- AGGBasicAppl static helpers ----

    /**
     * Loads a GraGra from a .ggx file and returns it.
     *
     * <p>This replaces {@code AGGBasicAppl.load(fName)}. It creates a new
     * GraGra via BaseFactory, loads it, and sets the file name.</p>
     *
     * @param filename The input filename (.ggx)
     * @return the loaded GraGra, or null if loading failed
     */
    public static GraGra loadGraGra(String filename) {
        if (filename == null || !filename.endsWith(".ggx")) {
            return null;
        }
        try {
            GraGra graGra = agg.xt_basis.BaseFactory.theFactory().createGraGra(false);
            load(graGra, filename);
            graGra.setFileName(filename);
            return graGra;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Saves a GraGra to a .ggx file.
     *
     * <p>This replaces {@code AGGBasicAppl.save(gra, outFileName)}. If the
     * filename is empty, a default name is generated from the GraGra name.</p>
     *
     * @param graGra     The GraGra to save
     * @param filename   The output filename
     * @return true if saving succeeded
     */
    public static boolean saveGraGra(GraGra graGra, String filename) {
        if (graGra == null) {
            return false;
        }
        String outfileName = filename;
        if (outfileName == null || outfileName.isEmpty()) {
            outfileName = graGra.getName() + "_out.ggx";
        } else if (!outfileName.contains(".ggx")) {
            outfileName = outfileName.concat(".ggx");
        }
        return save(graGra, outfileName);
    }

    // ---- File extension helpers ----

    /**
     * Checks if a filename has a .ggx extension (case-insensitive).
     *
     * @param filename The filename to check
     * @return true if the filename ends with .ggx
     */
    public static boolean isGgxFile(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".ggx");
    }

    /**
     * Checks if a filename has a .cpx extension (case-insensitive).
     *
     * @param filename The filename to check
     * @return true if the filename ends with .cpx
     */
    public static boolean isCpxFile(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".cpx");
    }

    /**
     * Checks if a filename has a .rsx extension (case-insensitive).
     *
     * @param filename The filename to check
     * @return true if the filename ends with .rsx
     */
    public static boolean isRsxFile(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".rsx");
    }

    /**
     * Checks if a filename has a .wsdl extension (case-insensitive).
     *
     * @param filename The filename to check
     * @return true if the filename ends with .wsdl
     */
    public static boolean isWsdlFile(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".wsdl");
    }
}
