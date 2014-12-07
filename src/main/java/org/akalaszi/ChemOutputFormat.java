package org.akalaszi;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ChemOutputFormat extends FileOutputFormat<String, String> {

	public static String SEPERATOR = "mapreduce.output.textoutputformat.separator";

	protected static class MoleculeRecordWriter extends
			RecordWriter<String, String> {
		private static final String utf8 = "UTF-8";
		private static final byte[] newline;
		static {
			try {
				newline = "\n".getBytes(utf8);
			} catch (UnsupportedEncodingException uee) {
				throw new IllegalArgumentException("can't find " + utf8
						+ " encoding");
			}
		}

		protected DataOutputStream out;

		public MoleculeRecordWriter(DataOutputStream out) {
			this.out = out;
		}

		public synchronized void write(String key, String value)
				throws IOException {

			if (value == null) {
				return;
			}
			out.write(value.getBytes(utf8));
			out.write(newline);
		}

		public synchronized void close(TaskAttemptContext context)
				throws IOException {
			out.close();
		}
	}

	public RecordWriter<String, String> getRecordWriter(TaskAttemptContext job)
			throws IOException, InterruptedException {
		Configuration conf = job.getConfiguration();
		String extension = "mrv";

		Path file = getDefaultWorkFile(job, extension);
		FileSystem fs = file.getFileSystem(conf);
		FSDataOutputStream fileOut = fs.create(file, false);
		return new MoleculeRecordWriter(fileOut);
	}
}
