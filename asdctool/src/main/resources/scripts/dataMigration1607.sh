#!/bin/bash

##############################
# Data Migration 1607
##############################

CURRENT_DIR=`pwd`
BASEDIR=$(dirname $0)

if [ ${BASEDIR:0:1} = "/" ]
then
                FULL_PATH=$BASEDIR
else
                FULL_PATH=$CURRENT_DIR/$BASEDIR
fi

source ${FULL_PATH}/baseOperation.sh

mainClass="org.openecomp.sdc.asdctool.main.MigrationMenu"

command="java $JVM_LOG_FILE -cp $JARS $mainClass migrate-1604-1607 $@"
echo $command

$command
result=$?

echo "***********************************"
echo "***** $result *********************"
echo "***********************************"

exit $result


