package org.akalaszi;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
        if (args.length != 3) {
            cliHelp();
        }

        long milliSeconds = 1000 * 60 * 20;
        if ("p".equals(args[0])) {
            runJob("preprocess", milliSeconds, args[1], args[2], ChemInputFormat.class, PreprocessMapper.class);
        } else if ("s".equals(args[0])) {
            runJob("screen", milliSeconds, args[1], args[2], ChemInputFormatMatrix.class, AlignmentMapper.class);
        } else {
            cliHelp();
        }

    }

    private static void cliHelp() {
        System.err.println("Distributed Screen3D");
        System.err.println("Usage: [command] [input path] [output path]");
        System.err.println("\twhere the command:\n\t\t p for preprocess\n\t\ts for screen");
        System.exit(1);
    }

    /**
     * Runs a map reduce job.
     * 
     * @param name name of the job.
     * @param timeout Max allowed time between heart beats from the worker node.
     * @param inputPath If file then processed. if dir, then all files in are processed.
     * @param outputPath Output path dir. Many files will be written here.
     * @param inputFormatClass {@link InputFormat} to process input file.
     * @param mapperClass {@link Mapper} that is doing the calculation.
     */
    private static void runJob(String name, long timeout, String inputPath, String outputPath,
            Class<? extends InputFormat<Text, Text>> inputFormatClass,
            Class<? extends Mapper<Text, Text, NullWritable, Text>> mapperClass) {
        try {
            Configuration conf = new Configuration();
            conf.setLong("mapreduce.task.timeout", timeout);

            Job job = Job.getInstance(conf, name);

            job.setInputFormatClass(inputFormatClass);
            job.setMapperClass(mapperClass);

            job.setJarByClass(Main.class);
            job.setCombinerClass(MRecordReducer.class);
            job.setReducerClass(MRecordReducer.class);

            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);

            job.setOutputFormatClass(TextOutputFormat.class);

            ChemInputFormat.setInputPaths(job, new Path(inputPath));
            TextOutputFormat.setOutputPath(job, new Path(outputPath));

            job.waitForCompletion(true);
        } catch (Exception io) {
            throw new IllegalStateException(io);
        }
    }

}
