# A template Dockerfile for all demonstrating java program using perf tools
#
#
FROM mcfongtw/ubuntu_perf_openjdk:latest

MAINTAINER Michael Fong <mcfong.open@gmail.com>

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]

WORKDIR /workspace

####################################################### 
#FIXME: The perf installation did not propagated properly from upstream. Remove this once resolved.
#
# perf related
#
RUN apt-get update && \ 
        apt-get install -y linux-tools-common linux-tools-generic linux-tools-`uname -r` 
#
# Set permission to collect perf stat
# -1 - Not paranoid at all
RUN echo 'kernel.perf_event_paranoid = -1' > /etc/sysctl.d/perf.conf
####################################################### 
# Git clone from example
RUN git clone --depth=1 https://github.com/mcfongtw/StudyNotesOnJavaPerformance /workspace/StudyNotesOnJavaPerformance

WORKDIR /workspace/StudyNotesOnJavaPerformance/examples/root

ENV SDKMAN_DIR /workspace/.sdkman

RUN bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh &&  mvn clean package -DskipTests"

WORKDIR /workspace
####################################################### 
# Setup entry point
COPY docker-entrypoint.sh /workspace/docker-entrypoint.sh

RUN chmod +x /workspace/docker-entrypoint.sh

ENTRYPOINT ["/workspace/docker-entrypoint.sh"]

####################################################### 
COPY docker-compose.yml /vagrant/docker-compose.yml

####################################################### 
# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

