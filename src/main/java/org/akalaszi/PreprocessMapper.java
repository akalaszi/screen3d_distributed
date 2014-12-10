package org.akalaszi;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.alignment.MMPAlignment;
import chemaxon.struc.Molecule;

public class PreprocessMapper extends Mapper<Text, Text, NullWritable, Text> {
    public static final String EXTENSION = "sdf";

    @Override
    public void map(Text key, Text molecule, Context context) throws IOException, InterruptedException {
        Molecule m = MMPAlignment.preprocess(MolImporter.importMol(molecule.toString()), true);
        context.write(NullWritable.get(), new Text(MolExporter.exportToFormat(m, EXTENSION)));
    }
}
