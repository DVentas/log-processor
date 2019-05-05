#!/usr/bin/env bash

echo -e "\nstart spool dir input log\n\n"
curl -H "Content-Type:application/json" -d @config/input-log.json http://localhost:8083/connectors

echo -e "\nstart spool dir hostname by time\n\n"
curl -H "Content-Type:application/json" -d @config/hostnames-by-time.json http://localhost:8083/connectors

echo -e "\nstart spool dir hostname agg by hour\n\n"
curl -H "Content-Type:application/json" -d @config/hostnames-agg-by-hour.json http://localhost:8083/connectors

echo -e "\nstart sink connector to hdfs\n\n"
curl -H "Content-Type:application/json" -d @config/hdfs-sink.json http://localhost:8083/connectors
