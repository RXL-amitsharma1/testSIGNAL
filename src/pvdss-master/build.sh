#!/bin/bash
set -eo pipefail

echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
echo ""
echo "Building docker based build for Decision Support System."
echo ""
echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"

echo """
22 ec2-user: ~/miniconda3/envs/dss_env/bin/python
22 root: /root/miniconda3/envs/dss_root/bin/python
105 ec2-user: ~/miniconda3/envs/dss/bin/python
105 root: /root/miniconda3/envs/dss_root2/bin/python
"""
# collect static files at a single place for nginx server
#read -p "Collecting static, Enter full python executable[.../python]: " PYTHON_EXEC_PATH
#$PYTHON_EXEC_PATH Decision_Support_System/manage.py collectstatic
#rm -r Decision_Support_System/static
#/root/miniconda3/envs/dss_root2/bin/python Decision_Support_System/manage.py collectstatic --no-input

# To build docker container for DSS
docker build --no-cache -t dss -f Dockerfile_new .

echo ""
echo "Build is created now saving it."
echo ""

APP_VERSION=$(cat system.properties | grep APP_VERSION|cut -d '=' -f 2)

# To save docker container
docker save -o dss_$APP_VERSION.tar.gz dss

# Copy config in the current directory
cp -r Decision_Support_System/DSS/static/DSS/config .

echo ""
echo "Creating the final build archive"

# Compile tar.gz file with all the required files
tar -czf dss.tar.gz dss_$APP_VERSION.tar.gz run.sh config system.properties

# Remove config.ini from the current directory once it has been compiled in tar
rm -r config
rm dss_$APP_VERSION.tar.gz

echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
echo ""
echo "Decision Support System docker based build has been created."
echo ""
echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"