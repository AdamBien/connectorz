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

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 *
 * @author adam bien, blog.adam-bien.com
 */
@Connector(reauthenticationSupport = false,
transactionSupport = TransactionSupport.TransactionSupportLevel.LocalTransaction)
public class FileAdapter implements ResourceAdapter {


    public void start(BootstrapContext bc) throws ResourceAdapterInternalException {
    }

    public void stop() {
    }

    public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as) throws ResourceException {
    }

    public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) {
    }

    public XAResource[] getXAResources(ActivationSpec[] ass) throws ResourceException {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    
    
}
