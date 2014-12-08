package org.akalaszi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.akalaszi.ParsedSplit.Element;
import org.junit.Test;

public class ParsedRecordReaderTest {
    private final String[] locations = new String[] { "l1", "l2", "l3" };

    @Test
    public void test() throws IOException, InterruptedException {

        List<Element> elements = new ArrayList<Element>();
        elements.add(new Element("1", "val1"));
        elements.add(new Element("2", "val2"));
        elements.add(new Element("3", "val3"));

        ParsedSplit ps = new ParsedSplit(elements, locations);

        ParsedRecordReader prr = new ParsedRecordReader();
        prr.initialize(ps, null);

        Assert.assertEquals("1", prr.getCurrentKey());
        Assert.assertEquals("val1", prr.getCurrentValue());
        Assert.assertEquals(0.33, prr.getProgress(), 1e-2);
        Assert.assertTrue(prr.nextKeyValue());

        Assert.assertEquals("2", prr.getCurrentKey());
        Assert.assertEquals("val2", prr.getCurrentValue());
        Assert.assertEquals(0.66, prr.getProgress(), 1e-2);
        Assert.assertTrue(prr.nextKeyValue());

        Assert.assertEquals("3", prr.getCurrentKey());
        Assert.assertEquals("val3", prr.getCurrentValue());
        Assert.assertEquals(1.0, prr.getProgress(), 1e-2);
        Assert.assertFalse(prr.nextKeyValue());

        prr.close();
    }

}
