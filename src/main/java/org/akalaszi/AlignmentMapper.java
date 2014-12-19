package org.akalaszi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import chemaxon.formats.MolExporter;
import chemaxon.marvin.alignment.Alignment;
import chemaxon.marvin.alignment.AlignmentException;
import chemaxon.marvin.alignment.AlignmentMolecule;
import chemaxon.marvin.alignment.AlignmentMoleculeFactory;
import chemaxon.marvin.alignment.AlignmentProperties.ColoringScheme;
import chemaxon.marvin.alignment.AlignmentProperties.DegreeOfFreedomType;
import chemaxon.marvin.alignment.AlignmentProperties.NodeType;
import chemaxon.struc.Molecule;

public class AlignmentMapper extends Mapper<Text, Text, NullWritable, Text> {
    public static final String EXTENSION = "sdf";
    public static final double MASS_DIFERENCE_LIMIT = 300;
    public static Logger log = Logger.getLogger(AlignmentMapper.class.getName());

    @Override
    public void map(Text key, Text mrecordPair, Context context) throws IOException, InterruptedException {
        try {
            SerializableMRecordPair pair = SerializableMRecordPair.fromJSON(mrecordPair.toString());
            Molecule m1 = pair.getRecord1().toMolecule();
            Molecule m2 = pair.getRecord2().toMolecule();
            
            if (Math.abs(m1.getExactMass() - m2.getExactMass()) > MASS_DIFERENCE_LIMIT) {
                return;
            }
            
            Molecule result = alignTwoStructures(m1, m2);
            result.setProperty("screenedID", key.toString());
            context.write(NullWritable.get(), new Text(MolExporter.exportToFormat(result, EXTENSION)));

        } catch (Exception e) {
            log.log(Level.INFO, "With structure: " + key, e);
        }
    }

    static Molecule alignTwoStructures(Molecule m1, Molecule m2) throws AlignmentException {
        AlignmentMoleculeFactory amf = new AlignmentMoleculeFactory();
        amf.setColor(ColoringScheme.EXTENDED_ATOMTYPES);
        amf.setNodeType(NodeType.SPHERIC);
        AlignmentMolecule am1 = amf.create(m1, DegreeOfFreedomType.TRANSLATE_ROTATE_FLEXIBLE);
        AlignmentMolecule am2 = amf.create(m2, DegreeOfFreedomType.TRANSLATE_ROTATE_FLEXIBLE);

        Alignment a = new Alignment();
        a.setTimeLimit(5 * 60 * 1000); // set time limit for 5 mins.
        a.setMolecules(am1, am2);
        a.align();
        Molecule ret = a.getAlignedMoleculesAsFragments();
        ret.setProperty("3DTanimoto", String.valueOf(a.tanimoto()));
        ret.setProperty("3DScore", String.valueOf(a.getVolumeScore()));
        return ret;
    }

}
