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
package org.connectorz.threading;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.connectorz.workmanager.WorkExecutorFactory;

/**
 *
 * @author adam bien, adam-bien.com
 */
@Path("threads")
@Stateless
@Produces(MediaType.TEXT_PLAIN)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ThreadsResource {
    
    @Resource(name="jca/workmanager")
    WorkExecutorFactory executorFactory;
    
    @GET
    public String threads(){
        Executor executor = executorFactory.newExecutor();
        Runnable runnable = new Runnable(){

            @Override
            public void run() {
                System.out.println("---- " + new Date());
            }
        
        };
        executor.execute(runnable);
        return "Done";
    }

    @GET
    @Path("{nr}")
    public String overload(@PathParam("nr") int numberOfLoops){
        Executor executor = executorFactory.newExecutor();
        Runnable runnable = new Runnable(){

            @Override
            public void run() {
                System.out.println("--before sleep");
                try {
                    Thread.sleep(2000);
                    System.out.println("--after sleep");
                } catch (InterruptedException ex) {
                    Logger.getLogger(ThreadsResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
        };
        for(int i=0;i<numberOfLoops;i++){
            executor.execute(runnable);
        }
        return "Done";
    }
}
