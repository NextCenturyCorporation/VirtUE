FROM openjdk:8 AS buildStage

COPY ./modules/virtue-admin /opt/virtue/virtue-admin/
COPY ./modules/common /opt/virtue/common/
WORKDIR /opt/virtue/virtue-admin
RUN ./gradlew build --info
WORKDIR /opt/virtue/virtue-admin/build/distributions

FROM openjdk:8-jre

COPY --from=buildStage /opt/virtue/virtue-admin/build/distributions /opt/virtue/virtue-admin/build/distributions
WORKDIR /opt/virtue/virtue-admin/build/distributions
RUN unzip virtue-admin-0.0.1-SNAPSHOT.zip
RUN rm -f virtue-admin-0.0.1-SNAPSHOT.zip
WORKDIR /opt/virtue/virtue-admin/build/distributions/virtue-admin-0.0.1-SNAPSHOT
COPY ./modules/docker/server-images/virtue-admin/application.properties /opt/virtue/virtue-admin/build/distributions/virtue-admin-0.0.1-SNAPSHOT
 
RUN  ls
 
EXPOSE 8080
CMD ["bin/virtue-admin"]
