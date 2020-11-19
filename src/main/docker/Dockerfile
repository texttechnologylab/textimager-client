# Dockerfile to set up TextImager Client Service

FROM openjdk:8-jdk

# install needed software
RUN apt-get update && apt-get install -y openssh-server less dnsutils vim

# add local ducc user
RUN useradd -m ducc -s /bin/bash -d /home/ducc

# copy ssh key
# TODO this needs to be the same for head node and agents at the moment
COPY ./src/main/docker/id_rsa* /home/ducc/.ssh/

# allow login using this key
RUN cp /home/ducc/.ssh/id_rsa.pub /home/ducc/.ssh/authorized_keys

# disable required user input at first connect
RUN echo "StrictHostKeyChecking=no" > /home/ducc/.ssh/config

# use the same key for root user
RUN cp -Rf /home/ducc/.ssh/ /root/
RUN chown -Rf root.root /root/.ssh/

# copy needed files...
COPY ./target/textimager-rest.jar /home/ducc/

# run script
COPY ./src/main/docker/run.sh /home/ducc/

# set permission of copied files
RUN chown -Rf ducc.ducc /home/ducc/

CMD ["/bin/bash", "/home/ducc/run.sh"]
