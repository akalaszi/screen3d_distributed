package org.akalaszi;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.alignment.MMPAlignment;
import chemaxon.struc.Molecule;

public class PreprocessMapper extends Mapper<Integer, Text, String, String> {

    public void map(Integer key, Text molecule, Context context) throws IOException, InterruptedException {
        Molecule m = MMPAlignment.preprocess(MolImporter.importMol(molecule.toString()), true);
        context.write(key.toString(), MolExporter.exportToFormat(m, "mrv"));
    }
}
