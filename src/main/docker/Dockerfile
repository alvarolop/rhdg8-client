FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:1.16

COPY ./target/*.jar /deployments/

# podman build -t localhost/rhdg8-client:latest -f src/main/docker/Dockerfile .

# podman run --rm --name rhdg8-client --net host -v ./src/main/resources/logback-spring.xml:/deployments/src/main/resources/logback-spring.xml:Z localhost/rhdg8-client:latest

# podman run --rm --name rhdg8-client --net host -v ./src/main/resources/logback-spring.xml:/deployments/src/main/resources/logback-spring.xml:Z quay.io/alopezme/rhdg8-client:latest
