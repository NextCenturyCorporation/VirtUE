FROM openjdk:8
 
COPY virtue-admin* /opt/virtue/
WORKDIR /opt/virtue/
RUN unzip virtue-admin-0.0.1-SNAPSHOT.zip
WORKDIR /opt/virtue/virtue-admin-0.0.1-SNAPSHOT
COPY application.properties /opt/virtue/virtue-admin-0.0.1-SNAPSHOT
 
RUN  ls
 
EXPOSE 8080
CMD ["bin/virtue-admin"]