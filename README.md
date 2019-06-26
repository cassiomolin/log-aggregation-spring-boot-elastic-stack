[in progress]

## Aggregating logs of Spring Boot applications with Elastic Stack

This post describes how to aggregate logs of Spring Boot applications running on Docker with Elastic Stack.

## What are logs and what are they meant for?

The [Twelve-Factor App methodology][12factor], a set of best practices for building _software as a service_ applications, define logs as _a stream of aggregated, time-ordered events collected from the output streams of all running processes and backing services_ which _provide visibility into the behavior of a running app._

This set of best practices recommends that logs should be treated as _event streams_:

> A twelve-factor app never concerns itself with routing or storage of its output stream. It should not attempt to write to or manage logfiles. Instead, each running process writes its event stream, unbuffered, to `stdout`. During local development, the developer will view this stream in the foreground of their terminal to observe the app’s behavior.
>
> In staging or production deploys, each process’ stream will be captured by the execution environment, collated together with all other streams from the app, and routed to one or more final destinations for viewing and long-term archival. These archival destinations are not visible to or configurable by the app, and instead are completely managed by the execution environment.

With that in mind, the log event stream for an application can be routed to a file, or watched via realtime `tail` in a terminal or, preferably, sent to a log indexing and analysis system such as Elastic Stack.

## What is Elastic Stack?

Elastic Stack is a group of open source applications from Elastic designed to help users take data from any source and in any format and then search, analyze, and visualize that data in real time. It was formerly known as _ELK Stack_, in which the letters in the name stood for the applications in the group: Elasticsearch, Logstash and Kibana. A fourth product, Beats, was subsequently added to the stack, rendering the potential acronym to be unpronounceable.

So let's have a quick look at each component of the Elastic Stack.

### Elasticsearch

Elasticsearch is a real-time, distributed storage, JSON-based search, and analytics engine designed for horizontal scalability, maximum reliability, and easy management. It can be used for many purposes, but one context where it excels is indexing streams of semi-structured data, such as logs or decoded network packets.

### Kibana

Kibana is an open source analytics and visualization platform designed to work with Elasticsearch. Kibana can be used to search, view, and interact with data stored in Elasticsearch indices. You can easily perform advanced data analysis and visualize your data in a variety of charts, tables, and maps.

### Beats

Beats are open source data shippers that can be installed as agents on servers to send operational data directly to Elasticsearch or via Logstash, where it can be further processed and enhanced. There's a number of Beats for different purposes:

- Filebeat: Log files
- Metricbeat: Metrics
- Packetbeat: Network data
- Heartbeat: Uptime monitoring
- And [more][beats].

As we intend to ship log files, we'll use Filebeat.

### Logstash

Logstash is a powerful tool that integrates with a wide variety of deployments. It offers a large selection of plugins to help you parse, enrich, transform, and buffer data from a variety of sources. If the data requires additional processing that is not available in Beats, then Logstash can be added to the deployment.

### Putting the pieces together

The following diagram illustrates how the components of Elastic Stack interact with each other:

![Elastic Stack][img.elastic-stack]

In a few words:

- Filebeat collects data from the log files and ships it to Logststash.
- Logstash enhances the data and send it to Elasticsearch.
- Elasticsearch stores and indexes the data.
- Kibana displays the data stored in Elasticsearch.

## Overview of our micro services

For this example, let's consider two micro services:

![Movie and review services][img.services]

The `movie-service` manages information related to movies while the `review-service` manages information related to the reviews of each movie. For simplicity, we'll support only `GET` requests.

## Tracing the requests

Unlike in a monolithic application, a single business operation is split across a number of services. To be able to trace the request across multiple services, we'll use [Spring Cloud Sleuth][spring-cloud-sleuth]: it implements a distributed tracing solution for Spring Cloud. 

Once we add the [Spring Cloud Sleuth][spring-cloud-sleuth] dependency to our application, all your interactions with external systems will be instrumented automatically:

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

## Creating the log appender

Our Spring Boot applications make use the `spring-boot-starter-web` artifact, which depends on Logback as logging system by default. The logging configurations are defined in the `logback-spring.xml` file, under the `resources` folder.

