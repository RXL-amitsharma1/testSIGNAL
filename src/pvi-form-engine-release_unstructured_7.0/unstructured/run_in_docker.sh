#!/usr/bin/env bash

set -e pipefail

#shutsdown server as well, Required to load new configs
if [ $(redis-cli ping | grep PONG) ]; then echo "Shuting down existing redis..."; redis-cli shutdown; sleep 3; else echo "No existing redis found"; fi

redis-server /unstructured/config/redis.conf &
# redis-server `pwd`/config/redis_mac.conf &
sleep 5

if [ $(redis-cli ping | grep PONG) ]; then echo "redis-server started"; else echo "redis-server start failed"; exit 126; fi

TIMEOUT=$(cat /unstructured/config/config.ini | grep GUNICORN_TIMEOUT|cut -d '=' -f 2)
WORKERS=$(cat /unstructured/config/config.ini | grep GUNICORN_WORKERS|cut -d '=' -f 2)
THREADS=$(cat /unstructured/config/config.ini | grep GUNICORN_THREADS|cut -d '=' -f 2)

# exec python /unstructured/unstructure_pipeline.py &
exec python /unstructured/src/src_en/spacy_api.py &
exec python /unstructured/src/src_en/biobert_api.py &
exec python /unstructured/src/src_en/parent_coreference_resolution.py &
exec python /unstructured/src/openai_api.py &
cd /unstructured/src/ && exec gunicorn -w $WORKERS --threads $THREADS -t $TIMEOUT wsgi-unstructured:app -b 0.0.0.0:9888
