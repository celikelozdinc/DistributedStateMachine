#FROM openjdk:8-jre-alpine
FROM alpine:edge

RUN \
  apk update && \
  apk add --no-cache bash && \
  apk add --no-cache busybox-extras && \
  apk add --no-cache openrc

 # install java #
RUN \
  apk add --no-cache openjdk8

# install mongodb #
RUN \
  echo 'http://dl-cdn.alpinelinux.org/alpine/v3.6/main' >> /etc/apk/repositories && \
  echo 'http://dl-cdn.alpinelinux.org/alpine/v3.6/community' >> /etc/apk/repositories && \
  apk update && \
  apk add --no-cache mongodb=3.4.4-r0 && \
  mkdir -p /data/db && \
  chown -R mongodb /data/db
VOLUME ["/data/db"]
EXPOSE 27017
CMD [ "/usr/bin/mongod" , "--bind_ip", "0.0.0.0"]

# Spring Boot #
ENV SPRINGBOOT_HOME /opt/statemachineapp
COPY out/artifacts/DistributedStateMachine_jar/ $SPRINGBOOT_HOME/DistributedStateMachine_jar/
# Copy entrypoint script #
COPY smoc/start_services.sh  $SPRINGBOOT_HOME/
RUN chmod +x $SPRINGBOOT_HOME/start_services.sh
WORKDIR $SPRINGBOOT_HOME