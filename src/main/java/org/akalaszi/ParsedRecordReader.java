package org.akalaszi;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class ParsedRecordReader extends RecordReader<Text, Text> {
    private List<ParsedSplit.Element> elements;
    private int currentIndex = 0;
    private int size;

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        elements = ((ParsedSplit) split).getElements();
        currentIndex = 0;
        size = elements.size();
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        currentIndex++;
        if (currentIndex >= size) {
            return false;
        }

        return true;
    }

    @Override
    public Text getCurrentKey() throws IOException, InterruptedException {
        return new Text(elements.get(currentIndex).getId());
    }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException {
        return new Text(elements.get(currentIndex).getValue());
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        double currentPos = Math.min(currentIndex + 1, size);
        return (float) (currentPos / size);
    }

    @Override
    public void close() throws IOException {

    }

}