To be easily processed by Elastic Stack, our applications produce logs in JSON, where each logging event is a JSON object. To accomplish it, the applications use the [Logstash Logback Encoder][logstash-logback-encoder], which provides Logback encoders, layouts, and appenders to log in JSON. It was originally written to support output in Logstash's JSON format, but has evolved into a highly-configurable, general-purpose, structured logging mechanism for JSON and other Jackson dataformats. The structure of the output, and the data it contains, is fully configurable.

Instead of managing log files directly, the applications log to the console using the `ConsoleAppender`. The simplest configuration we may have is using the `LogstashEncoder`, which comes with a pre-defined set of providers (https://github.com/logstash/logstash-logback-encoder#standard-fields):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <springProperty scope="context" name="application_name" source="spring.application.name"/>

    <appender name="jsonConsoleAppender" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="jsonConsoleAppender"/>
    </root>
    
</configuration>
```

The above configuration will produce the following log output:

```json
{"@timestamp":"2019-06-25T23:01:38.967+01:00","@version":"1","message":"Finding details of movie with id 1","logger_name":"com.cassiomolin.logaggregation.movie.service.MovieService","thread_name":"http-nio-8001-exec-3","level":"INFO","level_value":20000,"application_name":"movie-service","traceId":"c52d9ff782fa8f6e","spanId":"c52d9ff782fa8f6e","spanExportable":"false","X-Span-Export":"false","X-B3-SpanId":"c52d9ff782fa8f6e","X-B3-TraceId":"c52d9ff782fa8f6e"}
{"@timestamp":"2019-06-25T23:01:38.968+01:00","@version":"1","message":"Finding reviews of movie with id 1","logger_name":"com.cassiomolin.logaggregation.movie.service.MovieService","thread_name":"http-nio-8001-exec-3","level":"INFO","level_value":20000,"application_name":"movie-service","traceId":"c52d9ff782fa8f6e","spanId":"c52d9ff782fa8f6e","spanExportable":"false","X-Span-Export":"false","X-B3-SpanId":"c52d9ff782fa8f6e","X-B3-TraceId":"c52d9ff782fa8f6e"}
{"@timestamp":"2019-06-25T23:01:38.973+01:00","@version":"1","message":"Found 2 review(s) of movie with id 1","logger_name":"com.cassiomolin.logaggregation.movie.service.MovieService","thread_name":"http-nio-8001-exec-3","level":"INFO","level_value":20000,"application_name":"movie-service","traceId":"c52d9ff782fa8f6e","spanId":"c52d9ff782fa8f6e","spanExportable":"false","X-Span-Export":"false","X-B3-SpanId":"c52d9ff782fa8f6e","X-B3-TraceId":"c52d9ff782fa8f6e"}
```

```json
{"@timestamp":"2019-06-25T23:01:38.971+01:00","@version":"1","message":"Finding reviews of movie with id 1","logger_name":"com.cassiomolin.logaggregation.review.service.ReviewService","thread_name":"http-nio-8002-exec-3","level":"INFO","level_value":20000,"application_name":"review-service","traceId":"c52d9ff782fa8f6e","spanId":"713b166571044e38","spanExportable":"false","X-Span-Export":"false","X-B3-SpanId":"713b166571044e38","X-B3-ParentSpanId":"c52d9ff782fa8f6e","X-B3-TraceId":"c52d9ff782fa8f6e","parentId":"c52d9ff782fa8f6e"}
{"@timestamp":"2019-06-25T23:01:38.971+01:00","@version":"1","message":"Found 2 review(s) of movie with id 1","logger_name":"com.cassiomolin.logaggregation.review.service.ReviewService","thread_name":"http-nio-8002-exec-3","level":"INFO","level_value":20000,"application_name":"review-service","traceId":"c52d9ff782fa8f6e","spanId":"713b166571044e38","spanExportable":"false","X-Span-Export":"false","X-B3-SpanId":"713b166571044e38","X-B3-ParentSpanId":"c52d9ff782fa8f6e","X-B3-TraceId":"c52d9ff782fa8f6e","parentId":"c52d9ff782fa8f6e"}
```

To have more flexibility in the JSON format and in data included in logging, we can use the `LoggingEventCompositeJsonEncoder`. No providers are configured by default in the composite encoder, so we must add the providers we want to customize the output:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <springProperty scope="context" name="application_name" source="spring.application.name"/>

    <appender name="jsonConsoleAppender" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp>
                    <timeZone>UTC</timeZone>
                </timestamp>
                <version/>
                <logLevel/>
                <message/>
                <loggerName/>
                <threadName/>
                <context/>
                <pattern>
                    <omitEmptyFields>true</omitEmptyFields>
                    <pattern>
                        {
                            "trace": {
                                "trace_id": "%mdc{X-B3-TraceId}",
                                "span_id": "%mdc{X-B3-SpanId}",
                                "parent_span_id": "%mdc{X-B3-ParentSpanId}",
                                "exportable": "%mdc{X-Span-Export}"
                            }
                        }
                    </pattern>
                </pattern>
                <mdc>
                    <excludeMdcKeyName>traceId</excludeMdcKeyName>
                    <excludeMdcKeyName>spanId</excludeMdcKeyName>
                    <excludeMdcKeyName>parentId</excludeMdcKeyName>
                    <excludeMdcKeyName>spanExportable</excludeMdcKeyName>
                    <excludeMdcKeyName>X-B3-TraceId</excludeMdcKeyName>
                    <excludeMdcKeyName>X-B3-SpanId</excludeMdcKeyName>
                    <excludeMdcKeyName>X-B3-ParentSpanId</excludeMdcKeyName>
                    <excludeMdcKeyName>X-Span-Export</excludeMdcKeyName>
                </mdc>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="jsonConsoleAppender"/>
    </root>
    
</configuration>
```

Here's a sample of the log output for the above configuration:

```json
{"@timestamp":"2019-06-25T21:59:44.396Z","@version":"1","level":"INFO","message":"Finding details of movie with id 1","logger_name":"com.cassiomolin.logaggregation.movie.service.MovieService","thread_name":"http-nio-8001-exec-3","application_name":"movie-service","trace":{"trace_id":"933006b3cea25d5b","span_id":"933006b3cea25d5b","exportable":"false"}}
{"@timestamp":"2019-06-25T21:59:44.396Z","@version":"1","level":"INFO","message":"Finding reviews of movie with id 1","logger_name":"com.cassiomolin.logaggregation.movie.service.MovieService","thread_name":"http-nio-8001-exec-3","application_name":"movie-service","trace":{"trace_id":"933006b3cea25d5b","span_id":"933006b3cea25d5b","exportable":"false"}}
{"@timestamp":"2019-06-25T21:59:44.401Z","@version":"1","level":"INFO","message":"Found 2 review(s) of movie with id 1","logger_name":"com.cassiomolin.logaggregation.movie.service.MovieService","thread_name":"http-nio-8001-exec-3","application_name":"movie-service","trace":{"trace_id":"933006b3cea25d5b","span_id":"933006b3cea25d5b","exportable":"false"}}
```

```json
{"@timestamp":"2019-06-25T21:59:44.399Z","@version":"1","level":"INFO","message":"Finding reviews of movie with id 1","logger_name":"com.cassiomolin.logaggregation.review.service.ReviewService","thread_name":"http-nio-8002-exec-3","application_name":"review-service","trace":{"trace_id":"933006b3cea25d5b","span_id":"da8c865071df2ecf","parent_span_id":"933006b3cea25d5b","exportable":"false"}}
{"@timestamp":"2019-06-25T21:59:44.399Z","@version":"1","level":"INFO","message":"Found 2 review(s) of movie with id 1","logger_name":"com.cassiomolin.logaggregation.review.service.ReviewService","thread_name":"http-nio-8002-exec-3","application_name":"review-service","trace":{"trace_id":"933006b3cea25d5b","span_id":"da8c865071df2ecf","parent_span_id":"933006b3cea25d5b","exportable":"false"}}
```

## Elastic Stack with Docker

We'll run our applications along with Elastic Stack in Docker containers, as illustrated below:

![Elastic Stack][img.elastic-stack-docker]

As we'll have multiple containers, we'll use Docker Compose to manage them. With Compose, we use a `docker-compose.yml` file to configure our application’s services. Then, with a single command, we create and start all the services from our configuration. 

Feel free to check the [`docker-compose.yml`][repo.docker-compose.yml] file for details on how the services are configured.

Both movie and review services will produce logs to the standard output (`stdout`). By default, Docker captures the standard output (and standard error) of all your containers, and writes them in files using the JSON format, using the `json-file` driver. The logs are then stored as files the `/var/lib/docker/containers` directory and each log file contains information about only one container.

(/var/lib/docker/containers/<container-id>/<container-id>-json.log)

In the [`filebeat.docker.yml`][repo.filebeat.docker.yml] file, Filebeat is configured to:
- Read the Docker logs from the `/var/lib/docker/containers` directory
- Decode the `message` field
- Drop the events that don't contain the `application_name` field (let's consider our application logs only)
- Enrich with Docker metadata
- Send the events to Logstash with runs on the port `5044`

