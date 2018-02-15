NAME = savior-firefox
FEDORA_VERSION = latest

.PHONY: build

build: .image.date

.image.date: Dockerfile Makefile id_rsa id_rsa.pub
	docker build -t $(NAME) .
	touch -d `docker inspect --format '{{.Created}}' $(NAME)` $@
