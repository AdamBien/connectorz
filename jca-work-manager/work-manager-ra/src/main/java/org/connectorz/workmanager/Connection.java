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
package org.connectorz.workmanager;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import javax.resource.ResourceException;
import static javax.resource.spi.ConnectionEvent.*;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

public class Connection
        implements ManagedConnection {

    private ConnectionFactory mcf;
    private PrintWriter out;
    private JCAExecutor executorConnection;
    private ConnectionRequestInfo connectionRequestInfo;
    private List<ConnectionEventListener> listeners;

    Connection(PrintWriter out,ConnectionFactory mcf, ConnectionRequestInfo connectionRequestInfo) {
        this.out = out;
        out.println("#Connection");
        this.mcf = mcf;
        this.connectionRequestInfo = connectionRequestInfo;
        this.listeners = new LinkedList<>();
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {
        out.println("#Connection.getConnection");
        executorConnection = new JCAExecutor(out,this,mcf, connectionRequestInfo);
        return executorConnection;
    }

    @Override
    public void destroy() {
        out.println("#Connection.destroy");
        this.executorConnection.destroy();
    }

    @Override
    public void cleanup() {
        out.println("#Connection.cleanup");
    }

    @Override
    public void associateConnection(Object connection) {
        out.println("#Connection.associateConnection " + connection);
        this.executorConnection = (JCAExecutor) connection;

    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        out.println("#Connection.addConnectionEventListener");
        this.listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        out.println("#Connection.removeConnectionEventListener");
        this.listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource()
            throws ResourceException {
        out.println("#Connection.getXAResource");
        return null;
    }

  

    @Override
    public ManagedConnectionMetaData getMetaData()
            throws ResourceException {
        out.println("#Connection.getMetaData");
        return new ManagedConnectionMetaData() {

            public String getEISProductName()
                    throws ResourceException {
                out.println("#Connection.getEISProductName");
                return "Work Manager JCA";
            }

            @Override
            public String getEISProductVersion()
                    throws ResourceException {
                out.println("#Connection.getEISProductVersion");
                return "1.0";
            }

            @Override
            public int getMaxConnections()
                    throws ResourceException {
                out.println("#Connection.getMaxConnections");
                return mcf.getMaxNumberOfConcurrentRequests();
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
        System.out.println("#Connection.setLogWriter");
        this.out = out;
    }

    @Override
    public PrintWriter getLogWriter()
            throws ResourceException {
        System.out.println("#Connection.getLogWriter");
        return out;
    }
    
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Connection other = (Connection) obj;
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

    public ConnectionRequestInfo getConnectionRequestInfo() {
        return connectionRequestInfo;
    }

    public void fireConnectionEvent(int event) {
        ConnectionEvent connnectionEvent = new ConnectionEvent(this, event);
        connnectionEvent.setConnectionHandle(this.executorConnection);
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

    public void close() {
        this.fireConnectionEvent(CONNECTION_CLOSED);
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
