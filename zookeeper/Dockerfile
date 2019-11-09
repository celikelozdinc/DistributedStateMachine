FROM openjdk:8-jre-alpine
RUN apk update && \
apk add bash && \
apk add busybox-extras && \
apk add wget
 
# Zookeeper #
WORKDIR /opt
RUN wget -q kozyatagi.mirror.guzel.net.tr/apache/zookeeper/zookeeper-3.4.14/zookeeper-3.4.14.tar.gz && \
tar -xvf zookeeper-3.4.14.tar.gz && \
mv zookeeper-3.4.14 zookeeper && \
rm -rf zookeeper-3.4.14.tar.gz

ENV ZOOKEEPER_HOME /opt/zookeeper

WORKDIR $ZOOKEEPER_HOME
RUN mkdir zkDataDir && \
mkdir zkDataLogDir && \
touch conf/zoo.cfg && \
echo tickTime=2000 > conf/zoo.cfg && \
echo dataDir=/opt/zookeeper/zkDataDir >> conf/zoo.cfg && \
echo dataLogDir=/opt/zookeeper/zkDataLogDir >> conf/zoo.cfg && \
echo initLimit=10 >> conf/zoo.cfg && \
echo syncLimit=5 >> conf/zoo.cfg && \
echo clientPort=2181 >> conf/zoo.cfg
EXPOSE 2181

WORKDIR  $ZOOKEEPER_HOME/bin
# Start zookeper in foreground mode #
# You should start container with detached mode #
ENTRYPOINT ["/bin/bash","/opt/zookeeper/bin/zkServer.sh","start-foreground"]
