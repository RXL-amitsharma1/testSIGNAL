FROM python:3.7.11-slim-buster

RUN \
    mkdir /root/.Decision_Support_System/ && \
    mkdir /app/ && \
    mkdir /app/Decision_Support_System && \
    mkdir /app/pip_cache

# Installing Oracle instant client
WORKDIR /opt/oracle
COPY /libs/instantclient-basiclite-linuxx64.zip /opt/oracle
RUN apt-get update && apt-get install -y libaio1 wget unzip && \
    unzip instantclient-basiclite-linuxx64.zip && \
    rm -f instantclient-basiclite-linuxx64.zip && \
    cd instantclient* && \
    rm -f *jdbc* *occi* *mysql* *jar uidrvci genezi adrci && \
    echo /opt/oracle/instantclient* > /etc/ld.so.conf.d/oracle-instantclient.conf && \
    ldconfig && \
    apt-get install nginx nano unzip -y --no-install-recommends


COPY nginx.default /etc/nginx/sites-available/default
RUN ln -sf /dev/stdout /var/log/nginx/access.log && \
    ln -sf /dev/stderr /var/log/nginx/error.log && \
    nginx -t


# Copy required files and directory
WORKDIR /app
COPY requirements.txt start-server.sh /app/
COPY ./Decision_Support_System /app/Decision_Support_System/

RUN apt-get update && \
    buildDeps='build-essential gcc gfortran python3-dev' && \
    apt-get install -y $buildDeps --no-install-recommends && \
    apt-get install -y libopenblas-dev liblapack-dev --no-install-recommends && \
    apt-get install zlib1g-dev lib32z1-dev -y --no-install-recommends && \
    apt-get install libsasl2-dev libldap2-dev libssl-dev g++ -y --no-install-recommends && \
    apt-get install pkg-config libxml2-dev libxmlsec1-dev libxmlsec1-openssl -y --no-install-recommends && \
    wget http://xmlsoft.org/sources/libxml2-2.9.1.tar.gz && \
    tar -xvf libxml2-2.9.1.tar.gz && \
    echo "*****************1************" && \
    cd libxml2-2.9.1 && \
    ./configure && make && make install && \
    echo "*****************2************" && \
    cd ../ && \
    echo "*****************3************" && \
    pip install --no-cache-dir --upgrade pip && \
    pip3 install --no-cache-dir torch==1.10.1+cpu -f https://download.pytorch.org/whl/cpu/torch_stable.html && \
    pip install --no-cache-dir scikit-learn==1.0.1 && \
    pip install --no-cache-dir -r requirements.txt --compile --global-option=build_ext --global-option="-j 4" && \
    rm requirements.txt && \
    mv /app/Decision_Support_System/DSS/static/DSS/config /root/.Decision_Support_System/ && \
    cd /app/Decision_Support_System && \
    python setup.py build_ext --inplace && \
    python setup.py bdist_wheel && \
    rm -rf Decision_Support_System DSS && \
    unzip dist/python-1*.whl -d . && \
    rm -rf build dist python-1.* python.e* && \
    apt-get purge -y --auto-remove $buildDeps && \
    rm -rf /var/lib/apt/lists/*

EXPOSE 7000

CMD ["/app/start-server.sh"]
