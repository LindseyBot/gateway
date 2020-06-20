FROM azul/zulu-openjdk-alpine:14

LABEL maintainer="git@notfab.net"

COPY build/libs/*.jar /opt/

CMD ["java", "-jar", "/opt/lindsey.jar"]
