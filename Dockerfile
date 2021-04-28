FROM azul/zulu-openjdk-alpine:15

LABEL maintainer="git@notfab.net"

COPY build/libs/*.jar /opt/

CMD ["java", "-jar", "/opt/lindsey.jar"]
