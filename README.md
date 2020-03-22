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
```

# Root logger option
log4j.rootLogger=WARN, stdout
# Direct log messages to stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target=System.out
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{HH:mm:ss} %-5p %c{1}:%L - %m%n

log4j.logger.com.myapp=INFO

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

# MongoDB

    mkdir -p /usr/local/var/mongod/data/db
    less mongod.conf
```
net:
    bindIp: 127.0.0.1
    # MongoDB server listening port
    port: 27017
storage:
    # Data store directory
    dbPath: "/usr/local/var/mongod/data/db"
systemLog:
    # Write logs to log file
    destination: file
    path: "/usr/local/var/mongod/mongodb.log"  

```    
   
**Start Server**   
 
    bin/mongod --config mongod.conf -v

    ./bin/mongo --port 27018

**Commands**
     
     help
     show dbs
     show collections
     
     use online-school

     db.createCollection("students')
     
     db.students.insertOne( {"name": "Michael", "age": 25, favorite_colors: ["blue", "yellow"]} )
     db.students.insertOne( { "_id" : "5bc",  "name": "Michael2", "age": 25, favorite_colors: ["blue", "yellow"]} )
     
     db.students.find().pretty()
     
     db.students.insertMany( [
        {"name": "Michael3", "age": 23, favorite_colors: ["blue", "yellow", "white" ]},
        {"name": "Michael4", "age": 24, favorite_colors: ["blue", "yellow", "green"]}
     ] )
     
     db.students.find({ name: "Michael"})
     
     db.students.find({ age: { $gt: 24} })
     
     db.students.find( { $or : [ {favorite_colors: "blue" }, {favorite_colors: "yellow"}  ] } ).limit(2)
     
     db.students.updateOne( {"name": "Michael"}, {$set : {age: 32}} )
     
     db.students.deleteMany( { age: { $lt: 25 } } )

**Launch cluster**
    
    mkdir -p /usr/local/var/mongodb/rs0-1
    mkdir -p /usr/local/var/mongodb/rs0-2
    mkdir -p /usr/local/var/mongodb/rs0-3
    
    bin/mongod --replSet rs0 --port 27017 --bind_ip 127.0.0.1 --dbpath /usr/local/var/mongodb/rs0-1 --oplogSize 128
    bin/mongod --replSet rs0 --port 27018 --bind_ip 127.0.0.1 --dbpath /usr/local/var/mongodb/rs0-2 --oplogSize 128
    bin/mongod --replSet rs0 --port 27019 --bind_ip 127.0.0.1 --dbpath /usr/local/var/mongodb/rs0-3 --oplogSize 128
    
    
    ./bin/mongo --port 27017
    
    rs.initiate({
       _id: "rs0",
       members: [
         {
            _id: 0,
            host: "127.0.0.1:27017"
         },
         
         {
            _id: 1,
            host: "127.0.0.1:27018"
          },
           
          {
            _id: 2,
            host: "127.0.0.1:27019"
          }
       ]
    })
    
**Java code to connect cluster**

```java

    private static final String MONGO_DB_URL = "mongodb://127.0.0.1:27017,127.0.0.1:27018,127.0.0.1:27019/?replicaSet=rs0";

    MongoCollection<Document> courseCollection = database.getCollection(courseName)
                    .withWriteConcern(WriteConcern.MAJORITY)
                    .withReadPreference(ReadPreference.primaryPreferred());

```

**Sharded MongoDB Cluster**

![Image](./tree/master/resources/sharded-mongodb.jpg?raw=true)

* launch config server

``` 
     cd /Users/yongliu/Desktop/train/mongodb-macos-x86_64-4.2.3
     
     mkdir -p /usr/local/var/mongodb/config-srv-0
     mkdir -p /usr/local/var/mongodb/config-srv-1
     mkdir -p /usr/local/var/mongodb/config-srv-2
    
    bin/mongod --configsvr --replSet config-rs \
         --dbpath  /usr/local/var/mongodb/config-srv-0 \
         --bind_ip 127.0.0.1 --port 27020
    
    bin/mongod --configsvr --replSet config-rs \
        --dbpath  /usr/local/var/mongodb/config-srv-1 \
        --bind_ip 127.0.0.1 --port 27021
        
    bin/mongod --configsvr --replSet config-rs \
        --dbpath  /usr/local/var/mongodb/config-srv-2 \
        --bind_ip 127.0.0.1 --port 27022
    
    
