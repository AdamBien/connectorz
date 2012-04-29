package org.connectorz.threading;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author adam bien, adam-bien.com
 */
public class ThreadsResourceIT {
    
    private Client client;
    private WebResource threads;
    
    @Before
    public void init(){
        this.client = Client.create();
        this.threads = this.client.resource("http://localhost:8080/work-manager-client/resources/").path("threads");
        
    }

    @Test
    public void overload() {
        String loops = "5";
        String expected = "Done";
        String actual = this.threads.path(loops).get(String.class);
        assertThat(actual,is(expected));
    }

    @Test
    public void ping() {
        String expected = "Done";
        String actual = this.threads.get(String.class);
        assertThat(actual,is(expected));
    }
}
