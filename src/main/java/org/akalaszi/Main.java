package org.akalaszi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.akalaszi.WordCount.IntSumReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

public class Main {
	private final static Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		try {
			Job job = clean3dJob(args);
			System.exit(job.waitForCompletion(true) ? 0 : 1);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not finish job.", e);
		}
	}

	private static Job clean3dJob(String[] args) {
		try {
			Configuration conf = new Configuration();
			Job job = Job.getInstance(conf, "generate3dMols");
			job.setJarByClass(Main.class);
			job.setInputFormatClass(ChemInputFormat.class);

			job.setMapperClass(PreprocessMapper.class);

			job.setCombinerClass(IntSumReducer.class);
			job.setReducerClass(IntSumReducer.class);

			job.setOutputKeyClass(String.class);
			job.setOutputValueClass(String.class);
			
			job.setOutputFormatClass(ChemOutputFormat.class);

			ChemInputFormat.setInputPaths(job, new Path(args[0]));
			ChemOutputFormat.setOutputPath(job, new Path(args[1]));
			return job;
		} catch (IOException io) {
			throw new IllegalStateException(io);
		}
	}

}