```
* initiate config server

```

 bin/mongo --port 27020

 rs.initiate({
           _id: "config-rs",
           configsvr: true,
           members: [
               { _id: 0, host: "127.0.0.1:27020" },
               { _id: 1, host: "127.0.0.1:27021" },
               { _id: 2, host: "127.0.0.1:27022" }
           ]
         })
```

* launch mongodb shards
    
```
cd /Users/yongliu/Desktop/train/mongodb-macos-x86_64-4.2.3

mkdir -p /usr/local/var/mongodb/shard-0
mkdir /usr/local/var/mongodb/shard-1
mkdir /usr/local/var/mongodb/shard-2


bin/mongod --shardsvr --port 27017 --bind_ip 127.0.0.1 --dbpath /usr/local/var/mongodb/shard-0 --oplogSize 128
bin/mongod --shardsvr --port 27018 --bind_ip 127.0.0.1 --dbpath /usr/local/var/mongodb/shard-1 --oplogSize 128
bin/mongod --shardsvr --port 27019 --bind_ip 127.0.0.1 --dbpath /usr/local/var/mongodb/shard-2 --oplogSize 128

```

* launch mongos(router)

```
bin/mongos --configdb config-rs/127.0.0.1:27020,127.0.0.1:27021,127.0.0.1:27022 --bind_ip 127.0.0.1 --port 27023

```

* add shards to mongos(router)

```
bin/mongo --port 27023

sh.addShard("127.0.0.1:27017")
sh.addShard("127.0.0.1:27018")
sh.addShard("127.0.0.1:27019")


# change default chunksize for dev

show dbs
use config
db.settings.save ( {_id: "chunksize", value: 1} )  # change chunksize to 1M

``` 
 

* Range shards for movies

```
bin/mongo --port 27023

mongos> use videodb
mongos> sh.enableSharding("videodb")

db.movies.insertOne({
"name": "Pulp Fiction",
"directors": [ "Quentin Tarantino" ],
"year": 1994,
"cast": [ "Amanda Plummer", "Samuel L.Jackson", "Bruce Willis" ],
"rating": 10.0
})

db.movies.insertOne({
"name": "Pulp Fiction2",
"directors": [ "Quentin Tarantino 2" ],
"year": 1994,
"cast": [ "Amanda Plummer 2", "Samuel L.Jackson 2", "Bruce Willis 2" ],
"rating": 9.0
})


db.movies.insertOne({
"name": "Pulp Fiction 3",
"directors": [ "Quentin Tarantino 3" ],
"year": 1994,
"cast": [ "Amanda Plummer 3", "Samuel L.Jackson 3", "Bruce Willis 3" ],
"rating": 8.0
})


db.movies.createIndex( { name: 1} ) # sort asc

sh.shardCollection("videodb.movies",  { name: 1})

sh.status() # check shard info

```

```
sh.status()

mongos> sh.status()
--- Sharding Status ---
  sharding version: {
  	"_id" : 1,
  	"minCompatibleVersion" : 5,
  	"currentVersion" : 6,
  	"clusterId" : ObjectId("5e7758876a86567819c34eb6")
  }
  shards:
        {  "_id" : "shard0000",  "host" : "127.0.0.1:27017",  "state" : 1 }
        {  "_id" : "shard0001",  "host" : "127.0.0.1:27018",  "state" : 1 }
        {  "_id" : "shard0002",  "host" : "127.0.0.1:27019",  "state" : 1 }
  active mongoses:
        "4.2.3" : 1
  autosplit:
        Currently enabled: yes
  balancer:
        Currently enabled:  yes
        Currently running:  no
        Failed balancer rounds in last 5 attempts:  0
        Migration Results for the last 24 hours:
                No recent migrations
  databases:
        {  "_id" : "config",  "primary" : "config",  "partitioned" : true }
                config.system.sessions
                        shard key: { "_id" : 1 }
                        unique: false
                        balancing: true
                        chunks:
                                shard0000	1
                        { "_id" : { "$minKey" : 1 } } -->> { "_id" : { "$maxKey" : 1 } } on : shard0000 Timestamp(1, 0)
        {  "_id" : "videodb",  "primary" : "shard0002",  "partitioned" : true,  "version" : {  "uuid" : UUID("f411cb48-c337-465f-aaeb-fc38497164ab"),  "lastMod" : 1 } }
                videodb.movies
                        shard key: { "name" : 1 }
                        unique: false
                        balancing: true
                        chunks:
                                shard0002	1
                        { "name" : { "$minKey" : 1 } } -->> { "name" : { "$maxKey" : 1 } } on : shard0002 Timestamp(1, 0)

