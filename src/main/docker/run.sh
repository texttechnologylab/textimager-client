#!/bin/bash

set -e

# update ssh key access rights
chown -Rf ducc.ducc /home/ducc/.ssh
chmod 700 /home/ducc/.ssh
chmod 600 /home/ducc/.ssh/id_rsa
chmod 644 /home/ducc/.ssh/id_rsa.pub

chown -Rf root.root /root/.ssh
chmod 700 /root/.ssh
chmod 600 /root/.ssh/id_rsa
chmod 644 /root/.ssh/id_rsa.pub

# ssh
service ssh start

# run service
su - ducc -c "/usr/local/openjdk-8/bin/java -jar /home/ducc/textimager-rest.jar"