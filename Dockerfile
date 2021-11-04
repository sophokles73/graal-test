FROM eclipse-temurin:17-focal
WORKDIR /opt/app
COPY graal-test .
COPY graal-test-*.jar graal-test.jar
CMD ["/bin/bash"]
