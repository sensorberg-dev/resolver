#!/bin/sh
gradle --daemon clean build
scp -i ~/.ssh/key/resolver.key -P 22201 build/libs/service-resolve.jar smurf@staging-sbresolver-1.cloudapp.net:/data/resolver/resolver.jar
docker --tls -H tcp://staging-sbresolver-1.cloudapp.net:4243 restart resolver