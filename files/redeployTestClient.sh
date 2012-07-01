#!/bin/bash
$GLASSFISH_HOME/bin/asadmin --port 4848 deploy --force ./client/target/jca-file-client.war
$GLASSFISH_HOME/bin/asadmin restart-domain domain1

