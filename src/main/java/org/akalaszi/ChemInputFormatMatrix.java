package org.akalaszi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.formats.MolFormatException;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordParseException;
import chemaxon.marvin.io.MRecordReader;

import com.google.common.collect.ImmutableList;

public class ChemInputFormatMatrix extends InputFormat<Text, Text> {

    public static int DEFAULT_RECORDS_PER_SPLIT = 50;

    static List<MRecord> readRecordsFrom(InputStream istream) throws MolFormatException, IOException {
        MRecordReader mr = MFileFormatUtil.createRecordReader(istream, null);
        MRecord record = null;
        List<MRecord> records = new ArrayList<MRecord>();

        try {
            while ((record = mr.nextRecord()) != null) {
                records.add(record);
            }
        } catch (MRecordParseException me) {
            throw new IOException();
        } finally {
            mr.close();
        }
        return ImmutableList.copyOf(records);
    }

    @Override
    public List<InputSplit> getSplits(JobContext job) throws IOException, InterruptedException {
        String[] hosts = ChemInputFormat.getHosts(job);

        List<InputSplit> splits = new ArrayList<InputSplit>();
        int processedRecordCount = 0;
        List<ParsedSplit.Element> toCurrentSplit = new ArrayList<ParsedSplit.Element>();

        Path path = ChemInputFormat.listStatus(job).get(0).getPath();
        FSDataInputStream istream = path.getFileSystem(job.getConfiguration()).open(path);
        List<MRecord> records = readRecordsFrom(istream);

        for (int i = 0; i < records.size() - 1; i++) {
            MRecord ri = records.get(i);
            String idI = getId(ri);
            for (int j = i + 1; i < records.size(); i++) {
                MRecord rj = records.get(j);
                if (isDifferentID(idI, getId(rj))) {

                    if (!toCurrentSplit.isEmpty() && processedRecordCount % DEFAULT_RECORDS_PER_SPLIT == 0) {
                        splits.add(new ParsedSplit(toCurrentSplit, hosts));
                        toCurrentSplit = new ArrayList<ParsedSplit.Element>();
                    }

                    SerializableMRecordPair pair = new SerializableMRecordPair(new SerializableMRecord(ri),
                            new SerializableMRecord(rj));

                    toCurrentSplit.add(new ParsedSplit.Element(String.valueOf(processedRecordCount), pair.toJSON()));
                    processedRecordCount++;

                }
            }
        }

        if (!toCurrentSplit.isEmpty()) {
            splits.add(new ParsedSplit(toCurrentSplit, hosts));
        }

        return splits;
    }

    static boolean isDifferentID(String idI, String idJ) {
        return !idI.split(PreprocessMapper.STEREO_TAG)[0].equals(idJ.split(PreprocessMapper.STEREO_TAG)[0]);
    }

    private String getId(MRecord record) {
        return record.getPropertyContainer().get(PreprocessMapper.PREPROCESS_ID).getPropValue().toString();
    }

    @Override
    public RecordReader<Text, Text> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {

        return new ParsedRecordReader();
    }

}
