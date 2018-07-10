FROM nginx:alpine
 
COPY ./admin.tar.gz /usr/share/nginx/html
COPY ./default /etc/nginx/conf.d/default.conf
WORKDIR /usr/share/nginx/html
RUN tar -xvf admin.tar.gz
WORKDIR /usr/share/nginx/html
 
RUN  ls
 
EXPOSE 8080
