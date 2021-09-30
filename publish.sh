#!/usr/bin/env bash

set -e

docker build . -f ./deploy/docker/linux/arm64/Dockerfile -t baivoom/plantuml:1.2021.11.1 --platform linux/arm64
docker push baivoom/plantuml:1.2021.11.1
docker tag baivoom/plantuml:1.2021.11.1  baivoom/plantuml:latest
docker push baivoom/plantuml:latest




