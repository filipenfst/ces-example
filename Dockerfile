FROM ubuntu:22.04

VOLUME /tmp
RUN mkdir -p /opt/app/
WORKDIR /opt/app/

COPY ./build/native/nativeCompile/ces-micronaut-example appExecutable

CMD ./appExecutable
