#!/bin/bash
./bin/james stop
LOGDIR=log/
ARCHDIR=${LOGDIR}archive/
zip -m "${ARCHDIR}logs-$(date '+%Y-%m-%d %H.%M.%S').zip"  ${LOGDIR}*.*
./bin/james start
