package org.akalaszi;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

public class PreprocessTest {
    @SuppressWarnings("deprecation")
    @Test
    public void shouldGenerateEnantiomersIn3D() throws IOException {

        InputStream is = PreprocessTest.class.getResourceAsStream("racem.sdf");
        MolImporter molImporter = new MolImporter(is);
        try {
            Molecule m = molImporter.read();
            List<Molecule> result = PreprocessMapper.processStructureForScreen3DRun(m);
            Assert.assertEquals(2, result.size());
            for (Molecule molecule : result) {
                Assert.assertEquals("a", molecule.getProperty("a"));
                Assert.assertEquals("b", molecule.getProperty("b"));
                Assert.assertEquals(3, molecule.getDim());
            }
//            MolGeom.writeMol(result, "enum.mrv");

        } finally {
            molImporter.close();
        }
    }

}
