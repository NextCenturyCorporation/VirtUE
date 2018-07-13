FROM node:8

RUN npm -version
WORKDIR /opt/savior/modules/admin/
RUN npm install && npm run-script build && cd dist && tar -zcvf ../admin.tar.gz *
