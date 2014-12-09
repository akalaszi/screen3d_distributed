package org.akalaszi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class ParsedSplit extends InputSplit implements Writable {

    private Element[] elements;
    private String[] locations;
    private static Logger log = Logger.getLogger(ParsedSplit.class.getName());

    @Override
    public long getLength() throws IOException, InterruptedException {
        long ret = 0;
        for (Element element : elements) {
            ret += element.size();
        }
        return ret;
    }

    public ParsedSplit() {
    }

    public ParsedSplit(List<Element> elements, String[] locations) {
        this.elements = elements.toArray(new Element[elements.size()]);
        this.locations = locations;
        log.log(Level.INFO, Arrays.toString(locations));
    }

    @Override
    public String[] getLocations() throws IOException, InterruptedException {
        return locations;
    }

    public List<Element> getElements() {
        return ImmutableList.copyOf(elements);
    }

    public static class Element {
        private final String id;
        private final String value;

        public Element(String id, String value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public long size() {
            return id.length() + value.length();
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {

        String[] ids = new String[elements.length];
        String[] values = new String[elements.length];
        for (int i = 0; i < elements.length; i++) {
            ids[i] = elements[i].id;
            values[i] = elements[i].value;
        }

        writeStringArray(out, locations);
        writeStringArray(out, ids);
        writeStringArray(out, values);

    }

    static void writeStringArray(DataOutput out, String[] a) throws IOException {
        out.writeInt(a.length);
        for (String location : a) {
            Text.writeString(out, location);
        }
    }

    static String[] readStringArray(DataInput in) throws IOException {
        int length = in.readInt();
        String[] a = new String[length];
        for (int i = 0; i < a.length; i++) {
            a[i] = Text.readString(in);
        }
        return a;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.locations = readStringArray(in);

        String[] ids = readStringArray(in);
        String[] values = readStringArray(in);

        Preconditions.checkState(ids.length == values.length);
        this.elements = new Element[ids.length];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = new Element(ids[i], values[i]);
        }
    }

}
