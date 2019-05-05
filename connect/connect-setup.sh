#!/usr/bin/env bash

echo -e "\ninstall kafka connect spooldir\n"
confluent-hub install --no-prompt jcustenborder/kafka-connect-spooldir:latest

echo -e "\nrun connect\n"
/etc/confluent/docker/run