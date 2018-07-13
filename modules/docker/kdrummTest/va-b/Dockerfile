FROM openjdk:8

COPY ./modules /opt/virtue/ 
WORKDIR /opt/virtue/modules/virtue-admin
RUN ./gradlew build --info
WORKDIR /opt/virtue/virtue-admin/build/distributions
RUN unzip virtue-admin-0.0.1-SNAPSHOT.zip
WORKDIR /opt/virtue/virtue-admin/build/distributions/virtue-admin-0.0.1-SNAPSHOT
COPY ./modules/docker/server-images/virtue-admin/application.properties /opt/virtue/virtue-admin/build/distributions/virtue-admin-0.0.1-SNAPSHOT
 
RUN  ls
 
EXPOSE 8080
CMD ["bin/virtue-admin"]
