# core-java-spring-legacy-support
Support systems to use 4.1.2 minimal API in the 4.1.3 framework

### Requirements

The project has the following dependencies:
* **JRE/JDK 11** [Download from here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
* **Maven 3.5+** [Download from here](http://maven.apache.org/download.cgi) | [Install guide](https://www.baeldung.com/install-maven-on-windows-linux-mac)

### How to use it?

1) Compile and run Arrowhead Core Systems from the [legacy-support](https://github.com/arrowhead-f/core-java-spring/tree/legacy-support) branch of core-java-spring repository.
2) Compile and run Service Registry Translator and Orchestrator Translator.

### Important notes:

* Inter-Cloud orchestration and service consuming are not supported.
* If the translators are being used within a local cloud, than all **application systems (v4.1.2 and v4.1.3)** have to send their requests via the translator systems.
