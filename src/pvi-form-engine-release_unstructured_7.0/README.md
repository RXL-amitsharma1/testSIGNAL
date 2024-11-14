# Unstructured Parser Module

Module for extracting entities like product, event, doseinformation, medical history , patient details etc. from the text using OpenAI and trained NER models using Spacy and  BioBert. 

## Running Unstructured module with docker:

**$APP_VERSION** -- Variable used for current release version

1. To build docker container and save it run build.sh file
   * Command: **./build.sh**</br>
	It will create build archive : **unstructured_$APP_VERSION.tar.gz** </br>
	
2. Once docker archive is successfully built, move it to the desired server location and extract the archive file.
	* Command: `tar -xvf unstructured_$APP_VERSION.tar.gz`
3. After extracting archive file **package** folder will be created ,change directory to **package** ,it will contain following files and directories:
    * Command to change directory: `cd package`
    * **Directories:**
      * config - Directory containg all the config files used in unstructured module
      * logs - Directory where logs file will be created when the unstructured api is started.
      * models - Directory where different models used in module will be stored.
    * **Files:**
      * run.sh - shell script to load compressed docker image and start docker container.
      * fetch_model_from_s3.sh - shell script to download model from S3 bucket of engineering account.
      * unstructured_$APP_VERSION.tar.gz - compressed docker image of unstrcutured module.
5. Dowload models from engineering S3 bucket using **<i>fetch_model_from_s3.sh</i>**. <br>
   By executing <i>fetch_model_from_s3.sh</i> models will be downloaded to models directory.<br>
    * Commands:
      * `sudo chmod +x fetch_from_s3.sh`
      * `./fetch_from_s3.sh`
6. After downloading model from S3 verify configs and execute **<i>run.sh</i>**
    * Commands:
      * `sudo chmod +x run.sh`
      * `./run.sh`

Executing **run.sh** will remove previous container named <i>unstructured</i> and will start new docker container with same name.
  
## Runing Unstructured module without docker
1. To run code without a docker container , install Python version>=3.12.3
2. Create a virtual environment and activate created environment
    * Using python venv library
      	* Command to create env : `python -m venv *path/to/virtualenv*` <br>
      	* Command to activate env: `source *path/to/virtualenv*/bin/activate` <br>
    * Using conda:
      	* Command to create env : `conda create -n < new environment name >`
      	* Command to activate env : `conda activate < new environment name >`

3. Install dependencies
    * `sudo apt-get install mecab mecab-ipadic-utf8 libmecab-dev`
    * `pip install -r requirements.txt`
    * `python -m nltk.downloader stopwords`
    * `python -m nltk.downloader punkt`
    * `python -m spacy download en_core_web_sm`

4. After installing dependencies download models from S3 bucket.
     <br>for downloading models from S3 shell scripts named <i>fetch_model_from_s3.sh </i> or <i>fetch_model_from_s3_ml_account.sh </i> <br>
      can be used.
    * `./fetch_model_from_s3.sh` - for downloading models from engineering account S3 bucket
    * `./fetch_model_from_s3_ml_account.sh` - for downloading models from ml account S3 bucket
5. At last after getting all the required dependencies start the flask apis either use <i>run_in_docker.sh</i> to apis as background processes or 
  start apis manually using below commands:
    * `python spacy.py`
    * `python biobert_api.py`
    * `python unstructure_pipeline.py`
    * `python parent_coreference_resolution.py`
    * `python openai_api.py`

## Sample request for API 
* Using curl
	* `curl --location --request POST 'http://127.0.0.1:9888/unstruct/live' \
--form 'text="On 15-April-2022 the patient had a Life Threatening adverse event fever. Patient reported event as Pyrexia.
The outcome of the adverse event was Not Recovered.
On 11-April-2022 The subject experienced Fever and Rash and was Hospitalized because Rash."' \
--form 'language="en"'`
* Using Python request module
	* <pre>import requests <br>
	  url = "http://127.0.0.1:9888/unstruct/live"<br>
	  payload={'text': 'On 15-April-2022 the patient had a Life Threatening adverse event fever. Patient reported event as Pyrexia.
	  The outcome of the adverse event was Not Recovered.On 11-April-2022 The subject experienced Fever and Rash and was Hospitalized because Rash.', 'language': 'en'}
	  
	  response = requests.request("POST", url, data=payload)
	  print(response.text)</pre>
