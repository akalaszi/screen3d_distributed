package org.akalaszi;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer;

public class MRecordReducer extends Reducer<String, String, String, String> {

	@Override
	public void reduce(String key, Iterable<String> values, Context context)
			throws IOException, InterruptedException {

		for (String value : values) {
			context.write(key, value);
		}
	}
}