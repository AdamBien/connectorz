/*
Copyright 2012 Adam Bien, adam-bien.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.connectorz.files.store;

import java.io.PrintWriter;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import org.connectorz.files.BucketStore;

public class FileBucketStore
        implements BucketStore {

    private ManagedConnectionFactory mcf;
    private Reference reference;
    private ConnectionManager cm;
    private PrintWriter out;

    public FileBucketStore(PrintWriter out,ManagedConnectionFactory mcf, ConnectionManager cm) {
        out.println("#FileDataSource");
        this.mcf = mcf;
        this.cm = cm;
        this.out = out;
    }

    @Override
    public FileBucket getBucket(){
        out.println("#FileDataSource.getConnection " + this.cm + " MCF: " + this.mcf);
        try {
            return (FileBucket) cm.allocateConnection(mcf, getConnectionRequestInfo());
        } catch (ResourceException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
   
    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() {
        return reference;
    }

    private ConnectionRequestInfo getConnectionRequestInfo() {
        return new ConnectionRequestInfo() {

            @Override
            public boolean equals(Object obj) {
                return true;
            }

            @Override
            public int hashCode() {
                return 1;
            }
        };
    }
}
