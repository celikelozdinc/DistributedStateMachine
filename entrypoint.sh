#!/bin/bash
echo "Opening new terminal inside "$1"..."
docker exec -it $(docker ps | grep -w distributedstatemachine_"$1" | awk '{print $1}') /bin/sh