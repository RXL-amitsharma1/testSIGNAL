# PV Decision Support System

Story requirements: https://rxlogixdev.atlassian.net/browse/PVS-8403

Design Documentation: https://rxlogixdev.atlassian.net/wiki/spaces/SIG/pages/1705148641/DSS+Network+Structure


## Running DSS module with docker:

DSS should be deployed on the same machine where PV Signal is deployed.

1. To build docker container and save it run build.sh file
   * Command: **./build.sh**</br>
	It will create build archive : dss.tar.xz </br>
	
2. Once docker archive is successfully built, move it to the desired server location and load the file then run the shell 
   script to up docker container. 
   * Command to load docker image: **sudo docker load -i #path to image tar file#**</br>
   * Command to run docker image: **./run.sh**</br>

3. .Decision_Support_System directory is created in home folder and it has following files in it:-
   - config : This is a directory where configurable files like config.ini, node_labels, descriptions are present. 
   - system.properties : This file contains system level properties like RAM, CPU and API timeout. 
   - logs(directory) : This is a directory where logs are generated.
     
## Running DSS module without docker:

1. To run code without a docker container, install Python if it is not pre-installed on the server or if it is in a version< 3.6
	- Open Terminal and check whether Python is installed using below command
		* Command: **python --version**	</br>
		  Above command would give the anaconda version and path details if present.

	- If python version<3.6, navigate to pvdss and run install.sh file
		* Command: **./install.sh** </br>

	- Once python has been successfully setup, check pip version by this command
	    * Command: **pip --version** </br>
	If pip version is old, then update it with the pop up command in the message

2. Create python virtual environment:
   * Command: **python -m venv *path/to/virtualenv*** </br>
   
3. Activate Virtual environment and install the dependencies
   * Command: **source *env_name*/bin/activate** </br>
   * Command: **python -m pip install -r requirements.txt** </br>
4. Create nginx configurations for DSS
    	- Install nginx by running below command<br/>
		* Command: **sudo apt-get install nginx**<br/>
    	- Create a file /etc/nginx/sites-available/Decision_Support_System and type in the following:

      
      
		  server {
		    listen 7000;
		    server_name 0.0.0.0;

		    location = /favicon.ico { access_log off; log_not_found off; }

		    location /static/ {
			    root /home/ubuntu/pvs-ml/pvdss/Decision_Support_System;
		    }

		    location / {
			    include proxy_params;
			    proxy_pass http://unix:/home/ubuntu/pvs-ml/pvdss/Decision_Support_System/Decision_Support_System.sock;
			}
		  }


	Adjust the paths such as /home/ubuntu/pvs-ml/pvdss/Decision_Support_System to your own environment.

	The first two lines tell that it will listen to the port 7000 on 0.0.0.0. The next line about favicon will tell Nginx to ignore problems 	 with favicon.ico.

	The next block is very important. It says that static files, which all have a standard URI prefix of static/ should be looked for in 		/home/ubuntu/pvs-ml/pvdss/Decision_Support_System/static/ folder.

      And the last location block matches all other requests other that static ones. One thing to note here is that Nginx and Gunicorn "talk to" each other             through a unix socket. 
  
  - Enable this file by linking it to the sites-enabled folder:<br/>
  	* Command: **sudo ln -s /etc/nginx/sites-available/myproject /etc/nginx/sites-enabled**

  - check if our configuration file was correctly written: <br/>
  	* Command: **sudo nginx -t**
    
	If everything is OK, you should see something like this:

      

	      nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
	      nginx: configuration file /etc/nginx/nginx.conf test is successful 

    
    
5. After successfully establishing nginx connection run this command to up DSS ML API.
   * Command: **gunicorn --daemon --workers 2 --timeout 300 --bind unix:/home/ubuntu/pvs-ml/pvdss/Decision_Support_System.sock Decision_Support_System.wsgi**</br>
   
   
   
   
#### Open url with path - 'host:port/dss' in browser  to check whether app is up. It would show a message 'DSS Application is up'
