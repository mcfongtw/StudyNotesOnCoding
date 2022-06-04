# A template Dockerfile for all program demonstration
#
#
FROM mcfongtw/phusion_perf_openjdk:latest

MAINTAINER Michael Fong <mcfong.open@gmail.com>

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]

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
WORKDIR /vagrant/StudyNotesOnJavaPerformance/

ENV SDKMAN_DIR /workspace/.sdkman

COPY docker-mvn-build.sh /vagrant/StudyNotesOnJavaPerformance/examples/root

WORKDIR /vagrant/StudyNotesOnJavaPerformance/examples/

####################################################### 
# Setup POC program entry point
COPY docker-entrypoint.sh /workspace/docker-entrypoint.sh

RUN chmod +x /workspace/docker-entrypoint.sh

ENTRYPOINT ["/workspace/docker-entrypoint.sh"]

####################################################### 
# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