mongos>

```  

* add documents

run MoviesGenerator - gen 10000 movies

```
mongos> sh.status()
--- Sharding Status ---
  ....

  databases:
       {  "_id" : "videodb",  "primary" : "shard0002",  "partitioned" : true,  "version" : {  "uuid" : UUID("f411cb48-c337-465f-aaeb-fc38497164ab"),  "lastMod" : 1 } }
                videodb.movies
                        shard key: { "name" : 1 }
                        unique: false
                        balancing: true
                        chunks:
                                shard0000	4
                                shard0001	4
                                shard0002	4
                        { "name" : { "$minKey" : 1 } } -->> { "name" : "Abjiebjk" } on : shard0000 Timestamp(3, 0)
                        { "name" : "Abjiebjk" } -->> { "name" : "Cpzhra" } on : shard0001 Timestamp(5, 0)
                        { "name" : "Cpzhra" } -->> { "name" : "Fgvoztkd" } on : shard0000 Timestamp(6, 0)
                        { "name" : "Fgvoztkd" } -->> { "name" : "Htduvhsuo" } on : shard0001 Timestamp(7, 0)
                        { "name" : "Htduvhsuo" } -->> { "name" : "Khgrvvibob" } on : shard0001 Timestamp(8, 0)
                        { "name" : "Khgrvvibob" } -->> { "name" : "Mwkckj" } on : shard0000 Timestamp(9, 0)
                        { "name" : "Mwkckj" } -->> { "name" : "Pksavzxnzb" } on : shard0001 Timestamp(10, 0)
                        { "name" : "Pksavzxnzb" } -->> { "name" : "Rwilzehl" } on : shard0000 Timestamp(11, 0)
                        { "name" : "Rwilzehl" } -->> { "name" : "Uitvyhdio" } on : shard0002 Timestamp(11, 1)
                        { "name" : "Uitvyhdio" } -->> { "name" : "Wugwblkb" } on : shard0002 Timestamp(4, 9)
                        { "name" : "Wugwblkb" } -->> { "name" : "Xydfmmfn" } on : shard0002 Timestamp(4, 10)
                        { "name" : "Xydfmmfn" } -->> { "name" : { "$maxKey" : 1 } } on : shard0002 Timestamp(5, 1)

mongos>


```
  
* Hash shards for users

``` 
cd ~/Desktop/train/mongodb-macos-x86_64-4.2.3

bin/mongo --port 27023

use vediodb
db.users.insertOne({
   "user_name": "Miachael Liu",
   "watched_movies": [ "Moana", "Pulp Fiction" ],
   "favorite_genres": ["anime", "action", "super hero" ],
   "subscription_month": 10
})

db.users.insertOne({
   "user_name": "Miachael Liu 2",
   "watched_movies": [ "Moana 2", "Pulp Fiction" ],
   "favorite_genres": ["anime", "action", "super hero 2" ],
   "subscription_month": 8
})


db.users.createIndex({ _id: "hashed" })

sh.shardCollection("videodb.users", { _id: "hashed" })

sh.status(true)

