package org.akalaszi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.google.common.collect.ImmutableList;

import chemaxon.calculations.clean.Cleaner;
import chemaxon.calculations.hydrogenize.Hydrogenize;
import chemaxon.formats.MolExporter;
import chemaxon.marvin.alignment.MMPAlignment;
import chemaxon.marvin.calculations.StereoisomerPlugin;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.plugin.PluginException;
import chemaxon.struc.Molecule;
import chemaxon.struc.MoleculeGraph;

public class PreprocessMapper extends Mapper<Text, Text, NullWritable, Text> {
    public static final String EXTENSION = "sdf";
    public static final int STEREOISOMER_LIMIT = 50;
    public static final String PREPROCESS_ID = "preprocessId";
    public static final String STEREO_TAG = "_stereo_";

    public static Logger log = Logger.getLogger(PreprocessMapper.class.getName());

    static interface HeartBeat {
        void ping();
    }

    @Override
    public void map(Text key, Text mrecord, final Context context) throws IOException, InterruptedException {
        try {
            Molecule original = SerializableMRecord.fromJSON(mrecord.toString()).toMolecule();
            context.progress();
            List<Molecule> processed = processStructureForScreen3DRun(original, new HeartBeat() {

                @Override
                public void ping() {
                    context.progress();
                }
            });

            for (int i = 0; i < processed.size(); i++) {
                String id = key.toString();
                if (i > 1) {
                    id += STEREO_TAG + i;
                }
                Molecule toWrite = processed.get(i);
                toWrite.setProperty(PREPROCESS_ID, id);
                context.write(NullWritable.get(), new Text(MolExporter.exportToFormat(toWrite, EXTENSION)));
            }

        } catch (Exception e) {
            log.log(Level.INFO, "With structure: " + key, e);
        }
    }

    public static List<Molecule> processStructureForScreen3DRun(Molecule m, HeartBeat heart) {
        final Molecule ret = MMPAlignment.keepLargestFragment(m);
        MMPAlignment.removeLonePairs(ret);

        ret.valenceCheck();
        ret.aromatize(MoleculeGraph.AROM_BASIC);
        ret.valenceCheck();

        MMPAlignment.fixValence(m);
        Hydrogenize.convertExplicitHToImplicit(ret);

        StereoisomerPlugin plugin = new StereoisomerPlugin();
        plugin.setStereoisomerismType(StereoisomerPlugin.TETRAHEDRAL);
        plugin.setMaxNumberOfStereoisomers(STEREOISOMER_LIMIT);
        plugin.setCheck3DStereo(false);

        try {
            plugin.setMolecule(ret);
            plugin.run();

            Molecule[] molecules = plugin.getStereoisomers();
            List<Molecule> molsIn3D = new ArrayList<Molecule>();
            for (Molecule molecule : molecules) {

                heart.ping();
                try {
                    Cleaner.clean(molecule, 3);// this can be slow
                } catch (Exception e) {
                    log.log(Level.INFO, "Could not calciulate 3D coordinates.", e);
                }
                heart.ping();
                if (molecule.getDim() == 3) {
                    molecule.clearProperties();
                    copyProperties(m, molecule);
                    molsIn3D.add(molecule);
                }
            }
            return ImmutableList.copyOf(molsIn3D);

        } catch (PluginException e) {
            throw new RuntimeException(e);
        }

    }

    static void copyProperties(final Molecule from, final Molecule to) {
        copyProperties(from, to, null);
    }

    static void copyProperties(final Molecule from, final Molecule to, String nameSpace) {
        final String[] keys = from.properties().getKeys();

        for (String key : keys) {
            final String property = MPropHandler.convertToString(from.properties(), key);
            if (nameSpace != null) {
                key = nameSpace + key;
            }
            to.setProperty(key, property);
        }
    }

}
