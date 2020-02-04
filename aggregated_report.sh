#!/bin/bash
smocs=("smoc1" "smoc2" "smoc3" "smoc4" "smoc5" "smoc6" "smoc7" "smoc8" "smoc9" "smoc10" "smoc11" "smoc12" "smoc13" "smoc14" "smoc15" "smoc16")
for elem in "${smocs[@]}";
do
if [[ $(docker ps | grep -w distributedstatemachine_"$elem") ]];
then
container_id=$(docker ps | grep -w distributedstatemachine_"$elem" | awk '{print $1}');
docker exec -it "$container_id" ./report.sh;
else
echo "not found";
fi;
done