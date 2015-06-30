### how to set this up on azure? ###

$ openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout resolver.key -out resolver.pem

$ chmod 600 *.key

$ azure account affinity-group create -l "West Europe" -d "demo affinity group" DemoResolvers1

$ azure network vnet create --subnet-name frontend --affinity-group DemoResolvers1 DemoResolvers1Network

$ azure vm docker create --affinity-group "DemoResolvers1" --availability-set "DemoResolvers1" --vm-size "Small" --vm-name "demo1-sbresolver" --ssh 22201 --ssh-cert resolver.pem --no-ssh-password --virtual-network-name "DemoResolvers1Network" --subnet-names "DemoResolvers1Network" demo1-sbresolver "2b171e93f07c4903bcad35bda10acf22__CoreOS-Stable-607.0.0" smurf

$ ssh -i resolver.key <your-user-smurf>@<your-dns-name>.cloudapp.net -p 22201

$ mkdir -p /data/elasticsearch; mkdir -p /data/resolver

$ azure vm list

$ docker --tls -H tcp://<your-dns>.cloudapp.net:4243 pull elasticsearch:1.4.4

$ azure vm endpoint list staging-sbresolver-1

$ docker --tls -H tcp://staging-sbresolver-1.cloudapp.net:4243 pull java:8-jre

$ docker --tls -H tcp://staging-sbresolver-1.cloudapp.net:4243 run --name elasticsearch -d -p 9200:9200 -p 9300:9300 -v /data:/data/elasticsearch elasticsearch:1.4.4

$ docker --tls -H tcp://staging-sbresolver-1.cloudapp.net:4243 run --name resolver --link elasticsearch:elasticsearch -d -p 8080:8080 -v /data/resolver:/data/resolver java:8-jre bash -c "java -jar /data/resolver/resolver.jar --elasticsearch.connectionString=\$ELASTICSEARCH_PORT_9300_TCP"

CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAMES
e42c4eccc244 java:8-jre "bash c 'java -jar 10 seconds ago Up 6 seconds 0.0.0.0:8080>8080/tcp resolver
90cecbf2d13a elasticsearch:1.4.4 "/docker-entrypoint. 24 hours ago Up 23 hours 0.0.0.0:9200->9200/tcp, 0.0.0.0:9300->9300/tcp elasticsearch

$ docker --tls -H tcp://staging-sbresolver-1.cloudapp.net:4243 ps

$ azure vm endpoint create staging-sbresolver-1 80 8080