```yaml
filebeat.inputs:
  - type: docker
    enabled: true
    containers.ids: '*'
    paths:
      - /var/lib/docker/containers
    processors:
      - decode_json_fields:
          fields: ["message"]
          target: ""
          overwrite_keys: true
      - drop_event:
          when:
            not:
              has_fields: ['application_name']
      - add_docker_metadata: ~

logging.metrics.enabled: false

output.logstash:
  hosts: "logstash:5044"
```

In the [`logstash.conf`][repo.logstash.conf] file, Logstash is configured to:
- Expect an input from Beats in the port `5044`
- Apply filters
- Send result to Elasticsearch which runs on the port `9200`.

```conf
input {
  beats {
    port => 5044
  }
}

filter {

}

output {
  elasticsearch {
    hosts => "elasticsearch:9200"
  }
}
```

Elasticsearch will store and index the log events and, finally, we will be able to visualize the logs in Kibana, which exposes a UI in the port `5601`.

### Building the Spring Boot applications

Both `movie-service` and `review-service` use the `dockerfile-maven` from Spotify to make the Docker build process integrate with the Maven build process.

If you have Java 11, Maven and Docker configured, you are good to go.

- Change to the `review-service` folder: `cd review-service`
- Build the application and create a Docker image: `mvn clean install`
- Change to the parent folder: `cd ..`
- Change to the `movie-service` folder: `cd movie-service`
- Build the application and create a Docker image: `mvn clean install`
- Change to the parent folder: `cd ..`

