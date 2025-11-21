# Coraza WAF Kubernetes Operator

This operator showcases an app-centric WAF deployment using envoyproxy, OWASP Coraza and OWASP Core Rule Set.
It deploy an additional service and deployment which can be placed in front of the application.
The Ingress or Gateway then needs to be adjusted to point to the WAF service instead directly to the application
to activate the protection.

## Development

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```
> **_NOTE:_**  Quarkus Dev UI: <http://localhost:8080/q/dev/>.

### Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```
> **_NOTE:_** To ensure no reflection issues occure, you might need to run `ch.usp.oss.corazawafoperator.tools.GenerateReflectionConfig` first.
> This will update the `reflection-config.json` automatically.

### Building the container

See the instruction in `src/main/docker/Dockerfile.native` on how to build the container.