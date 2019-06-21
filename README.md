[in progress]

## Aggregating Spring Boot logs with Elastic Stack and Docker

This post describes how to aggregate logs from Spring Boot applications with Elastic Stack.

## What are logs and what are they meant for?

The [Twelve-Factor App methodology][12factor], a set of best practices for building software as a service applications, define logs as _a stream of aggregated, time-ordered events collected from the output streams of all running processes and backing services_ which _provide visibility into the behavior of a running app._



 these best practices recommends that logs should be treated as _event streams_:

> A twelve-factor app never concerns itself with routing or storage of its output stream. It should not attempt to write to or manage logfiles. Instead, each running process writes its event stream, unbuffered, to `stdout`. During local development, the developer will view this stream in the foreground of their terminal to observe the app’s behavior.
>
> In staging or production deploys, each process’ stream will be captured by the execution environment, collated together with all other streams from the app, and routed to one or more final destinations 
for viewing and long-term archival. These archival destinations are not visible to or configurable by the app, and instead are completely managed by the execution environment.

## What is Elastic Stack?

Elastic Stack is a group of open source products from Elastic designed to help users take data from any type of source and in any format and search, analyze, and visualize that data in real time. The product group was formerly known as ELK Stack, in which the letters in the name stood for the products in the group: Elasticsearch, Logstash and Kibana. A fourth product, Beats, was subsequently added to the stack, rendering the potential acronym unpronounceable. 

The Elastic Stack is the next evolution of the ELK Stack.

### Elasticsearch

Elasticsearch is a real-time, distributed storage, search, and analytics engine. It can be used for many purposes, but one context where it excels is indexing streams of semi-structured data, such as logs or decoded network packets.

Elasticsearch is a distributed, JSON-based search and analytics engine designed for horizontal scalability, maximum reliability, and easy management.

### Kibana

Kibana is an open source analytics and visualization platform designed to work with Elasticsearch. You use Kibana to search, view, and interact with data stored in Elasticsearch indices. You can easily perform advanced data analysis and visualize your data in a variety of charts, tables, and maps.

Kibana gives shape to your data.

### Beats

The Beats are open source data shippers that you install as agents on your servers to send operational data to Elasticsearch. Beats can send data directly to Elasticsearch or via Logstash, where you can further process and enhance the data. To ship log files, we will use Filebeat.

### Logstash

Logstash is a powerful tool that integrates with a wide variety of deployments. It offers a large selection of plugins to help you parse, enrich, transform, and buffer data from a variety of sources. If your data requires additional processing that is not available in Beats, then you need to add Logstash to your deployment.

Logstash is a dynamic data collection pipeline with an extensible plugin ecosystem

### Putting the pieces together

![Elastic Stack][img.elastic-stack]

## Introdicing our services

For this example, let's consider two services:

![Movie and review services][img.services]

The `movie-service` manages information related to movies while the `review-service` manages information related to the reviews of each movie. For simplicity, we'll support only `GET` requests.

## Tracing the requests

Unlike in a monolithic application, a single business operation is split across a number of services. When something goes wrong, it's important to be able to trace each operation across the services.

To trace the request across multiple services, we'll use [Spring Cloud Sleuth][spring-cloud-sleuth]. Implements a distributed tracing solution for Spring Cloud. For us, application developers, Sleuth is invisible, and all your interactions with external systems should be instrumented automatically.

Spring Cloud Sleuth implements a distributed tracing solution for Spring Cloud.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-sleuth</artifactId>
            <version>${spring-cloud-sleuth.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
    </dependency>
</dependencies>
```

[to be cobtinued]

## Creating the log appender

[coming soon]

## Elastic Stack with Docker

To get up and running quicker, we'll run our application in Docker containers. As we need multiple containers, we'll use Docker Compose.

With Docker Compose, we use a YAML file to configure our application’s services.

![Elastic Stack][img.elastic-stack-docker]

### Building the Spring Boot applications

- Change to the `review-service` folder: `cd review-service`
- Build the application and create a Docker image: `mvn clean install`

```java
...
[INFO] Successfully tagged cassiomolin/review-service:latest
[INFO] 
[INFO] Detected build of image with id e69b67fcd80a
[INFO] Building jar: /Users/cassiomolin/Projects/log-aggregationt/review-service/target/review-service-1.0-SNAPSHOT-docker-info.jar
[INFO] Successfully built cassiomolin/review-service:latest
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.236 s
[INFO] Finished at: 2019-06-20T22:37:33+01:00
[INFO] ------------------------------------------------------------------------
```

- Change to the parent folder: `cd ..`
- Change to the `movie-service` folder: `cd movie-service`
- Build the application and create a Docker image: `mvn clean install`

```java
...
[INFO] Successfully tagged cassiomolin/movie-service:latest
[INFO] 
[INFO] Detected build of image with id 0cc25953d5ec
[INFO] Building jar: /Users/cassiomolin/Projects/log-aggregation/movie-service/target/movie-service-1.0-SNAPSHOT-docker-info.jar
[INFO] Successfully built cassiomolin/movie-service:latest
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  10.045 s
[INFO] Finished at: 2019-06-20T22:39:48+01:00
[INFO] ------------------------------------------------------------------------
```

### Spinning up the containers

- Change to the parent folder: `cd ..`
- Start Docker Compose: `docker-compose up`

### Checking the logs in Kibana

- Perform a `GET` request to the `movie-service`: `http://localhost:8001/movies/2`
- Open Kibana: `http://localhost:5601`
  - Click the management icon
  - Create an index pattern
  - Visualize the logs
  
  
  [img.services]: /misc/diagrams/services.png
  [img.elastic-stack]: /misc/diagrams/elastic-stack.png
  [img.elastic-stack-docker]: /misc/diagrams/elastic-stack-docker.png


  [12factor]: https://12factor.net
  [spring-cloud-sleuth]: https://spring.io/projects/spring-cloud-sleuth
