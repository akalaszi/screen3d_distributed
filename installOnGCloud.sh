#Copies ChemAxon license to all nodes.

h_lic_dir=/home/hadoop/.chemaxon
home=/home/adminuser

for node in `gcutil listinstances --format=csv --columns name | grep -v name`
do
   echo $node
   gcutil push $node $home/.chemaxon/license.cxl $home
   gcutil ssh $node "sudo mkdir $h_lic_dir; sudo cp $home/license.cxl $h_lic_dir; sudo chown -R hadoop $h_lic_dir; sudo chgrp -R hadoop $h_lic_dir;"
   echo
done

#Build project
mvn clean install

#Push built artifact on the master node.
master_node=`gcutil listinstances --format=csv --columns name | grep [-]m[-]`

gcutil push $master_node target/screen3d_distributed-1.0-SNAPSHOT-job.jar $home 