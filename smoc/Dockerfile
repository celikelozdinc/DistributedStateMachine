FROM openjdk:8-jre-alpine
RUN apk update && \
apk add bash && \
apk add busybox-extras && \
apk add wget

 # Spring Boot #
 ENV SPRINGBOOT_HOME /opt/statemachineapp
 COPY out/artifacts/DistributedStateMachine_jar/ $SPRINGBOOT_HOME/DistributedStateMachine_jar/
 # Copy entrypoint script #
 COPY smoc/start_services.sh  $SPRINGBOOT_HOME/
 RUN chmod +x $SPRINGBOOT_HOME/start_services.sh
 WORKDIR $SPRINGBOOT_HOME