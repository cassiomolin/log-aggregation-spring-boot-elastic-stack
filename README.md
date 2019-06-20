# log-aggregation-elasticsearch-spring-boot

## Running the application

- Change to the `review-service` folder: `cd review-service`
- Build the application and create a Docker image: `mvn clean install`

```
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

```
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

- Change to the parent folder: `cd ..`
- Start Docker Compose: `docker-compose up`
- Perform a `GET` request to the `movie-service`: `http://localhost:8001/movies/2`
- Open Kibana: `http://localhost:5601`
  - Click the management icon
  - Create an index pattern
  - Visualize the logs