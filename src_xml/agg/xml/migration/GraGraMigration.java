/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.migration;

import agg.util.XMLHelper;
import agg.xt_basis.GraGra;
import agg.xml.adapter.GraGraAdapter;
import agg.xml.core.DOMXMLDeserializer;
import agg.xml.core.DOMXMLDeserializerContext;
import agg.xml.core.DOMXMLSerializer;
import agg.xml.core.DOMXMLSerializerContext;
import agg.xml.core.XMLSerializationException;
import agg.xml.legacy.LegacyCompatibility;
import agg.xml.legacy.XMLSaveLoad;
import agg.xml.validation.XMLValidator;

import java.io.File;

/**
 * Migration wrapper for {@link GraGra} save/load operations using feature flags.
 *
 * <p>This class provides a feature-flag-controlled bridge between the legacy
 * XMLHelper-based save/load and the new agg-xml infrastructure. When the flag
 * {@code USE_NEW_XML} is {@code false} (the default), operations use the legacy
 * XMLHelper path. When set to {@code true}, operations use the new
 * {@link XMLSaveLoad} / {@link LegacyCompatibility} path.</p>
 *
 * <p>Feature flags can be controlled programmatically or via the system property
 * {@code agg.xml.newxml} (set to {@code true} or {@code false}).</p>
 */
public class GraGraMigration {

    /**
     * System property name for enabling the new XML path.
     */
    public static final String SYSTEM_PROPERTY = "agg.xml.newxml";

    private static volatile boolean useNewXml = Boolean.getBoolean(SYSTEM_PROPERTY);

    /**
     * Returns whether the new XML infrastructure is enabled.
     *
     * @return true if new XML path is active, false for legacy
     */
    public static boolean isUseNewXml() {
        return useNewXml;
    }

    /**
     * Enables or disables the new XML infrastructure.
     *
     * @param enabled true to use new XML, false for legacy
     */
    public static void setUseNewXml(boolean enabled) {
        useNewXml = enabled;
    }

    /**
     * Saves a GraGra to a file, using the active feature-flag path.
     *
     * @param graGra   The GraGra to save
     * @param filename The output filename (full path)
     * @return true if save succeeded
     */
    public static boolean save(GraGra graGra, String filename) {
        if (useNewXml) {
            return saveUsingNewXml(graGra, filename);
        }
        return saveUsingLegacyXml(graGra, filename);
    }

    /**
     * Loads a GraGra from a file, using the active feature-flag path.
     *
     * @param graGra   The GraGra instance to populate
     * @param filename The input filename (full path)
     * @return true if load succeeded
     * @throws Exception if loading fails
     */
    public static boolean load(GraGra graGra, String filename) throws Exception {
        if (useNewXml) {
            return loadUsingNewXml(graGra, filename);
        }
        return loadUsingLegacyXml(graGra, filename);
    }

    /**
     * Validates a .ggx file against the AGG XML schema.
     *
     * @param filename The file to validate
     * @return true if valid
     */
    public static boolean validate(String filename) {
        try {
            XMLValidator validator = new XMLValidator();
            return validator.validate(new File(filename));
        } catch (XMLSerializationException e) {
            return false;
        }
    }

    private static boolean saveUsingLegacyXml(GraGra graGra, String filename) {
        String outfileName = filename;
        if (outfileName.indexOf(".ggx") == -1) {
            outfileName = outfileName.concat(".ggx");
        }
        XMLHelper xmlh = new XMLHelper();
        xmlh.addTopObject(graGra);
        boolean saved = xmlh.save_to_xml(outfileName);
        if (saved) {
            updateFileNames(graGra, outfileName);
        }
        return saved;
    }

    private static boolean saveUsingNewXml(GraGra graGra, String filename) {
        try {
            String outfileName = filename;
            if (outfileName.indexOf(".ggx") == -1) {
                outfileName = outfileName.concat(".ggx");
            }
            File outputFile = new File(outfileName);

            // Use DOMXMLSerializer with GraGraAdapter
            GraGraAdapter adapter = new GraGraAdapter(graGra);
            DOMXMLSerializer serializer = new DOMXMLSerializer();
            serializer.serializeToFile(adapter, outputFile);

            updateFileNames(graGra, outfileName);
            return true;
        } catch (XMLSerializationException e) {
            return false;
        }
    }

    private static boolean loadUsingLegacyXml(GraGra graGra, String filename) throws Exception {
        File f = new File(filename);
        if (!f.exists()) {
            throw new Exception("File \"" + filename + "\" doesn't exist!");
        }
        if (!filename.toLowerCase().endsWith(".ggx")) {
            throw new Exception("File \"" + filename + "\" is not a \".ggx\" file!");
        }
        XMLHelper h = new XMLHelper();
        if (h.read_from_xml(filename)) {
            h.getTopObject(graGra);
            updateFileNames(graGra, filename);
            return true;
        }
        throw new Exception("File \"" + filename + "\" is not an AGG file!");
    }

    private static boolean loadUsingNewXml(GraGra graGra, String filename) throws Exception {
        File f = new File(filename);
        if (!f.exists()) {
            throw new Exception("File \"" + filename + "\" doesn't exist!");
        }
        if (!filename.toLowerCase().endsWith(".ggx")) {
            throw new Exception("File \"" + filename + "\" is not a \".ggx\" file!");
        }
        try {
            // Use DOMXMLDeserializer with GraGraAdapter
            DOMXMLDeserializerContext context = new DOMXMLDeserializerContext(f);
            GraGraAdapter adapter = new GraGraAdapter(graGra);
            adapter.deserialize(context);
            updateFileNames(graGra, filename);
            return true;
        } catch (XMLSerializationException e) {
            throw new XMLSerializationException("Failed to load: " + filename, e);
        }
    }

    /**
     * Updates the fileName and dirName fields on the GraGra instance,
     * matching the behavior of the original GraGra.save()/load() methods.
     */
    private static void updateFileNames(GraGra graGra, String filename) {
        File f = new File(filename);
        if (f.exists()) {
            graGra.setFileName(f.getName());
            if (f.getParent() == null) {
                graGra.setDirName("." + File.separator);
            } else {
                graGra.setDirName(f.getParent() + File.separator);
            }
        }
    }
}
