# Requirements

Local installation of GraalVM. See https://www.graalvm.org/docs/getting-started/#install-graalvm

The `native image` tooling is required to compile the Java classes into a native executable

```sh
$GRAALVM_HOME/bin/gu install native-image
```

# Building

Set `JAVA_HOME` to your GraalVM installation directory

```sh
export JAVA_HOME=$(GRAALVM_HOME)
```

Then create the native image using

```sh
mvn -Pnative package
```

# Creating Container Image

From the project folder run

```sh
docker build -f Dockerfile -t desired_image_name:desired_tag target/
```

# Run Container

Run native executable using

```sh
docker run --rm -it --memory 100m --memory-swap 100m --cpus 1.0 desired_image_name:desired_tag /opt/app/graal-test
```

Run jar file

```sh
docker run --rm -it --memory 100m --memory-swap 100m --cpus 1.0 desired_image_name:desired_tag java -jar /opt/app/graal-test.jar
```
