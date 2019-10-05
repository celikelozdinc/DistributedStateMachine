FROM openjdk:8-jre-alpine
RUN apk update && \
apk add bash && \
apk add busybox-extras && \
apk add wget

 # Spring Boot #
 ENV SPRINGBOOT_HOME /opt/statemachineapp
 COPY out/artifacts/DistributedStateMachine_jar/ $SPRINGBOOT_HOME/DistributedStateMachine_jar/
 WORKDIR $SPRINGBOOT_HOME