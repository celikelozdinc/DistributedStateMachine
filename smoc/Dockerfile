#FROM openjdk:8-jre-alpine
FROM alpine:edge

MAINTAINER celikelozdinc "ozdinc.celikel@gmail.com"
ARG MONGO_VERSION=3.4.4-r0
ARG JDK_VERSION=openjdk8

RUN \
  apk update && \
  apk add --no-cache bash && \
  apk add --no-cache busybox-extras && \
  apk add --no-cache openrc

 # install java #
RUN \
  apk add --no-cache ${JDK_VERSION}

# install mongodb #
RUN \
  echo 'http://dl-cdn.alpinelinux.org/alpine/v3.6/main' >> /etc/apk/repositories && \
  echo 'http://dl-cdn.alpinelinux.org/alpine/v3.6/community' >> /etc/apk/repositories && \
  apk update && \
  apk add --no-cache mongodb=${MONGO_VERSION} && \
  mkdir -p /data/db && \
  mkdir -p /data/logs && \
  mkdir -p /data/config && \
  chown -R mongodb /data/db && \
  chown -R mongodb /data/logs && \
  chown -R mongodb /data/config && \
  rm -rf /var/cache/apk*
VOLUME ["/data"]
EXPOSE 27017
CMD [ "/usr/bin/mongod" , "--bind_ip", "0.0.0.0"]

# Spring Boot #
ARG SPRINGBOOT_HOME /opt/statemachineapp
COPY out/artifacts/DistributedStateMachine_jar/ $SPRINGBOOT_HOME/DistributedStateMachine_jar/
# Copy entrypoint script #
COPY smoc/start_services.sh  $SPRINGBOOT_HOME/
RUN chmod +x $SPRINGBOOT_HOME/start_services.sh
WORKDIR $SPRINGBOOT_HOME