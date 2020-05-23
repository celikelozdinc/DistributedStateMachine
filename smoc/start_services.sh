#!/bin/bash
echo "Starting mongodb database service..."
/usr/bin/mongod --fork --logpath /var/log/mongod.log --bind_ip 0.0.0.0 > /dev/null
echo "Starting statemachine jar and redirecting the output to log file..."
/usr/bin/java -javaagent:ObjectSizeFetcher.jar -Dtimesleep=1000  -jar DistributedStateMachine_jar/DistributedStateMachine.jar > log