#!/bin/bash

mkdir -p models/spacy_models

mkdir -p models/bert_models

mkdir -p models/coref_models

mkdir -p models/semantic_models

APP_VERSION=$(cat config/config.ini | grep APP_VERSION|cut -d '=' -f 2)

aws s3 cp --recursive s3://unstructured-release-$APP_VERSION/models/spacy_models/ models/spacy_models

aws s3 cp --recursive s3://unstructured-release-$APP_VERSION/models/bert_models/ models/bert_models
    
aws s3 cp --recursive s3://unstructured-release-$APP_VERSION/models/coref_models/ models/coref_models

aws s3 cp --recursive s3://unstructured-release-$APP_VERSION/models/semantic_models/ models/semantic_models
