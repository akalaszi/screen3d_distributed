package org.akalaszi;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class ParsedRecordReader extends RecordReader<String, String> {
	private List<ParsedSplit.Element> elements;
	private int currentIndex = 0;
	private int size;

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
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
	public String getCurrentKey() throws IOException, InterruptedException {
		return elements.get(currentIndex).getId();
	}

	@Override
	public String getCurrentValue() throws IOException, InterruptedException {
		return elements.get(currentIndex).getValue();
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		double currentPos = Math.min(currentIndex + 1, size);
		return (float) (currentPos / (double) size);
	}

	@Override
	public void close() throws IOException {

	}

}
