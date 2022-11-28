# CES Micronaut Example

---

## How to run

### GraalVm

To run this project it is necessary to have graalVm installed on your machine. The easiest way to install GraalVM on
Linux or Mac is to use [SDKMan.io](https://sdkman.io/)

```
sdk install java 22.2.r17-grl
```

For installation on Windows, or for manual installation on Linux or Mac, see
the [GraalVM Getting Started](https://www.graalvm.org/22.2/docs/getting-started/) documentation.

### Build docker image

After you have GraalVm setup you will need to compile the application code to native com and build a docker image. This
can be done with the following command:

```
./gradlew clean dockerBuildNative
```

### Docker compose
After the app image is build you can run the application with docker compose

```
docker-compose up
```

### Run tests as native image

```
./gradlew clean build testNativeImage
```

---
## Swagger
After running the app you can see the available apis on the [Swagger](http://localhost:8080/swagger-ui/index.html)
