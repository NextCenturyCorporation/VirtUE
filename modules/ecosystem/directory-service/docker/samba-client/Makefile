NAME = savior-client

.PHONY: build

build: .image.date

.image.date: Dockerfile Makefile krb5.conf smb.conf
	docker build -t $(NAME) .
	touch -d `docker inspect --format '{{.Created}}' $(NAME)` $@
