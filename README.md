[![Bordy - Building open source Trello with steroids.](/assets/bordy%20-%20frame.png)](https://bordy.io)

# Bordy

Building open source Trello with steroids.

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```
It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/bordy-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Application Setup

### System requirements

Right now Bordy is running well, for public users, on next machine:

- RAM: 1GiB
- CPU: 1 vCPU

Resources consumption (99th percentile):

RAM:

![RAM consumption](/assets/ram-consumption.png)

CPU:

![CPU consumption](/assets/cpu-consumption.png)

### Requirements

- Free [Auth0](https://auth0.com/) account
- Free [Mixpanel](https://mixpanel.com/) account
- [Google Cloud Platform account](https://cloud.google.com) for S3
- Mail account for SMTP
- Single node MongoDB cluster, at least