# Install Python3

# – For Centos/Fedora

yum install -y zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel gdbm-devel db4-devel libpcap-devel xz-devel expat-devel libffi-dev gcc gcc-c++ zlib libffi-devel python-devel openldap-devel

# – For Ubuntu/ LinuxMint/Debian
# apt-get install build-essential libncursesw5-dev libreadline5-dev libssl-dev libgdbm-dev libc6-dev libsqlite3-dev tk-dev -y

yum install wget -y

# Download the latest release of python
wget https://www.python.org/ftp/python/3.8.6/Python-3.8.6.tar.xz

yum install xz-utils -y
yum install gcc -y
yum install make -y
yum -y install zlib-devel

tar -xvf Python-3.8.6.tar.xz
cd Python-3.8.6

# 4. Install Python
./configure --prefix=/opt/python3
make
make install

# 5. Python will now be installed to /opt/python3
/opt/python3/bin/python3 -V

## ** Optional: You may need to change ownership of Python directory for current user
chown -R $USER:$USER /opt/python3

rm /usr/bin/python
ln -s /opt/python3/bin/python3 /usr/bin/python

rm /usr/bin/pip
ln -s /opt/python3/bin/pip /usr/bin/pip