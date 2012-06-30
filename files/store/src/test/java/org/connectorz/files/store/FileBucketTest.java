package org.connectorz.files.store;

import java.io.Closeable;
import java.io.File;
import java.io.PrintWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author adam-bien.com
 */
public class FileBucketTest {

    FileBucket cut;
    String directory = "./current";
    Closeable closeable;

    @Before
    public void initialize() {
        this.closeable = mock(Closeable.class);
        this.cut = new FileBucket(new PrintWriter(System.out), directory, this.closeable);
    }

    @Test
    public void autoClose() throws Exception {
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

    @After
    public void cleanup() {
        new File(directory).delete();
    }
}
