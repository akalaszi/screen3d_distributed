package org.akalaszi;

import java.io.IOException;
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
    public static Logger log = Logger.getLogger(PreprocessMapper.class.getName());

    static interface HeartBeat {
        void ping();
    }
    
    @Override
    public void map(Text key, Text mrecord, final Context context) throws IOException, InterruptedException {
        try {
            Molecule original = MRecordSerializer.fromJSON(mrecord.toString()).toMolecule();
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
                    id += "_stereo_" + i;
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
            for (Molecule molecule : molecules) {

                heart.ping();
                Cleaner.clean(molecule, 3);//this can be slow
                heart.ping();
                
                molecule.clearProperties();
                copyProperties(m, molecule);
            }
            return ImmutableList.copyOf(molecules);

        } catch (PluginException e) {
            throw new RuntimeException(e);
        }

    }

    private static void copyProperties(final Molecule from, final Molecule to) {
        final String[] keys = from.properties().getKeys();

        for (final String key : keys) {
            final String property = MPropHandler.convertToString(from.properties(), key);
            to.setProperty(key, property);
        }
    }

}
