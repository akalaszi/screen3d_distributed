package org.akalaszi;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.mapreduce.Job;
import org.junit.Before;
import org.junit.Test;

public class ChemInputFormatTest {

    // private String[] hosts = new String[] { "h1", "h2" };

    @Before
    public void before() {
    }

    @Test
    public void shouldCreateParsedRecordReader() throws IOException, InterruptedException {
        ChemInputFormat cif = new ChemInputFormat();
        Assert.assertTrue(cif.createRecordReader(null, null) instanceof ParsedRecordReader);
    }

    @Test
    public void shouldSaveInputPathsIntoTheJob() throws IOException, InterruptedException {
        Job job = Job.getInstance();
        Path p1 = new Path("file:/a/bc/d.mol");
        Path p2 = new Path("file:/a/bc/d2.mol");
        ChemInputFormat.setInputPaths(job, p1, p2);

        Path[] expected = new Path[] { p1, p2 };
        Path[] found = ChemInputFormat.getInputPaths(job);
        Assert.assertEquals(expected[0], found[0]);
        Assert.assertEquals(expected[1], found[1]);
    }

    // @Test
    // public void shouldGetTheSplits() throws IOException, InterruptedException {
    //
    // Job job = Job.getInstance();
    // Path p1 = new Path("file:/a/bc/d.mol");
    // Path p2 = new Path("file:/a/bc/d2.mol");
    // ChemInputFormat.setInputPaths(job, p1, p2);
    //
    // List<FileStatus> fs = new ArrayList<FileStatus>();
    // fs.add(createFileStatus());
    //
    // ChemInputFormat cif = Mockito.spy(new ChemInputFormat());
    // Mockito.when(cif.listStatus(job)).thenReturn(fs);
    //
    // cif.getSplits(job);
    // }

    private FileStatus createFileStatus() {
        long length = 1;
        boolean isdir = false;
        int block_replication = -1;
        int blocksize = -1;
        long modification_time = -1;
        long access_time = -1;
        FsPermission permission = null;
        String owner = null;
        String group = null;
        Path symlink = null;
        Path path = new Path("/a/b/c");
        FileStatus fileStatus = new FileStatus(length, isdir, block_replication, blocksize, modification_time,
                access_time, permission, owner, group, symlink, path);
        return fileStatus;
    }
}
