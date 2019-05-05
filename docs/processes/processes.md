# Processes docs

- **Spark**
    This process run indefinitely reading events from topic "hostname-connections-by-time". Each event has timestamp init, timestamp end and hostname. This data is the request to find in hdfs results and return list of hostname connected between timestamp to the hostname. It send events to "hostname-connections-by-time-result" topic.
    
- **Kafka-Stream**
    Read hostname-connections-agg-by-hour topic. In this topic comes hostname that will be used to aggregate topic input-log-avro and return aggregations every hour. The event produced should has hostname connected and hostname received in last hour and the name of hostname with more connexions (the last part of use cases its not finished).  