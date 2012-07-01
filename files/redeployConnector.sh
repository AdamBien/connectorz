#!/bin/bash
$GLASSFISH_HOME/bin/asadmin --port 4848 deploy --force ./store/target/jca-file-store.rar
$GLASSFISH_HOME/bin/asadmin restart-domain domain1

