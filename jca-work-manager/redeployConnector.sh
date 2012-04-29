#!/bin/bash
$GLASSFISH_HOME/bin/asadmin --port 4848 deploy --force ./work-manager/target/work-manager.rar
$GLASSFISH_HOME/bin/asadmin restart-domain domain1

