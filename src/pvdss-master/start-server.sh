#!/usr/bin/env bash
# start-server.sh

TIMEOUT=$(cat /root/.Decision_Support_System/system.properties | grep TIMEOUT | cut -d '=' -f 2)
WORKERS=$(cat /root/.Decision_Support_System/system.properties | grep WORKERS | cut -d '=' -f 2)

(cd Decision_Support_System; gunicorn Decision_Support_System.wsgi --bind 0.0.0.0:7010 --workers $WORKERS --timeout $TIMEOUT) & nginx -g "daemon off;"