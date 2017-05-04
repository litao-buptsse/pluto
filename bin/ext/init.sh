#!/bin/bash

if [ $# -ne 2 ]; then
  echo "usage: $0 <jobId> <tarLocation>"
  exit 1
fi

jobId=$1
tarLocation=$2

dataDir=/search/ted/pluto/data
jobHomeDir=$dataDir/$jobId

if [ -d $jobHomeDir ]; then
  rm -fr $jobHomeDir
fi
mkdir -p $jobHomeDir

hadoop fs -get $tarLocation $jobHomeDir
tarName=`basename $tarLocation`
mkdir -p $jobHomeDir/container
tar -xvf $jobHomeDir/$tarName --strip-components=1 -C $jobHomeDir/container
rm -f $jobHomeDir/$tarName
mkdir -p $jobHomeDir/logs