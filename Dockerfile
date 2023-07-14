FROM ghcr.io/graalvm/graalvm-ce:ol7-java17-22.3.0-b2

VOLUME /tmp
RUN mkdir -p /opt/app/
WORKDIR /opt/app/

RUN mkdir -p /opt/app/code

WORKDIR /opt/app/code
COPY ./ .

RUN ./gradlew clean nativeCompile
WORKDIR /opt/app/

RUN cp /opt/app/code/build/native/nativeCompile/ces-micronaut-example appExecutable
RUN rm -r /opt/app/code
#COPY ./build/native/nativeCompile/ces-micronaut-example appExecutable

CMD ./appExecutable
