NAME = savior-client

.PHONY: build

build: .image.date

.image.date: Dockerfile Makefile
	docker build -t $(NAME) .
	touch -d `docker inspect --format '{{.Created}}' $(NAME)` $@
