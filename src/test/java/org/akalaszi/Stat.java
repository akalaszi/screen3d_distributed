package org.akalaszi;

import java.io.IOException;
import java.util.List;

import chemaxon.checkers.ValenceErrorChecker;
import chemaxon.checkers.result.StructureCheckerResult;
import chemaxon.fixers.ValenceFixer;
import chemaxon.formats.MolExporter;
import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.modelling.mm.mmff94.MMFF94;
import chemaxon.struc.Molecule;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class Stat {
    /**
     * Create some statistics for a specific project.
     * 
     * @param args
     * @throws IOException
     * @throws MolFormatException
     */
    public static void main(String[] args) throws MolFormatException, IOException {
        // filterOut2DMols();
        // checkValence();
        getEnergyValues();
    }

    @SuppressWarnings("unused")
    private static void filterOut2DMols() throws MolFormatException, IOException {
        MolImporter mi = new MolImporter("out2_part-r-00000.sdf");
        try {
            MolExporter me = new MolExporter("3D_out2_part-r-00000.mrv", "mrv");
            try {
                Molecule molecule;

                while ((molecule = mi.read()) != null) {
                    if (molecule.getDim() == 3) {
                        me.write(molecule);
                    }
                }
            } finally {
                me.close();
            }
        } finally {
            mi.close();
        }

    }

    @SuppressWarnings("unused")
    private static void checkValence() throws MolFormatException, IOException {
        MolImporter mi = new MolImporter("NCI-merged-all.sdf");
        try {
            MolExporter me = new MolExporter("NCI-merged-all-valence-error.mrv", "mrv");
            try {
                Molecule molecule;
                int c = 0;
                while ((molecule = mi.read()) != null) {
                    Molecule fixed = checkValence(molecule);
                    if (fixed != null) {
                        System.err.println(c++);
                        me.write(molecule);
                        me.write(fixed);
                    }
                }
            } finally {
                me.close();
            }
        } finally {
            mi.close();
        }
    }

    private static class PropMol {
        Molecule m;
        double prop;

        public PropMol(Molecule m, double prop) {
            this.m = m;
            this.prop = prop;
        }
    }

    private static void getEnergyValues() throws MolFormatException, IOException {
        ListMultimap<String, PropMol> multimap = ArrayListMultimap.create();
        MolImporter mi = new MolImporter("3D_out2_part-r-00000.mrv");
        try {
            int c = 0;
            Molecule molecule;
            while ((molecule = mi.read()) != null) {
                @SuppressWarnings("deprecation")
                String id = molecule.getProperty("keyId");
                MMFF94 mm = new MMFF94();
                mm.init(molecule);
                multimap.put(id, new PropMol(molecule, mm.getEnergy()));
                System.err.println(c++);

            }
        } finally {
            mi.close();
        }

        MolExporter me = new MolExporter("3D_out2_part-r-00000-e-filtered.mrv", "mrv");
        try {
            for (String id : multimap.keySet()) {
                List<PropMol> props = multimap.get(id);
                final PropMol min = findMin(props);
                me.write(min.m);
                System.out.println(id + ": " + (props.get(0).prop - min.prop));
            }
        } finally {
            me.close();
        }
    }

    private static PropMol findMin(List<PropMol> props) {
        PropMol ret = null;
        for (PropMol propMol : props) {
            if (ret == null || ret.prop > propMol.prop) {
                ret = propMol;
            }
        }
        return ret;
    }

    public static Molecule checkValence(final Molecule mol) {
        Molecule ret = mol.clone();
        final ValenceErrorChecker valenceErrorChecker = new ValenceErrorChecker();
        final StructureCheckerResult scr = valenceErrorChecker.check(ret);
        if (scr != null) {
            final ValenceFixer valenceFixer = new ValenceFixer();
            valenceFixer.fix(scr);
            return ret;
        }
        return null;
    }
}
