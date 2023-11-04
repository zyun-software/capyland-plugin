#!/usr/bin/make

.PHONY: maven

SHELL = /bin/sh

CURRENT_UID := $(shell id -u)
CURRENT_GID := $(shell id -g)

export CURRENT_UID
export CURRENT_GID

ifneq (,$(wildcard ./.env))
	include .env
	export
endif

maven:
	docker compose run --rm maven bash
