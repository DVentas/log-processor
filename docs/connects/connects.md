# Connects docs

- **input-log**
    Read csv files from folder "/inputs/logs/in/". Files with errors will be drop to "/inputs/logs/errors/" folder. 
    Each line will be parsed to avro with InputLog schema (/schema/inputLog.avsc) and create event in input-log-avro topic.
    Configuration: /connect/config/input-log.json
- **hostname-by-time**
    Read csv files from folder "/inputs/hostnameByTime/in/". Files with errors will be drop to "/inputs/hostnameByTime/errors/" folder. 
    Each line will be parsed to avro with HostnameByTime schema (/schema/HostnameByTime.avsc) and create event in hostname-by-time topic. 
    Configuration: /connect/config/hostnames-by-time.json
- **hostname-agg-by-hour**
    Read csv files from folder "/inputs/hostnameAggByHour/in/". Files with errors will be drop to "/inputs/hostnameAggByHour/errors/" folder. 
    Each line will be parsed to avro with HostnameAggByHour schema (/schema/HostnameAggByHour.avsc) and create event in hostname-aggregations-by-hour topic.
    Configuration: /connect/config/hostnames-agg-by-hour.json
- **hdfs-sink**
    Read each event from input-log-avro topic and write in hdfs docker in path "/data/input-log/" in parquet format.
    Configuration: /connect/config/hdfs-link.json