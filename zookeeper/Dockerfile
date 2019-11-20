FROM openjdk:8-jre-alpine

MAINTAINER celikelozdinc "ozdinc.celikel@gmail.com"

ENV ZK_VERSION=zookeeper-3.4.14
ENV ZOOKEEPER_HOME=/opt/zookeeper
ENV PORT=2181

RUN apk update && \
apk add bash && \
apk add busybox-extras && \
apk add wget
 
# Zookeeper #
WORKDIR /opt
RUN wget -q kozyatagi.mirror.guzel.net.tr/apache/zookeeper/${ZK_VERSION}/${ZK_VERSION}.tar.gz && \
tar -xvf ${ZK_VERSION}.tar.gz && \
mv ${ZK_VERSION} zookeeper && \
rm -rf ${ZK_VERSION}.tar.gz


WORKDIR $ZOOKEEPER_HOME
RUN mkdir zkDataDir && \
mkdir zkDataLogDir && \
touch conf/zoo.cfg && \
echo tickTime=2000 > conf/zoo.cfg && \
echo dataDir=/opt/zookeeper/zkDataDir >> conf/zoo.cfg && \
echo dataLogDir=/opt/zookeeper/zkDataLogDir >> conf/zoo.cfg && \
echo initLimit=10 >> conf/zoo.cfg && \
echo syncLimit=5 >> conf/zoo.cfg && \
echo clientPort=${PORT} >> conf/zoo.cfg
EXPOSE ${PORT}

WORKDIR  $ZOOKEEPER_HOME/bin
# Start zookeper in foreground mode #
# You should start container with detached mode #
ENTRYPOINT ["/bin/bash","/opt/zookeeper/bin/zkServer.sh","start-foreground"]
