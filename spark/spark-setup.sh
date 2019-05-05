#!/usr/bin/env bash

# workaround problem with snappy compression
# https://github.com/big-data-europe/docker-spark/issues/62
apk update && apk add --no-cache libc6-compat

/spark/bin/spark-submit \
    --class com.dvl.logParser.HostnamesConnectedByTimeSpark \
    /tmp/bootstrap/spark-code.jar
