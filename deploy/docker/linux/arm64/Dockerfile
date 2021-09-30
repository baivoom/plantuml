# 11-jre linux/arm64/v8

FROM --platform=$BUILDPLATFORM openjdk:11-jre

WORKDIR /srv
ADD ./target/plantuml-1.2021.11-SNAPSHOT-jar-with-dependencies.jar /srv/plantuml.jar

EXPOSE 8080

CMD ["java", "-jar", "/srv/plantuml.jar", "-picoweb:8080"]

