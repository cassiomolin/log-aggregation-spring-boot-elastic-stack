FROM openjdk:11-jre
MAINTAINER Cassio Mazzochi Molin <cassio@cassiomolin.com>

VOLUME /tmp

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","com.cassiomolin.logaggregation.movie.MovieApplication"]