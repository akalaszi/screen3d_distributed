package org.akalaszi;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
        clean3dJob(args).waitForCompletion(true);
    }

    private static Job clean3dJob(String[] args) {
        try {
            Configuration conf = new Configuration();
            long milliSeconds = 1000*60*20; //<default is 600000, likewise can give any value)
            conf.setLong("mapreduce.task.timeout", milliSeconds);
            
            Job job = Job.getInstance(conf, "generate3dMols");
            job.setJarByClass(Main.class);
            job.setInputFormatClass(ChemInputFormat.class);

            job.setMapperClass(PreprocessMapper.class);

            job.setCombinerClass(MRecordReducer.class);
            job.setReducerClass(MRecordReducer.class);

            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);

            job.setOutputFormatClass(TextOutputFormat.class);

            ChemInputFormat.setInputPaths(job, new Path(args[0]));
            TextOutputFormat.setOutputPath(job, new Path(args[1]));

            return job;
        } catch (IOException io) {
            throw new IllegalStateException(io);
        }
    }

}
