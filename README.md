# log-aggregation-elasticsearch-spring-boot

Log aggregation is a common requirement of a microservices architecture.

Unlike in a monolithic application, a single business operation is split across a number of services. When something goes wrong, it's important to be able to trace each operation across the services.

---

- Start Docker Compose: `docker-compose up`
- Start both `movie-service` and `review-service`
- Open Kibana: `http://localhost:5601`
- Click the management icon
- Create an index pattern
- Perform a `GET` request to the `movie-service`: `http://localhost:8001/movies/2`
- Visualize the logs