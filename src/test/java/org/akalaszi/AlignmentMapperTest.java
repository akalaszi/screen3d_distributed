package org.akalaszi;

import org.junit.Assert;
import org.junit.Test;

import chemaxon.calculations.clean.Cleaner;
import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.alignment.AlignmentException;
import chemaxon.struc.Molecule;

public class AlignmentMapperTest {
    @SuppressWarnings("deprecation")
    @Test
    public void shouldAlignTwoStructures() throws MolFormatException, AlignmentException {
        Molecule m1 = MolImporter.importMol("CCC(C)CC");
        Cleaner.clean(m1, 3);
        Molecule m2 = MolImporter.importMol("CCC(O)CC");
        Cleaner.clean(m2, 3);
        Molecule res = AlignmentMapper.alignTwoStructures(m1, m2);
        Assert.assertEquals(0.866, Double.parseDouble(res.getProperty("3DTanimoto")), 1e-2);
    }
}
