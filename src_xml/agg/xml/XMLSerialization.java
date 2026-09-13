/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml;

import agg.util.XMLObject;
import agg.xt_basis.GraGra;
import agg.xml.adapter.GraGraAdapter;
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
 * <p>The methods delegate to {@link GraGraMigration} which uses a feature flag
 * ({@code agg.xml.newxml} system property) to switch between the legacy
 * XMLHelper path and the new DOM-based path:</p>
 *
 * <pre>{@code
 * // Save a GraGra
 * XMLSerialization.save(graGra, "mygrammar.ggx");
 *
 * // Load a GraGra
 * GraGra graGra = new GraGra();
 * XMLSerialization.load(graGra, "mygrammar.ggx");
 *
 * // Enable the new DOM path
 * XMLSerialization.setUseNewXml(true);
 *
 * // Check if file is a valid .ggx
 * if (XMLSerialization.canLoad("mygrammar.ggx")) { ... }
 * }</pre>
 *
 * <p><b>Feature flag control:</b></p>
 * <ul>
 *   <li>{@code agg.xml.newxml=false} (default): uses legacy XMLHelper</li>
 *   <li>{@code agg.xml.newxml=true}: uses new DOM-based serialization
 *       via GraGraAdapter and sub-adapters</li>
 * </ul>
 *
 * @see GraGraMigration
 * @see GraGraAdapter
 */
public final class XMLSerialization {

    private XMLSerialization() {
    }

    // ---- Feature flag control ----

    /**
     * Returns whether the new DOM-based XML serialization is enabled.
     *
     * @return true if the new XML path is active, false for legacy
     */
    public static boolean isUseNewXml() {
        return GraGraMigration.isUseNewXml();
    }

    /**
     * Enables or disables the new DOM-based XML serialization.
     *
     * <p>When enabled, save/load operations use the GraGraAdapter with
     * direct DOM serialization. When disabled (default), the legacy
     * XMLHelper path is used.</p>
     *
     * @param enabled true to use new XML, false for legacy
     */
    public static void setUseNewXml(boolean enabled) {
        GraGraMigration.setUseNewXml(enabled);
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

    // ---- Direct DOM serialization (bypassing feature flag) ----

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

    // ---- Generic XMLObject save/load (legacy-compatible) ----

    /**
     * Saves any XMLObject to a .ggx file using the legacy XMLHelper path.
     *
     * <p>This method is provided for backward compatibility with domain
     * classes that implement {@link XMLObject} but are not GraGra.
     * It uses the legacy XMLHelper internally.</p>
     *
     * @param object   The XMLObject to save
     * @param filename The output filename
     * @return true if saving succeeded
     * @deprecated Use {@link #save(GraGra, String)} for GraGra objects.
     *             For other XMLObject types, use the legacy XMLHelper directly
     *             until dedicated adapters are available.
     */
    @Deprecated
    public static boolean saveXmlObject(XMLObject object, String filename) {
        if (object == null || filename == null) {
            return false;
        }
        agg.util.XMLHelper helper = new agg.util.XMLHelper();
        helper.addTopObject(object);
        return helper.save_to_xml(filename);
    }

    /**
     * Loads any XMLObject from a .ggx file using the legacy XMLHelper path.
     *
     * @param template  The XMLObject instance to populate
     * @param filename  The input filename
     * @return true if loading succeeded
     * @deprecated Use {@link #load(GraGra, String)} for GraGra objects.
     */
    @Deprecated
    public static boolean loadXmlObject(XMLObject template, String filename) {
        if (template == null || filename == null) {
            return false;
        }
        agg.util.XMLHelper helper = new agg.util.XMLHelper();
        if (!helper.read_from_xml(filename)) {
            return false;
        }
        helper.getTopObject(template);
        return true;
    }
}
