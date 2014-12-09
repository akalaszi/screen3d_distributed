package org.akalaszi;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ChemOutputFormat extends FileOutputFormat<Text, Text> {

    protected static class MoleculeRecordWriter extends RecordWriter<Text, Text> {

        protected DataOutputStream out;

        public MoleculeRecordWriter(DataOutputStream out) {
            this.out = out;
        }

        @Override
        public synchronized void write(Text key, Text value) throws IOException {

            if (value == null) {
                return;
            }
            out.write(value.getBytes());
        }

        @Override
        public synchronized void close(TaskAttemptContext context) throws IOException {
            out.close();
        }
    }

    @Override
    public RecordWriter<Text, Text> getRecordWriter(TaskAttemptContext job) throws IOException, InterruptedException {
        Configuration conf = job.getConfiguration();
        String extension = "." + PreprocessMapper.EXTENSION;

        Path file = getDefaultWorkFile(job, extension);
        FileSystem fs = file.getFileSystem(conf);
        FSDataOutputStream fileOut = fs.create(file, false);
        return new MoleculeRecordWriter(fileOut);
    }
}
