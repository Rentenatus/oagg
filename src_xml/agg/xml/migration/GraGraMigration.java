/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.migration;

import agg.xt_basis.GraGra;
import agg.xml.adapter.GraGraAdapter;
import agg.xml.core.DOMXMLDeserializerContext;
import agg.xml.core.DOMXMLSerializer;
import agg.xml.core.XMLSerializationException;
import agg.xml.validation.XMLValidator;

import java.io.File;

/**
 * Save/load entry for {@link GraGra} .ggx files.
 *
 * <p>The former migration feature flag (and with it the legacy
 * XMLHelper bridge) has been removed with the legacy XML cleanup:
 * every save/load goes through the DOM-based adapters.</p>
 */
public class GraGraMigration {

    private GraGraMigration() {
    }

    /**
     * Saves a GraGra to a file.
     *
     * @param graGra   The GraGra to save
     * @param filename The output filename (full path)
     * @return true if save succeeded
     */
    public static boolean save(GraGra graGra, String filename) {
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

    /**
     * Loads a GraGra from a file.
     *
     * @param graGra   The GraGra instance to populate
     * @param filename The input filename (full path)
     * @return true if load succeeded
     * @throws Exception if loading fails
     */
    public static boolean load(GraGra graGra, String filename) throws Exception {
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
