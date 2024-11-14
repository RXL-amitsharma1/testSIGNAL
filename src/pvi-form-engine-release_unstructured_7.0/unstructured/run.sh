#!/bin/bash

set -e pipefail

echo ""
echo "Running Unstructured build"
echo ""
APP_VERSION=$(cat config/config.ini | grep APP_VERSION|cut -d '=' -f 2)

#stop and remove docker container and docker image
docker_image=$(docker ps -a | grep -w unstructured | awk '{print $2}') && [ -n "$docker_image" ] && (docker stop unstructured || true) && docker rm unstructured && docker rmi "$docker_image"

docker load -i unstructured_$APP_VERSION.tar.gz
docker run -idt -p 9888:9888 -v $PWD/models/:/unstructured/models/ -v $PWD/logs/:/unstructured/logs/ -v $PWD/config/:/unstructured/config/ --name unstructured unstructured:$APP_VERSION

echo ""
echo "Started Unstructured build"
echo ""