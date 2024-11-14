echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
echo ""
echo "Running Decision Support System docker based build."
echo ""
echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"

echo "Creating logs directory"
mkdir -p ~/.Decision_Support_System/logs/

echo "Moving config directory"
rm -r ~/.Decision_Support_System/config ~/.Decision_Support_System/system.properties
cp -r config ~/.Decision_Support_System/
cp system.properties ~/.Decision_Support_System/

echo "Memory and CPU setup from system properties"
MEMORY=$(cat ~/.Decision_Support_System/system.properties | grep MEMORY | cut -d '=' -f 2)
CPU=$(cat ~/.Decision_Support_System/system.properties | grep CPU | cut -d '=' -f 2)

# Stop and delete if there is any pre-existing docker container of the same name.
docker stop dss
docker ps -a | grep dss | cut -d ' ' -f 1 | xargs sudo docker rm

APP_VERSION=$(cat ~/.Decision_Support_System/system.properties | grep APP_VERSION|cut -d '=' -f 2)
echo "Loading following docker image: dss_$APP_VERSION.tar.gz"
docker load -i dss_$APP_VERSION.tar.gz

echo "Available images:"
docker images

echo "Running docker container"
docker run -idt -p 7000:7000 -v /tmp:/tmp -v ~/.Decision_Support_System/:/root/.Decision_Support_System/ -v ~/.Decision_Support_System/logs/:/app/Decision_Support_System/logs -m $MEMORY --cpus=$CPU --name dss dss

echo "Applying migrations if any"
docker exec dss python Decision_Support_System/manage.py makemigrations DSS
docker exec dss python Decision_Support_System/manage.py migrate DSS


echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
echo ""
echo "Decision Support System Started."
echo ""
echo "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