```

``` 

             videodb.users
                        shard key: { "_id" : "hashed" }
                        unique: false
                        balancing: true
                        chunks:
                                shard0000	2
                                shard0001	2
                                shard0002	2
                        { "_id" : { "$minKey" : 1 } } -->> { "_id" : NumberLong("-6148914691236517204") } on : shard0000 Timestamp(1, 0)
                        { "_id" : NumberLong("-6148914691236517204") } -->> { "_id" : NumberLong("-3074457345618258602") } on : shard0000 Timestamp(1, 1)
                        { "_id" : NumberLong("-3074457345618258602") } -->> { "_id" : NumberLong(0) } on : shard0001 Timestamp(1, 2)
                        { "_id" : NumberLong(0) } -->> { "_id" : NumberLong("3074457345618258602") } on : shard0001 Timestamp(1, 3)
                        { "_id" : NumberLong("3074457345618258602") } -->> { "_id" : NumberLong("6148914691236517204") } on : shard0002 Timestamp(1, 4)
                        { "_id" : NumberLong("6148914691236517204") } -->> { "_id" : { "$maxKey" : 1 } } on : shard0002 Timestamp(1, 5)

```

* add documents (hashed)

run UsersGenerator - gen 10000 users

```

videodb.users
                        shard key: { "_id" : "hashed" }
                        unique: false
                        balancing: true
                        chunks:
                                shard0000	6
                                shard0001	6
                                shard0002	6
                        { "_id" : { "$minKey" : 1 } } -->> { "_id" : NumberLong("-7672839211562217394") } on : shard0000 Timestamp(2, 13)
                        { "_id" : NumberLong("-7672839211562217394") } -->> { "_id" : NumberLong("-6153267075535769034") } on : shard0000 Timestamp(2, 14)
                        { "_id" : NumberLong("-6153267075535769034") } -->> { "_id" : NumberLong("-6148914691236517204") } on : shard0000 Timestamp(2, 15)
                        { "_id" : NumberLong("-6148914691236517204") } -->> { "_id" : NumberLong("-4581670205206018460") } on : shard0000 Timestamp(2, 4)
                        { "_id" : NumberLong("-4581670205206018460") } -->> { "_id" : NumberLong("-3097402018739567425") } on : shard0000 Timestamp(2, 5)
                        { "_id" : NumberLong("-3097402018739567425") } -->> { "_id" : NumberLong("-3074457345618258602") } on : shard0000 Timestamp(2, 6)
                        { "_id" : NumberLong("-3074457345618258602") } -->> { "_id" : NumberLong("-1613183966195811662") } on : shard0001 Timestamp(2, 7)
                        { "_id" : NumberLong("-1613183966195811662") } -->> { "_id" : NumberLong("-61965429116146353") } on : shard0001 Timestamp(2, 8)
                        { "_id" : NumberLong("-61965429116146353") } -->> { "_id" : NumberLong(0) } on : shard0001 Timestamp(2, 9)
                        { "_id" : NumberLong(0) } -->> { "_id" : NumberLong("1348797697994699667") } on : shard0001 Timestamp(2, 16)
                        { "_id" : NumberLong("1348797697994699667") } -->> { "_id" : NumberLong("2605891455325156239") } on : shard0001 Timestamp(2, 17)
                        { "_id" : NumberLong("2605891455325156239") } -->> { "_id" : NumberLong("3074457345618258602") } on : shard0001 Timestamp(2, 18)
                        { "_id" : NumberLong("3074457345618258602") } -->> { "_id" : NumberLong("4610046900827548174") } on : shard0002 Timestamp(2, 1)
                        { "_id" : NumberLong("4610046900827548174") } -->> { "_id" : NumberLong("6129659994746535288") } on : shard0002 Timestamp(2, 2)
                        { "_id" : NumberLong("6129659994746535288") } -->> { "_id" : NumberLong("6148914691236517204") } on : shard0002 Timestamp(2, 3)
                        { "_id" : NumberLong("6148914691236517204") } -->> { "_id" : NumberLong("7497862367770989095") } on : shard0002 Timestamp(2, 10)
                        { "_id" : NumberLong("7497862367770989095") } -->> { "_id" : NumberLong("8757439933620096769") } on : shard0002 Timestamp(2, 11)
                        { "_id" : NumberLong("8757439933620096769") } -->> { "_id" : { "$maxKey" : 1 } } on : shard0002 Timestamp(2, 12)

mongos>
```

* Fix hot movies read

![Image](./tree/master/resources/sharded-mongodb-hotread.jpg?raw=true)

db.movies.find({...}).readPref( {"secondaryPreferred"} )






   