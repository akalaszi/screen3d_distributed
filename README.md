screen3d_distributed
====================

Hadoop based distribution of screen3D


Install ChemAxon dependencies manually:
---------------------------------------
http://www.chemaxon.com/download/marvin-suite/ select:  Platform independent (.zip)
extract zip 
launch:
	./installChemAxonDeps.sh <path-to-marvinbeans-home> 


Build
-----
mvn install

Setup hadoop environment
---
1. have linux: 
	I used Ubuntu 14.04 virual box image from here:
	http://virtualboximages.com/Ubuntu+14.04+LTS+i386+Desktop+VirtualBox+VDI+Virtual+Computer

2. set environment variables:
	export JAVA_HOME=/usr/lib/jdk1.7.0_71
	export HADOOP_PREFIX=/usr/lib/hadoop-2.5.2
	export HADOOP_HOME=$HADOOP_PREFIX
	export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_PREFIX/lib/native/
	export HADOOP_OPTS=-Djava.library.path=$HADOOP_PREFIX/lib
	export PATH=$JAVA_HOME/bin:$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
	export HADOOP_LOG_DIR=~/hadoop_log
		
3. install hadoop:
	http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html

4. spin up the single node hadoop cluster for testing
	hdfs namenode -format
	start-dfs.sh


-type:
hadoop jar screen3d_distributed-1.0-SNAPSHOT-job.jar <parameters>