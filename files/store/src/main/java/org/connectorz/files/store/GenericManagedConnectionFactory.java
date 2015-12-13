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
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.connectorz.files.Bucket;
import org.connectorz.files.BucketStore;

@ConnectionDefinition(connectionFactory = BucketStore.class,
   connectionFactoryImpl = FileBucketStore.class,
   connection = Bucket.class,
   connectionImpl = FileBucket.class)
public class GenericManagedConnectionFactory
        implements ManagedConnectionFactory, Serializable {

    private PrintWriter out;
    @NotNull
    @Size(min = 1)
    private String rootDirectory;

    public GenericManagedConnectionFactory() {
        out = new PrintWriter(System.out);
        out.println("#GenericManagedConnectionFactory.constructor");
    }

    @ConfigProperty(defaultValue = "./store/", supportsDynamicUpdates = true, description = "The root folder of the file store")
    public void setRootDirectory(String rootDirectory) {
        out.println("#FileBucket.setRootDirectory: " + rootDirectory);
        this.rootDirectory = rootDirectory;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        out.println("#GenericManagedConnectionFactory.createConnectionFactory,1");
        return new FileBucketStore(out,this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        out.println("#GenericManagedConnectionFactory.createManagedFactory,2");
        return new FileBucketStore(out,this, null);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo info) {
        out.println("#GenericManagedConnectionFactory.createManagedConnection");
        return new GenericManagedConnection(out,this.rootDirectory,this, info);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo info)
            throws ResourceException {
        out.println("#GenericManagedConnectionFactory.matchManagedConnections Subject " + subject + " Info: " +  info);
        for (Object con : connectionSet) {
            GenericManagedConnection gmc = (GenericManagedConnection) con;
            ConnectionRequestInfo connectionRequestInfo = gmc.getConnectionRequestInfo();
            if((info == null) || connectionRequestInfo.equals(info))
                return gmc;
        }
        throw new ResourceException("Cannot find connection for info!");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        out.println("#GenericManagedConnectionFactory.setLogWriter");
        this.out = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        out.println("#GenericManagedConnectionFactory.getLogWriter");
        return this.out;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GenericManagedConnectionFactory other = (GenericManagedConnectionFactory) obj;
        if (!Objects.equals(this.rootDirectory, other.rootDirectory)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.rootDirectory);
        return hash;
    }

}
