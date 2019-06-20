[in progress]

## Aggregating Spring Boot logs with Elastic Stack and Docker

Log aggregation is a common requirement of a microservices architecture.

Unlike in a monolithic application, a single business operation is split across a number of services. When something goes wrong, it's important to be able to trace each operation across the services.

For this example, let's consider two services:

- `movie-service`
- `review-service`

The `movie-service` manages information related to movies while the `review-service` manages information related to the reviews of each movie. For simplicity, we'll support only `GET` requests.

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

## Tracing the requests

[coming soon]

## Creating the log appender

[coming soon]

## Building the Spring Boot applications

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

## Spinning up the containers

- Change to the parent folder: `cd ..`
- Start Docker Compose: `docker-compose up`

## Checking the logs in Kibana

- Perform a `GET` request to the `movie-service`: `http://localhost:8001/movies/2`
- Open Kibana: `http://localhost:5601`
  - Click the management icon
  - Create an index pattern
  - Visualize the logs