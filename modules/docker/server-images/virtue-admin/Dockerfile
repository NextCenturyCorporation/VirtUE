FROM openjdk:8

COPY ./modules /opt/virtue/ 
WORKDIR /opt/virtue/virtue-admin
RUN ./gradlew build --info
WORKDIR /opt/virtue/virtue-admin/build/distributions
COPY virtue-admin-0.0.1-SNAPSHOT.zip opt/virtue/
WORKDIR opt/virtue/
RUN unzip virtue-admin-0.0.1-SNAPSHOT.zip
WORKDIR /opt/virtue/virtue-admin-0.0.1-SNAPSHOT
COPY application.properties /opt/virtue/virtue-admin-0.0.1-SNAPSHOT
 
RUN  ls
 
EXPOSE 8080
CMD ["bin/virtue-admin"]
