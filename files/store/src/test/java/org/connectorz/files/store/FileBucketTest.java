package org.connectorz.files.store;

import java.io.Closeable;
import java.io.File;
import java.io.PrintWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

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

    @After
    public void cleanup() {
        new File(directory).delete();
    }
}
