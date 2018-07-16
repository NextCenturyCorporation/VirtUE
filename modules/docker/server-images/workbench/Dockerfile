FROM nginx:alpine

COPY ./admin.tar.gz /usr/share/nginx/html/admin.tar.gz
COPY ./default /etc/nginx/conf.d/default.conf
WORKDIR /usr/share/nginx/html
RUN ls -l
RUN tar -xvf admin.tar.gz
COPY ./baseUrl.json /usr/share/nginx/html/assets/json/
WORKDIR /usr/share/nginx/html
 
RUN  ls
 
EXPOSE 8080
