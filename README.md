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

Run
---
hadoop jar screen3d_distributed-1.0-SNAPSHOT-job.jar <parameters>