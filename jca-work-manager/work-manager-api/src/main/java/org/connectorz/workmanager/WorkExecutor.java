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

import java.util.concurrent.Executor;

/**
 * Sample usage:
 <pre>
&#064;Stateless
&#064;TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ThreadsResource {

    <b>&#064;Resource(name="jca/workmanager")</b>
    WorkExecutorFactory executorFactory;
    
    public String threads(){
        try(WorkExecutor executor = executorFactory.newExecutor();){
        Runnable runnable = new Runnable(){
            &#064;Override
            public void run() {
            //some work to do
            }
        };
        executor.execute(runnable);
  }
 * </pre>
 * 
 * @author adam bien, adam-bien.com
 */
public interface WorkExecutor extends Executor,AutoCloseable{
    @Override
    void close();
}
