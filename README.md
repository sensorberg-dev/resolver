#Resolver

The Resolver is a microservice that you can run in your own infrastructure or hosted by us, resposible for delivering all neccesary data to the SDKs in order for them to map a beacon the the attached content.

The Resolver runs standalone as a [Spring boot](http://projects.spring.io/spring-boot/) application.

Simply run

```
./gradlew run
```
to have your own instance.

#Dependencies

Only an instance of [elastic search](https://www.elastic.co) needs to be available on your machine, or available to your machine.

```
brew install elasticsearch
elasticsearch --config=/usr/local/opt/elasticsearch/config/elasticsearch.yml
[1990-01-01 00:00:59,300][INFO ][node                     ] [Deacon Frost] version[1.5.1], pid[49391], build[5e38401/2015-04-09T13:41:35Z]
[1990-01-01 00:00:59,300][INFO ][node                     ] [Deacon Frost] initializing ...
[1990-01-01 00:00:59,305][INFO ][plugins                  ] [Deacon Frost] loaded [], sites []
[1990-01-01 00:00:01,614][INFO ][node                     ] [Deacon Frost] initialized
[1990-01-01 00:00:01,615][INFO ][node                     ] [Deacon Frost] starting ...
[1990-01-01 00:00:01,678][INFO ][transport                ] [Deacon Frost] bound_address {inet[/127.0.0.1:9300]}, publish_address {inet[/127.0.0.1:9300]}
[1990-01-01 00:00:01,693][INFO ][discovery                ] [Deacon Frost] elasticsearch_brew/Me5IaghuRIqvbuoohcb4fQ
[1990-01-01 00:00:05,470][INFO ][cluster.service          ] [Deacon Frost] new_master [Deacon Frost][Me5IaghuRIqvbuoohcb4fQ][retina-volker-book.local][inet[/127.0.0.1:9300]], reason: zen-disco-join (elected_as_master)
[1990-01-01 00:00:05,491][INFO ][http                     ] [Deacon Frost] bound_address {inet[/127.0.0.1:9200]}, publish_address {inet[/127.0.0.1:9200]}
[1990-01-01 00:00:05,491][INFO ][node                     ] [Deacon Frost] started

```
You might need to change the connection string in the Resolver in */src/main/resources/application.properties*

```
elasticsearch.connectionString=http://localhost:9300/elasticsearch_brew
spring.jackson.date-format=com.fasterxml.jackson.databind.util.ISO8601DateFormat
apiKey.largeCompany=10000
```
The name of the cluster, when installing from brew is elasticsearch_brew

#Standalone

Once build, the jar artifact can be run standalone. *java -jar service-resolve.jar* and off you go!
```
 ./gradlew build
:compileJava UP-TO-DATE
:compileGroovy UP-TO-DATE
:processResources UP-TO-DATE
:classes UP-TO-DATE
:jar
:bootRepackage
:assemble
:compileTestJava UP-TO-DATE
:compileTestGroovy
:processTestResources UP-TO-DATE
:testClasses
:test
:check
:build

BUILD SUCCESSFUL

Total time: 7.354 secs
➜  service-resolve git:(master) java -jar build/libs/service-resolve.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.2.2.RELEASE)
 [...]
```

#API endpoints:

For a full list, visit our [readme.io live API page](https://sensorberg.readme.io/)

To get started quickly, here is a list of the most important endpoints:

##GET /ping
###return value:
Get stats about the running service
```
{
    "version": null,
    "beacon": 0,
    "action": 0,
    "application": 0,
    "syncApplications": 0,
    "synchronizationLog": 0,
    "monitoringLog": 0,
    "layoutLog": 0
}
```

##POST /synchronizations
Setup a synchronization configuration. The resolver will keep in sync with your changes.
###*headers*
```
{
    "content-type" : "application/json"
}
```
###*body*
```
{
	"host" : "https://connect.sensorberg.com",
	"apiKey" : "852d6a72cb8980ddadf0355ece37fa1c90ac9359b6ffc6accb47847f43eaf904"
}
```
With your apiKey from [manage.sensorberg.com/#/applications](https://manage.sensorberg.com/#/applications)
###*return value*
```
{
    "host": "https://connect.sensorberg.com",
    "apiKey": "852d6a72cb8980ddadf0355ece37fa1c90ac9359b6ffc6accb47847f43eaf904",
    "token": null,
    "environment": "https://connect.sensorberg.com/852d6a72cb8980ddadf0355ece37fa1c90ac9359b6ffc6accb47847f43eaf904"
}
```
##GET /synchronizations
Get a list of all the synchronizations that are set up for this host:
###return value:
```
[
    {
        "host": "https://connect.sensorberg.com",
        "apiKey": "852d6a72cb8980ddadf0355ece37fa1c90ac9359b6ffc6accb47847f43eaf904",
        "token": null,
        "environment": "https://connect.sensorberg.com/852d6a72cb8980ddadf0355ece37fa1c90ac9359b6ffc6accb47847f43eaf904"
    }
]
```
##GET /layout
Return the beacon layout for the api key
###headers:
```
{
    "X-Api-Key" : "852d6a72cb8980ddadf0355ece37fa1c90ac9359b6ffc6accb47847f43eaf904"
}
```
###return value
```
{
     "accountProximityUUIDs": [
         "7367672374000000ffff0000ffff0000"
     ],
     "actions": [
         {
             "eid": "2dbef6d02726416bbc392f6d69356306",
             "trigger": 1,
             "beacons": [
                 "7367672374000000ffff0000ffff00004845852108"
             ],
             "supressionTime": -1,
             "content": {
                 "body": "World",
                 "payload": null,
                 "subject": "Hello",
                 "url": "http://developer.sensorberg.com"
             },
             "type": 1,
             "timeframes": [
                 {
                     "start": "2015-04-14T14:19:37Z"
                 }
             ],
             "sendOnlyOnce": false,
             "typeString": "notification"
         }
     ]
}
```

If you want to use your own resolver in your owm application with the [Android SDK](/android-sdk) add this line to your AndroidManifest.xml:
```
<meta-data
    android:name="com.sensorberg.sdk.resolverURL"
    android:value="http://<your-ip-address>:8080/layout" />
```
<br/>