### Spinning up the containers

- Start Docker Compose: `docker-compose up`

### Checking the logs in Kibana

- To have some data, perform a `GET` request to the `movie-service`: `http://localhost:8001/movies/1`
- Open Kibana: `http://localhost:5601`
  - Click the management icon
  - Create an index pattern
  - Visualize the logs
  
  
  [img.services]: /misc/img/diagrams/services.png
  [img.elastic-stack]: /misc/img/diagrams/elastic-stack.png
  [img.elastic-stack-docker]: /misc/img/diagrams/services-and-elastic-stack-with-docker.png


  [12factor]: https://12factor.net
  [spring-cloud-sleuth]: https://spring.io/projects/spring-cloud-sleuth
  [beats]: https://www.elastic.co/products/beats
  [logback]: https://logback.qos.ch/
  [logstash-logback-encoder]: https://github.com/logstash/logstash-logback-encoder
  [dockerfile-maven]: https://github.com/spotify/dockerfile-maven
  [repo.docker-compose.yml]: https://github.com/cassiomolin/log-aggregation-elasticsearch-spring-boot/blob/master/docker-compose.yml
  [repo.logstash.conf]: https://github.com/cassiomolin/log-aggregation-elasticsearch-spring-boot/blob/master/logstash/pipeline/logstash.conf
  [repo.filebeat.docker.yml]: https://github.com/cassiomolin/log-aggregation-elasticsearch-spring-boot/blob/master/filebeat/filebeat.docker.yml
  [docker.json-file-logging-driver]: https://docs.docker.com/config/containers/logging/json-file/


---

https://www.elastic.co/blog/enrich-docker-logs-with-filebeat
