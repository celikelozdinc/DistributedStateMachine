#!/bin/bash
echo "Getting container id ..."
container_id=$(docker ps | grep -w distributedstatemachine_"$1" | awk '{print $1}')
echo "Runnig report inside container ""$container_id"
docker exec -it "$container_id" ./report.sh