package org.connectorz.files.store;

import java.io.Closeable;
import java.io.File;
import java.io.PrintWriter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author adam-bien.com
 */
public class FileBucketTest {

    FileBucket cut;
    Closeable closeable;

    @Rule
    public TemporaryFolder root = new TemporaryFolder();

    @Before
    public void initialize() {
        final String directory = root.getRoot().getAbsolutePath();
        this.closeable = mock(Closeable.class);
        this.cut = new FileBucket(new PrintWriter(System.out), directory, this.closeable);
    }

    @Test
    public void autoClose() throws Exception {
        final String directory = root.getRoot().getAbsolutePath();
        try (FileBucket bucket = new FileBucket(new PrintWriter(System.out), directory, this.closeable);) {
            bucket.begin();
        }
        verify(this.closeable).close();
    }
    
    @Test
    public void writeAndRollback() throws Exception{
        final String key = "hey";
        this.cut.begin();
        final byte[] content = "duke".getBytes();
        this.cut.write(key, content);
        byte[] actual = this.cut.fetch(key);
        assertThat(actual,is(content));
        this.cut.rollback();
        actual = this.cut.fetch(key);
        assertNull(actual);
    
    }
    
    @Test
    public void writeAndCommit() throws Exception{
        final String key = "hey";
        this.cut.begin();
        final byte[] content = "duke".getBytes();
        this.cut.write(key, content);
        byte[] actual = this.cut.fetch(key);
        assertThat(actual,is(content));
        this.cut.commit();
        actual = this.cut.fetch(key);
        assertThat(actual,is(content));
    }
    
    @Test
    public void deleteAndCommit() throws Exception{
        final String key = "hey";
        this.cut.begin();
        final byte[] content = "duke".getBytes();
        this.cut.write(key, content);
        byte[] actual = this.cut.fetch(key);
        assertThat(actual,is(content));
        this.cut.commit();
        this.cut.begin();
        this.cut.delete(key);
        this.cut.commit();
        actual = this.cut.fetch(key);
        assertNull(actual);
    }
}
