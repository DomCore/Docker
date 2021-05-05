SHELL := /bin/bash
SEMVER_REGEX=^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(\-[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?

# SemVer
ver ?= $(shell git describe --tags --abbrev=0)
pre ?= $(shell git branch | grep \* | cut -d ' ' -f2)
build ?= $(shell git rev-parse HEAD)
version ?= ${ver}$(shell [[ ${pre} != master ]] && echo -${pre} )

.PHONY: version # Display and check current version
version:
	@echo "[VERSION]"

	@if [[ ${version} =~ ${SEMVER_REGEX} ]]; then\
		echo "making v${version}";\
	else\
		error "FAIL: invalid version";\
	fi

	@echo ${version}

.PHONY: deps # Restore all dependencies prior to build
deps:
	@echo "[DEPS]"

.PHONY: build # Build output artifact
build: version deps
	@mvn clean package versions:set -DnewVersion=${version}
