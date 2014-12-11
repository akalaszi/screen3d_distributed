package org.akalaszi;

import org.junit.Assert;
import org.junit.Test;

import chemaxon.formats.MolFormatException;
import chemaxon.struc.MPropertyContainer;

public class MRecordSerilaizerTest {
    @Test
    public void shouldSerilizeToJSON() throws MolFormatException {
        String mol = "CCC";
        String name = "propane";

        MPropertyContainer mpr = new MPropertyContainer();
        mpr.setString("prop1key", "prop1value");
        mpr.setString("prop2key", "prop2value");

        MRecordSerializer m = new MRecordSerializer(name, mol, mpr, "id");
        String expected = "{\"molecule\":\"CCC\",\"name\":\"propane\","
                + "\"properties\":[[\"prop1key\",\"prop1value\"],[\"prop2key\",\"prop2value\"],[\"keyId\",\"id\"]]}";
        Assert.assertEquals(expected, m.toJSON());

    }
}
