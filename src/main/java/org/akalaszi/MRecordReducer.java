package org.akalaszi;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MRecordReducer extends Reducer<NullWritable, Text, NullWritable, Text> {

    @Override
    public void reduce(NullWritable key, Iterable<Text> values, Context context) throws IOException,
            InterruptedException {
        for (Text value : values) {
            String string = value.toString();
            if (string.endsWith("\n")) {
                value = new Text(string.substring(0, string.length() - 1));
            }
            context.write(key, value);
        }
    }
}