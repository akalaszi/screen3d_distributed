package org.akalaszi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.InvalidInputException;
import org.apache.hadoop.mapreduce.security.TokenCache;
import org.apache.hadoop.util.StringUtils;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordParseException;
import chemaxon.marvin.io.MRecordReader;

public class ChemInputFormat extends InputFormat<Text, Text> {

    private static final String INPUT_PATH = "INPUT_PATH";
    public static int DEFAULT_RECORDS_PER_SPLIT = 200;

    /**
     * based on {@link FileInputFormat} List input directories. Subclasses may override to, e.g., select only files
     * matching a regular expression.
     * 
     * @param job the job to list input paths for
     * @return array of FileStatus objects
     * @throws IOException if zero items.
     */
    List<FileStatus> listStatus(JobContext job) throws IOException {
        Path[] dirs = getInputPaths(job);
        if (dirs.length == 0) {
            throw new IOException("No input paths specified in job");
        }

        // get tokens for all the required FileSystems..
        TokenCache.obtainTokensForNamenodes(job.getCredentials(), dirs, job.getConfiguration());

        List<FileStatus> result = new ArrayList<FileStatus>();
        List<IOException> errors = new ArrayList<IOException>();
        for (int i = 0; i < dirs.length; ++i) {
            Path p = dirs[i];
            FileSystem fs = p.getFileSystem(job.getConfiguration());
            FileStatus[] matches = fs.globStatus(p);
            if (matches == null) {
                errors.add(new IOException("Input path does not exist: " + p));
            } else if (matches.length == 0) {
                errors.add(new IOException("Input Pattern " + p + " matches 0 files"));
            } else {
                for (FileStatus globStat : matches) {
                    if (globStat.isDirectory()) {
                        RemoteIterator<LocatedFileStatus> iter = fs.listLocatedStatus(globStat.getPath());
                        while (iter.hasNext()) {
                            LocatedFileStatus stat = iter.next();
                            result.add(stat);
                        }
                    } else {
                        result.add(globStat);
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException(errors);
        }
        return result;
    }

    @Override
    public List<InputSplit> getSplits(JobContext job) throws IOException, InterruptedException {
        List<FileStatus> files = listStatus(job);
        String[] hosts = getHosts(job);

        List<InputSplit> splits = new ArrayList<InputSplit>();
        int processedRecordCount = 0;
        List<ParsedSplit.Element> toCurrentSplit = new ArrayList<ParsedSplit.Element>();

        for (FileStatus file : files) {
            if (file.getLen() == 0) {
                continue;
            }

            int indexInFile = 0;
            Path path = file.getPath();
            FSDataInputStream istream = path.getFileSystem(job.getConfiguration()).open(path);

            MRecordReader mr = MFileFormatUtil.createRecordReader(istream, null);
            MRecord record = null;

            try {
                while ((record = mr.nextRecord()) != null) {

                    if (!toCurrentSplit.isEmpty() && processedRecordCount % DEFAULT_RECORDS_PER_SPLIT == 0) {
                        splits.add(new ParsedSplit(toCurrentSplit, hosts));
                        toCurrentSplit = new ArrayList<ParsedSplit.Element>();
                    }

                    String key = path.getName() + "_" + indexInFile++;
                    MRecordSerializer jsonSerializer = new MRecordSerializer(record, key);
                    toCurrentSplit.add(new ParsedSplit.Element(key, jsonSerializer.toJSON()));
                    processedRecordCount++;
                }

            } catch (MRecordParseException me) {
                throw new IOException();
            } finally {
                mr.close();
            }
        }

        if (!toCurrentSplit.isEmpty()) {
            splits.add(new ParsedSplit(toCurrentSplit, hosts));
        }

        return splits;
    }

    /**
     * @see http://www.infoq.com/articles/HadoopInputFormat
     *
     * @param context
     * @return
     */
    static String[] getHosts(JobContext context) {

        String[] servers = null;
        try {
            JobClient jc = new JobClient((JobConf) context.getConfiguration());
            ClusterStatus status = jc.getClusterStatus(true);
            Collection<String> atc = status.getActiveTrackerNames();
            servers = new String[atc.size()];
            int s = 0;
            for (String serverInfo : atc) {
                StringTokenizer st = new StringTokenizer(serverInfo, ":");
                String trackerName = st.nextToken();
                StringTokenizer st1 = new StringTokenizer(trackerName, "_");
                st1.nextToken();
                servers[s++] = st1.nextToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return servers;

    }

    @Override
    public RecordReader<Text, Text> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {

        return new ParsedRecordReader();
    }

    /**
     * Get the list of input {@link Path}s for the map-reduce job.
     * 
     * @param context The job
     * @return the list of input {@link Path}s for the map-reduce job.
     */
    public static Path[] getInputPaths(JobContext context) {
        String dirs = context.getConfiguration().get(INPUT_PATH, "");
        String[] list = StringUtils.split(dirs);
        Path[] result = new Path[list.length];
        for (int i = 0; i < list.length; i++) {
            result[i] = new Path(StringUtils.unEscapeString(list[i]));
        }
        return result;
    }

    public static void setInputPaths(Job job, Path... inputPaths) throws IOException {
        Configuration conf = job.getConfiguration();
        Path path = inputPaths[0].getFileSystem(conf).makeQualified(inputPaths[0]);
        StringBuffer str = new StringBuffer(StringUtils.escapeString(path.toString()));
        for (int i = 1; i < inputPaths.length; i++) {
            str.append(StringUtils.COMMA_STR);
            path = inputPaths[i].getFileSystem(conf).makeQualified(inputPaths[i]);
            str.append(StringUtils.escapeString(path.toString()));
        }
        conf.set(INPUT_PATH, str.toString());
    }

}
