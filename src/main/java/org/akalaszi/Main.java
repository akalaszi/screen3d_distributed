package org.akalaszi;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
        clean3dJob(args).waitForCompletion(true);
    }

    private static Job clean3dJob(String[] args) {
        try {
            Configuration conf = new Configuration();

            Job job = Job.getInstance(conf, "generate3dMols");
            job.setJarByClass(Main.class);
            job.setInputFormatClass(ChemInputFormat.class);

            job.setMapperClass(PreprocessMapper.class);

            job.setCombinerClass(MRecordReducer.class);
            job.setReducerClass(MRecordReducer.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            // job.setSortComparatorClass(OutputKeyComparator.class);

            job.setOutputFormatClass(ChemOutputFormat.class);

            ChemInputFormat.setInputPaths(job, new Path(args[0]));
            ChemOutputFormat.setOutputPath(job, new Path(args[1]));

            return job;
        } catch (IOException io) {
            throw new IllegalStateException(io);
        }
    }

}
