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
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.work.WorkManager;
import javax.security.auth.Subject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
/**
 * @author adam bien, adam-bien.com
 */
@ConnectionDefinition(connectionFactory = WorkExecutorFactory.class,
connectionFactoryImpl = JCAExecutorFactory.class,
connection = Executor.class,
connectionImpl = JCAExecutor.class)
public class ConnectionFactory
        implements ManagedConnectionFactory, ResourceAdapterAssociation, Serializable {

    private final Logger logger = Logger.getLogger("connectorz#jca-work-manager");
    private LogWriter log;
    private PrintWriter out;
    private WorkManagerBootstrap workManagerAdapter;
    @NotNull
    @Min(1)
    private Integer maxNumberOfConcurrentRequests;
    

    public ConnectionFactory() {
        log = new LogWriter(logger);
        log.println("#ConnectionFactory.constructor");
    }

    @ConfigProperty(defaultValue = "2", supportsDynamicUpdates = true, description = "Maximum number of concurrent connections from different processes that an EIS instance can supportMaximum number of concurrent connections from different processes that an EIS instance can support")
    public void setMaxNumberOfConcurrentRequests(Integer maxNumberOfConcurrentRequests) {
        this.maxNumberOfConcurrentRequests = maxNumberOfConcurrentRequests;
    }

    public Integer getMaxNumberOfConcurrentRequests() {
        return maxNumberOfConcurrentRequests;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        log.println("#ConnectionFactory.createConnectionFactory,1");
        return new JCAExecutorFactory(log, this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        log.println("#ConnectionFactory.createManagedFactory,2");
        return new JCAExecutorFactory(log, this, null);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo info) {
        log.println("#ConnectionFactory.createManagedConnection");
        return new Connection(log, this, info);
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo info)
            throws ResourceException {
        log.println("#ConnectionFactory.matchManagedConnections Subject " + subject + " Info: " + info);
        for (Iterator it = connectionSet.iterator(); it.hasNext();) {
            Object object = it.next();
            if (object instanceof Connection) {
                Connection gmc = (Connection) object;
                ConnectionRequestInfo connectionRequestInfo = gmc.getConnectionRequestInfo();
                if ((info == null) || connectionRequestInfo.equals(info)) {
                    return gmc;
                }
            }else{
                log.println("#ConnectionFactory.matchManagedConnections " + object + " is not a Connection");
            }
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        out.println("#ConnectionFactory.setLogWriter");
        this.out = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        log.println("#ConnectionFactory.getLogWriter");
        return this.out;
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return this.workManagerAdapter;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        this.workManagerAdapter = (WorkManagerBootstrap) ra;
    }

    public WorkManager getWorkManager() {
        return this.workManagerAdapter.getBootstrapContext().getWorkManager();
    }
}
