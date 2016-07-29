FROM anapsix/alpine-java:8_jdk_unlimited

# Cache dependencies
COPY build.sbt /tmp
COPY activator /tmp
COPY activator-launch-1.3.9.jar /tmp
COPY project/build.properties /tmp/project/
COPY project/plugins.sbt /tmp/project/
RUN cd /tmp && ./activator update


WORKDIR /code
COPY . /code

# Build application
RUN /code/activator compile test universal:packageZipTarball -Dsbt.log.noformat=true

RUN    cd /code/target/universal && \
       tar -xzf dimmer-1.0-SNAPSHOT.tgz

# RUN    cleanup


HEALTHCHECK --interval=5s --timeout=3s --retries=3 \
  CMD wget -nv http://localhost:9000/healthcheck || exit 1

CMD ["target/universal/bin/dimmer", "-Dlogger.resource=logger-config.xml"]

# TODO Do we need a wait-for-it script?
# wget --retry-connrefused --tries=50 --timeout=5 --wait=1 http://localhost:9000/diagnostics/heartbeat



