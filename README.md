# log-aggregation-elasticsearch-spring-boot

Log aggregation is a common requirement of a microservices architecture.

Unlike in a monolithic application, a single business operation is split across a number of services. When something goes wrong, it's important to be able to trace each operation across the services.

---

- Start Docker Compose: `docker-compose up`
- Perform a `GET` request to the gateway: `http://localhost:8001/greetings`
- Check Kibana: `http://localhost:5601`