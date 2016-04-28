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

import static javax.resource.spi.ConnectionEvent.CONNECTION_CLOSED;
import static javax.resource.spi.ConnectionEvent.LOCAL_TRANSACTION_COMMITTED;
import static javax.resource.spi.ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK;
import static javax.resource.spi.ConnectionEvent.LOCAL_TRANSACTION_STARTED;

import java.io.Closeable;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.DissociatableManagedConnection;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

public class GenericManagedConnection
        implements ManagedConnection, LocalTransaction, Closeable, DissociatableManagedConnection {

    private PrintWriter out;
    private FileBucket fileConnection;
    private ConnectionRequestInfo connectionRequestInfo;
    private List<ConnectionEventListener> listeners;
    private final String rootDirectory;

    GenericManagedConnection(PrintWriter out,String rootDirectory,ManagedConnectionFactory mcf, ConnectionRequestInfo connectionRequestInfo) {
        this.out = out;
        this.rootDirectory = rootDirectory;
        out.println("#GenericManagedConnection");
        this.connectionRequestInfo = connectionRequestInfo;
        this.listeners = new LinkedList<>();
        this.fileConnection = new FileBucket(out,this.rootDirectory,this);
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {
        out.println("#GenericManagedConnection.getConnection");
        return fileConnection;
    }

    @Override
    public void destroy() {
        out.println("#GenericManagedConnection.destroy");
        this.fileConnection.destroy();
    }

    @Override
    public void cleanup() {
        out.println("#GenericManagedConnection.cleanup");
        this.fileConnection.clear();
    }

    @Override
    public void associateConnection(Object connection) {
        out.println("#GenericManagedConnection.associateConnection " + connection);
        this.fileConnection = (FileBucket) connection;

    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        out.println("#GenericManagedConnection.addConnectionEventListener");
        this.listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        out.println("#GenericManagedConnection.removeConnectionEventListener");
        this.listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource()
            throws ResourceException {
        out.println("#GenericManagedConnection.getXAResource");
        throw new ResourceException("XA protocol is not supported by the file-jca adapter");
    }

    @Override
    public LocalTransaction getLocalTransaction() {
        out.println("#GenericManagedConnection.getLocalTransaction");
        return this;
    }

    @Override
    public ManagedConnectionMetaData getMetaData()
            throws ResourceException {
        out.println("#GenericManagedConnection.getMetaData");
        return new ManagedConnectionMetaData() {

            public String getEISProductName()
                    throws ResourceException {
                out.println("#GenericManagedConnection.getEISProductName");
                return "File JCA";
            }

            public String getEISProductVersion()
                    throws ResourceException {
                out.println("#GenericManagedConnection.getEISProductVersion");
                return "1.0";
            }

            public int getMaxConnections()
                    throws ResourceException {
                out.println("#GenericManagedConnection.getMaxConnections");
                return 5;
            }

            public String getUserName()
                    throws ResourceException {
                return null;
            }
        };
    }

    @Override
    public void setLogWriter(PrintWriter out)
            throws ResourceException {
        System.out.println("#GenericManagedConnection.setLogWriter");
        this.out = out;
    }

    @Override
    public PrintWriter getLogWriter()
            throws ResourceException {
        System.out.println("#GenericManagedConnection.getLogWriter");
        return out;
    }


    ConnectionRequestInfo getConnectionRequestInfo() {
        return connectionRequestInfo;
    }

    @Override
    public void begin() throws ResourceException {
        this.fileConnection.begin();
        this.fireConnectionEvent(LOCAL_TRANSACTION_STARTED);
    }

    @Override
    public void commit() throws ResourceException {
        this.fileConnection.commit();
        this.fireConnectionEvent(LOCAL_TRANSACTION_COMMITTED);
    }

    @Override
    public void rollback() throws ResourceException {
        this.fileConnection.rollback();
        this.fireConnectionEvent(LOCAL_TRANSACTION_ROLLEDBACK);
    }

    public void fireConnectionEvent(int event) {
        ConnectionEvent connnectionEvent = new ConnectionEvent(this, event);
        connnectionEvent.setConnectionHandle(this.fileConnection);
        for (ConnectionEventListener listener : this.listeners) {
            switch (event) {
                case LOCAL_TRANSACTION_STARTED:
                    listener.localTransactionStarted(connnectionEvent);
                    break;
                case LOCAL_TRANSACTION_COMMITTED:
                    listener.localTransactionCommitted(connnectionEvent);
                    break;
                case LOCAL_TRANSACTION_ROLLEDBACK:
                    listener.localTransactionRolledback(connnectionEvent);
                    break;
                case CONNECTION_CLOSED:
                    listener.connectionClosed(connnectionEvent);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown event: " + event);
            }
        }
    }

    @Override
   public void close() {
        this.fireConnectionEvent(CONNECTION_CLOSED);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GenericManagedConnection other = (GenericManagedConnection) obj;
        if (this.connectionRequestInfo != other.connectionRequestInfo && (this.connectionRequestInfo == null || !this.connectionRequestInfo.equals(other.connectionRequestInfo))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.connectionRequestInfo != null ? this.connectionRequestInfo.hashCode() : 0);
        return hash;
    }

    @Override
    public void dissociateConnections() throws ResourceException {
        fileConnection = null; 
    }
}
