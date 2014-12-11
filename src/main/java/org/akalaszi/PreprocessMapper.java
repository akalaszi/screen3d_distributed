package org.akalaszi;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import chemaxon.formats.MolExporter;
import chemaxon.marvin.alignment.MMPAlignment;
import chemaxon.struc.MPropertyContainer;
import chemaxon.struc.Molecule;

public class PreprocessMapper extends Mapper<Text, Text, NullWritable, Text> {
    public static final String EXTENSION = "sdf";

    @Override
    public void map(Text key, Text mrecord, Context context) throws IOException, InterruptedException {
        Molecule original = MRecordSerializer.fromJSON(mrecord.toString()).toMolecule();
        Molecule m = MMPAlignment.preprocess(original, true);
        context.write(NullWritable.get(), new Text(MolExporter.exportToFormat(m, EXTENSION)));
    }

    static Map<String, String> propertiesToMap(MPropertyContainer properties) {
        Map<String, String> props = new LinkedHashMap<String, String>();
        String[] keys = properties.getKeys();
        for (String propKey : keys) {
            props.put(propKey, properties.getObject(propKey).toString());
        }
        return props;
    }

    static void writeProperties(Map<String, String> props, Molecule to) {
        for (Map.Entry<String, String> entry : props.entrySet()) {
            to.setProperty(entry.getKey(), entry.getValue());
        }
    }

}
