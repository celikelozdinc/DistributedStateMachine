#!/bin/bash
echo "Opening new terminal inside "$1"..."
docker exec -it $(docker ps | grep $1 | awk '{print $1}') /bin/sh