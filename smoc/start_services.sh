#!/bin/bash
echo "Starting mongodb database service..."
/usr/bin/mongod --bind_ip 0.0.0.0 ;
echo "Starting statemachine jar and redirecting the output to log file..."
/usr/bin/java -Dtimesleep=1000 -jar DistributedStateMachine_jar/DistributedStateMachine.jar > log