## Log aggregation with Spring Boot, Elastic Stack and Docker

In a microservices architecture, a single business operation might trigger a chain of downstream microservice calls, which can be pretty challenging to debug. Things, however, can be easier when the logs of all microservices are centralized and each log event contains details that allow us to trace the interactions between the applications.

This project demonstrates how to use Elastic Stack along with Docker to collect, process, store, index and visualize logs of Spring Boot microservices.

##### Table of contents
- [What is Elastic Stack?](#what-is-elastic-stack)  
  - [Elasticsearch](#elasticsearch)
  - [Kibana](#kibana)
  - [Beats](#beats)
  - [Logstash](#logstash)
  - [Putting the pieces together](#putting-the-pieces-together)
- [Logs as streams of events](#logs-as-streams-of-events)
- [Logging with Logback and SLF4J](#logging-with-logback-and-slf4j)
  - [Enhancing log events with tracing details](#enhancing-log-events-with-tracing-details)
  - [Logging in JSON format](#logging-in-json-format)
- [Configuring Elastic Stack applications to run on Docker](#configuring-elastic-stack-applications-to-run-on-docker)
- [Example](#example)
  - [Building the applications and creating Docker images](#building-the-applications-and-creating-docker-images)
  - [Spinning up the containers](#spinning-up-the-containers)
  - [Visualizing logs in Kibana](#visualizing-logs-in-kibana)

## What is Elastic Stack?

Elastic Stack is a group of open source applications from Elastic designed to take data from any source and in any format and then search, analyze, and visualize that data in real time. It was formerly known as [_ELK Stack_][elk-stack], in which the letters in the name stood for the applications in the group: [_Elasticsearch_][elasticsearch], [_Logstash_][logstash] and [_Kibana_][kibana]. A fourth application, [_Beats_][beats], was subsequently added to the stack, rendering the potential acronym to be unpronounceable. So ELK Stack became Elastic Stack.

So let's have a quick look at each component of Elastic Stack.

### Elasticsearch

[Elasticsearch][elasticsearch] is a real-time, distributed storage, JSON-based search, and analytics engine designed for horizontal scalability, maximum reliability, and easy management. It can be used for many purposes, but one context where it excels is indexing streams of semi-structured data, such as logs or decoded network packets.

### Kibana

[Kibana][kibana] is an open source analytics and visualization platform designed to work with Elasticsearch. Kibana can be used to search, view, and interact with data stored in Elasticsearch indices, allowing advanced data analysis and visualizing data in a variety of charts, tables, and maps.

### Beats

[Beats][beats] are open source data shippers that can be installed as agents on servers to send operational data directly to Elasticsearch or via Logstash, where it can be further processed and enhanced. There's a number of Beats for different purposes:

- [Filebeat][filebeat]: Log files
- [Metricbeat][metricbeat]: Metrics
- [Packetbeat][packetbeat]: Network data
- [Heartbeat][heartbeat]: Uptime monitoring
- And [more][beats].

As we intend to ship log files, we'll use [Filebeat][filebeat].

### Logstash

[Logstash][logstash] is a powerful tool that integrates with a wide variety of deployments. It offers a large selection of plugins to help you parse, enrich, transform, and buffer data from a variety of sources. If the data requires additional processing that is not available in Beats, then Logstash can be added to the deployment.

### Putting the pieces together

The following illustration shows how the components of Elastic Stack interact with each other:

![Elastic Stack][img.elastic-stack]

In a few words:

- Filebeat collects data from the log files and sends it to Logststash.
- Logstash enhances the data and sends it to Elasticsearch.
- Elasticsearch stores and indexes the data.
- Kibana displays the data stored in Elasticsearch.

## Logs as streams of events

The [Twelve-Factor App methodology][12factor], a set of best practices for building _software as a service_ applications, define logs as _a stream of aggregated, time-ordered events collected from the output streams of all running processes and backing services_ which _provide visibility into the behavior of a running app._ This set of best practices recommends that [logs should be treated as _event streams_][12factor.logs]:

> A twelve-factor app never concerns itself with routing or storage of its output stream. It should not attempt to write to or manage logfiles. Instead, each running process writes its event stream, unbuffered, to `stdout`. During local development, the developer will view this stream in the foreground of their terminal to observe the app’s behavior.
>
> In staging or production deploys, each process’ stream will be captured by the execution environment, collated together with all other streams from the app, and routed to one or more final destinations for viewing and long-term archival. These archival destinations are not visible to or configurable by the app, and instead are completely managed by the execution environment.

With that in mind, the log event stream for an application can be routed to a file, or watched via realtime `tail` in a terminal or, preferably, sent to a log indexing and analysis system such as Elastic Stack.

## Logging with Logback and SLF4J

When creating Spring Boot applications that depends on the `spring-boot-starter-web` artifact, [Logback][logback] will be pulled as a transitive dependency and will be used default logging system. 

Logback is a mature and flexible logging system and we can use use it directly or, preferable, use it with [SLF4J][slf4j], a logging facade or abstraction for various logging frameworks. For logging with SLF4J, we first have to obtain a [`Logger`][org.slf4j.Logger] instance using [`LoggerFactory`][org.slf4j.LoggerFactory], as shown below:

```java
public class Example {
    final Logger log = LoggerFactory.getLogger(Example.class);
}
```

To be less verbose and avoid repeating ourselves in all classes we want to perform logging, we can use [Lombok][lombok]. It provides the [`@Slf4j`][lombok.slf4j] annotation for generating the logger field for us. The class shown above is is equivalent to the class show below: 

```java
@Slf4j
public class Example {

}
```

Once we get the logger instance, we can perform logging:

```java
log.trace("Logging at TRACE level");
log.debug("Logging at DEBUG level");
log.info("Logging at INFO level");
log.warn("Logging at WARN level");
log.error("Logging at ERROR level");
```

Parametrized messages with the `{}` syntax can also be used. This approach is preferable over string concatenation, as it doesn't incur the cost of the parameter construction in case the log level is disabled:

```java
log.debug("Found {} results", list.size());
```

In Spring Boot applications, Logback can be [configured][spring-boot.configure-logback] in the `logback-spring.xml` file, located under the `resources` folder. In this configuration file, we can take advantage of Spring profiles and the templating features provided by Spring Boot.

### Enhancing log events with tracing details

In a microservices architecture, a single business operation might trigger a chain of downstream microservice calls and such interactions between the services can be challenging to debug. To make things easier, we can use [Spring Cloud Sleuth][spring-cloud-sleuth] to enhance the application logs with tracing details. 

Spring Cloud Sleuth is a distributed tracing solution for Spring Cloud and it adds a _trace id_ and _span id_ to the logs:

- The _span_ represents a basic unit of work, for example sending an HTTP request.
- The _trace_ contains a set of spans, forming a tree-like structure. The trace id will remain the same as one microservice calls the next.

When visualizing the logs, we'll be able to get all events for a given trace or span id, providing visibility into the behavior of the chain of interactions between the services.

Once the Spring Cloud Sleuth dependency is added to the classpath, all interactions with the downstream services will be instrumented automatically and the trace and span ids will be added to the SLF4J's [Mapped Diagnostic Context][slf4j.mdc] (MDC), which will be included in the logs.

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

### Logging in JSON format

As we intend our log events to be indexed in Elasticserach, which stores JSON documents, it would be a good idea to produce log events in JSON format instead of having to parse plain text log events in Logstash.

To accomplish it, we can use the [Logstash Logback Encoder][logstash-logback-encoder], which provides Logback encoders, layouts, and appenders to log in JSON. The Logstash Logback Encoder was originally written to support output in Logstash's JSON format, but has evolved into a general-purpose, highly-configurable, structured logging mechanism for JSON and other Jackson dataformats. 

And, instead of managing log files directly, our microservices could log to the standard output using the `ConsoleAppender`. As the microservices will run in Docker containers, we can leave the responsibility of writing the log files to Docker. We will see more details about the Docker in the next section.

For a simple and quick configuration, we could use `LogstashEncoder`, which comes with a [pre-defined set of providers][logstash-logback-encoder.standard-fields]:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <springProperty scope="context" name="application_name" source="spring.application.name"/>

    <appender name="jsonConsoleAppender" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="jsonConsoleAppender"/>
    </root>
    
</configuration>
```

The above configuration will produce the following log output (just bear in mind that the actual output is a single line, but it's been formatted for better visualization):

```json
{
   "@timestamp": "2019-06-29T23:01:38.967+01:00",
   "@version": "1",
   "message": "Finding details of post with id 1",
   "logger_name": "com.cassiomolin.logaggregation.post.service.PostService",
   "thread_name": "http-nio-8001-exec-3",
   "level": "INFO",
   "level_value": 20000,
   "application_name": "post-service",
   "traceId": "c52d9ff782fa8f6e",
   "spanId": "c52d9ff782fa8f6e",
   "spanExportable": "false",
   "X-Span-Export": "false",
   "X-B3-SpanId": "c52d9ff782fa8f6e",
   "X-B3-TraceId": "c52d9ff782fa8f6e"
}
```

This encoder includes the values stored in MDC by default. When Spring Cloud Sleuth is in the classpath, the following properties will added to MDC and will be logged: `traceId`, `spanId`, `spanExportable`, `X-Span-Export`, `X-B3-SpanId` and `X-B3-TraceId`.

If we need more flexibility in the JSON format and in data included in log, we can use `LoggingEventCompositeJsonEncoder`. The composite encoder has no providers configured by default, so we must add the [providers][logstash-logback-encoder.providers-for-loggingevents] we want to customize the output:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

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

Find below a sample of the log output for the above configuration. Again, the actual output is a single line, but it's been formatted for better visualization:

```json
{  
   "@timestamp": "2019-06-29T22:01:38.967Z",
   "@version": "1",
   "level": "INFO",
   "message": "Finding details of post with id 1",
   "logger_name": "com.cassiomolin.logaggregation.post.service.PostService",
   "thread_name": "http-nio-8001-exec-3",
   "application_name": "post-service",
   "trace": {  
      "trace_id": "c52d9ff782fa8f6e",
      "span_id": "c52d9ff782fa8f6e",
      "exportable": "false"
   }
}
```

## Configuring Elastic Stack applications to run on Docker

We'll run Elastic Stack applications along with microservices in [Docker][docker] containers, as illustrated below:

![Docker containers][img.elastic-stack-docker]

As we have multiple containers, we'll use [Docker Compose][docker-compose] to manage them. With Compose, configure the application’s services in a YAML file. Then, with a single command, we create and start all the services from our configuration. Pretty cool stuff!

Have a look at how the services are defined and configured in the [`docker-compose.yml`][repo.docker-compose.yml]. What's important to highlight is the fact that _labels_ have been added to some services. Labels are simply metadata that only have meaning for who's using them. Let's have a quick looks at the labels that have been defined for the services:

- `collect_logs_with_filebeat`: When set to `true`, indicates that Filebeat should collect the logs produced by the Docker container.

- `decode_log_event_to_json_object`: Filebeat collects and stores the log event as a string in the `message` property of a JSON document. If the events are logged as JSON (which is the case when using the appenders defined above), the value of this label can be set to `true` to indicate that Filebeat should decode the JSON string stored in the `message` property to a JSON object.

Both post and comment services will produce logs to the standard output (`stdout`). By default, Docker captures the standard output (and standard error) of all your containers, and writes them to files in JSON format, using the `json-file` driver. The logs files are stored in the `/var/lib/docker/containers` directory and each log file contains information about only one container.

When applications run on containers, they become moving targets to the monitoring system. So we'll use the [autodiscover][filebeat.autodiscover] feature from Filebeat, which allows it to track the containers and adapt settings as changes happen. By defining configuration templates, the autodiscover subsystem can monitor services as they start running. So, in the [`filebeat.docker.yml`][repo.filebeat.docker.yml] file, Filebeat is configured to:

- Autodiscover the Docker containers that have the label `collect_logs_with_filebeat` set to `true`
- Collect logs from the containers that have been discovered 
- Decode the `message` field to a JSON object when the log event was produced by a container that have the label `decode_log_event_to_json_object` set to `true`
- Send the log events to Logstash which runs on the port `5044`

```yaml
filebeat.autodiscover:
  providers:
    - type: docker
      labels.dedot: true
      templates:
        - condition:
            contains:
              container.labels.collect_logs_with_filebeat: "true"
          config:
            - type: container
              format: docker
              paths:
                - "/var/lib/docker/containers/${data.docker.container.id}/*.log"
              processors:
                - decode_json_fields:
                    when.equals:
                      docker.container.labels.decode_log_event_to_json_object: "true"
                    fields: ["message"]
                    target: ""
                    overwrite_keys: true

output.logstash:
  hosts: "logstash:5044"
```

The above configuration uses a single processor. If we need, we could add more processors, which will be _chained_ and executed in the order they are defined in the configuration file. Each processor receives an event, applies a defined action to the event, and the processed event is the input of the next processor until the end of the chain.

Once the log event is collected and processed by Filebeat, it is sent to Logstash, which provides a rich set of plugins for further processing the events. 

The Logstash pipeline has two required elements, `input` and `output`, and one optional element, `filter`. The [input plugins][logstash.input-plugins] consume data from a source, the [filter plugins][logstash.filter-plugins] modify the data as we specify, and the [output plugins][logstash.output-plugins] write the data to a destination. 

![Logstash pipeline][img.logstash-pipeline]

In the [`logstash.conf`][repo.logstash.conf] file, Logstash is configured to:

- Receive events coming from Beats in the port `5044`
- Process the events by adding the tag `logstash_filter_applied`
- Send the processed events to Elasticsearch which runs on the port `9200`

```java
input {
  beats {
    port => 5044
  }
}

filter {
  mutate {
    add_tag => [ "logstash_filter_applied" ]
  }
}

output {
  elasticsearch {
    hosts => "elasticsearch:9200"
  }
}
```

Elasticsearch will store and index the log events and, finally, we will be able to visualize the logs in Kibana, which exposes a UI in the port `5601`.

## Example

For this example, let's consider we are creating a blog engine and we have the the following microservices:

- _Post service_: Manages details related to posts.
- _Comment service_: Manages details related to the comments of each post.

Each microservice is a Spring Boot application, exposing a HTTP API. As we intend to focus on _log aggregation_, let's keep it simple when it comes to the services architecture: One service will simply invoke the other service directly.

And, for demonstration purposes, all data handled by the services is stored in memory and only `GET` requests are supported. When a representation of post is requested, the post service will perform a `GET` request to the comment service to get a representation of the comments for that post. The post service will aggregate the results and return a representation of the post with comments to the client.

![Post and comment services][img.services]

Let's see how to build the source code, spin up the Docker containers, produce some log data and then visualize the logs in Kibana.

Before starting, ensure you at least Java 11, Maven 3.x and Docker set up. Then clone the [repository][repo] from GitHub:

```bash
git clone https://github.com/cassiomolin/log-aggregation-spring-boot-elastic-stack.git
```

### Building the applications and creating Docker images

Both post and comment services use the [`dockerfile-maven`][dockerfile-maven] plugin from Spotify to make the Docker build process integrate with the Maven build process. So when we build a Spring Boot artifact, we'll also build a Docker image.

- Change to the `comment-service` folder: `cd comment-service`
- Build the application and create a Docker image: `mvn clean install`
- Change back to the parent folder: `cd ..`

- Change to the `post-service` folder: `cd post-service`
- Build the application and create a Docker image: `mvn clean install`
- Change back to the parent folder: `cd ..`

### Spinning up the containers

In the root folder of our project, where the `docker-compose.yml` resides, spin up the Docker containers running `docker-compose up`.

### Visualizing logs in Kibana

- Open Kibana in your favourite browser: `http://localhost:5601`. When attempting to to access Kibana while it's starting, a message saying that Kibana is not ready yet will be displayed in the browser. Enhance your calm, give it a minute or two and then you are good to go.

- In the first time we access Kibana, we'll see a welcome page. Kibana comes with sample data in case we want to play with it. We will visualize our own data though. So click the _Explore on my own_ link.

![Welcome page][img.screenshot-01]

- On the left hand side, click the _Discover_ icon.

![Home][img.screenshot-02]

- Kibana uses index patterns for retrieving data from Elasticsearch. As it's the first time we are using Kibana, we must create an index pattern to explore our data. We should see an index that has been created by Logstash. So create a pattern for matching the Logstash indexes using `logstash-*` and then click the _Next step_ button.

![Creating index pattern][img.screenshot-03]

- Then pick a field for filtering the data by time. Choose `@timestamp` and click the _Create index pattern_ button.

![Picking a field for filtering data by time][img.screenshot-04]

- The index pattern will be created. Click again in the _Discover_ icon and the log events of both post and comment services start up will be shown:

![Viewing the log events][img.screenshot-05]

- To filter log events from the post service, for example, enter `application_name : "post-service"` in the search box. Click the _Update_ button and now we'll see log events from the post service only.

![Filtering logs by application name][img.screenshot-06]

- Clean the filter input and click the _Update_ button to view all logs. 

- Perform a `GET` request to `http://localhost:8001/posts/1` to generate some log data. Wait a few seconds and then click the _Refresh_ button. We will be able to see logs from the requests. The logs will contain tracing details, such as _trace.trace_id_ and _trace.span id_.

- In the left-hand side, there's a list of fields available. Hover over the list of fields and an _Add_ button will be shown for each field. Add a few fields such as `application_name`, `trace.trace_id`, `trace.span_id` and `message`.

- Let's trace a request. Pick a trace id from the logs and, in the filter box, input `trace.trace_id: "<value>"` where `<value>` is the trace id we want to use as filter criteria. Then click the _Update_ button and we will able to see logs of interactions between the services. As illustrated below, the trace id is the same for the entire operation, which started in the post service. The call to the downstream service, comment service, has been assigned a different span id.

![Filtering logs by trace id][img.screenshot-07]

To stop the containers, use `docker-compose down`. It's important to highlight that both Elasticsearch indexes and the Filebeat tracking data are stored in the host, under the `elasticseach/data` and `filebeat/data` folders. It means that, if you destroy the containers, no data will be lost.


  [img.services]: /misc/img/diagrams/services.png
  [img.elastic-stack]: /misc/img/diagrams/elastic-stack.png
  [img.elastic-stack-docker]: /misc/img/diagrams/services-and-elastic-stack.png
  [img.logstash-pipeline]: /misc/img/diagrams/logstash-pipeline.png
  [img.screenshot-01]: /misc/img/screenshots/01.png
  [img.screenshot-02]: /misc/img/screenshots/02.png
  [img.screenshot-03]: /misc/img/screenshots/03.png
  [img.screenshot-04]: /misc/img/screenshots/04.png
  [img.screenshot-05]: /misc/img/screenshots/05.png
  [img.screenshot-06]: /misc/img/screenshots/06.png
  [img.screenshot-07]: /misc/img/screenshots/07.png

  [12factor]: https://12factor.net
  [12factor.logs]: https://12factor.net/logs
 
  [spring-boot.configure-logback]: https://docs.spring.io/spring-boot/docs/current/reference/html/howto-logging.html#howto-configure-logback-for-logging
  [spring-cloud-sleuth]: https://spring.io/projects/spring-cloud-sleuth
  
  [dockerfile-maven]: https://github.com/spotify/dockerfile-maven
  
  [slf4j]: https://www.slf4j.org/
  [slf4j.manual]: https://www.slf4j.org/manual.html
  [logback]: https://logback.qos.ch/
  [logstash-logback-encoder]: https://github.com/logstash/logstash-logback-encoder
  [logstash-logback-encoder.standard-fields]: https://github.com/logstash/logstash-logback-encoder#standard-fields
  [logstash-logback-encoder.providers-for-loggingevents]: https://github.com/logstash/logstash-logback-encoder#providers-for-loggingevents
  
  [repo]: https://github.com/cassiomolin/log-aggregation-spring-boot-elastic-stack
  [repo.docker-compose.yml]: https://github.com/cassiomolin/log-aggregation-spring-boot-elastic-stack/blob/master/docker-compose.yml
  [repo.logstash.conf]: https://github.com/cassiomolin/log-aggregation-spring-boot-elastic-stack/blob/master/logstash/pipeline/logstash.conf
  [repo.filebeat.docker.yml]: https://github.com/cassiomolin/log-aggregation-spring-boot-elastic-stack/blob/master/filebeat/filebeat.docker.yml

  [elk-stack]: https://www.elastic.co/elk-stack
  [elasticsearch]: https://www.elastic.co/products/elasticsearch
  [logstash]: https://www.elastic.co/products/logstash
  [logstash.input-plugins]: https://www.elastic.co/guide/en/logstash/current/input-plugins.html
  [logstash.filter-plugins]: https://www.elastic.co/guide/en/logstash/current/filter-plugins.html
  [logstash.output-plugins]: https://www.elastic.co/guide/en/logstash/current/output-plugins.html
  [kibana]: https://www.elastic.co/products/kibana
  [beats]: https://www.elastic.co/products/beats
  [filebeat]: https://www.elastic.co/products/beats/filebeat
  [filebeat.autodiscover]: https://www.elastic.co/guide/en/beats/filebeat/current/configuration-autodiscover.html
  [metricbeat]: https://www.elastic.co/products/beats/metricbeat
  [packetbeat]: https://www.elastic.co/products/beats/packetbeat
  [heartbeat]: https://www.elastic.co/products/beats/heartbeat

  [docker]: https://docs.docker.com/
  [docker.json-file-logging-driver]: https://docs.docker.com/config/containers/logging/json-file/
  [docker-compose]: https://docs.docker.com/compose/

  [slf4j.mdc]: https://www.slf4j.org/manual.html#mdc
  
  [lombok]: https://projectlombok.org/
  [lombok.slf4j]: https://projectlombok.org/api/lombok/extern/slf4j/Slf4j.html
  
  [org.slf4j.Logger]: https://www.slf4j.org/api/org/slf4j/Logger.html
  [org.slf4j.LoggerFactory]: https://www.slf4j.org/api/org/slf4j/LoggerFactory.html