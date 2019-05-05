#!/usr/bin/env bash

echo -e "\nregister input-log-avro schema\n"
curl -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    -d @/tmp/schema/InputLog.avsc http://localhost:8081/subjects/input-log-avro/versions

echo -e "\nregister hostname-connections-by-time schema\n"
curl -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    -d @/tmp/schema/HostnameByTime.avsc http://localhost:8081/subjects/hostname-connections-by-time/versions

echo -e "\nregister hostname-connections-by-time-result schema\n"
curl -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    -d @/tmp/schema/HostnameByTimeResult.avsc http://localhost:8081/subjects/hostname-connections-by-time-result/versions

echo -e "\nregister hostname-aggregations-by-hour schema\n"
curl -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    -d @/tmp/schema/HostnameAggByHour.avsc http://localhost:8081/subjects/hostname-aggregations-by-hour/versions

# Issue when registering new schema. workaround
# https://github.com/confluentinc/schema-registry/issues/518
curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"compatibility": "NONE"}' \
    http://localhost:8081/config