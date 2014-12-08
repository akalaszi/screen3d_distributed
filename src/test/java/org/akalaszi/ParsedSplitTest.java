package org.akalaszi;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;

public class ParsedSplitTest {

    @Test
    public void testReadWriteArrays() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        DataOutput out = new DataOutputStream(baos);
        String[] a = new String[] { "a", "b", "c" };
        ParsedSplit.writeStringArray(out, a);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        DataInput in = new DataInputStream(bais);
        String[] result = ParsedSplit.readStringArray(in);

        Assert.assertArrayEquals(a, result);

    }

    @Test
    public void testReadWriteParsedSplit() throws IOException, InterruptedException {
        String[] locations = new String[] { "l1", "l2" };
        List<ParsedSplit.Element> elements = new ArrayList<ParsedSplit.Element>();
        elements.add(new ParsedSplit.Element("k1", "v1"));
        elements.add(new ParsedSplit.Element("k2", "v2"));
        ParsedSplit p = new ParsedSplit(elements, locations);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(baos);
        p.write(out);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInput in = new DataInputStream(bais);

        ParsedSplit p2 = new ParsedSplit(new ArrayList<ParsedSplit.Element>(), new String[] {});

        p2.readFields(in);

        Assert.assertArrayEquals(p.getLocations(), p2.getLocations());
        Assert.assertEquals(elements.size(), p2.getElements().size());
        Assert.assertEquals(elements.get(0).getId(), p2.getElements().get(0).getId());
        Assert.assertEquals(elements.get(0).getValue(), p2.getElements().get(0).getValue());
    }
}
