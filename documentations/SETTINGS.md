# Settings

The following table shows and explains all possible Engine configurations options.

## MarkovShieldClickstreams

Configuration | Parameter | Default value | Description
--|--|--|--
Helpmessage | help | - | Displays a message with all possible parameters
Kafka broker address | bootstrap | broker:9092 | The hostname and port of the Kafka broker
Zookeeper address | zookeeper | zookeeper:2181 | The hostname and port of the Zookeeper instance
Address of the schemaregistry | schemaregistry | http://schemaregistry:8081 | The address of the Schemaregistry
Number of threads | numthreads | 1 | The number of threads that Kafka streams will use
REST endpoint name | resthostname | localhost | The hostname of the REST endpoint, where it's possible to request the newest data. This should be set to the actual hostname in an distributed environment with multiple MarkovShieldClickstreams applications running
REST endpoint port | restport | 7777 | The port of the REST endpoint


## MarkovShieldModelUpdater

Configuration | Parameter | Default value | Description
--|--|--|--
Helpmessage | help | - | Displays a message with all possible parameters
Kafka broker address | bootstrap | broker:9092 | The hostname and port of the Kafka broker
Zookeeper address | zookeeper | zookeeper:2181 | The hostname and port of the Zookeeper instance
Lookbackperiod | lookbackperiod | 259200 |  The lookbackperiod defines how long an passed clickstream will be used it build ClickStreamModels. This parameter is defined in minutes and its default is set to 6 Months
Session timeout | sessiontimeout | 60 | The session timeout defines how long a passive session is still open. This parameter is in minutes.
Update interval | updateinterval | 1440 | The update interval defines in which cycle the ClickStreamModels should be recreated. This parameter is in minutes and it defaults to 24 hours.

## MarkovShieldAnalyser

Configuration | Parameter | Default value | Description
--|--|--|--
Helpmessage | help | - | Displays a message with all possible parameters
Kafka broker address | bootstrap | broker:9092 | The hostname and port of the Kafka broker
Zookeeper address | zookeeper | zookeeper:2181 | The hostname and port of the Zookeeper instance
Redis host | redishost | redis |  The hostname of the Redis server
Redis port | redisport | 6379 | The port of the Redis server
