FROM adoptopenjdk:11
RUN mkdir /opt/app
 
COPY target/default+uberjar/medegas-0.1.0-SNAPSHOT-standalone.jar /medegas-0.1.0-SNAPSHOT-standalone.jar
 
CMD ["java", "-jar", "/medegas-0.1.0-SNAPSHOT-standalone.jar"]