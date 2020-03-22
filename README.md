# Log4j

```xml
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
            <scope>runtime</scope>
        </dependency>
```

# maven-assembly-plugin

```xml
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>Application</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
            </plugin>
```

# Zoo keeper
    cd zookeeper/zookeeper-3.4.14/

**Config file**
    
    copy conf/zoo_sample.cfg conf/zoo.cfg
    ./bin/zkServer.sh start

**Zoo keeper client**

```bash

./zkCli.sh

ls /

create /parent "some parent data"

create /parent/child "some child data"

ls /parent

get /parent

get /parent/child

rmr /parent

create /election ""

set /target_znode "some new data"
 
```

# protoc

download:

https://github.com/protocolbuffers/protobuf/releases

Gen java code:
```bash 
~/Desktop/train/protoc-3.11.4-osx-x86_64/bin/protoc --java_out=src/main/java/ src/main/java/docSearch/model/proto/search_cluster_protos.proto
```

# ObjectMapper:
```java 
  this.objectMapper = new ObjectMapper();
  this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    
  objectMapper.readValue(requestPayload, FrontendSearchRequest.class);
```


# HAProxy

    To have launchd start haproxy now and restart at login:
      brew services start haproxy
    Or, if you don't want/need a background service you can just run:
      haproxy -f /usr/local/etc/haproxy.cfg
  
**http mode config example**

``` 
   
   global
   
   defaults
       mode http
       timeout connect 5000
       timeout client  50000
       timeout server  50000
   
   
   frontend http-in
       bind *:80
       acl even_cluster path_end -i /even
       acl odd_cluster path_end -i /odd
       
       use_backend even_servers if even_cluster
       use_backend odd_servers if odd_cluster
   
   backend even_servers
       balance roundrobin
       server server02 127.0.0.1:9001/time check
       server server03 127.0.0.1:9003/time check
   
   backend odd_servers
       balance roundrobin
       server server02 127.0.0.1:9002/time check

 ``` 
   

# Docker

**Docker for java**
   
    FROM maven:3.6.1-jdk-11 AS MAVEN_TOOL_CHAIN_CONTAINER
    RUN mkdir src
    COPY src /tmp/src
    COPY ./pom.xml /tmp/
    WORKDIR /tmp/
    RUN mvn package
    RUN ls -la /tmp
    
    FROM openjdk:11
    COPY --from=MAVEN_TOOL_CHAIN_CONTAINER /tmp/target/webapp-1.0-SNAPSHOT-jar-with-dependencies.jar /tmp/
    WORKDIR /tmp/
    ENTRYPOINT ["java","-jar", "webapp-1.0-SNAPSHOT-jar-with-dependencies.jar"]
    CMD ["80", "Server Name"]
    
   
**Docker compose**
    
    version: '3.4'
    
    services:
      app1:
        build: ./webapp 
        container_name: app1
        command: 9001 "Server 1"
        ports: 
          - "9001:9001"
    
      app2:
        build: ./webapp
        container_name: app2
        command: 9002 "Server 2"
        ports: 
          - "9002:9002"
    
      app3:
        build: ./webapp
        container_name: app3
        command: 9003 "Server 3"
        ports: 
          - "9003:9003"
    
      haproxy:
        build: ./haproxy
        container_name: haproxy
        ports:
          - "80:80"
          - "83:83"

      
# Kafka
  cd ~/Desktop/train/kafka_2.12-2.4.1 
  
  **Start server**
  
    bin/zookeeper-server-start.sh config/zookeeper.properties
  
    bin/kafka-server-start.sh config/server.properties
  
 **Create topic**
 
    bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic chat  
  
 **Describe**
   
     bin/kafka-topics.sh --describe  --bootstrap-server localhost:9092 --topic purchases
    
 **List topic** 
 
    bin/kafka-topics.sh --list --bootstrap-server localhost:9092 
  
 **Publish/Consume message**
 
    bin/kafka-console-producer.sh --broker-list localhost:9092 --topic chat
     
    bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic chat --from-beginning
  
 **Start cluster**
  
    edit config/server.properties:  broker.id, listeners, log.dirs
     
    bin/kafka-server-start.sh config/server_1.properties
    bin/kafka-server-start.sh config/server_2.properties

    bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 3 --partitions 3 --topic purchases 
     
    bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 2 --partitions 3 --topic events
    bin/kafka-topics.sh --describe  --bootstrap-server localhost:9092 --topic events
     

**Java API Producer**
```java

public static Producer<String, Transaction> createKafkaProducer(String bootstrapServers) {
        Properties properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "banking-api-service");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Transaction.TransactionSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

public static class TransactionSerializer implements Serializer<Transaction> {
        @Override
        public byte[] serialize(String topic, Transaction data) {
            byte[] serializedData = null;
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                serializedData = objectMapper.writeValueAsString(data).getBytes();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return serializedData;
        }
}

```

**Java API Consumer**

```java

public static Consumer<String, Transaction> createKafkaConsumer(String bootstrapServers, String consumerGroup) {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Transaction.TransactionDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        return new KafkaConsumer<>(properties);
    }

public static class TransactionDeserializer implements Deserializer<Transaction> {

        @Override
        public Transaction deserialize(String topic, byte[] data) {
            ObjectMapper mapper = new ObjectMapper();
            Transaction transaction = null;
            try {
                transaction = mapper.readValue(data, Transaction.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return transaction;

        }
    }

```
