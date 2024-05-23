#### How to publish data from a Kafka topic to a MigratoryData subject using Kafka Streams adding additional properties to the message


##### Prerequisites
Java Development Kit (JDK) 8+, Gradle 6+ 

#### Clone Project

Clone the getting started project from GitHub using your IDE of choice or using the following command:
> git clone https://github.com/migratorydata/getting-started-kafka-streams.git

#### Start MigratoryData Server and Kafka

> cd docker

> docker compose up

#### Build & Run
Use the following commands to build and run your project:
> ./gradlew clean build

> ./gradlew run

#### Test

Publish some data to the Kafka topic `server-raw` using a kafka producer and check the output of the Kafka topic `server` using
one of the MigratoryData Client API.