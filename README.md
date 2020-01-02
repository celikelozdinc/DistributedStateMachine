## 0.Build 

You can build images separately:

``
docker-compose build smoc1 smoc2 smoc3
``


or, you can simply use

``
docker-compose build
``

## 1. Start-Up SMOCs

Smocs must be started as detached mode, since they are being started with a MongoDB instance installed:

``
docker-compose up -d smoc1 smoc2 smoc3
``

Then, learn the container id via ``docker ps`` command,

``
docker exec -it <HASH> /bin/sh
``

and then, execute entrypoint script inside container:

``
./start_services.sh
``