package org.akalaszi;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.alignment.AlignmentException;
import chemaxon.marvin.alignment.AlignmentMolecule;
import chemaxon.marvin.alignment.AlignmentMoleculeFactory;
import chemaxon.marvin.alignment.AlignmentProperties.DegreeOfFreedomType;
import chemaxon.marvin.alignment.MMPAlignment;
import chemaxon.marvin.alignment.PairwiseAlignment;
import chemaxon.struc.Molecule;

public class ChemAxonLibraryTest {

    private void readMol(String fileName) throws IOException, MolFormatException {
        InputStream is = ChemAxonLibraryTest.class.getResourceAsStream(fileName);
        MolImporter im = new MolImporter(is);
        @SuppressWarnings("unused")
        Molecule m;
        while ((m = im.read()) != null) {

        }
        im.close();
    }

    @Test
    public void shouldReadMol() throws MolFormatException, IOException {
        readMol("in3.mol");
    }

    @Test
    public void shouldReadSdf() throws MolFormatException, IOException {
        readMol("mols.sdf");
    }

    @Test
    public void shouldReadMrv() throws MolFormatException, IOException {
        readMol("m_1.mrv");
    }

    @Test
    public void shouldPreprocess() throws MolFormatException {
        Molecule m = MolImporter.importMol("CCCCCC");
        MMPAlignment.preprocess(m, true);
    }

    @Test
    public void shouldAlignTwoMolecules() throws MolFormatException, AlignmentException {
        Molecule m = MMPAlignment.preprocess(MolImporter.importMol("CCCCCC"), true);

        Molecule mo = MMPAlignment.preprocess(MolImporter.importMol("CCCOCCC"), true);

        AlignmentMoleculeFactory amf = new AlignmentMoleculeFactory();
        AlignmentMolecule am1 = amf.create(m, DegreeOfFreedomType.TRANSLATE_ROTATE_FLEXIBLE);
        AlignmentMolecule am2 = amf.create(mo, DegreeOfFreedomType.TRANSLATE_ROTATE_FLEXIBLE);

        PairwiseAlignment pa = new PairwiseAlignment();
        pa.setQuery(am1);
        pa.similarity(am2);
//        MolGeom.writeMol(pa.getAlignedMoleculesAsFragments(), "aligned.mrv");

    }
}
