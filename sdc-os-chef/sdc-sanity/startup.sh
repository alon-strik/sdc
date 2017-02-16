#!/bin/sh

export CHEFNAME=${ENVNAME}
cd /root/chef-solo
chef-solo -c solo.rb -E ${CHEFNAME}

rc=$?

if [[ $rc != 0 ]]; then
   echo "Sanity failed !!!"
   exit $rc
else
   echo "completed successfully :-)"
   exit 0
fi

#/docker-entrypoint.sh 
