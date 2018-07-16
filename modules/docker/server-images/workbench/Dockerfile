FROM nginx:alpine

COPY ./modules/admin/build/admin.tar.gz /usr/share/nginx/html/
COPY ./default /etc/nginx/conf.d/default.conf
WORKDIR /usr/share/nginx/html
RUN ls -l
RUN tar -xvf admin.tar.gz
COPY ./baseUrl.json /usr/share/nginx/html/assets/json/
WORKDIR /usr/share/nginx/html
 
RUN  ls
 
EXPOSE 8080
