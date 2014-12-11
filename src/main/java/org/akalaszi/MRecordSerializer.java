package org.akalaszi;

import java.util.ArrayList;
import java.util.List;

import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.io.MRecord;
import chemaxon.struc.MPropertyContainer;
import chemaxon.struc.Molecule;

import com.google.gson.Gson;

/**
 * Helper class for serializing an MRecord to json.
 * 
 * @author Adrian Kalaszi
 *
 */
class MRecordSerializer {
    String molecule;
    String name;
    List<String[]> properties = new ArrayList<String[]>();

    MRecordSerializer(MRecord record, String id) {
        this(record.getMoleculeName(), record.getString(), record.getPropertyContainer(), id);
    }

    MRecordSerializer(String name, String molecule, MPropertyContainer container, String id) {
        this.name = name;
        this.molecule = molecule;
        addProperties(container);
        addProperty("keyId", id);
    }

    private void addProperty(String key, String value) {
        properties.add(new String[] { key, value });
    }

    private void addProperties(MPropertyContainer container) {
        for (String key : container.getKeys()) {
            addProperty(key, container.get(key).getPropValue().toString());
        }
    }

    Molecule toMolecule() throws MolFormatException {
        Molecule m = MolImporter.importMol(molecule);
        m.setName(name);
        for (String[] entry : properties) {
            m.setProperty(entry[0], entry[1]);
        }
        return m;
    }

    String toJSON() {
        return new Gson().toJson(this);
    }

    static MRecordSerializer fromJSON(String json) {
        return new Gson().fromJson(json, MRecordSerializer.class);
    }
}
