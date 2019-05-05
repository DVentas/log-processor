#!/usr/bin/env bash

echo -e "\nbootstrapping would be take 5 minutes.. plase wait"

echo -e "\ncompiling spark code..."
mvn clean package -f /code/spark-code/pom.xml -Dmaven.test.skip -q
echo -e "\ncompiling kafka code..."
mvn clean package -f /code/kafka-code/pom.xml -Dmaven.test.skip -q

cp -f /code/spark-code/target/*-jar-with-dependencies.jar /bootstrap/spark/spark-code.jar
cp -f /code/kafka-code/target/*-jar-with-dependencies.jar /bootstrap/kafka/kafka-code.jar
