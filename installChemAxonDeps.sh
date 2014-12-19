#!/bin/bash

set -e

echo "This command line tool installs ChemAxon jars into the local Maven repository."
echo "Maven is required."
if [ -z $1 ] 
then
	echo "	1st parameter: path to the marvinbeans install dir"
	echo "	2nd parameter: marvin version /optional/"
	exit
fi


cd $1
version=$2

if [ -z $version ]
then
    version=`grep Version help/applications/cxcalc-calculations.html | sed -e s/["h3"\|\<\|\>\|A-Z\|a-z\|\/\|[:space:]]//g`
fi

echo This will install ChemAxon jars with version $version to the local maven repository.

for f in `ls lib/com.chemaxon*`
do
	echo
	echo "======  install: $f ======"	
    artifact=`echo $f | sed -e s/'lib\/com\.chemaxon\-'// | sed -e s/'\.jar'//`
    mvn install:install-file -Dfile=$f -DgroupId=com.chemaxon -DartifactId=$artifact -Dversion=$version -Dpackaging=jar
done
