= Objective =
As a developer, I want my development environment setup to be easy and automatic, regardless my working operating system being Windows / Linux / MacOS. 

= Architecture =
== Docker + VagrantDocker ==
 * Vagrant is just a Docker wrapper on systems that support Docker natively while it spins up a « host VM » to run containers on systems that don’t support it. User don’t have to bother wether Docker is supported natively or not : the same configuration will work on every OS.
 * Docker hosts are not limited to a container (a Virtualbox image of Tiny Core Linux) but Debian, Ubuntu, CoreOS and other Linux distros are supported too. And can run can run on more stable VM managers than Virtualbox (e.g. VMWare).
 * Vagrant can orchestrate Docker containers: run multiple containers concurrently and link them together
 * (actually libcontainer which is a Docker module) still requires Linux kernel 3.8 or higher and x86_64 architecture. This bounds considerably the environments Docker can natively run on.
 
 Vagrant supports Docker both as provider and reuse the same Dockerfile on different Docker containers to execute services.
 
 Vagrantfile
 
 Vagrantfiles describe Vagrant boxes.
 

