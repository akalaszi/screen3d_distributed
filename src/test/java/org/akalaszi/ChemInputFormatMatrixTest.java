package org.akalaszi;

import org.junit.Assert;
import org.junit.Test;

public class ChemInputFormatMatrixTest {
    @Test
    public void shouldReturnTrueForDifferentMolIdsOnly() {
        String idI = "abc" + PreprocessMapper.STEREO_TAG + "123";
        String idJ = "abc" + PreprocessMapper.STEREO_TAG + "1234";
        Assert.assertFalse(ChemInputFormatMatrix.isDifferentID(idI, idJ));
        
        String idJ2 = "abc2" + PreprocessMapper.STEREO_TAG + "1234";
        Assert.assertTrue(ChemInputFormatMatrix.isDifferentID(idI, idJ2));
    }
}
