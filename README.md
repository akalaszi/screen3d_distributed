screen3d_distributed
====================
[Screen3d](http://www.chemaxon.com/products/screen "Official Screen3d docs") is a command line tool from ChemAxon, that can align pair of molecules. This project contains Hadoop mapreduce implementation, built on the top of Screen3D Java API, to run molecular pair overlay calculations on 1000s of computer nodes. The approach was tested on the Google compute engine, which contains easy to use, preconfigured Hadoop worker and master nodes.
On Google compute engine 4000 nodes can perform ~25 million comparison in an hour for ~500USD. 

The mapreduce job runs from  command line
* The input is a molecule file in one of the standard molecule file formats.
* The output is the all possible molecule pairs taken from the input file, aligned and written into an sdf file along with the similarity 3D Tanimoto score.

Requirements
------------ 
* Java
* Maven
* Linux (cygwin) I used Ubuntu 14.04 virual box image from [here] (http://virtualboximages.com/Ubuntu+14.04+LTS+i386+Desktop+VirtualBox+VDI+Virtual+Computer).
* Install ChemAxon dependencies into your local maven repository:
	1. [Download](http://www.chemaxon.com/download/jchem-suite/) Platform independent JChem zip
	2. extract JChem zip  
	3. launch:	`./installChemAxonDeps.sh path-to-jchem-home jchem-version`

Build
-----
mvn clean install

Setup hadoop environment
---
1. set environment variables:

    ```bash
    export JAVA_HOME=/usr/lib/jdk1.7.0_71
    export HADOOP_PREFIX=/usr/lib/hadoop-2.5.2
    export HADOOP_HOME=$HADOOP_PREFIX
    export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_PREFIX/lib/native/
    export HADOOP_OPTS=-Djava.library.path=$HADOOP_PREFIX/lib
    export PATH=$JAVA_HOME/bin:$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
    export HADOOP_LOG_DIR=~/hadoop_log
    ```	

2. install hadoop:
	
    	http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SingleCluster.html

Local single node test cluster
-----
1. spin up the single node Hadoop cluster for testing

	```
	hdfs namenode -format
	start-dfs.sh
	```
2. copy file to the hdfs:

	```
	hdfs dfs -mkdir /input
	hdfs dfs -put ./examples/io/basic/mols.sdf /input/
	```
3. run:

	```
	hadoop jar screen3d_distributed-1.0-SNAPSHOT-job.jar /input/mols.sdf /output
	```

Google compute engine
---
* set up the Hadoop cluster
* copy the compiled screen3d_distributed-1.0-SNAPSHOT-job.jar and the input file to the project
* install gcloud cli locally.
* copy your local ChemAxon license file to the nodes in the cloud: `./installChemAxonLicense.sh`
	
* To run on master node (example)
	
	 ```
	gcloud compute instances list
	gcloud compute ssh --zone=us-central1-a hadoop-m-5ar2
	hdfs dfs -get /screen3d_distributed-1.0-SNAPSHOT-job.jar
	```

* preprocess input: 2D -> 3D conversion, standardization of structures.

	```
	hadoop jar screen3d_distributed-1.0-SNAPSHOT-job.jar p NCI-merged-all.sdf /out
	```

* run screen
	
	```
	hadoop jar screen3d_distributed-1.0-SNAPSHOT-job.jar s NCI-merged-all-3d.sdf /out
	```
	