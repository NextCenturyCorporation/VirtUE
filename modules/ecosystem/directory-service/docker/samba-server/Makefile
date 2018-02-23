NAME = savior-dc
FEDORA_VERSION = latest

.PHONY: build

build: .image.date

.image.date: Dockerfile Makefile init-or-run-samba.sh
	docker build -t $(NAME) --build-arg FEDORA_VERSION=$(FEDORA_VERSION) .
	touch -d `docker inspect --format '{{.Created}}' $(NAME)` $@
