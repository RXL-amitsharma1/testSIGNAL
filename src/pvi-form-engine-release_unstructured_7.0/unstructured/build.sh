#!/bin/bash

set -e pipefail

echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
echo ""
echo "Building docker based build for Unstructured"
echo ""
echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"

APP_VERSION=$(cat config/config.ini | grep APP_VERSION|cut -d '=' -f 2)

# To build docker container for multilingual_unstructured
docker build --no-cache -t unstructured:$APP_VERSION -f Dockerfile .
echo "$APP_VERSION"

echo ""
echo "Build is created now saving it."
echo ""

# To save docker container
docker save unstructured:$APP_VERSION | gzip > unstructured_$APP_VERSION.tar.gz

mkdir -p package/logs
mkdir -p package/models
mv unstructured_$APP_VERSION.tar.gz package/
cp -r config package/
cp fetch_model_from_s3.sh run.sh package/

echo "Creating the final build archive"

# Compile tar.gz file with all the required files
tar -czf unstructured_$APP_VERSION.tar.gz package

# Remove config.ini from the current directory once it has been compiled in tar
rm -r package

echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
echo ""
echo "Unstructured docker based build has been created."
echo ""
echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"